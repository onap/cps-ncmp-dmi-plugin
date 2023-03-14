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
import org.onap.cps.ncmp.dmi.service.model.SubscriptionEventResponseStatus
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
            def responseStatus = SubscriptionEventResponseStatus.ACCEPTED
            def cmHandleIdToStatusMap = ['CmHandle1':responseStatus, 'CmHandle2':responseStatus]
            def subscriptionEventResponse = new SubscriptionEventResponse(subscriptionName: 'cm-subscription-001',
                clientId: 'SCO-9989752', dmiName: 'ncmp-dmi-plugin', cmHandleIdToStatus: cmHandleIdToStatusMap)
            objectUnderTest.cmAvcSubscriptionResponseTopic = testTopic
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
            def jsonData = TestUtils.getResourceFileContent('avcSubscriptionCreationEvent.json')
            def testEventSent = objectMapper.readValue(jsonData, SubscriptionEvent.class)
            objectUnderTest.cmAvcSubscriptionResponseTopic = testTopic
        when: 'the valid event is consumed'
            def eventKey = UUID.randomUUID().toString()
            def timeStampReceived = '1679521929511'
            objectUnderTest.consumeSubscriptionEvent(testEventSent, eventKey, timeStampReceived)
        then: 'no exception is thrown'
            noExceptionThrown()
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

    def 'Populate cm handle id to status map successfully.'() {
        given: 'a set of cm handle id'
            def cmHandleIds = ['CmHandle1', 'CmHandle2'] as Set
            def responseStatus = SubscriptionEventResponseStatus.ACCEPTED
        when: 'populate cm handle id to status map'
            def result = objectUnderTest.populateCmHandleIdToStatus(cmHandleIds)
        then: 'cm handle id to status map populated as expected'
            def expectedMap = ['CmHandle1':responseStatus,'CmHandle2':responseStatus]
            expectedMap == result
    }
}