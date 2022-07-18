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
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class NcmpRestClientSpec extends Specification {
    def objectUnderTest = new NcmpRestClient(mockCpsProperties, mockRestTemplate)
    def mockCpsProperties = Mock(DmiConfiguration.CpsProperties)
    def mockRestTemplate = Mock(RestTemplate)

    def setup() {
        objectUnderTest.cpsProperties = mockCpsProperties
        objectUnderTest.restTemplate = mockRestTemplate
    }

    def 'Register a cm handle.'() {
        given: 'json data'
            def jsonData = 'some json'
        and: 'configuration data'
            mockCpsProperties.baseUrl >> 'http://some-uri'
            mockCpsProperties.dmiRegistrationUrl >> 'some-url'
            mockCpsProperties.authUsername >> 'some-username'
            mockCpsProperties.authPassword >> 'some-password'
        and: 'the rest template returns a valid response entity'
            def mockResponseEntity = Mock(ResponseEntity)
        when: 'method is called'
            def result = objectUnderTest.registerCmHandlesWithNcmp(jsonData)
        then: 'the rest template is called with the correct uri and json in the body'
            1 * mockRestTemplate.exchange({ it.toString() == 'http://some-uri/some-url' },
                    HttpMethod.POST, { it.body.contains(jsonData) }, String.class) >> mockResponseEntity
        and: 'the output of the method is equal to the output from the test template'
            result == mockResponseEntity
    }
}