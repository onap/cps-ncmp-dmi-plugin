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

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
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
    private final ProducerRecord<String, ForwardedEvent> producerRecord;

    /**
     * Publish the ForwardedEvent into the request topic.
     */
    public void publish() {
        try {
            final RecordMetadata recordMetadata = producer.send(producerRecord)
                    .get(2, TimeUnit.SECONDS);

            log.debug("Publish completed in : {} to the topic {} with offset : {} and partition {} ",
                    new Date(recordMetadata.timestamp()), recordMetadata.topic(), recordMetadata.offset(),
                    recordMetadata.partition());

        } catch (ExecutionException | InterruptedException | TimeoutException exception) {
            log.error("Unable to publish message due to {}", exception.getClass().getName());
        } finally {
            producer.flush();
            producer.close();
        }
    }
}
