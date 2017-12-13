/*
 * Copyright 2017-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.fpcagent.util;

import com.google.common.collect.Maps;
import org.onosproject.config.DynamicConfigService;
import org.onosproject.config.Filter;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.ClientIdentifier;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.DefaultTenants;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.OpIdentifier;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.DefaultTenant;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.TenantKeys;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.yangautoprefixnotify.value.DefaultDownlinkDataNotification;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.yangautoprefixnotify.value.DownlinkDataNotification;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcDpnId;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcIdentity;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.fpcidentity.FpcIdentityUnion;
import org.onosproject.yang.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;

import static org.onosproject.fpcagent.util.Converter.fromIntToLong;
import static org.onosproject.fpcagent.util.Converter.toBigInt;

/**
 * Helper class which stores all the static variables.
 */
public class FpcUtil {
    public static final int MAX_EVENTS = 1000;
    public static final int MAX_BATCH_MS = 5000;
    public static final int MAX_IDLE_MS = 1000;
    public static final String TIMER = "dynamic-config-fpcagent-timer";
    public static final String UNKNOWN_EVENT = "FPC Agent listener: unknown event: {}";
    public static final String EVENT_NULL = "Event cannot be null";
    public static final String FPC_APP_ID = "org.onosproject.fpcagent";
    public static final FpcIdentity defaultIdentity = FpcIdentity.fromString("default");
    private static final Logger log = LoggerFactory.getLogger(FpcUtil.class);
    private static final Map<String, FpcDpnId> uplinkDpnMap = Maps.newConcurrentMap();
    private static final Map<String, Short> nodeToTopicMap = Maps.newConcurrentMap();
    private static final byte DPN_HELLO = 0b0000_0001;
    private static final byte DPN_BYE = 0b0000_0010;
    private static final byte DOWNLINK_DATA_NOTIFICATION = 0b0000_0101;
    private static final byte DPN_STATUS_INDICATION = 0b0000_1100;
    private static final byte DPN_OVERLOAD_INDICATION = 0b0000_0101;
    private static final byte DPN_REPLY = 0b0000_0100;
    private static final String DOWNLINK_DATA_NOTIFICATION_STRING = "Downlink-Data-Notification";
    public static DynamicConfigService dynamicConfigService = null;
    public static ModelConverter modelConverter = null;
    // Resource ID for Configure DPN RPC command
    public static ResourceId configureDpn;
    // Resource ID for Configure RPC command
    public static ResourceId configure;
    // Resource ID for tenants data
    public static ResourceId tenants;
    public static ResourceId configureBundles;
    public static ResourceId registerClient;
    public static ResourceId deregisterClinet;
    public static ResourceId module;

    /**
     * Returns resource id from model converter.
     *
     * @param modelId model object id
     * @return resource id
     */
    public static ResourceId getResourceVal(ModelObjectId modelId) {
        DefaultModelObjectData.Builder data = DefaultModelObjectData.builder()
                .identifier(modelId);
        ResourceData resData = modelConverter.createDataNode(data.build());
        return resData.resourceId();
    }

    /**
     * Returns the resource id, after constructing model object id and
     * converting it.
     */
    public static void getResourceId() {
        ModelObjectId moduleId = ModelObjectId.builder().build();
        module = getResourceVal(moduleId);

        ModelObjectId tenantsId = ModelObjectId.builder()
                .addChild(DefaultTenants.class)
                .build();

        tenants = getResourceVal(tenantsId);

        configure = ResourceId.builder()
                .addBranchPointSchema("/", null)
                .addBranchPointSchema("configure", "urn:ietf:params:xml:ns:yang:fpcagent")
                .build();

        configureDpn = ResourceId.builder()
                .addBranchPointSchema("/", null)
                .addBranchPointSchema("Configure-dpn", "urn:ietf:params:xml:ns:yang:fpcagent")
                .build();

        configureBundles = ResourceId.builder()
                .addBranchPointSchema("/", null)
                .addBranchPointSchema("Configure-bundles", "urn:ietf:params:xml:ns:yang:fpcagent")
                .build();

        registerClient = ResourceId.builder()
                .addBranchPointSchema("/", null)
                .addBranchPointSchema("register-client", "urn:onos:params:xml:ns:yang:fpc")
                .build();

        deregisterClinet = ResourceId.builder()
                .addBranchPointSchema("/", null)
                .addBranchPointSchema("deregister-client", "urn:onos:params:xml:ns:yang:fpc")
                .build();
    }

