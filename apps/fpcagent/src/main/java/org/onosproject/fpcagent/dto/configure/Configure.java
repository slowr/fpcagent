
package org.onosproject.fpcagent.dto.configure;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "input"
})
public class Configure {

    @JsonProperty("input")
    private ConfigureInput configureInput;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Configure() {
    }

    /**
     * 
     * @param configureInput
     */
    public Configure(ConfigureInput configureInput) {
        super();
        this.configureInput = configureInput;
    }

    @JsonProperty("input")
    public ConfigureInput getConfigureInput() {
        return configureInput;
    }

    @JsonProperty("input")
    public void setConfigureInput(ConfigureInput configureInput) {
        this.configureInput = configureInput;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("input", configureInput).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configureInput).append(additionalProperties).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Configure) == false) {
            return false;
        }
        Configure rhs = ((Configure) other);
        return new EqualsBuilder().append(configureInput, rhs.configureInput).append(additionalProperties, rhs.additionalProperties).isEquals();
    }

}
