package org.onosproject.fpcagent.helpers;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "monitor-threads",
        "scheduled-monitors-poolsize",
        "dpn-listener-uri",
        "dpn-listener-id",
        "dpn-client-uri",
        "dpn-client-threads",
        "metricsupdate-ms",
        "mobilityupdate-ms",
        "activation-threads",
        "target-read-limit",
        "http-notifier-clients",
        "zmq-nbi-server-poolsize",
        "zmq-nbi-server-uri",
        "zmq-nbi-inproc-uri",
        "zmq-nbi-handler-poolsize",
        "http-nio2-nb-poolsize",
        "http-nio2-nb-port",
        "node-id",
        "network-id",
        "zmq-broadcast-controllers",
        "zmq-broadcast-dpns",
        "zmq-broadcast-all"
})
public class ConfigHelper {

    @JsonProperty("monitor-threads")
    private Integer monitorThreads;
    @JsonProperty("scheduled-monitors-poolsize")
    private Integer scheduledMonitorsPoolsize;
    @JsonProperty("dpn-listener-uri")
    private String dpnListenerUri;
    @JsonProperty("dpn-listener-id")
    private Integer dpnListenerId;
    @JsonProperty("dpn-client-uri")
    private String dpnClientUri;
    @JsonProperty("dpn-client-threads")
    private Integer dpnClientThreads;
    @JsonProperty("metricsupdate-ms")
    private Integer metricsupdateMs;
    @JsonProperty("mobilityupdate-ms")
    private Integer mobilityupdateMs;
    @JsonProperty("activation-threads")
    private Integer activationThreads;
    @JsonProperty("target-read-limit")
    private Integer targetReadLimit;
    @JsonProperty("http-notifier-clients")
    private Integer httpNotifierClients;
    @JsonProperty("zmq-nbi-server-poolsize")
    private Integer zmqNbiServerPoolsize;
    @JsonProperty("zmq-nbi-server-uri")
    private String zmqNbiServerUri;
    @JsonProperty("zmq-nbi-inproc-uri")
    private String zmqNbiInprocUri;
    @JsonProperty("zmq-nbi-handler-poolsize")
    private Integer zmqNbiHandlerPoolsize;
    @JsonProperty("http-nio2-nb-poolsize")
    private Integer httpNio2NbPoolsize;
    @JsonProperty("http-nio2-nb-port")
    private Integer httpNio2NbPort;
    @JsonProperty("node-id")
    private String nodeId;
    @JsonProperty("network-id")
    private String networkId;
    @JsonProperty("zmq-broadcast-controllers")
    private String zmqBroadcastControllers;
    @JsonProperty("zmq-broadcast-dpns")
    private String zmqBroadcastDpns;
    @JsonProperty("zmq-broadcast-all")
    private String zmqBroadcastAll;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    /**
     * No args constructor for use in serialization
     */
    public ConfigHelper() {
    }

    @JsonProperty("monitor-threads")
    public Integer monitorThreads() {
        return monitorThreads;
    }

    @JsonProperty("monitor-threads")
    public void setMonitorThreads(Integer monitorThreads) {
        this.monitorThreads = monitorThreads;
    }

    @JsonProperty("scheduled-monitors-poolsize")
    public Integer scheduledMonitorsPoolsize() {
        return scheduledMonitorsPoolsize;
    }

    @JsonProperty("scheduled-monitors-poolsize")
    public void setScheduledMonitorsPoolsize(Integer scheduledMonitorsPoolsize) {
        this.scheduledMonitorsPoolsize = scheduledMonitorsPoolsize;
    }

    @JsonProperty("dpn-listener-uri")
    public String dpnListenerUri() {
        return dpnListenerUri;
    }

    @JsonProperty("dpn-listener-uri")
    public void setDpnListenerUri(String dpnListenerUri) {
        this.dpnListenerUri = dpnListenerUri;
    }

    @JsonProperty("dpn-listener-id")
    public Integer dpnListenerId() {
        return dpnListenerId;
    }

    @JsonProperty("dpn-listener-id")
    public void setDpnListenerId(Integer dpnListenerId) {
        this.dpnListenerId = dpnListenerId;
    }

    @JsonProperty("dpn-client-uri")
    public String dpnClientUri() {
        return dpnClientUri;
    }

    @JsonProperty("dpn-client-uri")
    public void setDpnClientUri(String dpnClientUri) {
        this.dpnClientUri = dpnClientUri;
    }

    @JsonProperty("dpn-client-threads")
    public Integer dpnClientThreads() {
        return dpnClientThreads;
    }

    @JsonProperty("dpn-client-threads")
    public void setDpnClientThreads(Integer dpnClientThreads) {
        this.dpnClientThreads = dpnClientThreads;
    }

    @JsonProperty("metricsupdate-ms")
    public Integer metricUpdateMs() {
        return metricsupdateMs;
    }

    @JsonProperty("metricsupdate-ms")
    public void setMetricsupdateMs(Integer metricsupdateMs) {
        this.metricsupdateMs = metricsupdateMs;
    }

