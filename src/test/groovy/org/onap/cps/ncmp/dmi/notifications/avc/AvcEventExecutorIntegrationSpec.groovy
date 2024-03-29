/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2023 Nordix Foundation
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.ncmp.dmi.notifications.avc

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.core.CloudEventUtils
import io.cloudevents.jackson.PojoCloudEventDataMapper
import org.onap.cps.ncmp.dmi.api.kafka.MessagingBaseSpec
import org.onap.cps.ncmp.events.avc1_0_0.AvcEvent
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.spock.Testcontainers

import java.time.Duration

@SpringBootTest(classes = [DmiDataAvcEventProducer])
@Testcontainers
@DirtiesContext
class AvcEventExecutorIntegrationSpec extends MessagingBaseSpec {

    @SpringBean
    DmiDataAvcEventProducer dmiDataAvcEventProducer = new DmiDataAvcEventProducer(cloudEventKafkaTemplate)

    def dmiService = new DmiDataAvcEventSimulationController(dmiDataAvcEventProducer)

    def objectMapper = new ObjectMapper()

    def 'Publish Avc Event'() {
        given: 'a simulated event'
            dmiService.simulateEvents(1)
        and: 'a consumer subscribed to dmi-cm-events topic'
            cloudEventKafkaConsumer.subscribe(['dmi-cm-events'])
        when: 'the next event record is consumed'
            def record = cloudEventKafkaConsumer.poll(Duration.ofMillis(1500)).iterator().next()
        then: 'record has correct topic'
            assert record.topic == 'dmi-cm-events'
        and: 'the record value can be mapped to an avcEvent'
            def dmiDataAvcEvent = record.value()
            def convertedAvcEvent = CloudEventUtils.mapData(dmiDataAvcEvent, PojoCloudEventDataMapper.from(objectMapper, AvcEvent.class)).getValue()
            assert convertedAvcEvent != null
    }
}