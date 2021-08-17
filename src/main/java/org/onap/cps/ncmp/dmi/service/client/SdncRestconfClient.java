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
        return getOperation(getResourceUrl, new HttpHeaders());
    }

    /**
     * Overloaded restconf get operation on sdnc with http headers.
     *
     * @param getResourceUrl sdnc get url
     * @param httpHeaders http headers
     *
     * @return the response entity
     */
    public ResponseEntity<String> getOperation(final String getResourceUrl, final HttpHeaders httpHeaders) {
        final String sdncBaseUrl = sdncProperties.getBaseUrl();
        final String sdncRestconfUrl = sdncBaseUrl.concat(getResourceUrl);
        httpHeaders.setBasicAuth(sdncProperties.getAuthUsername(), sdncProperties.getAuthPassword());
        final var httpEntity = new HttpEntity<>(httpHeaders);
        return restTemplate.getForEntity(sdncRestconfUrl, String.class, httpEntity);
    }

    /**
     * restconf post operation on sdnc.
     *
     * @param postResourceUrl sdnc post resource url
     * @param jsonData        json data
     * @return the response entity
     */
    public ResponseEntity<String> postOperationWithJsonData(final String postResourceUrl,
                                                            final String jsonData) {
        final var sdncBaseUrl = sdncProperties.getBaseUrl();
        final var sdncRestconfUrl = sdncBaseUrl.concat(postResourceUrl);
        final var httpEntity = new HttpEntity<>(jsonData, configureHttpHeaders());
        return restTemplate.postForEntity(sdncRestconfUrl, httpEntity, String.class);
    }

    private HttpHeaders configureHttpHeaders() {
        final var httpHeaders = new HttpHeaders();
        httpHeaders.setBasicAuth(sdncProperties.getAuthUsername(), sdncProperties.getAuthPassword());
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return httpHeaders;
    }
}