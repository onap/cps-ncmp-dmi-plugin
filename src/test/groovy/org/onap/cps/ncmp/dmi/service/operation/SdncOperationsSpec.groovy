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

import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.config.DmiConfiguration
import org.onap.cps.ncmp.dmi.exception.DmiException
import org.onap.cps.ncmp.dmi.service.client.SdncRestconfClient
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

@SpringBootTest
@ContextConfiguration(classes = [DmiConfiguration.SdncProperties, SdncOperations])
class SdncOperationsSpec extends Specification {

    @SpringBean
    SdncRestconfClient mockSdncRestClient = Mock()

    @Autowired
    SdncOperations objectUnderTest

    def 'get modules from node.'() {
        given: 'node id and url'
            def nodeId = 'node1'
            def expectedUrl = '/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/ietf-netconf-monitoring:netconf-state/schemas'
        and: 'sdnc operation returns response successfully'
            mockSdncRestClient.getOperation(expectedUrl) >>
                ResponseEntity.ok(TestUtils.getResourceFileContent('ModuleSchema.json'))
        when: 'called get modules from node'
            def moduleSchemas = objectUnderTest.getModuleSchemasFromNode(nodeId)
        then: 'one module is found'
            moduleSchemas.size() == 1
        and: 'module schema has expected values'
            def moduleSchema = moduleSchemas.get(0)
            moduleSchema.getIdentifier() == "example-identifier"
            moduleSchema.getNamespace() == "example:namespace"
            moduleSchema.getVersion() == "example-version"
            moduleSchema.getFormat() == "example-format"
            moduleSchema.getLocation() == ["example-location"]
    }

    def 'Empty modules from Node: #scenario .'() {
        given: 'node id and url'
            def nodeId = 'node1'
            def expectedUrl = '/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/ietf-netconf-monitoring:netconf-state/schemas'
        and: 'sdnc operation returns #scenario'
            mockSdncRestClient.getOperation(expectedUrl) >> ResponseEntity.ok(responseBody)
        when: 'called get modules from node'
            def moduleSchemas = objectUnderTest.getModuleSchemasFromNode(nodeId)
        then: 'empty moduleSchema list'
            moduleSchemas.size() == 0
        where:
            scenario               | responseBody
            'empty response body ' | ''
            'no module schema'     | '{ "ietf-netconf-monitoring:schemas" : { "schema" : [] } } '
    }

    def 'Error handling - modules from node: #scenario'() {
        given: 'node id and url'
            def nodeId = 'node1'
            def expectedUrl = '/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/ietf-netconf-monitoring:netconf-state/schemas'
        and: 'sdnc operation returns response'
            mockSdncRestClient.getOperation(expectedUrl) >> new ResponseEntity<>('invalid client error', HttpStatus.BAD_REQUEST)
        when: 'fetch modules for node'
            objectUnderTest.getModuleSchemasFromNode(nodeId)
        then: 'throws DMIException'
            thrown(DmiException)
        where:
            scenario                    | sdncHttpStatus         | sdncResponseBody
            'failed response from SDNC' | HttpStatus.BAD_REQUEST | '{ "errorMessage" : "incorrect input"}'
            'incorrect response format' | HttpStatus.OK          | '{ "format" : "incorrect" }'
    }

    def 'get modules from node: invalid response format from SDNC'() {
        given: 'node id and url'
            def nodeId = 'node1'
            def expectedUrl = '/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/ietf-netconf-monitoring:netconf-state/schemas'
        and: 'sdnc operation returns response'
            mockSdncRestClient.getOperation(expectedUrl) >> new ResponseEntity<>('invalid client error', HttpStatus.BAD_REQUEST)
        when: 'fetch modules for node'
            objectUnderTest.getModuleSchemasFromNode(nodeId)
        then: 'throws DMIException'
            thrown(DmiException)
    }

    // Add Error test cases for SDNC interaction
    // Response in incorrect format
    // Http request failed

    def 'Get module resources from SDNC.'() {
        given: 'node id and url'
            def nodeId = 'some-node'
            def expectedUrl = '/rests/operations/network-topology:network-topology/topology=test-topology/node=some-node/yang-ext:mount/ietf-netconf-monitoring:get-schema'
        when: 'get module resources is called with the expected parameters'
            objectUnderTest.getModuleResource(nodeId, 'some-json-data')
        then: 'the SDNC Rest client is invoked with the correct URL and json data'
            1 * mockSdncRestClient.postOperationWithJsonData(expectedUrl, 'some-json-data', _ as HttpHeaders)
    }

    def 'Get resource data from node to SDNC.'() {
        given: 'expected url, topology-id, sdncOperation object'
            def expectedUrl = '/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/testResourceId?a=1&b=2&content=testContent'
        when: 'called get modules from node'
            objectUnderTest.getResouceDataForOperationalAndRunning('node1', 'testResourceId',
                '(a=1,b=2)', 'testAcceptParam', 'content=testContent')
        then: 'the get operation is executed with the correct URL'
            1 * mockSdncRestClient.getOperation(expectedUrl, _ as HttpHeaders)
    }

    def 'Write resource data to SDNC.'() {
        given: 'expected url, topology-id, sdncOperation object'
            def expectedUrl = '/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/testResourceId'
        when: 'write resource data for pass through running is called'
            objectUnderTest.writeResourceDataPassthroughRunning('node1', 'testResourceId', 'application/json', 'requestData')
        then: 'the post operation is executed with the correct URL and data'
            1 * mockSdncRestClient.postOperationWithJsonData(expectedUrl, 'requestData', _ as HttpHeaders)
    }

    def 'build query param list for SDNC where options contains a #scenario'() {
        when: 'build query param list is called with #scenario'
            def result = objectUnderTest.buildQueryParamList(optionsParamInQuery, 'd=4')
        then: 'result equals to expected result'
            result == expectedResult
        where: 'following parameters are used'
            scenario                   | optionsParamInQuery || expectedResult
            'single key-value pair'    | '(a=x)'             || ['a=x', 'd=4']
            'multiple key-value pairs' | '(a=x,b=y,c=z)'     || ['a=x', 'b=y', 'c=z', 'd=4']
            '/ as special char'        | '(a=x,b=y,c=t/z)'   || ['a=x', 'b=y', 'c=t/z', 'd=4']
            '" as special char'        | '(a=x,b=y,c="z")'   || ['a=x', 'b=y', 'c="z"', 'd=4']
            '[] as special char'       | '(a=x,b=y,c=[z])'   || ['a=x', 'b=y', 'c=[z]', 'd=4']
            '= in value'               | '(a=(x=y),b=x=y)'   || ['a=(x=y)', 'b=x=y', 'd=4']
    }

    def 'options parameters contains a comma #scenario'() {
        // https://jira.onap.org/browse/CPS-719
        when: 'build query param list is called with #scenario'
            def result = objectUnderTest.buildQueryParamList(optionsParamInQuery, 'd=4')
        then: 'expect 2 elements from options +1 from content query param (2+1) = 3 elements'
            def expectedNoOfElements = 3
        and: 'results contains more elements than expected'
            result.size() > expectedNoOfElements
        where: 'following parameters are used'
            scenario              | optionsParamInQuery
            '"," in value'        | '(a=(x,y),b=y)'
            '"," in string value' | '(a="x,y",b=y)'
    }
}
