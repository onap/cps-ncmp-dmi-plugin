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
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.util.UriUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceIdentifierEncodingUtility {

    private static final String EQUALS_SIGN = "=";
    private static final String PATH_SEPARATOR = "/";
    private static final String ENCODED_EQUALS = "%3D";

    /**
     * Encode a nested resource path by URL-encoding path segments while preserving
     * key-value structure. Supports spaces, slashes, and '=' characters in values.
     *
     * @param resourcePath input path (may start with '/')
     * @return encoded resource path
     */
    public static String encodeNestedResourcePath(final String resourcePath) {

        final boolean hasLeadingPathSeparator = resourcePath.startsWith(PATH_SEPARATOR);
        final String resourcePathToProcess = hasLeadingPathSeparator ? resourcePath.substring(1) : resourcePath;

        final List<String> encodedSegments = parseAndEncodeSegments(resourcePathToProcess);
        final String encodedResourcePath = String.join(PATH_SEPARATOR, encodedSegments);

        return hasLeadingPathSeparator ? PATH_SEPARATOR + encodedResourcePath : encodedResourcePath;
    }

    private static List<String> parseAndEncodeSegments(final String resourcePath) {
        final List<String> resourcePathSegments = new ArrayList<>();
        final String[] resourcePathParts = resourcePath.split(PATH_SEPARATOR);

        int pathPartIndex = 0;
        while (pathPartIndex < resourcePathParts.length) {
            final String resourcePathPart = resourcePathParts[pathPartIndex];

            if (resourcePathPart.contains(EQUALS_SIGN)) {
                final StringBuilder segment = new StringBuilder(resourcePathPart);
                pathPartIndex++;
                // Continue collecting parts until we hit another key-value pair or end
                while (pathPartIndex < resourcePathParts.length && !resourcePathParts[pathPartIndex].contains(
                        EQUALS_SIGN)) {
                    segment.append(PATH_SEPARATOR).append(resourcePathParts[pathPartIndex]);
                    pathPartIndex++;
                }
                resourcePathSegments.add(encodePathSegment(segment.toString()));
            } else {
                // Simple resource path segment without equals
                resourcePathSegments.add(encodePathSegment(resourcePathPart));
                pathPartIndex++;
            }
        }
        return resourcePathSegments;
    }

    private static String encodePathSegment(final String segment) {
        if (segment.contains(EQUALS_SIGN)) {
            return encodePathSegmentWithEqualsSign(segment);
        }
        return UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8);

    }

    private static String encodePathSegmentWithEqualsSign(final String segment) {
        final int indexOfEqualSign = segment.indexOf(EQUALS_SIGN);
        final String key = segment.substring(0, indexOfEqualSign);
        final String value = segment.substring(indexOfEqualSign + 1);

        // encode both key and value, and replace '=' with %3D in the value
        final String encodedKey = UriUtils.encodePathSegment(key, StandardCharsets.UTF_8);
        final String encodedValue =
                UriUtils.encodePathSegment(value, StandardCharsets.UTF_8).replace(EQUALS_SIGN, ENCODED_EQUALS);
        return encodedKey + EQUALS_SIGN + encodedValue;
    }
}
