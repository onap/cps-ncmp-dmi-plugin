/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation
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

package org.onap.cps.ncmp.dmi.notifications.async;

import com.google.gson.JsonObject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.exception.DmiException;
import org.onap.cps.ncmp.dmi.exception.HttpClientRequestException;
import org.onap.cps.ncmp.dmi.model.DataAccessRequest;
import org.onap.cps.ncmp.event.model.DmiAsyncRequestResponseEvent;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncTaskExecutor {

    private final DmiAsyncRequestResponseEventProducerService dmiAsyncRequestResponseEventProducerService;

    private static final DmiAsyncRequestResponseEventCreator dmiAsyncRequestResponseEventUtil =
        new DmiAsyncRequestResponseEventCreator();

    /**
     * Execute task asynchronously and publish response to supplied topic.
     *
     * @param taskSupplier          functional method is get() task need to executed asynchronously
     * @param topicName             topic name where message need to be published
     * @param requestId             unique requestId for async request
     * @param operation             the operation performed
     * @param timeOutInMilliSeconds task timeout in milliseconds
     */
    public void executeAsyncTask(final Supplier<String> taskSupplier,
                                 final String topicName,
                                 final String requestId,
                                 final DataAccessRequest.OperationEnum operation,
                                 final int timeOutInMilliSeconds) {
        CompletableFuture.supplyAsync(taskSupplier::get)
            .orTimeout(timeOutInMilliSeconds, TimeUnit.SECONDS)
            .whenCompleteAsync((resourceDataAsJson, throwable) -> {
                if (throwable == null) {
                    switch (operation) {
                        case CREATE:
                            publishAsyncEvent(topicName, requestId, resourceDataAsJson, "CREATED", "201");
                            break;
                        default:
                            publishAsyncEvent(topicName, requestId, resourceDataAsJson, "SUCCESS", "200");
                            break;
                    }
                } else {
                    log.error("Error occurred with async request {}", throwable.getMessage());
                    publishAsyncFailureEvent(topicName, requestId, throwable);
                }
            });
        log.info("Async task completed successfully.");
    }

    private void publishAsyncEvent(final String topicName,
                                   final String requestId,
                                   final String resourceDataAsJson,
                                   final String status,
                                   final String code) {
        final DmiAsyncRequestResponseEvent cpsAsyncRequestResponseEvent = dmiAsyncRequestResponseEventUtil.createEvent(
            resourceDataAsJson, topicName, requestId, status, code);
        dmiAsyncRequestResponseEventProducerService.publishAsyncEvent(requestId, cpsAsyncRequestResponseEvent);
    }

    protected void publishAsyncFailureEvent(final String topicName,
                                            final String requestId,
                                            final Throwable throwable) {

        final String status;
        final String code;
        if (throwable instanceof HttpClientRequestException) {
            HttpClientRequestException httpClientRequestException = (HttpClientRequestException) throwable;
            status = httpClientRequestException.getHttpStatus().getReasonPhrase();
            code = String.valueOf(httpClientRequestException.getHttpStatus().value());
        } else {
            status = "";
            code = "";
        }

        final JsonObject errorDetails = new JsonObject();
        errorDetails.addProperty("errorDetails", throwable.getMessage());
        publishAsyncEvent(topicName, requestId, errorDetails.toString(), status, code);

        final String message = throwable instanceof TimeoutException
            ? "Request Timeout Error." : "Internal Server Error.";
        throw new DmiException(message, throwable.getMessage(), throwable);
    }
}



