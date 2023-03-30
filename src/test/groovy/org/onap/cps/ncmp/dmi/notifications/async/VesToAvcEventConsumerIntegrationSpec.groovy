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

package org.onap.cps.ncmp.dmi.notifications.async

import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.cps.ncmp.dmi.api.kafka.MessagingBaseSpec
import org.onap.cps.ncmp.event.model.AvcEvent
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.testcontainers.spock.Testcontainers
import org.onap.cps.ncmp.dmi.TestUtils

import java.time.Duration

@SpringBootTest(classes = [DmiAsyncRequestResponseEventConsumer])
@Testcontainers
@DirtiesContext
class VesToAvcEventConsumerIntegrationSpec extends MessagingBaseSpec {

    @SpringBean
    DmiAsyncRequestResponseEventConsumer dmiAsyncRequestResponseEventConsumer =
        new DmiAsyncRequestResponseEventConsumer(kafkaTemplate)

    def spiedObjectMapper = Spy(ObjectMapper)

    def objectUnderTest = dmiAsyncRequestResponseEventConsumer;

    private static final String TEST_TOPIC = 'unauthenticated.SEC_3GPP_PROVISIONING_OUTPUT'

    def setup() {
        consumer.subscribe([TEST_TOPIC] as List<String>)
    }

    def cleanup() {
        consumer.close()
    }

    def 'Publish and Subscribe message - success'() {
        when: 'a successful event is published'
            def sampleJsonData = TestUtils.getResourceFileContent('STDEvent.json')
            objectUnderTest.consumeFromTopic(sampleJsonData)
        and: 'the topic is polled'
            def records = consumer.poll(Duration.ofMillis(1500))
        then: 'the record received is the event sent'
            def record = records.iterator().next()
            AvcEvent event = spiedObjectMapper.readValue(record.value(), AvcEvent)
        and: 'the status & code matches expected'
            assert event.getEventSource() == 'ncmp-datastore:passthrough-operational'
            assert event.getEventSchemaVersion() == '1.0'
    }


}