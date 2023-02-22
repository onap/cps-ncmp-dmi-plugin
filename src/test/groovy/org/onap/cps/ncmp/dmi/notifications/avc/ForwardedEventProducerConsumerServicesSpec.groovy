/*
 *  ============LICENSE_START=======================================================
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

import java.time.Duration
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.config.DmiKafkaConfig
import org.onap.cps.ncmp.event.model.ForwardedEvent
import spock.lang.Specification

class ForwardedEventProducerConsumerServicesSpec extends Specification {

    def kafkaConfigProducer = DmiKafkaConfig.builder()
        .bootstrapServers('localhost:19092')
        .keySerializer('org.apache.kafka.common.serialization.StringSerializer')
        .valueSerializer('org.springframework.kafka.support.serializer.JsonSerializer')
        .build();

    def kafkaProducerFactory = new KafkaProducerFactory(kafkaConfigProducer)
    def producer = kafkaProducerFactory.getForwardedEventKafkaProducer()
    def topicName = 'ncmp-dmi-cm-avc-subscription'
    def messageKey = UUID.randomUUID().toString()

    def messageValueJson = TestUtils.getResourceFileContent('avcSubscriptionCreationForwardedEvent.json')
    def objectMapper = new ObjectMapper()
    def testEventSent = objectMapper.readValue(messageValueJson, ForwardedEvent.class)

    def producerRecord = new ProducerRecord<String, ForwardedEvent>(topicName, messageKey, testEventSent)
    def producerService = new ForwardedEventProducerService(producer, producerRecord)

    def kafkaConfigConsumer = DmiKafkaConfig.builder()
        .bootstrapServers('localhost:19092')
        .groupId('ncmp-group')
        .keyDeserializer('org.apache.kafka.common.serialization.StringDeserializer')
        .valueDeserializer('org.springframework.kafka.support.serializer.JsonDeserializer')
        .build();

    def kafkaConsumerFactory = new KafkaConsumerFactory(kafkaConfigConsumer)
    def consumer = kafkaConsumerFactory.getForwardedEventKafkaConsumer()
    def consumerService = new ForwardedEventConsumerService(consumer)

    def 'Publish and consume forwarded subscription event successfully'() {
        given: 'a simulated forwarded event published to the topic'
            producerService.publish()
        and: 'a consumer subscribed to the topic'
            consumerService.subscribe(topicName)
        when: 'the forwarded event is consumed by polling in two seconds'
            def record = consumer.poll(Duration.ofMillis(1500)).iterator().next()
        then: 'the record has correct topic'
            assert topicName == record.topic
        and: 'the simulated message value is equal to consumed value'
            assert testEventSent.equals(record.value())

    }
}
