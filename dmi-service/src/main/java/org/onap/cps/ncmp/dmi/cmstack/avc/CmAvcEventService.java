/*
 * ============LICENSE_START========================================================
 *  Copyright (c) 2025 OpenInfra Foundation Europe. All rights reserved.
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

package org.onap.cps.ncmp.dmi.cmstack.avc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.model.DataAccessRequest;
import org.onap.cps.ncmp.events.avc1_0_0.AvcEvent;
import org.onap.cps.ncmp.events.avc1_0_0.Data;
import org.onap.cps.ncmp.events.avc1_0_0.DatastoreChanges;
import org.onap.cps.ncmp.events.avc1_0_0.Edit;
import org.onap.cps.ncmp.events.avc1_0_0.IetfYangPatchYangPatch;
import org.onap.cps.ncmp.events.avc1_0_0.PushChangeUpdate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmAvcEventService {

    @Value("${app.dmi.avc.cm-avc-events-topic:dmi-cm-events}")
    private String dmiCmAvcEventsTopic;

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, CloudEvent> cloudEventKafkaTemplate;

    /**
     * Handle the event creation and sends event to NCMP.
     *
     * @param operation          Operation
     * @param cmHandle           cm handle identifier
     * @param resourceIdentifier resource identifier
     * @param data               actual data in the form of JSON
     */
    public void sendCmAvcEvent(final DataAccessRequest.OperationEnum operation, final String cmHandle,
            final String resourceIdentifier, final String data) {

        final CloudEvent cmAvcEventAsCloudEvent =
                convertAvcEventToCloudEvent(cmAvcEventsCreator(operation, resourceIdentifier, data));

        if (cmAvcEventAsCloudEvent == null) {
            log.warn("cmAvcEventAsCloudEvent is null, skipping send for cmHandle: {}", cmHandle);
        } else {
            cloudEventKafkaTemplate.send(dmiCmAvcEventsTopic, cmHandle, cmAvcEventAsCloudEvent);
        }

    }

    private AvcEvent cmAvcEventsCreator(final DataAccessRequest.OperationEnum operation,
            final String resourceIdentifier, final String jsonData) {
        final AvcEvent avcEvent = new AvcEvent();
        final Data data = new Data();
        final PushChangeUpdate pushChangeUpdate = new PushChangeUpdate();
        final DatastoreChanges datastoreChanges = new DatastoreChanges();
        final IetfYangPatchYangPatch ietfYangPatchYangPatch = new IetfYangPatchYangPatch();
        ietfYangPatchYangPatch.setPatchId(UUID.randomUUID().toString());
        final Edit edit = new Edit();
        edit.setEditId(UUID.randomUUID() + "-edit-id");
        edit.setOperation(operation.getValue());
        edit.setTarget(resourceIdentifier);
        edit.setValue(jsonData);
        ietfYangPatchYangPatch.setEdit(List.of(edit));
        datastoreChanges.setIetfYangPatchYangPatch(ietfYangPatchYangPatch);
        pushChangeUpdate.setDatastoreChanges(datastoreChanges);
        data.setPushChangeUpdate(pushChangeUpdate);
        avcEvent.setData(data);
        return avcEvent;
    }

    private CloudEvent convertAvcEventToCloudEvent(final AvcEvent avcEvent) {

        try {
            return CloudEventBuilder.v1()
                    .withId(UUID.randomUUID().toString())
                    .withSource(URI.create("ONAP-DMI-PLUGIN"))
                    .withType(AvcEvent.class.getName())
                    .withDataSchema(URI.create("urn:cps:" + AvcEvent.class.getName() + ":1.0.0"))
                    .withData(objectMapper.writeValueAsBytes(avcEvent))
                    .build();
        } catch (final JsonProcessingException jsonProcessingException) {
            log.error("Unable to convert object to json : {}", jsonProcessingException.getMessage());
        }

        return null;

    }

}
