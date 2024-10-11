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
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class YangModuleFactory {

    private static final int TARGET_FILE_SIZE_IN_KB = 32 * 1024;
    private static final List<String> MODULE_SET_TAGS = generateTags();
    private static final String DEFAULT_TAG = "tagDefault";
    private static final int NUMBER_OF_MODULES_PER_MODULE_SET = 200;
    private static final int NUMBER_OF_MODULES_NOT_IN_MODULE_SET = 10;
    private static final String SERIALIZATION_ERROR = "Error serializing {}: {}";
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

    @PostConstruct
    private void initializeModuleJsonStrings() {
        MODULE_SET_TAGS.forEach(tag -> {
            moduleReferencesJsonMap.put(tag, createModuleReferencesJson(tag, NUMBER_OF_MODULES_PER_MODULE_SET));
            moduleResourcesJsonMap.put(tag, createModuleResourcesJson(tag, NUMBER_OF_MODULES_PER_MODULE_SET));
        });

        // Initialize default tag
        moduleReferencesJsonMap.put(DEFAULT_TAG,
            createModuleReferencesJson(DEFAULT_TAG, NUMBER_OF_MODULES_NOT_IN_MODULE_SET));
        moduleResourcesJsonMap.put(DEFAULT_TAG,
            createModuleResourcesJson(DEFAULT_TAG, NUMBER_OF_MODULES_NOT_IN_MODULE_SET));
    }

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

    /**
     * Generates a list of tags from 'A' to 'E'.
     *
     * @return a list of tags in the format "tagX" where X is each character from 'A' to 'E'
     */
    public static List<String> generateTags() {
        final List<String> tags = new ArrayList<>(5);
        for (char currentChar = 'A'; currentChar <= 'E'; currentChar++) {
            tags.add("tag" + currentChar);
        }
        return tags;
    }

    private String createModuleReferencesJson(final String tag, final int numberOfModules) {
        final List<ModuleReference> moduleReferencesList = new ArrayList<>(numberOfModules);
        final String moduleRevision = generateModuleRevision(tag);
        for (int i = 0; i < numberOfModules; i++) {
            moduleReferencesList.add(new ModuleReference("module" + i, moduleRevision));
        }
        return serializeToJson(new ModuleReferences(moduleReferencesList), "ModuleReferences");
    }

    private String createModuleResourcesJson(final String tag, final int numberOfModules) {
        final List<ModuleResource> moduleResourceList = new ArrayList<>(numberOfModules);
        final String moduleRevision = generateModuleRevision(tag);
        for (int i = 0; i < numberOfModules; i++) {
            final String moduleName = "module" + i;
            final String yangSource = generateYangSource(moduleName, moduleRevision);
            moduleResourceList.add(new ModuleResource(moduleName, moduleRevision, yangSource));
        }
        return serializeToJson(moduleResourceList, "ModuleResources");
    }

    private String serializeToJson(final Object objectToSerialize, final String objectType) {
        try {
            return objectMapper.writeValueAsString(objectToSerialize);
        } catch (final JsonProcessingException jsonProcessingException) {
            log.error(SERIALIZATION_ERROR, objectType, jsonProcessingException.getMessage());
            return null;
        }
    }

    private String generateModuleRevision(final String tag) {
        // set tagIndex to 0 for the default tag, otherwise set it to the index of the tag in the list
        final int tagIndex = tag.equals(DEFAULT_TAG) ? 0 : MODULE_SET_TAGS.indexOf(tag);
        return LocalDate.of(2024, tagIndex + 1, tagIndex + 1).toString();
    }

    private static String generateYangSource(final String moduleName, final String moduleRevision) {
        final int paddingSize = TARGET_FILE_SIZE_IN_KB - MODULE_TEMPLATE.length();
        final String padding = "*".repeat(Math.max(0, paddingSize));
        return MODULE_TEMPLATE.replaceAll("<MODULE_NAME>", moduleName)
            .replace("<MODULE_REVISION>", moduleRevision)
            .replace("<DESCRIPTION>", padding);
    }
}