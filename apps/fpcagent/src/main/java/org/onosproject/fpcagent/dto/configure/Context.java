
package org.onosproject.fpcagent.dto.configure;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "instructions",
    "context-id",
    "dpn-group",
    "delegating-ip-prefixes",
    "ul",
    "dl",
    "dpns",
    "imsi",
    "ebi",
    "lbi"
})
public class Context {

    @JsonProperty("instructions")
    private Instructions instructions;
    @JsonProperty("context-id")
    private Integer context_id;
    @JsonProperty("dpn-group")
    private String dpn_group;
    @JsonProperty("delegating-ip-prefixes")
    private List<String> delegating_ip_prefixes = null;
    @JsonProperty("ul")
    private Ul ul;
    @JsonProperty("dl")
    private Dl dl;
    @JsonProperty("dpns")
    private List<Dpn> dpns = null;
    @JsonProperty("imsi")
    private String imsi;
    @JsonProperty("ebi")
    private String ebi;
    @JsonProperty("lbi")
    private String lbi;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Context() {
    }

    /**
     * 
     * @param lbi
     * @param dpns
     * @param ul
     * @param dpn_group
     * @param context_id
     * @param instructions
     * @param dl
     * @param delegating_ip_prefixes
     * @param imsi
     * @param ebi
     */
    public Context(Instructions instructions, Integer context_id, String dpn_group, List<String> delegating_ip_prefixes, Ul ul, Dl dl, List<Dpn> dpns, String imsi, String ebi, String lbi) {
        super();
        this.instructions = instructions;
        this.context_id = context_id;
        this.dpn_group = dpn_group;
        this.delegating_ip_prefixes = delegating_ip_prefixes;
        this.ul = ul;
        this.dl = dl;
        this.dpns = dpns;
        this.imsi = imsi;
        this.ebi = ebi;
        this.lbi = lbi;
    }

    @JsonProperty("instructions")
    public Instructions getInstructions() {
        return instructions;
    }

    @JsonProperty("instructions")
    public void setInstructions(Instructions instructions) {
        this.instructions = instructions;
    }

    @JsonProperty("context-id")
    public Integer getContext_id() {
        return context_id;
    }

    @JsonProperty("context-id")
    public void setContext_id(Integer context_id) {
        this.context_id = context_id;
    }

    @JsonProperty("dpn-group")
    public String getDpn_group() {
        return dpn_group;
    }

    @JsonProperty("dpn-group")
    public void setDpn_group(String dpn_group) {
        this.dpn_group = dpn_group;
    }

    @JsonProperty("delegating-ip-prefixes")
    public List<String> getDelegating_ip_prefixes() {
        return delegating_ip_prefixes;
    }

    @JsonProperty("delegating-ip-prefixes")
    public void setDelegating_ip_prefixes(List<String> delegating_ip_prefixes) {
        this.delegating_ip_prefixes = delegating_ip_prefixes;
    }

    @JsonProperty("ul")
    public Ul getUl() {
        return ul;
    }

    @JsonProperty("ul")
    public void setUl(Ul ul) {
        this.ul = ul;
    }

    @JsonProperty("dl")
    public Dl getDl() {
        return dl;
    }

    @JsonProperty("dl")
    public void setDl(Dl dl) {
        this.dl = dl;
    }

    @JsonProperty("dpns")
    public List<Dpn> getDpns() {
        return dpns;
    }

    @JsonProperty("dpns")
    public void setDpns(List<Dpn> dpns) {
        this.dpns = dpns;
    }

    @JsonProperty("imsi")
    public String getImsi() {
        return imsi;
    }

    @JsonProperty("imsi")
    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    @JsonProperty("ebi")
    public String getEbi() {
        return ebi;
    }

    @JsonProperty("ebi")
    public void setEbi(String ebi) {
        this.ebi = ebi;
    }

    @JsonProperty("lbi")
    public String getLbi() {
        return lbi;
    }

    @JsonProperty("lbi")
    public void setLbi(String lbi) {
        this.lbi = lbi;
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
        return new ToStringBuilder(this).append("instructions", instructions).append("context_id", context_id).append("dpn_group", dpn_group).append("delegating_ip_prefixes", delegating_ip_prefixes).append("ul", ul).append("dl", dl).append("dpns", dpns).append("imsi", imsi).append("ebi", ebi).append("lbi", lbi).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(lbi).append(dpns).append(ul).append(dpn_group).append(context_id).append(instructions).append(additionalProperties).append(dl).append(delegating_ip_prefixes).append(imsi).append(ebi).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Context) == false) {
            return false;
        }
        Context rhs = ((Context) other);
        return new EqualsBuilder().append(lbi, rhs.lbi).append(dpns, rhs.dpns).append(ul, rhs.ul).append(dpn_group, rhs.dpn_group).append(context_id, rhs.context_id).append(instructions, rhs.instructions).append(additionalProperties, rhs.additionalProperties).append(dl, rhs.dl).append(delegating_ip_prefixes, rhs.delegating_ip_prefixes).append(imsi, rhs.imsi).append(ebi, rhs.ebi).isEquals();
    }

}
