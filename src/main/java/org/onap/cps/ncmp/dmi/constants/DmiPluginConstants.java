/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2022 Nordix Foundation
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.cps.ncmp.dmi.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DmiPluginConstants {

    public static final String TOPOLOGY_URL_TEMPLATE_DATA =
            "/rests/data/network-topology:network-topology/";
    public static final String TOPOLOGY_URL_TEMPLATE_OPERATIONAL =
            "/rests/operations/network-topology:network-topology/";
    public static final String MOUNT_URL_TEMPLATE = "/node={nodeId}/yang-ext:mount";
    public static final String GET_SCHEMA_URL = "ietf-netconf-monitoring:netconf-state/schemas";
    public static final String GET_SCHEMA_SOURCES_URL = "/ietf-netconf-monitoring:get-schema";
    public static final String PATH_TO_MODULE_SCHEMAS = "$.ietf-netconf-monitoring:schemas.schema";
    public static final String TOPOLOGY_ID = "topology={topologyId}";
    public static final String COMMA_DELIMITER = ",";
    public static final String QUERY_SEPARATOR = "=";
    public static final int QUERY_MAP_LIMIT = 2;
    public static final int QUERY_PARAM_NAME_INDEX = 0;
    public static final int QUERY_PARAM_LENGTH = 1;
    public static final int QUERY_PARAM_VALUE_INDEX = 1;
    public static final String NODE_ID = "node={nodeId}";
    public static final String YANG_EXT_MOUNT = "yang-ext:mount";
    public static final String PATH_SEPARATOR = "/";
}
