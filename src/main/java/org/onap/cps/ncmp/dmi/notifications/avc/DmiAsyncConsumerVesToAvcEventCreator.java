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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.service.model.avc.RootStandardDefinedFieldsEvent;
import org.onap.cps.ncmp.dmi.service.model.avc.StandardDefinedFieldsData;
import org.onap.cps.ncmp.event.model.AvcEvent;




@Slf4j
public class DmiAsyncConsumerVesToAvcEventCreator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create event from ves.
     */
    public AvcEvent createEventFromVes(final String msg) throws JsonProcessingException {

        final RootStandardDefinedFieldsEvent vesevent;
        vesevent = objectMapper.readValue(msg, RootStandardDefinedFieldsEvent.class);

        log.info("Json Consumed message: {},", msg);

        final AvcEvent avcEvent = new AvcEvent();
        avcEvent.setEventId(vesevent.getEvent().getCommonEventHeader().getEventId());
        avcEvent.setEventCorrelationId("cmHandleId-" + vesevent.getEvent().getCommonEventHeader().getSourceId());
        avcEvent.setEventTime(vesevent.getEvent().getStndDefinedFields().getData().getEventTime());
        avcEvent.setEventSource("ncmp-datastore:passthrough-operational");
        avcEvent.setEventType(AvcEvent.class.getName());
        avcEvent.setEventSchema(AvcEvent.class.getName() + ".rfc8641");
        avcEvent.setEventSchemaVersion("1.0");

        final ObjectNode pushchangeupdate = objectMapper.createObjectNode();
        final ObjectNode datastorechanges = objectMapper.createObjectNode();
        final ObjectNode ietfyangpatch = objectMapper.createObjectNode();
        final ObjectNode editvalue = objectMapper.createObjectNode();
        final ArrayNode editarray = objectMapper.createArrayNode();
        final ObjectNode editarrayObject = objectMapper.createObjectNode();

        final StandardDefinedFieldsData data = vesevent.getEvent().getStndDefinedFields().getData();
        editarrayObject.put("edit-id", data.getNotificationId());
        editarrayObject.put("operation", data.getMoiChanges().get(0).getOperation());
        editarrayObject.put("target", "/_3gpp-common-managed-element:"
               + "ManagedElement=Kista-001/_3gpp-nr-nrm-gnbdufunction:"
               + "GNBDUFunction=1/_3gpp-nr-nrm-nrcelldu:NRCellDU=1");
        editarrayObject.put("value", data.getMoiChanges().get(0).getValue());
        editarray.add(editarrayObject);
        ietfyangpatch.put("edit", editarray);
        ietfyangpatch.put("patch-id", UUID.randomUUID().toString());
        datastorechanges.set("ietf-yang-patch:yang-patch", ietfyangpatch);
        pushchangeupdate.set("datastore-changes", datastorechanges);

        editvalue.set("push-change-update", pushchangeupdate);
        final Map<String, Object> result =
                objectMapper.convertValue(editvalue, new TypeReference<Map<String, Object>>() {}); 
        avcEvent.setEvent(result);
        log.info("pushchangeupdateobject: {},", avcEvent.getEvent());

        return avcEvent;
    }
}
