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

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.event.model.AvcEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DmiAsyncRequestResponseEventConsumer {

    private static final DmiAsyncConsumerVesToAvcEventCreator dmiAsyncConsumerVesToAvcEventCreator
            = new DmiAsyncConsumerVesToAvcEventCreator();

    private final KafkaTemplate<String, AvcEvent> kafkaTemplate;

    /**
     * Kafka Listener consuming events from vescollector for "domain": "stndDefined".
     */
    @KafkaListener(topics = "${app.ves.consumer.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeFromTopic(final String msg) throws JsonProcessingException {

        log.info("consumed ves message: {},", msg);

        final AvcEvent avcEvent = dmiAsyncConsumerVesToAvcEventCreator.createEventFromVes(msg);


        sendMessage("cmHandleId-" + avcEvent.getEventCorrelationId(), avcEvent);


    }


    public void sendMessage(final String requestId, final AvcEvent avcEvent) {
        kafkaTemplate.send("dmi-cm-events", requestId, avcEvent);
        log.info("3gpp provisioning-AVC event sent");
    }

}
