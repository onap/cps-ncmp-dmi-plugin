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

import org.onap.cps.ncmp.dmi.service.client.NcmpRestClient
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class DmiServiceImplSpec extends Specification {

    def objectUnderTest = new DmiServiceImpl()

    def mockNcmpRestClient = Mock(NcmpRestClient)

    def setup() {
        objectUnderTest.ncmpRestClient = mockNcmpRestClient
    }

    def 'Register cm handles with ncmp.'() {
        def jsonString = "this Json"

        given: "ncmpRestClient mocks returning response entity"
            mockNcmpRestClient.registerCmHandlesWithNcmp(jsonString) >> responseEntity

        when: "registerCmHandles service method called"
            def response = objectUnderTest.registerCmHandles(jsonString)

        then: "returns expected result"
            response == expectedresult

        where: 'given response entity'
                casedetector                               |   responseEntity                                   ||     expectedresult
                'response entity is ok'                    |   new ResponseEntity<>(HttpStatus.OK)              ||     true
                'response entity is created'               |   new ResponseEntity<>(HttpStatus.CREATED)         ||     true
                'response entity is bad request'           |   new ResponseEntity<>(HttpStatus.BAD_REQUEST)     ||     false
    }
}
