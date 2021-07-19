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

import java.util.Optional;

/**
 * Interface for handling Dmi plugin Data.
 */
public interface DmiService {
    /**
     * Return Simple Hello World Statement.
     */
    String getHelloWorld();

    /**
     * This method used to register the given {@code CmHandles}
     * which contains list of {@code CmHandle} to cps repository.
     *
     * @param cmHandle cm-handle to fetch the modules information
     * @return {@code boolean} returns true for success and false for failure
     */
    Optional<String> getModulesForCmhandle(String cmHandle);

}
