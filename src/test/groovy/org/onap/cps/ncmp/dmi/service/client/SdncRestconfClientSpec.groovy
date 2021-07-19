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
import org.springframework.http.HttpMethod
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
            mockSdncProperties.baseUrl >> 'http://test-sdnc-uri'
            mockSdncProperties.authUsername >> 'test-username'
            mockSdncProperties.authPassword >> 'test-password'
            mockSdncProperties.topologyId >> 'testTopologyId'
        and: 'the rest template returns a valid response entity'
            def mockResponseEntity = Mock(ResponseEntity)
            mockRestTemplate.getForEntity({ it.toString() == 'http://test-sdnc-uri/getResourceUrl' }, String.class, _ as HttpEntity) >> mockResponseEntity
        when: 'GET operation is invoked'
            def result = objectUnderTest.getOperation(getResourceUrl)
        then: 'the output of the method is equal to the output from the test template'
            result == mockResponseEntity
    }
}