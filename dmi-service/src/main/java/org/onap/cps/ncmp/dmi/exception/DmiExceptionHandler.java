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

import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.model.ErrorMessage;
import org.onap.cps.ncmp.dmi.rest.controller.DmiRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(assignableTypes = {DmiRestController.class})
public class DmiExceptionHandler {

    private DmiExceptionHandler() {
    }

    /**
     * Default exception handler.
     *
     * @param exception the exception to handle
     * @return response with response code 500.
     */
    @ExceptionHandler
    public static ResponseEntity<Object> handleInternalServerErrorExceptions(final Exception exception) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    @ExceptionHandler({ModulesNotFoundException.class, ModuleResourceNotFoundException.class})
    public static ResponseEntity<Object> handleNotFoundExceptions(final DmiException exception) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, exception);
    }

    @ExceptionHandler({CmHandleRegistrationException.class, DmiException.class})
    public static ResponseEntity<Object> handleAnyOtherDmiExceptions(final DmiException exception) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception);
    }

    private static ResponseEntity<Object> buildErrorResponse(final HttpStatus httpStatus, final Exception exception) {
        logForNonDmiException(exception);
        final var errorMessage = new ErrorMessage();
        errorMessage.setStatus(httpStatus.toString());
        errorMessage.setMessage(exception.getMessage());
        errorMessage.setDetails(exception instanceof DmiException ? ((DmiException) exception).getDetails() :
                "Check logs for details.");
        return new ResponseEntity<>(errorMessage, httpStatus);
    }

    private static void logForNonDmiException(final Exception exception) {
        if (exception.getCause() != null || !(exception instanceof DmiException)) {
            log.error("Exception occurred", exception);
        }
    }
}