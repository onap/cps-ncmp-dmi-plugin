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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.CloudEventUtils;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.onap.cps.ncmp.event.model.SubscriptionEvent;
import org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.Data;
import org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.SubscriptionEventResponse;
import org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.SubscriptionStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
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

    final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Consume the specified event.
     *
     * @param subscriptionCloudEvent the event to be consumed
     */
    @KafkaListener(topics = "${app.dmi.avc.subscription-topic}",
        containerFactory = "cloudEventConcurrentKafkaListenerContainerFactory")
    public void consumeSubscriptionEvent(final ConsumerRecord<String, CloudEvent> subscriptionCloudEvent,
                                         @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) final String eventKey,
                                         @Header(KafkaHeaders.RECEIVED_TIMESTAMP) final String timeStampReceived) {
        final PojoCloudEventData<SubscriptionEvent> deserializedCloudEvent =
            CloudEventUtils.mapData(subscriptionCloudEvent.value(),
            PojoCloudEventDataMapper.from(objectMapper, SubscriptionEvent.class));
        if (deserializedCloudEvent == null) {
            log.debug("No data found in the consumed event");
        } else {
            final SubscriptionEvent subscriptionEvent = deserializedCloudEvent.getValue();
            final String subscriptionName = subscriptionEvent.getEvent().getSubscription().getName();
            final Date dateAndTimeReceived = new Date(Long.parseLong(timeStampReceived));
            log.info("Subscription for SubscriptionName {} is received at {} by dmi plugin.", subscriptionName,
                dateAndTimeReceived);
            sendSubscriptionResponseMessage(eventKey, formSubscriptionEventResponse(subscriptionEvent));
        }
    }

    /**
     * Sends message to the configured topic.
     *
     * @param eventKey is the kafka message key
     * @param subscriptionEventResponse is the payload of the kafka message
     */
    public void sendSubscriptionResponseMessage(final String eventKey,
                                                final SubscriptionEventResponse subscriptionEventResponse) {
        cloudEventKafkaTemplate.send(cmAvcSubscriptionResponseTopic, eventKey,
            convertSubscriptionResponseEvent(eventKey, subscriptionEventResponse));
    }

    private CloudEvent convertSubscriptionResponseEvent(final String eventKey,
                                                        final SubscriptionEventResponse subscriptionEventResponse) {
        CloudEvent cloudEvent = null;

        try {
            cloudEvent = CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create(dmiName))
                .withType(SubscriptionEventResponse.class.getName())
                .withDataSchema(URI.create("urn:cps:" + SubscriptionEventResponse.class.getName() + ":1.0.0"))
                .withExtension("correlationid", eventKey)
                .withData(objectMapper.writeValueAsBytes(subscriptionEventResponse)).build();
        } catch (final Exception ex) {
            throw new RuntimeException("The Cloud Event could not be constructed.", ex);
        }

        return cloudEvent;
    }

    private SubscriptionEventResponse formSubscriptionEventResponse(final SubscriptionEvent subscriptionEvent) {
        final SubscriptionEventResponse subscriptionEventResponse = new SubscriptionEventResponse();
        final Data subscriptionResponseData = new Data();
        subscriptionResponseData.setClientId(subscriptionEvent.getEvent().getSubscription().getClientID());
        subscriptionResponseData.setSubscriptionName(subscriptionEvent.getEvent().getSubscription().getName());
        subscriptionResponseData.setDmiName(dmiName);

        final List<Object> cmHandleIdToCmHandlePropertyMap = subscriptionEvent.getEvent()
                .getPredicates()
                .getTargets();
        subscriptionResponseData
                .setSubscriptionStatus(populateSubscriptionStatus(extractCmHandleIds(cmHandleIdToCmHandlePropertyMap)));
        subscriptionEventResponse.setData(subscriptionResponseData);
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
