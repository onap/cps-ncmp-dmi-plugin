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


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Specification

@SpringBootTest
@Testcontainers
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

    def 'sample test'() {
        given: 'a sample messsage and key'
            def message = 'sample message'
            def messageKey = 'message-key'
            def objectUnderTest = new NcmpKafkaPublisher(kafkaTemplate, topic)
        when: 'a message is published'
            objectUnderTest.sendMessage(messageKey, message)
        then: 'no exception is thrown'
            noExceptionThrown()

    }

    @DynamicPropertySource
    static void registerKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add('spring.kafka.bootstrap-servers', kafkaTestContainer::getBootstrapServers)
    }
}
