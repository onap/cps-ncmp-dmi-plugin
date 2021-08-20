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

package org.onap.cps.ncmp.dmi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.onap.cps.ncmp.dmi.config.DmiPluginConfig.DmiPluginProperties;
import org.onap.cps.ncmp.dmi.exception.CmHandleRegistrationException;
import org.onap.cps.ncmp.dmi.exception.DmiException;
import org.onap.cps.ncmp.dmi.exception.ModuleResourceNotFoundException;
import org.onap.cps.ncmp.dmi.exception.ModulesNotFoundException;
import org.onap.cps.ncmp.dmi.exception.ResourceDataNotFound;
import org.onap.cps.ncmp.dmi.model.CmHandleOperation;
import org.onap.cps.ncmp.dmi.model.CreatedCmHandle;
import org.onap.cps.ncmp.dmi.model.ModuleReference;
import org.onap.cps.ncmp.dmi.model.ModuleSchemaProperties;
import org.onap.cps.ncmp.dmi.model.ModuleSchemas;
import org.onap.cps.ncmp.dmi.model.ModuleSet;
import org.onap.cps.ncmp.dmi.model.ModuleSetSchemas;
import org.onap.cps.ncmp.dmi.service.client.NcmpRestClient;
import org.onap.cps.ncmp.dmi.service.operation.SdncOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DmiServiceImpl implements DmiService {

    private SdncOperations sdncOperations;
    private NcmpRestClient ncmpRestClient;
    private ObjectMapper objectMapper;
    private DmiPluginProperties dmiPluginProperties;
    private static final String CONTENT_QUERY_PASSTHROUGH_OPERATIONAL = "content=all";
    private static final String CONTENT_QUERY_PASSTHROUGH_RUNNING = "content=config";

    /**
     * Constructor.
     *
     * @param dmiPluginProperties dmiPluginProperties
     * @param ncmpRestClient      ncmpRestClient
     * @param sdncOperations      sdncOperations
     * @param objectMapper        objectMapper
     */
    public DmiServiceImpl(final DmiPluginProperties dmiPluginProperties,
        final NcmpRestClient ncmpRestClient,
        final SdncOperations sdncOperations, final ObjectMapper objectMapper) {
        this.dmiPluginProperties = dmiPluginProperties;
        this.ncmpRestClient = ncmpRestClient;
        this.objectMapper = objectMapper;
        this.sdncOperations = sdncOperations;
    }

    @Override
    public ModuleSet getModulesForCmHandle(final String cmHandle) throws DmiException {
        final ResponseEntity<String> responseEntity = sdncOperations.getModulesFromNode(cmHandle);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            final String responseBody = responseEntity.getBody();
            if (StringUtils.isEmpty(responseBody)) {
                throw new ModulesNotFoundException(cmHandle, "SDNC returned no modules for given cm-handle.");
            }
            return createModuleSchema(responseBody);
        } else {
            throw new DmiException("SDNC is not able to process request.",
                "response code : " + responseEntity.getStatusCode() + " message : " + responseEntity.getBody());
        }
    }

    @Override
    public String getModuleResources(final String cmHandle, final List<ModuleReference> moduleReferences) {
        final JSONArray getModuleResponses = new JSONArray();
        for (final var moduleReference : moduleReferences) {
            final var moduleRequest = createModuleRequest(moduleReference);
            final var responseEntity = sdncOperations.getModuleResource(cmHandle, moduleRequest);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                getModuleResponses.add(responseEntity.getBody());
            } else {
                log.error("SDNC did not return a module resource for the given cmHandle {}", cmHandle);
                throw new ModuleResourceNotFoundException(cmHandle,
                    "SDNC did not return a module resource for the given cmHandle.");
            }
        }
        return getModuleResponses.toJSONString();
    }

    @Override
    public void registerCmHandles(final List<String> cmHandles) {
        final CmHandleOperation cmHandleOperation = new CmHandleOperation();
        cmHandleOperation.setDmiPlugin(dmiPluginProperties.getDmiServiceName());
        final List<CreatedCmHandle> createdCmHandleList = new ArrayList<>();
        for (final String cmHandle : cmHandles) {
            final CreatedCmHandle createdCmHandle = new CreatedCmHandle();
            createdCmHandle.setCmHandle(cmHandle);
            createdCmHandleList.add(createdCmHandle);
        }
        cmHandleOperation.setCreatedCmHandles(createdCmHandleList);
        final String cmHandlesJson;
        try {
            cmHandlesJson = objectMapper.writeValueAsString(cmHandleOperation);
        } catch (final JsonProcessingException e) {
            log.error("Parsing error occurred while converting cm-handles to JSON {}", cmHandles);
            throw new DmiException("Internal Server Error.",
                "Parsing error occurred while converting given cm-handles object list to JSON ");
        }
        final ResponseEntity<String> responseEntity = ncmpRestClient.registerCmHandlesWithNcmp(cmHandlesJson);
        if ((responseEntity.getStatusCode() != HttpStatus.CREATED)) {
            throw new CmHandleRegistrationException(responseEntity.getBody());
        }
    }

    private ModuleSet createModuleSchema(final String responseBody) {
        final var moduleSchemas = convertModulesToNodeSchema(responseBody);
        final List<ModuleSetSchemas> moduleSetSchemas = new ArrayList<>();
        for (final ModuleSchemaProperties schemaProperties : moduleSchemas.getSchemas().getSchema()) {
            moduleSetSchemas.add(convertModulesToModuleSchemas(schemaProperties));
        }
        final var moduleSet = new ModuleSet();
        moduleSet.setSchemas(moduleSetSchemas);
        return moduleSet;
    }

    private ModuleSetSchemas convertModulesToModuleSchemas(final ModuleSchemaProperties moduleSchemaProperties) {
        final var moduleSetSchemas = new ModuleSetSchemas();
        moduleSetSchemas.setModuleName(moduleSchemaProperties.getIdentifier());
        moduleSetSchemas.setNamespace(moduleSchemaProperties.getNamespace());
        moduleSetSchemas.setRevision(moduleSchemaProperties.getVersion());
        return moduleSetSchemas;
    }

    private ModuleSchemas convertModulesToNodeSchema(final String modulesListAsJson) {
        try {
            return objectMapper.readValue(modulesListAsJson, ModuleSchemas.class);
        } catch (final JsonProcessingException e) {
            log.error("JSON exception occurred when converting the following modules to node schema "
                + "{}", modulesListAsJson);
            throw new DmiException("Unable to process JSON.",
                "JSON exception occurred when mapping modules.", e);
        }
    }

    @Override
    public Object getResourceDataOperationalForCmHandle(final @NotNull String cmHandle,
        final @NotNull String resourceIdentifier,
        final String acceptParam,
        final String fieldsQuery,
        final Integer depthQuery,
        final Map<String, String> cmHandlePropertyMap) {
        // not using cmHandlePropertyMap of onap dmi impl , other dmi impl might use this.
        final ResponseEntity<String> responseEntity = sdncOperations.getResouceDataForOperationalAndRunning(cmHandle,
            resourceIdentifier,
            fieldsQuery,
            depthQuery,
            acceptParam,
                CONTENT_QUERY_PASSTHROUGH_OPERATIONAL);
        return prepareAndSendResponse(responseEntity, cmHandle);
    }

    @Override
    public Object getResourceDataPassThroughRunningForCmHandle(final @NotNull String cmHandle,
        final @NotNull String resourceIdentifier,
        final String acceptParam,
        final String fieldsQuery,
        final Integer depthQuery,
        final Map<String, String> cmHandlePropertyMap) {
        // not using cmHandlePropertyMap of onap dmi impl , other dmi impl might use this.
        final ResponseEntity<String> responseEntity = sdncOperations.getResouceDataForOperationalAndRunning(cmHandle,
            resourceIdentifier,
            fieldsQuery,
            depthQuery,
            acceptParam,
            CONTENT_QUERY_PASSTHROUGH_RUNNING);
        return prepareAndSendResponse(responseEntity, cmHandle);
    }

    @Override
    public String writeResourceDataPassthroughForCmHandle(final String cmHandle, final String resourceIdentifier,
        final String dataType, final Object data) {
        final String jsonData;
        try {
            jsonData = objectMapper.writeValueAsString(data);
        } catch (final JsonProcessingException e) {
            log.error("JSON exception occurred when processing pass through request data for the given cmHandle {}",
                cmHandle);
            throw new DmiException("Unable to process JSON.",
                "JSON exception occurred when writing data for the given cmHandle " + cmHandle, e);
        }
        final ResponseEntity<String> responseEntity =
            sdncOperations.writeResourceDataPassthroughRunnng(cmHandle, resourceIdentifier, dataType, jsonData);
        if (responseEntity.getStatusCode() == HttpStatus.CREATED) {
            return responseEntity.getBody();
        } else {
            throw new DmiException(cmHandle,
                "response code : " + responseEntity.getStatusCode() + " message : " + responseEntity.getBody());
        }
    }

    private String prepareAndSendResponse(final ResponseEntity<String> responseEntity, final String cmHandle) {
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        } else {
            throw new ResourceDataNotFound(cmHandle,
                "response code : " + responseEntity.getStatusCode() + " message : " + responseEntity.getBody());
        }
    }

    private String createModuleRequest(final ModuleReference moduleReference) {
        final var ietfNetconfModuleReferences = new LinkedHashMap<>();
        ietfNetconfModuleReferences.put("ietf-netconf-monitoring:identifier", moduleReference.getName());
        ietfNetconfModuleReferences.put("ietf-netconf-monitoring:version", moduleReference.getRevision());
        final var writer = objectMapper.writer().withRootName("ietf-netconf-monitoring:input");
        final String moduleRequest;
        try {
            moduleRequest = writer.writeValueAsString(ietfNetconfModuleReferences);
        } catch (final JsonProcessingException e) {
            log.error("JSON exception occurred when creating the module request for the given module reference {}",
                moduleReference.getName());
            throw new DmiException("Unable to process JSON.",
                "JSON exception occurred when creating the module request.", e);
        }
        return moduleRequest;
    }
}