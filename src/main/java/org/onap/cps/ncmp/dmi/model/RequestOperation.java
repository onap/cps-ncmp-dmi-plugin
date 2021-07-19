package org.onap.cps.ncmp.dmi.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Operation
 */
public class RequestOperation   {
    /**
     * Gets or Sets operation
     */
    public enum OperationEnum {
        READ("read"),

        WRITE("write");

        private String value;

        OperationEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static org.onap.cps.ncmp.dmi.model.RequestOperation.OperationEnum fromValue(String text) {
            for (org.onap.cps.ncmp.dmi.model.RequestOperation.OperationEnum b : org.onap.cps.ncmp.dmi.model.RequestOperation.OperationEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }
    @JsonProperty("operation")
    private org.onap.cps.ncmp.dmi.model.RequestOperation.OperationEnum operation = null;

    /**
     * Gets or Sets dataType
     */
    public enum DataTypeEnum {
        JSON("application/json");

        private String value;

        DataTypeEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static org.onap.cps.ncmp.dmi.model.RequestOperation.DataTypeEnum fromValue(String text) {
            for (org.onap.cps.ncmp.dmi.model.RequestOperation.DataTypeEnum b : org.onap.cps.ncmp.dmi.model.RequestOperation.DataTypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }
    @JsonProperty("data-type")
    private org.onap.cps.ncmp.dmi.model.RequestOperation.DataTypeEnum dataType = null;

    @JsonProperty("data")
    private Object data = null;

    @JsonProperty("additionalProperties")
    @Valid
    private Map<String, String> additionalProperties = null;

    public org.onap.cps.ncmp.dmi.model.RequestOperation operation(org.onap.cps.ncmp.dmi.model.RequestOperation.OperationEnum operation) {
        this.operation = operation;
        return this;
    }

    /**
     * Get operation
     * @return operation
     **/
    @ApiModelProperty(value = "")

    public org.onap.cps.ncmp.dmi.model.RequestOperation.OperationEnum getOperation() {
        return operation;
    }

    public void setOperation(org.onap.cps.ncmp.dmi.model.RequestOperation.OperationEnum operation) {
        this.operation = operation;
    }

    public org.onap.cps.ncmp.dmi.model.RequestOperation dataType(org.onap.cps.ncmp.dmi.model.RequestOperation.DataTypeEnum dataType) {
        this.dataType = dataType;
        return this;
    }

    /**
     * Get dataType
     * @return dataType
     **/
    @ApiModelProperty(value = "")

    public org.onap.cps.ncmp.dmi.model.RequestOperation.DataTypeEnum getDataType() {
        return dataType;
    }

    public void setDataType(org.onap.cps.ncmp.dmi.model.RequestOperation.DataTypeEnum dataType) {
        this.dataType = dataType;
    }

    public org.onap.cps.ncmp.dmi.model.RequestOperation data(Object data) {
        this.data = data;
        return this;
    }

    /**
     * Get data
     * @return data
     **/
    @ApiModelProperty(value = "")

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public org.onap.cps.ncmp.dmi.model.RequestOperation additionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
        return this;
    }

    public org.onap.cps.ncmp.dmi.model.RequestOperation putAdditionalPropertiesItem(String key, String additionalPropertiesItem) {
        if (this.additionalProperties == null) {
            this.additionalProperties = new HashMap<String, String>();
        }
        this.additionalProperties.put(key, additionalPropertiesItem);
        return this;
    }

    /**
     * Get additionalProperties
     * @return additionalProperties
     **/
    @ApiModelProperty(value = "")

    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        org.onap.cps.ncmp.dmi.model.RequestOperation Operation = (org.onap.cps.ncmp.dmi.model.RequestOperation) o;
        return Objects.equals(this.operation, Operation.operation) &&
                Objects.equals(this.dataType, Operation.dataType) &&
                Objects.equals(this.data, Operation.data) &&
                Objects.equals(this.additionalProperties, Operation.additionalProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, dataType, data, additionalProperties);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
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
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

