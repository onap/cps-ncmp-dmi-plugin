/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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

package org.onap.cps.ncmp.dmi.service.operation;

import org.jetbrains.annotations.NotNull;
import org.onap.cps.ncmp.dmi.config.DmiConfiguration.SdncProperties;
import org.onap.cps.ncmp.dmi.service.client.SdncRestconfClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class SdncOperations {

    private static final String TOPOLOGY_URL_TEMPLATE = "/rests/data/network-topology:network-topology"
            + "/topology={topologyId}";
    private static final String MOUNT_URL_TEMPLATE = "/node={nodeId}/yang-ext:mount";
    private static final String GET_SCHEMA_URL = "/ietf-netconf-monitoring:netconf-state/schemas";
    private static final String TOPOLOGY_ID_TEMPLATE = "{topologyId}";
    private static final String NODE_ID_TEMPLATE = "{nodeId}";

    private SdncProperties sdncProperties;
    private SdncRestconfClient sdncRestconfClient;
    private final String topologyUrl;
    private final String topologyMountUrlTemplate;
    private final int nodeTemplateIndex;

    /**
     * Constructor for {@code SdncOperations}. This method also manipulates
     * url properties.
     *
     * @param sdncProperties {@code SdncProperties}
     * @param sdncRestconfClient {@code SdncRestconfClient}
     */

    public SdncOperations(final SdncProperties sdncProperties, final SdncRestconfClient sdncRestconfClient) {
        this.sdncProperties = sdncProperties;
        this.sdncRestconfClient = sdncRestconfClient;
        topologyUrl = TOPOLOGY_URL_TEMPLATE.replace(TOPOLOGY_ID_TEMPLATE, this.sdncProperties.getTopologyId());
        topologyMountUrlTemplate = topologyUrl + MOUNT_URL_TEMPLATE;
        nodeTemplateIndex = topologyMountUrlTemplate.indexOf(NODE_ID_TEMPLATE);
    }

    /**
     * This method fetches list of modules usind sdnc client.
     *
     * @param nodeId node id for node
     * @return returns {@code ResponseEntity} which contains list of modules
     */
    public ResponseEntity<String> getModulesFromNode(final String nodeId) {
        final StringBuilder builder = prepareGetSchemaUrl(nodeId);
        return sdncRestconfClient.getOp(builder.toString(), "application/json");
    }

    @NotNull
    private StringBuilder prepareGetSchemaUrl(final String nodeId) {
        final StringBuilder builder = new StringBuilder(topologyMountUrlTemplate);
        builder.replace(nodeTemplateIndex, nodeTemplateIndex + NODE_ID_TEMPLATE.length(), nodeId);
        builder.append(GET_SCHEMA_URL);
        return builder;
    }
}
