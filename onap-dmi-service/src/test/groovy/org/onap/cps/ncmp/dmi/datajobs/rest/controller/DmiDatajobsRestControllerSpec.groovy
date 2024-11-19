/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2024 Nordix Foundation
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

package org.onap.cps.ncmp.dmi.datajobs.rest.controller

import org.onap.cps.ncmp.dmi.config.WebSecurityConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@Import(WebSecurityConfig)
@WebMvcTest(DmiDatajobsRestController.class)
@WithMockUser
class DmiDatajobsRestControllerSpec extends Specification{

    @Autowired
    private MockMvc mvc

    @Value('${rest.api.dmi-base-path}/v1')
    def basePathV1

    def 'write request should return 501 HTTP Status' () {
        given: 'URL to write a data job'
            def getModuleUrl = "${basePathV1}/cmwriteJob?destination=001"
        when: 'the request is posted'
            def response = mvc.perform(
                post(getModuleUrl)
                        .contentType('application/3gpp-json-patch+json')
        ).andReturn().response
        then: 'response value is Not Implemented'
            response.status == HttpStatus.NOT_IMPLEMENTED.value()
    }

    def 'read request should return 501 HTTP Status' () {
        given: 'URL to write a data job'
            def getModuleUrl = "${basePathV1}/cmreadJob?destination=001"
        when: 'the request is posted'
            def response = mvc.perform(
                post(getModuleUrl)
                        .contentType('application/3gpp-json-patch+json')
        ).andReturn().response
        then: 'response value is Not Implemented'
        response.status == HttpStatus.NOT_IMPLEMENTED.value()
    }

    def 'get status request should return 501 HTTP Status' () {
        given: 'URL to get the status of a data job'
            def getStatus = "${basePathV1}/cmwriteJob/dataProducer/data-producer-id/dataProducerJob/data-producerd-job-id}/status"
        when: 'the request is performed'
            def response = mvc.perform(
                    get(getStatus)
                            .contentType('application/json')
            ).andReturn().response
        then: 'response value is Not Implemented'
            response.status == HttpStatus.NOT_IMPLEMENTED.value()
    }

    def 'get result request should return 501 HTTP Status' () {
        given: 'URL to get the result of a data job'
            def getStatus = "${basePathV1}/cmwriteJob/dataProducer/some-identifier/dataProducerJob/some-producer-job-identifier/result?destination=some-destination"
        when: 'the request is performed'
            def response = mvc.perform(
                    get(getStatus)
                            .contentType('application/json')
            ).andReturn().response
        then: 'response value is Not Implemented'
            response.status == HttpStatus.NOT_IMPLEMENTED.value()
    }
}
