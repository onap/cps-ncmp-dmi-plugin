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
 *  ============LICENSE_END=========================================================
 */

package org.onap.cps.ncmp.dmi.service.client;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.onap.cps.ncmp.dmi.config.DmiConfiguration.SdncProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SdncRestconfClient {

    private final SdncProperties sdncProperties;
    private final RestTemplate restTemplate;

    /**
     * restconf get operation on sdnc.
     *
     * @param getResourceUrl sdnc get url
     * @return the response entity
     */
    public ResponseEntity<String> getOperation(final String getResourceUrl) {
        return getOperation(getResourceUrl, new HttpHeaders());
    }

    /**
     * Overloaded restconf get operation on sdnc with http headers.
     *
     * @param getResourceUrl sdnc get url
     * @param httpHeaders    http headers
     * @return the response entity
     */
    public ResponseEntity<String> getOperation(final String getResourceUrl, final HttpHeaders httpHeaders) {
        return  httpOperationWithJsonData(HttpMethod.GET, getResourceUrl, null, httpHeaders);
    }

    /**
     * restconf http operations on sdnc.
     *
     * @param httpMethod HTTP Method
     * @param resourceUrl sdnc resource url
     * @param jsonData json data
     * @param httpHeaders HTTP Headers
     * @return response entity
     */
    public ResponseEntity<String> httpOperationWithJsonData(final HttpMethod httpMethod,
                                                            final String resourceUrl,
                                                            final String jsonData,
                                                            final HttpHeaders httpHeaders) {
        return executeHttpOperation(httpMethod, resourceUrl, jsonData, httpHeaders);
    }

    private ResponseEntity<String> executeHttpOperation(final HttpMethod httpMethod, final String resourceUrl,
            final String jsonData, final HttpHeaders httpHeaders) {
        final String sdncBaseUrl = sdncProperties.getBaseUrl();
        final String sdncRestconfUrl = sdncBaseUrl.concat(resourceUrl);
        httpHeaders.setBasicAuth(sdncProperties.getAuthUsername(), sdncProperties.getAuthPassword());

        final HttpEntity<String> httpEntity =
                jsonData == null ? new HttpEntity<>(httpHeaders) : new HttpEntity<>(jsonData, httpHeaders);

        final URI sdncRestconfUri = URI.create(sdncRestconfUrl);
        log.debug("sdncRestconfUri: {}", sdncRestconfUri);
        return restTemplate.exchange(sdncRestconfUri, httpMethod, httpEntity, String.class);

    }
}
