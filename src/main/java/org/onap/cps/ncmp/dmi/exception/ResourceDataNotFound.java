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

package org.onap.cps.ncmp.dmi.exception;

public class ResourceDataNotFound extends DmiException {

    private static final long serialVersionUID = 881438585188332404L;

    private static final String ERROR_MESSAGE = "Resource data not found for the given cm-handles: ";

    /**
     * Constructor.
     *
     * @param cmHandle cmHandle identifier
     * @param details the error details
     */
    public ResourceDataNotFound(final String cmHandle, final String details) {
        super(ERROR_MESSAGE + cmHandle, details);
    }
}
