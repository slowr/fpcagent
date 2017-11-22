
package org.onosproject.fpcagent.dto.configure;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "tunnel-type",
    "tunnel-identifier"
})
public class Mobility_tunnel_parameters_ {

    @JsonProperty("tunnel-type")
    private String tunnel_type;
    @JsonProperty("tunnel-identifier")
    private String tunnel_identifier;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Mobility_tunnel_parameters_() {
    }

    /**
     * 
     * @param tunnel_type
     * @param tunnel_identifier
     */
    public Mobility_tunnel_parameters_(String tunnel_type, String tunnel_identifier) {
        super();
        this.tunnel_type = tunnel_type;
        this.tunnel_identifier = tunnel_identifier;
    }

    @JsonProperty("tunnel-type")
    public String getTunnel_type() {
        return tunnel_type;
    }

    @JsonProperty("tunnel-type")
    public void setTunnel_type(String tunnel_type) {
        this.tunnel_type = tunnel_type;
    }

    @JsonProperty("tunnel-identifier")
    public String getTunnel_identifier() {
        return tunnel_identifier;
    }

    @JsonProperty("tunnel-identifier")
    public void setTunnel_identifier(String tunnel_identifier) {
        this.tunnel_identifier = tunnel_identifier;
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
        return new ToStringBuilder(this).append("tunnel_type", tunnel_type).append("tunnel_identifier", tunnel_identifier).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(additionalProperties).append(tunnel_type).append(tunnel_identifier).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Mobility_tunnel_parameters_) == false) {
            return false;
        }
        Mobility_tunnel_parameters_ rhs = ((Mobility_tunnel_parameters_) other);
        return new EqualsBuilder().append(additionalProperties, rhs.additionalProperties).append(tunnel_type, rhs.tunnel_type).append(tunnel_identifier, rhs.tunnel_identifier).isEquals();
    }

}
