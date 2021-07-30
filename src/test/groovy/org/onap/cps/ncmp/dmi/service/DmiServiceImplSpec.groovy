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
import org.onap.cps.ncmp.dmi.exception.DmiException
import org.onap.cps.ncmp.dmi.exception.ModulesNotFoundException
import org.onap.cps.ncmp.dmi.service.models.ModuleData
import org.onap.cps.ncmp.dmi.service.operation.SdncOperations
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class DmiServiceImplSpec extends Specification {

    def objectUnderTest = new DmiServiceImpl()

    def mockSdncOperations = Mock(SdncOperations)

    def setup() {
        objectUnderTest.sdncOperations = mockSdncOperations
    }

    def 'Call getModulesForCmhandle on DmiService.'() {
        given: 'cm handle id'
            def cmHandle = "node1"
        and: 'request operation returns OK'
            def body = "body"
            mockSdncOperations.getModulesFromNode(cmHandle) >> new ResponseEntity<String>(body, HttpStatus.OK)
        when: 'getModulesForCmhandle is called'
            def result = objectUnderTest.getModulesForCmHandle(cmHandle)
        then: 'result is equal to response body'
            result == body
    }

    def 'Call getModulesForCmhandle and SDNC returns BAD_REQUEST.'() {
        given: 'cm handle id'
            def cmHandle = "node1"
        and: 'getModulesFromNode returns BAD_REQUEST'
            mockSdncOperations.getModulesFromNode(cmHandle) >> new ResponseEntity<String>("body", HttpStatus.BAD_REQUEST)
        when: 'getModulesForCmhandle is called'
            objectUnderTest.getModulesForCmHandle(cmHandle)
        then: 'DmiException is thrown'
            thrown(DmiException)
    }

    def 'Call getModulesForCmhandle and SDNC returns OK with empty body.'() {
        given: 'cm handle id'
            def cmHandle = "node1"
        and: 'getModulesFromNode returns OK with empty body'
            mockSdncOperations.getModulesFromNode(cmHandle) >> new ResponseEntity<String>("", HttpStatus.OK)
        when: 'getModulesForCmhandle is called'
            objectUnderTest.getModulesForCmHandle(cmHandle)
        then: 'ModulesNotFoundException is thrown'
            thrown(ModulesNotFoundException)
    }

    def 'Get module resources.'() {
        given: 'request operation for get yang resources is invoked and returns ok'
            mockSdncOperations.getYangResources(_, _) >> new ResponseEntity<String>('some-response-body', HttpStatus.OK)
        when: 'get module resources is invoked with the given cm handle and a module list'
            def modules = new ModuleData()
            modules.namespace >> 'some-namespace'
            modules.name >> 'some-name'
            modules.revision >> 'some-revision'
            def moduleData = [modules] as LinkedList<ModuleData>
            def response = objectUnderTest.getModuleSources('some-cmHandle', moduleData)
        then: 'the response contains the expected response body'
            response == 'some-response-body'
    }

    def 'Get module resources for a failed module request.'() {
        given: 'get yang resources is invoked and returns not found'
            mockSdncOperations.getYangResources(_, _) >> new ResponseEntity<String>('some-response-body', HttpStatus.NOT_FOUND)
        when: 'get module resources is invoked with the given cm handle and a module list'
            def modules = new ModuleData()
            modules.namespace >> 'some-namespace'
            modules.name >> 'some-name'
            modules.revision >> 'some-revision'
            def modulesDataModulesList = [modules] as LinkedList<ModuleData>
            objectUnderTest.getModuleSources('some-cmHandle', modulesDataModulesList)
        then: 'ModulesNotFoundException is thrown'
            thrown(ModulesNotFoundException)
    }
}
