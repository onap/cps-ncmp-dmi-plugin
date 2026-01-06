/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2025 OpenInfra Foundation Europe
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

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.provmns.api.ProvMnS;
import org.onap.cps.ncmp.dmi.provmns.model.ClassNameIdGetDataNodeSelectorParameter;
import org.onap.cps.ncmp.dmi.provmns.model.ErrorResponseDefault;
import org.onap.cps.ncmp.dmi.provmns.model.PatchItem;
import org.onap.cps.ncmp.dmi.provmns.model.Resource;
import org.onap.cps.ncmp.dmi.provmns.model.ResourceOneOf;
import org.onap.cps.ncmp.dmi.provmns.model.Scope;
import org.onap.cps.ncmp.dmi.rest.stub.utils.Sleeper;
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

    static final Pattern PATTERN_SIMULATION = Pattern.compile("dmiSimulation=(\\w+_\\d{1,3})");
    static final Pattern PATTERN_HTTP_ERROR = Pattern.compile("httpError_(\\d{3})");
    static final Pattern PATTERN_SLOW_RESPONSE = Pattern.compile("slowResponse_(\\d{1,3})");

    private final Sleeper sleeper;
    private final ObjectMapper objectMapper;

    static {
        dummyResource.setObjectClass("dummyClass");
        dummyResource.setObjectInstance("dummyInstance");
        dummyResource.setAttributes(Collections.singletonMap("dummyAttribute", "dummy value"));
    }

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
        final Optional<ResponseEntity<Object>> optionalResponseEntity = simulate(httpServletRequest);
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
        final Optional<ResponseEntity<Object>> optionalResponseEntity = simulate(httpServletRequest);
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
        final Optional<ResponseEntity<Object>> optionalResponseEntity = simulate(httpServletRequest);
        return optionalResponseEntity.orElseGet(() -> new ResponseEntity<>(patchItems, HttpStatus.OK));
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
        final Optional<ResponseEntity<Object>> optionalResponseEntity = simulate(httpServletRequest);
        return optionalResponseEntity.orElseGet(() -> new ResponseEntity<>(HttpStatus.OK));
    }

    private Optional<ResponseEntity<Object>> simulate(final HttpServletRequest httpServletRequest) {
        Matcher matcher = PATTERN_SIMULATION.matcher(httpServletRequest.getRequestURI());
        if (matcher.find()) {
            final String simulation = matcher.group(1);
            matcher = PATTERN_SLOW_RESPONSE.matcher(simulation);
            if (matcher.matches()) {
                haveALittleRest(Integer.parseInt(matcher.group(1)));
            }
            matcher = PATTERN_HTTP_ERROR.matcher(simulation);
            if (matcher.matches()) {
                return Optional.of(createErrorRsponseEntity(Integer.parseInt(matcher.group(1))));
            }
        }
        return Optional.empty();
    }

    private void haveALittleRest(final int durationInSeconds) {
        log.warn("Stub is mocking slow response; delay {} seconds", durationInSeconds);
        try {
            sleeper.haveALittleRest(durationInSeconds);
        } catch (final InterruptedException e) {
            log.trace("Sleep interrupted, re-interrupting the thread");
            Thread.currentThread().interrupt();
        }
    }

    private static ResponseEntity<Object> createErrorRsponseEntity(final int errorCode) {
        log.warn("Stub is mocking an error response, code: {}", errorCode);
        final ErrorResponseDefault errorResponseDefault = new ErrorResponseDefault("ERROR_FROM_STUB");
        errorResponseDefault.setTitle("Title set by Stub");
        errorResponseDefault.setStatus(String.valueOf(errorCode));
        return new ResponseEntity<>(errorResponseDefault, HttpStatus.valueOf(errorCode));
    }

}
