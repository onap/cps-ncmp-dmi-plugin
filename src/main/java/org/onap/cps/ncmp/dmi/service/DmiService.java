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

import java.util.List;
import org.onap.cps.ncmp.dmi.exception.DmiException;
import org.onap.cps.ncmp.dmi.model.ModuleReference;

/**
 * Interface for handling Dmi plugin Data.
 */
public interface DmiService {

    /**
     * This method fetches all modules for given Cm Handle.
     *
     * @param cmHandle cm-handle to fetch the modules information
     * @return {@code String} returns all modules
     * @throws DmiException can throw dmi exception
     */
    String getModulesForCmHandle(String cmHandle) throws DmiException;

    /**
     * This method used to register the given {@code CmHandles}
     * which contains list of {@code CmHandle} to cps repository.
     *
     * @param cmHandles list of cm-handles
     */
    void registerCmHandles(List<String> cmHandles);

    /**
     * Get module sources for the given cm handle and modules.
     *
     * @param cmHandle cmHandle
     * @param modules a list of module data
     * @return returns all module sources
     */
    String getModuleSources(String cmHandle, List<ModuleReference> modules);
}
