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

package org.onap.cps.ncmp.dmi.rest.controller


import org.onap.cps.ncmp.dmi.exception.DmiException
import org.onap.cps.ncmp.dmi.service.DmiService
import org.springframework.http.MediaType

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

import org.spockframework.spring.SpringBean
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
class DmiRestControllerSpec extends Specification {

    @SpringBean
    DmiService mockDmiService = Mock()

    @Autowired
    private MockMvc mvc

    @Value('${rest.api.dmi-base-path}')
    def basePath

    def basePathV1

    def setup(){
        basePathV1 = "$basePath/v1"
    }

    def 'Get Hello World'() {
        given: 'hello world endpoint'
            def helloWorldEndpoint = "$basePath/v1/helloworld"

        when: 'get hello world api is invoked'
            def response = mvc.perform(
                                    get(helloWorldEndpoint)
                           ).andReturn().response

        then: 'Response Status is OK and contains expected text'
            response.status == HttpStatus.OK.value()
        then: 'the java API was called with the correct parameters'
            1 * mockDmiService.getHelloWorld()
    }

    def 'Get all moduels for given cm handle with scenarios.'() {

        given: 'url and request body'
            def getModuleUrl = "$basePathV1/ch/node1/modules"

            def jsonBody = "some json"

            mockDmiService.getModulesForCmHandle(_ as String) >> responseFromService

        when: 'post is being called'
            def response = mvc.perform( post(getModuleUrl)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(jsonBody)).andReturn().response

        then: 'response should match'
            response.status == expectedResponse

        where:
            scenario                       |  responseFromService                                || expectedResponse
            'valid response body'          |  Optional.of("{json}")                              || HttpStatus.OK.value()
            'empty response body'          |  Optional.empty()                                   || HttpStatus.NOT_FOUND.value()

    }

    def 'Get all moduels for given cm handle threw excpetion.'() {

        given: 'url and request body'
        def getModuleUrl = "$basePathV1/ch/node1/modules"

        def jsonBody = "some json"

        mockDmiService.getModulesForCmHandle(_ as String) >> { throw new DmiException("message", "detail") }

        when: 'post is being called'
        def response = mvc.perform( post(getModuleUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody)).andReturn().response

        then: 'response should be internal server error'
            response.status == HttpStatus.INTERNAL_SERVER_ERROR.value()
    }
}
