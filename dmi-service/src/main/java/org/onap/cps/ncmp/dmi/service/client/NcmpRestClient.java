/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2021-2025 OpenInfra Foundation Europe. All rights reserved.
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

package org.onap.cps.ncmp.dmi.service.client;

import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.config.DmiConfiguration.CpsProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class NcmpRestClient {

    private CpsProperties cpsProperties;
    private RestTemplate restTemplate;

    public NcmpRestClient(final CpsProperties cpsProperties, final RestTemplate restTemplate) {
        this.cpsProperties = cpsProperties;
        this.restTemplate = restTemplate;
    }

    /**
     * Register a cmHandle with NCMP using a HTTP call.
     * @param jsonData json data
     * @return the response entity
     */
    public ResponseEntity<String> registerCmHandlesWithNcmp(final String jsonData) {
        final String ncmpRegistrationUrl = buildNcmpRegistrationUrl();
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(cpsProperties.getAuthUsername(), cpsProperties.getAuthPassword());
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        final HttpEntity<String> httpEntity = new HttpEntity<>(jsonData, httpHeaders);
        return restTemplate.exchange(ncmpRegistrationUrl, HttpMethod.POST, httpEntity, String.class);
    }

    /**
     * Register a cmHandle with NCMP using a HTTP call.
     *
     * @param cmHandleId cm handle identifier
     * @return the response entity
     */
    public ResponseEntity<String> enabledDataSyncFlagWithNcmp(final String cmHandleId) {
        final String ncmpDataSyncEnabledUrl = buildDataSyncEnabledUrl(cmHandleId);
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        final HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
        return restTemplate.exchange(ncmpDataSyncEnabledUrl, HttpMethod.PUT, httpEntity, String.class);
    }

    private String buildNcmpRegistrationUrl() {
        return UriComponentsBuilder
            .fromUriString(cpsProperties.getBaseUrl())
            .path(cpsProperties.getDmiRegistrationUrl())
            .toUriString();
    }

    private String buildDataSyncEnabledUrl(final String cmHandleId) {
        final String dataSyncEnabledUrl = UriComponentsBuilder.fromUriString(cpsProperties.getBaseUrl())
                                                  .path(cpsProperties.getDataSyncEnabledUrl()).path("/")
                                                  .path(cmHandleId).path("/").path("data-sync")
                                                  .queryParam("dataSyncEnabled", Boolean.TRUE).toUriString();
        log.debug("dataSyncEnabledUrl : {}", dataSyncEnabledUrl);
        return dataSyncEnabledUrl;
    }
}