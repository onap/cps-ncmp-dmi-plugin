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

package org.onap.cps.ncmp.dmi.rest.controller


import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.cps.ncmp.dmi.TestUtils
import org.onap.cps.ncmp.dmi.exception.DmiException
import org.onap.cps.ncmp.dmi.exception.ModuleResourceNotFoundException
import org.onap.cps.ncmp.dmi.exception.ModulesNotFoundException
import org.onap.cps.ncmp.dmi.model.ModuleReference
import org.onap.cps.ncmp.dmi.model.ModuleSchemaList
import org.onap.cps.ncmp.dmi.model.ModuleSet
import org.onap.cps.ncmp.dmi.model.ModuleSetSchemas
import org.onap.cps.ncmp.dmi.service.DmiService
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
class DmiRestControllerSpec extends Specification {

    @SpringBean
    DmiService mockDmiService = Mock()

    @SpringBean
    ObjectMapper mockObjectMapper = Spy()

    @Autowired
    private MockMvc mvc

    @Value('${rest.api.dmi-base-path}/v1')
    def basePathV1

    def 'Get all modules for given cm handle.'() {
        given: 'REST endpoint for getting all modules'
            def getModuleUrl = "$basePathV1/ch/node1/modules"
        and: 'get modules for cm-handle returns a json'
            def moduleSetSchema = new ModuleSetSchemas()
            moduleSetSchema.namespace('some-namespace')
            moduleSetSchema.moduleName('some-moduleName')
            moduleSetSchema.revision('some-revision')
            def moduleSetSchemasList = [moduleSetSchema] as List<ModuleSetSchemas>
            def moduleSet = new ModuleSet()
            moduleSet.schemas(moduleSetSchemasList)
            mockDmiService.getModulesForCmHandle('node1') >> moduleSet
        when: 'post is being called'
            def response = mvc.perform(post(getModuleUrl)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andReturn().response
        then: 'status is OK'
            response.status == HttpStatus.OK.value()
        and: 'the response content matches the result from the DMI service'
            response.getContentAsString() == '{"schemas":[{"moduleName":"some-moduleName","revision":"some-revision","namespace":"some-namespace"}]}'
    }

    def 'Get all modules for given cm handle with invalid json.'() {
        given: 'REST endpoint for getting all modules'
            def getModuleUrl = "$basePathV1/ch/node1/modules"
        and: 'get modules for cmHandle throws an exception'
            mockObjectMapper.readValue(_ as String, _ as ModuleSchemaList) >> { throw Mock(DmiException.class) }
            mockDmiService.getModulesForCmHandle('node1') >> 'some-value'
        when: 'post is being called'
            def response = mvc.perform(post(getModuleUrl)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andReturn().response
        then: 'the status is as expected'
            response.status == HttpStatus.INTERNAL_SERVER_ERROR.value()
    }

    def 'Get all modules for given cm handle with exception handling of #scenario.'() {
        given: 'REST endpoint for getting all modules'
            def getModuleUrl = "$basePathV1/ch/node1/modules"
        and: 'get modules for cm-handle throws #exceptionClass'
            mockDmiService.getModulesForCmHandle('node1') >> { throw Mock(exceptionClass) }
        when: 'post is invoked'
            def response = mvc.perform( post(getModuleUrl)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andReturn().response
        then: 'response status is #expectedResponse'
            response.status == expectedResponse
        where: 'the scenario is #scenario'
            scenario                       |  exceptionClass                 || expectedResponse
            'dmi service exception'        |  DmiException.class             || HttpStatus.INTERNAL_SERVER_ERROR.value()
            'no modules found'             |  ModulesNotFoundException.class || HttpStatus.NOT_FOUND.value()
            'any other runtime exception'  |  RuntimeException.class         || HttpStatus.INTERNAL_SERVER_ERROR.value()
    }

    def 'Register given list of cm handles.'() {
        given: 'register cm handle url and cm handles json'
            def registerCmhandlesPost = "${basePathV1}/inventory/cmHandles"
            def cmHandleJson = '{"cmHandles":["node1", "node2"]}'
        when: 'post register cm handles api is invoked'
            def response = mvc.perform(
                    post(registerCmhandlesPost)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(cmHandleJson)
            ).andReturn().response
        then: 'register cm handles in dmi service is called once'
            1 * mockDmiService.registerCmHandles(_ as List<String>)
        and: 'response status is created'
            response.status == HttpStatus.CREATED.value()
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
            response.status == HttpStatus.BAD_REQUEST.value()
        and: 'dmi service is not called'
            0 * mockDmiService.registerCmHandles(_)
    }

    def 'Retrieve module resources.'() {
        given: 'an endpoint and json data'
            def getModulesEndpoint = "$basePathV1/ch/some-cm-handle/moduleResources"
            def jsonData = TestUtils.getResourceFileContent('GetModules.json')
        and: 'the DMI service returns some json data'
            ModuleReference moduleReference1 = new ModuleReference()
            moduleReference1.name = 'ietf-yang-library'
            moduleReference1.revision = '2016-06-21'
            ModuleReference moduleReference2 = new ModuleReference()
            moduleReference2.name = 'nc-notifications'
            moduleReference2.revision = '2008-07-14'
            def moduleReferences = [moduleReference1, moduleReference2]
            mockDmiService.getModuleResources('some-cm-handle', moduleReferences) >> '{some-json}'
        when: 'get module resource api is invoked'
            def response = mvc.perform(post(getModulesEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonData)).andReturn().response
        then: 'a OK status is returned'
            response.status == HttpStatus.OK.value()
        and: 'the expected response is returned'
            response.getContentAsString() == '{some-json}'
    }

    def 'Retrieve module resources with exception handling.'() {
        given: 'an endpoint and json data'
            def getModulesEndpoint = "$basePathV1/ch/some-cm-handle/moduleResources"
            def jsonData = TestUtils.getResourceFileContent('GetModules.json')
        and: 'the service method is invoked to get module resources and throws an exception'
            mockDmiService.getModuleResources('some-cm-handle', _) >> { throw Mock(ModuleResourceNotFoundException.class) }
        when: 'get module resource api is invoked'
            def response = mvc.perform(post(getModulesEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonData)).andReturn().response
        then: 'a not found status is returned'
            response.status == HttpStatus.NOT_FOUND.value()
    }

    def 'Get resource data for pass-through operational from cm handle.'() {
        given: 'Get resource data url'
            def getResourceDataForCmHandleUrl = "${basePathV1}/ch/some-cmHandle/data/ds/ncmp-datastore:passthrough-operational" +
                    "/resourceIdentifier?fields=myfields&depth=5"
            def json = '{"cmHandleProperties" : { "prop1" : "value1", "prop2" : "value2"}}'
        when: 'get resource data PUT api is invoked'
            def response = mvc.perform(
                    put(getResourceDataForCmHandleUrl).contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON).content(json)
            ).andReturn().response
        then: 'response status is ok'
            response.status == HttpStatus.OK.value()
        and: 'dmi service called with get resource data for cm handle'
            1 * mockDmiService.getResourceDataOperationalForCmHandle('some-cmHandle',
                    'resourceIdentifier',
                    'application/json',
                    'myfields',
                    5,
                    ['prop1': 'value1', 'prop2': 'value2'])
    }

    def 'Write data using passthrough running for a cm handle using #scenario.'() {
        given: 'write data for cmHandle url and jsonData'
            def writeDataforCmHandlePassthroughRunning = "${basePathV1}/ch/some-cmHandle/data/ds/ncmp-datastore:passthrough-running/some-resourceIdentifier"
            def jsonData = TestUtils.getResourceFileContent(requestBodyFile)
        and: 'dmi service is called'
            mockDmiService.writeResourceDataPassthroughForCmHandle('some-cmHandle',
                    'some-resourceIdentifier', 'application/json',
                    expectedRequestData) >> '{some-json}'
        when: 'write cmHandle passthrough running post api is invoked with json data'
            def response = mvc.perform(
                    post(writeDataforCmHandlePassthroughRunning).contentType(MediaType.APPLICATION_JSON)
                            .content(jsonData)
            ).andReturn().response
       then: 'response status is 201 CREATED'
            response.status == HttpStatus.CREATED.value()
        and: 'the data in the request body is as expected'
            response.getContentAsString() == '{some-json}'
        where: 'given request body and data'
            scenario                  | requestBodyFile           || expectedRequestData
            'data with normal chars'  | 'dataWithNormalChar.json' || 'normal request body'
            'data with special chars' | 'dataWithSpecialChar.json'|| 'data with quote \" and new line \n'
    }

    def 'Get resource data for pass-through running from cm handle.'() {
        given: 'Get resource data url'
            def getResourceDataForCmHandleUrl = "${basePathV1}/ch/some-cmHandle/data/ds/ncmp-datastore:passthrough-running" +
                     "/testResourceIdentifier?fields=testFields&depth=5"
            def json = '{"cmHandleProperties" : { "prop1" : "value1", "prop2" : "value2"}}'
        when: 'get resource data PUT api is invoked'
            def response = mvc.perform(
                    put(getResourceDataForCmHandleUrl).contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON).content(json)
            ).andReturn().response
        then: 'response status is ok'
            response.status == HttpStatus.OK.value()
        and: 'dmi service called with get resource data for cm handle'
            1 * mockDmiService.getResourceDataPassThroughRunningForCmHandle('some-cmHandle',
                    'testResourceIdentifier',
                    'application/json',
                    'testFields',
                    5,
                    ['prop1':'value1', 'prop2':'value2'])
    }
}