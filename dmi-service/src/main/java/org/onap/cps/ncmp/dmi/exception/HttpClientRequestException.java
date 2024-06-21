/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2022 Nordix Foundation
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

package org.onap.cps.ncmp.dmi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class HttpClientRequestException extends DmiException {

    private static final long serialVersionUID = 881438585188332404L;

    private final HttpStatus httpStatus;

    /**
     * Constructor.
     *
     * @param cmHandle cmHandle identifier
     * @param details    response body from the client available as details
     * @param httpStatus http status from the client
     */
    public HttpClientRequestException(final String cmHandle, final String details, final HttpStatus httpStatus) {
        super("Resource data request failed for CM Handle: " + cmHandle, details);
        this.httpStatus = httpStatus;
    }
}
