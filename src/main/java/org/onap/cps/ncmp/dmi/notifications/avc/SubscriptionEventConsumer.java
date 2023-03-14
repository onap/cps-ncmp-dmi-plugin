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
     * @param subscriptionEventResponse is the payload of the kafka message
     */
    public void sendSubscriptionResponseMessage(final SubscriptionEventResponse subscriptionEventResponse) {
        kafkaTemplate.send(cmAvcSubscriptionResponseTopic, subscriptionEventResponse);
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
        sendSubscriptionResponseMessage(formSubscriptionEventResponse(subscriptionEvent));
    }

    private SubscriptionEventResponse formSubscriptionEventResponse(final SubscriptionEvent subscriptionEvent) {
        final SubscriptionEventResponse subscriptionEventResponse = new SubscriptionEventResponse();
        subscriptionEventResponse.setClientId(subscriptionEvent.getEvent().getSubscription().getClientID());
        subscriptionEventResponse.setStatus(SubscriptionEventResponseStatus.ACCEPTED.getResponseStatus());
        final List<Object> cmHandleIdTocmHandlePropertyMap = subscriptionEvent.getEvent()
                .getPredicates()
                .getTargets();
        subscriptionEventResponse.setCmHandleIds(extractCmHandleIds(cmHandleIdTocmHandlePropertyMap));
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
}
