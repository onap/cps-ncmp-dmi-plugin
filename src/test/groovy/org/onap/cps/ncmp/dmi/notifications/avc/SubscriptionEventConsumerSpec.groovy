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

package org.onap.cps.ncmp.dmi.notifications.avc

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.api.kafka.MessagingBaseSpec
import org.onap.cps.ncmp.dmi.service.model.SubscriptionEventResponse
import org.onap.cps.ncmp.event.model.SubscriptionEvent
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.spock.Testcontainers

import java.time.Duration

@SpringBootTest(classes = [SubscriptionEventConsumer])
@Testcontainers
@DirtiesContext
class SubscriptionEventConsumerSpec extends MessagingBaseSpec {

    def kafkaConsumer = new KafkaConsumer<>(consumerConfigProperties('ncmp-group'))

    def objectMapper = new ObjectMapper()
    def testTopic = 'dmi-ncmp-cm-avc-subscription'

    @SpringBean
    SubscriptionEventConsumer objectUnderTest = new SubscriptionEventConsumer(kafkaTemplate)

    def 'Sends subscription event response successfully.'() {
        given: 'an subscription event response'
            def cmHandleIdToStatusMap = ['CmHandle1':'accepted', 'CmHandle2':'declined']
            def subscriptionEventResponse = new SubscriptionEventResponse(clientId: 'client-1', cmHandleIdToStatus: cmHandleIdToStatusMap)
            objectUnderTest.cmAvcSubscriptionResponseTopic = testTopic
        and: 'consumer has a subscription'
            kafkaConsumer.subscribe([testTopic] as List<String>)
        when: 'an event is published'
            objectUnderTest.sendSubscriptionResponseMessage(subscriptionEventResponse)
        and: 'topic is polled'
            def records = kafkaConsumer.poll(Duration.ofMillis(1500))
        then: 'poll returns one record'
            assert records.size() == 1
            def record = records.iterator().next()
        and: 'the record value matches the expected event value'
            def expectedValue = objectMapper.writeValueAsString(subscriptionEventResponse)
            assert expectedValue == record.value
    }

    def 'Consume valid message'() {
        given: 'an event'
            def jsonData = TestUtils.getResourceFileContent('avcSubscriptionCreationEvent.json')
            def testEventSent = objectMapper.readValue(jsonData, SubscriptionEvent.class)
            objectUnderTest.cmAvcSubscriptionResponseTopic = testTopic
        when: 'the valid event is consumed'
            objectUnderTest.consumeSubscriptionEvent(testEventSent)
        then: 'no exception is thrown'
            noExceptionThrown()
    }
}