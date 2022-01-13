/*
 * ============LICENSE_START=======================================================
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

package org.onap.cps.ncmp.dmi.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Provides access to cps base url and cps authentication.
 */
@Configuration
public class DmiConfiguration {

    private static final int TIMEOUT = 500;

    @Getter
    @Component
    public static class CpsProperties {

        @Value("${cps-core.baseUrl}")
        private String baseUrl;
        @Value("${cps-core.dmiRegistrationUrl}")
        private String dmiRegistrationUrl;
        @Value("${cps-core.auth.username}")
        private String authUsername;
        @Value("${cps-core.auth.password}")
        private String authPassword;
    }

    @Getter
    @Component
    public static class SdncProperties {

        @Value("${sdnc.baseUrl}")
        private String baseUrl;
        @Value("${sdnc.auth.username}")
        private String authUsername;
        @Value("${sdnc.auth.password}")
        private String authPassword;
        @Value("${sdnc.topologyId}")
        public String topologyId;
    }

    /**
     * Returns restTemplate bean for the spring context.
     *
     * @param restTemplateBuilder   restTemplate builder
     * @return {@code RestTemplate} rest template
     */
    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder) {
        final RestTemplate restTemplate =  restTemplateBuilder.build();
        final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(TIMEOUT);
        requestFactory.setReadTimeout(TIMEOUT);

        restTemplate.setRequestFactory(requestFactory);
        return restTemplate;
    }
}