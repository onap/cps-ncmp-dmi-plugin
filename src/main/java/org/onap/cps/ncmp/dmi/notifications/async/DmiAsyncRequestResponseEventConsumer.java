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

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.json.JSONObject;
import org.json.JSONArray;
import org.onap.cps.ncmp.event.model.AvcEvent;
import org.onap.cps.ncmp.event.model.Event;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DmiAsyncRequestResponseEventConsumer {
    
    private final KafkaTemplate<String, AvcEvent> kafkaTemplate;    
	
    @KafkaListener(topics = "${app.ves.consumer.topic}" , groupId = "${spring.kafka.consumer.group-id}")
    public void consumeFromTopic( String msg) {
        
	    System.out.println("consumed ves message" + msg);

	    JSONObject obj_name = new JSONObject(msg);
            	    
	    System.out.println("Json Consumed message" + obj_name);
	    
	    JSONObject eventJsonObject = obj_name.getJSONObject("event");
	    JSONObject commonEventHeaderJsonObject = eventJsonObject.getJSONObject("commonEventHeader");
	    JSONObject stndDefinedFieldsJsonObject = eventJsonObject.getJSONObject("stndDefinedFields");
	    JSONObject datastndDefinedFieldsJO = stndDefinedFieldsJsonObject.getJSONObject("data");
	    JSONArray moichangesJA = datastndDefinedFieldsJO.getJSONArray("moiChanges");
	    JSONObject moichangesJAElement = moichangesJA.getJSONObject(0);
            JSONObject moichangesvalue = moichangesJAElement.getJSONObject("value");

	    final AvcEvent avcEvent = new AvcEvent();
            avcEvent.setEventId(commonEventHeaderJsonObject.getString("eventId"));
		avcEvent.setEventCorrelationId("cmHandleId-" + commonEventHeaderJsonObject.getString("sourceId"));
		avcEvent.setEventTime(datastndDefinedFieldsJO.getString("eventTime"));
		avcEvent.setEventSource("ncmp-datastore:passthrough-operational");
		avcEvent.setEventType(AvcEvent.class.getName());
		avcEvent.setEventSchema( AvcEvent.class.getName() + ".rfc8641");
		avcEvent.setEventSchemaVersion("1.0");
		//avcEvent.setEventTarget("NCMP");

		JSONObject pushchangeupdate = new JSONObject();
		JSONObject datastorechanges = new JSONObject();
		JSONObject ietfyangpatch = new JSONObject();
		JSONObject editvalue = new JSONObject();
		JSONArray editarray= new JSONArray();
		JSONObject editarrayObject = new JSONObject();

		editarrayObject.put("edit-id", moichangesJAElement.getInt("notificationId"));
		editarrayObject.put("operation", moichangesJAElement.getString("operation"));
		editarrayObject.put("target", "/_3gpp-common-managed-element:ManagedElement=Kista-001/_3gpp-nr-nrm-gnbdufunction:GNBDUFunction=1/_3gpp-nr-nrm-nrcelldu:NRCellDU=1");
		editarrayObject.put("value", moichangesvalue);
		editarray.put(editarrayObject);
		ietfyangpatch.put("edit", editarray);
		ietfyangpatch.put("patch-id", UUID.randomUUID().toString());
		datastorechanges.put("ietf-yang-patch:yang-patch", ietfyangpatch);
		pushchangeupdate.put("datastore-changes", datastorechanges);


		System.out.println("pushchangeupdateobject: " + pushchangeupdate.toString());
		//final Event event = new Event();
		//event.setAdditionalProperty("push-change-update", pushchangeupdate);
		editvalue.put("push-change-update", pushchangeupdate);
		avcEvent.setEvent(editvalue.toMap());
		System.out.println("pushchangeupdateobject: " + avcEvent.getEvent());
                
		sendMessage( "cmHandleId-" + commonEventHeaderJsonObject.getString("sourceId"), avcEvent);


	    }


               public void sendMessage(final String requestId, final AvcEvent avcEvent) {
        kafkaTemplate.send("dmi-cm-events", requestId, avcEvent);
        System.out.println("3gpp provisioning-AVC event sent");
    }

	}
