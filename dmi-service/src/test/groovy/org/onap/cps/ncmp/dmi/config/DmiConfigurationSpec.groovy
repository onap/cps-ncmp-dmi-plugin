/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2025 OpenInfra Foundation Europe. All rights reserved.
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

package org.onap.cps.ncmp.dmi.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@SpringBootTest
@ContextConfiguration(classes = [DmiConfiguration.CpsProperties, DmiConfiguration.SdncProperties])
class DmiConfigurationSpec extends Specification {

    @Autowired
    DmiConfiguration.CpsProperties cpsProperties

    @Autowired
    DmiConfiguration.SdncProperties sdncProperties

    def 'CPS properties configuration.'() {
        expect: 'CPS properties are set to values in test configuration yaml file'
            cpsProperties.baseUrl == 'some url for cps'
            cpsProperties.dmiRegistrationUrl == 'some registration url'
            cpsProperties.dataSyncEnabledUrl == 'some data sync url/{some-cm-handle}?dataSyncFlag=true'
    }

    def 'SDNC properties configuration.'() {
        expect: 'SDNC properties are set to values in test configuration yaml file'
            sdncProperties.authUsername == 'test'
            sdncProperties.authPassword == 'test'
            sdncProperties.baseUrl == 'http://test'
            sdncProperties.topologyId == 'test-topology'
    }

    def 'Rest template building.'() {
        given: 'a DMI configuration'
            DmiConfiguration objectUnderTest = new DmiConfiguration()
        and: 'a rest template builder'
            RestTemplateBuilder mockRestTemplateBuilder = Spy(RestTemplateBuilder)
        when: 'rest template method is invoked'
            objectUnderTest.restTemplate(mockRestTemplateBuilder)
        then: 'DMI configuration uses the build method on the template builder'
            1 * mockRestTemplateBuilder.build()
    }

}
