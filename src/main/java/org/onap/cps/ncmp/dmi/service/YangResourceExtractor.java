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

package org.onap.cps.ncmp.dmi.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.exception.ModuleResourceNotFoundException;
import org.onap.cps.ncmp.dmi.model.YangResource;
import org.onap.cps.ncmp.dmi.service.model.ModuleReference;
import org.springframework.http.ResponseEntity;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class YangResourceExtractor {

    static YangResource toYangResource(final ModuleReference moduleReference,
                                       final ResponseEntity<String> responseEntity) {
        final YangResource yangResource = new YangResource();
        yangResource.setModuleName(moduleReference.getName());
        yangResource.setRevision(moduleReference.getRevision());
        yangResource.setYangSource(extractYangSourceFromBody(responseEntity));
        return yangResource;
    }

    private static String extractYangSourceFromBody(final ResponseEntity<String> responseEntity) {
        final JsonObject responseBodyAsJsonObject = new Gson().fromJson(responseEntity.getBody(), JsonObject.class);
        final JsonObject monitoringOutputAsJsonObject =
            responseBodyAsJsonObject.getAsJsonObject("ietf-netconf-monitoring:output");
        if (monitoringOutputAsJsonObject == null
            || monitoringOutputAsJsonObject.getAsJsonPrimitive("data") == null) {
            log.error("Error occurred when trying to parse the response body from sdnc {}", responseEntity.getBody());
            throw new ModuleResourceNotFoundException(responseEntity.getBody(),
                "Error occurred when trying to parse the response body from sdnc.");
        }
        return monitoringOutputAsJsonObject.getAsJsonPrimitive("data").getAsString();
    }

}
