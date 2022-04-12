/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2022 Nordix Foundation
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.ncmp.dmi.notifications;

import lombok.extern.slf4j.Slf4j;
import org.onap.cps.event.model.CpsAsyncRequestResponseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
@Slf4j
public class CpsAsyncRequestResponseEventProducer {

    private final KafkaTemplate<String, CpsAsyncRequestResponseEvent> kafkaTemplate;
    private final String topicName;

    /**
     * KafkaTemplate and Topic name.
     *
     * @param kafkaTemplate kafka template
     * @param topicName     topic name
     */
    @Autowired
    public CpsAsyncRequestResponseEventProducer(
        final KafkaTemplate<String, CpsAsyncRequestResponseEvent> kafkaTemplate,
        final @Value("${app.ncmp.async-m2m.topic}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    /**
     * Sends message to the configured topic with a message key.
     *
     * @param messageKey message key
     * @param payload    message payload
     */
    public void sendMessage(final String messageKey, final CpsAsyncRequestResponseEvent payload) {
        final ListenableFuture<SendResult<String, CpsAsyncRequestResponseEvent>> resultListenableFuture =
            kafkaTemplate.send(topicName, messageKey, payload);

        resultListenableFuture.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(final Throwable ex) {
                log.warn("Failed to resultListenableFuture the messages {}", ex.getMessage()
                );
            }

            @Override
            public void onSuccess(final SendResult<String, CpsAsyncRequestResponseEvent> result) {
                log.debug("Sent message {}", result.getProducerRecord());
            }
        });
    }
}
