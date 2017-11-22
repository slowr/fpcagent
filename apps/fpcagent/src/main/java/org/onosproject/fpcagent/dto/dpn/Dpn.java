
package org.onosproject.fpcagent.dto.dpn;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dpn-id",
    "dpn-name",
    "dpn-groups",
    "node-id",
    "network-id"
})
public class Dpn {

    @JsonProperty("dpn-id")
    private String dpn_id;
    @JsonProperty("dpn-name")
    private String dpn_name;
    @JsonProperty("dpn-groups")
    private List<String> dpn_groups = null;
    @JsonProperty("node-id")
    private String node_id;
    @JsonProperty("network-id")
    private String network_id;
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
     * @param dpn_name
     * @param network_id
     * @param dpn_groups
     * @param dpn_id
     * @param node_id
     */
    public Dpn(String dpn_id, String dpn_name, List<String> dpn_groups, String node_id, String network_id) {
        super();
        this.dpn_id = dpn_id;
        this.dpn_name = dpn_name;
        this.dpn_groups = dpn_groups;
        this.node_id = node_id;
        this.network_id = network_id;
    }

    @JsonProperty("dpn-id")
    public String getDpn_id() {
        return dpn_id;
    }

    @JsonProperty("dpn-id")
    public void setDpn_id(String dpn_id) {
        this.dpn_id = dpn_id;
    }

    @JsonProperty("dpn-name")
    public String getDpn_name() {
        return dpn_name;
    }

    @JsonProperty("dpn-name")
    public void setDpn_name(String dpn_name) {
        this.dpn_name = dpn_name;
    }

    @JsonProperty("dpn-groups")
    public List<String> getDpn_groups() {
        return dpn_groups;
    }

    @JsonProperty("dpn-groups")
    public void setDpn_groups(List<String> dpn_groups) {
        this.dpn_groups = dpn_groups;
    }

    @JsonProperty("node-id")
    public String getNode_id() {
        return node_id;
    }

    @JsonProperty("node-id")
    public void setNode_id(String node_id) {
        this.node_id = node_id;
    }

    @JsonProperty("network-id")
    public String getNetwork_id() {
        return network_id;
    }

    @JsonProperty("network-id")
    public void setNetwork_id(String network_id) {
        this.network_id = network_id;
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
        return new ToStringBuilder(this).append("dpn_id", dpn_id).append("dpn_name", dpn_name).append("dpn_groups", dpn_groups).append("node_id", node_id).append("network_id", network_id).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dpn_name).append(network_id).append(dpn_groups).append(additionalProperties).append(dpn_id).append(node_id).toHashCode();
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
        return new EqualsBuilder().append(dpn_name, rhs.dpn_name).append(network_id, rhs.network_id).append(dpn_groups, rhs.dpn_groups).append(additionalProperties, rhs.additionalProperties).append(dpn_id, rhs.dpn_id).append(node_id, rhs.node_id).isEquals();
    }

}
