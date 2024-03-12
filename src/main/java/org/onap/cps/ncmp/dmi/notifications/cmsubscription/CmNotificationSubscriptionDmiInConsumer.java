/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2024 Nordix Foundation
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
import org.onap.cps.ncmp.dmi.notifications.mapper.CloudEventMapper;
import org.onap.cps.ncmp.events.cmnotificationsubscription_merge1_0_0.dmi_to_ncmp.CmNotificationSubscriptionDmiOutEvent;
import org.onap.cps.ncmp.events.cmnotificationsubscription_merge1_0_0.dmi_to_ncmp.Data;
import org.onap.cps.ncmp.events.cmnotificationsubscription_merge1_0_0.ncmp_to_dmi.CmNotificationSubscriptionDmiInEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CmNotificationSubscriptionDmiInConsumer {


    @Value("${app.dmi.avc.subscription-response-topic}")
    String cmAvcSubscriptionResponseTopic;
    @Value("${dmi.service.name}")
    String dmiName;
    private final KafkaTemplate<String, CloudEvent> cloudEventKafkaTemplate;

    /**
     * Consume the specified event.
     *
     * @param cmNotificationSubscriptionDmiInCloudEvent the event to be consumed
     */
    @KafkaListener(topics = "${app.dmi.avc.subscription-topic}",
        containerFactory = "cloudEventConcurrentKafkaListenerContainerFactory")
    public void consumeCmSubscriptionDmiInEvent(
        final ConsumerRecord<String, CloudEvent> cmNotificationSubscriptionDmiInCloudEvent) {
        final CmNotificationSubscriptionDmiInEvent cmSubscriptionDmiInEvent =
            CloudEventMapper.toTargetEvent(cmNotificationSubscriptionDmiInCloudEvent.value(),
                CmNotificationSubscriptionDmiInEvent.class);
        if (cmSubscriptionDmiInEvent != null) {
            final String eventKey = cmNotificationSubscriptionDmiInCloudEvent.value().getId();
            final String subscriptionType = cmNotificationSubscriptionDmiInCloudEvent.value().getType();
            final String correlationId = String.valueOf(cmNotificationSubscriptionDmiInCloudEvent.value()
                .getExtension("correlationid"));

            if ("subscriptionCreated".equals(subscriptionType)) {
                createAndSendCmNotificationSubscriptionDmiOutEvent(eventKey, "subscriptionCreatedStatus",
                    correlationId);
            } else if ("subscriptionDeleted".equals(subscriptionType)) {
                createAndSendCmNotificationSubscriptionDmiOutEvent(eventKey, "subscriptionDeletedStatus",
                    correlationId);
            }
        }
    }

    public void createAndSendCmNotificationSubscriptionDmiOutEvent(final String eventKey, final String subscriptionType,
                                              final String correlationId) {
        final CmNotificationSubscriptionDmiOutEvent cmNotificationSubscriptionDmiOutEvent =
            new CmNotificationSubscriptionDmiOutEvent();
        final Data cmNotificationSubscriptionDmiOutEventData = new Data();
        cmNotificationSubscriptionDmiOutEventData.setStatusCode("1");
        cmNotificationSubscriptionDmiOutEventData.setStatusMessage("Success");
        cmNotificationSubscriptionDmiOutEvent.setData(cmNotificationSubscriptionDmiOutEventData);

        cloudEventKafkaTemplate.send(cmAvcSubscriptionResponseTopic, eventKey,
            CmNotificationSubscriptionDmiOutEventToCloudEventMapper.toCloudEvent(cmNotificationSubscriptionDmiOutEvent,
                subscriptionType, dmiName, correlationId));

    }



}
