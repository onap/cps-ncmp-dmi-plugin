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
    private static final String TOPOLOGY_URL_OPERATION_TEMPLATE =
        "/restconf/operations/network-topology:network-topology"
            + "/topology/{topologyId}";
    private static final String MOUNT_URL_TEMPLATE = "/node={nodeId}/yang-ext:mount";
    private static final String MOUNT_URL_GET_OPERATION_TEMPLATE = "/node/{nodeId}/yang-ext:mount";
    private static final String GET_SCHEMA_URL = "/ietf-netconf-monitoring:netconf-state/schemas";
    private static final String GET_SCHEMA_SOURCES_URL = "/ietf-netconf-monitoring:get-schema";

    private SdncProperties sdncProperties;
    private SdncRestconfClient sdncRestconfClient;

    /**
     * Constructor for {@code SdncOperations}. This method also manipulates url properties.
     *
     * @param sdncProperties     {@code SdncProperties}
     * @param sdncRestconfClient {@code SdncRestconfClient}
     */

    public SdncOperations(final SdncProperties sdncProperties, final SdncRestconfClient sdncRestconfClient) {
        this.sdncProperties = sdncProperties;
        this.sdncRestconfClient = sdncRestconfClient;
    }

    /**
     * This method fetches list of modules usind sdnc client.
     *
     * @param nodeId node id for node
     * @return returns {@code ResponseEntity} which contains list of modules
     */
    public ResponseEntity<String> getModulesFromNode(final String nodeId) {
        final String urlWithNodeId = prepareGetSchemaUrl(nodeId);
        return sdncRestconfClient.getOperation(urlWithNodeId);
    }

    @NotNull
    private String prepareGetSchemaUrl(final String nodeId) {
        final String topologyUrl = TOPOLOGY_URL_TEMPLATE.replace("{topologyId}", this.sdncProperties.getTopologyId());
        final String topologyMountUrlTemplate = topologyUrl + MOUNT_URL_TEMPLATE;
        final String topologyMountUrl = topologyMountUrlTemplate;
        final String topologyMountUrlWithNodeId = topologyMountUrl.replace("{nodeId}", nodeId);
        final String resourceUrl = topologyMountUrlWithNodeId.concat(GET_SCHEMA_URL);
        return resourceUrl;
    }

    /**
     * Get yang resources.
     *
     * @param nodeId           node ID
     * @param moduleProperties module properties
     * @return response entity
     */
    public ResponseEntity<String> getYangResources(final String nodeId, final String moduleProperties) {
        final var topologyUrl =
            TOPOLOGY_URL_OPERATION_TEMPLATE.replace("{topologyId}", this.sdncProperties.getTopologyId());
        final var topologyMountUrlTemplate = topologyUrl + MOUNT_URL_GET_OPERATION_TEMPLATE;
        final var topologyMountUrl = topologyMountUrlTemplate;
        final var topologyMountUrlWithNodeId = topologyMountUrl.replace("{nodeId}", nodeId);
        final var resourceUrl = topologyMountUrlWithNodeId.concat(GET_SCHEMA_SOURCES_URL);
        return sdncRestconfClient.getModuleSources(resourceUrl, moduleProperties);
    }
}
