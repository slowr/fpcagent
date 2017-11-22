
package org.onosproject.fpcagent.dto.client;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.onosproject.fpcagent.dto.configure.ConfigureInput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "client-id",
    "tenant-id",
    "supported-features",
    "endpoint-uri"
})
public class BindClient {

    @JsonProperty("client-id")
    private String client_id;
    @JsonProperty("tenant-id")
    private String tenant_id;
    @JsonProperty("supported-features")
    private List<String> supported_features = null;
    @JsonProperty("endpoint-uri")
    private String endpoint_uri;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public BindClient() {
    }

    /**
     * 
     * @param endpoint_uri
     * @param supported_features
     * @param tenant_id
     * @param client_id
     */
    public BindClient(String client_id, String tenant_id, List<String> supported_features, String endpoint_uri) {
        super();
        this.client_id = client_id;
        this.tenant_id = tenant_id;
        this.supported_features = supported_features;
        this.endpoint_uri = endpoint_uri;
    }

    @JsonProperty("client-id")
    public String getClient_id() {
        return client_id;
    }

    @JsonProperty("client-id")
    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    @JsonProperty("tenant-id")
    public String getTenant_id() {
        return tenant_id;
    }

    @JsonProperty("tenant-id")
    public void setTenant_id(String tenant_id) {
        this.tenant_id = tenant_id;
    }

    @JsonProperty("supported-features")
    public List<String> getSupported_features() {
        return supported_features;
    }

    @JsonProperty("supported-features")
    public void setSupported_features(List<String> supported_features) {
        this.supported_features = supported_features;
    }

    @JsonProperty("endpoint-uri")
    public String getEndpoint_uri() {
        return endpoint_uri;
    }

    @JsonProperty("endpoint-uri")
    public void setEndpoint_uri(String endpoint_uri) {
        this.endpoint_uri = endpoint_uri;
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
        return new ToStringBuilder(this).append("client_id", client_id).append("tenant_id", tenant_id).append("supported_features", supported_features).append("endpoint_uri", endpoint_uri).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(additionalProperties).append(endpoint_uri).append(supported_features).append(tenant_id).append(client_id).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ConfigureInput) == false) {
            return false;
        }
        BindClient rhs = ((BindClient) other);
        return new EqualsBuilder().append(additionalProperties, rhs.additionalProperties).append(endpoint_uri, rhs.endpoint_uri).append(supported_features, rhs.supported_features).append(tenant_id, rhs.tenant_id).append(client_id, rhs.client_id).isEquals();
    }

}
