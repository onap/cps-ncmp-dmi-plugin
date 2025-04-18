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

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.service.DmiService
import org.onap.cps.ncmp.events.lcm.v1.LcmEvent
import org.slf4j.LoggerFactory
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

    def logger = Spy(ListAppender<ILoggingEvent>)

    void setup() {
        jsonData = TestUtils.getResourceFileContent('sampleLcmEvent.json')
        ((Logger) LoggerFactory.getLogger(LcmEventConsumer.class)).addAppender(logger)
        logger.start()
    }

    void cleanup() {
        ((Logger) LoggerFactory.getLogger(LcmEventConsumer.class)).detachAndStopAllAppenders()
    }

    def 'Consume a valid LCM message when the cm handle is READY( module sync done )'() {
        given: 'a valid LCM event is created and sent '
            def lcmEvent = objectMapper.readValue(jsonData, LcmEvent.class)
        when: 'event is consumed when the device is mounted'
            objectUnderTest.consumeLcmEvent(lcmEvent)
        then: 'the dmi service is called to register the devices with NCMP'
            1 * dmiService.enableDataSyncForCmHandles(['pynts-o-du-o1'])

    }

    def 'Consume a valid LCM message when the cm handle is in LOCKED state'() {
        given: 'a valid LCM event is created and sent '
            def lcmEvent = objectMapper.readValue(jsonData, LcmEvent.class)
        and: 'cm handle is in LOCKED state'
            lcmEvent.event.newValues.cmHandleState = LOCKED
        when: 'event is consumed when the device is mounted'
            objectUnderTest.consumeLcmEvent(lcmEvent)
        then: 'data sync flag is not enabled as state is not READy'
            0 * dmiService.enableDataSyncForCmHandles(['pynts-o-du-o1'])

    }

    def 'Consume a valid LCM message with no target state'() {
        given: 'a valid LCM event is created and sent '
            def lcmEvent = objectMapper.readValue(jsonData, LcmEvent.class)
        and: 'the target state ( newValues ) is set to null'
            lcmEvent.event.newValues = null
        when: 'event is consumed'
            objectUnderTest.consumeLcmEvent(lcmEvent)
        then: 'data sync flag is not enabled as state is not READy'
            0 * dmiService.enableDataSyncForCmHandles(_)

    }
}
