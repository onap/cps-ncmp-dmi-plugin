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
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.onap.cps.ncmp.dmi.api.kafka.MessagingBaseSpec
import org.onap.cps.ncmp.dmi.notifications.async.AsyncTaskExecutor
import org.onap.cps.ncmp.dmi.rest.controller.DmiRestController
import org.onap.cps.ncmp.dmi.service.DmiService
import org.onap.cps.ncmp.event.model.AvcEvent
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
    def dmiDataAvcEventProducer = new DmiDataAvcEventProducer(kafkaTemplate)

    def spiedObjectMapper = Spy(ObjectMapper)

    def dmiService =  new DmiRestController(
            Mock(DmiService), spiedObjectMapper,
            Mock(AsyncTaskExecutor), dmiDataAvcEventProducer)

    KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(consumerConfigProperties('test'))

    def setup() {
        consumer.subscribe(['dmi-cm-events'] as List<String>)
    }

    def cleanup() {
        consumer.close()
    }

    def 'Publish Avc Event'() {
        when: 'simulate events is invoked'
            dmiService.simulateEvents(1)
        and: 'the next event from the dmi-cm-events is topic'
            def records = consumer.poll(Duration.ofMillis(1500)).iterator().next()
        and: 'record is converted into AvcEvent'
            AvcEvent avcEvent  = spiedObjectMapper.readValue(record.value(), AvcEvent)
        then: 'the target is NCMP'
            assert avcEvent.getEventTarget() == 'NCMP'
    }
}