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

package org.onap.cps.ncmp.dmi.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.cps.ncmp.dmi.config.DmiPluginConfig
import org.onap.cps.ncmp.dmi.exception.CmHandleRegistrationException
import org.onap.cps.ncmp.dmi.exception.DmiException
import org.onap.cps.ncmp.dmi.exception.ModulesNotFoundException
import org.onap.cps.ncmp.dmi.service.client.NcmpRestClient
import org.onap.cps.ncmp.dmi.service.operation.SdncOperations
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class DmiServiceImplSpec extends Specification {


    def mockNcmpRestClient = Mock(NcmpRestClient)
    def mockDmiPluginProperties = Mock(DmiPluginConfig.DmiPluginProperties)
    def objectMapper = new ObjectMapper()
    def mockObjectMapper = Mock(ObjectMapper)
    def mockSdncOperations = Mock(SdncOperations)
    def objectUnderTest = new DmiServiceImpl(mockDmiPluginProperties, mockNcmpRestClient, objectMapper, mockSdncOperations)

    def 'Call get modules for cm-handle on dmi Service.'() {
        given: 'cm handle id'
            def cmHandle = 'node1'
        and: 'request operation returns OK'
            def body = 'body'
            mockSdncOperations.getModulesFromNode(cmHandle) >> new ResponseEntity<String>(body, HttpStatus.OK)
        when: 'get modules for cm-handle is called'
            def result = objectUnderTest.getModulesForCmHandle(cmHandle)
        then: 'result is equal to the response from the SDNC service'
            result == body
    }

    def 'Call get modules for cm-handle and SDNC returns "bad request" status.'() {
        given: 'cm handle id'
            def cmHandle = 'node1'
        and: 'get modules from node returns "bad request" status'
            mockSdncOperations.getModulesFromNode(cmHandle) >> new ResponseEntity<String>('body', HttpStatus.BAD_REQUEST)
        when: 'get modules for cm-handle is called'
            objectUnderTest.getModulesForCmHandle(cmHandle)
        then: 'dmi exception is thrown'
            thrown( DmiException )
    }

    def 'Call get modules for cm-handle and SDNC returns OK with empty body.'() {
        given: 'cm handle id'
            def cmHandle = 'node1'
        and: 'get modules for cm-handle returns OK with empty body'
            mockSdncOperations.getModulesFromNode(cmHandle) >> new ResponseEntity<String>('', HttpStatus.OK)
        when: 'get modules for cm-handle is called'
            objectUnderTest.getModulesForCmHandle(cmHandle)
        then: 'ModulesNotFoundException is thrown'
            thrown( ModulesNotFoundException )
    }

    def 'Register cm handles with ncmp.'() {
        given: 'cm-handle list and json payload'
            def cmHandlesList = ['node1', 'node2']
            def json =   '{"dmiPlugin":"test-dmi-service","createdCmHandles":[{"cmHandle":"node1"},{"cmHandle":"node2"}]}'
        and: 'register cm handle with ncmp return "created" status'
            mockNcmpRestClient.registerCmHandlesWithNcmp(json) >> new ResponseEntity<>(HttpStatus.CREATED)
        and: 'mockDmiPluginProperties returns test-dmi-service'
            mockDmiPluginProperties.getDmiServiceName() >> "test-dmi-service"
        when: 'register cm handles service method called'
            objectUnderTest.registerCmHandles(cmHandlesList)
        then: 'no exception is thrown'
            noExceptionThrown()
    }

    def 'Register cm handles with ncmp called with exception #scenario.'() {
        given: 'cm-handle list'
            def cmHandlesList = ["node1", "node2"]
        and: 'json payload to verify ncmp client payload'
            def json =   '{"dmiPlugin":"test-dmi-service","createdCmHandles":[{"cmHandle":"node1"},{"cmHandle":"node2"}]}'
        and: 'dmi plugin service name is "test-dmi-service"'
            mockDmiPluginProperties.getDmiServiceName() >> "test-dmi-service"
        and: 'ncmp rest client returns #responseEntity'
            mockNcmpRestClient.registerCmHandlesWithNcmp(json.strip()) >> responseEntity
        when: 'register cm handles service method called'
            objectUnderTest.registerCmHandles(cmHandlesList)
        then: 'threw CmHandleRegistrationException'
            thrown(CmHandleRegistrationException.class)
        where: 'given #scenario'
            scenario                                        |   responseEntity
            'ncmp rest client returns bad request'          |   new ResponseEntity<>(HttpStatus.BAD_REQUEST)
            'ncmp rest client returns internal server error'|   new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    def 'Register cm handles with ncmp with wrong data.'() {
        given: 'objectMapper mock and cm-handle list'
            def cmHandlesList = ["node1", "node2"]
        and: 'objectMapper returns "JsonProcessingException" during parse'
            objectUnderTest.objectMapper = mockObjectMapper
            mockObjectMapper.writeValueAsString(_) >> { throw new JsonProcessingException("ex") }
        when: 'register cm handles service method called'
            objectUnderTest.registerCmHandles(cmHandlesList)
        then: 'threw dmi exception'
            thrown(DmiException.class)
    }
}
