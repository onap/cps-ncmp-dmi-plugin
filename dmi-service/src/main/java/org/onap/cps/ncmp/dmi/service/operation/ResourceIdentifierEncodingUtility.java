/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2025 OpenInfra Foundation Europe. All rights reserved.
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

package org.onap.cps.ncmp.dmi.service.operation;


import java.nio.charset.StandardCharsets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriUtils;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceIdentifierEncodingUtility {

    private static final String EQUALS_SIGN = "=";
    private static final String PATH_SEPARATOR = "/";
    private static final String ENCODED_EQUALS = "%3D";

    /**
     * Encodes a nested resource path by URL-encoding path segments while preserving
     * key-value pairs structure. Key-value pairs are identified by the presence of '='
     * and can span multiple path segments until the next key-value pair is encountered.
     *
     * @param resourcePath the resource path to encode
     * @return the encoded resource path, or the original path if null/blank
     */
    public static String encodeNestedResourcePath(final String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return resourcePath;
        }

        final boolean hasLeadingSlash = resourcePath.startsWith(PATH_SEPARATOR);
        final String[] pathSegments =
                (hasLeadingSlash ? resourcePath.substring(1) : resourcePath).split(PATH_SEPARATOR);
        final StringBuilder result = new StringBuilder();

        for (int pathSegmentIndex = 0; pathSegmentIndex < pathSegments.length; pathSegmentIndex++) {
            final String segment = pathSegments[pathSegmentIndex];

            if (segment.isEmpty()) {
                continue;
            }

            if (result.length() > 0) {
                result.append(PATH_SEPARATOR);
            }

            if (segment.contains(EQUALS_SIGN)) {
                pathSegmentIndex = processKeyValuePair(pathSegments, pathSegmentIndex, result);
            } else {
                result.append(UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8));
            }
        }

        return hasLeadingSlash ? PATH_SEPARATOR + result : result.toString();
    }

    private static int processKeyValuePair(final String[] pathSegments, final int pathSegmentIndex,
            final StringBuilder result) {
        final String keyValueSegment = pathSegments[pathSegmentIndex];
        final int equalsIndex = keyValueSegment.indexOf(EQUALS_SIGN);
        final String keySegment = keyValueSegment.substring(0, equalsIndex);
        final StringBuilder valueSegment = new StringBuilder(keyValueSegment.substring(equalsIndex + 1));

        // Append subsequent segments until next key-value pair
        int nextPathSegmentIndex = pathSegmentIndex + 1;
        while (nextPathSegmentIndex < pathSegments.length && !pathSegments[nextPathSegmentIndex].contains(
                EQUALS_SIGN)) {
            if (!pathSegments[nextPathSegmentIndex].isEmpty()) {
                valueSegment.append(PATH_SEPARATOR).append(pathSegments[nextPathSegmentIndex]);
            }
            nextPathSegmentIndex++;
        }

        final String encodedValue = UriUtils.encodePathSegment(valueSegment.toString(), StandardCharsets.UTF_8)
                                            .replace(EQUALS_SIGN, ENCODED_EQUALS);

        result.append(keySegment).append(EQUALS_SIGN).append(encodedValue);
        return nextPathSegmentIndex - 1; // Return last processed index
    }
}
