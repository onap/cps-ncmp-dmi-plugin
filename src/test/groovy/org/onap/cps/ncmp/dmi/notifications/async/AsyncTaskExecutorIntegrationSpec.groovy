/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2022-2023 Nordix Foundation
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.ncmp.dmi.notifications.async

import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.cps.ncmp.dmi.api.kafka.MessagingBaseSpec
import org.onap.cps.ncmp.dmi.exception.HttpClientRequestException
import org.onap.cps.ncmp.event.model.DmiAsyncRequestResponseEvent
import org.onap.cps.ncmp.event.model.AvcEvent
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.spock.Testcontainers

import java.time.Duration

@SpringBootTest(classes = [AsyncTaskExecutor, DmiAsyncRequestResponseEventProducer])
@Testcontainers
@DirtiesContext
class AsyncTaskExecutorIntegrationSpec extends MessagingBaseSpec {

    @SpringBean
    DmiAsyncRequestResponseEventProducer cpsAsyncRequestResponseEventProducer =
        new DmiAsyncRequestResponseEventProducer(kafkaTemplate)

    def spiedObjectMapper = Spy(ObjectMapper)

    def objectUnderTest = new AsyncTaskExecutor(cpsAsyncRequestResponseEventProducer)

    private static final String TEST_TOPIC = 'test-topic'

    def setup() {
        cpsAsyncRequestResponseEventProducer.dmiNcmpTopic = TEST_TOPIC
        consumer.subscribe([TEST_TOPIC] as List<String>)
    }

    def cleanup() {
        consumer.close()
    }

    def 'Publish and Subscribe message - success'() {
        when: 'a successful event is published'
            objectUnderTest.publishAsyncEvent(TEST_TOPIC, '12345','{}', 'OK', '200')
        and: 'the topic is polled'
            def records = consumer.poll(Duration.ofMillis(1500))
        then: 'the record received is the event sent'
            def record = records.iterator().next()
            DmiAsyncRequestResponseEvent event  = spiedObjectMapper.readValue(record.value(), DmiAsyncRequestResponseEvent)
        and: 'the status & code matches expected'
            assert event.getEventContent().getResponseStatus() == 'OK'
            assert event.getEventContent().getResponseCode() == '200'
    }

    def 'Publish and Subscribe message - failure'() {
        when: 'a failure event is published'
            def exception = new HttpClientRequestException('some cm handle', 'Node not found', HttpStatus.INTERNAL_SERVER_ERROR)
            objectUnderTest.publishAsyncFailureEvent(TEST_TOPIC, '67890', exception)
        and: 'the topic is polled'
            def records = consumer.poll(Duration.ofMillis(1500))
        then: 'the record received is the event sent'
            def record = records.iterator().next()
            DmiAsyncRequestResponseEvent event  = spiedObjectMapper.readValue(record.value(), DmiAsyncRequestResponseEvent)
        and: 'the status & code matches expected'
            assert event.getEventContent().getResponseStatus() == 'Internal Server Error'
            assert event.getEventContent().getResponseCode() == '500'
    }

    def 'Publish and Subscribe message - success'() {
        when: 'a successful event is published'
            objectUnderTest.publishAsyncEvent(unauthenticated.SEC_3GPP_PROVISIONING_OUTPUT, "{\r\n\"eventId\":\"stnddefined000001\",\r\n\"eventCorrelationId\":\"cmHandleId-nearrtric-22_cucpserver2\", // as is\r\n\"eventTime\":\"2021-08-23T11:52:10.6Z\",\r\n\"eventSource\":\"ncmp-datastore:passthrough-operational\",\r\n\"eventType\":\"org.onap.cps.ncmp.event.model.AvcEvent\",\r\n\"eventSchema\":\"org.onap.cps.ncmp.event.model.AvcEvent.rfc8641\",\r\n\"eventSchemaVersion\":\"1.0\",\r\n\"event\":{\r\n\"push-change-update\":{\r\n\"datastore-changes\":{\r\n\"ietf-yang-patch:yang-patch\":{\r\n\"edit\":[\r\n{\r\n\"edit-id\":123,\r\n\"operation\":\"replace\",\r\n\"value\":{\"isHOAllowed\":\"true\"},\r\n\"target\":\"/_3gpp-common-managed-element:ManagedElement=Kista-001/_3gpp-nr-nrm-gnbdufunction:GNBDUFunction=1/_3gpp-nr-nrm-nrcelldu:NRCellDU=1\"\r\n}\r\n],\r\n\"patch-id\":\"ff22d1d8-80ed-4db6-aedc-518d65ec0ed8\"\r\n}\r\n}\r\n}\r\n}\r\n}",'{}', 'OK', '200')
        and: 'the topic is polled'
            def records = consumer.poll(Duration.ofMillis(1500))
        then: 'the record received is the event sent'
            def record = records.iterator().next()
            AVCEvent event  = spiedObjectMapper.readValue(record.value(), AVCEvent)
        and: 'the status & code matches expected'
            assert event.getEventContent().getResponseStatus() == 'OK'
            assert event.getEventContent().getResponseCode() == '200'
    }

    def 'Publish and Subscribe message - failure'() {
        when: 'a failure event is published'
            def exception = new HttpClientRequestException('some cm handle', 'Node not found', HttpStatus.INTERNAL_SERVER_ERROR)
            objectUnderTest.publishAsyncFailureEvent(unauthenticated.SEC_3GPP_PROVISIONING_OUTPUT, '67890', exception)
        and: 'the topic is polled'
            def records = consumer.poll(Duration.ofMillis(1500))
        then: 'the record received is the event sent'
            def record = records.iterator().next()
            AVCEvent event  = spiedObjectMapper.readValue(record.value(), AVCEvent)
        and: 'the status & code matches expected'
            assert event.getEventContent().getResponseStatus() == 'Internal Server Error'
    }
}