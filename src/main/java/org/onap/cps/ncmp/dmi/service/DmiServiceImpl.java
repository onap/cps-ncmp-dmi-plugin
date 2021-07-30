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
import io.micrometer.core.instrument.util.StringUtils;
import java.util.LinkedHashMap;
import java.util.List;
import org.onap.cps.ncmp.dmi.exception.DmiException;
import org.onap.cps.ncmp.dmi.exception.ModuleResourceNotFoundException;
import org.onap.cps.ncmp.dmi.exception.ModulesNotFoundException;
import org.onap.cps.ncmp.dmi.service.models.ModuleData;
import org.onap.cps.ncmp.dmi.service.operation.SdncOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DmiServiceImpl implements DmiService {

    private SdncOperations sdncOperations;

    private ObjectMapper objectMapper;

    @Autowired
    public DmiServiceImpl(final SdncOperations sdncOperations, final ObjectMapper objectMapper) {
        this.sdncOperations = sdncOperations;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getModulesForCmHandle(final String cmHandle) throws DmiException {
        final ResponseEntity<String> responseEntity = sdncOperations.getModulesFromNode(cmHandle);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            final String body = responseEntity.getBody();
            if (StringUtils.isEmpty(body)) {
                throw new ModulesNotFoundException(cmHandle, "SDNC returned no modules for given cm-handle.");
            }
            return responseEntity.getBody();
        } else {
            throw new DmiException("SDNC is not able to process request.",
                "response code : " + responseEntity.getStatusCode() + " message : " + responseEntity.getBody());
        }
    }

    @Override
    public String getModuleSources(final String cmHandle, final List<ModuleData> moduleData) {
        final var response = new StringBuilder();
        for (final var module : moduleData) {
            final var moduleRequest = createModuleRequest(module);
            final var responseEntity = sdncOperations.getYangResources(cmHandle, moduleRequest);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                response.append(responseEntity.getBody());
            } else {
                throw new ModuleResourceNotFoundException(cmHandle,
                    "SDNC did not return a module resource for given cmHandle.");
            }
        }
        return response.toString();
    }

    private String createModuleRequest(final ModuleData moduleData) {
        final var module = new LinkedHashMap<>();
        module.put("ietf-netconf-monitoring:identifier", moduleData.getName());
        module.put("ietf-netconf-monitoring:version", moduleData.getRevision());
        final var writer = objectMapper.writer().withRootName("ietf-netconf-monitoring:input");
        final String moduleRequest;
        try {
            moduleRequest = writer.writeValueAsString(module);
        } catch (final JsonProcessingException e) {
            throw new DmiException("Unable to process JSON.",
                "Json exception occurred when processing creating the module request.", e);
        }
        return moduleRequest;
    }
}