    @JsonProperty("mobilityupdate-ms")
    public Integer mobilityUpdateMs() {
        return mobilityupdateMs;
    }

    @JsonProperty("mobilityupdate-ms")
    public void setMobilityupdateMs(Integer mobilityupdateMs) {
        this.mobilityupdateMs = mobilityupdateMs;
    }

    @JsonProperty("activation-threads")
    public Integer activationThreads() {
        return activationThreads;
    }

    @JsonProperty("activation-threads")
    public void setActivationThreads(Integer activationThreads) {
        this.activationThreads = activationThreads;
    }

    @JsonProperty("target-read-limit")
    public Integer targetReadLimit() {
        return targetReadLimit;
    }

    @JsonProperty("target-read-limit")
    public void setTargetReadLimit(Integer targetReadLimit) {
        this.targetReadLimit = targetReadLimit;
    }

    @JsonProperty("http-notifier-clients")
    public Integer httpNotifierClients() {
        return httpNotifierClients;
    }

    @JsonProperty("http-notifier-clients")
    public void setHttpNotifierClients(Integer httpNotifierClients) {
        this.httpNotifierClients = httpNotifierClients;
    }

    @JsonProperty("zmq-nbi-server-poolsize")
    public Integer zmqServerPoolsize() {
        return zmqNbiServerPoolsize;
    }

    @JsonProperty("zmq-nbi-server-poolsize")
    public void setZmqNbiServerPoolsize(Integer zmqNbiServerPoolsize) {
        this.zmqNbiServerPoolsize = zmqNbiServerPoolsize;
    }

    @JsonProperty("zmq-nbi-server-uri")
    public String zmqServerUri() {
        return zmqNbiServerUri;
    }

    @JsonProperty("zmq-nbi-server-uri")
    public void setZmqNbiServerUri(String zmqNbiServerUri) {
        this.zmqNbiServerUri = zmqNbiServerUri;
    }

    @JsonProperty("zmq-nbi-inproc-uri")
    public String zmqInprocUri() {
        return zmqNbiInprocUri;
    }

    @JsonProperty("zmq-nbi-inproc-uri")
    public void setZmqNbiInprocUri(String zmqNbiInprocUri) {
        this.zmqNbiInprocUri = zmqNbiInprocUri;
    }

    @JsonProperty("zmq-nbi-handler-poolsize")
    public Integer zmqHandlerPoolsize() {
        return zmqNbiHandlerPoolsize;
    }

    @JsonProperty("zmq-nbi-handler-poolsize")
    public void setZmqNbiHandlerPoolsize(Integer zmqNbiHandlerPoolsize) {
        this.zmqNbiHandlerPoolsize = zmqNbiHandlerPoolsize;
    }

    @JsonProperty("http-nio2-nb-poolsize")
    public Integer httpNio2Poolsize() {
        return httpNio2NbPoolsize;
    }

    @JsonProperty("http-nio2-nb-poolsize")
    public void setHttpNio2NbPoolsize(Integer httpNio2NbPoolsize) {
        this.httpNio2NbPoolsize = httpNio2NbPoolsize;
    }

    @JsonProperty("http-nio2-nb-port")
    public Integer httpNio2Port() {
        return httpNio2NbPort;
    }

    @JsonProperty("http-nio2-nb-port")
    public void setHttpNio2NbPort(Integer httpNio2NbPort) {
        this.httpNio2NbPort = httpNio2NbPort;
    }

    @JsonProperty("node-id")
    public String nodeId() {
        return nodeId;
    }

    @JsonProperty("node-id")
    public void setNodeId(String node_id) {
        this.nodeId = node_id;
    }

    @JsonProperty("network-id")
    public String networkId() {
        return networkId;
    }

    @JsonProperty("network-id")
    public void setNetworkId(String network_id) {
        this.networkId = network_id;
    }

    @JsonProperty("zmq-broadcast-controllers")
    public String zmqBroadcastControllers() {
        return zmqBroadcastControllers;
    }

    @JsonProperty("zmq-broadcast-controllers")
    public void setZmqBroadcastControllers(String zmqBroadcastControllers) {
        this.zmqBroadcastControllers = zmqBroadcastControllers;
    }

    @JsonProperty("zmq-broadcast-dpns")
    public String zmqBroadcastDpns() {
        return zmqBroadcastDpns;
    }

    @JsonProperty("zmq-broadcast-dpns")
    public void setZmqBroadcastDpns(String zmqBroadcastDpns) {
        this.zmqBroadcastDpns = zmqBroadcastDpns;
    }

    @JsonProperty("zmq-broadcast-all")
    public String zmqBroadcastAll() {
        return zmqBroadcastAll;
    }

    @JsonProperty("zmq-broadcast-all")
    public void setZmqBroadcastAll(String zmqBroadcastAll) {
        this.zmqBroadcastAll = zmqBroadcastAll;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
