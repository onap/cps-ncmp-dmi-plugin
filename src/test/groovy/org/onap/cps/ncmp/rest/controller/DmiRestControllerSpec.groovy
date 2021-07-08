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

import org.onap.cps.ncmp.rest.model.AdditionalProperty
import org.onap.cps.ncmp.rest.model.CmHandle
import org.onap.cps.ncmp.rest.model.CmHandles
import org.springframework.http.MediaType
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

    def 'Post request for register cm handles called with correct content.'() {

        given: 'register cm handle url and cm handles list'
            def registerCmhandlesPost = "${basePath}/v1/inventory/cmhandles"
            def cmHandle = new CmHandle()
            cmHandle.setId("idval")
            cmHandle.setDmiServiceName("dminameval")
            def additionalProp = new AdditionalProperty()
            additionalProp.setName("nameval")
            additionalProp.setValue("valval")
            cmHandle.addAdditionalPropertiesItem(additionalProp)
            def cmhandles = new CmHandles();
            cmhandles.addCmHandlesItem(cmHandle)

            def cmhandlejsoncontent =  org.onap.cps.dmi.TestUtils.getResourceFileContent('cmhandles.json')

        when: 'get register cmhandles post api is invoked'
            def response = mvc.perform(
                    post(registerCmhandlesPost).contentType(MediaType.APPLICATION_JSON)
                            .content(cmhandlejsoncontent)
            ).andReturn().response

        then: 'response status is success'
            response.status == HttpStatus.OK.value()

        and: 'service called once'
            1 * mockDmiService.registerCmHandles(cmhandles)
    }

    def 'Post request for register cm handles called with wrong content.'() {

        given: 'register cm handle url'
            def registerCmhandlesPost = "${basePath}/v1/inventory/cmhandles"

        when: 'get register cm handles post api is invoked with no content'
            def response = mvc.perform(
                    post(registerCmhandlesPost).contentType(MediaType.APPLICATION_JSON)
                            .content(contentValue)
                    ).andReturn().response

        then: 'response Status is bad request'
            response.status == HttpStatus.BAD_REQUEST.value()

        and: 'the service is not called'
            0 * mockDmiService.registerCmHandles(_ as CmHandles)

        where: 'given content value is wrong'
            casedetector                                 |          contentValue
            'content value missing id'                   |          org.onap.cps.dmi.TestUtils.getResourceFileContent('cmhandles_with_missing_id.json')

            'content value missing dmi-service-name'     |          org.onap.cps.dmi.TestUtils.getResourceFileContent('cmhandles_with_missing_dmiservicename.json')

            'content is empty'                           |          ""
    }

    def 'Post request for register cm handles called with no content.'() {
        
        given: 'register cm handle url'
            def registerCmhandlesPost = "${basePath}/v1/inventory/cmhandles"

        when: 'get register cmhandles post api is invoked with no content'
            def response = mvc.perform(
                    post(registerCmhandlesPost)
                            .content("")
            ).andReturn().response

        then: 'response status is unsupported media type'
            response.status == HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()

        and: 'the service is not called'
            0 * mockDmiService.registerCmHandles(_ as CmHandles)
    }
}
