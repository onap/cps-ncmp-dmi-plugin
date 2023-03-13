/*
 * ============LICENSE_START=======================================================
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
import org.onap.cps.ncmp.dmi.service.model.SubscriptionEventResponse;
import org.onap.cps.ncmp.dmi.service.model.SubscriptionEventResponseStatus;
import org.onap.cps.ncmp.event.model.SubscriptionEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubscriptionEventConsumer {

    @Value("${app.dmi.avc.subscription-response-topic}")
    private String cmAvcSubscriptionResponseTopic;
    private final KafkaTemplate<String, SubscriptionEventResponse> kafkaTemplate;

    /**
     * Sends message to the configured topic.
     *
     * @param key is the unique identifier of the kafka message
     * @param value is the payload of the kafka message
     */
    public void sendSubscriptionResponseMessage(final String key,
                                             final SubscriptionEventResponse value) {
        kafkaTemplate.send(cmAvcSubscriptionResponseTopic, key, value);
    }

    /**
     * Consume the specified event.
     *
     * @param subscriptionEvent the event to be consumed
     */
    @KafkaListener(topics = "${app.dmi.avc.subscription-topic}",
            properties = {"spring.json.value.default.type=org.onap.cps.ncmp.event.model.SubscriptionEvent"})
    public void consumeSubscriptionEvent(final SubscriptionEvent subscriptionEvent) {
        final String clientId = subscriptionEvent.getEvent().getSubscription().getClientID();

        log.info("Subscription for ClientID {} is received by dmi plugin.", clientId);

        final SubscriptionEventResponse subscriptionEventResponse = new SubscriptionEventResponse();
        subscriptionEventResponse.setClientId(clientId);
        subscriptionEventResponse.setStatus(SubscriptionEventResponseStatus.ACCEPTED.getResponseStatus());

        sendSubscriptionResponseMessage("some-key", subscriptionEventResponse);
    }
}
