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

package org.onap.cps.ncmp.dmi.service

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
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

import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG

@SpringBootTest
@Testcontainers
@DirtiesContext
class NcmpKafkaPublisherSpec extends Specification {

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

    KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(kafkaConsumerConfig())

    def 'Publish and Subscribe message'() {
        given: 'a sample messsage and key'
            def message = 'sample message'
            def messageKey = 'message-key'
            def objectUnderTest = new NcmpKafkaPublisher(kafkaTemplate, topic)
        when: 'a message is published'
            objectUnderTest.sendMessage(messageKey, message)
        then: 'a message is consumed'
            consumer.subscribe([topic] as List<String>)
            def records = consumer.poll(Duration.ofMillis(1000))
            assert records.size() == 1
            assert messageKey == records[0].key
            assert message == records[0].value
    }

    def kafkaConsumerConfig() {
        def configs = [:]
        configs.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.name)
        configs.put(VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.name)
        configs.put(AUTO_OFFSET_RESET_CONFIG, 'earliest')
        configs.put(BOOTSTRAP_SERVERS_CONFIG, kafkaTestContainer.getBootstrapServers().split(",")[0])
        configs.put(GROUP_ID_CONFIG, 'test')
        return configs
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
