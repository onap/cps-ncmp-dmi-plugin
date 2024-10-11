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
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.rest.stub.model.module.ModuleReference;
import org.onap.cps.ncmp.dmi.rest.stub.model.module.ModuleReferences;
import org.onap.cps.ncmp.dmi.rest.stub.model.module.ModuleResource;
import org.onap.cps.ncmp.dmi.rest.stub.utils.ModuleSetTagUtil;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class YangModuleFactory {

    private static final int TARGET_SIZE = 32;
    private static final int KILOBYTE = 1024;
    private static final List<String> NUMBER_OF_MODULE_SET_TAGS = ModuleSetTagUtil.generateTags('A', 'E');
    private static final String DEFAULT_TAG = "tagDefault";
    private static final int NUMBER_OF_MODULES_PER_MODULE_SET = 200;
    private static final int NUMBER_OF_MODULES_NOT_IN_MODULE_SET = 10;
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

    private final ObjectMapper objectMapper;
    private final Map<String, String> moduleReferencesJsonMap = new HashMap<>();
    private final Map<String, String> moduleResourcesJsonMap = new HashMap<>();

    /**
     * Retrieves the JSON representation of module references for the given tag.
     *
     * @param tag the tag identifying the set of module references
     * @return the JSON string of module references for the specified tag, or the default tag if not found
     */
    public String getModuleReferencesJson(final String tag) {
        return moduleReferencesJsonMap.getOrDefault(tag, moduleReferencesJsonMap.get(DEFAULT_TAG));
    }

    /**
     * Retrieves the JSON representation of module resources for the given tag.
     *
     * @param tag the tag identifying the set of module resources
     * @return the JSON string of module resources for the specified tag, or the default tag if not found
     */
    public String getModuleResourcesJson(final String tag) {
        return moduleResourcesJsonMap.getOrDefault(tag, moduleResourcesJsonMap.get(DEFAULT_TAG));
    }

    @PostConstruct
    private void initializeModuleJsonStrings() {
        for (final String tag : NUMBER_OF_MODULE_SET_TAGS) {
            moduleReferencesJsonMap.put(tag, makeModuleReferences(NUMBER_OF_MODULES_PER_MODULE_SET, tag));
            moduleResourcesJsonMap.put(tag, makeModuleResources(NUMBER_OF_MODULES_PER_MODULE_SET, tag));
        }
        moduleReferencesJsonMap.put(DEFAULT_TAG,
            makeModuleReferences(NUMBER_OF_MODULES_NOT_IN_MODULE_SET, DEFAULT_TAG));
        moduleResourcesJsonMap.put(DEFAULT_TAG,
            makeModuleResources(NUMBER_OF_MODULES_NOT_IN_MODULE_SET, DEFAULT_TAG));
    }

    private String makeModuleReferences(final int length, final String tag) {
        final List<ModuleReference> moduleReferencesList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            final String moduleName = "module" + i;
            final String revision = makeModuleRevision(tag, i);
            moduleReferencesList.add(new ModuleReference(moduleName, revision));
        }
        final ModuleReferences moduleReferences = new ModuleReferences(moduleReferencesList);
        try {
            return objectMapper.writeValueAsString(moduleReferences);
        } catch (final JsonProcessingException jsonProcessingException) {
            log.error("Error serializing ModuleReferences: {}", jsonProcessingException.getMessage());
            return null;
        }
    }

    private String makeModuleResources(final int length, final String tag) {
        final List<ModuleResource> moduleResourceList = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            final String moduleName = "module" + i;
            final String revision = makeModuleRevision(tag, i);
            final String yangSource = makeYangSource(moduleName, revision);
            moduleResourceList.add(new ModuleResource(moduleName, revision, yangSource));
        }

        try {
            return objectMapper.writeValueAsString(moduleResourceList);
        } catch (final JsonProcessingException jsonProcessingException) {
            log.error("Error serializing ModuleResources: {}", jsonProcessingException.getMessage());
            return null;
        }
    }

    private static String makeModuleRevision(final String tag, final int index) {
        return LocalDate.of(2021, 1, 1).plusDays(index) + "-" + tag;
    }

    private static String makeYangSource(final String moduleName, final String moduleRevision) {
        final String padding = String.valueOf('*').repeat(TARGET_SIZE * KILOBYTE - MODULE_TEMPLATE.length());
        return MODULE_TEMPLATE.replaceAll("<MODULE_NAME>", moduleName)
            .replaceAll("<MODULE_REVISION>", moduleRevision)
            .replaceAll("<DESCRIPTION>", padding);
    }
}