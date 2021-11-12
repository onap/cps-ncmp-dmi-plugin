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

package org.onap.cps.ncmp.dmi.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import org.onap.cps.ncmp.dmi.config.DmiPluginConfig
import org.onap.cps.ncmp.dmi.exception.CmHandleRegistrationException
import org.onap.cps.ncmp.dmi.exception.DmiException
import org.onap.cps.ncmp.dmi.exception.ModuleResourceNotFoundException
import org.onap.cps.ncmp.dmi.exception.ModulesNotFoundException
import org.onap.cps.ncmp.dmi.exception.ResourceDataNotFound
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
    def objectUnderTest = new DmiServiceImpl(mockDmiPluginProperties, mockNcmpRestClient, mockSdncOperations, spyObjectMapper)

    def ' Get modules for a cm-handle.'() {
        given: 'a cm handle'
            def cmHandle = 'node1'
        and: 'sdnc operations returns one module schema for the cmhandle'
            def moduleSchema = new ModuleSchema(
                identifier: "example-identifier",
                namespace: "example:namespace",
                version: "example-version")
            mockSdncOperations.getModuleSchemasFromNode(cmHandle) >> List.of(moduleSchema)
        when: 'get modules for cm-handle is called'
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
        and: 'sdnc operations returns no modules'
            mockSdncOperations.getModuleSchemasFromNode(cmHandle) >> Collections.emptyList();
        when: 'get modules for cm-handle is called'
            objectUnderTest.getModulesForCmHandle(cmHandle)
        then: 'module not found exception is thrown'
            thrown(ModulesNotFoundException)
    }

    def 'Register cm handles with ncmp.'() {
        given: 'cm-handle list and json payload'
            def givenCmHandlesList = ['node1', 'node2']
            def expectedJson = '{"dmiPlugin":"test-dmi-service","createdCmHandles":[{"cmHandle":"node1"},{"cmHandle":"node2"}]}'
        and: 'mockDmiPluginProperties returns test-dmi-service'
            mockDmiPluginProperties.getDmiServiceUrl() >> 'test-dmi-service'
        when: 'register cm handles service method with the given cm handles'
            objectUnderTest.registerCmHandles(givenCmHandlesList)
        then: 'register cm handle with ncmp called once and return "created" status'
            1 * mockNcmpRestClient.registerCmHandlesWithNcmp(expectedJson) >> new ResponseEntity<>(HttpStatus.CREATED)
    }

    def 'Register cm handles with ncmp called with exception #scenario.'() {
        given: 'cm-handle list'
            def cmHandlesList = ['node1', 'node2']
        and: 'dmi plugin service name is "test-dmi-service"'
            mockDmiPluginProperties.getDmiServiceUrl() >> 'test-dmi-service'
        and: 'ncmp rest client returns #responseEntity'
            mockNcmpRestClient.registerCmHandlesWithNcmp(_ as String) >> responseEntity
        when: 'register cm handles service method called'
            objectUnderTest.registerCmHandles(cmHandlesList)
        then: 'a registration exception is thrown'
            thrown(CmHandleRegistrationException.class)
        where: 'given #scenario'
            scenario                                         | responseEntity
            'ncmp rest client returns bad request'           | new ResponseEntity<>(HttpStatus.BAD_REQUEST)
            'ncmp rest client returns internal server error' | new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    def 'Register cm handles with ncmp with wrong data.'() {
        given: 'objectMapper mock and cm-handle list'
            def cmHandlesList = ['node1', 'node2']
        and: 'objectMapper returns "JsonProcessingException" during parse'
            objectUnderTest.objectMapper = mockObjectMapper
            mockObjectMapper.writeValueAsString(_) >> { throw new JsonProcessingException('some error.') }
        when: 'register cm handles service method called'
            objectUnderTest.registerCmHandles(cmHandlesList)
        then: 'a dmi exception is thrown'
            thrown(DmiException.class)
    }

    def 'Get multiple module resources.'() {
        given: 'a cmHandle and module reference list'
            def cmHandle = 'some-cmHandle'
            def moduleReference1 = new ModuleReference(name: 'name-1', revision: 'revision-1')
            def moduleReference2 = new ModuleReference(name: 'name-2', revision: 'revision-2')
            def moduleList = [moduleReference1, moduleReference2]
        when: 'get module resources is invoked with the given cm handle and a module list'
            def result = objectUnderTest.getModuleResources(cmHandle, moduleList)
        then: 'get modules resources is called twice'
            2 * mockSdncOperations.getModuleResource(cmHandle, _) >>> [new ResponseEntity<String>('{"ietf-netconf-monitoring:output": {"data": "some-data1"}}', HttpStatus.OK),
                                                                       new ResponseEntity<String>('{"ietf-netconf-monitoring:output": {"data": "some-data2"}}', HttpStatus.OK)]
        and: 'the result is a yang resources object with the expected names, revisions and yang-sources'
            def yangResources = new YangResources()
            def yangResource1 = new YangResource(yangSource: '"some-data1"', moduleName: 'name-1', revision: 'revision-1')
            def yangResource2 = new YangResource(yangSource: '"some-data2"', moduleName: 'name-2', revision: 'revision-2')
            yangResources.add(yangResource1)
            yangResources.add(yangResource2)
            assert result == yangResources
    }

    def 'Get a module resource with module resource not found exception for #scenario.'() {
        given: 'a cmHandle and module reference list'
            def cmHandle = 'some-cmHandle'
            def moduleReference = new ModuleReference(name: 'NAME', revision: 'REVISION')
            def moduleList = [moduleReference]
        when: 'get module resources is invoked with the given cm handle and a module list'
            objectUnderTest.getModuleResources(cmHandle, moduleList)
        then: 'get modules resources is called once with a response body that contains no data'
            1 * mockSdncOperations.getModuleResource(cmHandle, _) >> new ResponseEntity<String>(responseBody, HttpStatus.OK)
        and: 'a module resource not found exception is thrown'
            thrown(ModuleResourceNotFoundException)
        where: 'the following values are returned'
            scenario                                                              | responseBody
            'a response body containing no data object'                           | '{"ietf-netconf-monitoring:output": {"null": "some-data"}}'
            'a response body containing no ietf-netconf-monitoring:output object' | '{"null": {"data": "some-data"}}'
    }

    def 'Get module resources when sdnc returns #scenario response.'() {
        given: 'get module schema is invoked and returns a response from sdnc'
            mockSdncOperations.getModuleResource(_ as String, _ as String) >> new ResponseEntity<String>('some-response-body', httpStatus)
        when: 'get module resources is invoked with the given cm handle and a module list'
            objectUnderTest.getModuleResources('some-cmHandle', [new ModuleReference()] as LinkedList<ModuleReference>)
        then: '#expectedException is thrown'
            thrown(expectedException)
        where: 'the following values are returned'
            scenario            | httpStatus                       || expectedException
            'not found'         | HttpStatus.NOT_FOUND             || ModuleResourceNotFoundException
            'a internal server' | HttpStatus.INTERNAL_SERVER_ERROR || DmiException
    }

    def 'Get module resources with JSON processing exception.'() {
        given: 'a json processing exception during conversion'
            def mockObjectWriter = Mock(ObjectWriter)
            spyObjectMapper.writer() >> mockObjectWriter
            mockObjectWriter.withRootName(_) >> mockObjectWriter
            def jsonProcessingException = new JsonProcessingException('')
            mockObjectWriter.writeValueAsString(_) >> { throw jsonProcessingException }
        when: 'get module resources is invoked with the given cm handle and a module list'
            objectUnderTest.getModuleResources('some-cmHandle', [new ModuleReference()] as LinkedList<ModuleReference>)
        then: 'a DMI exception is thrown'
            def thrownException = thrown(DmiException.class)
        and: 'the exception has the expected message and details'
            thrownException.message == 'Unable to process JSON.'
            thrownException.details == 'JSON exception occurred when creating the module request.'
        and: 'the cause is the original json processing exception'
            thrownException.cause == jsonProcessingException
    }

    def 'Get resource data for passthrough operational.'() {
        given: 'cm-handle, passthrough parameter, resourceId, accept header, fields, depth'
            def cmHandle = 'testCmHandle'
            def resourceId = 'testResourceId'
            def acceptHeaderParam = 'testAcceptParam'
            def optionsParam = '(fields=x/y/z,depth=10,test=abc)'
            def contentQuery = 'content=all'
        and: 'sdnc operation returns OK response'
            mockSdncOperations.getResouceDataForOperationalAndRunning(cmHandle, resourceId, optionsParam, acceptHeaderParam, contentQuery) >> new ResponseEntity<>('response json', HttpStatus.OK)
        when: 'get resource data from cm handles service method invoked'
            def response = objectUnderTest.getResourceData(cmHandle,
                resourceId, acceptHeaderParam,
                optionsParam, contentQuery)
        then: 'response have expected json'
            response == 'response json'
    }

    def 'Get resource data with not found exception.'() {
        given: 'cm-handle, passthrough parameter, resourceId, accept header, fields, depth, query param'
            def cmHandle = 'testCmHandle'
            def resourceId = 'testResourceId'
            def acceptHeaderParam = 'testAcceptParam'
            def optionsParam = '(fields=x/y/z,depth=10,test=abc)'
            def restConfQueryParam = 'content=config'
        and: 'sdnc operation returns "NOT_FOUND" response'
            mockSdncOperations.getResouceDataForOperationalAndRunning(cmHandle, resourceId, optionsParam, acceptHeaderParam, _ as String) >> new ResponseEntity<>(HttpStatus.NOT_FOUND)
        when: 'get resource data from cm handles service method invoked'
            objectUnderTest.getResourceData(cmHandle,
                resourceId, acceptHeaderParam,
                optionsParam, restConfQueryParam)
        then: 'resource data not found'
            thrown(ResourceDataNotFound.class)
    }

    def 'Get resource data for passthrough running.'() {
        given: 'cm-handle, passthrough parameter, resourceId, accept header, fields, depth'
            def cmHandle = 'testCmHandle'
            def resourceId = 'testResourceId'
            def acceptHeaderParam = 'testAcceptParam'
            def optionsParam = '(fields=x/y/z,depth=10,test=abc)'
            def contentQuery = 'content=config'
        and: 'sdnc operation returns OK response'
            mockSdncOperations.getResouceDataForOperationalAndRunning(cmHandle, resourceId, optionsParam,
                acceptHeaderParam, contentQuery) >> new ResponseEntity<>('response json', HttpStatus.OK)
        when: 'get resource data from cm handles service method invoked'
            def response = objectUnderTest.getResourceData(cmHandle,
                resourceId, acceptHeaderParam,
                optionsParam, contentQuery)
        then: 'response have expected json'
            response == 'response json'
    }

    def 'Write resource data for passthrough running with a #scenario from sdnc.'() {
        given: 'sdnc returns a response with #scenario'
            mockSdncOperations.writeData(operationEnum, _, _, _, _) >> new ResponseEntity<String>('response json', httpResponse)
        when: 'write resource data for cm handle method invoked'
            def response = objectUnderTest.writeData(operationEnum,'some-cmHandle',
                'some-resourceIdentifier', 'some-dataType', '{some-data}')
        then: 'the response contains the expected json data from sdnc'
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
        when: 'write resource data from cm handles service method invoked'
            def response = objectUnderTest.writeData(CREATE, 'some-cmHandle',
                'some-resourceIdentifier', 'some-dataType', 'data with quote " and \n new line')
        then: 'response have expected json'
            response == 'response json'
    }

    def 'Write resource data for passthrough running with a 500 response from sdnc.'() {
        given: 'sdnc returns a 500 response for the write operation'
            mockSdncOperations.writeData(CREATE, _, _, _, _) >> new ResponseEntity<String>('response json', HttpStatus.INTERNAL_SERVER_ERROR)
        when: 'write resource data for passthrough method is invoked'
            objectUnderTest.writeData(CREATE, 'some-cmHandle',
                'some-resourceIdentifier', 'some-dataType', _ as String)
        then: 'a dmi exception is thrown'
            thrown(DmiException.class)
    }
}
