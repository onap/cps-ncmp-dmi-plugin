package org.onap.cps.ncmp.dmi.service.operation

import org.onap.cps.ncmp.dmi.config.DmiConfiguration
import org.onap.cps.ncmp.dmi.service.client.SdncRestconfClient
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class SdncOperationsSpec extends  Specification {
    def mockSdncProperties = Mock(DmiConfiguration.SdncProperties)
    def mockSdncRestClient = Mock(SdncRestconfClient)

    def 'call getModulesFromNode using valid params'() {

        given: 'nodeid, topology-id, responseentity'
            def nodeId = "node1"
            def expectedUrl = "/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/ietf-netconf-monitoring:netconf-state/schemas"
            mockSdncProperties.getTopologyId() >> "test-topology"

            def objectUnderTest = new SdncOperations(mockSdncProperties, mockSdncRestClient)

        when: 'called getModulesFromNode'
            objectUnderTest.getModulesFromNode(nodeId)

        then: 'getOperation called once'
            1 * mockSdncRestClient.getOperation(expectedUrl)

    }
}
