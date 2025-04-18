/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2025 OpenInfra Foundation Europe. All rights reserved.
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
        given: 'some request data'
            def someRequestData = 'some request data'
        and: 'configuration data'
            mockCpsProperties.baseUrl >> 'http://some-uri'
            mockCpsProperties.dmiRegistrationUrl >> 'some-url'
        and: 'the rest template returns a valid response entity'
            def mockResponseEntityFromRestTemplate = Mock(ResponseEntity)
        when: 'registering a cm handle'
            def result = objectUnderTest.registerCmHandlesWithNcmp(someRequestData)
        then: 'the rest template is called with the correct uri and original request data in the body'
            1 * mockRestTemplate.exchange({ it.toString() == 'http://some-uri/some-url' },
                    HttpMethod.POST, { it.body.contains(someRequestData) }, String.class) >> mockResponseEntityFromRestTemplate
        and: 'the output of the method is equal to the output from the rest template service'
            result == mockResponseEntityFromRestTemplate
    }

    def 'Enable data sync for a cm handle identifier.'() {
        given: 'some cm handle id'
            def someCmHandleId = 'some-cm-handle-id'
        and: 'configuring the url to enable data sync'
            mockCpsProperties.baseUrl >> 'http://my-base-uri'
            mockCpsProperties.dataSyncEnabledUrl >> 'datasync-url/{test-parameter}/data-sync?dataSyncEnabled=true'
        and: 'the rest template returns a response entity'
            def mockResponseEntityFromRestTemplate = Mock(ResponseEntity)
        when: 'enabling the data sync flag'
            def result = objectUnderTest.enableDataSyncFlagWithNcmp(someCmHandleId)
        then: 'the rest template is called with the correct uri'
            mockRestTemplate.exchange({ it.toString() == 'http://my-base-uri/datasync-url/some-cm-handle-id/data-sync?dataSyncEnabled=true' },
                HttpMethod.PUT, _, String.class) >> mockResponseEntityFromRestTemplate
        and: 'the output of the method is equal to the output from the rest template service'
            assert result == mockResponseEntityFromRestTemplate
    }
}