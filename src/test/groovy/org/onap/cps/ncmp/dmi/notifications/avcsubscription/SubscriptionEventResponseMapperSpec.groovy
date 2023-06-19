/*
 * ============LICENSE_START=======================================================
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

package org.onap.cps.ncmp.dmi.notifications.avcsubscription

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.core.builder.CloudEventBuilder
import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.exception.CloudEventConstructionException
import org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.Data
import org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.SubscriptionEventResponse
import org.onap.cps.ncmp.events.avcsubscription1_0_0.dmi_to_ncmp.SubscriptionStatus
import org.onap.cps.ncmp.events.avcsubscription1_0_0.ncmp_to_dmi.CmHandle
import org.onap.cps.ncmp.events.avcsubscription1_0_0.ncmp_to_dmi.SubscriptionEvent
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = [ObjectMapper])
class SubscriptionEventResponseMapperSpec extends Specification {

    @Autowired
    def objectMapper = new ObjectMapper()

    @SpringBean
    SubscriptionEventResponseMapper objectUnderTest = new SubscriptionEventResponseMapper()

    def 'Convert a SubscriptionResponseEvent to CloudEvent successfully.'() {
        given: 'a SubscriptionResponseEvent and an event key'
            def dmiName = 'test-ncmp-dmi'
            def responseStatus = SubscriptionStatus.Status.ACCEPTED
            def subscriptionStatuses = [new SubscriptionStatus(id: 'CmHandle1', status: responseStatus),
                                        new SubscriptionStatus(id: 'CmHandle2', status: responseStatus)]
            def subscriptionEventResponseData = new Data(subscriptionName: 'cm-subscription-001',
                    clientId: 'SCO-9989752', dmiName: 'ncmp-dmi-plugin', subscriptionStatus: subscriptionStatuses)
            def subscriptionEventResponse =
                    new SubscriptionEventResponse().withData(subscriptionEventResponseData)
        when: 'a SubscriptionResponseEvent is converted'
            def result = objectUnderTest.toCloudEvent(subscriptionEventResponse,"subscriptionCreated", dmiName)
        then: 'SubscriptionResponseEvent is converted as expected'
            def expectedCloudEvent = CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create('test-ncmp-dmi'))
                .withType("subscriptionCreated")
                .withDataSchema(URI.create("urn:cps:" + SubscriptionEventResponse.class.getName() + ":1.0.0"))
                .withExtension("correlationid", subscriptionEventResponse.getData().getClientId() + ":"
                        + subscriptionEventResponse.getData().getSubscriptionName())
                .withData(objectMapper.writeValueAsBytes(subscriptionEventResponse)).build()
            assert expectedCloudEvent.data == result.data
    }

    def 'Map the Cloud Event to data of the subscription event with incorrect content causes an exception'() {
        given: 'an empty subscription response event and event key'
            def dmiName = 'test-ncmp-dmi'
            def testSubscriptionEventResponse = new SubscriptionEventResponse()
        when: 'the subscription response event map to data of cloud event'
            objectUnderTest.toCloudEvent(testSubscriptionEventResponse, "subscriptionCreated", dmiName)
        then: 'a run time exception is thrown'
           thrown(CloudEventConstructionException)
    }

    def 'Convert a CloudEvent to SubscriptionEvent.'() {
        given: 'a CloudEvent'
            def eventKey = UUID.randomUUID().toString()
            def jsonData = TestUtils.getResourceFileContent('avcSubscriptionCreationEvent.json')
            def subscriptionEvent = objectMapper.readValue(jsonData, SubscriptionEvent.class)
            def cloudEvent = CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create('test-ncmp-dmi'))
                    .withType("subscriptionCreated")
                    .withDataSchema(URI.create("urn:cps:" + SubscriptionEvent.class.getName() + ":1.0.0"))
                    .withExtension("correlationid", eventKey)
                    .withData(objectMapper.writeValueAsBytes(subscriptionEvent)).build()
        when: 'a SubscriptionEvent is formed'
            def result = objectUnderTest.toSubscriptionEvent(cloudEvent)
        then: 'Confirm SubscriptionEvent was formed as expected'
            assert result == subscriptionEvent
    }

    def 'Convert a CloudEvent with Null data to SubscriptionEvent.'() {
        given: 'a CloudEvent with null data'
            def eventKey = UUID.randomUUID().toString()
            def cloudEvent = CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create('test-ncmp-dmi'))
                    .withType("subscriptionCreated")
                    .withDataSchema(URI.create("urn:cps:" + SubscriptionEvent.class.getName() + ":1.0.0"))
                    .withExtension("correlationid", eventKey)
                    .withData(null).build()
        when: 'a SubscriptionEvent is formed'
            def result = objectUnderTest.toSubscriptionEvent(cloudEvent)
        then: 'Confirm SubscriptionEventResponse was formed as expected'
            assert result == null
    }
}