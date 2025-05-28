/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2025 OpenInfra Foundation Europe. All rights reserved.
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

package org.onap.cps.ncmp.dmi.rest.stub.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class DmiRestStubExceptionHandler extends ResponseEntityExceptionHandler {

    // Handles validation errors on @RequestBody and @Valid
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull final MethodArgumentNotValidException
                                                                          methodArgumentNotValidException,
                                                                  @NonNull final HttpHeaders httpHeaders,
                                                                  @NonNull final HttpStatusCode httpStatusCode,
                                                                  @NonNull final WebRequest webRequest) {
        final Map<String, String> errors = new HashMap<>();
        methodArgumentNotValidException.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        log.warn("Validation failed: {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // Handles missing @RequestParam
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            @NonNull final MissingServletRequestParameterException missingServletRequestParameterException,
            @NonNull final HttpHeaders httpHeaders,
            @NonNull final HttpStatusCode httpStatusCode,
            @NonNull final WebRequest webRequest) {
        final String error = "Missing required query parameter: "
                + missingServletRequestParameterException.getParameterName();
        log.warn(error);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all uncaught exceptions in the application.
     * This method acts as a global fallback exception handler using Spring's
     * {@link ExceptionHandler} mechanism. It captures any exceptions that are not
     * explicitly handled by other more specific exception handlers.
     *
     * @param exception  the exception that was thrown
     * @param webRequest the current web request during which the exception occurred
     * @return a {@link ResponseEntity} containing a generic error message and an
     *     {@link HttpStatus#INTERNAL_SERVER_ERROR} status code
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(final Exception exception, final WebRequest webRequest) {
        log.error("Unexpected server error: {}", exception.getLocalizedMessage());
        return new ResponseEntity<>("Internal server error. Please contact support." + webRequest.getContextPath(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}