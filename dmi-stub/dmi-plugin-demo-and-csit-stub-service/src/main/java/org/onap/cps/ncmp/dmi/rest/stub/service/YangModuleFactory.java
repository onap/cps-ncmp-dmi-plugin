/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2024 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.ncmp.dmi.rest.stub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.rest.stub.model.module.ModuleReference;
import org.onap.cps.ncmp.dmi.rest.stub.model.module.ModuleReferences;
import org.onap.cps.ncmp.dmi.rest.stub.model.module.ModuleResource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class YangModuleFactory {

    private final ObjectMapper objectMapper;
    private static final int KILOBYTE = 1000;
    private static final String MODULE_TEMPLATE = """
        module <MODULE_NAME> {
            yang-version 1.1;
            namespace "org:onap:cps:test:<MODULE_NAME>";
            prefix tree;
            revision "<MODULE_REVISION>" {
                description "<DESCRIPTION>";
            }
            container tree {
                list branch {
                    key "name";
                    leaf name {
                        type string;
                    }
                }
            }
        }
        """;

    public String makeModuleReferences(int length) {
        List<ModuleReference> moduleReferencesList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            String moduleName = "module" + i;
            String revision = "2021-01-" + String.format("%02d", (i % 31) + 1);
            moduleReferencesList.add(new ModuleReference(moduleName, revision));
        }
        ModuleReferences moduleReferences = new ModuleReferences(moduleReferencesList);
        try {
            return objectMapper.writeValueAsString(moduleReferences);
        } catch (JsonProcessingException e) {
            log.error("Error serializing ModuleReferences: {}", e.getMessage());
            return null;
        }
    }

    public String makeModuleResources(int length) {
        List<ModuleResource> moduleResourceList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            String moduleName = "module" + i;
            String revision = "2021-01-" + String.format("%02d", (i % 31) + 1);
            String yangSource = makeYangSource(moduleName);
            moduleResourceList.add(new ModuleResource(moduleName, revision, yangSource));
        }

        try {
            return objectMapper.writeValueAsString(moduleResourceList);
        } catch (JsonProcessingException e) {
            log.error("Error serializing ModuleReferences: {}", e.getMessage());
            return null;
        }
    }

    private static String makeYangSource(String moduleName) {
        final String padding = String.valueOf('*').repeat(100 * KILOBYTE - MODULE_TEMPLATE.length());
        return MODULE_TEMPLATE.replaceAll("<MODULE_NAME>", moduleName)
            .replaceAll("<MODULE_REVISION>", "2021-01-01")
            .replaceAll("<DESCRIPTION>", padding);
    }
}