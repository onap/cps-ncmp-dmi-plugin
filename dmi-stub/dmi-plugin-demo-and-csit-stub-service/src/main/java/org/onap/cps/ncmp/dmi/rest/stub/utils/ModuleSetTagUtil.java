/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2024 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

package org.onap.cps.ncmp.dmi.rest.stub.utils;

import java.util.ArrayList;
import java.util.List;

public class ModuleSetTagUtil {

    /**
     * Generates a list of tags with characters ranging from the specified start to end.
     *
     * @param start the starting character for the tags
     * @param end the ending character for the tags
     * @return a list of tags in the format "tagX" where X is each character from start to end
     */
    public static List<String> generateTags(final char start, final char end) {
        final List<String> tags = new ArrayList<>();
        for (char c = start; c <= end; c++) {
            tags.add("tag" + c);
        }
        return tags;
    }
}
