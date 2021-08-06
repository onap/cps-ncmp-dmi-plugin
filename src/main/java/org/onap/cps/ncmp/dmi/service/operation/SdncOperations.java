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

import java.util.List;

@Component
public class SdncOperations {

    private static final String TOPOLOGY_URL_TEMPLATE = "/rests/data/network-topology:network-topology"
            + "/topology={topologyId}";
    private static final String MOUNT_URL_TEMPLATE = "/node={nodeId}/yang-ext:mount";
    private static final String GET_SCHEMA_URL = "/ietf-netconf-monitoring:netconf-state/schemas";

    private SdncProperties sdncProperties;
    private SdncRestconfClient sdncRestconfClient;
    private final String topologyUrl;
    private final String topologyMountUrlTemplate;

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
        topologyUrl = TOPOLOGY_URL_TEMPLATE.replace("{topologyId}", this.sdncProperties.getTopologyId());
        topologyMountUrlTemplate = topologyUrl + MOUNT_URL_TEMPLATE;
    }

    /**
     * This method fetches list of modules usind sdnc client.
     *
     * @param nodeId node id for node
     * @return returns {@code ResponseEntity} which contains list of modules
     */
    public ResponseEntity<String> getModulesFromNode(@NotNull final String nodeId) {
        final String urlWithNodeId = prepareGetSchemaUrl(nodeId);
        return sdncRestconfClient.getOperation(urlWithNodeId);
    }

    public ResponseEntity<String> getResouceDataFromNode(final String nodeId, final String resourceId, final List<String> queryList) {
        final String getResourceDataUrl = prepareResourceDataUrl(nodeId, resourceId, queryList);
        return sdncRestconfClient.getOperation(getResourceDataUrl);
    }

    @NotNull
    private String prepareGetSchemaUrl(final String nodeId) {
        final var getSchemaUrl = addResource(addTopologywithNodeUrl(nodeId), GET_SCHEMA_URL);
        return getSchemaUrl;
    }

    @NotNull
    private String prepareResourceDataUrl(final String nodeId,
                                             final String resourceId,
                                             final List<String> queryList) {
        final var resourceDataUrl = addQuery(addResource(addTopologywithNodeUrl(nodeId), resourceId), queryList);
        return resourceDataUrl;
    }

    @NotNull
    private String addTopologywithNodeUrl(final String nodeId) {
        final String topologyMountUrl = topologyMountUrlTemplate;
        return topologyMountUrl.replace("{nodeId}", nodeId);
    }

    @NotNull
    private String addResource(final String url, final String resourceId) {
        if(resourceId.startsWith("/")) {
            return url.concat(resourceId);
        } else {
            return url.concat("/"+resourceId);
        }
    }

    @NotNull
    private String addQuery(final String url, final List<String> queryList) {
        if(queryList.isEmpty()) {
            return url;
        }
        final StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        urlBuilder.append(queryList.get(0));
        for(int i = 1;i<queryList.size();i++) {
            urlBuilder.append("&");
            urlBuilder.append(queryList.get(i));
        }
        return urlBuilder.toString();
    }
}
