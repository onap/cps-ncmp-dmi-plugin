package org.onap.cps.ncmp.dmi.notifications.async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.event.model.AvcEvent;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class DmiAsyncConsumerVesToAvcEventCreator {

    private final ObjectMapper objectMapper = new ObjectMapper();


    public AvcEvent CreateEventFromVes(final String msg) throws JsonProcessingException {

        final JsonNode vesevent = objectMapper.readTree(msg);

        log.info("Json Consumed message" + vesevent);

        final JsonNode eventJsonNode = vesevent.path("event");
        final JsonNode commonEventHeaderJsonNode = eventJsonNode.path("commonEventHeader");
        final JsonNode stndDefinedFieldsJsonNode = eventJsonNode.path("stndDefinedFields");
        final JsonNode datastndDefinedFieldsJO = stndDefinedFieldsJsonNode.path("data");
        final JsonNode moichangesJA = datastndDefinedFieldsJO.path("moiChanges");
        JsonNode moichangesvalue = objectMapper.createObjectNode();
        String notificationId = "";
        String operation = "";
        if (moichangesJA.isArray()) {

            for (JsonNode node : moichangesJA) {
                moichangesvalue = node.path("value");
                notificationId = node.path("notificationId").asText();
                operation = node.path("operation").asText();
            }
        }
        final AvcEvent avcEvent = new AvcEvent();
        avcEvent.setEventId(commonEventHeaderJsonNode.path("eventId").asText());
        avcEvent.setEventCorrelationId("cmHandleId-" + commonEventHeaderJsonNode.path("sourceId").asText());
        avcEvent.setEventTime(datastndDefinedFieldsJO.path("eventTime").asText());
        avcEvent.setEventSource("ncmp-datastore:passthrough-operational");
        avcEvent.setEventType(AvcEvent.class.getName());
        avcEvent.setEventSchema(AvcEvent.class.getName() + ".rfc8641");
        avcEvent.setEventSchemaVersion("1.0");
        //avcEvent.setEventTarget("NCMP");

        final ObjectNode pushchangeupdate = objectMapper.createObjectNode();
        final ObjectNode datastorechanges = objectMapper.createObjectNode();
        final ObjectNode ietfyangpatch = objectMapper.createObjectNode();
        final ObjectNode editvalue = objectMapper.createObjectNode();
        final ArrayNode editarray = objectMapper.createArrayNode();
        final ObjectNode editarrayObject = objectMapper.createObjectNode();

        editarrayObject.put("edit-id", notificationId);
        editarrayObject.put("operation", operation);
        editarrayObject.put("target", "/_3gpp-common-managed-element:ManagedElement=Kista-001/_3gpp-nr-nrm-gnbdufunction:GNBDUFunction=1/_3gpp-nr-nrm-nrcelldu:NRCellDU=1");
        editarrayObject.set("value", moichangesvalue);
        editarray.add(editarrayObject);
        ietfyangpatch.put("edit", editarray);
        ietfyangpatch.put("patch-id", UUID.randomUUID().toString());
        datastorechanges.set("ietf-yang-patch:yang-patch", ietfyangpatch);
        pushchangeupdate.set("datastore-changes", datastorechanges);


        //System.out.println("pushchangeupdateobject: " + pushchangeupdate.toString());
        //final Event event = new Event();
        //event.setAdditionalProperty("push-change-update", pushchangeupdate);


        editvalue.set("push-change-update", pushchangeupdate);
        final Map<String, Object> result = objectMapper.convertValue(editvalue, new TypeReference<Map<String, Object>>() {
        });
        avcEvent.setEvent(result);
        log.info("pushchangeupdateobject: " + avcEvent.getEvent());

        return avcEvent;
    }
}
