/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.ncmp.client;

import org.onap.cps.ncmp.model.AppConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class DmiRestClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AppConfiguration appConfiguration;

    public ResponseEntity<String> registerCmHandle(final String jsonInputString) {
        final var registerCmHandlesUri = buildCpsUrl("cps-ncmp/api/ncmp-dmi/v1/ch");
        final var headers = new HttpHeaders();
        headers.setBasicAuth(appConfiguration.getUsername(), appConfiguration.getPassword());
        headers.set("Content-type", "application/json");
        final var httpEntity = new HttpEntity<>(jsonInputString, headers);
        final var responseEntity =
            restTemplate.exchange(registerCmHandlesUri, HttpMethod.POST, httpEntity, String.class);
        return responseEntity;
    }

    private String buildCpsUrl(final String registerCmHandlePath) {
        final String cpsBaseUrl = appConfiguration.getCpsBaseUrl();
        return UriComponentsBuilder
            .fromHttpUrl(cpsBaseUrl)
            .path(registerCmHandlePath)
            .toUriString();
    }
}
