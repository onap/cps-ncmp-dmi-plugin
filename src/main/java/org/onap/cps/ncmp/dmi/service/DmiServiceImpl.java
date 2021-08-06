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
import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.onap.cps.ncmp.dmi.config.DmiPluginConfig.DmiPluginProperties;
import org.onap.cps.ncmp.dmi.exception.CmHandleRegistrationException;
import org.onap.cps.ncmp.dmi.exception.DmiException;
import org.onap.cps.ncmp.dmi.exception.ModulesNotFoundException;
import org.onap.cps.ncmp.dmi.model.CmHandleOperation;
import org.onap.cps.ncmp.dmi.model.CreatedCmHandle;
import org.onap.cps.ncmp.dmi.service.client.NcmpRestClient;
import org.onap.cps.ncmp.dmi.service.operation.SdncOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;


@Service
@Slf4j
public class DmiServiceImpl implements DmiService {

    private static final String PASSTHROUGH_OPERATIONAL = "ncmp-datastore:passthrough-operational";

    private SdncOperations sdncOperations;
    private NcmpRestClient ncmpRestClient;
    private ObjectMapper objectMapper;
    private DmiPluginProperties dmiPluginProperties;

    /**
     * Constructor.
     *
     * @param dmiPluginProperties dmiPluginProperties
     * @param ncmpRestClient ncmpRestClient
     * @param objectMapper objectMapper
     * @param sdncOperations sdncOperations
     */
    @Autowired
    public DmiServiceImpl(final DmiPluginProperties dmiPluginProperties,
                          final NcmpRestClient ncmpRestClient,
                          final ObjectMapper objectMapper,
                          final SdncOperations sdncOperations) {
        this.dmiPluginProperties = dmiPluginProperties;
        this.ncmpRestClient = ncmpRestClient;
        this.objectMapper = objectMapper;
        this.sdncOperations = sdncOperations;
    }

    @Override
    public String getModulesForCmHandle(final String cmHandle) throws DmiException {
        final ResponseEntity<String> responseEntity = sdncOperations.getModulesFromNode(cmHandle);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            final String responseBody = responseEntity.getBody();
            if (StringUtils.isEmpty(responseBody)) {
                throw new ModulesNotFoundException(cmHandle, "SDNC returned no modules for given cm-handle.");
            }
            return responseBody;
        } else {
            throw new DmiException("SDNC is not able to process request.",
                    "response code : " + responseEntity.getStatusCode() + " message : " + responseEntity.getBody());
        }
    }

    @Override
    public void registerCmHandles(final List<String> cmHandles) {
        final CmHandleOperation cmHandleOperation = new CmHandleOperation();
        cmHandleOperation.setDmiPlugin(dmiPluginProperties.getDmiServiceName());
        final List<CreatedCmHandle> createdCmHandleList = new ArrayList<>();
        for (final String cmHandle: cmHandles) {
            final CreatedCmHandle createdCmHandle = new CreatedCmHandle();
            createdCmHandle.setCmHandle(cmHandle);
            createdCmHandleList.add(createdCmHandle);
        }
        cmHandleOperation.setCreatedCmHandles(createdCmHandleList);
        final String cmHandlesJson;
        try {
            cmHandlesJson = objectMapper.writeValueAsString(cmHandleOperation);
        } catch (final JsonProcessingException e) {
            log.error("Parsing error occurred while converting cm-handles to JSON {}", cmHandles);
            throw new DmiException("Internal Server Error.",
                    "Parsing error occurred while converting given cm-handles object list to JSON ");
        }
        final ResponseEntity<String> responseEntity = ncmpRestClient.registerCmHandlesWithNcmp(cmHandlesJson);
        if (!(responseEntity.getStatusCode() == HttpStatus.CREATED)) {
            throw new CmHandleRegistrationException(responseEntity.getBody());
        }
    }

    @Override
    public Object getResourceDataForCmHandle(final @NotNull String cmHandle,
                                             final @NotNull String passThroughParam,
                                             final @NotNull String resourceIdentifier,
                                             final String fieldsQuery,
                                             final Integer depthQuery) {
        final List<String> queryList = getQueryList(fieldsQuery, depthQuery);
        addContentQueryByPassthrough(passThroughParam, queryList);
        final ResponseEntity<String> responseEntity = sdncOperations.getResouceDataFromNode(cmHandle,
                                                                                            resourceIdentifier,
                                                                                            queryList);
        return preareAndSendResponse(responseEntity);
    }

    private String preareAndSendResponse(ResponseEntity<String> responseEntity) {
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        } else {
            throw new DmiException("SDNC returned no value for this request.",
                    "response code : " + responseEntity.getStatusCode() + " message : " + responseEntity.getBody());
        }
    }

    private void addContentQueryByPassthrough(final String passThroughParam, final List<String> queryList) {
        switch (passThroughParam) {
            case PASSTHROUGH_OPERATIONAL: queryList.add("content=all");
                                          break;
            default: throw  new DmiException("Wrong pass-through datastore type.",
                    "Wrong pass-through datastore type given in url, please provide correct pass-through datastore.");
        }
    }

    @NotNull
    private List<String> getQueryList(final String fieldsQuery, final Integer depthQuery) {
        final List <String> queryList = new LinkedList<>();
        if(fieldsQuery != null && !fieldsQuery.isEmpty()){
            queryList.add("fields="+ fieldsQuery);
        }
        if(depthQuery !=null){
            queryList.add("depth="+ depthQuery);
        }
        return queryList;
    }
}
