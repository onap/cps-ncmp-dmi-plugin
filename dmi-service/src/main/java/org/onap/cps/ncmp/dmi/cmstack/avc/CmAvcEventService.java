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

    @Value("${app.dmi.avc.cm-events}")
    private String dmiCmEventsTopic;

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, CloudEvent> cloudEventKafkaTemplate;

    /**
     * Handle the event creation and responding back to NCMP to keep the cache in sync.
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

        if (cmAvcEventAsCloudEvent != null) {
            cloudEventKafkaTemplate.send(dmiCmEventsTopic, cmHandle, cmAvcEventAsCloudEvent);
        } else {
            log.warn("cmAvcEventAsCloudEvent is null, skipping send for cmHandle: {}", cmHandle);
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
        final Edit edit1 = new Edit();
        edit1.setEditId(UUID.randomUUID() + ":" + "-edit-id");
        edit1.setOperation(operation.getValue());
        edit1.setTarget(resourceIdentifier);
        edit1.setValue(jsonData);
        ietfYangPatchYangPatch.setEdit(List.of(edit1));
        datastoreChanges.setIetfYangPatchYangPatch(ietfYangPatchYangPatch);
        pushChangeUpdate.setDatastoreChanges(datastoreChanges);
        data.setPushChangeUpdate(pushChangeUpdate);
        avcEvent.setData(data);
        return avcEvent;
    }

    private CloudEvent convertAvcEventToCloudEvent(final AvcEvent avcEvent) {

        CloudEvent cloudEvent = null;

        try {
            cloudEvent = CloudEventBuilder.v1().withId(UUID.randomUUID().toString())
                                 .withSource(URI.create("ONAP-DMI-PLUGIN")).withType(AvcEvent.class.getName())
                                 .withDataSchema(URI.create("urn:cps:" + AvcEvent.class.getName() + ":1.0.0"))
                                 .withData(objectMapper.writeValueAsBytes(avcEvent)).build();
        } catch (final JsonProcessingException jsonProcessingException) {
            log.error("Unable to convert object to json : {}", jsonProcessingException.getMessage());
        }

        return cloudEvent;

    }

}
