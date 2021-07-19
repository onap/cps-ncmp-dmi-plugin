package org.onap.cps.ncmp.dmi.service.client;

import org.jetbrains.annotations.NotNull;
import org.onap.cps.ncmp.dmi.config.DmiConfiguration.SdncProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeException;

@Component
public class SdncOperations {

    private static final String TOPOLOGY_URL_TEMPLATE = "/rests/data/network-topology:network-topology/topology={topologyId}";
    private static final String MOUNT_URL_TEMPLATE = "/node={nodeId}/yang-ext:mount";
    private static final String GET_SCHEMA_URL = "/ietf-netconf-monitoring:netconf-state/schemas";
    private static final String TOPOLOGY_ID_TEMPLATE = "{topologyId}";
    private static final String NODE_ID_TEMPLATE = "{nodeId}";

    private SdncProperties sdncProperties;
    private SdncRestconfClient sdncRestconfClient;
    private final String topologyUrl;
    private final String topologyMountUrlTemplate;
    private final int nodeTemplateIndex;

    public SdncOperations(final SdncProperties sdncProperties, final SdncRestconfClient sdncRestconfClient) {
        this.sdncProperties = sdncProperties;
        this.sdncRestconfClient = sdncRestconfClient;
        topologyUrl = TOPOLOGY_URL_TEMPLATE.replace(TOPOLOGY_ID_TEMPLATE, this.sdncProperties.getTopologyId());
        topologyMountUrlTemplate = topologyUrl+MOUNT_URL_TEMPLATE;
        nodeTemplateIndex = topologyMountUrlTemplate.indexOf(NODE_ID_TEMPLATE);
    }

    public ResponseEntity<String> getModulesFromNode(final String nodeId) {
        final StringBuilder builder = prepareGetSchemaUrl(nodeId);
        return sdncRestconfClient.getOp(builder.toString(), "application/json");
    }

    @NotNull
    private StringBuilder prepareGetSchemaUrl(String nodeId) {
        final StringBuilder builder = new StringBuilder(topologyMountUrlTemplate);
        builder.replace(nodeTemplateIndex, nodeTemplateIndex+ NODE_ID_TEMPLATE.length(), nodeId);
        builder.append(GET_SCHEMA_URL);
        return builder;
    }
}
