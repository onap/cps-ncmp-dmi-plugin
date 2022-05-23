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
import org.onap.cps.ncmp.event.model.NcmpAsyncRequestResponseEvent
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Specification

import java.time.Duration

@SpringBootTest(classes = [DmiAsyncRequestResponseEventProducer])
@Testcontainers
@DirtiesContext
class DmiAsyncRequestResponseEventProducerSpec extends Specification {

    static kafkaTestContainer = new GenericContainer (
        DockerImageName.parse('confluentinc/cp-kafka:6.1.1')
    )

    static zookeeperTestContainer = new GenericContainer (
        DockerImageName.parse('confluentinc/cp-zookeeper:6.1.1')
    )

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaTestContainer::stop))
    }

    def setupSpec()  {
//        zookeeperTestContainer.addEnv('ZOOKEEPER_CLIENT_PORT', '2181')
        zookeeperTestContainer.withExposedPorts(2181).start()
        kafkaTestContainer.addEnv('KAFKA_ZOOKEEPER_CONNECT', 'localhost:2181')
        kafkaTestContainer.withExposedPorts( 9092, 19092).start()
    }

    def producerConfigProperties = [
        (ProducerConfig.BOOTSTRAP_SERVERS_CONFIG)      : 'localhost:9092',
        (ProducerConfig.RETRIES_CONFIG)                : 0,
        (ProducerConfig.BATCH_SIZE_CONFIG)             : 16384,
        (ProducerConfig.LINGER_MS_CONFIG)              : 1,
        (ProducerConfig.BUFFER_MEMORY_CONFIG)          : 33554432,
        (ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG)   : StringSerializer,
        (ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG) : JsonSerializer
    ]

    def consumerConfigProperties = [
        (ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG)       : 'localhost:9092',
        (ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG)  : StringDeserializer,
        (ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG): StringDeserializer,
        (ConsumerConfig.AUTO_OFFSET_RESET_CONFIG)       : 'earliest',
        (ConsumerConfig.GROUP_ID_CONFIG)                : 'test'
    ]

    def kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<Integer, String>(producerConfigProperties))

    @SpringBean
    DmiAsyncRequestResponseEventProducer cpsAsyncRequestResponseEventProducer =
        new DmiAsyncRequestResponseEventProducer(kafkaTemplate)

    KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(consumerConfigProperties)

    def 'Publish and Subscribe message - success'() {
        given: 'a sample message and key'
            def dmiAsyncRequestResponseEventUtil = new DmiAsyncRequestResponseEventCreator()
            def testEventSent = dmiAsyncRequestResponseEventUtil.createEvent(
                "{'data' : { 'property1' : 'value1'}}", 'test-topic', '12345', 'SUCCESS', '200')
            def messageKey = 'message-key'
            def objectUnderTest = new DmiAsyncRequestResponseEventProducerService(cpsAsyncRequestResponseEventProducer)
        when: 'an event is published'
            objectUnderTest.publishAsyncEvent(messageKey, testEventSent)
        then: 'no exception is thrown'
            noExceptionThrown()
        and: 'the topic is polled'
        //        consumer.subscribe(singletonList(topicName));
            def records = consumer.poll(Duration.ofMillis(250))
        then: 'poll returns one record'
            assert records.size() == 1
        and: 'the record received is the event sent'
            def record = records.iterator().next()
            assert testEventSent == record.value() as NcmpAsyncRequestResponseEvent
    }

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add('spring.kafka.bootstrap-servers', kafkaTestContainer::getBootstrapServers)
    }

}

@Configuration
class TopicConfig {
    @Bean
    NewTopic newTopic() {
        return new NewTopic('test-topic', 1, (short) 1);
    }
}