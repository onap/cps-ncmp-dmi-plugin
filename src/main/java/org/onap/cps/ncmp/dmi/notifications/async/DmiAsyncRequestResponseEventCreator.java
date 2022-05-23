/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2022 Nordix Foundation
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

package org.onap.cps.ncmp.dmi.notifications.async;

import com.google.gson.Gson;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.UUID;
import org.onap.cps.ncmp.event.model.DmiAsyncRequestResponseEvent;
import org.onap.cps.ncmp.event.model.EventContent;

/**
 * Helper to create DmiAsyncRequestResponseEvent.
 */
public class DmiAsyncRequestResponseEventCreator {

    private static final DateTimeFormatter dateTimeFormatter
        = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    /**
     * Create an event.
     *
     * @param resourceDataAsJson the resource data as json
     * @param topicParamInQuery the topic to send response to
     * @param requestId the request id
     * @param status the status of the request
     * @param code the code of the response
     *
     * @return DmiAsyncRequestResponseEvent
     */
    public DmiAsyncRequestResponseEvent createEvent(final String resourceDataAsJson,
                                                    final String topicParamInQuery,
                                                    final String requestId,
                                                    final String status,
                                                    final String code) {
        final DmiAsyncRequestResponseEvent dmiAsyncRequestResponseEvent = new DmiAsyncRequestResponseEvent();

        dmiAsyncRequestResponseEvent.setEventId(UUID.randomUUID().toString());
        dmiAsyncRequestResponseEvent.setEventCorrelationId(requestId);
        dmiAsyncRequestResponseEvent.setEventType(DmiAsyncRequestResponseEvent.class.getName());
        dmiAsyncRequestResponseEvent.setEventSchema("urn:cps:" + DmiAsyncRequestResponseEvent.class.getName() + ":v1");
        dmiAsyncRequestResponseEvent.setEventSource("org.onap.cps.ncmp.dmi");
        dmiAsyncRequestResponseEvent.setEventTarget(topicParamInQuery);
        dmiAsyncRequestResponseEvent.setEventTime(ZonedDateTime.now().format(dateTimeFormatter));
        dmiAsyncRequestResponseEvent.setEventContent(getEventContent(resourceDataAsJson, status, code));

        return dmiAsyncRequestResponseEvent;
    }

    private EventContent getEventContent(final String resourceDataAsJson, final String status, final String code) {
        final EventContent eventContent = new EventContent();

        eventContent.setResponseDataSchema("urn:cps:org.onap.cps:async-request-response-event-schema:v1");
        eventContent.setResponseStatus(status);
        eventContent.setResponseCode(code);

        // TODO: Discuss with Sourabh about GSON
        final HashMap<String, Object> responseData = new Gson().fromJson(resourceDataAsJson, HashMap.class);
        eventContent.setAdditionalProperty("response-data", responseData);

        return eventContent;
    }

}
