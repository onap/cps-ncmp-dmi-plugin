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

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.service.DmiService
import org.onap.cps.ncmp.rest.model.CmHandles
import org.springframework.http.MediaType
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

    @SpringBean
    ObjectMapper objectMapper = new ObjectMapper()

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

            def cmhandlejsoncontent =  org.onap.cps.ncmp.dmi.TestUtils.getResourceFileContent('cmhandles.json')

        when: 'get register cmhandles post api is invoked'
            def response = mvc.perform(
                    post(registerCmhandlesPost)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cmhandlejsoncontent)
                    ).andReturn().response

        then: 'service called once'
            1 * mockDmiService.registerCmHandles(cmhandlejsoncontent)


    }

    def 'Post request for register cm handles called with wrong content.'() {

        given: 'register cm handle url'
            def registerCmhandlesPost = "${basePath}/inventory/cmhandles"

        when: 'get register cm handles post api is invoked with no content'
            def response = mvc.perform(
                    post(registerCmhandlesPost).contentType(MediaType.APPLICATION_JSON)
                            .content(contentValue)
                    ).andReturn().response

        then: 'response Status is bad request'
            response.status == HttpStatus.BAD_REQUEST.value()

        and: 'the service is not called'
            0 * mockDmiService.registerCmHandles(_)

        where: 'given content value is wrong'
            scenario                                     |          contentValue
            'content value without id'                   |          TestUtils.getResourceFileContent('cmhandles_with_without_id.json')

            'content value without dmi-service-name'     |          TestUtils.getResourceFileContent('cmhandles_with_without_dmiservicename.json')

            'content is empty'                           |          ""
    }

    def 'Post request for register cm handles called with no content.'() {
        
        given: 'register cm handle url'
            def registerCmhandlesPost = "${basePath}/inventory/cmhandles"

        when: 'get register cmhandles post api is invoked with no content'
            def response = mvc.perform(
                    post(registerCmhandlesPost)
                    .contentType(MediaType.APPLICATION_XML)
                            .content("")
            ).andReturn().response

        then: 'response status is unsupported media type'
            response.status == HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()

        and: 'the service is not called'
            0 * mockDmiService.registerCmHandles(_)
    }
}
