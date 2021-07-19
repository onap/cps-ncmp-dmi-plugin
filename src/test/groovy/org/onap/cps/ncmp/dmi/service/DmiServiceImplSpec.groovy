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

    def 'Retrieve Hello World'() {
        expect: 'Hello World is Returned'
            objectUnderTest.getHelloWorld() == 'Hello World'
    }

    def 'Call getModulesForCmhandle with valid params.'() {

        given: 'cm handle id , requestoperation'
            def cmHandle = "node1"
            mockSdncOperations.getModulesFromNode(cmHandle) >> responseEntity

        when: 'getModulesForCmhandle is called'
            def optional = objectUnderTest.getModulesForCmhandle(cmHandle)

        then:
            optional.isEmpty() == expected

        where:
            scenario                               |   responseEntity                                              ||  expected
            'sdncoperation returns OK'             |   new ResponseEntity<String>("body", HttpStatus.OK)           ||  false
            'sdncoperation return BAD REQUEST'     |   new ResponseEntity<String>("body", HttpStatus.BAD_REQUEST)  ||  true
    }
}
