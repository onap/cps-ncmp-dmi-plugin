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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.service.DmiService;
import org.onap.cps.ncmp.rest.api.DmiPluginApi;
import org.onap.cps.ncmp.rest.model.CmHandles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("${rest.api.dmi-base-path}")
@RestController
@Slf4j
public class DmiRestController implements DmiPluginApi {

    @Autowired
    private DmiService dmiService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.
     *
     * @param cmHandles list of cm-handles
     * @return (@code ResponseEntity) response entity
     */
    public ResponseEntity<String> registerCmHandles(final @Valid CmHandles cmHandles) {
        try {
            final String jsonString = objectMapper.writeValueAsString(cmHandles);
            final boolean result = dmiService.registerCmHandles(jsonString);
            return result ? new ResponseEntity<>(HttpStatus.CREATED) : new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (final JsonProcessingException e) {
            log.error("Parsing error occurred while converting cm-handles to JSON {}", cmHandles);
            return new ResponseEntity<>("Wrong data", HttpStatus.BAD_REQUEST);
        }
    }

}
