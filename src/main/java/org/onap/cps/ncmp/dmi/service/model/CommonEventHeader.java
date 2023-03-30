/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2023 Nordix Foundation
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

package org.onap.cps.ncmp.dmi.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class CommonEventHeader {
    private Double startEpochMicrosec;
    private String sourceId;
    private String eventId;
    private String timeZoneOffset;
    private String reportingEntityId;
    private InternalHeaderFields internalHeaderFields;
    private String priority;
    private String version;
    private String nfVendorName;
    private String reportingEntityName;
    private Integer sequence;
    private String domain;
    private Double lastEpochMicrosec;
    private String eventName;
    private String vesEventListenerVersion;
    private String stndDefinedNamespace;
    private String sourceName;
    private String nfNamingCode;

}
