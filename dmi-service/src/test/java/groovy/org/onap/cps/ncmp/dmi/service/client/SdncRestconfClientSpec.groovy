/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2022 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package groovy.org.onap.cps.ncmp.dmi.service.client

import org.onap.cps.ncmp.dmi.config.DmiConfiguration
import org.onap.cps.ncmp.dmi.service.client.SdncRestconfClient
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

import static org.springframework.http.HttpMethod.*

class SdncRestconfClientSpec extends Specification {

    def mockSdncProperties = Mock(DmiConfiguration.SdncProperties)
    def mockRestTemplate = Mock(RestTemplate)
    def objectUnderTest = new SdncRestconfClient(mockSdncProperties, mockRestTemplate)

    def 'SDNC GET operation.'() {
        given: 'a get resource url'
            def getResourceUrl = '/getResourceUrl'
        and: 'test configuration data'
            setupTestConfigurationData()
        and: 'the process returns a valid response entity'
            def mockResponseEntity = Mock(ResponseEntity)
            mockRestTemplate.exchange({ it.toString() == 'http://some-uri/getResourceUrl' },
                    HttpMethod.GET, _ as HttpEntity, String.class) >> mockResponseEntity
        when: 'the resource is fetched'
            def result = objectUnderTest.getOperation(getResourceUrl)
        then: 'the output of the method is equal to the output from the rest template service'
            result == mockResponseEntity
    }

    def 'SDNC #scenario operation called.'() {
        given: 'some request data'
            def someRequestData = 'some request data'
        and: 'a url for get module resources'
            def getModuleResourceUrl = '/getModuleResourceUrl'
        and: 'test configuration data'
            setupTestConfigurationData()
        and: 'the process returns a valid response entity'
            def mockResponseEntity = Mock(ResponseEntity)
        when: 'the resource is fetched'
            def result = objectUnderTest.httpOperationWithJsonData(expectedHttpMethod, getModuleResourceUrl, someRequestData, new HttpHeaders())
        then: 'the rest template is called with the correct uri and json in the body'
            1 * mockRestTemplate.exchange({ it.toString() == 'http://some-uri/getModuleResourceUrl' },
                    expectedHttpMethod, { it.body.contains(someRequestData) }, String.class) >> mockResponseEntity
        and: 'the output of the method is the same as the output from the rest template service'
            result == mockResponseEntity
        where: 'the following values are used'
            scenario || expectedHttpMethod
            'POST'   || POST
            'PUT'    || PUT
            'GET'    || GET
            'DELETE' || DELETE
    }

    def 'SDNC GET operation with headers.'() {
        given: 'a get url'
            def getResourceUrl = '/getResourceUrl'
        and: 'test configuration data'
            setupTestConfigurationData()
        and: 'the process returns a valid response entity'
            def mockResponseEntity = Mock(ResponseEntity)
            mockRestTemplate.exchange({ it.toString() == 'http://some-uri/getResourceUrl' },
                    HttpMethod.GET, _ as HttpEntity, String.class) >> mockResponseEntity
        when: 'the resource is fetched with headers'
            def result = objectUnderTest.getOperation(getResourceUrl, new HttpHeaders())
        then: 'the output of the method is equal to the output from the rest template service'
            result == mockResponseEntity
    }

    def setupTestConfigurationData() {
        mockSdncProperties.baseUrl >> 'http://some-uri'
        mockSdncProperties.authUsername >> 'some-username'
        mockSdncProperties.authPassword >> 'some-password'
        mockSdncProperties.topologyId >> 'some-topology-id'
    }
}