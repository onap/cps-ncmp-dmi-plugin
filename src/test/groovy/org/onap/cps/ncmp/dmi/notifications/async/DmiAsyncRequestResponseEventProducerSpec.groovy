/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2022 Nordix Foundation
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

package org.onap.cps.ncmp.dmi.notifications.async

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.onap.cps.ncmp.dmi.exception.DmiException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.TimeoutException

@SpringBootTest
@Testcontainers
@DirtiesContext
class DmiAsyncRequestResponseEventProducerSpec extends Specification {

    static kafkaTestContainer = new KafkaContainer()

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaTestContainer::stop))
    }

    def setupSpec() {
        kafkaTestContainer.start()
    }

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate

    @Value('${app.ncmp.async-m2m.topic}')
    String topic

    @Autowired
    DmiAsyncRequestResponseEventProducer cpsAsyncRequestResponseEventProducer;

    KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(consumerConfig())

    def 'Publish and Subscribe message - success'() {
        given: 'a sample message and key'
            def dmiAsyncRequestResponseEventUtil = new DmiAsyncRequestResponseEventUtil()
            def message = dmiAsyncRequestResponseEventUtil.createEvent(
                '{"data" : { "property1" : "value1"}}', 'ncmp-async-m2m', '12345', 'SUCCESS', '200')
            def messageKey = 'message-key'
            def objectUnderTest = new DmiAsyncRequestResponseEventProducerService(cpsAsyncRequestResponseEventProducer)
        when: 'an event is published'
            objectUnderTest.publishAsyncEvent(messageKey, message)
        then: 'no exception is thrown'
            noExceptionThrown()
        and: 'we receive a message'
            consumer.subscribe([topic] as List<String>)
            def records = consumer.poll(Duration.ofMillis(250))
            for (record in records) {
                assert messageKey == record.key()
                assert message == record.value()
            }
    }

    def 'Publish and Subscribe message - failure'() {
        given: 'producer service is setup'
            def cpsAsyncRequestResponseProducerService = new DmiAsyncRequestResponseEventProducerService(cpsAsyncRequestResponseEventProducer)
            def objectUnderTest = new AsyncTaskExecutor(cpsAsyncRequestResponseProducerService)
        when: 'a failure event is published'
            objectUnderTest.publishAsyncFailureEvent(topic, "123", new DmiException("Node not found", "node not found"))
        then: 'an exception is thrown'
            def exception = thrown(DmiException)
            assert exception.getMessage() == "Internal Server Error."
        and: 'an event is sent to topic'
            consumer.subscribe([topic] as List<String>)
            def records = consumer.poll(Duration.ofMillis(250))
            for (record in records) {
                assert "123" == record.key()
            }
    }

    def 'Publish and Subscribe message - failure (timeout)'() {
        given: 'producer service is setup'
            def cpsAsyncRequestResponseProducerService = new DmiAsyncRequestResponseEventProducerService(cpsAsyncRequestResponseEventProducer)
            def objectUnderTest = new AsyncTaskExecutor(cpsAsyncRequestResponseProducerService)
        when: 'a failure event is published'
            objectUnderTest.publishAsyncFailureEvent(topic, "6789", new TimeoutException("Timed out"))
        then: 'an exception is thrown'
            def exception = thrown(DmiException)
            assert exception.getMessage() == "Request Timeout Error."
        and: 'an event is sent to topic'
            consumer.subscribe([topic] as List<String>)
            def records = consumer.poll(Duration.ofMillis(250))
            for (record in records) {
                assert "6789" == record.key()
            }
    }

    def consumerConfig() {
        return [
            'key.deserializer' : StringDeserializer.name,
            'value.deserializer' : JsonDeserializer.name,
            'bootstrap.servers' : kafkaTestContainer.getBootstrapServers().split(",")[0],
            'group.id' : "test"]
    }

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add('spring.kafka.bootstrap-servers', kafkaTestContainer::getBootstrapServers)
    }
}

@Configuration
class TopicConfig {
    @Bean
    NewTopic newTopic() {
        return new NewTopic("ncmp-async-m2m", 1, (short) 1);
    }
}

