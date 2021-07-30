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

import org.onap.cps.ncmp.dmi.config.DmiConfiguration.SdncProperties;
import org.onap.cps.ncmp.dmi.service.client.SdncRestconfClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class SdncOperations {


    private static final String TOPOLOGY_URL_TEMPLATE_DATA =
        "/rests/data/network-topology:network-topology/topology={topologyId}";
    private static final String TOPOLOGY_URL_TEMPLATE_OPERATIONAL =
        "/rests/operations/network-topology:network-topology/topology={topologyId}";
    private static final String MOUNT_URL_TEMPLATE = "/node={nodeId}/yang-ext:mount";
    private static final String GET_SCHEMA_URL = "/ietf-netconf-monitoring:netconf-state/schemas";
    private static final String GET_SCHEMA_SOURCES_URL = "/ietf-netconf-monitoring:get-schema";

    private SdncProperties sdncProperties;
    private SdncRestconfClient sdncRestconfClient;
    private final String topologyUrlData;
    private final String topologyUrlOperational;

    /**
     * Constructor for {@code SdncOperations}. This method also manipulates url properties.
     *
     * @param sdncProperties     {@code SdncProperties}
     * @param sdncRestconfClient {@code SdncRestconfClient}
     */
    public SdncOperations(final SdncProperties sdncProperties, final SdncRestconfClient sdncRestconfClient) {
        this.sdncProperties = sdncProperties;
        this.sdncRestconfClient = sdncRestconfClient;
        topologyUrlOperational =
            TOPOLOGY_URL_TEMPLATE_OPERATIONAL.replace("{topologyId}", this.sdncProperties.getTopologyId());
        topologyUrlData = TOPOLOGY_URL_TEMPLATE_DATA.replace("{topologyId}", this.sdncProperties.getTopologyId());
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

    /**
     * Get module schema.
     *
     * @param nodeId           node ID
     * @param moduleProperties module properties
     * @return response entity
     */
    public ResponseEntity<String> getModuleResource(final String nodeId, final String moduleProperties) {
        final String getYangResourceUrl = prepareGetOperationSchemaUrl(nodeId);
        return sdncRestconfClient.postOperationWithJsonData(getYangResourceUrl, moduleProperties);
    }

    private String prepareGetSchemaUrl(final String nodeId) {
        final var topologyMountUrl = topologyUrlData + MOUNT_URL_TEMPLATE;
        final String topologyMountUrlWithNodeId = topologyMountUrl.replace("{nodeId}", nodeId);
        final String resourceUrl = topologyMountUrlWithNodeId.concat(GET_SCHEMA_URL);
        return resourceUrl;
    }

    private String prepareGetOperationSchemaUrl(final String nodeId) {
        final var topologyMountUrl = topologyUrlOperational + MOUNT_URL_TEMPLATE;
        final var topologyMountUrlWithNodeId = topologyMountUrl.replace("{nodeId}", nodeId);
        return topologyMountUrlWithNodeId.concat(GET_SCHEMA_SOURCES_URL);
    }
}
