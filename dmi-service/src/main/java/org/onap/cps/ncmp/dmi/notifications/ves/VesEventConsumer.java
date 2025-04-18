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

package org.onap.cps.ncmp.dmi.notifications.ves;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.exception.CmHandleRegistrationException;
import org.onap.cps.ncmp.dmi.service.DmiService;
import org.onap.cps.ncmp.events.ves30_2_1.VesEventSchema;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VesEventConsumer {

    private final DmiService dmiService;

    /**
     * Consume the VES event to discover the devices that are registered with SDNC.
     *
     * @param vesEventSchema Vnf Event Stream
     */
    @KafkaListener(
            topics = {"unauthenticated.VES_PNFREG_OUTPUT", "unauthenticated.VES_O1_NOTIFY_PNF_REGISTRATION_OUTPUT"},
            containerFactory = "legacyEventConcurrentKafkaListenerContainerFactory",
            properties = {"spring.json.value.default.type=org.onap.cps.ncmp.events.ves30_2_1.VesEventSchema"})
    public void consumeVesEvent(final VesEventSchema vesEventSchema) {

        final String sourceName = vesEventSchema.getEvent().getCommonEventHeader().getSourceName();
        log.info("Source name / CmHandleId from the VES event is : {}", sourceName);
        try {
            dmiService.registerCmHandles(List.of(sourceName));
        } catch (final CmHandleRegistrationException cmHandleRegistrationException) {
            log.warn("Exception occured while registering the device : {} , cause : {}", sourceName,
                    cmHandleRegistrationException.getMessage());
        } catch (final Exception exception) {
            log.warn("Exception occured with cause : {}", exception.getMessage());
        }
    }

}
