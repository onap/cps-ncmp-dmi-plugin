/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2024-2025 OpenInfra Foundation Europe. All rights reserved.
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
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.onap.cps.ncmp.dmi.notifications.cmsubscription.model.CmNotificationSubscriptionStatus;
import org.onap.cps.ncmp.dmi.notifications.mapper.CloudEventMapper;
import org.onap.cps.ncmp.impl.datajobs.subscription.dmi_to_ncmp.Data;
import org.onap.cps.ncmp.impl.datajobs.subscription.dmi_to_ncmp.DataJobSubscriptionDmiOutEvent;
import org.onap.cps.ncmp.impl.datajobs.subscription.ncmp_to_dmi.DataJobSubscriptionDmiInEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DmiInEventConsumer {


    @Value("${app.dmi.avc.cm-subscription-dmi-out}")
    private String dmoOutEventTopic;
    @Value("${dmi.service.name}")
    private String dmiName;
    private final KafkaTemplate<String, CloudEvent> cloudEventKafkaTemplate;

    /**
     * Consume the DmiInCloudEvent.
     *
     * @param dmiInCloudEvent the event to be consumed
     */
    @KafkaListener(topics = "${app.dmi.avc.cm-subscription-dmi-in}",
        containerFactory = "cloudEventConcurrentKafkaListenerContainerFactory")
    public void consumeDmiInEvent(
        final ConsumerRecord<String, CloudEvent> dmiInCloudEvent) {
        final DataJobSubscriptionDmiInEvent dataJobSubscriptionDmiInEvent =
            CloudEventMapper.toTargetEvent(dmiInCloudEvent.value(),
                    DataJobSubscriptionDmiInEvent.class);
        if (dataJobSubscriptionDmiInEvent != null) {
            final String subscriptionId = dmiInCloudEvent.value().getId();
            final String subscriptionType = dmiInCloudEvent.value().getType();
            final String correlationId = String.valueOf(dmiInCloudEvent.value()
                .getExtension("correlationid"));

            if ("subscriptionCreateRequest".equals(subscriptionType)) {
                createAndSendCmNotificationSubscriptionDmiOutEvent(subscriptionId, "subscriptionCreateResponse",
                    correlationId, CmNotificationSubscriptionStatus.ACCEPTED);
            } else if ("subscriptionDeleteRequest".equals(subscriptionType)) {
                createAndSendCmNotificationSubscriptionDmiOutEvent(subscriptionId, "subscriptionDeleteResponse",
                    correlationId, CmNotificationSubscriptionStatus.ACCEPTED);
            }
        }
    }

    /**
     * Create Dmi out event object and send to response topic.
     *
     * @param eventKey the events key
     * @param subscriptionType the subscriptions type
     * @param correlationId the events correlation Id
     * @param cmNotificationSubscriptionStatus subscriptions status accepted/rejected
     */
    public void createAndSendCmNotificationSubscriptionDmiOutEvent(
        final String eventKey, final String subscriptionType, final String correlationId,
        final CmNotificationSubscriptionStatus cmNotificationSubscriptionStatus) {

        final DataJobSubscriptionDmiOutEvent dataJobSubscriptionDmiOutEvent =
            new DataJobSubscriptionDmiOutEvent();
        final Data dmiOutEventData = new Data();

        if (cmNotificationSubscriptionStatus.equals(CmNotificationSubscriptionStatus.ACCEPTED)) {
            dmiOutEventData.setStatusCode("1");
            dmiOutEventData.setStatusMessage("ACCEPTED");
        } else {
            dmiOutEventData.setStatusCode("104");
            dmiOutEventData.setStatusMessage("REJECTED");
        }
        dataJobSubscriptionDmiOutEvent.setData(dmiOutEventData);

        cloudEventKafkaTemplate.send(dmoOutEventTopic, eventKey,
            DmiOutEventToCloudEventMapper.toCloudEvent(dataJobSubscriptionDmiOutEvent,
                subscriptionType, dmiName, correlationId));

    }



}
