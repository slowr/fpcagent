
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
    "instr-3gpp-mob"
})
public class Instructions {

    @JsonProperty("instr-3gpp-mob")
    private String instr_3gpp_mob;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Instructions() {
    }

    /**
     * 
     * @param instr_3gpp_mob
     */
    public Instructions(String instr_3gpp_mob) {
        super();
        this.instr_3gpp_mob = instr_3gpp_mob;
    }

    @JsonProperty("instr-3gpp-mob")
    public String getInstr_3gpp_mob() {
        return instr_3gpp_mob;
    }

    @JsonProperty("instr-3gpp-mob")
    public void setInstr_3gpp_mob(String instr_3gpp_mob) {
        this.instr_3gpp_mob = instr_3gpp_mob;
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
        return new ToStringBuilder(this).append("instr_3gpp_mob", instr_3gpp_mob).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(additionalProperties).append(instr_3gpp_mob).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Instructions) == false) {
            return false;
        }
        Instructions rhs = ((Instructions) other);
        return new EqualsBuilder().append(additionalProperties, rhs.additionalProperties).append(instr_3gpp_mob, rhs.instr_3gpp_mob).isEquals();
    }

}
