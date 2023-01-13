/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2022 Nordix Foundation
 *  Modifications Copyright (C) 2022 Bell Canada
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

import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.model.CmHandles;
import org.onap.cps.ncmp.dmi.model.DataAccessRequest;
import org.onap.cps.ncmp.dmi.model.ModuleReferencesRequest;
import org.onap.cps.ncmp.dmi.model.ModuleResourcesReadRequest;
import org.onap.cps.ncmp.dmi.model.ModuleSet;
import org.onap.cps.ncmp.dmi.model.YangResources;
import org.onap.cps.ncmp.dmi.notifications.async.AsyncTaskExecutor;
import org.onap.cps.ncmp.dmi.notifications.avc.DmiDataAvcEventCreator;
import org.onap.cps.ncmp.dmi.notifications.avc.DmiDataAvcEventProducer;
import org.onap.cps.ncmp.dmi.rest.api.DmiPluginApi;
import org.onap.cps.ncmp.dmi.rest.api.DmiPluginInternalApi;
import org.onap.cps.ncmp.dmi.service.DmiService;
import org.onap.cps.ncmp.dmi.service.model.ModuleReference;
import org.onap.cps.ncmp.event.model.AvcEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("${rest.api.dmi-base-path}")
@RestController
@Slf4j
@RequiredArgsConstructor
public class DmiRestController implements DmiPluginApi, DmiPluginInternalApi {

    private final DmiService dmiService;
    private final ObjectMapper objectMapper;
    private final AsyncTaskExecutor asyncTaskExecutor;

    private final DmiDataAvcEventProducer dmiDataAvcEventProducer;
    private static final Map<OperationEnum, HttpStatus> operationToHttpStatusMap = new HashMap<>(6);

    @Value("${notification.async.executor.time-out-value-in-ms:2000}")
    private int timeOutInMillis;

