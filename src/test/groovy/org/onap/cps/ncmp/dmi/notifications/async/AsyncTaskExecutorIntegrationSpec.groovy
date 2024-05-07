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
import org.onap.cps.ncmp.dmi.model.DataAccessRequest
import org.onap.cps.ncmp.event.model.DmiAsyncRequestResponseEvent
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.spock.Testcontainers

import java.time.Duration
import java.util.function.Supplier

@SpringBootTest(classes = [AsyncTaskExecutor, DmiAsyncRequestResponseEventProducer])
@Testcontainers
@DirtiesContext
class AsyncTaskExecutorIntegrationSpec extends MessagingBaseSpec {

    @SpringBean
    DmiAsyncRequestResponseEventProducer cpsAsyncRequestResponseEventProducer =
        new DmiAsyncRequestResponseEventProducer(kafkaTemplate)

    def spiedObjectMapper = Spy(ObjectMapper)
    def mockSupplier = Mock(Supplier)

    def objectUnderTest = new AsyncTaskExecutor(cpsAsyncRequestResponseEventProducer)

    private static final String TEST_TOPIC = 'test-topic'

    def setup() {
        cpsAsyncRequestResponseEventProducer.dmiNcmpTopic = TEST_TOPIC
        kafkaConsumer.subscribe([TEST_TOPIC] as List<String>)
    }

    def cleanup() {
        kafkaConsumer.close()
    }

    def 'Publish and Subscribe message - success'() {
        when: 'a successful event is published'
            objectUnderTest.publishAsyncEvent(TEST_TOPIC, '12345','{}', 'OK', '200')
        and: 'the topic is polled'
            def records = kafkaConsumer.poll(Duration.ofMillis(1500))
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
            def records = kafkaConsumer.poll(Duration.ofMillis(1500))
        then: 'the record received is the event sent'
            def record = records.iterator().next()
            DmiAsyncRequestResponseEvent event  = spiedObjectMapper.readValue(record.value(), DmiAsyncRequestResponseEvent)
        and: 'the status & code matches expected'
            assert event.getEventContent().getResponseStatus() == 'Internal Server Error'
            assert event.getEventContent().getResponseCode() == '500'
    }

    def 'Execute an Async Task using asyncTaskExecutor and throw an error'() {
        given: 'A task to be executed'
            def requestId = '123456'
            def operationEnum = DataAccessRequest.OperationEnum.CREATE
            def timeOut = 100
        when: 'AsyncTask has been executed'
            objectUnderTest.executeAsyncTask(taskSupplierForFailingTask(), TEST_TOPIC, requestId, operationEnum, timeOut)
            def records = kafkaConsumer.poll(Duration.ofMillis(1500))
        then: 'the record received is the event sent'
            def record = records.iterator().next()
            DmiAsyncRequestResponseEvent event  = spiedObjectMapper.readValue(record.value(), DmiAsyncRequestResponseEvent)
        and: 'the status & code matches expected'
            assert event.getEventContent().getResponseStatus() == 'Internal Server Error'
            assert event.getEventContent().getResponseCode() == '500'

    }

    def taskSupplierForFailingTask() {
        return () -> { throw new RuntimeException('original exception message') }
    }

}