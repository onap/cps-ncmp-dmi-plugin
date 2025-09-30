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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.net.URI;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.onap.cps.ncmp.dmi.exception.CloudEventConstructionException;
import org.onap.cps.ncmp.impl.datajobs.subscription.dmi_to_ncmp.DataJobSubscriptionDmiOutEvent;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DmiOutEventToCloudEventMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Maps SubscriptionEventResponse to a CloudEvent.
     *
     * @param dataJobSubscriptionDmiOutEvent event object.
     * @param subscriptionType               String of subscription type.
     * @param dmiName                        String of dmiName.
     * @param correlationId                  String of correlationId.
     * @return CloudEvent built.
     */
    public static CloudEvent toCloudEvent(final DataJobSubscriptionDmiOutEvent dataJobSubscriptionDmiOutEvent,
                                          final String subscriptionType, final String dmiName,
                                          final String correlationId) {
        try {
            return CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create(dmiName))
                .withType(subscriptionType)
                .withDataSchema(URI.create("urn:cps:org.onap.ncmp.dmi.cm.subscription:1.0.0"))
                .withExtension("correlationid", correlationId)
                .withData(objectMapper.writeValueAsBytes(dataJobSubscriptionDmiOutEvent)).build();
        } catch (final Exception ex) {
            throw new CloudEventConstructionException("The Cloud Event could not be constructed",
                "Invalid object passed", ex);
        }
    }

}
