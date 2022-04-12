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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.UUID;
import org.onap.cps.event.model.DmiAsyncRequestResponseEvent;
import org.onap.cps.event.model.EventContent;

/**
 * Helper to create DmiAsyncRequestResponseEvent.
 */
public class DmiAsyncRequestResponseEventUtil {

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

        dmiAsyncRequestResponseEvent.setEventId(requestId);
        dmiAsyncRequestResponseEvent.setEventCorrelationId(UUID.randomUUID().toString());
        dmiAsyncRequestResponseEvent.setEventType("org.onap.ncmp.async-request-response-event");
        dmiAsyncRequestResponseEvent.setEventSchema("urn:cps:org.onap.cps:async-request-response-event-schema:v1");
        dmiAsyncRequestResponseEvent.setEventSource("org.onap.ncmp");
        dmiAsyncRequestResponseEvent.setEventTarget(topicParamInQuery);
        dmiAsyncRequestResponseEvent.setEventTime(getEventDateTime());
        dmiAsyncRequestResponseEvent.setEventType(DmiEvents.ASYNC_REQUEST.toString());
        dmiAsyncRequestResponseEvent.setEventContent(getEventContent(resourceDataAsJson, status, code));

        return dmiAsyncRequestResponseEvent;
    }

    private EventContent getEventContent(final String resourceDataAsJson, final String status, final String code) {
        final EventContent eventContent = new EventContent();

        eventContent.setResponseDataSchema("urn:cps:org.onap.cps:async-request-response-event-schema:v1");
        eventContent.setResponseStatus(status);
        eventContent.setResponseCode(code);

        final JsonObject jsonObject = JsonParser.parseString(resourceDataAsJson).getAsJsonObject();
        final HashMap<String, Object> responseData = new Gson().fromJson(jsonObject, HashMap.class);
        eventContent.setAdditionalProperty("response-data", responseData);

        return eventContent;
    }

    private String getEventDateTime() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return ZonedDateTime.now().format(formatter);
    }

}
