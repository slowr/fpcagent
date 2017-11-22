
package org.onosproject.fpcagent.dto.configure;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dpn-id",
    "direction",
    "dpn-parameters"
})
public class Dpn {

    @JsonProperty("dpn-id")
    private String dpn_id;
    @JsonProperty("direction")
    private String direction;
    @JsonProperty("dpn-parameters")
    private Dpn_parameters__ dpn_parameters;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Dpn() {
    }

    /**
     * 
     * @param dpn_parameters
     * @param direction
     * @param dpn_id
     */
    public Dpn(String dpn_id, String direction, Dpn_parameters__ dpn_parameters) {
        super();
        this.dpn_id = dpn_id;
        this.direction = direction;
        this.dpn_parameters = dpn_parameters;
    }

    @JsonProperty("dpn-id")
    public String getDpn_id() {
        return dpn_id;
    }

    @JsonProperty("dpn-id")
    public void setDpn_id(String dpn_id) {
        this.dpn_id = dpn_id;
    }

    @JsonProperty("direction")
    public String getDirection() {
        return direction;
    }

    @JsonProperty("direction")
    public void setDirection(String direction) {
        this.direction = direction;
    }

    @JsonProperty("dpn-parameters")
    public Dpn_parameters__ getDpn_parameters() {
        return dpn_parameters;
    }

    @JsonProperty("dpn-parameters")
    public void setDpn_parameters(Dpn_parameters__ dpn_parameters) {
        this.dpn_parameters = dpn_parameters;
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
        return new ToStringBuilder(this).append("dpn_id", dpn_id).append("direction", direction).append("dpn_parameters", dpn_parameters).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dpn_parameters).append(additionalProperties).append(direction).append(dpn_id).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Dpn) == false) {
            return false;
        }
        Dpn rhs = ((Dpn) other);
        return new EqualsBuilder().append(dpn_parameters, rhs.dpn_parameters).append(additionalProperties, rhs.additionalProperties).append(direction, rhs.direction).append(dpn_id, rhs.dpn_id).isEquals();
    }

}
