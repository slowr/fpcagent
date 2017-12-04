package org.onosproject.fpcagent.helpers;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "monitor-threads",
        "scheduled-monitors-poolsize",
        "dpn-subscriber-uri",
        "dpn-listener-id",
        "dpn-publisher-uri",
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
    @JsonProperty("dpn-subscriber-uri")
    private String dpnSubscriberUri;
    @JsonProperty("dpn-listener-id")
    private Integer dpnListenerId;
    @JsonProperty("dpn-publisher-uri")
    private String dpnPublisherUri;
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


    public Integer monitorThreads() {
        return monitorThreads;
    }


    public void setMonitorThreads(Integer monitorThreads) {
        this.monitorThreads = monitorThreads;
    }


    public Integer scheduledMonitorsPoolsize() {
        return scheduledMonitorsPoolsize;
    }


    public void setScheduledMonitorsPoolsize(Integer scheduledMonitorsPoolsize) {
        this.scheduledMonitorsPoolsize = scheduledMonitorsPoolsize;
    }


    public String dpnSubscriberUri() {
        return dpnSubscriberUri;
    }


    public void setDpnSubscriberUri(String dpnSubscriberUri) {
        this.dpnSubscriberUri = dpnSubscriberUri;
    }


    public Integer dpnListenerId() {
        return dpnListenerId;
    }


    public void setDpnListenerId(Integer dpnListenerId) {
        this.dpnListenerId = dpnListenerId;
    }


    public String dpnPublisherUri() {
        return dpnPublisherUri;
    }

    public void setDpnPublisherUri(String dpnPublisherUri) {
        this.dpnPublisherUri = dpnPublisherUri;
    }


    public Integer dpnClientThreads() {
        return dpnClientThreads;
    }


    public void setDpnClientThreads(Integer dpnClientThreads) {
        this.dpnClientThreads = dpnClientThreads;
    }


    public Integer metricUpdateMs() {
        return metricsupdateMs;
    }


    public void setMetricsupdateMs(Integer metricsupdateMs) {
        this.metricsupdateMs = metricsupdateMs;
    }


    public Integer mobilityUpdateMs() {
        return mobilityupdateMs;
    }


    public void setMobilityupdateMs(Integer mobilityupdateMs) {
        this.mobilityupdateMs = mobilityupdateMs;
    }


    public Integer activationThreads() {
        return activationThreads;
    }


    public void setActivationThreads(Integer activationThreads) {
        this.activationThreads = activationThreads;
    }


    public Integer targetReadLimit() {
        return targetReadLimit;
    }


    public void setTargetReadLimit(Integer targetReadLimit) {
        this.targetReadLimit = targetReadLimit;
    }


    public Integer httpNotifierClients() {
        return httpNotifierClients;
    }


    public void setHttpNotifierClients(Integer httpNotifierClients) {
        this.httpNotifierClients = httpNotifierClients;
    }


    public Integer zmqServerPoolsize() {
        return zmqNbiServerPoolsize;
    }


    public void setZmqNbiServerPoolsize(Integer zmqNbiServerPoolsize) {
        this.zmqNbiServerPoolsize = zmqNbiServerPoolsize;
    }


    public String zmqServerUri() {
        return zmqNbiServerUri;
    }


    public void setZmqNbiServerUri(String zmqNbiServerUri) {
        this.zmqNbiServerUri = zmqNbiServerUri;
    }


    public String zmqInprocUri() {
        return zmqNbiInprocUri;
    }


    public void setZmqNbiInprocUri(String zmqNbiInprocUri) {
        this.zmqNbiInprocUri = zmqNbiInprocUri;
    }


    public Integer zmqHandlerPoolsize() {
        return zmqNbiHandlerPoolsize;
    }


    public void setZmqNbiHandlerPoolsize(Integer zmqNbiHandlerPoolsize) {
        this.zmqNbiHandlerPoolsize = zmqNbiHandlerPoolsize;
    }


    public Integer httpNio2Poolsize() {
        return httpNio2NbPoolsize;
    }


    public void setHttpNio2NbPoolsize(Integer httpNio2NbPoolsize) {
        this.httpNio2NbPoolsize = httpNio2NbPoolsize;
    }


    public Integer httpNio2Port() {
        return httpNio2NbPort;
    }


    public void setHttpNio2NbPort(Integer httpNio2NbPort) {
        this.httpNio2NbPort = httpNio2NbPort;
    }


    public String nodeId() {
        return nodeId;
    }


    public void setNodeId(String node_id) {
        this.nodeId = node_id;
    }


    public String networkId() {
        return networkId;
    }


    public void setNetworkId(String network_id) {
        this.networkId = network_id;
    }


    public String zmqBroadcastControllers() {
        return zmqBroadcastControllers;
    }


    public void setZmqBroadcastControllers(String zmqBroadcastControllers) {
        this.zmqBroadcastControllers = zmqBroadcastControllers;
    }

    public String zmqBroadcastDpns() {
        return zmqBroadcastDpns;
    }


    public void setZmqBroadcastDpns(String zmqBroadcastDpns) {
        this.zmqBroadcastDpns = zmqBroadcastDpns;
    }


    public String zmqBroadcastAll() {
        return zmqBroadcastAll;
    }


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
