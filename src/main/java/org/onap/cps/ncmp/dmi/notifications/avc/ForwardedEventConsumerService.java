/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation
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

package org.onap.cps.ncmp.dmi.notifications.avc;

import java.time.Duration;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.onap.cps.ncmp.event.model.ForwardedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * ForwardedEventConsumerService to consume from the response topic.
 */

@Slf4j
@Service
@RequiredArgsConstructor
@EnableScheduling
public class ForwardedEventConsumerService {
    private final KafkaConsumer<String, ForwardedEvent> consumer;

    /**
     * Consume the ForwardedEvent from the topic and wait for two seconds
     * unless it wakes up.
     * fixedDelay is to set scheduler for next consuming after one second.
     *
     */
    @Scheduled(fixedDelay = 1000)
    public void consume() {
        try {
            consumer.poll(Duration.ofMillis(2000));
        } catch (final RuntimeException exception) {
            log.error("Unable to consume due to {}", exception.getMessage());
        } finally {
            consumer.close();
        }
    }

    /**
     * Subscribes to topic, and gets ready to poll.
     *
     * @param topicName to subscribe
     */
    public void subscribe(final String topicName) {
        consumer.subscribe(Arrays.asList(topicName));
    }
}
