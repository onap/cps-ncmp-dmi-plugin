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

class ResourceIdentifierEncodingUtilitySpec extends Specification {

    def 'encodeNestedResourcePath should handle valid paths correctly: #scenario'() {
        when: 'we encode a valid resource path'
            def result = ResourceIdentifierEncodingUtility.encodeNestedResourcePath(input)
        then: 'the result matches expected encoded format'
            assert result == expected
        where: 'following scenarios are used'
            scenario                              | input                                                         || expected
            'bookstore-address with space in key' | '/bookstore-address=My Bookstore/address=221B Baker Street'   || '/bookstore-address=My%20Bookstore/address=221B%20Baker%20Street'
            'category code containing slashes'    | '/bookstore/categories=C1/id/with/slashes/books=Far Horizons' || '/bookstore/categories=C1%2Fid%2Fwith%2Fslashes/books=Far%20Horizons'
            'path without leading slash'          | 'bookstore/categories=My Category/books=My Book'              || 'bookstore/categories=My%20Category/books=My%20Book'
            'equals signs in value'               | '/books=Key=Value=Another'                                    || '/books=Key%3DValue%3DAnother'
            'value spanning multiple segments'    | '/books=Title/subtitle/chapter'                               || '/books=Title%2Fsubtitle%2Fchapter'
            'consecutive slashes with key-values' | '/bookstore//categories=C1//books=Test'                       || '/bookstore/categories=C1/books=Test'
            'null input'                          | null                                                          || null
            'empty string'                        | ''                                                            || ''
    }
}
