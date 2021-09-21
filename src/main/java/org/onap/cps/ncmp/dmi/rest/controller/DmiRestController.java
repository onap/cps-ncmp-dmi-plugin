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

package org.onap.cps.ncmp.dmi.rest.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.model.CmHandles;
import org.onap.cps.ncmp.dmi.model.DataAccessReadRequest;
import org.onap.cps.ncmp.dmi.model.DataAccessWriteRequest;
import org.onap.cps.ncmp.dmi.model.DmiReadRequestBody;
import org.onap.cps.ncmp.dmi.model.ModuleReference;
import org.onap.cps.ncmp.dmi.model.ModuleResources;
import org.onap.cps.ncmp.dmi.model.ModuleSet;
import org.onap.cps.ncmp.dmi.rest.api.DmiPluginApi;
import org.onap.cps.ncmp.dmi.rest.api.DmiPluginInternalApi;
import org.onap.cps.ncmp.dmi.service.DmiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("${rest.api.dmi-base-path}")
@RestController
@Slf4j
public class DmiRestController implements DmiPluginApi, DmiPluginInternalApi {

    private DmiService dmiService;

    private ObjectMapper objectMapper;

    public DmiRestController(final DmiService dmiService, final ObjectMapper objectMapper) {
        this.dmiService = dmiService;
        this.objectMapper = objectMapper;
    }

    @Override
    public ResponseEntity<ModuleSet> getModulesForCmHandle(final String cmHandle) {
        final var moduleSet = dmiService.getModulesForCmHandle(cmHandle);
        return new ResponseEntity<>(moduleSet, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ModuleResources> retrieveModuleResources(@Valid final DmiReadRequestBody dmiReadRequestBody,
        final String cmHandle) {
        if (dmiReadRequestBody.getOperation().toString().equals("read")) {
            final var moduleReferenceList = convertRestObjectToJavaApiObject(dmiReadRequestBody);
            final var response = dmiService.getModuleResources(cmHandle, moduleReferenceList);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

    /**
     * Write data using passthrough for the given cmHandle.
     *
     * @param dataAccessWriteRequest pass through request
     * @param cmHandle               cmHandle
     * @param resourceIdentifier     resource identifier
     * @return (@ code ResponseEntity) response entity
     */
    @Override
    public ResponseEntity<String> writeDataByPassthroughRunningForCmHandle(
        final DataAccessWriteRequest dataAccessWriteRequest,
        final String cmHandle, final String resourceIdentifier) {
        final String response = dmiService.writeResourceDataPassthroughForCmHandle(cmHandle,
            resourceIdentifier,
            MediaType.APPLICATION_JSON_VALUE,
            dataAccessWriteRequest.getData());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * This method register given list of cm-handles to ncmp.
     *
     * @param cmHandles list of cm-handles
     * @return (@ code ResponseEntity) response entity
     */
    public ResponseEntity<String> registerCmHandles(final @Valid CmHandles cmHandles) {
        final List<String> cmHandlesList = cmHandles.getCmHandles();
        if (cmHandlesList.isEmpty()) {
            return new ResponseEntity<>("Need at least one cmHandle to process.", HttpStatus.BAD_REQUEST);
        }
        dmiService.registerCmHandles(cmHandlesList);
        return new ResponseEntity<>("cm-handle registered successfully.", HttpStatus.CREATED);
    }

    /**
     * This method fetches the resource for given cm handle using pass through operational. It filters the response on
     * the basis of depth and field query parameters and returns response.
     *
     * @param cmHandle              cm handle identifier
     * @param resourceIdentifier    resource identifier to fetch data
     * @param dataAccessReadRequest data Access Read Request
     * @param accept                accept header parameter
     * @param fields                fields to filter the response data
     * @param depth                 depth parameter for the response
     * @return {@code ResponseEntity} response entity
     */
    @Override
    public ResponseEntity<Object> getResourceDataOperationalForCmHandle(final String cmHandle,
        final String resourceIdentifier,
        final @Valid DataAccessReadRequest dataAccessReadRequest,
        final String accept,
        final @Valid String fields,
        final @Min(1) @Valid Integer depth) {
        final var modulesListAsJson = dmiService.getResourceDataOperationalForCmHandle(cmHandle,
            resourceIdentifier,
            accept,
            fields,
            depth,
            dataAccessReadRequest.getCmHandleProperties());
        return ResponseEntity.ok(modulesListAsJson);
    }

    /**
     * This method fetches the resource for given cm handle using pass through running. It filters the response on the
     * basis of depth and field query parameters and returns response.
     *
     * @param cmHandle              cm handle identifier
     * @param resourceIdentifier    resource identifier to fetch data
     * @param dataAccessReadRequest data Access Read Request
     * @param accept                accept header parameter
     * @param fields                fields to filter the response data
     * @param depth                 depth parameter for the response
     * @return {@code ResponseEntity} response entity
     */
    @Override
    public ResponseEntity<Object> getResourceDataPassthroughRunningForCmHandle(final String cmHandle,
        final String resourceIdentifier,
        final @Valid DataAccessReadRequest dataAccessReadRequest,
        final String accept,
        final @Valid String fields,
        final @Min(1) @Valid Integer depth) {
        final var modulesListAsJson = dmiService.getResourceDataPassThroughRunningForCmHandle(cmHandle,
            resourceIdentifier,
            accept,
            fields,
            depth,
            dataAccessReadRequest.getCmHandleProperties());
        return ResponseEntity.ok(modulesListAsJson);
    }

    private List<ModuleReference> convertRestObjectToJavaApiObject(final DmiReadRequestBody dmiReadRequestBody) {
        return objectMapper
            .convertValue(dmiReadRequestBody.getData().getModules(), new TypeReference<List<ModuleReference>>() {
            });
    }
}