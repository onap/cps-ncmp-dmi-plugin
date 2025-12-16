/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2025 OpenInfra Foundation Europe. All rights reserved.
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
import org.onap.cps.ncmp.dmi.provmns.model.ResourceOneOf
import org.onap.cps.ncmp.dmi.rest.stub.utils.Sleeper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.validation.Validator
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put

@WebMvcTest(controllers = ProvMnsStubController)
class ProvMnsStubControllerSpec extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    Sleeper sleeper = Mock()

    @Autowired
    ObjectMapper objectMapper

    @SpringBean
    Validator validator = Mock()


    def 'ProvMnS GET request with #scenario.'() {
        given: 'url'
            def url = "/ProvMnS/v1/someSegment=1/otherSegment=2/${segmentName}=${segmentValue}/finalSegment=3"
        when: 'get request is executed'
            def response = mockMvc.perform(get(url)).andReturn().response
        then: 'response status is #expectedHttpStatus'
            assert response.status == expectedHttpStatus.value()
        where: 'following simulation are applied'
            scenario        | segmentName      | segmentValue     || expectedHttpStatus
            'no simulation' | 'anotherSegment' | 'some value'     || HttpStatus.OK
            'delay 1'       | 'dmiSimulation'  | 'slowResponse_1' || HttpStatus.OK
            'http error'    | 'dmiSimulation'  | 'httpError_418'  || HttpStatus.I_AM_A_TEAPOT
    }

    def 'ProvMnS PUT request with #scenario.'() {
        given: 'url and some resource as body'
            def url = "/ProvMnS/v1/someSegment=1/otherSegment=2/${segmentName}=${segmentValue}/finalSegment=3"
            def requestBody = objectMapper.writeValueAsString(new ResourceOneOf('someId'))
        when: 'put request is executed'
            def response = mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andReturn().response
        then: 'response status is #expectedHttpStatus'
            assert response.status == expectedHttpStatus.value()
        where: 'following simulation are applied'
            scenario        | segmentName      | segmentValue     || expectedHttpStatus
            'no simulation' | 'anotherSegment' | 'some value'     || HttpStatus.OK
            'delay 1'       | 'dmiSimulation'  | 'slowResponse_1' || HttpStatus.OK
            'http error'    | 'dmiSimulation'  | 'httpError_418'  || HttpStatus.I_AM_A_TEAPOT
    }

    def 'ProvMnS PATCH request with #scenario.'() {
        given: 'url and some resource as body'
            def url = "/ProvMnS/v1/someSegment=1/otherSegment=2/${segmentName}=${segmentValue}/finalSegment=3"
            def requestBody = objectMapper.writeValueAsString(new ResourceOneOf('someId'))
        when: 'patch request is executed'
            def response = mockMvc.perform(patch(url)
                .contentType("application/json-patch+json")
                .content(requestBody))
                .andReturn().response
        then: 'response status is #expectedHttpStatus'
            assert response.status == expectedHttpStatus.value()
        where: 'following simulation are applied'
            scenario        | segmentName      | segmentValue     || expectedHttpStatus
            'no simulation' | 'anotherSegment' | 'some value'     || HttpStatus.OK
            'delay 1'       | 'dmiSimulation'  | 'slowResponse_1' || HttpStatus.OK
            'http error'    | 'dmiSimulation'  | 'httpError_418'  || HttpStatus.I_AM_A_TEAPOT
    }

    def 'ProvMnS DELETE request with #scenario.'() {
        given: 'url'
            def url = "/ProvMnS/v1/someSegment=1/otherSegment=2/${segmentName}=${segmentValue}/finalSegment=3"
        when: 'delete request is executed'
            def response = mockMvc.perform(delete(url)).andReturn().response
        then: 'response status is #expectedHttpStatus'
            assert response.status == expectedHttpStatus.value()
        where: 'following simulation are applied'
            scenario        | segmentName      | segmentValue     || expectedHttpStatus
            'no simulation' | 'anotherSegment' | 'some value'     || HttpStatus.OK
            'delay 1'       | 'dmiSimulation'  | 'slowResponse_1' || HttpStatus.OK
            'http error'    | 'dmiSimulation'  | 'httpError_418'  || HttpStatus.I_AM_A_TEAPOT
    }

}
