/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2022 Nordix Foundation
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

import org.springdoc.core.GroupedOpenApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@SpringBootTest
@ContextConfiguration(classes = [DmiPluginConfig.DmiPluginProperties])
class DmiPluginConfigSpec extends Specification {

    @Autowired
    DmiPluginConfig.DmiPluginProperties dmiPluginProperties

    def 'DMI plugin properties configuration.'() {
        expect: 'DMI plugin properties are set to values in test configuration yaml file'
            dmiPluginProperties.dmiServiceUrl == 'some url for the dmi service'
            dmiPluginProperties.dmiServiceName == 'ncmp-dmi-plugin'
    }

    def 'DMI plugin api creation.'() {
        given: 'a DMI plugin configuration'
            DmiPluginConfig objectUnderTest = new DmiPluginConfig()
        when: 'the api method is invoked'
            def result = objectUnderTest.api()
        then: 'a spring web plugin docket is returned'
            result instanceof GroupedOpenApi
        and: 'it is named "dmi-plugin-api"'
            result.group == 'dmi-plugin-api'
    }

}
