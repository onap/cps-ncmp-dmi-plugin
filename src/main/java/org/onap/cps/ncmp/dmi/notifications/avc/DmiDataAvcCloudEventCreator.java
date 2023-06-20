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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.events.avc1_0_0.AvcEvent;
import org.onap.cps.ncmp.events.avc1_0_0.Data;
import org.onap.cps.ncmp.events.avc1_0_0.DatastoreChanges;
import org.onap.cps.ncmp.events.avc1_0_0.Edit;
import org.onap.cps.ncmp.events.avc1_0_0.IetfYangPatchYangPatch;
import org.onap.cps.ncmp.events.avc1_0_0.PushChangeUpdate;
import org.onap.cps.ncmp.events.avc1_0_0.Value;

/**
 * Helper to create AvcEvents.
 */
@Slf4j
public class DmiDataAvcCloudEventCreator {

    private static final DateTimeFormatter dateTimeFormatter
            = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates CloudEvent for DMI Data AVC.
     *
     * @param eventCorrelationId correlationid
     * @return Cloud Event
     */
    public CloudEvent createCloudEvent(final String eventCorrelationId) {

        CloudEvent cloudEvent = null;

        try {
            cloudEvent = CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create("NCMP"))
                    .withType(AvcEvent.class.getName())
                    .withDataSchema(URI.create("urn:cps:" + AvcEvent.class.getName() + ":1.0.0"))
                    .withExtension("correlationid", eventCorrelationId)
                    .withData(objectMapper.writeValueAsBytes(createDmiDataAvcEvent())).build();
        } catch (final JsonProcessingException jsonProcessingException) {
            log.error("Unable to convert object to json : {}", jsonProcessingException.getMessage());
        }

        return cloudEvent;
    }

    private AvcEvent createDmiDataAvcEvent() {
        final AvcEvent avcEvent = new AvcEvent();
        final Data data = new Data();
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
        data.setPushChangeUpdate(pushChangeUpdate);

        avcEvent.setData(data);
        return avcEvent;
    }

}