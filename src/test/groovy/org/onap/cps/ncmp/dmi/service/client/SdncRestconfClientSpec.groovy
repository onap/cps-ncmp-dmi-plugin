/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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

package org.onap.cps.ncmp.dmi.service.client

import org.onap.cps.ncmp.dmi.config.DmiConfiguration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class SdncRestconfClientSpec extends Specification {

    def mockSdncProperties = Mock(DmiConfiguration.SdncProperties)
    def mockRestTemplate = Mock(RestTemplate)
    def objectUnderTest = new SdncRestconfClient(mockSdncProperties, mockRestTemplate)

    def 'SDNC GET operation.'() {
        given: 'a get url'
            def getResourceUrl = '/getResourceUrl'
        and: 'sdnc properties'
            setupTestConfigurationData()
        and: 'the rest template returns a valid response entity'
            def mockResponseEntity = Mock(ResponseEntity)
            mockRestTemplate.getForEntity({ it.toString() == 'http://some-uri/getResourceUrl' }, String.class, _ as HttpEntity) >> mockResponseEntity
        when: 'GET operation is invoked'
            def result = objectUnderTest.getOperation(getResourceUrl)
        then: 'the output of the method is equal to the output from the test template'
            result == mockResponseEntity
    }

    def 'SDNC POST operation called.'() {
        given: 'json data'
            def jsonData = 'some-json'
        and: 'a url for get module resources'
            def getModuleResourceUrl = '/getModuleResourceUrl'
        and: 'configuration data'
            setupTestConfigurationData()
        and: 'the rest template returns a valid response entity'
            def mockResponseEntity = Mock(ResponseEntity)
        when: 'get module resources is invoked'
            def result = objectUnderTest.postOperationWithJsonData(getModuleResourceUrl, jsonData, new HttpHeaders())
        then: 'the rest template is called with the correct uri and json in the body'
            1 * mockRestTemplate.postForEntity({ it.toString() == 'http://some-uri/getModuleResourceUrl' },
                    { it.body.contains(jsonData) }, String.class) >> mockResponseEntity
        and: 'the output of the method is the same as the output from the test template'
            result == mockResponseEntity
    }

    def 'SDNC GET operation with header.'() {
        given: 'a get url'
            def getResourceUrl = '/getResourceUrl'
        and: 'sdnc properties'
            setupTestConfigurationData()
        and: 'the rest template returns a valid response entity'
            def mockResponseEntity = Mock(ResponseEntity)
            mockRestTemplate.getForEntity({ it.toString() == 'http://some-uri/getResourceUrl' }, String.class, _ as HttpEntity) >> mockResponseEntity
        when: 'GET operation is invoked'
            def result = objectUnderTest.getOperation(getResourceUrl, new HttpHeaders())
        then: 'the output of the method is equal to the output from the test template'
            result == mockResponseEntity
    }

    def setupTestConfigurationData() {
        mockSdncProperties.baseUrl >> 'http://some-uri'
        mockSdncProperties.authUsername >> 'some-username'
        mockSdncProperties.authPassword >> 'some-password'
        mockSdncProperties.topologyId >> 'some-topology-id'
    }
}