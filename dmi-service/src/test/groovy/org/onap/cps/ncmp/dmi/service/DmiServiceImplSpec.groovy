/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2025 OpenInfra Foundation Europe. All rights reserved.
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

package org.onap.cps.ncmp.dmi.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import org.onap.cps.ncmp.dmi.cmstack.avc.CmAvcEventService
import org.onap.cps.ncmp.dmi.config.DmiPluginConfig
import org.onap.cps.ncmp.dmi.exception.CmHandleRegistrationException
import org.onap.cps.ncmp.dmi.exception.DmiException
import org.onap.cps.ncmp.dmi.exception.ModuleResourceNotFoundException
import org.onap.cps.ncmp.dmi.exception.ModulesNotFoundException
import org.onap.cps.ncmp.dmi.exception.HttpClientRequestException
import org.onap.cps.ncmp.dmi.service.model.ModuleReference
import org.onap.cps.ncmp.dmi.model.YangResource
import org.onap.cps.ncmp.dmi.model.YangResources
import org.onap.cps.ncmp.dmi.service.client.NcmpRestClient
import org.onap.cps.ncmp.dmi.service.model.ModuleSchema
import org.onap.cps.ncmp.dmi.service.operation.SdncOperations
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.CREATE
import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.UPDATE

class DmiServiceImplSpec extends Specification {


    def mockNcmpRestClient = Mock(NcmpRestClient)
    def mockDmiPluginProperties = Mock(DmiPluginConfig.DmiPluginProperties)
    def spyObjectMapper = Spy(ObjectMapper)
    def mockObjectMapper = Mock(ObjectMapper)
    def mockSdncOperations = Mock(SdncOperations)
    def mockCmAvcEventService = Mock(CmAvcEventService)
    def objectUnderTest = new DmiServiceImpl(mockDmiPluginProperties, mockNcmpRestClient, mockSdncOperations, spyObjectMapper, mockCmAvcEventService)

    def 'Register cm handles with ncmp.'() {
        given: 'some cm-handle ids'
            def givenCmHandlesList = ['node1', 'node2']
        and: 'json payload'
            def expectedJson = '{"dmiPlugin":"test-dmi-service","createdCmHandles":[{"cmHandle":"node1"},{"cmHandle":"node2"}]}'
        and: 'process returns "test-dmi-service" for service name'
            mockDmiPluginProperties.getDmiServiceUrl() >> 'test-dmi-service'
        when: 'the cm handles are registered'
            objectUnderTest.registerCmHandles(givenCmHandlesList)
        then: 'register cm handle with ncmp is called with the expected json and return "created" status'
            1 * mockNcmpRestClient.registerCmHandlesWithNcmp(expectedJson) >> new ResponseEntity<>(HttpStatus.CREATED)
    }