    /**
     * Extract Resource Data from specified DataNode and Resource Id.
     *
     * @param dataNode DataNode
     * @param resId    Resource Identifier
     * @return Resource Data
     */
    public static ResourceData getResourceData(DataNode dataNode, ResourceId resId) {
        if (resId != null) {
            return DefaultResourceData.builder()
                    .addDataNode(dataNode)
                    .resourceId(resId)
                    .build();
        } else {
            return DefaultResourceData.builder()
                    .addDataNode(dataNode)
                    .build();
        }
    }

    /**
     * Returns the resource ID of the parent data node pointed by {@code path}.
     *
     * @param path resource ID of the given data node
     * @return resource ID of the parent data node
     */
    public static ResourceId parentOf(ResourceId path) throws Exception {
        try {
            return path.copyBuilder().removeLastKey().build();
        } catch (CloneNotSupportedException e) {
            log.error("Could not copy {}", path, e);
            throw new RuntimeException("Could not copy " + path, e);
        }
    }

    public static ModelObjectId.Builder tenantBuilder(FpcIdentity fpcIdentity) {
        TenantKeys tenantKeys = new TenantKeys();
        tenantKeys.tenantId(fpcIdentity);

        return ModelObjectId.builder()
                .addChild(DefaultTenants.class)
                .addChild(DefaultTenant.class, tenantKeys);
    }

    /**
     * Returns resource id for the specific tenant ID.
     *
     * @param tenantId tenant id
     * @return resource ids
     */
    public static ResourceId getTenantResourceId(FpcIdentity tenantId) {
        TenantKeys tenantKeys = new TenantKeys();
        tenantKeys.tenantId(tenantId);

        return getResourceVal(ModelObjectId.builder()
                .addChild(DefaultTenants.class)
                .addChild(DefaultTenant.class, tenantKeys)
                .build());
    }

    /**
     * Ensures the session id is an unsigned 64 bit integer
     *
     * @param sessionId - session id received from the DPN
     * @return unsigned session id
     */
    private static BigInteger checkSessionId(BigInteger sessionId) {
        if (sessionId.compareTo(BigInteger.ZERO) < 0) {
            sessionId = sessionId.add(BigInteger.ONE.shiftLeft(64));
        }
        return sessionId;
    }

    /**
     * Decodes a DownlinkDataNotification
     *
     * @param buf - message buffer
     * @param key - Concatenation of node id + / + network id
     * @return DownlinkDataNotification or null if it could not be successfully decoded
     */
    private static DownlinkDataNotification processDDN(byte[] buf, String key) {
        DownlinkDataNotification ddnB = new DefaultDownlinkDataNotification();
        ddnB.sessionId(checkSessionId(toBigInt(buf, 2)));
        ddnB.notificationMessageType(DOWNLINK_DATA_NOTIFICATION_STRING);
        ddnB.clientId(ClientIdentifier.of(FpcIdentity.of(FpcIdentityUnion.of(fromIntToLong(buf, 10)))));
        ddnB.opId(OpIdentifier.of(BigInteger.valueOf(fromIntToLong(buf, 14))));
        ddnB.notificationDpnId(uplinkDpnMap.get(key));
        return ddnB;
    }

    /**
     * Decodes a DPN message.
     *
     * @param buf - message buffer
     * @return - A pair with the DPN Id and decoded Object
     */
    public static Map.Entry<FpcDpnId, Object> decode(byte[] buf) {
        if (buf[1] == DPN_REPLY) {
            return null;
        } else if (buf[1] == DOWNLINK_DATA_NOTIFICATION) {
            short nodeIdLen = buf[18];
            short networkIdLen = buf[19 + nodeIdLen];
            String key = new String(Arrays.copyOfRange(buf, 19, 19 + nodeIdLen)) + "/" + new String(Arrays.copyOfRange(buf, 20 + nodeIdLen, 20 + nodeIdLen + networkIdLen));
            return uplinkDpnMap.get(key) == null ? null : new AbstractMap.SimpleEntry<>(uplinkDpnMap.get(key), processDDN(buf, key));
        } else if (buf[1] == DPN_STATUS_INDICATION) {
            DPNStatusIndication.Status status = null;

            short nodeIdLen = buf[8];
            short networkIdLen = buf[9 + nodeIdLen];
            String key = new String(Arrays.copyOfRange(buf, 9, 9 + nodeIdLen)) + "/" + new String(Arrays.copyOfRange(buf, 10 + nodeIdLen, 10 + nodeIdLen + networkIdLen));
            if (buf[3] == DPN_OVERLOAD_INDICATION) {
                status = DPNStatusIndication.Status.OVERLOAD_INDICATION;
            } else if (buf[3] == DPN_HELLO) {
                status = DPNStatusIndication.Status.HELLO;
                log.info("Hello {} on topic {}", key, buf[2]);
                nodeToTopicMap.put(key, (short) buf[2]);
            } else if (buf[3] == DPN_BYE) {
                status = DPNStatusIndication.Status.BYE;
                log.info("Bye {}", key);
                nodeToTopicMap.remove(key);
            }
            return new AbstractMap.SimpleEntry<>(uplinkDpnMap.get(key), new DPNStatusIndication(status, key));
        }
        return null;
    }

