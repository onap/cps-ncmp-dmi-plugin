/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2023 Nordix Foundation
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

package groovy.org.onap.cps.ncmp.dmi.rest.controller

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.config.WebSecurityConfig
import org.onap.cps.ncmp.dmi.exception.DmiException
import org.onap.cps.ncmp.dmi.exception.ModuleResourceNotFoundException
import org.onap.cps.ncmp.dmi.exception.ModulesNotFoundException
import org.onap.cps.ncmp.dmi.model.ModuleSet
import org.onap.cps.ncmp.dmi.model.ModuleSetSchemasInner
import org.onap.cps.ncmp.dmi.model.YangResource
import org.onap.cps.ncmp.dmi.model.YangResources
import org.onap.cps.ncmp.dmi.notifications.async.AsyncTaskExecutor
import org.onap.cps.ncmp.dmi.notifications.async.DmiAsyncRequestResponseEventProducer
import org.onap.cps.ncmp.dmi.rest.controller.DmiRestController
import org.onap.cps.ncmp.dmi.service.DmiService
import org.onap.cps.ncmp.dmi.service.model.ModuleReference
import org.slf4j.LoggerFactory
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.onap.cps.ncmp.dmi.model.DataAccessRequest.OperationEnum.*
import static org.springframework.http.HttpStatus.*
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@Import(WebSecurityConfig)
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

    def logger = Spy(ListAppender<ILoggingEvent>)

    void setup() {
        ((Logger) LoggerFactory.getLogger(DmiRestController.class)).addAppender(logger)
        logger.start()
    }

    void cleanup() {
        ((Logger) LoggerFactory.getLogger(DmiRestController.class)).detachAndStopAllAppenders()
    }

    @Value('${rest.api.dmi-base-path}/v1')
    def basePathV1

    def 'Get all modules.'() {
        given: 'URL for getting all modules and some request data'
            def getModuleUrl = "$basePathV1/ch/node1/modules"
            def someValidJson = '{}'
        and: 'DMI service returns some module'
            def moduleSetSchema = new ModuleSetSchemasInner(namespace:'some-namespace',
                    moduleName:'some-moduleName',
                    revision:'some-revision')
            def moduleSetSchemasList = [moduleSetSchema] as List<ModuleSetSchemasInner>
            def moduleSet = new ModuleSet()
            moduleSet.schemas(moduleSetSchemasList)
            mockDmiService.getModulesForCmHandle('node1') >> moduleSet
        when: 'the request is posted'
            def response = mvc.perform(post(getModuleUrl)
                    .contentType(MediaType.APPLICATION_JSON).content(someValidJson))
                    .andReturn().response
        then: 'status is OK'
            response.status == OK.value()
        and: 'the response content matches the result from the DMI service'
            response.getContentAsString() == '{"schemas":[{"moduleName":"some-moduleName","revision":"some-revision","namespace":"some-namespace"}]}'
    }

    def 'Get all modules with exception handling of #scenario.'() {
        given: 'URL for getting all modules and some request data'
            def getModuleUrl = "$basePathV1/ch/node1/modules"
            def someValidJson = '{}'
        and: 'a #exception is thrown during the process'
            mockDmiService.getModulesForCmHandle('node1') >> { throw exception }
        when: 'the request is posted'
            def response = mvc.perform( post(getModuleUrl)
                    .contentType(MediaType.APPLICATION_JSON).content(someValidJson))
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
        given: 'register cm handle url and cmHandles'
            def registerCmhandlesPost = "${basePathV1}/inventory/cmHandles"
            def cmHandleJson = '{"cmHandles":["node1", "node2"]}'
        when: 'the request is posted'
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
        when: 'the request is posted'
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
        given: 'URL to get module resources'
            def getModulesEndpoint = "$basePathV1/ch/some-cm-handle/moduleResources"
        and: 'request data to get some modules'
            String jsonData = TestUtils.getResourceFileContent('moduleResources.json')
        and: 'the DMI service returns the yang resources'
            ModuleReference moduleReference1 = new ModuleReference(name: 'ietf-yang-library', revision: '2016-06-21')
            ModuleReference moduleReference2 = new ModuleReference(name: 'nc-notifications', revision: '2008-07-14')
            def moduleReferences = [moduleReference1, moduleReference2]
            def yangResources = new YangResources()
            def yangResource = new YangResource(yangSource: '"some-data"', moduleName: 'NAME', revision: 'REVISION')
            yangResources.add(yangResource)
            mockDmiService.getModuleResources('some-cm-handle', moduleReferences) >> yangResources
        when: 'the request is posted'
            def response = mvc.perform(post(getModulesEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonData)).andReturn().response
        then: 'a OK status is returned'
            response.status == OK.value()
        and: 'the response content matches the result from the DMI service'
            response.getContentAsString() == '[{"yangSource":"\\"some-data\\"","moduleName":"NAME","revision":"REVISION"}]'
    }

    def 'Retrieve module resources with exception handling.'() {
        given: 'URL to get module resources'
            def getModulesEndpoint = "$basePathV1/ch/some-cm-handle/moduleResources"
        and: 'request data to get some modules'
            String jsonData = TestUtils.getResourceFileContent('moduleResources.json')
        and: 'the system throws a not-found exception (during the processing)'
            mockDmiService.getModuleResources('some-cm-handle', _) >> { throw Mock(ModuleResourceNotFoundException.class) }
        when: 'the request is posted'
            def response = mvc.perform(post(getModulesEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonData)).andReturn().response
        then: 'a not found status is returned'
            response.status == HttpStatus.NOT_FOUND.value()
    }

    def 'Retrieve module resources and ensure module set tag is logged.'() {
        given: 'URL to get module resources'
            def getModulesEndpoint = "$basePathV1/ch/some-cm-handle/moduleResources"
        and: 'request data to get some modules'
            String jsonData = TestUtils.getResourceFileContent('moduleResources.json')
        and: 'the DMI service returns the yang resources'
            def moduleReferences = []
            def yangResources = new YangResources()
            def yangResource = new YangResource()
            yangResources.add(yangResource)
            mockDmiService.getModuleResources('some-cm-handle', moduleReferences) >> yangResources
        when: 'the request is posted'
            mvc.perform(post(getModulesEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonData))
        then: 'the module set tag is logged'
            def loggingMessage = getLoggingMessage(0)
            assert loggingMessage.contains('module-set-tag1')
    }

    def 'Get resource data for pass-through operational.'() {
        given: 'Get resource data url and some request data'
            def getResourceDataForCmHandleUrl = "${basePathV1}/ch/some-cmHandle/data/ds/ncmp-datastore:passthrough-operational" +
                    "?resourceIdentifier=parent/child&options=(fields=myfields,depth=5)"
            def someValidJson = '{}'
        when: 'the request is posted'
            def response = mvc.perform(
                    post(getResourceDataForCmHandleUrl).contentType(MediaType.APPLICATION_JSON).content(someValidJson)
            ).andReturn().response
        then: 'response status is ok'
            response.status == OK.value()
        and: 'dmi service method to get resource data is invoked once'
            1 * mockDmiService.getResourceData('some-cmHandle',
                    'parent/child',
                    '(fields=myfields,depth=5)',
                    'content=all')
    }

    def 'Get resource data for pass-through operational with write request (invalid).'() {
        given: 'Get resource data url'
            def getResourceDataForCmHandleUrl = "${basePathV1}/ch/some-cmHandle/data/ds/ncmp-datastore:passthrough-operational" +
                    "?resourceIdentifier=parent/child&options=(fields=myfields,depth=5)"
        and: 'an invalid write request data for "create" operation'
            def jsonData = '{"operation":"create"}'
        when: 'the request is posted'
            def response = mvc.perform(
                    post(getResourceDataForCmHandleUrl).contentType(MediaType.APPLICATION_JSON).content(jsonData)
            ).andReturn().response
        then: 'response status is bad request'
            response.status == BAD_REQUEST.value()
        and: 'dmi service is not invoked'
            0 * mockDmiService.getResourceData(*_)
    }

    def 'Get resource data for invalid datastore'() {
        given: 'Get resource data url'
            def getResourceDataForCmHandleUrl = "${basePathV1}/ch/some-cmHandle/data/ds/dummy-datastore" +
                "?resourceIdentifier=parent/child&options=(fields=myfields,depth=5)"
        and: 'an invalid write request data for "create" operation'
            def jsonData = '{"operation":"create"}'
        when: 'the request is posted'
            def response = mvc.perform(
                post(getResourceDataForCmHandleUrl).contentType(MediaType.APPLICATION_JSON).content(jsonData)
            ).andReturn().response
        then: 'response status is internal server error'
            response.status == INTERNAL_SERVER_ERROR.value()
        and: 'response contains expected error message'
            response.contentAsString.contains('dummy-datastore is an invalid datastore name')
    }

    def 'data with #scenario operation using passthrough running.'() {
        given: 'write data for passthrough running url'
            def writeDataForPassthroughRunning = "${basePathV1}/ch/some-cmHandle/data/ds/ncmp-datastore:passthrough-running" +
                    "?resourceIdentifier=some-resourceIdentifier"
        and: 'request data for #scenario'
            def jsonData = TestUtils.getResourceFileContent(requestBodyFile)
        and: 'dmi service is called'
            mockDmiService.writeData(operationEnum, 'some-cmHandle',
                    'some-resourceIdentifier', dataType,
                    'normal request body' ) >> '{some-json}'
        when: 'the request is posted'
            def response = mvc.perform(
                    post(writeDataForPassthroughRunning).contentType(MediaType.APPLICATION_JSON)
                            .content(jsonData)
            ).andReturn().response
        then: 'response status is #expectedResponseStatus'
            response.status == expectedResponseStatus
        and: 'the response content matches the result from the DMI service'
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
        given: 'create data for cmHandle url'
            def writeDataForCmHandlePassthroughRunning = "${basePathV1}/ch/some-cmHandle/data/ds/ncmp-datastore:passthrough-running" +
                    "?resourceIdentifier=some-resourceIdentifier"
        and: 'request data with special characters'
            def jsonData = TestUtils.getResourceFileContent('createDataWithSpecialChar.json')
        and: 'dmi service returns data'
            mockDmiService.writeData(CREATE, 'some-cmHandle', 'some-resourceIdentifier', 'application/json',
                    'data with quote \" and new line \n') >> '{some-json}'
        when: 'the request is posted'
            def response = mvc.perform(
                    post(writeDataForCmHandlePassthroughRunning).contentType(MediaType.APPLICATION_JSON).content(jsonData)
            ).andReturn().response
        then: 'response status is CREATED'
            response.status == CREATED.value()
        and: 'the response content matches the result from the DMI service'
            response.getContentAsString() == '{some-json}'
    }

    def 'PassThrough Returns OK when topic is used for async'(){
        given: 'Passthrough read URL and request data  with a topic (parameter)'
            def readPassThroughUrl ="${basePathV1}/ch/some-cmHandle/data/ds/ncmp-datastore:" +
                    resourceIdentifier +
                    '?resourceIdentifier=some-resourceIdentifier&topic=test-topic'
            def jsonData = TestUtils.getResourceFileContent('readData.json')
        when: 'the request is posted'
            def response = mvc.perform(
                    post(readPassThroughUrl).contentType(MediaType.APPLICATION_JSON).content(jsonData)
            ).andReturn().response
        then: 'response status is OK'
            assert response.status == HttpStatus.NO_CONTENT.value()
        where: 'the following values are used'
            resourceIdentifier << ['passthrough-operational', 'passthrough-running']
    }

    def 'PassThrough logs module set tag'(){
        given: 'Passthrough read URL and request data with a module set tag (parameter)'
            def readPassThroughUrl ="${basePathV1}/ch/some-cmHandle/data/ds/ncmp-datastore:" +
                'passthrough-running?resourceIdentifier=some-resourceIdentifier'
            def jsonData = TestUtils.getResourceFileContent('readData.json')
        when: 'the request is posted'
            mvc.perform(
                post(readPassThroughUrl).contentType(MediaType.APPLICATION_JSON).content(jsonData))
        then: 'response status is OK'
            def loggingMessage = getLoggingMessage(0)
            assert loggingMessage.contains('module-set-tag-example')
    }

    def 'Get resource data for pass-through running with #scenario value in resource identifier param.'() {
        given: 'Get resource data url'
            def getResourceDataForCmHandleUrl = "${basePathV1}/ch/some-cmHandle/data/ds/ncmp-datastore:passthrough-running" +
                    "?resourceIdentifier="+resourceIdentifier+"&options=(fields=myfields,depth=5)"
        and: 'some valid json data'
            def json = '{"cmHandleProperties" : { "prop1" : "value1", "prop2" : "value2"}}'
        when: 'the request is posted'
            def response = mvc.perform(
                    post(getResourceDataForCmHandleUrl).contentType(MediaType.APPLICATION_JSON).content(json)
            ).andReturn().response
        then: 'response status is ok'
            response.status == OK.value()
        and: 'dmi service method to get resource data is invoked once with correct parameters'
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

    def 'Execute a data operation for a list of operations.'() {
        given: 'an endpoint for a data operation request with list of cmhandles in request body'
            def resourceDataUrl = "$basePathV1/data?topic=client-topic-name&requestId=some-requestId"
        and: 'list of operation details are received into request body'
            def dataOperationRequestBody = '[{"operation": "read", "operationId": "14", "datastore": "ncmp-datastore:passthrough-operational", "options": "some options", "resourceIdentifier": "some resourceIdentifier",' +
                '"cmHandles": [ {"id": "cmHandle123", "moduleSetTag": "module-set-tag1", "cmHandleProperties": { "myProp`": "some value", "otherProp": "other value"}}]}]'
        when: 'the dmi resource data for dataOperation api is called.'
            def response = mvc.perform(
                post(resourceDataUrl).contentType(MediaType.APPLICATION_JSON).content(dataOperationRequestBody)
            ).andReturn().response
        then: 'the resource data operation endpoint returns the not implemented response'
            assert response.status == 501
        and: 'the job details are correctly received (logged)'
            assert getLoggingMessage(1).contains('some-requestId')
            assert getLoggingMessage(2).contains('client-topic-name')
            assert getLoggingMessage(4).contains('some resourceIdentifier')
            assert getLoggingMessage(5).contains('module-set-tag1')
        and: 'the operation Id is correctly received (logged)'
            assert getLoggingMessage(6).contains('14')
    }

    def getLoggingMessage(int index) {
        return logger.list[index].formattedMessage
    }
}