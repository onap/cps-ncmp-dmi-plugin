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

package org.onap.cps.ncmp.dmi.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.exception.DmiException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AsyncTaskExecutor {

    /**
     * Execute task asynchronously and publish response to supplied topic.
     *
     * @param taskSupplier functional method is get() task need to executed asynchronously
     * @param topicName    topic name where message need to be published.
     * @param requestId    unique requestId for async request
     */
    public void executeTaskAsynchronously(final Supplier<String> taskSupplier,
                                          final String topicName, final String requestId) {
        CompletableFuture.supplyAsync(() -> taskSupplier.get())
                .orTimeout(2, TimeUnit.SECONDS)
                .whenCompleteAsync((resourceDataAsJson, error) -> {
                    if (error == null) {
                        log.info(resourceDataAsJson);
                    } else {
                        final String errorMessage = error.getMessage();
                        if (error instanceof TimeoutException) {
                            log.error(errorMessage);
                            throw new DmiException("Request Timeout Error.", errorMessage, error);
                        }
                        log.error(errorMessage);
                        throw new DmiException("Internal Server Error.", errorMessage, error);
                    }
                });
    }
}
