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

import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.model.CmHandles;
import org.onap.cps.ncmp.dmi.rest.api.DmiPluginApi;
import org.onap.cps.ncmp.dmi.rest.api.DmiPluginInternalApi;
import org.onap.cps.ncmp.dmi.service.DmiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("${rest.api.dmi-base-path}")
@RestController
@Slf4j
public class DmiRestController implements DmiPluginApi, DmiPluginInternalApi {

    private DmiService dmiService;

    @Autowired
    public DmiRestController(final DmiService dmiService) {
        this.dmiService = dmiService;
    }

    @Override
    public ResponseEntity<String> getModulesForCmHandle(final String cmHandle) {

        final String modulesListAsJson = dmiService.getModulesForCmHandle(cmHandle);
        return new ResponseEntity<>(modulesListAsJson, HttpStatus.OK);
    }

    /**
     * This method register given list of cm-handles to ncmp.
     *
     * @param cmHandles list of cm-handles
     * @return (@code ResponseEntity) response entity
     */
    public ResponseEntity<String> registerCmHandles(final @Valid CmHandles cmHandles) {
        final List<String> cmHandlesList = cmHandles.getCmHandles();
        if (cmHandlesList.isEmpty()) {
            return new ResponseEntity<>("Need at least one cmHandle to process.", HttpStatus.BAD_REQUEST);
        }
        dmiService.registerCmHandles(cmHandlesList);
        return new ResponseEntity<>("cm-handle registered successfully.", HttpStatus.CREATED);
    }

    /**
     * This method fetches the resource for given cm handle using pass
     * through option. It filters the response on the basis of depth and field
     * query parameters and returns response.

     * @param cmHandle cm handle identifier
     * @param passThroughParam pass through datastore parameter
     * @param resourceIdentifier resource identifier to fetch data
     * @param accept accept header parameter
     * @param fields fields to filter the response data
     * @param depth depth parameter for the response
     * @return {@code ResponseEntity} response entity
     */
    @Override
    public ResponseEntity<Object> getResourceDataForCmHandle(final @Valid String cmHandle,
                                                         final @Valid String passThroughParam,
                                                         final @Valid String resourceIdentifier,
                                                         final @Valid String accept,
                                                         final @Valid String fields,
                                                         final @Valid Integer depth) {
        final var modulesListAsJson = dmiService.getResourceDataForCmHandle(cmHandle,
                                                                                    passThroughParam,
                                                                                    resourceIdentifier,
                                                                                    accept,
                                                                                    fields,
                                                                                    depth);
        return new ResponseEntity<>(modulesListAsJson, HttpStatus.OK);
    }
}
