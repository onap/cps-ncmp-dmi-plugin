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
import org.onap.cps.ncmp.dmi.exception.ModulesNotFoundException
import org.onap.cps.ncmp.dmi.service.DmiService
import org.onap.cps.ncmp.dmi.service.DmiServiceImpl
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

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
class DmiRestControllerSpec extends Specification {

    @SpringBean
    DmiService mockDmiService = Mock()

    @Autowired
    private MockMvc mvc

    @Value('${rest.api.dmi-base-path}/v1')
    def basePathV1

    def 'Get all modules for given cm handle.'() {
        given: 'REST endpoint for getting all modules'
            def getModuleUrl = "$basePathV1/ch/node1/modules"
        and: 'get modules for cm-handle returns a json'
            def someJson = 'some-json'
            mockDmiService.getModulesForCmHandle('node1') >> someJson
        when: 'post is being called'
            def response = mvc.perform( post(getModuleUrl)
                            .contentType(MediaType.APPLICATION_JSON))
                            .andReturn().response
        then: 'status is OK'
            response.status == HttpStatus.OK.value()
        and: 'the response content matches the result from the DMI service'
            response.getContentAsString() == someJson
    }

    def 'Get all modules for given cm handle with exception handling of #scenario.'() {
        given: 'REST endpoint for getting all modules'
            def getModuleUrl = "$basePathV1/ch/node1/modules"
        and: 'get modules for cm-handle throws #exceptionClass'
            mockDmiService.getModulesForCmHandle('node1') >> { throw Mock(exceptionClass) }
        when: 'post is invoked'
            def response = mvc.perform( post(getModuleUrl)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andReturn().response
        then: 'response status is #expectedResponse'
            response.status == expectedResponse
        where: 'the scenario is #scenario'
            scenario                       |  exceptionClass                 || expectedResponse
            'dmi service exception'        |  DmiException.class             || HttpStatus.INTERNAL_SERVER_ERROR.value()
            'no modules found'             |  ModulesNotFoundException.class || HttpStatus.NOT_FOUND.value()
            'any other runtime exception'  |  RuntimeException.class         || HttpStatus.INTERNAL_SERVER_ERROR.value()
    }
}
