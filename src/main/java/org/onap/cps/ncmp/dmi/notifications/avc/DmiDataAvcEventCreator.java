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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.events.avc.v1.AvcEvent;
import org.onap.cps.ncmp.events.avc.v1.AvcEventHeader;
import org.onap.cps.ncmp.events.avc.v1.DatastoreChanges;
import org.onap.cps.ncmp.events.avc.v1.Edit;
import org.onap.cps.ncmp.events.avc.v1.Event;
import org.onap.cps.ncmp.events.avc.v1.IetfYangPatchYangPatch;
import org.onap.cps.ncmp.events.avc.v1.PushChangeUpdate;
import org.onap.cps.ncmp.events.avc.v1.Value;

/**
 * Helper to create AvcEvents.
 */
@Slf4j
public class DmiDataAvcEventCreator {

    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    /**
     * Create an AVC event.
     *
     * @return DmiAsyncRequestResponseEvent
     */
    public AvcEvent createEvent() {
        final AvcEvent avcEvent = new AvcEvent();
        final Event event = new Event();
        final PushChangeUpdate pushChangeUpdate = new PushChangeUpdate();
        final DatastoreChanges datastoreChanges = new DatastoreChanges();
        final IetfYangPatchYangPatch ietfYangPatchYangPatch = new IetfYangPatchYangPatch();
        ietfYangPatchYangPatch.setPatchId("abcd");
        final Edit edit1 = new Edit();
        final Value value = new Value();
        final Map<String, Object> attributeMap = new LinkedHashMap<>();
        attributeMap.put("isHoAllowed", false);
        value.setAttributes(List.of(attributeMap));
        edit1.setEditId("editId");
        edit1.setOperation("replace");
        edit1.setTarget("target_xpath");
        edit1.setValue(value);
        ietfYangPatchYangPatch.setEdit(List.of(edit1));
        datastoreChanges.setIetfYangPatchYangPatch(ietfYangPatchYangPatch);
        pushChangeUpdate.setDatastoreChanges(datastoreChanges);
        event.setPushChangeUpdate(pushChangeUpdate);

        avcEvent.setEvent(event);
        return avcEvent;
    }

    /**
     * Create an AvcEventHeader.
     *
     * @param eventCorrelationId the event correlation id
     * @return DmiAsyncRequestResponseEventHeader
     */
    public AvcEventHeader createAvcEventHeader(final String eventCorrelationId) {
        final AvcEventHeader avcEventHeader = new AvcEventHeader();

        avcEventHeader.setEventId(UUID.randomUUID().toString());
        avcEventHeader.setEventCorrelationId(eventCorrelationId);
        avcEventHeader.setEventType(AvcEvent.class.getName());
        avcEventHeader.setEventSchema("urn:cps:" + AvcEvent.class.getName());
        avcEventHeader.setEventSchemaVersion("v1");
        avcEventHeader.setEventSource("NCMP");
        avcEventHeader.setEventTime(ZonedDateTime.now().format(dateTimeFormatter));

        return avcEventHeader;
    }

}