/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.onap.cps.ncmp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.onap.cps.dmi.clients.NcmpRestClient;
import org.onap.cps.ncmp.rest.model.CmHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DmiServiceImpl implements DmiService {

    private static final Logger LOG = LoggerFactory.getLogger(DmiServiceImpl.class);

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    NcmpRestClient ncmpRestClient;

    public DmiServiceImpl() {
    }

    @Override
    public String getHelloWorld() {
        return "Hello World";
    }

    @Override
    public ResponseEntity<String> registerCmHandles(final List<CmHandle> cmHandles) {
        try {
            final String jsonString = mapper.writeValueAsString(cmHandles);
            return ncmpRestClient.registerCmHandlesWithNcmp(jsonString);
        }catch (final JsonProcessingException jPE)
        {
            LOG.error("Parsing error occured while converting cm-handles to JSON {}", cmHandles);
            return new ResponseEntity<>("Wrong data", HttpStatus.BAD_REQUEST);
        }

    }
}
