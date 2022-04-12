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

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.onap.cps.event.model.NcmpAsyncRequestResponseEvent
import org.onap.cps.ncmp.dmi.exception.DmiException
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Specification

import java.time.Duration

@SpringBootTest(classes = [DmiAsyncRequestResponseEventProducer])
@Testcontainers
@DirtiesContext
class DmiAsyncRequestResponseEventProducerSpec extends Specification {

    private static final String CPS_ASYNC_EVENT_TOPIC_NAME = 'ncmp-async-m2m'

    static kafkaTestContainer = new KafkaContainer(
        DockerImageName.parse('confluentinc/cp-kafka:6.2.1')
    )

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaTestContainer::stop))
    }

    def setupSpec() {
        kafkaTestContainer.start()
    }

    KafkaTemplate<String, NcmpAsyncRequestResponseEvent> spiedKafkaTemplate = Spy(createTemplate())

    @SpringBean
    DmiAsyncRequestResponseEventProducer cpsAsyncRequestResponseEventProducer =
        new DmiAsyncRequestResponseEventProducer(spiedKafkaTemplate)

    KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(getConsumerConfig())

    def 'Publish and Subscribe message - success'() {
        given: 'a sample message and key'
            def dmiAsyncRequestResponseEventUtil = new DmiAsyncRequestResponseEventCreator()
            def testEventSent = dmiAsyncRequestResponseEventUtil.createEvent(
                "{'data' : { 'property1' : 'value1'}}", 'ncmp-async-m2m', '12345', 'SUCCESS', '200')
            def messageKey = 'message-key'
            def objectUnderTest = new DmiAsyncRequestResponseEventProducerService(cpsAsyncRequestResponseEventProducer)
        when: 'an event is published'
            objectUnderTest.publishAsyncEvent(messageKey, testEventSent)
        then: 'no exception is thrown'
            noExceptionThrown()
        and: 'the topic is polled'
            def records = consumer.poll(Duration.ofMillis(250))
        then: 'poll returns one record'
            assert records.size() == 1
        and: 'the record received is the event sent'
            def record = records.iterator().next()
            assert testEventSent == record.value() as NcmpAsyncRequestResponseEvent
    }

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add('spring.kafka.bootstrap-servers', kafkaTestContainer::getBootstrapServers)
    }

    private KafkaTemplate<Integer, String> createTemplate() {
        Map<String, Object> producerProps = producerConfigProps();
        ProducerFactory<Integer, String> producerFactory =
            new DefaultKafkaProducerFactory<Integer, String>(producerProps);
        KafkaTemplate<Integer, String> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        return kafkaTemplate;
    }

    def producerConfigProps() {
        Map<String, Object> producerConfigProps = new HashMap<>();
        producerConfigProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaTestContainer.getBootstrapServers().split(',')[0]);
        producerConfigProps.put(ProducerConfig.RETRIES_CONFIG, 0);
        producerConfigProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        producerConfigProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        producerConfigProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        producerConfigProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer);
        producerConfigProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer);
        return producerConfigProps;
    }

    def getConsumerConfig() {
        Map<String, Object> consumerConfigProps = new HashMap<>();
        consumerConfigProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaTestContainer.getBootstrapServers().split(',')[0]);
        consumerConfigProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer);
        consumerConfigProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer);
        consumerConfigProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, 'earliest');
        consumerConfigProps.put(ConsumerConfig.GROUP_ID_CONFIG, 'test');
        return consumerConfigProps;
    }
}

@Configuration
class TopicConfig {
    @Bean
    NewTopic newTopic() {
        return new NewTopic(DmiAsyncRequestResponseEventProducerSpec.CPS_ASYNC_EVENT_TOPIC_NAME, 1, (short) 1);
    }
}
