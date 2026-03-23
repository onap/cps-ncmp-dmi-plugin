/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2025-2026 OpenInfra Foundation Europe
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

package org.onap.cps.ncmp.dmi.rest.stub.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.provmns.api.ProvMnS;
import org.onap.cps.ncmp.dmi.provmns.model.ClassNameIdGetDataNodeSelectorParameter;
import org.onap.cps.ncmp.dmi.provmns.model.PatchItem;
import org.onap.cps.ncmp.dmi.provmns.model.PatchOperation;
import org.onap.cps.ncmp.dmi.provmns.model.Resource;
import org.onap.cps.ncmp.dmi.provmns.model.ResourceOneOf;
import org.onap.cps.ncmp.dmi.provmns.model.Scope;
import org.onap.cps.ncmp.dmi.rest.stub.utils.ControllerSimulation;
import org.onap.cps.ncmp.dmi.rest.stub.utils.ResourceFileReaderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${rest.api.provmns-base-path}")
@RequiredArgsConstructor
@Slf4j
public class ProvMnsStubController implements ProvMnS {

    static final ResourceOneOf dummyResource = new ResourceOneOf("some id");

    private final ApplicationContext applicationContext;
    private final ControllerSimulation controllerSimulation;

    static {
        dummyResource.setObjectClass("dummyClass");
        dummyResource.setObjectInstance("dummyInstance");
    }

    @Value("${delay.provmns-read-delay-ms}")
    private long provMnSReadDelayMs;
    @Value("${delay.provmns-write-delay-ms}")
    private long provMnSWriteDelayMs;

    /**
     * Replaces a complete single resource or creates it if it does not exist.
     *
     * @param httpServletRequest      URI request including path
     * @param resource                Resource representation of the resource to be created or replaced
     * @return {@code Object}         The representation of the updated resource is returned in the response
     *                                message body.
     */
    @Override
    public ResponseEntity<Object> putMoi(final HttpServletRequest httpServletRequest, final Resource resource) {
        log.info("putMoi: {}", resource);
        final ResourceOneOf stubResource = new ResourceOneOf("Id set by Stub");
        stubResource.setObjectClass("ObjectClass set by Stub");
        stubResource.setObjectInstance("ObjectInstance set by Stub");
        stubResource.setAttributes("Attribute set by Stub");
        final Optional<ResponseEntity<Object>> optionalResponseEntity = controllerSimulation.simulate(
            httpServletRequest, provMnSWriteDelayMs);
        return optionalResponseEntity.orElseGet(() -> new ResponseEntity<>(stubResource, HttpStatus.OK));
    }

    /**
     * Reads one or multiple resources.
     *
     * @param httpServletRequest      URI request including path
     * @param scope                   Extends the set of targeted resources beyond the base
     *                                resource identified with the authority and path component of
     *                                the URI.
     * @param filter                  Reduces the targeted set of resources by applying a filter to
     *                                the scoped set of resource representations. Only resources
     *                                representations for which the filter construct evaluates to
     *                                "true" are targeted.
     * @param attributes              Attributes of the scoped resources to be returned. The
     *                                value is a comma-separated list of attribute names.
     * @param fields                  Attribute fields of the scoped resources to be returned. The
     *                                value is a comma-separated list of JSON pointers to the
     *                                attribute fields.
     * @param dataNodeSelector        dataNodeSelector object
     * @return {@code ResponseEntity} The resources identified in the request for retrieval are returned
     *                                in the response message body.
     */
    @Override
    public ResponseEntity<Object> getMoi(final HttpServletRequest httpServletRequest, final Scope scope,
                                           final String filter, final List<String> attributes,
                                           final List<String> fields,
                                           final ClassNameIdGetDataNodeSelectorParameter dataNodeSelector) {
        log.info("getMoi: scope: {}, filter: {}, attributes: {}, fields: {}, dataNodeSelector: {}",
                scope, filter, attributes, fields, dataNodeSelector);
        final Optional<ResponseEntity<Object>> optionalResponseEntity = controllerSimulation.simulate(
            httpServletRequest, provMnSReadDelayMs);
        final String sampleFiveKbJson = ResourceFileReaderUtil.getResourceFileContent(applicationContext.getResource(
            ResourceLoader.CLASSPATH_URL_PREFIX + "data/ietf-network-topology-sample-rfc8345-large.json"));
        dummyResource.setAttributes(sampleFiveKbJson);
        return optionalResponseEntity.orElseGet(() -> new ResponseEntity<>(dummyResource, HttpStatus.OK));
    }

    /**
     * Patches (Create, Update or Delete) one or multiple resources.
     *
     * @param httpServletRequest      URI request including path
     * @param patchItems              A list of items to be created, updated or replaced
     * @return {@code ResponseEntity} The updated resource representations are returned in the response message body.
     */
    @Override
    public ResponseEntity<Object> patchMoi(final HttpServletRequest httpServletRequest,
                                           final List<PatchItem> patchItems) {
        log.info("patchMoi: {}", patchItems);
        final String sampleFiveKbJson = ResourceFileReaderUtil.getResourceFileContent(applicationContext.getResource(
            ResourceLoader.CLASSPATH_URL_PREFIX + "data/ietf-network-topology-sample-rfc8345-large.json"));
        final PatchItem addOperationPatchItem = new PatchItem(PatchOperation.ADD, "/path=setByStub");
        addOperationPatchItem.setValue(sampleFiveKbJson);
        final List<PatchItem> stubResponse = new ArrayList<>();
        stubResponse.add(addOperationPatchItem);
        stubResponse.add(new PatchItem(PatchOperation.REMOVE, "/path=alsoSetByStub"));
        final Optional<ResponseEntity<Object>> optionalResponseEntity = controllerSimulation.simulate(
            httpServletRequest, provMnSWriteDelayMs);
        return optionalResponseEntity.orElseGet(() -> new ResponseEntity<>(stubResponse, HttpStatus.OK));
    }

    /**
     * Delete one or multiple resources.
     *
     * @param httpServletRequest      URI request including path
     * @return {@code ResponseEntity} The response body is empty, HTTP status returned.
     */
    @Override
    public ResponseEntity<Object> deleteMoi(final HttpServletRequest httpServletRequest) {
        log.info("deleteMoi:");
        final Optional<ResponseEntity<Object>> optionalResponseEntity = controllerSimulation.simulate(
            httpServletRequest, provMnSWriteDelayMs);
        return optionalResponseEntity.orElseGet(() -> new ResponseEntity<>(HttpStatus.OK));
    }
}
