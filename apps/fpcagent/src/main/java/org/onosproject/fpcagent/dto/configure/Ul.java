
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
    "tunnel-local-address",
    "tunnel-remote-address",
    "mobility-tunnel-parameters",
    "dpn-parameters"
})
public class Ul {

    @JsonProperty("tunnel-local-address")
    private String tunnel_local_address;
    @JsonProperty("tunnel-remote-address")
    private String tunnel_remote_address;
    @JsonProperty("mobility-tunnel-parameters")
    private Mobility_tunnel_parameters mobility_tunnel_parameters;
    @JsonProperty("dpn-parameters")
    private Dpn_parameters dpn_parameters;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Ul() {
    }

    /**
     * 
     * @param tunnel_remote_address
     * @param dpn_parameters
     * @param tunnel_local_address
     * @param mobility_tunnel_parameters
     */
    public Ul(String tunnel_local_address, String tunnel_remote_address, Mobility_tunnel_parameters mobility_tunnel_parameters, Dpn_parameters dpn_parameters) {
        super();
        this.tunnel_local_address = tunnel_local_address;
        this.tunnel_remote_address = tunnel_remote_address;
        this.mobility_tunnel_parameters = mobility_tunnel_parameters;
        this.dpn_parameters = dpn_parameters;
    }

    @JsonProperty("tunnel-local-address")
    public String getTunnel_local_address() {
        return tunnel_local_address;
    }

    @JsonProperty("tunnel-local-address")
    public void setTunnel_local_address(String tunnel_local_address) {
        this.tunnel_local_address = tunnel_local_address;
    }

    @JsonProperty("tunnel-remote-address")
    public String getTunnel_remote_address() {
        return tunnel_remote_address;
    }

    @JsonProperty("tunnel-remote-address")
    public void setTunnel_remote_address(String tunnel_remote_address) {
        this.tunnel_remote_address = tunnel_remote_address;
    }

    @JsonProperty("mobility-tunnel-parameters")
    public Mobility_tunnel_parameters getMobility_tunnel_parameters() {
        return mobility_tunnel_parameters;
    }

    @JsonProperty("mobility-tunnel-parameters")
    public void setMobility_tunnel_parameters(Mobility_tunnel_parameters mobility_tunnel_parameters) {
        this.mobility_tunnel_parameters = mobility_tunnel_parameters;
    }

    @JsonProperty("dpn-parameters")
    public Dpn_parameters getDpn_parameters() {
        return dpn_parameters;
    }

    @JsonProperty("dpn-parameters")
    public void setDpn_parameters(Dpn_parameters dpn_parameters) {
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
        return new ToStringBuilder(this).append("tunnel_local_address", tunnel_local_address).append("tunnel_remote_address", tunnel_remote_address).append("mobility_tunnel_parameters", mobility_tunnel_parameters).append("dpn_parameters", dpn_parameters).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tunnel_remote_address).append(dpn_parameters).append(additionalProperties).append(tunnel_local_address).append(mobility_tunnel_parameters).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Ul) == false) {
            return false;
        }
        Ul rhs = ((Ul) other);
        return new EqualsBuilder().append(tunnel_remote_address, rhs.tunnel_remote_address).append(dpn_parameters, rhs.dpn_parameters).append(additionalProperties, rhs.additionalProperties).append(tunnel_local_address, rhs.tunnel_local_address).append(mobility_tunnel_parameters, rhs.mobility_tunnel_parameters).isEquals();
    }

}
