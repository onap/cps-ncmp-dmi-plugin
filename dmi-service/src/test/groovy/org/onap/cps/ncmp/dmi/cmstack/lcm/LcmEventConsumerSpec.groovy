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

package org.onap.cps.ncmp.dmi.cmstack.lcm

import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.service.DmiService
import org.onap.cps.ncmp.events.lcm.v1.LcmEvent
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.spock.Testcontainers
import spock.lang.Specification

import static org.onap.cps.ncmp.events.lcm.v1.Values.CmHandleState.LOCKED

@SpringBootTest(classes = [ObjectMapper])
@Testcontainers
@DirtiesContext
class LcmEventConsumerSpec extends Specification {

    def objectMapper = new ObjectMapper()
    def dmiService = Mock(DmiService)

    def jsonData

    @SpringBean
    LcmEventConsumer objectUnderTest = new LcmEventConsumer(dmiService)

    void setup() {
        jsonData = TestUtils.getResourceFileContent('sampleLcmEvent.json')
    }

    def 'Consume LCM message when the cm handle is READY( module sync done )'() {
        given: 'LCM event is created and sent '
            def lcmEvent = objectMapper.readValue(jsonData, LcmEvent.class)
        when: 'event is consumed'
            objectUnderTest.consumeLcmEvent(lcmEvent)
        then: 'cm handle(s) are enabled for data sync'
            1 * dmiService.enableDataSyncForCmHandles(['ch-1'])

    }

    def 'Consume LCM message when the cm handle is in LOCKED state'() {
        given: 'LCM event is created and sent '
            def lcmEvent = objectMapper.readValue(jsonData, LcmEvent.class)
        and: 'cm handle is in LOCKED state'
            lcmEvent.event.newValues.cmHandleState = LOCKED
        when: 'event is consumed'
            objectUnderTest.consumeLcmEvent(lcmEvent)
        then: 'data sync flag is not enabled as state is not READY'
            0 * dmiService.enableDataSyncForCmHandles(['ch-1'])

    }

    def 'Consume LCM message with no target state'() {
        given: 'LCM event is created and sent '
            def lcmEvent = objectMapper.readValue(jsonData, LcmEvent.class)
        and: 'the target state ( newValues ) is set to null'
            lcmEvent.event.newValues = null
        when: 'event is consumed'
            objectUnderTest.consumeLcmEvent(lcmEvent)
        then: 'data sync flag is not enabled as state is not READY'
            0 * dmiService.enableDataSyncForCmHandles(_)

    }
}
