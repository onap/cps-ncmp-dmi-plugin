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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.service.model.SubscriptionEventResponse;
import org.onap.cps.ncmp.dmi.service.model.SubscriptionEventResponseStatus;
import org.onap.cps.ncmp.event.model.SubscriptionEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SubscriptionEventConsumer {

    @Value("${app.dmi.avc.subscription-response-topic}")
    private String cmAvcSubscriptionResponseTopic;
    @Value("${dmi.service.name}")
    private String dmiName;
    private final KafkaTemplate<String, SubscriptionEventResponse> kafkaTemplate;

    /**
     * Consume the specified event.
     *
     * @param subscriptionEvent the event to be consumed
     */
    @KafkaListener(topics = "${app.dmi.avc.subscription-topic}",
            properties = {"spring.json.value.default.type=org.onap.cps.ncmp.event.model.SubscriptionEvent"})
    public void consumeSubscriptionEvent(@Payload final SubscriptionEvent subscriptionEvent,
                                         @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) final String eventKey,
                                         @Header(KafkaHeaders.RECEIVED_TIMESTAMP) final String timeStampReceived) {
        final Date dateAndTimeReceived = new Date(Long.parseLong(timeStampReceived));
        final String subscriptionName = subscriptionEvent.getEvent().getSubscription().getName();
        log.info("Subscription for SubscriptionName {} is received at {} by dmi plugin.", subscriptionName,
                dateAndTimeReceived);
        sendSubscriptionResponseMessage(eventKey, formSubscriptionEventResponse(subscriptionEvent));
    }

    /**
     * Sends message to the configured topic.
     *
     * @param eventKey is the kafka message key
     * @param subscriptionEventResponse is the payload of the kafka message
     */
    public void sendSubscriptionResponseMessage(final String eventKey,
                                                final SubscriptionEventResponse subscriptionEventResponse) {
        kafkaTemplate.send(cmAvcSubscriptionResponseTopic, eventKey, subscriptionEventResponse);
    }

    private SubscriptionEventResponse formSubscriptionEventResponse(final SubscriptionEvent subscriptionEvent) {
        final SubscriptionEventResponse subscriptionEventResponse = new SubscriptionEventResponse();
        subscriptionEventResponse.setClientId(subscriptionEvent.getEvent().getSubscription().getClientID());
        subscriptionEventResponse.setSubscriptionName(subscriptionEvent.getEvent().getSubscription().getName());
        subscriptionEventResponse.setDmiName(dmiName);
        final List<Object> cmHandleIdToCmHandlePropertyMap = subscriptionEvent.getEvent()
                .getPredicates()
                .getTargets();
        subscriptionEventResponse
                .setCmHandleIdToStatus(populateCmHandleIdToStatus(extractCmHandleIds(cmHandleIdToCmHandlePropertyMap)));
        return subscriptionEventResponse;
    }

    private Set<String> extractCmHandleIds(final List<Object> cmHandleIdTocmHandlePropertyMap) {
        final Set<String> cmHandleIds = new HashSet<>();
        for (final Object obj: cmHandleIdTocmHandlePropertyMap) {
            final Map<String, Object> cmHandleIdToPropertiesMap = (Map<String, Object>) obj;
            cmHandleIds.addAll(cmHandleIdToPropertiesMap.keySet());
        }
        return cmHandleIds;
    }

    private Map<String, String> populateCmHandleIdToStatus(final Set<String> cmHandleIds) {
        final Map<String, String> result = new HashMap<>();
        for (final String cmHandleId : cmHandleIds) {
            result.put(cmHandleId, SubscriptionEventResponseStatus.ACCEPTED.getResponseStatus());
        }
        return result;
    }
}