    static {
        operationToHttpStatusMap.put(null, HttpStatus.OK);
        operationToHttpStatusMap.put(OperationEnum.READ, HttpStatus.OK);
        operationToHttpStatusMap.put(OperationEnum.CREATE, HttpStatus.CREATED);
        operationToHttpStatusMap.put(OperationEnum.PATCH, HttpStatus.OK);
        operationToHttpStatusMap.put(OperationEnum.UPDATE, HttpStatus.OK);
        operationToHttpStatusMap.put(OperationEnum.DELETE, HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<ModuleSet> getModuleReferences(final String cmHandle,
                                                         final @Valid ModuleReferencesRequest body) {
        // For onap-dmi-plugin we don't need cmHandleProperties, so DataAccessReadRequest is not used.
        final ModuleSet moduleSet = dmiService.getModulesForCmHandle(cmHandle);
        return ResponseEntity.ok(moduleSet);
    }

    @Override
    public ResponseEntity<YangResources> retrieveModuleResources(
        final @Valid ModuleResourcesReadRequest moduleResourcesReadRequest,
        final String cmHandle) {
        final List<ModuleReference> moduleReferences = convertRestObjectToJavaApiObject(moduleResourcesReadRequest);
        final YangResources yangResources = dmiService.getModuleResources(cmHandle, moduleReferences);
        return new ResponseEntity<>(yangResources, HttpStatus.OK);
    }

    /**
     * This method register given list of cm-handles to ncmp.
     *
     * @param cmHandles list of cm-handles
     * @return (@ code ResponseEntity) response entity
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
     * This method fetches the resource for given cm handle using pass through operational datastore. It filters the
     * response on the basis of options query parameters and returns response. Does not support write operations.
     *
     * @param resourceIdentifier    resource identifier to fetch data
     * @param cmHandle              cm handle identifier
     * @param dataAccessRequest     data Access Request
     * @param optionsParamInQuery   options query parameter
     * @param topicParamInQuery     topic name for (triggering) async responses
     * @return {@code ResponseEntity} response entity
     */
    @Override
    public ResponseEntity<Object> dataAccessPassthroughOperational(final String resourceIdentifier,
                                                                   final String cmHandle,
                                                                   final @Valid DataAccessRequest dataAccessRequest,
                                                                   final @Valid String optionsParamInQuery,
                                                                   final String topicParamInQuery) {
        if (isReadOperation(dataAccessRequest)) {
            if (hasTopic(topicParamInQuery)) {
                return handleAsyncRequest(resourceIdentifier, cmHandle, dataAccessRequest, optionsParamInQuery,
                    topicParamInQuery);
            }

            final String resourceDataAsJson = dmiService.getResourceData(cmHandle, resourceIdentifier,
                optionsParamInQuery, DmiService.RESTCONF_CONTENT_PASSTHROUGH_OPERATIONAL_QUERY_PARAM);
            return ResponseEntity.ok(resourceDataAsJson);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    /**
     * This method fetches the resource for given cm handle using pass through running datastore. It filters the
     * response on the basis of options query parameters and returns response. It supports both read and write
     * operation.
     *
     * @param resourceIdentifier    resource identifier to fetch data
     * @param cmHandle              cm handle identifier
     * @param dataAccessRequest     data Access Request
     * @param optionsParamInQuery   options query parameter
     * @param topicParamInQuery     topic name for (triggering) async responses
     * @return {@code ResponseEntity} response entity
     */
    @Override
    public ResponseEntity<Object> dataAccessPassthroughRunning(final String resourceIdentifier,
                                                               final String cmHandle,
                                                               final @Valid DataAccessRequest dataAccessRequest,
                                                               final @Valid String optionsParamInQuery,
                                                               final String topicParamInQuery) {
        if (hasTopic(topicParamInQuery)) {
            asyncTaskExecutor.executeAsyncTask(() ->
                    getSdncResponseForPassThroughRunning(
                        resourceIdentifier,
                        cmHandle,
                        dataAccessRequest,
                        optionsParamInQuery),
                topicParamInQuery,
                dataAccessRequest.getRequestId(),
                dataAccessRequest.getOperation(),
                timeOutInMillis
            );
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        final String sdncResponse =
            getSdncResponseForPassThroughRunning(resourceIdentifier, cmHandle, dataAccessRequest, optionsParamInQuery);
        return new ResponseEntity<>(sdncResponse, operationToHttpStatusMap.get(dataAccessRequest.getOperation()));
    }

    @Override
    public ResponseEntity<Void> simulateEvents(final Integer numberOfSimulatedEvents) {
        final DmiDataAvcEventCreator dmiDataAvcEventCreator =  new DmiDataAvcEventCreator();

        for (int i = 0; i < numberOfSimulatedEvents; i++) {
            final String eventCorrelationId = UUID.randomUUID().toString();
            final AvcEvent avcEvent = dmiDataAvcEventCreator.createEvent(eventCorrelationId);
            dmiDataAvcEventProducer.sendMessage(eventCorrelationId, avcEvent);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private String getSdncResponseForPassThroughRunning(final String resourceIdentifier,
                                                        final String cmHandle,
                                                        final DataAccessRequest dataAccessRequest,
                                                        final String optionsParamInQuery) {
        if (isReadOperation(dataAccessRequest)) {
            return dmiService.getResourceData(cmHandle, resourceIdentifier, optionsParamInQuery,
                DmiService.RESTCONF_CONTENT_PASSTHROUGH_RUNNING_QUERY_PARAM);
        }

        return dmiService.writeData(dataAccessRequest.getOperation(), cmHandle, resourceIdentifier,
            dataAccessRequest.getDataType(), dataAccessRequest.getData());
    }

    private boolean isReadOperation(final @Valid DataAccessRequest dataAccessRequest) {
        return dataAccessRequest.getOperation() == null
            || dataAccessRequest.getOperation().equals(DataAccessRequest.OperationEnum.READ);
    }

    private List<ModuleReference> convertRestObjectToJavaApiObject(
        final ModuleResourcesReadRequest moduleResourcesReadRequest) {
        return objectMapper
            .convertValue(moduleResourcesReadRequest.getData().getModules(),
                new TypeReference<List<ModuleReference>>() {});
    }

    private boolean hasTopic(final String topicParamInQuery) {
        return !(topicParamInQuery == null || topicParamInQuery.isBlank());
    }

    private ResponseEntity<Object> handleAsyncRequest(final String resourceIdentifier,
                                                      final String cmHandle,
                                                      final DataAccessRequest dataAccessRequest,
                                                      final String optionsParamInQuery,
                                                      final String topicParamInQuery) {
        asyncTaskExecutor.executeAsyncTask(() ->
                dmiService.getResourceData(
                    cmHandle,
                    resourceIdentifier,
                    optionsParamInQuery,
                    DmiService.RESTCONF_CONTENT_PASSTHROUGH_OPERATIONAL_QUERY_PARAM),
            topicParamInQuery,
            dataAccessRequest.getRequestId(),
            dataAccessRequest.getOperation(),
            timeOutInMillis
        );
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
