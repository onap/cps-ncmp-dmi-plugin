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
import org.spockframework.spring.SpringBean
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class DmiServiceImplSpec extends Specification {

    def objectUnderTest = new DmiServiceImpl()

    @SpringBean
    def mockNcmpRestClient = Mock(NcmpRestClient)

    def setup() {
        objectUnderTest.ncmpRestClient = mockNcmpRestClient
    }

    def 'Retrieve Hello World'() {
        expect: 'Hello World is Returned'
            objectUnderTest.getHelloWorld() == 'Hello World'
    }

    def 'register cmHandles ncmp rest client invoked correctly'() {
        def cmHandles = new CmHandles()
        def jsonString = "this Json"

        given: "cmhandles with valid json string"
            ObjectMapper objectMapperStub = Stub(ObjectMapper.class)
            objectUnderTest.mapper = objectMapperStub
            objectMapperStub.writeValueAsString(cmHandles) >> jsonString

        when: "service method for register cmhandles called"
           objectUnderTest.registerCmHandles(cmHandles)

        then: "ncmpRestclient registerCmHandlesWithNcmp called once"
            1 * mockNcmpRestClient.registerCmHandlesWithNcmp(jsonString)
    }

    def 'register cmHandles service called correctly and returning OK status'() {
        def cmHandles = new CmHandles()
        def jsonString = "this Json"

        given: "ncmpRestClient, objectMapper stub"
            ObjectMapper objectMapperStub = Stub(ObjectMapper.class)
            objectUnderTest.mapper = objectMapperStub
            NcmpRestClient ncmpRestClientStub = Stub(NcmpRestClient.class)
            objectUnderTest.ncmpRestClient = ncmpRestClientStub

            objectMapperStub.writeValueAsString(cmHandles) >> jsonString
            ncmpRestClientStub.registerCmHandlesWithNcmp(jsonString) >> new ResponseEntity<String>(HttpStatus.OK)

        when: "registerCmHandles service method called"
            def response = objectUnderTest.registerCmHandles(cmHandles)

        then: "returns OK status for registration"
            response.statusCodeValue == HttpStatus.OK.value()
    }

    def 'register cmHandles service called and returning BAD_REQUEST status'() {
        def cmHandles = new CmHandles()

        given: "ObjectMapper stub returns parsing exception"
            ObjectMapper objectMapperStub = Stub(ObjectMapper.class)
            objectUnderTest.mapper = objectMapperStub

            objectMapperStub.writeValueAsString(cmHandles) >> { throw new JsonProcessingException("test exception") }

        when: "registerCmHandles service method called"
            def response = objectUnderTest.registerCmHandles(cmHandles)

        then: "returns BAD_REQUEST response"
            response.statusCodeValue == HttpStatus.BAD_REQUEST.value()
    }
}
