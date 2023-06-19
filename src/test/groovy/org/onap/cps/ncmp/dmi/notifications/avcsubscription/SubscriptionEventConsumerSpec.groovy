/*
 *
 *  * Copyright 2016 [company name] and others.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package org.onap.cps.ncmp.dmi.notifications.avcsubscription

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.api.kafka.MessagingBaseSpec
import org.onap.cps.ncmp.dmi.notifications.avcsubscription.SubscriptionEventConsumer
import org.onap.cps.ncmp.event.model.SubscriptionEvent
import org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.Data
import org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.SubscriptionEventResponse
import org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.SubscriptionStatus
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.spock.Testcontainers

import java.time.Duration

@SpringBootTest(classes = [SubscriptionEventConsumer])
@Testcontainers
@DirtiesContext
class SubscriptionEventConsumerSpec extends MessagingBaseSpec {

    def objectMapper = new ObjectMapper()
    def testTopic = 'dmi-ncmp-cm-avc-subscription'

    @SpringBean
    SubscriptionEventConsumer objectUnderTest = new SubscriptionEventConsumer(cloudEventKafkaTemplate)

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
            objectUnderTest.sendSubscriptionResponseMessage(eventKey, subscriptionEventResponse)
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
            def jsonData = TestUtils.getResourceFileContent('avcSubscriptionCreationEvent.json')
            def subscriptionEvent = objectMapper.readValue(jsonData, SubscriptionEvent.class)
            objectUnderTest.cmAvcSubscriptionResponseTopic = testTopic
            def cloudEvent = CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create('test-ncmp-dmi'))
                    .withType(SubscriptionEvent.class.getName())
                    .withDataSchema(URI.create("urn:cps:" + SubscriptionEvent.class.getName() + ":1.0.0"))
                    .withExtension("correlationid", eventKey)
                    .withData(objectMapper.writeValueAsBytes(subscriptionEvent)).build()
            def testEventSent = new ConsumerRecord<String, CloudEvent>('topic-name', 0, 0, 'event-key', cloudEvent)
        when: 'the valid event is consumed'
            def timeStampReceived = '1679521929511'
            objectUnderTest.consumeSubscriptionEvent(testEventSent, eventKey, timeStampReceived)
        then: 'no exception is thrown'
            noExceptionThrown()
    }

    def 'Consume invalid message.'() {
        given: 'an invalid event type'
        objectUnderTest.dmiName = 'test-ncmp-dmi'
        def eventKey = UUID.randomUUID().toString()
        objectUnderTest.cmAvcSubscriptionResponseTopic = testTopic
        def cloudEvent = CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create('test-ncmp-dmi'))
                .withType(SubscriptionEventResponse.class.getName())
                .withDataSchema(URI.create("urn:cps:" + SubscriptionEventResponse.class.getName() + ":1.0.0"))
                .withExtension("correlationid", eventKey).build()
        def testEventSent = new ConsumerRecord<String, CloudEvent>('topic-name', 0, 0, 'event-key', cloudEvent)
        when: 'the invalid event is consumed'
        def timeStampReceived = '1679521929511'
        objectUnderTest.consumeSubscriptionEvent(testEventSent, eventKey, timeStampReceived)
        then: 'no exception is thrown and event is logged'
        noExceptionThrown()
    }

    def 'Convert a SubscriptionResponseEvent to CloudEvent successfully.'() {
        given: 'a SubscriptionResponseEvent and an event key'
            objectUnderTest.dmiName = 'test-ncmp-dmi'
            def responseStatus = SubscriptionStatus.Status.ACCEPTED
            def subscriptionStatuses = [new SubscriptionStatus(id: 'CmHandle1', status: responseStatus),
                                        new SubscriptionStatus(id: 'CmHandle2', status: responseStatus)]
            def subscriptionEventResponseData = new Data(subscriptionName: 'cm-subscription-001',
                    clientId: 'SCO-9989752', dmiName: 'ncmp-dmi-plugin', subscriptionStatus: subscriptionStatuses)
            def subscriptionEventResponse =
                    new SubscriptionEventResponse().withData(subscriptionEventResponseData)
            def eventKey = UUID.randomUUID().toString()
        when: 'a SubscriptionResponseEvent is converted'
            def result = objectUnderTest.convertSubscriptionResponseEvent(eventKey, subscriptionEventResponse)
        then: 'SubscriptionResponseEvent is converted as expected'
            def expectedCloudEvent = CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create('test-ncmp-dmi'))
                .withType(SubscriptionEventResponse.class.getName())
                .withDataSchema(URI.create("urn:cps:" + SubscriptionEventResponse.class.getName() + ":1.0.0"))
                .withExtension("correlationid", eventKey)
                .withData(objectMapper.writeValueAsBytes(subscriptionEventResponse)).build()
            assert expectedCloudEvent.data == result.data
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

    def 'Extract cm handle ids from cm handle id to cm handle property map successfully.'() {
        given: 'a list of cm handle id to cm handle property map'
            def cmHandleIdToPropertyMap =
                ['CmHandle1':['prop-x':'prop-valuex'], 'CmHandle2':['prop-y':'prop-valuey']]
            def listOfCmHandleIdToPropertyMap =
                [cmHandleIdToPropertyMap]
        when: 'extract the cm handle ids'
            def result = objectUnderTest.extractCmHandleIds(listOfCmHandleIdToPropertyMap)
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

    def 'Map the Cloud Event to data of the subscription event with wrong content causes an exception'() {
        given: 'an empty subscription response event and event key'
        def eventKey = UUID.randomUUID().toString()
            def testSubscriptionEventResponse = new SubscriptionEventResponse()
        when: 'the subscription response event map to data of cloud event'
            def thrownException = null
            try {
                objectUnderTest.convertSubscriptionResponseEvent(eventKey, testSubscriptionEventResponse)
            } catch (Exception e) {
                thrownException  = e
            }
        then: 'a run time exception is thrown'
            assert thrownException instanceof RuntimeException
    }
}