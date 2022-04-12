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

package org.onap.cps.ncmp.dmi.service

import org.onap.cps.event.model.CpsAsyncRequestResponseEvent
import org.onap.cps.ncmp.dmi.notifications.CpsAsyncRequestResponseEventProducer
import org.onap.cps.ncmp.dmi.notifications.CpsAsyncRequestResponseEventProducerService
import spock.lang.Specification

class CpsAsyncRequestResponseEventProducerServiceSpec extends Specification {

    def mockNcmpKafkaPublisher = Mock(CpsAsyncRequestResponseEventProducer)
    def objectUnderTest = new CpsAsyncRequestResponseEventProducerService(mockNcmpKafkaPublisher)

    def 'Message publishing'() {
        given: 'a sample message with key'
            def messageKey = 'sample message'
            def message = new CpsAsyncRequestResponseEvent()
        when: 'published'
            objectUnderTest.publishEvent(messageKey, message)
        then: 'no exception is thrown'
            noExceptionThrown()
    }
}
