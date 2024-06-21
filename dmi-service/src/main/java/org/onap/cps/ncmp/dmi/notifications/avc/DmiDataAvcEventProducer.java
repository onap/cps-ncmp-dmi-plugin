/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2023 Nordix Foundation
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

package org.onap.cps.ncmp.dmi.notifications.avc;


import io.cloudevents.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DmiDataAvcEventProducer {

    private final KafkaTemplate<String, CloudEvent> cloudEventKafkaTemplate;
    
    /**
     * Publishing DMI Data AVC event payload as CloudEvent.
     *
     * @param requestId     the request id
     * @param cloudAvcEvent event with data as DMI DataAVC event
     */
    public void publishDmiDataAvcCloudEvent(final String requestId, final CloudEvent cloudAvcEvent) {
        final ProducerRecord<String, CloudEvent> producerRecord =
                new ProducerRecord<>("dmi-cm-events", requestId, cloudAvcEvent);
        cloudEventKafkaTemplate.send(producerRecord);
        log.debug("AVC event sent");
    }
}
