/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2023 Nordix Foundation
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.ncmp.dmi.notifications.avc;

import groovy.util.logging.Slf4j;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.onap.cps.ncmp.event.model.AvcEvent;
import org.onap.cps.ncmp.event.model.Event;

/**
 * Helper to create AvcEvents.
 */
@Slf4j
public class DmiDataAvcEventCreator {

    private static final DateTimeFormatter dateTimeFormatter
            = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    /**
     * Create an AVC event.
     *
     * @param eventCorrelationId  the event correlation id
     * @return DmiAsyncRequestResponseEvent
     */
    public AvcEvent createEvent(final String eventCorrelationId) {
        final AvcEvent avcEvent = new AvcEvent();
        avcEvent.setEventId(UUID.randomUUID().toString());
        avcEvent.setEventCorrelationId(eventCorrelationId);
        avcEvent.setEventType(AvcEvent.class.getName());
        avcEvent.setEventSchema("urn:cps:" + AvcEvent.class.getName());
        avcEvent.setEventSchemaVersion("v1");
        avcEvent.setEventTarget("NCMP");
        avcEvent.setEventTime(ZonedDateTime.now().format(dateTimeFormatter));

        final Event event = new Event();
        event.setAdditionalProperty("payload", "Hello world!");
        avcEvent.setEvent(event);

        return avcEvent;
    }

}