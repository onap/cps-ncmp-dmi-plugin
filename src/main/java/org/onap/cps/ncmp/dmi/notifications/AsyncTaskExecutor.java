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

package org.onap.cps.ncmp.dmi.notifications;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.event.model.CpsAsyncRequestResponseEvent;
import org.onap.cps.ncmp.dmi.exception.DmiException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncTaskExecutor {

    private final CpsAsyncRequestResponseEventProducerService cpsAsyncRequestResponseEventProducerService;

    private static final CpsAsyncRequestResponseEventUtil cpsAsyncRequestResponseEventUtil =
        new CpsAsyncRequestResponseEventUtil();

    /**
     * Execute task asynchronously and publish response to supplied topic.
     *
     * @param taskSupplier functional method is get() task need to executed asynchronously
     * @param topicName    topic name where message need to be published.
     * @param requestId    unique requestId for async request
     */
    public void executeTaskAsynchronously(final Supplier<String> taskSupplier,
                                          final String topicName,
                                          final String requestId) {
        CompletableFuture.supplyAsync(taskSupplier::get)
            .orTimeout(2, TimeUnit.SECONDS)
            .whenCompleteAsync((resourceDataAsJson, error) -> {
                if (error == null) {
                    publishEvent(topicName, requestId, resourceDataAsJson, "SUCCESS", "200");
                } else {
                    log.error("Error occurred with async request {}", error.getMessage());
                    publishFailureEvent(topicName, requestId, error);
                }
            });
    }

    protected void publishFailureEvent(final String topicName,
                                       final String requestId,
                                       final Throwable error) {
        publishEvent(topicName, requestId, "{}", "FAILURE", "424");

        if (error instanceof TimeoutException) {
            throw new DmiException("Request Timeout Error.", error.getMessage(), error);
        }
        throw new DmiException("Internal Server Error.", error.getMessage(), error);
    }

    private void publishEvent(final String topicName,
                              final String requestId,
                              final String resourceDataAsJson,
                              final String status,
                              final String code) {
        final CpsAsyncRequestResponseEvent cpsAsyncRequestResponseEvent = cpsAsyncRequestResponseEventUtil.createEvent(
            resourceDataAsJson, topicName, requestId, status, code);
        cpsAsyncRequestResponseEventProducerService.publishToNcmp(requestId, cpsAsyncRequestResponseEvent);
    }
}



