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

package org.onap.cps.ncmp.dmi.notifications.async;

import lombok.RequiredArgsConstructor;
import org.onap.cps.ncmp.event.model.DmiAsyncRequestResponseEvent;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DmiAsyncRequestResponseEventProducerService {

    private final DmiAsyncRequestResponseEventProducer dmiAsyncRequestResponseEventProducer;

    /**
     * publish the message to event bus.
     * @param messageKey message key
     * @param message    message payload
     */
    public void publishAsyncEvent(final String messageKey, final DmiAsyncRequestResponseEvent message) {
        dmiAsyncRequestResponseEventProducer.sendMessage(messageKey, message);
    }
}
