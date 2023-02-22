/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2023 Nordix Foundation
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.ncmp.dmi.notifications.avc

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.core.read.ListAppender
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.onap.cps.ncmp.event.model.Payload
import org.onap.cps.ncmp.event.model.ResponseData
import org.slf4j.LoggerFactory
import org.springframework.kafka.KafkaException
import spock.lang.Specification

class SubscriptionEventResponseProducerServiceSpec extends Specification {

    def mockSubscriptionEventResponseProducer = Mock(SubscriptionEventResponseProducer)
    def objectUnderTest = new SubscriptionEventResponseProducerService(mockSubscriptionEventResponseProducer)

    def logger
    def appender

    @BeforeEach
    void setup() {
        logger = (Logger) LoggerFactory.getLogger(objectUnderTest.getClass())
        appender = new ListAppender()
        logger.setLevel(Level.ERROR)
        appender.start()
        logger.addAppender(appender)
    }

    @AfterEach
    void teardown() {
        ((Logger) LoggerFactory.getLogger(objectUnderTest.getClass())).detachAndStopAllAppenders();
    }

    def 'Create and publish subscription response event successfully'() {
        given: 'a message key and a message value'
            def messageKey = UUID.randomUUID().toString()
            def messageValue = new ResponseData(payload: new Payload())
        when: 'service is called to publish subscription response data'
            objectUnderTest.publishSubscriptionEventResponse(messageKey, messageValue)
        then: 'producer is called one time'
            1 * mockSubscriptionEventResponseProducer.publishResponseEventMessage(_, messageKey, messageValue)
        and: 'no exception thrown'
            noExceptionThrown()
    }

    def 'Create and publish subscription response event fails'() {
        given: 'a message key and a message value'
            def messageKey = UUID.randomUUID().toString()
            def messageValue = new ResponseData(payload: new Payload())
        and: 'the subscription response producer throws an exception; Unable to publish'
            mockSubscriptionEventResponseProducer.publishResponseEventMessage(*_)
                >> { throw new KafkaException('Unable to publish') }
        when: 'the publisher service is called to publish subscription response data'
            objectUnderTest.publishSubscriptionEventResponse(messageKey, messageValue)
        then: 'the error log message contains the correct error message'
            def errorMessage = appender.list[0].toString()
            assert errorMessage.contains("Unable to publish subscription event response message")
    }
}
