/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2023 Nordix Foundation
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

import org.onap.cps.ncmp.dmi.config.WebSecurityConfig
import org.springframework.context.annotation.Import

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

@WebMvcTest(controllers = TestController.class)
@Import(WebSecurityConfig)
class ControllerSecuritySpec extends Specification {

    @Autowired
    MockMvc mvc

    def testEndpoint = '/test'

    def 'Get request with valid authentication'() {
        when: 'request is sent with authentication'
            def response = mvc.perform(
                    get(testEndpoint).header("Authorization", 'Basic Y3BzdXNlcjpjcHNyMGNrcyE=')
            ).andReturn().response
        then: 'HTTP OK status code is returned'
            assert response.status == HttpStatus.OK.value()
    }

    def 'Get request without authentication'() {
        when: 'request is sent without authentication'
            def response = mvc.perform(get(testEndpoint)).andReturn().response
        then: 'HTTP Unauthorized status code is returned'
            assert response.status == HttpStatus.UNAUTHORIZED.value()
    }

    def 'Get request with invalid authentication'() {
        when: 'request is sent with invalid authentication'
            def response = mvc.perform(
                    get(testEndpoint).header("Authorization", 'Basic invalid auth')
            ).andReturn().response
        then: 'HTTP Unauthorized status code is returned'
            assert response.status == HttpStatus.UNAUTHORIZED.value()
    }

    def 'Security Config #scenario permit URIs'() {
        expect: 'can create a web security configuration'
            new WebSecurityConfig(permitUris,'user','password')
        where: 'the following string of permit URIs is provided'
            scenario  | permitUris
            'with'    | 'a,b'
            'without' | ''
    }
}
