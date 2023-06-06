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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.onap.cps.ncmp.events.avc.v1.AvcEvent;
import org.onap.cps.ncmp.events.avc.v1.AvcEventHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class DmiDataAvcEventProducer {

    private final KafkaTemplate<String, AvcEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Sends message to the configured topic with a message key.
     *
     * @param requestId      the request id
     * @param avcEventHeader avc event header
     * @param avcEvent       the actual avc event
     */
    public void sendMessageWithHeaders(final String requestId, final AvcEventHeader avcEventHeader,
            final AvcEvent avcEvent) {
        // convert to headers
        final Map<String, Object> avcEventHeadersMap = objectMapper.convertValue(avcEventHeader, Map.class);
        final ProducerRecord<String, AvcEvent> producerRecord =
                new ProducerRecord<>("dmi-cm-events", null, requestId, avcEvent,
                        convertToKafkaHeader(avcEventHeadersMap));
        kafkaTemplate.send(producerRecord);
        log.debug("AVC event sent");
    }

    private Collection<Header> convertToKafkaHeader(final Map<String, Object> eventHeaders) {
        final Collection<Header> headers = new ArrayList<>();
        eventHeaders.forEach((key, value) -> {
            final RecordHeader recordHeader = new RecordHeader(key, SerializationUtils.serialize(value));
            headers.add(recordHeader);
        });
        return headers;
    }


}
