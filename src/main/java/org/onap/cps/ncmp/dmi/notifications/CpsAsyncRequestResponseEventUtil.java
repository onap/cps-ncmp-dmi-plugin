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

package org.onap.cps.ncmp.dmi.notifications;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.onap.cps.event.model.CpsAsyncRequestResponseEvent;
import org.onap.cps.event.model.EventContent;
import org.onap.cps.event.model.ResponseData;
import org.onap.cps.ncmp.dmi.rest.events.NcmpEvents;

/**
 * Helper to create CpsAsyncRequestResponseEvents.
 */
public class CpsAsyncRequestResponseEventUtil {

    /**
     * Create an event.
     *
     * @param resourceDataAsJson the resource data as json
     * @param topicParamInQuery the topic to send response to
     * @param requestId the request id
     * @param status the status of the request
     * @param code the code of the response
     *
     * @return CpsAsyncRewustResponseEvent
     */
    public CpsAsyncRequestResponseEvent createEvent(final String resourceDataAsJson,
                                                    final String topicParamInQuery,
                                                    final String requestId,
                                                    final String status,
                                                    final String code) {
        final CpsAsyncRequestResponseEvent cpsAsyncRequestResponseEvent = new CpsAsyncRequestResponseEvent();

        cpsAsyncRequestResponseEvent.setEventId(requestId);
        cpsAsyncRequestResponseEvent.setEventCorrelationId(UUID.randomUUID().toString());

        // TODO: @Joe - will this remain static?
        cpsAsyncRequestResponseEvent.setEventSchema("urn:cps:org.onap.cps:async-request-response-event-schema:v1");
        cpsAsyncRequestResponseEvent.setEventSource("org.onap.ncmp");

        // cpsAsyncRequestResponseEvent.setEventTarget(
        // hasTopic(topicParamInQuery) ?  "topic:" + topicParamInQuery : null);

        cpsAsyncRequestResponseEvent.setEventTarget(topicParamInQuery);

        cpsAsyncRequestResponseEvent.setEventTime(getEventDateTime());
        cpsAsyncRequestResponseEvent.setEventType(NcmpEvents.ASYNC_REQUEST.toString());

        cpsAsyncRequestResponseEvent.setEvent(getEventContent(resourceDataAsJson, status, code));

        return cpsAsyncRequestResponseEvent;
    }

    private EventContent getEventContent(final String resourceDataAsJson, final String status, final String code) {
        final EventContent eventContent = new EventContent();

        // TODO: @Joe - we will need to add logic to determine what
        //  operation was performed as part of the async request and add it here
        eventContent.setResponseDataSchema("undetermined");

        eventContent.setResponseCode(status);
        eventContent.setResponseStatus(code);

        final ResponseData responseData = new ResponseData();

        responseData.setAdditionalProperty("data", resourceDataAsJson);
        eventContent.setResponseData(responseData);

        return eventContent;
    }

    private String getEventDateTime() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return ZonedDateTime.now().format(formatter);
    }

}
