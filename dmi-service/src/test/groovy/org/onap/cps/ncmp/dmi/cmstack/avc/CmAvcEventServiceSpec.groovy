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

package org.onap.cps.ncmp.dmi.cmstack.avc

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import org.springframework.kafka.core.KafkaTemplate
import spock.lang.Specification

import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.CREATE
import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.DELETE

class CmAvcEventServiceSpec extends Specification {

    def mockKafkaTemplate = Mock(KafkaTemplate)
    def mockObjectMapper = Mock(ObjectMapper)
    def objectUnderTest = new CmAvcEventService(mockObjectMapper, mockKafkaTemplate)

    def setup() {
        def dmiCmEventsTopicField = CmAvcEventService.getDeclaredField('dmiCmEventsTopic')
        dmiCmEventsTopicField.accessible = true
        dmiCmEventsTopicField.set(objectUnderTest, 'test-topic')
    }

    def 'Produce cm avc event in case of write operation'() {
        given: 'Data that is sent for write operation'
            def cmHandleId = 'my-cm-handle'
        and: 'mocking successful serialization'
            byte[] expectedBytes = [1, 2, 3]
            mockObjectMapper.writeValueAsBytes(_ as Object) >> expectedBytes
        when: 'the event is sent'
            objectUnderTest.sendCmAvcEvent(CREATE, cmHandleId, '/my/resource/path', '{"data":"my data"}')
        then: 'the event contains relevant details'
            1 * mockKafkaTemplate.send('test-topic', cmHandleId, { CloudEvent event ->
                event.getSource().toString() == 'ONAP-DMI-PLUGIN' &&
                    event.getType().contains('AvcEvent') &&
                    event.getData().toBytes() == expectedBytes
            })
    }

    def 'Event is not sent when cloudEvent is null'() {
        given: 'mocking failed serialization to throw exception resulting in null CloudEvent'
            mockObjectMapper.writeValueAsBytes(_ as Object) >> { throw new JsonProcessingException('failed') {} }
        when: 'cm avc event is sent'
            objectUnderTest.sendCmAvcEvent(DELETE, 'some-cm-handle', '/some/resource', '{"data":"some data"}')
        then: 'kafka template is not invoked as event is null'
            0 * mockKafkaTemplate.send(*_)
    }
}
