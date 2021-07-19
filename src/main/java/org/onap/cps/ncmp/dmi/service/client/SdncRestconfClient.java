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

package org.onap.cps.ncmp.dmi.service.client;

import org.onap.cps.ncmp.dmi.config.DmiConfiguration.SdncProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SdncRestconfClient {
    private SdncProperties sdncProperties;
    private RestTemplate restTemplate;

    public SdncRestconfClient(final SdncProperties sdncProperties, final RestTemplate restTemplate) {
        this.sdncProperties = sdncProperties;
        this.restTemplate = restTemplate;
    }

    /**
     * restconf get operation on sdnc.
     *
     * @param getResourceUrl sdnc get url
     *
     * @return the response entity
     */
    public ResponseEntity<String> getOperation(final String getResourceUrl) {
        final StringBuilder sdncBaseUrl = new StringBuilder(sdncProperties.getBaseUrl());
        sdncBaseUrl.append(getResourceUrl);
        final var httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(sdncProperties.getAuthUsername(), sdncProperties.getAuthPassword());
        httpHeaders.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON.toString());
        final var httpEntity = new HttpEntity<>(httpHeaders);
        return restTemplate.getForEntity(sdncBaseUrl.toString(), String.class, httpEntity);
    }
}
