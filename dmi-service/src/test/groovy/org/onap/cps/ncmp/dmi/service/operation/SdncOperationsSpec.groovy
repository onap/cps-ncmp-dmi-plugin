/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2022 Nordix Foundation
 *  Modifications Copyright (C) 2021-2022 Bell Canada
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
import spock.lang.Specification

import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.CREATE
import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.DELETE
import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.PATCH
import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.READ
import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.UPDATE

@SpringBootTest
@ContextConfiguration(classes = [DmiConfiguration.SdncProperties, SdncOperations])
class SdncOperationsSpec extends Specification {

    @SpringBean
    SdncRestconfClient mockSdncRestClient = Mock()

    @Autowired
    SdncOperations objectUnderTest

    def 'get modules from node.'() {
        given: 'a node id and url'
            def nodeId = 'node1'
            def expectedUrl = '/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/ietf-netconf-monitoring:netconf-state/schemas'
        and: 'sdnc returns one module during process'
            mockSdncRestClient.getOperation(expectedUrl) >>
                ResponseEntity.ok(TestUtils.getResourceFileContent('ModuleSchema.json'))
        when: 'module schemas from node are fetched'
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
            def expectedUrl = '/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/ietf-netconf-monitoring:netconf-state/schemas'
        and: 'sdnc operation returns #scenario'
            mockSdncRestClient.getOperation(expectedUrl) >> ResponseEntity.ok(responseBody)
        when: 'the module schemas are requested'
            def moduleSchemas = objectUnderTest.getModuleSchemasFromNode(nodeId)
        then: 'no module schemas are returned'
            moduleSchemas.size() == 0
        where:
            scenario               | responseBody
            'null response body'   | null
            'empty response body ' | ''
            'no module schema'     | '{ "ietf-netconf-monitoring:schemas" : { "schema" : [] } } '
    }

    def 'Error handling - modules from node: #scenario'() {
        given: 'node id and url'
            def nodeId = 'node1'
            def expectedUrl = '/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/ietf-netconf-monitoring:netconf-state/schemas'
        and: '#scenario is returned during process'
            mockSdncRestClient.getOperation(expectedUrl) >> new ResponseEntity<>(sdncResponseBody, sdncHttpStatus)
        when: 'module schemas from node are fetched'
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
            def expectedUrl = '/rests/operations/network-topology:network-topology/topology=test-topology/node=some-node/yang-ext:mount/ietf-netconf-monitoring:get-schema'
        when: 'module resource is fetched with the expected parameters'
            objectUnderTest.getModuleResource(nodeId, 'some-json-data')
        then: 'the SDNC Rest client is invoked with the correct parameters'
            1 * mockSdncRestClient.httpOperationWithJsonData(HttpMethod.POST, expectedUrl, 'some-json-data', _ as HttpHeaders)
    }

    def 'Get resource data from node to SDNC.'() {
        given: 'expected url'
            def expectedUrl = '/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/testResourceId?a=1&b=2&content=testContent'
        when: 'resource data is fetched for given node ID'
            objectUnderTest.getResouceDataForOperationalAndRunning('node1', 'testResourceId',
                '(a=1,b=2)', 'content=testContent')
        then: 'the SDNC get operation is executed with the correct URL'
            1 * mockSdncRestClient.getOperation(expectedUrl)
    }

    def 'Write resource data with #scenario operation to SDNC.'() {
        given: 'expected url'
            def expectedUrl = '/rests/data/network-topology:network-topology/topology=test-topology/node=node1/yang-ext:mount/testResourceId'
        when: 'write resource data for passthrough running is called'
            objectUnderTest.writeData(operationEnum, 'node1', 'testResourceId', 'application/json', 'requestData')
        then: 'the #expectedHttpMethod operation is executed with the correct parameters'
            1 * mockSdncRestClient.httpOperationWithJsonData(expectedHttpMethod, expectedUrl, 'requestData', _ as HttpHeaders)
        where: 'the following values are used'
            scenario  | operationEnum  || expectedHttpMethod
            'Create'  | CREATE         || HttpMethod.POST
            'Update'  | UPDATE         || HttpMethod.PUT
            'Read'    | READ           || HttpMethod.GET
            'Delete'  | DELETE         || HttpMethod.DELETE
            'Patch'   | PATCH          || HttpMethod.PATCH
    }

    def 'build query param list for SDNC where options #scenario'() {
        when: 'query param list is built'
            def result = objectUnderTest.buildQueryParamMap(optionsParamInQuery, 'd=4')
                    .toSingleValueMap().toString()
        then: 'result matches the expected result'
            result == expectedResult
        where: 'following parameters are used'
            scenario                       | optionsParamInQuery || expectedResult
            'is single key-value pair'     | '(a=x)'             || '[a:x, d:4]'
            'is multiple key-value pairs'  | '(a=x,b=y,c=z)'     || '[a:x, b:y, c:z, d:4]'
            'has / as special char'        | '(a=x,b=y,c=t/z)'   || '[a:x, b:y, c:t/z, d:4]'
            'has " as special char'        | '(a=x,b=y,c="z")'   || '[a:x, b:y, c:"z", d:4]'
            'has [] as special char'       | '(a=x,b=y,c=[z])'   || '[a:x, b:y, c:[z], d:4]'
            'has = in value'               | '(a=(x=y),b=x=y)'   || '[a:(x=y), b:x=y, d:4]'
            'is empty'                     | ''                  || '[:]'
            'is null'                      | null                || '[:]'
    }

    def 'options parameters contains a comma #scenario'() {
        when: 'query param list is built with #scenario'
            def result = objectUnderTest.buildQueryParamMap(optionsParamInQuery, 'd=4').toSingleValueMap()
        then: 'expect 2 elements from options where we are ignoring empty query param value, +1 from content query param (2+1) = 3 elements'
            def expectedNoOfElements = 3
        and: 'results contains equal elements as expected'
            result.size() == expectedNoOfElements
        where: 'following parameters are used'
            scenario              | optionsParamInQuery
            '"," in value'        | '(a=(x,y),b=y)'
            '"," in string value' | '(a="x,y",b=y)'
    }

    def 'add resource identifier to the url path'() {
        given: 'base url and resource identifier'
            def baseUrl = 'some-base-url'
        when: 'the resource id is added as separate path segments'
            def result = objectUnderTest.addResource(baseUrl, resourceId)
        then: 'the url is passed as is and no further encoding/decoding takes place'
            assert result == expectedUrl
        where: 'following scenarios are used'
            scenario                                    | resourceId || expectedUrl
            'empty resource id'                         | ''         || 'some-base-url'
            'forward slash'                             | '/'        || 'some-base-url'
            'resource id with forward slash in between' | 'a/b'      || 'some-base-url/a/b'
            'encoded slash in resource id'              | 'a%2Fb/c'  || 'some-base-url/a%2Fb/c'
    }
}
