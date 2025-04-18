/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2025 OpenInfra Foundation Europe. All rights reserved.
 *  Modifications Copyright (C) 2021-2022 Bell Canada
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
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.config.DmiPluginConfig.DmiPluginProperties;
import org.onap.cps.ncmp.dmi.exception.CmHandleRegistrationException;
import org.onap.cps.ncmp.dmi.exception.DmiException;
import org.onap.cps.ncmp.dmi.exception.HttpClientRequestException;
import org.onap.cps.ncmp.dmi.exception.ModuleResourceNotFoundException;
import org.onap.cps.ncmp.dmi.exception.ModulesNotFoundException;
import org.onap.cps.ncmp.dmi.model.DataAccessRequest;
import org.onap.cps.ncmp.dmi.model.ModuleSet;
import org.onap.cps.ncmp.dmi.model.ModuleSetSchemasInner;
import org.onap.cps.ncmp.dmi.model.YangResource;
import org.onap.cps.ncmp.dmi.model.YangResources;
import org.onap.cps.ncmp.dmi.service.client.NcmpRestClient;
import org.onap.cps.ncmp.dmi.service.model.CmHandleOperation;
import org.onap.cps.ncmp.dmi.service.model.CreatedCmHandle;
import org.onap.cps.ncmp.dmi.service.model.ModuleReference;
import org.onap.cps.ncmp.dmi.service.model.ModuleSchema;
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
        final Collection<ModuleSchema> moduleSchemas = sdncOperations.getModuleSchemasFromNode(cmHandle);
        if (moduleSchemas.isEmpty()) {
            throw new ModulesNotFoundException(cmHandle, "SDNC returned no modules for given cm-handle.");
        } else {
            final ModuleSet moduleSet = new ModuleSet();
            moduleSchemas.forEach(moduleSchema ->
                moduleSet.addSchemasItem(toModuleSetSchemas(moduleSchema)));
            return moduleSet;
        }
    }

    @Override
    public YangResources getModuleResources(final String cmHandle, final List<ModuleReference> moduleReferences) {
        final YangResources yangResources = new YangResources();
        for (final ModuleReference moduleReference : moduleReferences) {
            final String moduleRequest = createModuleRequest(moduleReference);
            final ResponseEntity<String> responseEntity = sdncOperations.getModuleResource(cmHandle, moduleRequest);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                final YangResource yangResource = YangResourceExtractor.toYangResource(moduleReference, responseEntity);
                yangResources.add(yangResource);
            } else if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.error("SDNC did not return a module resource for the given cmHandle {}", cmHandle);
                throw new ModuleResourceNotFoundException(cmHandle,
                    "SDNC did not return a module resource for the given cmHandle.");
            } else {
                log.error("Error occurred when getting module resources from SDNC for the given cmHandle {}", cmHandle);
                throw new HttpClientRequestException(
                    cmHandle, responseEntity.getBody(), (HttpStatus) responseEntity.getStatusCode());
            }
        }
        return yangResources;
    }

    @Override
    public void registerCmHandles(final List<String> cmHandles) {
        final CmHandleOperation cmHandleOperation = new CmHandleOperation();
        cmHandleOperation.setDmiPlugin(dmiPluginProperties.getDmiServiceUrl());
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
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new CmHandleRegistrationException(responseEntity.getBody());
        }
    }

    @Override
    public void enableDataSyncForCmHandles(final List<String> cmHandles) {
        log.info("Enabling dataSync flag for : {}", cmHandles);
        cmHandles.forEach(cmHandleId -> ncmpRestClient.enabledDataSyncFlagWithNcmp(cmHandleId));
    }

    private ModuleSetSchemasInner toModuleSetSchemas(final ModuleSchema moduleSchema) {
        final ModuleSetSchemasInner moduleSetSchemas = new ModuleSetSchemasInner();
        moduleSetSchemas.setModuleName(moduleSchema.getIdentifier());
        moduleSetSchemas.setNamespace(moduleSchema.getNamespace());
        moduleSetSchemas.setRevision(moduleSchema.getVersion());
        return moduleSetSchemas;
    }

    @Override
    public String getResourceData(final String cmHandle,
        final String resourceIdentifier,
        final String optionsParamInQuery,
        final String restconfContentQueryParam) {
        final ResponseEntity<String> responseEntity = sdncOperations.getResouceDataForOperationalAndRunning(cmHandle,
            resourceIdentifier,
            optionsParamInQuery,
            restconfContentQueryParam);
        return prepareAndSendResponse(responseEntity, cmHandle);
    }

    @Override
    public String writeData(final DataAccessRequest.OperationEnum operation,
                            final String cmHandle,
                            final String resourceIdentifier,
                            final String dataType, final String data) {
        final ResponseEntity<String> responseEntity =
            sdncOperations.writeData(operation, cmHandle, resourceIdentifier, dataType, data);
        return prepareAndSendResponse(responseEntity, cmHandle);
    }

    private String prepareAndSendResponse(final ResponseEntity<String> responseEntity, final String cmHandle) {
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return responseEntity.getBody();
        } else {
            throw new HttpClientRequestException(cmHandle, responseEntity.getBody(),
                    (HttpStatus) responseEntity.getStatusCode());
        }
    }

    private String createModuleRequest(final ModuleReference moduleReference) {
        final Map<String, String> ietfNetconfModuleReferences = new LinkedHashMap<>();
        ietfNetconfModuleReferences.put("ietf-netconf-monitoring:identifier", moduleReference.getName());
        ietfNetconfModuleReferences.put("ietf-netconf-monitoring:version", moduleReference.getRevision());
        final ObjectWriter objectWriter = objectMapper.writer().withRootName("ietf-netconf-monitoring:input");
        final String moduleRequest;
        try {
            moduleRequest = objectWriter.writeValueAsString(ietfNetconfModuleReferences);
        } catch (final JsonProcessingException e) {
            log.error("JSON exception occurred when creating the module request for the given module reference {}",
                moduleReference.getName());
            throw new DmiException("Unable to process JSON.",
                "JSON exception occurred when creating the module request.", e);
        }
        return moduleRequest;
    }

}
