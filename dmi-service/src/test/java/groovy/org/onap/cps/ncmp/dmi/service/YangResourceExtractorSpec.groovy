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

package groovy.org.onap.cps.ncmp.dmi.service

import com.google.gson.JsonSyntaxException
import org.onap.cps.ncmp.dmi.exception.ModuleResourceNotFoundException
import org.onap.cps.ncmp.dmi.service.YangResourceExtractor
import org.onap.cps.ncmp.dmi.service.model.ModuleReference
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class YangResourceExtractorSpec extends Specification {

    static def BACK_SLASH = '\\';
    static def NEW_LINE = '\n';
    static def QUOTE = '"';
    static def TAB = '\t';

    static def YANG_ESCAPED_NEW_LINE = '\\n'
    static def YANG_ESCAPED_BACK_SLASH = '\\\\'
    static def YANG_ESCAPED_QUOTE = '\\"'
    static def YANG_ESCAPED_TAB = '\\t'

    static def SDNC_OUTPUT_JSON_NAME = '"ietf-netconf-monitoring:output"'

    def moduleReference = new ModuleReference(name: 'test', revision: 'rev')
    def responseEntity = Mock(ResponseEntity)

    def 'Extract yang resource with escaped characters in the source.'() {
        given: 'a response entity with a data field of value #jsonValue'
            responseEntity.getBody() >> '{' + SDNC_OUTPUT_JSON_NAME + ': { "data": "' + jsonValue + '" }}'
        when: 'the yang resource is extracted'
            def result = YangResourceExtractor.toYangResource(moduleReference, responseEntity)
        then: 'the yang source string is as expected'
            result.getYangSource() == expectedString
        where: 'the following data is used'
            jsonValue                                 || expectedString
            'line1' + YANG_ESCAPED_NEW_LINE + 'line2' || 'line1' + NEW_LINE + 'line2'
            'a' + YANG_ESCAPED_BACK_SLASH+'b'         || 'a'+BACK_SLASH +'b'
            'a' + YANG_ESCAPED_QUOTE + 'b'            || 'a'+QUOTE+'b'
            'a' + YANG_ESCAPED_TAB + 'b'              || 'a'+TAB+'b'
    }

    def 'Extract yang resource with escaped characters in the source inside escaped double quotes.'() {
        given: 'a response entity with a data field of value #jsonValue wrapped in escaped double quotes'
            responseEntity.getBody() >> '{' + SDNC_OUTPUT_JSON_NAME + ': { "data": "' + YANG_ESCAPED_QUOTE + jsonValue + YANG_ESCAPED_QUOTE + '" }}'
        when: 'the yang resource is extracted'
            def result = YangResourceExtractor.toYangResource(moduleReference, responseEntity)
        then: 'the yang source string is as expected'
            result.getYangSource() == expectedString
        where: 'the following data is used'
            jsonValue                                 || expectedString
            'line1' + YANG_ESCAPED_NEW_LINE + 'line2' || '"line1' + NEW_LINE + 'line2"'
            'a' + YANG_ESCAPED_BACK_SLASH+'b'         || '"a'+BACK_SLASH +'b"'
            'a' + YANG_ESCAPED_QUOTE + 'b'            || '"a'+QUOTE+'b"'
            'a' + YANG_ESCAPED_TAB + 'b'              || '"a'+TAB+'b"'
    }

    def 'Attempt to extract yang resource with un-escaped double quotes in the source.'() {
        given: 'a response entity with a data field with unescaped double quotes'
            responseEntity.getBody() >> '{' + SDNC_OUTPUT_JSON_NAME + ': { "data": "' + QUOTE + 'some data' + QUOTE + '" }}'
        when: 'Json is converted to String'
            YangResourceExtractor.toYangResource(moduleReference, responseEntity)
        then: 'the output of the method is equal to the output from the test template'
            thrown(JsonSyntaxException)
    }

    def 'Attempt to extract yang resource without #without.'() {
        given: 'a response entity with a body without #without'
            responseEntity.getBody() >> jsonBody
        when: 'Json is converted to String'
            YangResourceExtractor.toYangResource(moduleReference, responseEntity)
        then: 'the output of the method is equal to the output from the test template'
            thrown(ModuleResourceNotFoundException)
        where:
            without               | jsonBody
            'data'                | '{' + SDNC_OUTPUT_JSON_NAME + ': { "something": "else" }}'
            SDNC_OUTPUT_JSON_NAME | '{"something:else": { "data": "data" }}'
    }

}
