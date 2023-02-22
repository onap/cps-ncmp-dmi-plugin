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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.event.model.ResponseData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Service;

/**
 * SubscriptionEventResponseProducerService publishes subscription event response to the corresponding topic.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionEventResponseProducerService {

    private final SubscriptionEventResponseProducer subscriptionEventResponseProducer;

    @Value("${app.dmi.avc-response.topic:dmi-ncmp-cm-avc-subscription}")
    private String topicName;

    /**
     * Publish the ResponseData to the corresponding topic.
     *
     * @param messageKey is a Kafka message key
     * @param messageValue  is Kafka message value that contains DMI details
     */
    public void publishSubscriptionEventResponse(final String messageKey, final ResponseData messageValue) {
        try {
            subscriptionEventResponseProducer.publishResponseEventMessage(topicName, messageKey, messageValue);
        } catch (final KafkaException e) {
            log.error("Unable to publish subscription event response message to topic : {} and cause : {}", topicName, e.getMessage());
        }
    }
}
