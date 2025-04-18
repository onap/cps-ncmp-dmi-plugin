/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2025 OpenInfra Foundation Europe. All rights reserved.
 *  Modifications Copyright (C) 2022 Bell Canada
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

import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.onap.cps.ncmp.dmi.exception.DmiException;
import org.onap.cps.ncmp.dmi.model.DataAccessRequest;
import org.onap.cps.ncmp.dmi.model.ModuleSet;
import org.onap.cps.ncmp.dmi.model.YangResources;
import org.onap.cps.ncmp.dmi.service.model.ModuleReference;



/**
 * Interface for handling Dmi plugin Data.
 */
public interface DmiService {

    String RESTCONF_CONTENT_PASSTHROUGH_OPERATIONAL_QUERY_PARAM = "content=all";
    String RESTCONF_CONTENT_PASSTHROUGH_RUNNING_QUERY_PARAM = "content=config";

    /**
     * This method fetches all modules for given Cm Handle.
     *
     * @param cmHandle cm-handle to fetch the modules information
     * @return {@code String} returns all modules
     * @throws DmiException can throw dmi exception
     */
    ModuleSet getModulesForCmHandle(String cmHandle) throws DmiException;

    /**
     * This method used to register the given {@code CmHandles} which contains list of {@code CmHandle} to cps
     * repository.
     *
     * @param cmHandles list of cm-handles
     */
    void registerCmHandles(List<String> cmHandles);

    /**
     * This method is used to enable data synchronization for
     * the given {@code CmHandles}.
     *
     * @param cmHandles list of cm-handles
     */
    void enableNcmpDataSyncForCmHandles(List<String> cmHandles);


    /**
     * Get module resources for the given cm handle and modules.
     *
     * @param cmHandle cmHandle
     * @param modules  a list of module data
     * @return returns all yang resources
     */
    YangResources getModuleResources(String cmHandle, List<ModuleReference> modules);

    /**
     * This method use to fetch the resource data from cm handle for the given datastore and resource
     * Identifier. Options query parameter are used to filter the response from network resource.
     *
     * @param cmHandle                  cm handle identifier
     * @param resourceIdentifier        resource identifier
     * @param optionsParamInQuery       options query parameter
     * @param restconfContentQueryParam restconf content i.e. datastore to use
     * @return {@code Object} response from network function
     */
    String getResourceData(@NotNull String cmHandle,
        @NotNull String resourceIdentifier,
        String optionsParamInQuery,
        String restconfContentQueryParam);

    /**
     * Write resource data to sdnc (will default to 'content=config', does not need to be specified).
     *
     * @param cmHandle           cmHandle
     * @param resourceIdentifier resource identifier
     * @param dataType           accept header parameter
     * @param data               request data
     * @return response from sdnc
     */
    String writeData(DataAccessRequest.OperationEnum operation, String cmHandle,
                     String resourceIdentifier, String dataType,
                     String data);
}
