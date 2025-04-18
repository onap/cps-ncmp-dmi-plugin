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

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.api.kafka.MessagingBaseSpec
import org.onap.cps.ncmp.dmi.exception.CmHandleRegistrationException
import org.onap.cps.ncmp.dmi.service.DmiService
import org.onap.cps.ncmp.events.ves30_2_1.VesEventSchema
import org.slf4j.LoggerFactory
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.spock.Testcontainers

@SpringBootTest(classes = [ObjectMapper])
@Testcontainers
@DirtiesContext
class VesEventConsumerSpec extends MessagingBaseSpec {

    def objectMapper = new ObjectMapper()
    def dmiService = Mock(DmiService)

    @SpringBean
    VesEventConsumer objectUnderTest = new VesEventConsumer(dmiService)

    def logger = Spy(ListAppender<ILoggingEvent>)

    void setup() {
        ((Logger) LoggerFactory.getLogger(VesEventConsumer.class)).addAppender(logger)
        logger.start()
    }

    void cleanup() {
        ((Logger) LoggerFactory.getLogger(VesEventConsumer.class)).detachAndStopAllAppenders()
    }


    def 'Consume a valid VES message during device mounting'() {
        given: 'a valid VES event is created and sent '
            def jsonData = TestUtils.getResourceFileContent('sampleVesEvent.json')
            def vesEvent = objectMapper.readValue(jsonData, VesEventSchema.class)
        when: 'event is consumed when the device is mounted'
            objectUnderTest.consumeVesEvent(vesEvent)
        then: 'the dmi service is called to register the devices with NCMP'
            1 * dmiService.registerCmHandles(['pynts-o-du-o1'])

    }

    def 'Consume a valid VES message during device mounting but device already registered with NCMP'() {
        given: 'a valid VES event is created and sent '
            def jsonData = TestUtils.getResourceFileContent('sampleVesEvent.json')
            def vesEvent = objectMapper.readValue(jsonData, VesEventSchema.class)
        and: 'the device is already registered with NCMP'
            dmiService.registerCmHandles(_) >> { throw new CmHandleRegistrationException('the cm handle already exists') }
        when: 'event is consumed when the device is mounted'
            objectUnderTest.consumeVesEvent(vesEvent)
        then: 'an exception is logged'
            def loggingEvent = logger.list[1]
            assert loggingEvent.level == Level.WARN
            assert loggingEvent.formattedMessage.contains('Exception occurred while registering the device : pynts-o-du-o1')
    }

    def 'Consume a valid VES message during device mounting but device registration results in exception'() {
        given: 'a valid VES event is created and sent '
            def jsonData = TestUtils.getResourceFileContent('sampleVesEvent.json')
            def vesEvent = objectMapper.readValue(jsonData, VesEventSchema.class)
        and: 'exception occurs when registering the device with NCMP'
            dmiService.registerCmHandles(_) >> { throw new Exception('unknown exception occurred') }
        when: 'event is consumed when the device is mounted'
            objectUnderTest.consumeVesEvent(vesEvent)
        then: 'an exception is logged'
            def loggingEvent = logger.list[1]
            assert loggingEvent.level == Level.WARN
            assert loggingEvent.formattedMessage.contains('Exception occurred')
    }

}
