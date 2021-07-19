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
     * @param cmHandleId cm-handle id to fetch the modules information
     * @param jsonBody body of post request
     * @return {@code boolean} returns true for success and false for failure
     */
    Object getModulesForCmhandle(String cmHandleId, String jsonBody);

}