    /**
     * Gets the mapping for node id / network id to ZMQ Topic
     *
     * @param Key - Concatenation of node id + / + network id
     * @return - ZMQ Topic
     */
    public static Short getTopicFromNode(String Key) {
        return nodeToTopicMap.get(Key);
    }

    /**
     * Returns the root level node for Tenants.
     * Tenants is an interface that includes a List of Tenant objects.
     *
     * @return Optional Tenants
     */
    public static Optional<DefaultTenants> getTenants() {
        Filter filter = Filter.builder().build();
        DataNode dataNode = dynamicConfigService.readNode(tenants, filter);

        return getModelObjects(dataNode, null)
                .stream()
                .map(modelObject -> (DefaultTenants) modelObject)
                .findFirst();
    }

    public static Optional<DefaultTenant> getTenant(FpcIdentity tenantId) {
        Filter filter = Filter.builder().build();
        DataNode dataNode = dynamicConfigService.readNode(getTenantResourceId(tenantId), filter);

        return getModelObjects(dataNode, tenants)
                .stream()
                .map(modelObject -> (DefaultTenant) modelObject)
                .findFirst();
    }

    /**
     * Get Tenant by its Identifier.
     *
     * @param clientId Tenant Identifier
     * @return Optional Tenant
     */

    public static Optional<DefaultTenant> getTenant(ClientIdentifier clientId) {
        return Optional.empty();
    }

    /**
     * Converts DataNode to a ModelObject.
     *
     * @param dataNode   DataNode
     * @param resourceId Resource Identifier
     * @return Model Object
     */

    public static List<ModelObject> getModelObjects(DataNode dataNode, ResourceId resourceId) {
        ResourceData data = getResourceData(dataNode, resourceId);
        ModelObjectData modelData = modelConverter.createModel(data);
        return modelData.modelObjects();
    }

    /**
     * Creates a Node inside the Dynamic Configuration Store.
     *
     * @param innerModelObject inner model object to create
     * @param modelObjectId    Model Object ID
     */
    public static void createNode(InnerModelObject innerModelObject, ModelObjectId modelObjectId) {
        ResourceData dataNode = modelConverter.createDataNode(
                DefaultModelObjectData.builder()
                        .identifier(modelObjectId)
                        .addModelObject(innerModelObject)
                        .build()
        );
        dataNode.dataNodes().forEach(
                node -> dynamicConfigService.createNode(dataNode.resourceId(), node)
        );
    }

    /**
     * Updates a Node inside the Dynamic Configuration Store.
     *
     * @param innerModelObject inner model object to update
     * @param modelObjectId    Model Object ID
     */
    public static void updateNode(InnerModelObject innerModelObject, ModelObjectId modelObjectId) {
        ResourceData dataNode = modelConverter.createDataNode(
                DefaultModelObjectData.builder()
                        .identifier(modelObjectId)
                        .addModelObject(innerModelObject)
                        .build()
        );
        dataNode.dataNodes().forEach(
                node -> dynamicConfigService.updateNode(dataNode.resourceId(), node)
        );
    }

    /**
     * Provides basic status changes,
     */
    public static class DPNStatusIndication {
        private final Status status;
        private final String key; //nodeId +"/"+ networkId
        /**
         * Node Reference of the DPN
         */
        public Short nodeRef;

        /**
         * Constructor providing the DPN and its associated Status.
         *
         * @param status - DPN Status
         * @param key    - Combination of node id and network id
         */
        public DPNStatusIndication(Status status,
                                   String key) {
            this.status = status;
            this.key = key;
        }

        /**
         * Provides DPN Status
         *
         * @return Status associated to the DPN.
         */
        public Status getStatus() {
            return status;
        }

        /**
         * Provides the DPN key - nodeId +"/"+ networkId
         *
         * @return FpcDpnId
         */
        public String getKey() {
            return this.key;
        }

        /**
         * Basic DPN Status
         */
        public enum Status {
            HELLO,
            BYE,
            OVERLOAD_INDICATION
        }
    }
}
