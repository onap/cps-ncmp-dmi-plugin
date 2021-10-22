/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Bell Canada
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

import org.springframework.http.HttpStatus;

/*
Use this exception when SDNC contract fails
 */
public class SdncException extends DmiException {

    private static final long serialVersionUID = -2076096996672060566L;

    /**
     * Constructor.
     *
     * @param message      message
     * @param httpStatus   httpStatus
     * @param responseBody responseBody
     */
    public SdncException(final String message, final HttpStatus httpStatus, final String responseBody) {
        super(message, String.format("sdnc http status: %s, response body : %s ",
            httpStatus.toString(),
            responseBody));
    }

    public SdncException(final String message, final String details, final Throwable cause) {
        super(message, details, cause);
    }

}
