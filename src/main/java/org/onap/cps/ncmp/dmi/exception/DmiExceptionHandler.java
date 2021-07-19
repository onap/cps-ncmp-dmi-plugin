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

package org.onap.cps.ncmp.dmi.exception;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.WebUtils;

@ControllerAdvice
@Slf4j
public class DmiExceptionHandler {

    /**
     * Provides handling for exceptions throughout this service.
     *
     * @param ex The target exception
     * @param request The current request
     */
    @ExceptionHandler({
            DmiException.class,
            ModulesNotFoundException.class,
            RuntimeException.class
    })
    @Nullable
    public final ResponseEntity<ApiError> handleException(final Exception ex, final WebRequest request) {
        final HttpHeaders headers = new HttpHeaders();

        log.error("Handling {} due to {}", ex.getClass().getSimpleName(), ex.getMessage());

        if (ex instanceof DmiException) {
            final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            final DmiException dmiEx = (DmiException) ex;

            return handleDmiException(dmiEx, headers, status, request);
        } else if (ex instanceof ModulesNotFoundException) {
            final HttpStatus status = HttpStatus.NOT_FOUND;
            final ModulesNotFoundException mnfe = (ModulesNotFoundException) ex;

            return handleModulesNotFoundException(mnfe, headers, status, request);
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Unknown exception type: {}", ex.getClass().getName());
            }

            final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return handleExceptionInternal(ex, null, headers, status, request);
        }
    }

    /**
     * Customize the response for DmiException.
     *
     * @param ex The exception
     * @param headers The headers to be written to the response
     * @param status The selected response status
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<ApiError> handleDmiException(final DmiException ex, final HttpHeaders headers,
                                                          final HttpStatus status, final WebRequest request) {
        final List<String> errors = Collections.singletonList(ex.getMessage() + " " + ex.getDetails());
        return handleExceptionInternal(ex, new ApiError(errors), headers, status, request);
    }

    /**
     * Customize the response for ModulesNotFoundException.
     *
     * @param ex The exception
     * @param headers The headers to be written to the response
     * @param status The selected response status
     * @return a {@code ResponseEntity} instance
     */
    protected ResponseEntity<ApiError> handleModulesNotFoundException(final ModulesNotFoundException ex,
                                                                      final HttpHeaders headers,
                                                                      final HttpStatus status,
                                                                      final WebRequest request) {
        final List<String> errors = Collections.singletonList(ex.getMessage());
        return handleExceptionInternal(ex, new ApiError(errors), headers, status, request);
    }

    /**
     * A single place to customize the response body of all Exception types.
     *
     * <p>The default implementation sets the {@link WebUtils#ERROR_EXCEPTION_ATTRIBUTE}
     * request attribute and creates a {@link ResponseEntity} from the given
     * body, headers, and status.
     *
     * @param ex The exception
     * @param body The body for the response
     * @param headers The headers for the response
     * @param status The response status
     * @param request The current request
     */
    protected ResponseEntity<ApiError> handleExceptionInternal(final Exception ex, @Nullable final ApiError body,
                                                               final HttpHeaders headers, final HttpStatus status,
                                                               final WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }

        return new ResponseEntity<>(body, headers, status);
    }
}
