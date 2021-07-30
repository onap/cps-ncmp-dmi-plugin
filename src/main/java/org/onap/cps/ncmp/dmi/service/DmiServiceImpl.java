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
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.onap.cps.ncmp.dmi.config.DmiPluginConfig.DmiPluginProperties;
import org.onap.cps.ncmp.dmi.exception.CmHandleRegistrationException;
import org.onap.cps.ncmp.dmi.exception.DmiException;
import org.onap.cps.ncmp.dmi.exception.ModuleResourceNotFoundException;
import org.onap.cps.ncmp.dmi.exception.ModulesNotFoundException;
import org.onap.cps.ncmp.dmi.model.CmHandleOperation;
import org.onap.cps.ncmp.dmi.model.CreatedCmHandle;
import org.onap.cps.ncmp.dmi.model.ModuleReference;
import org.onap.cps.ncmp.dmi.service.client.NcmpRestClient;
import org.onap.cps.ncmp.dmi.service.operation.SdncOperations;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    public DmiServiceImpl(final DmiPluginProperties dmiPluginProperties,
        final NcmpRestClient ncmpRestClient,
        final SdncOperations sdncOperations, final ObjectMapper objectMapper) {
        this.dmiPluginProperties = dmiPluginProperties;
        this.ncmpRestClient = ncmpRestClient;
        this.objectMapper = objectMapper;
        this.sdncOperations = sdncOperations;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getModulesForCmHandle(final String cmHandle) throws DmiException {
        final ResponseEntity<String> responseEntity = sdncOperations.getModulesFromNode(cmHandle);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            final String responseBody = responseEntity.getBody();
            if (StringUtils.isEmpty(responseBody)) {
                throw new ModulesNotFoundException(cmHandle, "SDNC returned no modules for given cm-handle.");
            }
            return responseBody;
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
            final var responseEntity = sdncOperations.getModuleRescource(cmHandle, moduleRequest);
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
        if (!(responseEntity.getStatusCode() == HttpStatus.CREATED)) {
            throw new CmHandleRegistrationException(responseEntity.getBody());
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
