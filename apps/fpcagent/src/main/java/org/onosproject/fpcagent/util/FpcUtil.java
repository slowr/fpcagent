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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.onosproject.config.DynamicConfigService;
import org.onosproject.config.Filter;
import org.onosproject.core.IdGenerator;
import org.onosproject.fpcagent.workers.HTTPNotifier;
import org.onosproject.net.Device;
import org.onosproject.net.device.DeviceStore;
import org.onosproject.restconf.utils.RestconfUtils;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.registerclient.DefaultRegisterClientInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.ClientIdentifier;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.DefaultTenants;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.DefaultYangAutoPrefixNotify;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.DefaultTenant;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.TenantKeys;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcDpnId;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcIdentity;
import org.onosproject.yang.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import static org.onosproject.net.DeviceId.deviceId;

/**
 * Helper class which stores all the static variables.
 */
public class FpcUtil {
    public static final String FPC_APP_ID = "org.onosproject.fpcagent";
    public static final FpcIdentity defaultIdentity = FpcIdentity.fromString("default");
    private static final Logger log = LoggerFactory.getLogger(FpcUtil.class);

    public static DynamicConfigService dynamicConfigService = null;
    public static ModelConverter modelConverter = null;
    public static DeviceStore deviceStore = null;

    public static ResourceId configureDpn;
    public static ResourceId configure;
    public static ResourceId tenants;
    public static ResourceId configureBundles;
    public static ResourceId registerClient;
    public static ResourceId deregisterClient;
    public static ResourceId module;
    public static ResourceId notification;

    public static IdGenerator notificationIds;

    public static final ConcurrentMap<ClientIdentifier, DefaultRegisterClientInput> clientInfo = Maps.newConcurrentMap();
    public static final ConcurrentMap<FpcIdentity, HashSet<ClientIdentifier>> tenantInfo = Maps.newConcurrentMap();
    public static final HashSet<FpcDpnId> dpnInfo = Sets.newHashSet();

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
                .addBranchPointSchema("configure-dpn", "urn:ietf:params:xml:ns:yang:fpcagent")
                .build();

        configureBundles = ResourceId.builder()
                .addBranchPointSchema("/", null)
                .addBranchPointSchema("configure-bundles", "urn:ietf:params:xml:ns:yang:fpcagent")
                .build();

        registerClient = ResourceId.builder()
                .addBranchPointSchema("/", null)
                .addBranchPointSchema("register-client", "urn:onos:params:xml:ns:yang:fpc")
                .build();

        deregisterClient = ResourceId.builder()
                .addBranchPointSchema("/", null)
                .addBranchPointSchema("deregister-client", "urn:onos:params:xml:ns:yang:fpc")
                .build();

        notification = ResourceId.builder()
                .addBranchPointSchema("/", null)
                .addBranchPointSchema("notify", "urn:ietf:params:xml:ns:yang:fpcagent")
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
     * Gets the mapping for node id / network id to ZMQ Topic
     *
     * @param key - Concatenation of node id + / + network id
     * @return ZMQ Topic
     */
    public static byte getTopicFromNode(String key) {
        // TODO add cache
        Device device = deviceStore.getDevice(deviceId("fpc:" + key));
        if (device != null) {
            String topic = device.annotations().value("topic");
            return Byte.parseByte(topic);
        }
        return -1;
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

    public static void sendNotification(DefaultYangAutoPrefixNotify notify, ClientIdentifier client) {
        ResourceData dataNode = modelConverter.createDataNode(
                DefaultModelObjectData.builder()
                        .addModelObject(notify)
                        .build()
        );
        ObjectNode jsonNodes = RestconfUtils.convertDataNodeToJson(module, dataNode.dataNodes().get(0));
        ObjectMapper mapper = new ObjectMapper();

        try {
            log.info("Sending HTTP notification {} to {}", notify, client);
            HTTPNotifier.getInstance().send(
                    new AbstractMap.SimpleEntry<>(
                            clientInfo.get(client).endpointUri().toString(),
                            mapper.writeValueAsString(jsonNodes)
                    )
            );
        } catch (JsonProcessingException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
    }
}
