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

package org.onap.cps.ncmp.dmi.notifications.cmsubscription

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.core.builder.CloudEventBuilder
import org.onap.cps.ncmp.dmi.exception.CloudEventConstructionException
import org.onap.cps.ncmp.events.cmsubscription1_0_0.dmi_to_ncmp.CmSubscriptionDmiOutEvent
import org.onap.cps.ncmp.events.cmsubscription1_0_0.dmi_to_ncmp.Data
import org.onap.cps.ncmp.events.cmsubscription1_0_0.dmi_to_ncmp.SubscriptionStatus
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest(classes = [ObjectMapper])
class CmSubscriptionDmiOutEventToCloudEventMapperSpec extends Specification {

    @Autowired
    def objectMapper = new ObjectMapper()

    @SpringBean
    CmSubscriptionDmiOutEventToCloudEventMapper objectUnderTest = new CmSubscriptionDmiOutEventToCloudEventMapper()

    def 'Convert a Cm Subscription DMI Out Event to CloudEvent successfully.'() {
        given: 'a Cm Subscription DMI Out Event and an event key'
            def dmiName = 'test-ncmp-dmi'
            def responseStatus = SubscriptionStatus.Status.ACCEPTED
            def subscriptionStatuses = [new SubscriptionStatus(id: 'CmHandle1', status: responseStatus),
                                        new SubscriptionStatus(id: 'CmHandle2', status: responseStatus)]
            def cmSubscriptionDmiOutEventData = new Data(subscriptionName: 'cm-subscription-001',
                clientId: 'SCO-9989752', dmiName: 'ncmp-dmi-plugin', subscriptionStatus: subscriptionStatuses)
            def cmSubscriptionDmiOutEvent =
                new CmSubscriptionDmiOutEvent().withData(cmSubscriptionDmiOutEventData)
        when: 'a Cm Subscription DMI Out Event is converted'
            def result = objectUnderTest.toCloudEvent(cmSubscriptionDmiOutEvent, "subscriptionCreatedStatus", dmiName)
        then: 'Cm Subscription DMI Out Event is converted as expected'
            def expectedCloudEvent = CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create('test-ncmp-dmi'))
                .withType("subscriptionCreated")
                .withDataSchema(URI.create("urn:cps:" + CmSubscriptionDmiOutEvent.class.getName() + ":1.0.0"))
                .withExtension("correlationid", cmSubscriptionDmiOutEvent.getData().getClientId() + ":" + cmSubscriptionDmiOutEvent.getData().getSubscriptionName())
                .withData(objectMapper.writeValueAsBytes(cmSubscriptionDmiOutEvent)).build()
            assert expectedCloudEvent.data == result.data
    }

    def 'Map the Cloud Event to data of the subscription event with incorrect content causes an exception'() {
        given: 'an empty subscription response event and event key'
            def dmiName = 'test-ncmp-dmi'
            def cmSubscriptionDmiOutEvent = new CmSubscriptionDmiOutEvent()
        when: 'the cm subscription dmi out Event map to data of cloud event'
            objectUnderTest.toCloudEvent(cmSubscriptionDmiOutEvent, "subscriptionCreatedStatus", dmiName)
        then: 'a run time exception is thrown'
            thrown(CloudEventConstructionException)
    }
}