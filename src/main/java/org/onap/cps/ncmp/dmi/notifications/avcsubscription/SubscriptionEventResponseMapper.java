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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.CloudEventUtils;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import java.net.URI;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.exception.CloudEventConstructionException;
import org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.SubscriptionEventResponse;
import org.onap.cps.ncmp.events.avcsubscription1_0_0.ncmp_to_dmi.SubscriptionEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class SubscriptionEventResponseMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Maps CloudEvent object to SubscriptionEvent object.
     *
     * @param cloudEvent object
     * @return SubscriptionEvent deserialized
     */
    public static SubscriptionEvent toSubscriptionEvent(final CloudEvent cloudEvent) {
        final PojoCloudEventData<SubscriptionEvent> deserializedCloudEvent =
            CloudEventUtils.mapData(cloudEvent,
                PojoCloudEventDataMapper.from(objectMapper, SubscriptionEvent.class));
        if (deserializedCloudEvent == null) {
            log.debug("No data found in the consumed subscription response event");
            return null;
        } else {
            final SubscriptionEvent subscriptionEvent = deserializedCloudEvent.getValue();
            log.debug("Consuming subscription response event {}", subscriptionEvent);
            return subscriptionEvent;
        }
    }

    /**
     * Maps SubscriptionEventResponse to a CloudEvent.
     *
     * @param subscriptionEventResponse object.
     * @param eventKey as String.
     * @return CloudEvent built.
     */
    public static CloudEvent toCloudEvent(
        final org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.SubscriptionEventResponse
            subscriptionEventResponse,
        final String eventKey,
        final String subscriptionType,
        final String dmiName) {
        try {
            return CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create(dmiName))
                .withType(subscriptionType)
                .withDataSchema(URI.create("urn:cps:" + SubscriptionEventResponse.class.getName() + ":1.0.0"))
                .withExtension("correlationid", eventKey)
                .withData(objectMapper.writeValueAsBytes(subscriptionEventResponse))
                .withSource(URI.create(subscriptionEventResponse.getData().getClientId()))
                .build();
        } catch (final Exception ex) {
            throw new CloudEventConstructionException("The Cloud Event could not be constructed", "Invalid object to "
                + "serialize or required headers is missing", ex);
        }
    }


}
