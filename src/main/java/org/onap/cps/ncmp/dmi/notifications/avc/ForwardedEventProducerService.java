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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.onap.cps.ncmp.event.model.ForwardedEvent;
import org.springframework.stereotype.Service;

/**
 * ForwardedEventProducerService to publish on the request topic.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ForwardedEventProducerService {

    private final KafkaProducer<String, ForwardedEvent> producer;

    /**
     * Publish the ForwardedEvent into the request topic.
     *
     * @param producerRecord to be published
     */
    public void publish(final ProducerRecord<String, ForwardedEvent> producerRecord) {
        try {
            producer.send(producerRecord);
        } catch (final Exception exception) {
            log.error("Unable to publish message due to {}", exception.getClass().getName());
        } finally {
            producer.flush();
            producer.close();
        }
    }
}
