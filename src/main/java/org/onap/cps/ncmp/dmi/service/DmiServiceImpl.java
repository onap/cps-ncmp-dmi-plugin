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
import org.onap.cps.ncmp.dmi.exception.DmiException;
import org.onap.cps.ncmp.dmi.service.operation.SdncOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DmiServiceImpl implements DmiService {

    private SdncOperations sdncOperations;

    @Autowired
    public DmiServiceImpl(final SdncOperations sdncOperations) {
        this.sdncOperations = sdncOperations;
    }

    @Override
    public String getHelloWorld() {
        return "Hello World";
    }

    @Override
    public Optional<String> getModulesForCmHandle(final String cmHandle) throws DmiException {
        final ResponseEntity<String> responseEntity = sdncOperations.getModulesFromNode(cmHandle);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return Optional.of(responseEntity.getBody());
        } else {
            throw new DmiException("Server is not able to process request.",
                    "server code : " + responseEntity.getStatusCode() + " message : " + responseEntity.getBody());
        }
    }
}
