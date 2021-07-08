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

package org.onap.cps.ncmp.rest.controller

import org.springframework.http.MediaType

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

import org.onap.cps.ncmp.service.DmiService
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@WebMvcTest(DmiRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class DmiRestControllerSpec extends Specification {

    @SpringBean
    DmiService mockDmiService = Mock()

    @Autowired
    private MockMvc mvc

    @Value('${rest.api.dmi-base-path}')
    def basePath

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

    def 'post request for register cmhandles success'() {
        given: 'register cmhandles endpoint'
            def registerCmhandlesPost = "$basePath/v1/cmhandles"

        when: 'get register cmhandles post api is invoked'
            def response = mvc.perform(
                    post(registerCmhandlesPost).contentType(MediaType.APPLICATION_JSON)
                            .content("{\n" +
                                    "  \"cm-handles\": [\n" +
                                    "    {\n" +
                                    "      \"id\": \"idval\",\n" +
                                    "      \"dmi-service-name\": \"dminameval\",\n" +
                                    "      \"additional-properties\": [\n" +
                                    "        {\n" +
                                    "          \"name\": \"nameval\",\n" +
                                    "          \"value\": \"valval\"\n" +
                                    "        }\n" +
                                    "      ]\n" +
                                    "    }\n" +
                                    "  ]\n" +
                                    "}\n")
            ).andReturn().response

        then: 'response Status is OK '
            response.status == HttpStatus.OK.value()

        then: 'and service called once'
            1 * mockDmiService.registerCmHandles(_)
    }

    def 'post request for register cmhandles fails for missing required id in content'() {
        given: 'register cmhandles endpoint'
            def registerCmhandlesPost = "$basePath/v1/cmhandles"

        when: 'get register cmhandles post api is invoked with no content'
            def response = mvc.perform(
                    post(registerCmhandlesPost).contentType(MediaType.APPLICATION_JSON)
                            .content(contentValue)
                    ).andReturn().response

        then: 'response Status is BAD_REQUEST'
            response.status == HttpStatus.BAD_REQUEST.value()

        then: 'and the service is not called'
            0 * mockDmiService.registerCmHandles(_)

        where: 'given content value is wrong'
            casedetector                                 |          contentValue
            'content value missing id'                   |          "{\n" +
                                                                    "  \"cm-handles\": [\n" +
                                                                    "    {\n" +
                                                                    "      \"dmi-service-name\": \"dminameval\",\n" +
                                                                    "      \"additional-properties\": [\n" +
                                                                    "        {\n" +
                                                                    "          \"name\": \"nameval\",\n" +
                                                                    "          \"value\": \"valval\"\n" +
                                                                    "        }\n" +
                                                                    "      ]\n" +
                                                                    "    }\n" +
                                                                    "  ]\n" +
                                                                    "}\n"

            'content value missing dmi-service-name'     |          "{\n" +
                                                                    "  \"cm-handles\": [\n" +
                                                                    "    {\n" +
                                                                    "      \"id\": \"idval\",\n" +
                                                                    "      \"additional-properties\": [\n" +
                                                                    "        {\n" +
                                                                    "          \"name\": \"nameval\",\n" +
                                                                    "          \"value\": \"valval\"\n" +
                                                                    "        }\n" +
                                                                    "      ]\n" +
                                                                    "    }\n" +
                                                                    "  ]\n" +
                                                                    "}\n"

            'content is empty'                           |          ""
    }

    def 'post request for register cmhandles fails for missing content type'() {
        given: 'register cmhandles endpoint'
            def registerCmhandlesPost = "$basePath/v1/cmhandles"

        when: 'get register cmhandles post api is invoked with no content'
            def response = mvc.perform(
                    post(registerCmhandlesPost)
                            .content("")
            ).andReturn().response

        then: 'Response Status is BAD_REQUEST'
            response.status == HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()

        then: 'the service is not called'
            0 * mockDmiService.registerCmHandles(_)
    }
}
