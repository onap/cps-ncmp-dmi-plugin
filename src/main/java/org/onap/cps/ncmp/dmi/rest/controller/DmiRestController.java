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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.model.CmHandles;
import org.onap.cps.ncmp.dmi.model.ModuleReferences;
import org.onap.cps.ncmp.dmi.model.Modules;
import org.onap.cps.ncmp.dmi.rest.api.DmiPluginApi;
import org.onap.cps.ncmp.dmi.rest.api.DmiPluginInternalApi;
import org.onap.cps.ncmp.dmi.service.DmiService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<String> getModulesForCmHandle(final String cmHandle) {

        final String modulesListAsJson = dmiService.getModulesForCmHandle(cmHandle);
        return new ResponseEntity<>(modulesListAsJson, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> retrieveModuleResources(@Valid final ModuleReferences moduleReferences,
        final String cmHandle) {
        final var modulesData = convertRestObjectToJavaApiObject(moduleReferences);
        final var response = dmiService.getModuleResources(cmHandle, modulesData.getModules());
        if (response.isEmpty()) {
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
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

    private Modules convertRestObjectToJavaApiObject(final ModuleReferences moduleReferences) {
        return objectMapper.convertValue(moduleReferences.getData(), Modules.class);
    }
}
