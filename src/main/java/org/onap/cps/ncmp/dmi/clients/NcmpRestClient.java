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
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.ncmp.dmi.clients;

import org.onap.cps.ncmp.config.CpsConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class NcmpRestClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CpsConfiguration cpsConfiguration;

    public ResponseEntity<String> registerCmHandlesWithNcmp(final String jsonData) {
        final var ncmpRegistrationUrl = buildNcmpRegistrationUrl();
        final var httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(cpsConfiguration.getAuthUsername(), cpsConfiguration.getAuthPassword());
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        final var httpEntity = new HttpEntity<>(jsonData, httpHeaders);
        return restTemplate.postForEntity(ncmpRegistrationUrl, httpEntity, String.class);
    }

    private String buildNcmpRegistrationUrl() {
        return UriComponentsBuilder
            .fromHttpUrl(cpsConfiguration.getBaseUrl())
            .path(cpsConfiguration.getDmiRegistrationUrl())
            .toUriString();
    }
}