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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import javax.validation.Valid;
import org.onap.cps.ncmp.dmi.model.OperationBody;
import org.onap.cps.ncmp.dmi.model.RequestOperation;
import org.onap.cps.ncmp.dmi.rest.api.DmiPluginApi;
import org.onap.cps.ncmp.dmi.service.DmiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("${rest.api.dmi-base-path}")
@RestController
public class DmiRestController implements DmiPluginApi {

    @Autowired
    private DmiService dmiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ResponseEntity<Object> helloWorld() {
        final var helloWorld = dmiService.getHelloWorld();
        return new ResponseEntity<>(helloWorld, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getModulesForCmhandle(@Valid final OperationBody operationBody,
                                                        final String cmhandleid) {
        if (cmhandleid == null
                || cmhandleid.isEmpty()
                || operationBody.getOperation() != OperationBody.OperationEnum.READ) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        final RequestOperation requestOperation = objectMapper.convertValue(operationBody, RequestOperation.class);
        final Optional<String> optional = dmiService.getModulesForCmhandle(cmhandleid, requestOperation);

        if (optional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(optional.get(), HttpStatus.OK);
    }

}
