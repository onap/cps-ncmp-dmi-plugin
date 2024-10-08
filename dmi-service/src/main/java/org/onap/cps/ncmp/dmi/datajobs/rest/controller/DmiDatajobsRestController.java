/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2024 Nordix Foundation
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

package org.onap.cps.ncmp.dmi.datajobs.rest.controller;

import org.onap.cps.ncmp.dmi.datajobs.model.SubjobReadRequest;
import org.onap.cps.ncmp.dmi.datajobs.model.SubjobWriteRequest;
import org.onap.cps.ncmp.dmi.datajobs.rest.api.DmiDatajobApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("${rest.api.dmi-base-path}")
@RestController
public class DmiDatajobsRestController implements DmiDatajobApi {
    /**
     * This method is not implemented for ONAP DMI plugin.
     *
     * @param subjobReadRequest        Operation body (optional)
     * @return (@ code ResponseEntity) Response entity
     */
    @Override
    public ResponseEntity<Void> readDataJob(final String destination, final SubjobReadRequest subjobReadRequest) {

        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * This method is not implemented for ONAP DMI plugin.
     *
     * @param subjobWriteRequest       Operation body (optional)
     * @return (@ code ResponseEntity) Response entity
     */
    @Override
    public ResponseEntity<Void> writeDataJob(final String destination, final SubjobWriteRequest subjobWriteRequest) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * This method is not implemented for ONAP DMI plugin.
     *
     * @param dataProducerJobId     Identifier for the data producer job (required)
     * @param dataProducerId        Identifier for the data producer (required)
     * @return ResponseEntity       Response entity indicating the method is not implemented
     */
    @Override
    public ResponseEntity<Void> getDataJobStatus(final String dataProducerJobId,
                                                 final String dataProducerId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    /**
     * This method is not implemented for ONAP DMI plugin.
     *
     * @param dataProducerId        Identifier for the data producer as a query parameter (required)
     * @param dataProducerJobId     Identifier for the data producer job (required)
     * @param destination           The destination of the results, Kafka topic name or s3 bucket name (required)
     * @return ResponseEntity       Response entity indicating the method is not implemented
     */
    @Override
    public ResponseEntity<Void> getDataJobResult(final String dataProducerId,
                                                 final String dataProducerJobId,
                                                 final String destination) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
