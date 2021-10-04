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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.onap.cps.ncmp.dmi.config.DmiConfiguration.SdncProperties;
import org.onap.cps.ncmp.dmi.service.client.SdncRestconfClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
        final var getYangResourceUrl = prepareGetOperationSchemaUrl(nodeId);
        final var httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return sdncRestconfClient
            .postOperationWithJsonData(getYangResourceUrl, moduleProperties, httpHeaders);
    }

    /**
     * This method fetches the resource data for given node identifier on given resource using sdnc client.
     *
     * @param nodeId      network resource identifier
     * @param resourceId  resource identifier
     * @param optionsQuery fields query
     * @param acceptParam accept parameter
     * @return {@code ResponseEntity} response entity
     */
    public ResponseEntity<String> getResouceDataForOperationalAndRunning(final String nodeId,
        final String resourceId,
        final String optionsQuery,
        final String acceptParam,
        final String contentQuery) {
        final String getResourceDataUrl = prepareResourceDataUrl(nodeId,
            resourceId,
            getQueryList(optionsQuery, contentQuery));
        final HttpHeaders httpHeaders = new HttpHeaders();
        if (!StringUtils.isEmpty(acceptParam)) {
            httpHeaders.set(HttpHeaders.ACCEPT, acceptParam);
        }
        return sdncRestconfClient.getOperation(getResourceDataUrl, httpHeaders);
    }

    /**
     * Write resource data using passthrough running.
     *
     * @param nodeId      network resource identifier
     * @param resourceId  resource identifier
     * @param contentType http content type
     * @param requestData request data
     * @return {@code ResponseEntity} response entity
     */
    public ResponseEntity<String> writeResourceDataPassthroughRunning(final String nodeId,
        final String resourceId, final String contentType, final String requestData) {
        final var getResourceDataUrl = preparePassthroughRunningUrl(nodeId, resourceId);
        final var httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType(contentType));
        return sdncRestconfClient.postOperationWithJsonData(getResourceDataUrl, requestData, httpHeaders);
    }

    @NotNull
    private List<String> getQueryList(final String optionsQuery, final String contentQuery) {
        final List<String> queryList = new LinkedList<>();
        if (!StringUtils.isEmpty(optionsQuery)) {
            final String tempQuery = optionsQuery.substring(1, optionsQuery.length() - 1);
            queryList.addAll(Arrays.asList(tempQuery.split(",")));
        }
        if (!StringUtils.isEmpty(contentQuery)) {
            queryList.add(contentQuery);
        }
        return queryList;
    }


    @NotNull
    private String prepareGetSchemaUrl(final String nodeId) {
        return addResource(addTopologyDataUrlwithNode(nodeId), GET_SCHEMA_URL);
    }

    private String preparePassthroughRunningUrl(final String nodeId, final String resourceId) {
        return addResource(addTopologyDataUrlwithNode(nodeId), "/" + resourceId);
    }

    private String prepareGetOperationSchemaUrl(final String nodeId) {
        final var topologyMountUrl = topologyUrlOperational + MOUNT_URL_TEMPLATE;
        final var topologyMountUrlWithNodeId = topologyMountUrl.replace("{nodeId}", nodeId);
        return topologyMountUrlWithNodeId.concat(GET_SCHEMA_SOURCES_URL);
    }

    @NotNull
    private String prepareResourceDataUrl(final String nodeId,
        final String resourceId,
        final List<String> queryList) {
        return addQuery(addResource(addTopologyDataUrlwithNode(nodeId), resourceId), queryList);
    }

    @NotNull
    private String addResource(final String url, final String resourceId) {
        if (resourceId.startsWith("/")) {
            return url.concat(resourceId);
        } else {
            return url.concat("/" + resourceId);
        }
    }

    @NotNull
    private String addQuery(final String url, final List<String> queryList) {
        if (queryList.isEmpty()) {
            return url;
        }
        final StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        urlBuilder.append(queryList.get(0));
        for (int i = 1; i < queryList.size(); i++) {
            urlBuilder.append("&");
            urlBuilder.append(queryList.get(i));
        }
        return urlBuilder.toString();
    }

    @NotNull
    private String addTopologyDataUrlwithNode(final String nodeId) {
        final String topologyMountUrl = topologyUrlData + MOUNT_URL_TEMPLATE;
        return topologyMountUrl.replace("{nodeId}", nodeId);
    }
}