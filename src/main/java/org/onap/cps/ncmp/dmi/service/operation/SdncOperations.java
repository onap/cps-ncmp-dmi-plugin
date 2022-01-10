/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  Modifications Copyright (C) 2021 Bell Canada
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

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.onap.cps.ncmp.dmi.config.DmiConfiguration.SdncProperties;
import org.onap.cps.ncmp.dmi.exception.SdncException;
import org.onap.cps.ncmp.dmi.model.DataAccessRequest;
import org.onap.cps.ncmp.dmi.service.client.SdncRestconfClient;
import org.onap.cps.ncmp.dmi.service.model.ModuleSchema;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
    private static final String PATH_TO_MODULE_SCHEMAS = "$.ietf-netconf-monitoring:schemas.schema";

    private SdncProperties sdncProperties;
    private SdncRestconfClient sdncRestconfClient;
    private final String topologyUrlData;
    private final String topologyUrlOperational;

    private Configuration jsonPathConfiguration = Configuration.builder()
        .mappingProvider(new JacksonMappingProvider())
        .jsonProvider(new JacksonJsonProvider())
        .build();

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
     * @return a collection of module schemas
     */
    public Collection<ModuleSchema> getModuleSchemasFromNode(final String nodeId) {
        final String urlWithNodeId = prepareGetSchemaUrl(nodeId);
        final ResponseEntity<String> modulesResponseEntity = sdncRestconfClient.getOperation(urlWithNodeId);
        if (modulesResponseEntity.getStatusCode() == HttpStatus.OK) {
            final String modulesResponseBody = modulesResponseEntity.getBody();
            return (StringUtils.isEmpty(modulesResponseBody)) ? Collections.emptyList()
                : convertToModuleSchemas(modulesResponseBody);
        } else {
            throw new SdncException(
                String.format("SDNC failed to get Modules Schema for node %s", nodeId),
                modulesResponseEntity.getStatusCode(), modulesResponseEntity.getBody());
        }
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
        return sdncRestconfClient.httpOperationWithJsonData(
            HttpMethod.POST, getYangResourceUrl, moduleProperties, httpHeaders);
    }

    /**
     * This method fetches the resource data for given node identifier on given resource using sdnc client.
     *
     * @param nodeId                    network resource identifier
     * @param resourceId                resource identifier
     * @param optionsParamInQuery       fields query
     * @param acceptParamInHeader       accept parameter
     * @param restconfContentQueryParam restconf content query param
     * @return {@code ResponseEntity} response entity
     */
    public ResponseEntity<String> getResouceDataForOperationalAndRunning(final String nodeId,
        final String resourceId,
        final String optionsParamInQuery,
        final String acceptParamInHeader,
        final String restconfContentQueryParam) {
        final String getResourceDataUrl = prepareResourceDataUrl(nodeId,
            resourceId,
            buildQueryParamList(optionsParamInQuery, restconfContentQueryParam));
        final HttpHeaders httpHeaders = new HttpHeaders();
        if (!StringUtils.isEmpty(acceptParamInHeader)) {
            httpHeaders.set(HttpHeaders.ACCEPT, acceptParamInHeader);
        }
        return sdncRestconfClient.getOperation(getResourceDataUrl, httpHeaders);
    }

    /**
     * Write resource data.
     *
     * @param nodeId      network resource identifier
     * @param resourceId  resource identifier
     * @param contentType http content type
     * @param requestData request data
     * @return {@code ResponseEntity} response entity
     */
    public ResponseEntity<String> writeData(final DataAccessRequest.OperationEnum operation,
                                            final String nodeId,
                                            final String resourceId,
                                            final String contentType,
                                            final String requestData) {
        final var getResourceDataUrl = prepareWriteUrl(nodeId, resourceId);
        final var httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.parseMediaType(contentType));
        final HttpMethod httpMethod = getHttpMethod(operation);
        return sdncRestconfClient.httpOperationWithJsonData(httpMethod, getResourceDataUrl, requestData, httpHeaders);
    }

    private List<String> buildQueryParamList(final String optionsParamInQuery, final String restconfContentQueryParam) {
        final List<String> queryParamAsList = getOptionsParamAsList(optionsParamInQuery);
        queryParamAsList.add(restconfContentQueryParam);
        return queryParamAsList;
    }

    private List<String> getOptionsParamAsList(final String optionsParamInQuery) {
        final List<String> queryParamAsList = new LinkedList<>();
        if (!StringUtils.isEmpty(optionsParamInQuery)) {
            final String tempQuery = stripParenthesisFromOptionsQuery(optionsParamInQuery);
            queryParamAsList.addAll(Arrays.asList(tempQuery.split(",")));
        }
        return queryParamAsList;
    }

    private String stripParenthesisFromOptionsQuery(final String optionsParamInQuery) {
        return optionsParamInQuery.substring(1, optionsParamInQuery.length() - 1);
    }

    private String prepareGetSchemaUrl(final String nodeId) {
        return addResource(addTopologyDataUrlwithNode(nodeId), GET_SCHEMA_URL);
    }

    private String prepareWriteUrl(final String nodeId, final String resourceId) {
        return addResource(addTopologyDataUrlwithNode(nodeId), "/" + resourceId);
    }

    private String prepareGetOperationSchemaUrl(final String nodeId) {
        final var topologyMountUrl = topologyUrlOperational + MOUNT_URL_TEMPLATE;
        final var topologyMountUrlWithNodeId = topologyMountUrl.replace("{nodeId}", nodeId);
        return topologyMountUrlWithNodeId.concat(GET_SCHEMA_SOURCES_URL);
    }

    private String prepareResourceDataUrl(final String nodeId,
        final String resourceId,
        final List<String> queryList) {
        return addQuery(addResource(addTopologyDataUrlwithNode(nodeId), resourceId), queryList);
    }

    private String addResource(final String url, final String resourceId) {
        if (resourceId.startsWith("/")) {
            return url.concat(resourceId);
        } else {
            return url.concat("/" + resourceId);
        }
    }

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

    private String addTopologyDataUrlwithNode(final String nodeId) {
        final String topologyMountUrl = topologyUrlData + MOUNT_URL_TEMPLATE;
        return topologyMountUrl.replace("{nodeId}", nodeId);
    }

    private List<ModuleSchema> convertToModuleSchemas(final String modulesListAsJson) {
        try {
            return JsonPath.using(jsonPathConfiguration).parse(modulesListAsJson).read(
                PATH_TO_MODULE_SCHEMAS, new TypeRef<>() {
                });
        } catch (final JsonPathException jsonPathException) {
            throw new SdncException("SDNC Response processing failed",
                "SDNC response is not in the expected format.", jsonPathException);
        }
    }

    private HttpMethod getHttpMethod(final DataAccessRequest.OperationEnum operation) {
        HttpMethod httpMethod = null;
        switch (operation) {
            case READ:
                httpMethod = HttpMethod.GET;
                break;
            case CREATE:
                httpMethod = HttpMethod.POST;
                break;
            case PATCH:
                httpMethod = HttpMethod.PATCH;
                break;
            case UPDATE:
                httpMethod = HttpMethod.PUT;
                break;
            case DELETE:
                httpMethod = HttpMethod.DELETE;
                break;
            default:
                //unreachable code but checkstyle made me do this!
        }
        return httpMethod;
    }

}
