/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2023 Nordix Foundation
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

package org.onap.cps.ncmp.dmi.notifications.avc

import org.onap.cps.ncmp.event.model.Payload
import org.onap.cps.ncmp.event.model.ResponseData
import spock.lang.Specification

class SubscriptionEventResponseProducerServiceSpec extends Specification {

    def mockSubscriptionEventResponseProducer = Mock(SubscriptionEventResponseProducer)

    def objectUnderTest = new SubscriptionEventResponseProducerService(mockSubscriptionEventResponseProducer)

    def 'Create and publish subscription response event successfully'() {
        given: 'a message key and a message value'
            def messageKey= UUID.randomUUID().toString()
            def messageValue = new ResponseData(payload: new Payload())
        when: 'service is called to publish subscription response data'
            objectUnderTest.publishSubscriptionEventResponse(messageKey, messageValue)
        then: 'producer is called one time'
            1 * mockSubscriptionEventResponseProducer.publishResponseEventMessage(_, messageKey, messageValue)
    }
}