    def 'Register cm handles with ncmp called with exception #scenario.'() {
        given: 'some cm-handle ids'
            def cmHandlesList = ['node1', 'node2']
        and: 'process returns "test-dmi-service" for service name'
            mockDmiPluginProperties.getDmiServiceUrl() >> 'test-dmi-service'
        and: 'returns #responseEntity'
            mockNcmpRestClient.registerCmHandlesWithNcmp(_ as String) >> responseEntity
        when: 'the cm handles are registered'
            objectUnderTest.registerCmHandles(cmHandlesList)
        then: 'a registration exception is thrown'
            thrown(CmHandleRegistrationException.class)
        where: 'given #scenario'
            scenario                                         | responseEntity
            'ncmp rest client returns bad request'           | new ResponseEntity<>(HttpStatus.BAD_REQUEST)
            'ncmp rest client returns internal server error' | new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    def 'Register cm handles with ncmp with wrong data.'() {
        given: 'some cm-handle ids'
            def cmHandlesList = ['node1', 'node2']
        and: ' "JsonProcessingException" occurs during parsing'
            objectUnderTest.objectMapper = mockObjectMapper
            mockObjectMapper.writeValueAsString(_) >> { throw new JsonProcessingException('some error.') }
        when: 'the cmHandles are registered'
            objectUnderTest.registerCmHandles(cmHandlesList)
        then: 'a dmi exception is thrown'
            thrown(DmiException.class)
    }

    def ' Get modules for a cm-handle.'() {
        given: 'a cm handle'
            def cmHandle = 'node1'
        and: 'process returns one module schema for the cmhandle'
            def moduleSchema = new ModuleSchema(
                    identifier: "example-identifier",
                    namespace: "example:namespace",
                    version: "example-version")
            mockSdncOperations.getModuleSchemasFromNode(cmHandle) >> List.of(moduleSchema)
        when: 'modules for cmHandle is requested'
            def result = objectUnderTest.getModulesForCmHandle(cmHandle)
        then: 'one module is returned'
            result.schemas.size() == 1
        and: 'module has expected values'
            with(result.schemas[0]) {
                it.getRevision() == moduleSchema.getVersion()
                it.getModuleName() == moduleSchema.getIdentifier()
                it.getNamespace() == moduleSchema.getNamespace();
            }
    }

    def 'no modules found for the cmhandle.'() {
        given: 'cm handle id'
            def cmHandle = 'node1'
        and: 'process returns no modules'
            mockSdncOperations.getModuleSchemasFromNode(cmHandle) >> Collections.emptyList();
        when: 'modules for cm-handle is requested'
            objectUnderTest.getModulesForCmHandle(cmHandle)
        then: 'module not found exception is thrown'
            thrown(ModulesNotFoundException)
    }

    def 'Get multiple module resources.'() {
        given: 'a cmHandle'
            def cmHandle = 'some-cmHandle'
        and: 'multiple module references'
            def moduleReference1 = new ModuleReference(name: 'name-1', revision: 'revision-1')
            def moduleReference2 = new ModuleReference(name: 'name-2', revision: 'revision-2')
            def moduleList = [moduleReference1, moduleReference2]
        when: 'module resources is requested'
            def result = objectUnderTest.getModuleResources(cmHandle, moduleList)
        then: 'SDNC operation service is called same number of module references given'
            2 * mockSdncOperations.getModuleResource(cmHandle, _) >>> [new ResponseEntity<String>('{"ietf-netconf-monitoring:output": {"data": "some-data1"}}', HttpStatus.OK),
                                                                       new ResponseEntity<String>('{"ietf-netconf-monitoring:output": {"data": "some-data2"}}', HttpStatus.OK)]
        and: 'the result contains the expected properties'
            def yangResources = new YangResources()
            def yangResource1 = new YangResource(yangSource: 'some-data1', moduleName: 'name-1', revision: 'revision-1')
            def yangResource2 = new YangResource(yangSource: 'some-data2', moduleName: 'name-2', revision: 'revision-2')
            yangResources.add(yangResource1)
            yangResources.add(yangResource2)
            assert result == yangResources
    }

    def 'Get a module resource with module resource not found exception for #scenario.'() {
        given: 'a cmHandle and module reference list'
            def cmHandle = 'some-cmHandle'
            def moduleReference = new ModuleReference(name: 'NAME', revision: 'REVISION')
            def moduleList = [moduleReference]
        when: 'module resources is requested'
            objectUnderTest.getModuleResources(cmHandle, moduleList)
        then: 'SDNC operation service is called once with a response body that contains no data'
            1 * mockSdncOperations.getModuleResource(cmHandle, _) >> new ResponseEntity<String>(responseBody, HttpStatus.OK)
        and: 'an exception is thrown'
            thrown(ModuleResourceNotFoundException)
        where: 'the following values are returned'
            scenario                                                              | responseBody
            'a response body containing no data object'                           | '{"ietf-netconf-monitoring:output": {"null": "some-data"}}'
            'a response body containing no ietf-netconf-monitoring:output object' | '{"null": {"data": "some-data"}}'
    }

    def 'Get module resources when sdnc returns #scenario response.'() {
        given: 'sdnc returns a #scenario response'
            mockSdncOperations.getModuleResource(_ as String, _ as String) >> new ResponseEntity<String>('some-response-body', httpStatus)
        when: 'module resources is requested'
            objectUnderTest.getModuleResources('some-cmHandle', [new ModuleReference()] as LinkedList<ModuleReference>)
        then: '#expectedException is thrown'
            thrown(expectedException)
        where: 'the following values are returned'
            scenario                | httpStatus                       || expectedException
            'not found'             | HttpStatus.NOT_FOUND             || ModuleResourceNotFoundException
            'internal server error' | HttpStatus.INTERNAL_SERVER_ERROR || DmiException
    }

    def 'Get module resources with JSON processing exception.'() {
        given: 'a json processing exception during process'
            def mockObjectWriter = Mock(ObjectWriter)
            spyObjectMapper.writer() >> mockObjectWriter
            mockObjectWriter.withRootName(_) >> mockObjectWriter
            def jsonProcessingException = new JsonProcessingException('')
            mockObjectWriter.writeValueAsString(_) >> { throw jsonProcessingException }
        when: 'module resources is requested'
            objectUnderTest.getModuleResources('some-cmHandle', [new ModuleReference()] as LinkedList<ModuleReference>)
        then: 'an exception is thrown'
            def thrownException = thrown(DmiException.class)
        and: 'the exception has the expected message and details'
            thrownException.message == 'Unable to process JSON.'
            thrownException.details == 'JSON exception occurred when creating the module request.'
        and: 'the cause is the original json processing exception'
            thrownException.cause == jsonProcessingException
    }

    def 'Get resource data for passthrough operational.'() {
        given: 'sdnc operation returns OK response'
            mockSdncOperations.getResouceDataForOperationalAndRunning(
                    'someCmHandle',
                    'someResourceId',
                    '(fields=x/y/z,depth=10,test=abc)',
                    'content=all') >> new ResponseEntity<>('response json', HttpStatus.OK)
        when: 'resource data is requested'
            def response = objectUnderTest.getResourceData(
                    'someCmHandle',
                    'someResourceId',
                    '(fields=x/y/z,depth=10,test=abc)',
                    'content=all')
        then: 'response matches the response returned from the SDNC service'
            response == 'response json'
    }

    def 'Get resource data with not found exception.'() {
        given: 'sdnc operation returns "NOT_FOUND" response'
            mockSdncOperations.getResouceDataForOperationalAndRunning(*_) >> new ResponseEntity<>(HttpStatus.NOT_FOUND)
        when: 'resource data is requested'
            objectUnderTest.getResourceData('someCmHandle', 'someResourceId',
                    '(fields=x/y/z,depth=10,test=abc)', 'content=config')
        then: 'http client request exception'
            thrown(HttpClientRequestException.class)
    }

    def 'Get resource data for passthrough running.'() {
        given: 'sdnc operation returns OK response'
            mockSdncOperations.getResouceDataForOperationalAndRunning(*_) >> new ResponseEntity<>('response json', HttpStatus.OK)
        when: 'resource data is requested'
            def response = objectUnderTest.getResourceData(
                    'someCmHandle',
                    'someResourceId',
                    '(fields=x/y/z,depth=10,test=abc)',
                    'content=config')
        then: 'response have expected json'
            response == 'response json'
    }

    def 'Write resource data for passthrough running with a #scenario from sdnc.'() {
        given: 'sdnc returns a response with #scenario'
            mockSdncOperations.writeData(operationEnum, _, _, _, _) >> new ResponseEntity<String>('response json', httpResponse)
        when: 'resource data is written to sdnc'
            def response = objectUnderTest.writeData(operationEnum, 'some-cmHandle',
                    'some-resourceIdentifier', 'some-dataType', '{some-data}')
        then: 'the response matches the expected data'
            response == 'response json'
        where: 'the following values are used'
            scenario                              | httpResponse       | operationEnum
            '200 OK with an update operation'     | HttpStatus.OK      | UPDATE
            '201 CREATED with a create operation' | HttpStatus.CREATED | CREATE
    }

    def 'Write resource data with special characters.'() {
        given: 'sdnc returns a created response'
            mockSdncOperations.writeData(CREATE, 'some-cmHandle',
                    'some-resourceIdentifier', 'some-dataType', 'data with quote " and \n new line') >> new ResponseEntity<String>('response json', HttpStatus.CREATED)
        when: 'resource data is written to sdnc'
            def response = objectUnderTest.writeData(CREATE, 'some-cmHandle',
                    'some-resourceIdentifier', 'some-dataType', 'data with quote " and \n new line')
        then: 'the response matches the expected data'
            response == 'response json'
    }

    def 'Write resource data for passthrough running with a 500 response from sdnc.'() {
        given: 'sdnc returns internal server error response'
            mockSdncOperations.writeData(CREATE, _, _, _, _) >> new ResponseEntity<String>('response json', HttpStatus.INTERNAL_SERVER_ERROR)
        when: 'resource data is written to sdnc'
            objectUnderTest.writeData(CREATE, 'some-cmHandle',
                    'some-resourceIdentifier', 'some-dataType', _ as String)
        then: 'a dmi exception is thrown'
            thrown(DmiException.class)
    }

    def 'Enabling data synchronization flag'() {
        when: 'data sync is enabled for the cm handles'
            objectUnderTest.enableNcmpDataSyncForCmHandles(['ch-1', 'ch-2'])
        then: 'the ncmp rest client is invoked for the cm handles'
            2 * mockNcmpRestClient.enableNcmpDataSync(_)

    }
}