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

package org.onap.cps.ncmp.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.cps.dmi.clients.NcmpRestClient
import org.onap.cps.ncmp.rest.model.CmHandles
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class DmiServiceImplSpec extends Specification {

    def objectUnderTest = new DmiServiceImpl()

    def mockNcmpRestClient = Mock(NcmpRestClient)

    def mockObjectMapper = Mock(ObjectMapper)

    def cmHandles;

    def setup() {
        cmHandles = new CmHandles()
        objectUnderTest.objectMapper = mockObjectMapper
        objectUnderTest.ncmpRestClient = mockNcmpRestClient
    }

    def 'Register cm handles with ncmp.'() {
        def jsonString = "this Json"
        def responseEntity = new ResponseEntity<String>(HttpStatus.OK)

        given: "objectMapper, ncmpRestClient mocks returning json string and response entity"
            mockObjectMapper.writeValueAsString(cmHandles) >> jsonString
            mockNcmpRestClient.registerCmHandlesWithNcmp(jsonString) >> responseEntity

        when: "registerCmHandles service method called"
            def response = objectUnderTest.registerCmHandles(cmHandles)

        then: "returns same response entity supplied by client"
            response == responseEntity
    }

    def 'Register cm handles service called and throwing exception.'() {
        given: "objectmapper mock returns parsing exception"
            mockObjectMapper.writeValueAsString(cmHandles) >> { throw new JsonProcessingException("test exception") }

        when: "registerCmHandles service method being called"
            def response = objectUnderTest.registerCmHandles(cmHandles)

        then: "returns bad request response entity"
            response.statusCodeValue == HttpStatus.BAD_REQUEST.value()
    }
}
