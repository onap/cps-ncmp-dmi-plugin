/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2022 Nordix Foundation
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
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.onap.cps.ncmp.dmi.exception.HttpClientRequestException
import org.onap.cps.ncmp.dmi.model.DataAccessRequest
import org.onap.cps.ncmp.event.model.DmiAsyncRequestResponseEvent
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Specification

import java.time.Duration

@SpringBootTest(classes = [AsyncTaskExecutor, DmiAsyncRequestResponseEventProducer])
@Testcontainers
@DirtiesContext
class AsyncTaskExecutorIntegrationSpec extends Specification {

    static kafkaTestContainer = new KafkaContainer(
        DockerImageName.parse('confluentinc/cp-kafka:6.2.1')
    )

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaTestContainer::stop))
    }

    def setupSpec() {
        kafkaTestContainer.start()
    }

    def producerConfigProperties = [
        'bootstrap.servers' : kafkaTestContainer.getBootstrapServers().split(',')[0],
        'retries'           : 0,
        'batch.size'        : 16384,
        'linger.ms'         : 1,
        'buffer.memory'     : 33554432,
        'key.serializer'    : StringSerializer,
        'value.serializer'  : JsonSerializer
    ]

    def consumerConfigProperties = [
        'bootstrap.servers' : kafkaTestContainer.getBootstrapServers().split(',')[0],
        'key.deserializer'  : StringDeserializer,
        'value.deserializer': StringDeserializer,
        'auto.offset.reset' : 'earliest',
        'group.id'          : 'test'
    ]

    def kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<Integer, String>(producerConfigProperties))

    @SpringBean
    DmiAsyncRequestResponseEventProducer cpsAsyncRequestResponseEventProducer =
        new DmiAsyncRequestResponseEventProducer(kafkaTemplate)

    KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(consumerConfigProperties)

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
            objectUnderTest.publishAsyncFailureEvent(TEST_TOPIC, '67890', DataAccessRequest.OperationEnum.READ, exception)
        and: 'the topic is polled'
            def records = consumer.poll(Duration.ofMillis(1500))
        then: 'the record received is the event sent'
            def record = records.iterator().next()
            DmiAsyncRequestResponseEvent event  = spiedObjectMapper.readValue(record.value(), DmiAsyncRequestResponseEvent)
        and: 'the status & code matches expected'
            assert event.getEventContent().getResponseStatus() == 'Internal Server Error'
            assert event.getEventContent().getResponseCode() == '500'
    }

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add('spring.kafka.bootstrap-servers', kafkaTestContainer::getBootstrapServers)
    }

}