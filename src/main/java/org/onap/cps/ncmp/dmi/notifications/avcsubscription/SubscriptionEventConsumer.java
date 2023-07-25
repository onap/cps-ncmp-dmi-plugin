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

package org.onap.cps.ncmp.dmi.notifications.avcsubscription;

import io.cloudevents.CloudEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.Data;
import org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.SubscriptionEventResponse;
import org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.SubscriptionStatus;
import org.onap.cps.ncmp.events.avcsubscription1_0_0.ncmp_to_dmi.CmHandle;
import org.onap.cps.ncmp.events.avcsubscription1_0_0.ncmp_to_dmi.SubscriptionEvent;
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
    @Value("${dmi.service.name}")
    private String dmiName;
    private final KafkaTemplate<String, CloudEvent> cloudEventKafkaTemplate;

    /**
     * Consume the specified event.
     *
     * @param subscriptionCloudEvent the event to be consumed
     */
    @KafkaListener(topics = "${app.dmi.avc.subscription-topic}",
        containerFactory = "cloudEventConcurrentKafkaListenerContainerFactory")
    public void consumeSubscriptionEvent(final ConsumerRecord<String, CloudEvent> subscriptionCloudEvent) {
        final SubscriptionEvent subscriptionEvent =
            SubscriptionEventResponseMapper.toSubscriptionEvent(subscriptionCloudEvent.value());
        if (subscriptionEvent != null) {
            final String eventKey = subscriptionCloudEvent.value().getId();
            final String subscriptionType = subscriptionCloudEvent.value().getType();
            if ("subscriptionCreated".equals(subscriptionType)) {
                sendSubscriptionResponseMessage(eventKey, "subscriptionCreatedStatus",
                    formSubscriptionEventResponse(subscriptionEvent));
            } else if ("subscriptionDeleted".equals(subscriptionType)) {
                sendSubscriptionResponseMessage(eventKey, "subscriptionDeletedStatus",
                    formSubscriptionEventResponse(subscriptionEvent));
            }
        }
    }

    /**
     * Sends message to the configured topic.
     *
     * @param eventKey is the kafka message key
     * @param subscriptionType is the type of subscription action
     * @param subscriptionEventResponse is the payload of the kafka message
     */
    public void sendSubscriptionResponseMessage(final String eventKey,
                                                final String subscriptionType,
                                                final SubscriptionEventResponse subscriptionEventResponse) {
        cloudEventKafkaTemplate.send(cmAvcSubscriptionResponseTopic, eventKey,
            SubscriptionEventResponseMapper.toCloudEvent(subscriptionEventResponse, subscriptionType, dmiName));
    }

    private SubscriptionEventResponse formSubscriptionEventResponse(final SubscriptionEvent subscriptionEvent) {
        final SubscriptionEventResponse subscriptionEventResponse = new SubscriptionEventResponse();
        final Data subscriptionResponseData = new Data();
        subscriptionResponseData.setClientId(subscriptionEvent.getData().getSubscription().getClientID());
        subscriptionResponseData.setSubscriptionName(subscriptionEvent.getData().getSubscription().getName());
        subscriptionResponseData.setDmiName(dmiName);

        final List<CmHandle> cmHandles = subscriptionEvent.getData()
            .getPredicates().getTargets();
        subscriptionResponseData
                .setSubscriptionStatus(
                    populateSubscriptionStatus(
                        extractCmHandleIds(cmHandles)));
        subscriptionEventResponse.setData(subscriptionResponseData);
        return subscriptionEventResponse;
    }

    private Set<String> extractCmHandleIds(final List<CmHandle> cmHandles) {
        final Set<String> cmHandleIds = new HashSet<>();

        for (final CmHandle cmHandle : cmHandles) {
            cmHandleIds.add(cmHandle.getId());
        }
        return cmHandleIds;
    }

    private List<SubscriptionStatus> populateSubscriptionStatus(final Set<String> cmHandleIds) {
        final List<SubscriptionStatus> subscriptionStatuses = new ArrayList<>();
        for (final String cmHandleId : cmHandleIds) {
            final SubscriptionStatus status = new SubscriptionStatus();
            status.setId(cmHandleId);
            status.setStatus(SubscriptionStatus.Status.ACCEPTED);
            subscriptionStatuses.add(status);
        }
        return subscriptionStatuses;
    }

}
