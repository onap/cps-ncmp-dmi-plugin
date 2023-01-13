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

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.event.model.AvcEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("${rest.api.dmi-base-path}")
@RestController
@Slf4j
@RequiredArgsConstructor
public class DmiDataAvcEventSimulationController {

    private final DmiDataAvcEventProducer dmiDataAvcEventProducer;

    /**
     * Simulate Event for AVC.
     * @param numberOfSimulatedEvents number of events to be generated
     * @return ResponseEntity
     */
    @GetMapping(path = "/v1/simulateDmiDataEvent")
    public ResponseEntity<Void> simulateEvents(@RequestParam("numberOfSimulatedEvents")
                                                   final Integer numberOfSimulatedEvents) {
        final DmiDataAvcEventCreator dmiDataAvcEventCreator = new DmiDataAvcEventCreator();

        for (int i = 0; i < numberOfSimulatedEvents; i++) {
            final String eventCorrelationId = UUID.randomUUID().toString();
            final AvcEvent avcEvent = dmiDataAvcEventCreator.createEvent(eventCorrelationId);
            dmiDataAvcEventProducer.sendMessage(eventCorrelationId, avcEvent);
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
