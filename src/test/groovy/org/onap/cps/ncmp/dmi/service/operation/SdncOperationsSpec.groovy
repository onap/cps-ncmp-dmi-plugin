/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  Modifications Copyright (C) 2021 Bell Canada
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
import org.onap.cps.ncmp.dmi.exception.SdncException
import org.onap.cps.ncmp.dmi.service.client.SdncRestconfClient
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.MultiValueMap
import spock.lang.Specification

import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.CREATE
import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.DELETE
import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.PATCH
import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.UPDATE
import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.READ

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
            def expectedUrl = '/rests/data/network-topology:network-topology/node=node1/yang-ext:mount/ietf-netconf-monitoring:netconf-state/schemas/?topology=test-topology'
        and: 'sdnc returns one module in response'
            mockSdncRestClient.getOperation(expectedUrl) >>
                ResponseEntity.ok(TestUtils.getResourceFileContent('ModuleSchema.json'))
        when: 'get modules from node is called'
            def moduleSchemas = objectUnderTest.getModuleSchemasFromNode(nodeId)
        then: 'one module is found'
            moduleSchemas.size() == 1
        and: 'module schema has expected values'
            with(moduleSchemas[0]) {
                it.getIdentifier() == "example-identifier"
                it.getNamespace() == "example:namespace"
                it.getVersion() == "example-version"
                it.getFormat() == "example-format"
                it.getLocation() == ["example-location"]
            }
    }

    def 'No modules from Node: SDNC Response - #scenario .'() {
        given: 'node id and url'
            def nodeId = 'node1'
            def expectedUrl = '/rests/data/network-topology:network-topology/node=node1/yang-ext:mount/ietf-netconf-monitoring:netconf-state/schemas/?topology=test-topology'
        and: 'sdnc operation returns #scenario'
            mockSdncRestClient.getOperation(expectedUrl) >> ResponseEntity.ok(responseBody)
        when: 'modules from node is called'
            def moduleSchemas = objectUnderTest.getModuleSchemasFromNode(nodeId)
        then: 'no modules are returned'
            moduleSchemas.size() == 0
        where:
            scenario               | responseBody
            'empty response body ' | ''
            'no module schema'     | '{ "ietf-netconf-monitoring:schemas" : { "schema" : [] } } '
    }

    def 'Error handling - modules from node: #scenario'() {
        given: 'node id and url'
            def nodeId = 'node1'
            def expectedUrl = '/rests/data/network-topology:network-topology/node=node1/yang-ext:mount/ietf-netconf-monitoring:netconf-state/schemas/?topology=test-topology'
        and: 'sdnc operation returns configured response'
            mockSdncRestClient.getOperation(expectedUrl) >> new ResponseEntity<>(sdncResponseBody, sdncHttpStatus)
        when: 'modules for node are fetched'
            objectUnderTest.getModuleSchemasFromNode(nodeId)
        then: 'SDNCException is thrown'
            def thrownException = thrown(SdncException)
            thrownException.getDetails().contains(expectedExceptionDetails)
        where:
            scenario                             | sdncHttpStatus         | sdncResponseBody                        || expectedExceptionDetails
            'failed response from SDNC'          | HttpStatus.BAD_REQUEST | '{ "errorMessage" : "incorrect input"}' || '{ "errorMessage" : "incorrect input"}'
            'invalid json response'              | HttpStatus.OK          | 'invalid-json'                          || 'SDNC response is not in the expected format'
            'response in unexpected json schema' | HttpStatus.OK          | '{ "format" : "incorrect" }'            || 'SDNC response is not in the expected format'
    }

    def 'Get module resources from SDNC.'() {
        given: 'node id and url'
            def nodeId = 'some-node'
            def expectedUrl = '/rests/operations/network-topology:network-topology/node=some-node/yang-ext:mount/ietf-netconf-monitoring:get-schema/?topology=test-topology'
        when: 'get module resources is called with the expected parameters'
            objectUnderTest.getModuleResource(nodeId, 'some-json-data')
        then: 'the SDNC Rest client is invoked with the correct URL and json data'
            1 * mockSdncRestClient.httpOperationWithJsonData(HttpMethod.POST, expectedUrl, 'some-json-data', _ as HttpHeaders)
    }

    def 'Get resource data from node to SDNC.'() {
        given: 'expected url, topology-id, sdncOperation object'
            def expectedUrl = '/rests/data/network-topology:network-topology/node=node1/yang-ext:mount/testResourceId/?topology=test-topology&a=1&b=2&content=testContent'
        when: 'called get modules from node'
            objectUnderTest.getResouceDataForOperationalAndRunning('node1', 'testResourceId',
                '(a=1,b=2)', 'testAcceptParam', 'content=testContent')
        then: 'the get operation is executed with the correct URL'
            1 * mockSdncRestClient.getOperation(expectedUrl, _ as HttpHeaders)
    }

    def 'Write resource data with #scenario operation to SDNC.'() {
        given: 'expected url, topology-id, sdncOperation object'
            def expectedUrl = '/rests/data/network-topology:network-topology/node=node1/yang-ext:mount/testResourceId/?topology=test-topology'
        when: 'write resource data for passthrough running is called'
            objectUnderTest.writeData(operationEnum, 'node1', 'testResourceId', 'application/json', 'requestData')
        then: 'the #expectedHttpMethod operation is executed with the correct URL and data'
            1 * mockSdncRestClient.httpOperationWithJsonData(expectedHttpMethod, expectedUrl, 'requestData', _ as HttpHeaders)
        where: 'the following values are used'
            scenario  | operationEnum  || expectedHttpMethod
            'Create'  | CREATE         || HttpMethod.POST
            'Update'  | UPDATE         || HttpMethod.PUT
            'Read'    | READ           || HttpMethod.GET
            'Delete'  | DELETE         || HttpMethod.DELETE
            'Patch'   | PATCH          || HttpMethod.PATCH
    }

    def 'build query param list for SDNC where options contains a #scenario'() {
        when: 'build query param list is called with #scenario'
            def result = objectUnderTest.buildQueryParamMap(optionsParamInQuery, 'd=4').toSingleValueMap()
        then: 'result size is equals to'
            result.size() == expectedResultSize
        and: 'result equals to expected result'
            result.toString() == expectedResult
        where: 'following parameters are used'
            scenario                   | optionsParamInQuery || expectedResultSize | expectedResult
            'single key-value pair'    | '(a=x)'             || 2                  | '[a:x, d:4]'
            'multiple key-value pairs' | '(a=x,b=y,c=z)'     || 4                  | '[a:x, b:y, c:z, d:4]'
            '/ as special char'        | '(a=x,b=y,c=t/z)'   || 4                  | '[a:x, b:y, c:t/z, d:4]'
            '" as special char'        | '(a=x,b=y,c="z")'   || 4                  | '[a:x, b:y, c:"z", d:4]'
            '[] as special char'       | '(a=x,b=y,c=[z])'   || 4                  | '[a:x, b:y, c:[z], d:4]'
            '= in value'               | '(a=(x=y),b=x=y)'   || 3                  | '[a:(x=y), b:x=y, d:4]'
    }

    def 'options parameters contains a comma #scenario'() {
        // https://jira.onap.org/browse/CPS-719
        when: 'build query param list is called with #scenario'
            def result = objectUnderTest.buildQueryParamMap(optionsParamInQuery, 'd=4').toSingleValueMap()
        then: 'expect 3 elements from options +1 from content query param (3+1) = 4 elements'
            def expectedNoOfElements = 4
        and: 'results contains more elements than expected'
            result.size() == expectedNoOfElements
        where: 'following parameters are used'
            scenario              | optionsParamInQuery
            '"," in value'        | '(a=(x,y),b=y)'
            '"," in string value' | '(a="x,y",b=y)'
    }
}
