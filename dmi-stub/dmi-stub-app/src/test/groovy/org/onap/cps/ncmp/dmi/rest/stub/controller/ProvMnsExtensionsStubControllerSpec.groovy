/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2026 OpenInfra Foundation Europe. All rights reserved.
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

package org.onap.cps.ncmp.dmi.rest.stub.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.cps.ncmp.dmi.rest.stub.utils.Sleeper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.validation.Validator
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@WebMvcTest(controllers = ProvMnSExtensionsStubController)
class ProvMnsStubControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    Sleeper sleeper = Mock()

    @Autowired
    ObjectMapper objectMapper

    @SpringBean
    Validator validator = Mock()

    def 'ProvMnSExtensions POST request with #scenario.'() {
        given: 'url and some resource as body'
            def url = "/ProvMnSExtensions/v1/actions/someSegment=1/otherSegment=2/${segmentName}=${segmentValue}/finalSegment"
        when: 'post request is executed'
            def response = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content('{}'))
                .andReturn().response
        then: 'response status is #expectedHttpStatus'
            assert response.status == expectedHttpStatus.value()
        and: 'content contains the expected json (snippet)'
            assert response.getContentAsString().contains(expectedContentSnippet)
        where: 'following simulations are applied'
            scenario        | segmentName      | segmentValue     || expectedHttpStatus       || expectedContentSnippet
            'no simulation' | 'anotherSegment' | 'some value'     || HttpStatus.OK            || '"id":"myId"'
            'delay 1'       | 'dmiSimulation'  | 'slowResponse_1' || HttpStatus.OK            || '"id":"myId"'
            'http error'    | 'dmiSimulation'  | 'httpError_418'  || HttpStatus.I_AM_A_TEAPOT || '"status":"418"'
    }

}
