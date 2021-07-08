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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.config.DmiPluginConfig.DmiPluginProperties;
import org.onap.cps.ncmp.dmi.exception.CmHandleRegistrationException;
import org.onap.cps.ncmp.dmi.exception.DmiException;
import org.onap.cps.ncmp.dmi.model.CmHandleOperation;
import org.onap.cps.ncmp.dmi.model.CreatedCmHandle;
import org.onap.cps.ncmp.dmi.service.client.NcmpRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DmiServiceImpl implements DmiService {

    private NcmpRestClient ncmpRestClient;

    private ObjectMapper objectMapper;

    private DmiPluginProperties dmiPluginProperties;

    /**
     * Constructor.
     *
     * @param dmiPluginProperties dmiPluginProperties
     * @param ncmpRestClient ncmpRestClient
     * @param objectMapper objectMapper
     */
    @Autowired
    public DmiServiceImpl(final DmiPluginProperties dmiPluginProperties,
                          final NcmpRestClient ncmpRestClient,
                          final ObjectMapper objectMapper) {
        this.dmiPluginProperties = dmiPluginProperties;
        this.ncmpRestClient = ncmpRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean registerCmHandles(final List<String> cmHandles) {
        final CmHandleOperation cmHandleOperation = new CmHandleOperation();
        cmHandleOperation.setDmiPlugin(dmiPluginProperties.getDmiServiceName());
        final List<CreatedCmHandle> createdCmHandleList = new ArrayList<>();
        for (final String cmHandle: cmHandles) {
            final CreatedCmHandle createdCmHandle = new CreatedCmHandle();
            createdCmHandle.setCmhandle(cmHandle);
            createdCmHandleList.add(createdCmHandle);
        }
        cmHandleOperation.setCreatedCmHandles(createdCmHandleList);
        final String cmHandlesJson;

        try {
            cmHandlesJson = objectMapper.writeValueAsString(cmHandleOperation);
        } catch (final JsonProcessingException e) {
            log.error("Parsing error occurred while converting cm-handles to JSON {}", cmHandles);
            throw new DmiException("Internal Server Error.",
                    "Parsing error occurred while converting given cm-handles to JSON ");
        }
        final ResponseEntity<String> responseEntity = ncmpRestClient.registerCmHandlesWithNcmp(cmHandlesJson);

        if (responseEntity.getStatusCode() == HttpStatus.OK
                || responseEntity.getStatusCode() == HttpStatus.CREATED) {
            return true;
        } else {
            throw new CmHandleRegistrationException(responseEntity.getBody());
        }
    }
}
