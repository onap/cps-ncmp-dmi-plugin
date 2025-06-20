/*
 * ============LICENSE_START========================================================
 *  Copyright (c) 2025 OpenInfra Foundation Europe. All rights reserved.
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

package org.onap.cps.ncmp.dmi.cmstack.lcm;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.service.DmiService;
import org.onap.cps.ncmp.events.lcm.v1.LcmEvent;
import org.onap.cps.ncmp.events.lcm.v1.Values;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class LcmEventConsumer {

    private final DmiService dmiService;

    /**
     * Consume the LCM event to enable the data synchronization for the ready cm handle.
     *
     * @param lcmEvent Lifecycle event of a cm handle id
     */
    @KafkaListener(topics = "${app.ncmp.lcm.topic}",
            containerFactory = "legacyEventConcurrentKafkaListenerContainerFactory",
            properties = {"spring.json.value.default.type=org.onap.cps.ncmp.events.lcm.v1.LcmEvent"})
    public void consumeLcmEvent(final LcmEvent lcmEvent) {

        final Values newValues = lcmEvent.getEvent().getNewValues();

        if (newValues == null) {
            return;
        }

        if (newValues.getCmHandleState() == Values.CmHandleState.READY) {
            final String cmHandleId = lcmEvent.getEvent().getCmHandleId();
            log.info("Enabling data sync flag for cmHandleId : {}", cmHandleId);
            dmiService.enableNcmpDataSyncForCmHandles(List.of(cmHandleId));
        }
    }

}