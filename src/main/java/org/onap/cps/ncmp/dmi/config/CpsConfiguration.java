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
import org.springframework.context.annotation.Configuration;

/**
 * Provides access to cps base url and cps authentication.
 */
@Getter
@Configuration
public class CpsConfiguration {

    private String baseUrl;

    private String dmiRegistrationUrl;

    private String authUsername;

    private String authPassword;

    public CpsConfiguration(@Value("${cps-core.baseUrl}") final String baseUrl,
        @Value("${cps-core.dmiRegistrationUrl}") final String dmiRegistrationUrl,
        @Value("${cps-core.auth.username}") final String authUsername,
        @Value("${cps-core.auth.password}") final String authPassword) {
        this.baseUrl = baseUrl;
        this.dmiRegistrationUrl = dmiRegistrationUrl;
        this.authUsername = authUsername;
        this.authPassword = authPassword;
    }
}