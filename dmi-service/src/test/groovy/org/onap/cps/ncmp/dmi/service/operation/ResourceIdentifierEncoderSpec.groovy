/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2025 OpenInfra Foundation Europe. All rights reserved.
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

import spock.lang.Specification

class ResourceIdentifierEncoderSpec extends Specification {

    def 'encodeNestedResourcePath should handle valid paths correctly: #scenario'() {
        when: 'we encode a valid resource path'
            def result = ResourceIdentifierEncoder.encodeNestedResourcePath(input)
        then: 'the result matches expectedEncodedString encoded format'
            assert result == expectedEncodedString
        where: 'following scenarios are used'
            scenario                                     | input                                            || expectedEncodedString
            'simple path without leading path separator' | 'container'                                      || 'container'
            'list entry with space in key value'         | '/list-name=My Container/leaf=leaf with space'   || '/list-name=My%20Container/leaf=leaf%20with%20space'
            'key value containing path separator'        | '/container/list=id/with/slashes/leaf=Some Leaf' || '/container/list=id%2Fwith%2Fslashes/leaf=Some%20Leaf'
            'equals signs in key value'                  | '/list=Key=Value=Another'                        || '/list=Key%3DValue%3DAnother'
    }
}
