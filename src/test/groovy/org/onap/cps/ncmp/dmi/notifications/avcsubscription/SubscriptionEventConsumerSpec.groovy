/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation
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

package org.onap.cps.ncmp.dmi.notifications.avcsubscription

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.api.kafka.MessagingBaseSpec
import org.onap.cps.ncmp.events.avcsubscription1_0_0.ncmp_to_dmi.CmHandle
import org.onap.cps.ncmp.events.avcsubscription1_0_0.ncmp_to_dmi.SubscriptionEvent
import org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.Data
import org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.SubscriptionEventResponse
import org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.SubscriptionStatus
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.spock.Testcontainers

import java.sql.Timestamp
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId

@SpringBootTest(classes = [SubscriptionEventConsumer])
@Testcontainers
@DirtiesContext
class SubscriptionEventConsumerSpec extends MessagingBaseSpec {

    def objectMapper = new ObjectMapper()
    def testTopic = 'dmi-ncmp-cm-avc-subscription'

    @SpringBean
    SubscriptionEventConsumer objectUnderTest = new SubscriptionEventConsumer(cloudEventKafkaTemplate, objectMapper)

    def 'Sends subscription cloud event response successfully.'() {
        given: 'an subscription event response'
            objectUnderTest.dmiName = 'test-ncmp-dmi'
            objectUnderTest.cmAvcSubscriptionResponseTopic = testTopic
            def responseStatus = SubscriptionStatus.Status.ACCEPTED
            def subscriptionStatuses = [new SubscriptionStatus(id: 'CmHandle1', status: responseStatus),
                                        new SubscriptionStatus(id: 'CmHandle2', status: responseStatus)]
            def subscriptionEventResponseData = new Data(subscriptionName: 'cm-subscription-001',
                clientId: 'SCO-9989752', dmiName: 'ncmp-dmi-plugin', subscriptionStatus: subscriptionStatuses)
            def subscriptionEventResponse =
                    new SubscriptionEventResponse().withData(subscriptionEventResponseData)
        and: 'consumer has a subscription'
            kafkaConsumer.subscribe([testTopic] as List<String>)
        when: 'an event is published'
            def eventKey = UUID.randomUUID().toString()
            objectUnderTest.sendSubscriptionResponseMessage(eventKey, "CREATE", subscriptionEventResponse)
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
            def jsonData = TestUtils.getResourceFileContent('avcSubscriptionCreationEvent.json')
            def subscriptionEvent = objectMapper.readValue(jsonData, SubscriptionEvent.class)
            objectUnderTest.cmAvcSubscriptionResponseTopic = testTopic
            def cloudEvent = CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create('test-ncmp-dmi'))
                    .withType("CREATE")
                    .withDataSchema(URI.create("urn:cps:" + SubscriptionEvent.class.getName() + ":1.0.0"))
                    .withExtension("correlationid", eventKey)
                    .withTime(OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneId.of("UTC")))
                    .withData(objectMapper.writeValueAsBytes(subscriptionEvent)).build()
            def testEventSent = new ConsumerRecord<String, CloudEvent>('topic-name', 0, 0, eventKey, cloudEvent)
        when: 'the valid event is consumed'
            objectUnderTest.consumeSubscriptionEvent(testEventSent)
        then: 'no exception is thrown'
            noExceptionThrown()
    }

    def 'Consume invalid message.'() {
        given: 'an invalid event type'
            objectUnderTest.dmiName = 'test-ncmp-dmi'
            def eventKey = UUID.randomUUID().toString()
            def timestamp = new Timestamp(1679521929511)
            objectUnderTest.cmAvcSubscriptionResponseTopic = testTopic
            def cloudEvent = CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create('test-ncmp-dmi'))
                .withType("CREATE")
                .withDataSchema(URI.create("urn:cps:" + SubscriptionEventResponse.class.getName() + ":1.0.0"))
                .withTime(OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneId.of("UTC")))
                .withExtension("correlationid", eventKey).build()
            def testEventSent = new ConsumerRecord<String, CloudEvent>('topic-name', 0, 0, eventKey, cloudEvent)
        when: 'the invalid event is consumed'
            objectUnderTest.consumeSubscriptionEvent(testEventSent)
        then: 'no exception is thrown and event is logged'
            noExceptionThrown()
    }

    def 'Form a SubscriptionEventResponse from a SubscriptionEvent.'() {
        given: 'a SubscriptionEvent'
            def jsonData = TestUtils.getResourceFileContent('avcSubscriptionCreationEvent.json')
            def subscriptionEvent = objectMapper.readValue(jsonData, SubscriptionEvent.class)
        when: 'a SubscriptionResponseEvent is formed'
            def result = objectUnderTest.formSubscriptionEventResponse(subscriptionEvent)
        then: 'Confirm SubscriptionEventResponse was formed as expected'
            assert result.data.clientId == "SCO-9989752"
            assert result.data.subscriptionName == "cm-subscription-001"
    }

    def 'Extract cm handle ids from cm handle successfully.'() {
        given: 'a list of cm handles'
            def cmHandleIds =
                [new CmHandle(id:'CmHandle1', additionalProperties: ['prop-x':'prop-valuex']),
                 new CmHandle(id:'CmHandle2', additionalProperties: ['prop-y':'prop-valuey'])]
        when: 'extract the cm handle ids'
            def result = objectUnderTest.extractCmHandleIds(cmHandleIds)
        then: 'cm handle ids are extracted as expected'
            def expectedCmHandleIds = ['CmHandle1', 'CmHandle2'] as Set
            assert expectedCmHandleIds == result
    }

    def 'Populate cm handle id to subscriptionStatus successfully.'() {
        given: 'a set of cm handle id'
            def cmHandleIds = ['CmHandle1', 'CmHandle2'] as Set
            def responseStatus = SubscriptionStatus.Status.ACCEPTED
        when: 'populate cm handle id to subscriptionStatus'
            def result = objectUnderTest.populateSubscriptionStatus(cmHandleIds).status
        then: 'cm handle id to subscriptionStatus populated as expected'
            def expectedStatus = [responseStatus,responseStatus]
            expectedStatus == result
    }
}