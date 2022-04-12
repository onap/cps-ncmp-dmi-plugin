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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.event.model.DmiAsyncRequestResponseEvent;
import org.onap.cps.ncmp.dmi.exception.DmiException;
import org.onap.cps.ncmp.dmi.model.DataAccessRequest;
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
     * @param taskSupplier functional method is get() task need to executed asynchronously
     * @param topicName    topic name where message need to be published
     * @param requestId    unique requestId for async request
     * @param timeOutInMilliSeconds task timeout in milliseconds
     * @param operation the operation performed
     */
    public void executeAsyncTask(final Supplier<String> taskSupplier,
                                 final String topicName,
                                 final String requestId,
                                 final int timeOutInMilliSeconds,
                                 final DataAccessRequest.OperationEnum operation) {
        CompletableFuture.supplyAsync(taskSupplier::get)
            .orTimeout(timeOutInMilliSeconds, TimeUnit.SECONDS)
            .whenCompleteAsync((resourceDataAsJson, error) -> {
                if (error == null) {
                    switch (operation) {
                        case CREATE:
                            publishAsyncEvent(topicName, requestId, resourceDataAsJson, "CREATED", "201");
                            break;
                        default:
                            publishAsyncEvent(topicName, requestId, resourceDataAsJson, "SUCCESS", "200");
                            break;
                    }
                } else {
                    log.error("Error occurred with async request {}", error.getMessage());
                    publishAsyncFailureEvent(topicName, requestId, error);
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
        final Pattern responseStatusPattern = Pattern.compile("(\\d{3})(\\W\\b[A-Z].*?\\b)+");
        final Matcher responseStatusMatcher = responseStatusPattern.matcher(throwable.getMessage());

        String status = null;
        String code = null;

        if (responseStatusMatcher.find()) {
            status = responseStatusMatcher.group(0).substring(4);
            code = responseStatusMatcher.group(1);
        }

        final JsonObject errorDetails = new JsonObject();
        errorDetails.addProperty("errorDetails", throwable.getMessage());
        publishAsyncEvent(topicName, requestId, errorDetails.toString(), status, code);

        final String message = throwable instanceof TimeoutException
            ? "Request Timeout Error." : "Internal Server Error.";
        throw new DmiException(message, throwable.getMessage(), throwable);
    }
}



