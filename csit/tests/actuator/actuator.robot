# ============LICENSE_START=======================================================
# Copyright (C) 2022-2024 Nordix Foundation.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# ============LICENSE_END=========================================================

*** Settings ***
Documentation         DMI - Actuator and Swagger UI endpoints

Library               Collections
Library               RequestsLibrary

Suite Setup           Create Session    DMI_URL    http://${DMI_HOST}:${DMI_PORT}

*** Variables ***
${actuatorPath}      /actuator/health
${swaggerPath}       /swagger-ui/index.html

*** Test Cases ***

Test DMI Enhanced Healthcheck
    [Documentation]         Runs DMI Health Check. It will check for overall status update of DMI component like, Database and diskspace status along with liveliness and readiness check
    ${response}=            GET On Session        DMI_URL     ${actuatorPath}    expected_status=200
    ${resp_body}=           Convert to string     ${response.text}
    Should Contain          ${resp_body}          UP
    Should Not Contain      ${resp_body}          DOWN

Test DMI Swagger UI
    [Documentation]       Runs health check for DMI Swagger UI. If the DMI Swagger URL is accessible, status should be 200.
    GET On Session        DMI_URL             ${swaggerPath}    expected_status=200
