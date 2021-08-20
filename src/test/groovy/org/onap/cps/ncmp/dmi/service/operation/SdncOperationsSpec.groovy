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

package org.onap.cps.ncmp.dmi.service.operation

import org.onap.cps.ncmp.dmi.config.DmiConfiguration
import org.onap.cps.ncmp.dmi.service.client.SdncRestconfClient
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@SpringBootTest
@ContextConfiguration(classes = [DmiConfiguration.SdncProperties, SdncOperations])
class SdncOperationsSpec extends Specification {

    @SpringBean
    SdncRestconfClient mockSdncRestClient = Mock()

    @Autowired
    SdncOperations objectUnderTest

    def 'call get modules from node to SDNC.'() {
        given: 'node id and url'
            def nodeId = 'node1'
            def expectedUrl = '/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/ietf-netconf-monitoring:netconf-state/schemas'
        when: 'called get modules from node'
            objectUnderTest.getModulesFromNode(nodeId)
        then: 'the get operation is executed with the correct URL'
            1 * mockSdncRestClient.getOperation(expectedUrl)
    }

    def 'Get module resources from SDNC.'() {
        given: 'node id and url'
            def nodeId = 'some-node'
            def expectedUrl = '/rests/operations/network-topology:network-topology/topology=test-topology/node=some-node/yang-ext:mount/ietf-netconf-monitoring:get-schema'
        when: 'get module resources is called with the expected parameters'
            objectUnderTest.getModuleResource(nodeId, 'some-json-data')
        then: 'the SDNC Rest client is invoked with the correct URL and json data'
            1 * mockSdncRestClient.postOperationWithJsonData(expectedUrl, 'some-json-data', new HttpHeaders())
    }

    def 'Get resource data from node to SDNC.'() {
        given: 'excpected url, topology-id, sdncOperation object'
            def expectedUrl = '/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/testResourceId?fields=testFields&depth=10&content=testContent'
        when: 'called get modules from node'
            objectUnderTest.getResouceDataForOperationalAndRunning('node1', 'testResourceId',
                    'testFields', 10, 'testAcceptParam', 'content=testContent')
        then: 'the get operation is executed with the correct URL'
            1 * mockSdncRestClient.getOperation(expectedUrl, _ as HttpHeaders)
    }

    def 'Write resource data to SDNC.'() {
        given: 'excpected url, topology-id, sdncOperation object'
            def expectedUrl = '/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/testResourceId'
        when: 'write resource data for pass through running is called'
            objectUnderTest.writeResourceDataPassthroughRunnng('node1', 'testResourceId', 'application/json' ,'testData')
        then: 'the post operation is executed with the correct URL'
            1 * mockSdncRestClient.postOperationWithJsonData(expectedUrl, _ as String, _ as HttpHeaders)
    }
}