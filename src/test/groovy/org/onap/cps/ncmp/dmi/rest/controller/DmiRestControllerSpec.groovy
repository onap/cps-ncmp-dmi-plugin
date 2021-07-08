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

import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.service.DmiService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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

    def setup(){
        basePath = basePath + "/v1"
    }

    def 'Post request for register cm handles called with correct content.'() {

        given: 'register cm handle url and cm handles json'
            def registerCmhandlesPost = "${basePath}/inventory/cmhandles"

            def cmHandleJson =  TestUtils.getResourceFileContent('cmhandles.json')

        when: 'get register cmhandles post api is invoked'
            def response = mvc.perform(
                    post(registerCmhandlesPost)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cmHandleJson)
                    ).andReturn().response

        then: 'service called once and returns true'
            1 * mockDmiService.registerCmHandles(_ as List<String>) >> true

        and: 'resonse should be CREATED'
            response.status == HttpStatus.CREATED.value()


    }

    def 'Post request for register cm handles called with empty content.'() {

        given: 'register cm handle url and empty json'
            def registerCmhandlesPost = "${basePath}/inventory/cmhandles"

            def emptyJson = TestUtils.getResourceFileContent('empty_cmhandles.json')

        when: 'get register cm handles post api is invoked with no content'
            def response = mvc.perform(
                    post(registerCmhandlesPost).contentType(MediaType.APPLICATION_JSON)
                            .content(emptyJson)
                    ).andReturn().response

        then: 'response Status is not acceptable'
            response.status == HttpStatus.NOT_ACCEPTABLE.value()

        and: 'the service is not called'
            0 * mockDmiService.registerCmHandles(_)
    }
}
