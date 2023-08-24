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

package org.onap.cps.ncmp.dmi.notifications.cmsubscription;

import io.cloudevents.CloudEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.onap.cps.ncmp.dmi.notifications.mapper.CloudEventMapper;
import org.onap.cps.ncmp.events.cmsubscription1_0_0.dmi_to_ncmp.CmSubscriptionDmiOutEvent;
import org.onap.cps.ncmp.events.cmsubscription1_0_0.dmi_to_ncmp.Data;
import org.onap.cps.ncmp.events.cmsubscription1_0_0.dmi_to_ncmp.SubscriptionStatus;
import org.onap.cps.ncmp.events.cmsubscription1_0_0.ncmp_to_dmi.CmHandle;
import org.onap.cps.ncmp.events.cmsubscription1_0_0.ncmp_to_dmi.CmSubscriptionDmiInEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CmSubscriptionDmiInEventConsumer {

    @Value("${app.dmi.avc.subscription-response-topic}")
    private String cmAvcSubscriptionResponseTopic;
    @Value("${dmi.service.name}")
    private String dmiName;
    private final KafkaTemplate<String, CloudEvent> cloudEventKafkaTemplate;

    /**
     * Consume the specified event.
     *
     * @param cmSubscriptionDmiInCloudEvent the event to be consumed
     */
    @KafkaListener(topics = "${app.dmi.avc.subscription-topic}",
            containerFactory = "cloudEventConcurrentKafkaListenerContainerFactory")
    public void consumeCmSubscriptionDmiInEvent(
            final ConsumerRecord<String, CloudEvent> cmSubscriptionDmiInCloudEvent) {
        final CmSubscriptionDmiInEvent cmSubscriptionDmiInEvent =
                CloudEventMapper.toTargetEvent(cmSubscriptionDmiInCloudEvent.value(), CmSubscriptionDmiInEvent.class);
        if (cmSubscriptionDmiInEvent != null) {
            final String eventKey = cmSubscriptionDmiInCloudEvent.value().getId();
            final String subscriptionType = cmSubscriptionDmiInCloudEvent.value().getType();
            if ("subscriptionCreated".equals(subscriptionType)) {
                sendCmSubscriptionDmiOutEvent(eventKey, "subscriptionCreatedStatus",
                        formCmSubscriptionDmiOutEvent(cmSubscriptionDmiInEvent));
            } else if ("subscriptionDeleted".equals(subscriptionType)) {
                sendCmSubscriptionDmiOutEvent(eventKey, "subscriptionDeletedStatus",
                        formCmSubscriptionDmiOutEvent(cmSubscriptionDmiInEvent));
            }
        }
    }

    /**
     * Sends message to the configured topic.
     *
     * @param eventKey                  is the kafka message key
     * @param subscriptionType          is the type of subscription action
     * @param cmSubscriptionDmiOutEvent is the payload of the kafka message
     */
    public void sendCmSubscriptionDmiOutEvent(final String eventKey, final String subscriptionType,
            final CmSubscriptionDmiOutEvent cmSubscriptionDmiOutEvent) {
        cloudEventKafkaTemplate.send(cmAvcSubscriptionResponseTopic, eventKey,
                CmSubscriptionDmiOutEventToCloudEventMapper.toCloudEvent(cmSubscriptionDmiOutEvent, subscriptionType,
                        dmiName));
    }

    private CmSubscriptionDmiOutEvent formCmSubscriptionDmiOutEvent(
            final CmSubscriptionDmiInEvent cmSubscriptionDmiInEvent) {
        final CmSubscriptionDmiOutEvent cmSubscriptionDmiOutEvent = new CmSubscriptionDmiOutEvent();
        final Data cmSubscriptionDmiOutEventData = new Data();
        cmSubscriptionDmiOutEventData.setClientId(cmSubscriptionDmiInEvent.getData().getSubscription().getClientID());
        cmSubscriptionDmiOutEventData.setSubscriptionName(
                cmSubscriptionDmiInEvent.getData().getSubscription().getName());
        cmSubscriptionDmiOutEventData.setDmiName(dmiName);

        final List<CmHandle> cmHandles = cmSubscriptionDmiInEvent.getData().getPredicates().getTargets();
        cmSubscriptionDmiOutEventData.setSubscriptionStatus(populateSubscriptionStatus(extractCmHandleIds(cmHandles)));
        cmSubscriptionDmiOutEvent.setData(cmSubscriptionDmiOutEventData);
        return cmSubscriptionDmiOutEvent;
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
