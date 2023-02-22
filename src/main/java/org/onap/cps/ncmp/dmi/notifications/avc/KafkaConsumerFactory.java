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

package org.onap.cps.ncmp.dmi.notifications.avc;

import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.onap.cps.ncmp.dmi.config.DmiKafkaConfig;
import org.onap.cps.ncmp.event.model.ForwardedEvent;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Component;

/**
 * KafkaConsumerFactory creates and return KafkaConsumer instances using DmiKafkaConfig.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerFactory {

    private final DmiKafkaConfig dmiKafkaConfig;

    /**
     * Creates a new instance of KafkaConsumer with the type of ForwardedEvent.
     *
     * @return KafkaConsumer instance
     */
    public KafkaConsumer<String, ForwardedEvent> getForwardedEventKafkaConsumer() {
        return new KafkaConsumer<>(getKafkaProperties());
    }

    private Properties getKafkaProperties() {
        final Properties props = new Properties();
        log.debug("Configured dmi plugin properties : {}", dmiKafkaConfig);

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, dmiKafkaConfig.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, dmiKafkaConfig.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, dmiKafkaConfig.getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, dmiKafkaConfig.getValueDeserializer());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        return props;
    }
}
