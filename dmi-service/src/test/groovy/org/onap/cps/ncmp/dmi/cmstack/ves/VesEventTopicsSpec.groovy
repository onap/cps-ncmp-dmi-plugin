/*
 * ============LICENSE_START========================================================
 *  Copyright (c) 2025 OpenInfra Foundation Europe. All rights reserved.
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

package org.onap.cps.ncmp.dmi.cmstack.ves

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@SpringBootTest
@ContextConfiguration(classes = [VesEventTopics])
@EnableConfigurationProperties
class VesEventTopicsSpec extends Specification {

    @Autowired
    VesEventTopics vesEventTopics

    def 'Check the test topics configured'() {
        expect: 'VES topics are populated'
            assert vesEventTopics.topics.size() > 0
        and: 'correct topics are present'
            assert vesEventTopics.topics.contains('unauthenticated.VES_PNFREG_OUTPUT')
            assert vesEventTopics.topics.contains('unauthenticated.VES_O1_NOTIFY_PNF_REGISTRATION_OUTPUT')
    }

}
