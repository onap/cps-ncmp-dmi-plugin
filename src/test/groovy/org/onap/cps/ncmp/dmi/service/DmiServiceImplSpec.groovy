    /*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
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

package org.onap.cps.ncmp.dmi.service

    import com.fasterxml.jackson.core.JsonProcessingException
    import com.fasterxml.jackson.databind.ObjectMapper
    import org.onap.cps.ncmp.dmi.TestUtils
    import org.onap.cps.ncmp.dmi.config.DmiPluginConfig
    import org.onap.cps.ncmp.dmi.service.client.NcmpRestClient
    import org.springframework.http.HttpStatus
    import org.springframework.http.ResponseEntity
    import spock.lang.Specification

    class DmiServiceImplSpec extends Specification {

    def objectUnderTest = new DmiServiceImpl()

    def mockNcmpRestClient = Mock(NcmpRestClient)

    def mockDmiPluginProperties = Mock(DmiPluginConfig.DmiPluginProperties)

    def objectMapper = new ObjectMapper()

    def mockObjectMapper = Mock(ObjectMapper)

    def setup() {
        objectUnderTest.ncmpRestClient = mockNcmpRestClient
        objectUnderTest.dmiPluginProperties = mockDmiPluginProperties
        objectUnderTest.objectMapper = objectMapper
    }

    def 'Register cm handles with ncmp.'() {

        given: 'cm-handle list and json payload to verify ncmp client payload'
            def cmHandlesList = new ArrayList<>();
            cmHandlesList.add("node1")
            cmHandlesList.add("node2")

            def json =  TestUtils.getResourceFileContent('ncmp_register_payload.json')

        and: 'mockDmiPluginProperties returns dmi-service-name'
            mockDmiPluginProperties.getDmiServiceName() >> "test-dmi-service"

        when: 'registerCmHandles service method called'
            def result = objectUnderTest.registerCmHandles(cmHandlesList)

        then: 'registerCmHandlesWithNcmp called once and returns'
            1 * mockNcmpRestClient.registerCmHandlesWithNcmp(json.strip()) >> responseEntity

        and: 'compare result as expected'
            result == expectedresult

        where: 'given response entity and expected result'
            scenarios                                  |   responseEntity                                   ||     expectedresult
            'response entity is ok'                    |   new ResponseEntity<>(HttpStatus.OK)              ||     true
            'response entity is created'               |   new ResponseEntity<>(HttpStatus.CREATED)         ||     true
            'response entity is bad request'           |   new ResponseEntity<>(HttpStatus.BAD_REQUEST)     ||     false
    }

    def 'RegisterCmHandles called with wrong content.'() {

        given: 'objectMapper mock and cm-handle list'
            objectUnderTest.objectMapper = mockObjectMapper
            def cmHandlesList = new ArrayList<>();
            cmHandlesList.add("node1")

        when: 'registerCmHandles service method called'
            def result = objectUnderTest.registerCmHandles(cmHandlesList)

        then: 'objectMapper called and threw exception'
            1 * mockObjectMapper.writeValueAsString(_) >> { throw new JsonProcessingException("ex") }

        and: 'returns false'
            result == false
    }
}
