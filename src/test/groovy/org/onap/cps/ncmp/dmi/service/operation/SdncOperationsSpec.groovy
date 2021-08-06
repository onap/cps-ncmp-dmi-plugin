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
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class SdncOperationsSpec extends  Specification {
    def mockSdncProperties = Mock(DmiConfiguration.SdncProperties)
    def mockSdncRestClient = Mock(SdncRestconfClient)

    def 'Call get modules from node to SDNC.'() {
        given: 'nodeid, topology-id, responseentity'
            def nodeId = 'node1'
            def expectedUrl = '/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/ietf-netconf-monitoring:netconf-state/schemas'
            mockSdncProperties.getTopologyId() >> 'test-topology'
            def objectUnderTest = new SdncOperations(mockSdncProperties, mockSdncRestClient)
        when: 'called get modules from node'
            objectUnderTest.getModulesFromNode(nodeId)
        then: 'the get operation is executed with the correct URL'
            1 * mockSdncRestClient.getOperation(expectedUrl)
    }


    def 'Get resource data from node to SDNC.'() {
        given: 'excpected url, topology-id, sdncOperation object'
            def expectedUrl = '/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/testResourceId?fields=testFields&depth=10&content=all'
            mockSdncProperties.getTopologyId() >> 'test-topology'
            def objectUnderTest = new SdncOperations(mockSdncProperties, mockSdncRestClient)
        when: 'called get modules from node'
            objectUnderTest.getResouceDataFromNode('node1', 'testResourceId',  ['fields=testFields', 'depth=10', 'content=all'],'testAcceptParam')
        then: 'the get operation is executed with the correct URL'
            1 * mockSdncRestClient.getOperation(expectedUrl, _ as HttpHeaders)
    }
}
