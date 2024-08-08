/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2024 Nordix Foundation
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

package org.onap.cps.ncmp.dmi.rest.stub.controller.aop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Aspect to handle initial processing for methods annotated with @ModuleInitialProcess.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ModuleInitialProcessAspect {

    private final ObjectMapper objectMapper;
    private static final Map<String, Long> firstRequestTimePerModuleSetTag = new ConcurrentHashMap<>();
    private static final long INITIAL_PROCESSING_DELAY_MS = TimeUnit.MINUTES.toMillis(2);

    /**
     * Around advice to handle methods annotated with @ModuleInitialProcess.
     *
     * @param joinPoint            the join point representing the method execution
     * @param moduleInitialProcess the annotation containing the module set tag
     * @return the result of the method execution or a ResponseEntity indicating that the service is unavailable
     * @throws Throwable if the method execution fails
     */
    @Around("@annotation(moduleInitialProcess)")
    public Object handleModuleInitialProcess(ProceedingJoinPoint joinPoint, ModuleInitialProcess moduleInitialProcess) throws Throwable {
        log.debug("Aspect invoked for method: {}", joinPoint.getSignature());
        Object moduleRequest = joinPoint.getArgs()[1];
        String moduleSetTag = extractModuleSetTagFromRequest(moduleRequest);

        if (isModuleSetTagEmptyOrInvalid(moduleSetTag)) {
            log.debug("Received request with an empty or null moduleSetTag. Returning default processing.");
            return joinPoint.proceed();
        }

        long firstRequestTimestamp = getFirstRequestTimestamp(moduleSetTag);
        long currentTimestamp = System.currentTimeMillis();

        if (isInitialProcessingComplete(currentTimestamp, firstRequestTimestamp)) {
            log.debug("Initial processing for moduleSetTag '{}' is complete.", moduleSetTag);
            return joinPoint.proceed();
        }

        long remainingProcessingTime = calculateRemainingProcessingTime(currentTimestamp, firstRequestTimestamp);
        log.debug("Initial processing for moduleSetTag '{}' is still active. Returning HTTP 503. Remaining time: {} ms.", moduleSetTag, remainingProcessingTime);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    private String extractModuleSetTagFromRequest(Object moduleRequest) {
        JsonNode rootNode = objectMapper.valueToTree(moduleRequest);
        return rootNode.path("moduleSetTag").asText(null);
    }

    private boolean isModuleSetTagEmptyOrInvalid(String moduleSetTag) {
        return moduleSetTag == null || moduleSetTag.trim().isEmpty();
    }

    private long getFirstRequestTimestamp(String moduleSetTag) {
        return firstRequestTimePerModuleSetTag.computeIfAbsent(moduleSetTag, firstRequestTime -> System.currentTimeMillis());
    }

    private boolean isInitialProcessingComplete(long currentTimestamp, long firstRequestTimestamp) {
        return currentTimestamp - firstRequestTimestamp > INITIAL_PROCESSING_DELAY_MS;
    }

    private long calculateRemainingProcessingTime(long currentTimestamp, long firstRequestTimestamp) {
        return INITIAL_PROCESSING_DELAY_MS - (currentTimestamp - firstRequestTimestamp);
    }
}
