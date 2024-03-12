/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2024 Nordix Foundation
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

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.api.kafka.MessagingBaseSpec
import org.onap.cps.ncmp.events.cmnotificationsubscription_merge1_0_0.dmi_to_ncmp.CmNotificationSubscriptionDmiOutEvent
import org.onap.cps.ncmp.events.cmnotificationsubscription_merge1_0_0.dmi_to_ncmp.Data
import org.onap.cps.ncmp.events.cmnotificationsubscription_merge1_0_0.ncmp_to_dmi.CmNotificationSubscriptionDmiInEvent
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.spock.Testcontainers

import java.sql.Timestamp
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId


@SpringBootTest(classes = [CmNotificationSubscriptionDmiInEventConsumer])
@Testcontainers
@DirtiesContext
class CmNotificationSubscriptionDmiInEventConsumerSpec extends MessagingBaseSpec {
    def objectMapper = new ObjectMapper()
    def testTopic = 'dmi-ncmp-cm-avc-subscription'

    @SpringBean
    CmNotificationSubscriptionDmiInEventConsumer objectUnderTest = new CmNotificationSubscriptionDmiInEventConsumer(cloudEventKafkaTemplate)

    def 'Sends subscription cloud event response successfully.'() {
        given: 'an subscription event response'
            objectUnderTest.dmiName = 'test-ncmp-dmi'
            objectUnderTest.cmAvcSubscriptionResponseTopic = testTopic
            def correlationId = 'subscription1#test-ncmp-dmi'
            def cmSubscriptionDmiOutEventData = new Data(statusCode: '1', statusMessage: 'ACCEPTED')
            def subscriptionEventResponse =
                    new CmNotificationSubscriptionDmiOutEvent().withData(cmSubscriptionDmiOutEventData)
        and: 'consumer has a subscription'
            kafkaConsumer.subscribe([testTopic] as List<String>)
        when: 'an event is published'
            def eventKey = UUID.randomUUID().toString()
            objectUnderTest.createAndSendCmNotificationSubscriptionDmiOutEvent(eventKey, "subscriptionCreatedStatus", correlationId, "ACCEPTED")
        and: 'topic is polled'
            def records = kafkaConsumer.poll(Duration.ofMillis(1500))
        then: 'poll returns one record'
            assert records.size() == 1
            def record = records.iterator().next()
        and: 'the record value matches the expected event value'
            def expectedValue = objectMapper.writeValueAsString(subscriptionEventResponse)
            assert expectedValue == record.value
            assert eventKey == record.key
    }

    def 'Consume valid message.'() {
        given: 'an event'
            objectUnderTest.dmiName = 'test-ncmp-dmi'
            def eventKey = UUID.randomUUID().toString()
            def timestamp = new Timestamp(1679521929511)
            def jsonData = TestUtils.getResourceFileContent('cmNotificationSubscriptionCreationEvent.json')
            def subscriptionEvent = objectMapper.readValue(jsonData, CmNotificationSubscriptionDmiInEvent.class)
            objectUnderTest.cmAvcSubscriptionResponseTopic = testTopic
            def cloudEvent = CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create('test-ncmp-dmi'))
                    .withType(subscriptionType)
                    .withDataSchema(URI.create("urn:cps:" + CmNotificationSubscriptionDmiInEvent.class.getName() + ":1.0.0"))
                    .withExtension("correlationid", eventKey)
                    .withTime(OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneId.of("UTC")))
                    .withData(objectMapper.writeValueAsBytes(subscriptionEvent)).build()
            def testEventSent = new ConsumerRecord<String, CloudEvent>('topic-name', 0, 0, eventKey, cloudEvent)
        when: 'the valid event is consumed'
            objectUnderTest.consumeCmSubscriptionDmiInEvent(testEventSent)
        then: 'no exception is thrown'
            noExceptionThrown()
        where: 'given #senario'
            scenario                    | subscriptionType
            'Subscription Create Event' | "subscriptionCreated"
            'Subscription Delete Event' | "subscriptionDeleted"
    }

    def 'Consume invalid message.'() {
        given: 'an invalid event type'
            objectUnderTest.dmiName = 'test-ncmp-dmi'
            def eventKey = UUID.randomUUID().toString()
            def timestamp = new Timestamp(1679521929511)
            objectUnderTest.cmAvcSubscriptionResponseTopic = testTopic
            def cloudEvent = CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create('test-ncmp-dmi'))
                    .withType("subscriptionCreated")
                    .withDataSchema(URI.create("urn:cps:org.onap.ncmp.dmi.cm.subscription:1.0.0"))
                    .withTime(OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneId.of("UTC")))
                    .withExtension("correlationid", eventKey).build()
            def testEventSent = new ConsumerRecord<String, CloudEvent>('topic-name', 0, 0, eventKey, cloudEvent)
        when: 'the invalid event is consumed'
            objectUnderTest.consumeCmSubscriptionDmiInEvent(testEventSent)
        then: 'no exception is thrown and event is logged'
            noExceptionThrown()
    }
}