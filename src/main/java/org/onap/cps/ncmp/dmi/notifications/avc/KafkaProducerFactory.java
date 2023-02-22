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
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.onap.cps.ncmp.dmi.config.DmiKafkaConfig;
import org.onap.cps.ncmp.event.model.ForwardedEvent;
import org.springframework.stereotype.Component;

/**
 * KafkaProducerFactory creates and return KafkaProducer instances using DmiKafkaConfig.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducerFactory {

    private final DmiKafkaConfig dmiKafkaConfig;

    /**
     * Creates a new instance of KafkaProducer with the type of ForwardedEvent.
     *
     * @return KafkaProducer instance
     */
    public KafkaProducer<String, ForwardedEvent> getForwardedEventKafkaProducer() {
        return new KafkaProducer<>(getKafkaProperties());
    }

    /**
     * Creates a new instance of Properties with given parameters in dmi kafka configuration.
     *
     * @return Properties instance
     */
    public Properties getKafkaProperties() {
        final Properties props = new Properties();
        log.debug("Configured dmi plugin properties : {}", dmiKafkaConfig);

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, dmiKafkaConfig.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, dmiKafkaConfig.getKeySerializer());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, dmiKafkaConfig.getValueSerializer());
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.valueOf(3));

        return props;
    }
}
