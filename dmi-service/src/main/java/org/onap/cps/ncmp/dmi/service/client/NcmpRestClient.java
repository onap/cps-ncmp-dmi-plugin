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
     *
     * @param jsonData json data
     * @return the response entity
     */

    public ResponseEntity<String> registerCmHandlesWithNcmp(final String jsonData) {
        final String ncmpRegistrationUrl = buildNcmpRegistrationUrl();
        return performNcmpRequest(ncmpRegistrationUrl, HttpMethod.POST, jsonData);
    }

    /**
     * Enable NCMP data sync flag to enable data sync for a cmHandle with NCMP using a HTTP call.
     *
     * @param cmHandleId cm handle identifier
     * @return the response entity
     */
    public ResponseEntity<String> enableNcmpDataSync(final String cmHandleId) {
        final String dataSyncEnabledUrl = buildDataSyncEnabledUrl(cmHandleId);
        return performNcmpRequest(dataSyncEnabledUrl, HttpMethod.PUT, null);
    }

    private ResponseEntity<String> performNcmpRequest(final String ncmpUrl, final HttpMethod httpMethod,
            final String requestBody) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        final HttpEntity<String> httpEntity =
                (requestBody != null) ? new HttpEntity<>(requestBody, httpHeaders) : new HttpEntity<>(httpHeaders);
        return restTemplate.exchange(ncmpUrl, httpMethod, httpEntity, String.class);
    }

    private String buildNcmpRegistrationUrl() {
        return UriComponentsBuilder
            .fromUriString(cpsProperties.getBaseUrl())
            .path(cpsProperties.getDmiRegistrationUrl())
            .toUriString();
    }

    private String buildDataSyncEnabledUrl(final String cmHandleId) {
        final String dataSyncEnabledUrl = UriComponentsBuilder.fromUriString(cpsProperties.getBaseUrl())
                                                  .path(cpsProperties.getDataSyncEnabledUrl())
                                                  .buildAndExpand(cmHandleId)
                                                  .toUriString();
        log.debug("dataSyncEnabledUrl : {}", dataSyncEnabledUrl);
        return dataSyncEnabledUrl;
    }
}