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

package org.onap.cps.ncmp.dmi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.validation.Valid;

/**
 * RequestOperation.
 */
public class RequestOperation   {
    /**
     * Gets or Sets operation.
     */
    public enum OperationEnum {
        READ("read"),

        WRITE("write");

        private String value;

        OperationEnum(final String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        /**
         * Returns an enum from text.
         *
         * @param text input
         * @return {@code OperationEnum}
         */
        @JsonCreator
        public static RequestOperation.OperationEnum fromValue(final String text) {
            for (final RequestOperation.OperationEnum b : RequestOperation.OperationEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    @JsonProperty("operation")
    private RequestOperation.OperationEnum operation = null;

    /**
     * Gets or Sets dataType.
     */
    public enum DataTypeEnum {
        JSON("application/json");

        private String value;

        DataTypeEnum(final String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        /**
         * Returns an enum from text.
         *
         * @param text input
         * @return {@code DataTypeEnum}
         */
        @JsonCreator
        public static RequestOperation.DataTypeEnum fromValue(final String text) {
            for (final RequestOperation.DataTypeEnum b : RequestOperation.DataTypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    @JsonProperty("data-type")
    private RequestOperation.DataTypeEnum dataType = null;

    @JsonProperty("data")
    private Object data = null;

    @JsonProperty("additionalProperties")
    @Valid
    private Map<String, String> additionalProperties = null;

    public RequestOperation operation(final RequestOperation.OperationEnum operation) {
        this.operation = operation;
        return this;
    }

    /**
     * Get operation.
     * @return operation
     **/
    @ApiModelProperty(value = "")
    public RequestOperation.OperationEnum getOperation() {
        return operation;
    }

    /**
     * Set operation.
     *
     * @param operation operation enum name
     */
    public void setOperation(final RequestOperation.OperationEnum operation) {
        this.operation = operation;
    }

    public RequestOperation dataType(final RequestOperation.DataTypeEnum dataType) {
        this.dataType = dataType;
        return this;
    }

    /**
     * Get dataType.
     *
     * @return dataType {@code DataTypeEnum}
     **/
    @ApiModelProperty(value = "")
    public RequestOperation.DataTypeEnum getDataType() {
        return dataType;
    }

    /**
     * Set datatype.
     *
     * @param dataType {@code DataTypeEnum}
     */
    public void setDataType(final RequestOperation.DataTypeEnum dataType) {
        this.dataType = dataType;
    }

    public RequestOperation data(final Object data) {
        this.data = data;
        return this;
    }

    /**
     * Get data.
     * @return data {@code Object}
     **/
    @ApiModelProperty(value = "")
    public Object getData() {
        return data;
    }

    /**
     * Set Data.
     *
     * @param data {@code Object}
     */
    public void setData(final Object data) {
        this.data = data;
    }

    /**
     * Add additionalProperties to {@code RequestOperation} using builder.
     *
     * @param additionalProperties {@code Map}
     * @return this object
     */
    public RequestOperation additionalProperties(final Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
        return this;
    }

    /**
     * Add additionalPropertiesItem to {@code RequestOperation} using builder.
     *
     * @param key key
     * @param additionalPropertiesItem additionalPropertiesItem
     * @return this object
     */
    public RequestOperation putAdditionalPropertiesItem(final String key, final String additionalPropertiesItem) {
        if (this.additionalProperties == null) {
            this.additionalProperties = new HashMap<String, String>();
        }
        this.additionalProperties.put(key, additionalPropertiesItem);
        return this;
    }

    /**
     * Get additionalProperties.
     *
     * @return additionalProperties {@code Map}
     **/
    @ApiModelProperty(value = "")
    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * Set additionalProperties.
     *
     * @param additionalProperties {@code Map}
     */
    public void setAdditionalProperties(final Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @Override
    public boolean equals(final java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RequestOperation operation = (RequestOperation) o;
        return Objects.equals(this.operation, operation.operation)
                && Objects.equals(this.dataType, operation.dataType)
                && Objects.equals(this.data, operation.data)
                && Objects.equals(this.additionalProperties, operation.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, dataType, data, additionalProperties);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("class Operation {\n");

        sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
        sb.append("    dataType: ").append(toIndentedString(dataType)).append("\n");
        sb.append("    data: ").append(toIndentedString(data)).append("\n");
        sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(final java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

