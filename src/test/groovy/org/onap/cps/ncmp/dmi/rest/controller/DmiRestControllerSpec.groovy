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

package org.onap.cps.ncmp.dmi.rest.controller


import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.exception.DmiException
import org.onap.cps.ncmp.dmi.exception.ModuleResourceNotFoundException
import org.onap.cps.ncmp.dmi.exception.ModulesNotFoundException
import org.onap.cps.ncmp.dmi.notifications.async.AsyncTaskExecutor
import org.onap.cps.ncmp.dmi.notifications.async.DmiAsyncRequestResponseEventProducer

import org.onap.cps.ncmp.dmi.service.model.ModuleReference
import org.onap.cps.ncmp.dmi.model.ModuleSet
import org.onap.cps.ncmp.dmi.model.ModuleSetSchemas
import org.onap.cps.ncmp.dmi.model.YangResource
import org.onap.cps.ncmp.dmi.model.YangResources
import org.onap.cps.ncmp.dmi.service.DmiService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.DELETE
import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.PATCH
import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.READ
import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.CREATE
import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.UPDATE
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.OK

@WebMvcTest(DmiRestController.class)
@WithMockUser
class DmiRestControllerSpec extends Specification {

    @Autowired
    private MockMvc mvc

    @SpringBean
    DmiService mockDmiService = Mock()

    @SpringBean
    DmiAsyncRequestResponseEventProducer cpsAsyncRequestResponseEventProducer = new DmiAsyncRequestResponseEventProducer(Mock(KafkaTemplate))

    @SpringBean
    AsyncTaskExecutor asyncTaskExecutor = new AsyncTaskExecutor(cpsAsyncRequestResponseEventProducer)

    @Value('${rest.api.dmi-base-path}/v1')
    def basePathV1

    def 'Get all modules.'() {
        given: 'REST endpoint for getting all modules'
            def getModuleUrl = "$basePathV1/ch/node1/modules"
        and: 'get modules for cm-handle returns a json'
            def json = '{"cmHandleProperties" : {}}'
            def moduleSetSchema = new ModuleSetSchemas(namespace:'some-namespace',
                                                        moduleName:'some-moduleName',
                                                        revision:'some-revision')
            def moduleSetSchemasList = [moduleSetSchema] as List<ModuleSetSchemas>
            def moduleSet = new ModuleSet()
            moduleSet.schemas(moduleSetSchemasList)
            mockDmiService.getModulesForCmHandle('node1') >> moduleSet
        when: 'post is being called'
            def response = mvc.perform(post(getModuleUrl)
                    .contentType(MediaType.APPLICATION_JSON).content(json))
                    .andReturn().response
        then: 'status is OK'
            response.status == OK.value()
        and: 'the response content matches the result from the DMI service'
            response.getContentAsString() == '{"schemas":[{"moduleName":"some-moduleName","revision":"some-revision","namespace":"some-namespace"}]}'
    }

    def 'Get all modules with exception handling of #scenario.'() {
        given: 'REST endpoint for getting all modules'
            def getModuleUrl = "$basePathV1/ch/node1/modules"
        and: 'given request body and get modules for cm-handle throws #exceptionClass'
            def json = '{"cmHandleProperties" : {}}'
            mockDmiService.getModulesForCmHandle('node1') >> { throw exception }
        when: 'post is invoked'
            def response = mvc.perform( post(getModuleUrl)
                    .contentType(MediaType.APPLICATION_JSON).content(json))
                    .andReturn().response
        then: 'response status is #expectedResponse'
            response.status == expectedResponse.value()
        where: 'the scenario is #scenario'
            scenario                       | exception                                        || expectedResponse
            'dmi service exception'        | new DmiException('','')                          || HttpStatus.INTERNAL_SERVER_ERROR
            'no modules found'             | new ModulesNotFoundException('','')              || HttpStatus.NOT_FOUND
            'any other runtime exception'  | new RuntimeException()                           || HttpStatus.INTERNAL_SERVER_ERROR
            'runtime exception with cause' | new RuntimeException('', new RuntimeException()) || HttpStatus.INTERNAL_SERVER_ERROR
    }

