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
        "op-id",
        "targets",
        "contexts",
        "client-id",
        "session-state",
        "admin-state",
        "op-type",
        "op-ref-scope"
})
public class ConfigureInput {

    @JsonProperty("op-id")
    private String op_id;
    @JsonProperty("targets")
    private List<Target> targets = null;
    @JsonProperty("contexts")
    private List<Context> contexts = null;
    @JsonProperty("client-id")
    private String client_id;
    @JsonProperty("session-state")
    private String session_state;
    @JsonProperty("admin-state")
    private String admin_state;
    @JsonProperty("op-type")
    private String op_type;
    @JsonProperty("op-ref-scope")
    private String op_ref_scope;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     */
    public ConfigureInput() {
    }

    @JsonProperty("op-id")
    public String getOp_id() {
        return op_id;
    }

    @JsonProperty("op-id")
    public void setOp_id(String op_id) {
        this.op_id = op_id;
    }

    @JsonProperty("targets")
    public List<Target> getTargets() {
        return targets;
    }

    @JsonProperty("targets")
    public void setTargets(List<Target> targets) {
        this.targets = targets;
    }

    @JsonProperty("contexts")
    public List<Context> getContexts() {
        return contexts;
    }

    @JsonProperty("contexts")
    public void setContexts(List<Context> contexts) {
        this.contexts = contexts;
    }

    @JsonProperty("client-id")
    public String getClient_id() {
        return client_id;
    }

    @JsonProperty("client-id")
    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    @JsonProperty("session-state")
    public String getSession_state() {
        return session_state;
    }

    @JsonProperty("session-state")
    public void setSession_state(String session_state) {
        this.session_state = session_state;
    }

    @JsonProperty("admin-state")
    public String getAdmin_state() {
        return admin_state;
    }

    @JsonProperty("admin-state")
    public void setAdmin_state(String admin_state) {
        this.admin_state = admin_state;
    }

    @JsonProperty("op-type")
    public String getOp_type() {
        return op_type;
    }

    @JsonProperty("op-type")
    public void setOp_type(String op_type) {
        this.op_type = op_type;
    }

    @JsonProperty("op-ref-scope")
    public String getOp_ref_scope() {
        return op_ref_scope;
    }

    @JsonProperty("op-ref-scope")
    public void setOp_ref_scope(String op_ref_scope) {
        this.op_ref_scope = op_ref_scope;
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
        return new ToStringBuilder(this).append("op_id", op_id).append("targets", targets).append("contexts", contexts).append("client_id", client_id).append("session_state", session_state).append("admin_state", admin_state).append("op_type", op_type).append("op_ref_scope", op_ref_scope).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(session_state).append(op_id).append(additionalProperties).append(admin_state).append(op_type).append(op_ref_scope).append(contexts).append(client_id).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConfigureInput) == false) {
            return false;
        }
        ConfigureInput rhs = ((ConfigureInput) other);
        return new EqualsBuilder().append(session_state, rhs.session_state).append(op_id, rhs.op_id).append(additionalProperties, rhs.additionalProperties).append(admin_state, rhs.admin_state).append(op_type, rhs.op_type).append(op_ref_scope, rhs.op_ref_scope).append(contexts, rhs.contexts).append(client_id, rhs.client_id).isEquals();
    }

}
