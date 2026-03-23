/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2026 OpenInfra Foundation Europe
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

package org.onap.cps.ncmp.dmi.rest.stub.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.provmns.model.ErrorResponseDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ControllerSimulation {

    private final Sleeper sleeper;

    static final Pattern PATTERN_SIMULATION = Pattern.compile("dmiSimulation=(\\w+_\\d{1,3})");
    static final Pattern PATTERN_HTTP_ERROR = Pattern.compile("httpError_(\\d{3})");
    static final Pattern PATTERN_SLOW_RESPONSE = Pattern.compile("slowResponse_(\\d{1,3})");

    /**
     * Constructs a ControllerSimulation with the given sleeper.
     *
     * @param sleeper the sleeper used to introduce delays
     */
    public ControllerSimulation(final Sleeper sleeper) {
        this.sleeper = sleeper;
    }

    /**
     * Simulates a DMI response based on the request URI. If the URI contains a simulation directive,
     * it will either delay the response or return an error. Otherwise, the default delay is applied.
     *
     * @param httpServletRequest the incoming HTTP request to inspect for simulation directives
     * @param defaultDelay      the default delay in milliseconds when no simulation is matched
     * @return an optional error response entity if an error simulation is triggered, or empty otherwise
     */
    public Optional<ResponseEntity<Object>> simulate(final HttpServletRequest httpServletRequest,
                                                      final long defaultDelay) {
        Matcher matcher = PATTERN_SIMULATION.matcher(httpServletRequest.getRequestURI());
        if (matcher.find()) {
            final String simulation = matcher.group(1);
            matcher = PATTERN_SLOW_RESPONSE.matcher(simulation);
            if (matcher.matches()) {
                haveALittleRest(Integer.parseInt(matcher.group(1)));
            }
            matcher = PATTERN_HTTP_ERROR.matcher(simulation);
            if (matcher.matches()) {
                return Optional.of(createErrorResponseEntity(Integer.parseInt(matcher.group(1))));
            }
        } else {
            sleeper.delay(defaultDelay);
        }
        return Optional.empty();
    }

    /**
     * Pauses execution for the specified duration to simulate a slow response.
     *
     * @param durationInSeconds the duration to sleep in seconds
     */
    public void haveALittleRest(final int durationInSeconds) {
        log.warn("Stub is mocking slow response; delay {} seconds", durationInSeconds);
        try {
            sleeper.haveALittleRest(durationInSeconds);
        } catch (final InterruptedException e) {
            log.trace("Sleep interrupted, re-interrupting the thread");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Creates an error response entity with the given HTTP error code.
     *
     * @param errorCode the HTTP status code for the error response
     * @return a response entity containing an error body and the corresponding HTTP status
     */
    public static ResponseEntity<Object> createErrorResponseEntity(final int errorCode) {
        log.warn("Stub is mocking an error response, code: {}", errorCode);
        final ErrorResponseDefault errorResponseDefault = new ErrorResponseDefault("ERROR_FROM_STUB");
        errorResponseDefault.setTitle("Title set by Stub");
        errorResponseDefault.setStatus(String.valueOf(errorCode));
        return new ResponseEntity<>(errorResponseDefault, HttpStatus.valueOf(errorCode));
    }
}
