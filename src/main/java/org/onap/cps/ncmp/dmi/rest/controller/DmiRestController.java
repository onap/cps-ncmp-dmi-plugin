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
import javax.validation.Valid;
import org.onap.cps.ncmp.dmi.model.ModuleSources;
import org.onap.cps.ncmp.dmi.rest.api.DmiPluginApi;
import org.onap.cps.ncmp.dmi.service.DmiService;
import org.onap.cps.ncmp.dmi.service.models.Modules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("${rest.api.dmi-base-path}")
@RestController
public class DmiRestController implements DmiPluginApi {

    private DmiService dmiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public DmiRestController(final DmiService dmiService) {
        this.dmiService = dmiService;
    }

    @Override
    public ResponseEntity<String> getModulesForCmHandle(final String cmHandle) {

        final String modulesListAsJson = dmiService.getModulesForCmHandle(cmHandle);
        return new ResponseEntity<>(modulesListAsJson, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> retrieveModuleResources(@Valid final ModuleSources moduleSources,
        final String cmHandle) {
        final var modulesData = convertRestObjectToJavaApiObject(moduleSources);
        final var response = dmiService.getModuleSources(cmHandle, modulesData.getModules());
        if (!response.isEmpty()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    private Modules convertRestObjectToJavaApiObject(final ModuleSources moduleSources) {
        return objectMapper.convertValue(moduleSources.getData(), Modules.class);
    }
}