    def 'Register given list.'() {
        given: 'register cm handle url and cm handles json'
            def registerCmhandlesPost = "${basePathV1}/inventory/cmHandles"
            def cmHandleJson = '{"cmHandles":["node1", "node2"]}'
        when: 'register cm handles api is invoked with POST'
            def response = mvc.perform(
                    post(registerCmhandlesPost)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cmHandleJson)
            ).andReturn().response
        then: 'register cm handles in dmi service is invoked with correct parameters'
            1 * mockDmiService.registerCmHandles(_ as List<String>)
        and: 'response status is created'
            response.status == CREATED.value()
    }

    def 'register cm handles called with empty content.'() {
        given: 'register cm handle url and empty json'
            def registerCmhandlesPost = "${basePathV1}/inventory/cmHandles"
            def emptyJson = '{"cmHandles":[]}'
        when: 'register cm handles post api is invoked with no content'
            def response = mvc.perform(
                    post(registerCmhandlesPost).contentType(MediaType.APPLICATION_JSON)
                            .content(emptyJson)
            ).andReturn().response
        then: 'response status is "bad request"'
            response.status == BAD_REQUEST.value()
        and: 'dmi service is not called'
            0 * mockDmiService.registerCmHandles(_)
    }

    def 'Retrieve module resources.'() {
        given: 'an endpoint and json data'
            def getModulesEndpoint = "$basePathV1/ch/some-cm-handle/moduleResources"
            String jsonData = TestUtils.getResourceFileContent('GetModules.json')
        and: 'the DMI service returns the yang resources'
            ModuleReference moduleReference1 = new ModuleReference(name: 'ietf-yang-library', revision: '2016-06-21')
            ModuleReference moduleReference2 = new ModuleReference(name: 'nc-notifications', revision: '2008-07-14')
            def moduleReferences = [moduleReference1, moduleReference2]
            def yangResources = new YangResources()
            def yangResource = new YangResource(yangSource: '"some-data"', moduleName: 'NAME', revision: 'REVISION')
            yangResources.add(yangResource)
            mockDmiService.getModuleResources('some-cm-handle', moduleReferences) >> yangResources
        when: 'get module resource api is invoked'
            def response = mvc.perform(post(getModulesEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonData)).andReturn().response
        then: 'a OK status is returned'
            response.status == OK.value()
        and: 'the expected response is returned'
            response.getContentAsString() == '[{"yangSource":"\\"some-data\\"","moduleName":"NAME","revision":"REVISION"}]'
    }

    def 'Retrieve module resources with exception handling.'() {
        given: 'an endpoint and json data'
            def getModulesEndpoint = "$basePathV1/ch/some-cm-handle/moduleResources"
            String jsonData = TestUtils.getResourceFileContent('GetModules.json')
        and: 'the service method is invoked to get module resources and throws an exception'
            mockDmiService.getModuleResources('some-cm-handle', _) >> { throw Mock(ModuleResourceNotFoundException.class) }
        when: 'get module resource api is invoked'
            def response = mvc.perform(post(getModulesEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonData)).andReturn().response
        then: 'a not found status is returned'
            response.status == HttpStatus.NOT_FOUND.value()
    }

    def 'Get resource data for pass-through operational.'() {
        given: 'Get resource data url'
            def getResourceDataForCmHandleUrl = "${basePathV1}/ch/some-cmHandle/data/ds/ncmp-datastore:passthrough-operational" +
                    "?resourceIdentifier=parent/child&options=(fields=myfields,depth=5)"
            def json = '{"cmHandleProperties" : { "prop1" : "value1", "prop2" : "value2"}}'
        when: 'get resource data POST api is invoked'
            def response = mvc.perform(
                    post(getResourceDataForCmHandleUrl).contentType(MediaType.APPLICATION_JSON).content(json)
            ).andReturn().response
        then: 'response status is ok'
            response.status == OK.value()
        and: 'dmi service called with get resource data'
            1 * mockDmiService.getResourceData('some-cmHandle',
                    'parent/child',
                    '(fields=myfields,depth=5)',
                    'content=all')
    }

    def 'Get resource data for pass-through operational with bad request.'() {
        given: 'Get resource data url'
            def getResourceDataForCmHandleUrl = "${basePathV1}/ch/some-cmHandle/data/ds/ncmp-datastore:passthrough-operational" +
                "?resourceIdentifier=parent/child&options=(fields=myfields,depth=5)"
            def jsonData = TestUtils.getResourceFileContent('createDataWithNormalChar.json')
        when: 'get resource data POST api is invoked'
            def response = mvc.perform(
                post(getResourceDataForCmHandleUrl).contentType(MediaType.APPLICATION_JSON).content(jsonData)
            ).andReturn().response
        then: 'response status is bad request'
            response.status == BAD_REQUEST.value()
        and: 'dmi service is not invoked'
            0 * mockDmiService.getResourceData(*_)
    }

    def 'data with #scenario operation using passthrough running.'() {
        given: 'write data for passthrough running url and jsonData'
            def writeDataForPassthroughRunning = "${basePathV1}/ch/some-cmHandle/data/ds/ncmp-datastore:passthrough-running" +
                    "?resourceIdentifier=some-resourceIdentifier"
            def jsonData = TestUtils.getResourceFileContent(requestBodyFile)
        and: 'dmi service is called'
            mockDmiService.writeData(operationEnum, 'some-cmHandle',
                    'some-resourceIdentifier', dataType,
                    'normal request body' ) >> '{some-json}'
        when: 'write data for passthrough running post api is invoked with json data'
            def response = mvc.perform(
                    post(writeDataForPassthroughRunning).contentType(MediaType.APPLICATION_JSON)
                            .content(jsonData)
            ).andReturn().response
       then: 'response status is #expectedResponseStatus'
            response.status == expectedResponseStatus
        and: 'the data in the request body is as expected'
            response.getContentAsString() == expectedJsonResponse
        where: 'given request body and data'
            scenario   | requestBodyFile                 | operationEnum     | dataType                      || expectedResponseStatus | expectedJsonResponse
            'Create'   | 'createDataWithNormalChar.json' | CREATE            | 'application/json'            || CREATED.value()        | '{some-json}'
            'Update'   | 'updateData.json'               | UPDATE            | 'application/json'            || OK.value()             | '{some-json}'
            'Delete'   | 'deleteData.json'               | DELETE            | 'application/json'            || NO_CONTENT.value()     | '{some-json}'
            'Read'     | 'readData.json'                 | READ              | 'application/json'            || OK.value()             | ''
            'Patch'    | 'patchData.json'                | PATCH             | 'application/yang.patch+json' || OK.value()             | '{some-json}'
    }

    def 'Create data using passthrough for special characters.'(){
         given: 'create data for cmHandle url and JsonData'
            def writeDataForCmHandlePassthroughRunning = "${basePathV1}/ch/some-cmHandle/data/ds/ncmp-datastore:passthrough-running" +
             "?resourceIdentifier=some-resourceIdentifier"
            def jsonData = TestUtils.getResourceFileContent('createDataWithSpecialChar.json')
         and: 'dmi service is called'
            mockDmiService.writeData(CREATE, 'some-cmHandle', 'some-resourceIdentifier', 'application/json',
                'data with quote \" and new line \n') >> '{some-json}'
         when: 'create cmHandle passthrough running post api is invoked with json data with special chars'
            def response = mvc.perform(
                post(writeDataForCmHandlePassthroughRunning).contentType(MediaType.APPLICATION_JSON).content(jsonData)
            ).andReturn().response
         then: 'response status is CREATED'
            response.status == CREATED.value()
         and: 'the data in the request body is as expected'
            response.getContentAsString() == '{some-json}'
    }

    def 'PassThrough Returns OK when topic is used for async'(){
        given: 'an endpoint'
            def readPassThroughOperational ="${basePathV1}/ch/some-cmHandle/data/ds/ncmp-datastore:" +
                resourceIdentifier +
                '?resourceIdentifier=some-resourceIdentifier&topic=test-topic'
        when: 'endpoint is invoked'
            def jsonData = TestUtils.getResourceFileContent('readData.json')
            def response = mvc.perform(
                post(readPassThroughOperational).contentType(MediaType.APPLICATION_JSON).content(jsonData)
            ).andReturn().response
        then: 'response status is OK'
            assert response.status == HttpStatus.NO_CONTENT.value()
        where: 'the following values are used'
             resourceIdentifier << ['passthrough-operational', 'passthrough-running']
    }

    def 'Get resource data for pass-through running with #scenario value in resource identifier param.'() {
        given: 'Get resource data url'
            def getResourceDataForCmHandleUrl = "${basePathV1}/ch/some-cmHandle/data/ds/ncmp-datastore:passthrough-running" +
                    "?resourceIdentifier="+resourceIdentifier+"&options=(fields=myfields,depth=5)"
            def json = '{"cmHandleProperties" : { "prop1" : "value1", "prop2" : "value2"}}'
        when: 'get resource data POST api is invoked'
            def response = mvc.perform(
                    post(getResourceDataForCmHandleUrl).contentType(MediaType.APPLICATION_JSON).content(json)
            ).andReturn().response
        then: 'response status is ok'
            response.status == OK.value()
        and: 'dmi service called with get resource data for a cm handle'
            1 * mockDmiService.getResourceData('some-cmHandle',
                    resourceIdentifier,
                    '(fields=myfields,depth=5)',
                    'content=config')
        where: 'tokens are used in the resource identifier parameter'
            scenario                       | resourceIdentifier
            '/'                            | 'id/with/slashes'
            '?'                            | 'idWith?'
            ','                            | 'idWith,'
            '='                            | 'idWith='
            '[]'                           | 'idWith[]'
            '? needs to be encoded as %3F' | 'idWith%3F'

    }
}
