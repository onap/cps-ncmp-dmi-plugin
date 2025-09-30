/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2024-2025 OpenInfra Foundation Europe. All rights reserved.
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

package org.onap.cps.ncmp.dmi.notifications.cmsubscription

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.api.kafka.MessagingBaseSpec
import org.onap.cps.ncmp.dmi.notifications.cmsubscription.model.CmNotificationSubscriptionStatus
import org.onap.cps.ncmp.dmi.notifications.mapper.CloudEventMapper
import org.onap.cps.ncmp.impl.datajobs.subscription.dmi_to_ncmp.Data
import org.onap.cps.ncmp.impl.datajobs.subscription.dmi_to_ncmp.DataJobSubscriptionDmiOutEvent
import org.onap.cps.ncmp.impl.datajobs.subscription.ncmp_to_dmi.DataJobSubscriptionDmiInEvent
import org.slf4j.LoggerFactory
import org.spockframework.spring.SpringBean
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.spock.Testcontainers

import java.sql.Timestamp
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId


@Testcontainers
@DirtiesContext
class DmiInEventConsumerSpec extends MessagingBaseSpec {
    def objectMapper = new ObjectMapper()
    def testTopic = 'dmi-ncmp-cm-avc-subscription'
    def testDmiName = 'test-ncmp-dmi'

    @SpringBean
    DmiInEventConsumer objectUnderTest = new DmiInEventConsumer(cloudEventKafkaTemplate)

    def logger = Spy(ListAppender<ILoggingEvent>)

    void setup() {
        ((Logger) LoggerFactory.getLogger(CloudEventMapper.class)).addAppender(logger)
        logger.start()
    }

    void cleanup() {
        ((Logger) LoggerFactory.getLogger(CloudEventMapper.class)).detachAndStopAllAppenders()
    }

    def 'Sends subscription cloud event response successfully.'() {
        given: 'an subscription event response'
            objectUnderTest.dmiName = testDmiName
            objectUnderTest.dmoOutEventTopic = testTopic
            def correlationId = 'test-subscriptionId#test-ncmp-dmi'
            def cmSubscriptionDmiOutEventData = new Data(statusCode: subscriptionStatusCode, statusMessage: subscriptionStatusMessage)
            def subscriptionEventResponse =
                    new DataJobSubscriptionDmiOutEvent().withData(cmSubscriptionDmiOutEventData)
        and: 'consumer has a subscription'
            kafkaConsumer.subscribe([testTopic] as List<String>)
        when: 'an event is published'
            def eventKey = UUID.randomUUID().toString()
            objectUnderTest.createAndSendCmNotificationSubscriptionDmiOutEvent(eventKey, "subscriptionCreatedStatus", correlationId, subscriptionAcceptanceType)
        and: 'topic is polled'
            def records = kafkaConsumer.poll(Duration.ofMillis(1500))
        then: 'poll returns one record and close kafkaConsumer'
            assert records.size() == 1
            def record = records.iterator().next()
            kafkaConsumer.close()
        and: 'the record value matches the expected event value'
            def expectedValue = objectMapper.writeValueAsString(subscriptionEventResponse)
            assert expectedValue == record.value
            assert eventKey == record.key
        where: 'given #scenario'
            scenario                   | subscriptionAcceptanceType                | subscriptionStatusCode | subscriptionStatusMessage
            'Subscription is Accepted' | CmNotificationSubscriptionStatus.ACCEPTED | '1'                    | 'ACCEPTED'
            'Subscription is Rejected' | CmNotificationSubscriptionStatus.REJECTED | '104'                  | 'REJECTED'
    }

    def 'Consume valid message.'() {
        given: 'an event'
            objectUnderTest.dmiName = testDmiName
            def eventKey = UUID.randomUUID().toString()
            def timestamp = new Timestamp(1679521929511)
            def jsonData = TestUtils.getResourceFileContent('cmNotificationSubscriptionCreationEvent.json')
            def subscriptionEvent = objectMapper.readValue(jsonData, DataJobSubscriptionDmiInEvent.class)
            objectUnderTest.dmoOutEventTopic = testTopic
            def cloudEvent = CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create('test-ncmp-dmi'))
                    .withType(subscriptionType)
                    .withDataSchema(URI.create("urn:cps:" + DataJobSubscriptionDmiInEvent.class.getName() + ":1.0.0"))
                    .withExtension("correlationid", eventKey)
                    .withTime(OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneId.of("UTC")))
                    .withData(objectMapper.writeValueAsBytes(subscriptionEvent)).build()
            def testEventSent = new ConsumerRecord<String, CloudEvent>('topic-name', 0, 0, eventKey, cloudEvent)
        when: 'the valid event is consumed'
            objectUnderTest.consumeDmiInEvent(testEventSent)
        then: 'no exception is thrown'
            noExceptionThrown()
        where: 'given #scenario'
            scenario                    | subscriptionType
            'Subscription Create Event' | "subscriptionCreated"
            'Subscription Delete Event' | "subscriptionDeleted"
    }

    def 'Consume invalid message.'() {
        given: 'an invalid event body'
            objectUnderTest.dmiName = testDmiName
            def eventKey = UUID.randomUUID().toString()
            def timestamp = new Timestamp(1679521929511)
            def invalidJsonBody = "/////"
            objectUnderTest.dmoOutEventTopic = testTopic
            def cloudEvent = CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create('test-ncmp-dmi'))
                    .withType("subscriptionCreated")
                    .withDataSchema(URI.create("urn:cps:org.onap.ncmp.dmi.cm.subscription:1.0.0"))
                    .withTime(OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneId.of("UTC")))
                    .withExtension("correlationid", eventKey).withData(objectMapper.writeValueAsBytes(invalidJsonBody)).build()
            def testEventSent = new ConsumerRecord<String, CloudEvent>('topic-name', 0, 0, eventKey, cloudEvent)
        when: 'the invalid event is consumed'
            objectUnderTest.consumeDmiInEvent(testEventSent)
        then: 'exception is thrown and event is logged'
            def loggingEvent = getLoggingEvent()
            assert loggingEvent.level == Level.ERROR
            assert loggingEvent.formattedMessage.contains('Unable to map cloud event to target event class type')
    }

    def getLoggingEvent() {
        return logger.list[0]
    }
}