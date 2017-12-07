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

package org.onosproject.fpcagent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.felix.scr.annotations.*;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.Ip4Prefix;
import org.onlab.util.AbstractAccumulator;
import org.onlab.util.Accumulator;
import org.onosproject.config.*;
import org.onosproject.fpcagent.util.CacheManager;
import org.onosproject.fpcagent.util.DpnCommunicationService;
import org.onosproject.fpcagent.util.DpnNgicCommunicator;
import org.onosproject.fpcagent.util.FpcUtil;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.DefaultConnectionInfo;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.registerclient.DefaultRegisterClientInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.*;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configure.DefaultConfigureOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configuredpn.DefaultConfigureDpnInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configuredpn.DefaultConfigureDpnOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.instructions.instructions.instrtype.Instr3GppMob;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.CreateOrUpdate;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.DeleteOrQuery;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.payload.Contexts;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.result.ResultEnum;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbody.resulttype.DefaultCommonSuccess;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbody.resulttype.DefaultDeleteSuccess;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbodydpn.resulttype.DefaultCommonDeleteSuccess;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbodydpn.resulttype.DefaultEmptyCase;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.DefaultTenant;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.tenant.DefaultFpcMobility;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.tenant.DefaultFpcPolicy;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.tenant.DefaultFpcTopology;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.tenant.fpcmobility.ContextsKeys;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.tenant.fpcmobility.DefaultContexts;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.tenant.fpctopology.DefaultDpns;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcContextId;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcDpnId;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcIdentity;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.fpccontext.Dpns;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.mobilityinfo.mobprofileparameters.ThreegppTunnel;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.targetsvalue.Targets;
import org.onosproject.yang.gen.v1.ietfdmmthreegpp.rev20160803.ietfdmmthreegpp.threegppinstr.Bits;
import org.onosproject.yang.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.fpcagent.util.Converter.convertContext;
import static org.onosproject.fpcagent.util.FpcUtil.*;

@Component(immediate = true)
@Service
public class TenantManager implements TenantService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final InternalConfigListener listener = new InternalConfigListener();

    private final Accumulator<DynamicConfigEvent> accumulator = new InternalEventAccumulator();

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private ModelConverter modelConverter;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private DynamicConfigService dynamicConfigService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private DynamicConfigStore dynamicConfigStore;

    private DpnCommunicationService dpnCommunicationService;

    @Activate
    protected void activate() {
        CacheManager.addManager(this);
        FpcUtil.modelConverter = modelConverter;
        getResourceId();

        dpnCommunicationService = new DpnNgicCommunicator();

        dynamicConfigService.addListener(listener);

        // Create the Default Tenant and added to the Tenants structure.
        final DefaultTenants tenants = new DefaultTenants();
        final DefaultTenant tenant = new DefaultTenant();
        tenant.tenantId(defaultIdentity);
        tenant.fpcTopology(new DefaultFpcTopology());
        tenant.fpcPolicy(new DefaultFpcPolicy());
        tenant.fpcMobility(new DefaultFpcMobility());
        tenants.addToTenant(tenant);

        // Initialize FPC Agent Information.
        final DefaultFpcAgentInfo fpcAgentInfo = new DefaultFpcAgentInfo();

        // Create nodes in dynamic configuration store for RESTCONF accessibility.
        final ModelObjectId root = ModelObjectId.builder().build();

        final DefaultConnectionInfo defaultConnectionInfo = new DefaultConnectionInfo();

        createNode(tenants, root);
        createNode(fpcAgentInfo, root);
        createNode(defaultConnectionInfo, root);

        log.info("Tenant Service Started");
    }

    @Deactivate
    protected void deactivate() {
        dynamicConfigService.removeListener(listener);
        log.info("Tenant Service Stopped");
    }

    @Override
    public Optional<DefaultTenants> getTenants() {
        Filter filter = Filter.builder().build();
        DataNode dataNode = dynamicConfigService.readNode(tenants, filter);

        return getModelObjects(dataNode, null)
                .stream()
                .map(modelObject -> (DefaultTenants) modelObject)
                .findFirst();
    }

    @Override
    public Optional<DefaultTenant> getTenant(FpcIdentity tenantId) {
        Filter filter = Filter.builder().build();
        DataNode dataNode = dynamicConfigService.readNode(getTenantResourceId(tenantId), filter);

        return getModelObjects(dataNode, tenants)
                .stream()
                .map(modelObject -> (DefaultTenant) modelObject)
                .findFirst();
    }

    public Optional<DefaultTenant> getDefaultTenant() {
        Filter filter = Filter.builder().build();
        DataNode dataNode = dynamicConfigService.readNode(defaultTenant, filter);

        return getModelObjects(dataNode, tenants)
                .stream()
                .map(modelObject -> (DefaultTenant) modelObject)
                .findFirst();
    }

    @Override
    public Optional<DefaultTenant> getTenant(ClientIdentifier clientId) {
        return Optional.empty();
    }

    @Override
    public List<ModelObject> getModelObjects(DataNode dataNode, ResourceId resourceId) {
        ResourceData data = getResourceData(dataNode, resourceId);
        ModelObjectData modelData = modelConverter.createModel(data);
        return modelData.modelObjects();
    }

    @Override
    public void createNode(InnerModelObject innerModelObject, ModelObjectId modelObjectId) {
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

    @Override
    public void updateNode(InnerModelObject innerModelObject, ModelObjectId modelObjectId) {
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

    private Collection<Callable<Object>> create(
            Dpns dpn,
            Contexts context,
            DefaultRegisterClientInput clientId,
            OpIdentifier operationId
    ) throws Exception {
        Collection<Callable<Object>> tasks = new ArrayList<>();
        CacheManager cacheManager = CacheManager.getInstance(clientId.tenantId());
        // check if dpns exists and if there is a DPN registered for the wanted identifier.
        if (!cacheManager.dpnsCache.get(dpn.dpnId()).isPresent()) {
            // throw exception if DPN ID is not registered.
            throw new RuntimeException("DPN ID is not registered to the topology.");
        }

        // handle only 3GPP instructions.
        if (!(context.instructions().instrType() instanceof Instr3GppMob)) {
            throw new RuntimeException("No 3GPP instructions where given.");
        }

        // from DPN ID find the Network and Node Identifiers
        Optional<String> key = cacheManager.dpnsCache.get(dpn.dpnId())
                .map(node -> node.nodeId() + "/" + node.networkId());
        if (!key.isPresent()) {
            throw new RuntimeException("DPN does not have node and network ID defined.");
        }

        // get DPN Topic from Node/Network pair
        Short topic_id = getTopicFromNode(key.get());
        if (topic_id == null) {
            throw new RuntimeException("Could not find Topic ID");
        }

        // parse tunnel identifiers. throw exception if mobility profile parameters are missing.
        if (!(context.ul().mobilityTunnelParameters().mobprofileParameters() instanceof ThreegppTunnel)) {
            throw new RuntimeException("mobprofileParameters are not instance of ThreegppTunnel");

        }
        if (!(context.dl().mobilityTunnelParameters().mobprofileParameters() instanceof ThreegppTunnel)) {
            throw new RuntimeException("mobprofileParameters are not instance of ThreegppTunnel");
        }

        // Extract variables
        Instr3GppMob instr3GppMob = (Instr3GppMob) context.instructions().instrType();
        String commands = Bits.toString(instr3GppMob.instr3GppMob().bits());

        Ip4Address s1u_enodeb_ipv4 = Ip4Address.valueOf(context.ul().tunnelLocalAddress().toString()),
                s1u_sgw_ipv4 = Ip4Address.valueOf(context.ul().tunnelRemoteAddress().toString());

        long client_id = clientId.clientId().fpcIdentity().union().int64(),
                session_id = context.contextId().fpcIdentity().union().int64(),
                s1u_sgw_gtpu_teid = ((ThreegppTunnel) context.ul().mobilityTunnelParameters()
                        .mobprofileParameters()).tunnelIdentifier(),
                s1u_enb_gtpu_teid = ((ThreegppTunnel) context.ul().mobilityTunnelParameters()
                        .mobprofileParameters()).tunnelIdentifier();

        BigInteger op_id = operationId.uint64(),
                imsi = context.imsi().uint64();

        short default_ebi = context.ebi().uint8();

        // TODO try to make sense out of this...
        if (commands.contains("session")) {
            DefaultContexts convertContext = convertContext(context);
            tasks.add(Executors.callable(() -> {
                dpnCommunicationService.create_session(
                        topic_id,
                        imsi,
                        default_ebi,
                        Ip4Prefix.valueOf(context.delegatingIpPrefixes().get(0).toString()).address(),
                        s1u_sgw_gtpu_teid,
                        s1u_sgw_ipv4,
                        session_id,
                        client_id,
                        op_id
                );

                ModelObjectId modelObjectId = defaultTenantBuilder()
                        .addChild(DefaultFpcMobility.class)
                        .build();
                createNode(convertContext, modelObjectId);
                cacheManager.contextsCache.put(convertContext.contextId(), Optional.of(convertContext));
            }));

            if (commands.contains("downlink")) {
                tasks.add(Executors.callable(() -> {
                    dpnCommunicationService.modify_bearer(
                            topic_id,
                            s1u_sgw_ipv4,
                            s1u_enb_gtpu_teid,
                            s1u_enodeb_ipv4,
                            session_id,
                            client_id,
                            op_id
                    );

                    ModelObjectId modelObjectId = defaultTenantBuilder()
                            .addChild(DefaultFpcMobility.class)
                            .build();
                    createNode(convertContext, modelObjectId);
                    cacheManager.contextsCache.put(convertContext.contextId(), Optional.of(convertContext));
                }));
            }
        } else if (commands.contains("indirect-forward")) {
            // TODO - Modify API for Indirect Forwarding to/from another SGW
        } else if (commands.contains("uplink")) {
            // TODO create bearer ul
        }
        return tasks;
    }

    @Override
    public DefaultConfigureOutput configureCreate(
            CreateOrUpdate create,
            DefaultRegisterClientInput clientInfo,
            OpIdentifier operationId
    ) throws Exception {
        DefaultConfigureOutput configureOutput = new DefaultConfigureOutput();
        Collection<Callable<Object>> tasks = new ArrayList<>();

        DefaultCommonSuccess defaultCommonSuccess = new DefaultCommonSuccess();
        for (Contexts context : create.contexts()) {
            // add context to response.
            defaultCommonSuccess.addToContexts(context);

            // check if mobility exists and if the context id exists.
            if (CacheManager.getInstance(clientInfo.tenantId()).contextsCache.get(context.contextId()).isPresent()) {
                // throw exception if trying to create a Context that already exists.
                throw new RuntimeException("Context tried to create already exists. Please issue update operation..");
            }

            for (Dpns dpn : context.dpns()) {
                tasks = create(dpn, context, clientInfo, operationId);
            }
        }

        // execute all tasks.
        ExecutorService executor = Executors.newWorkStealingPool();
        executor.invokeAll(tasks).forEach(
                future -> {
                    try {
                        future.get();
                    } catch (Exception e) {
                        log.error(ExceptionUtils.getFullStackTrace(e));
                        throw new RuntimeException(e);
                    }
                }
        );

        configureOutput.resultType(defaultCommonSuccess);
        configureOutput.result(Result.of(ResultEnum.OK));

        return configureOutput;
    }

    private Collection<Callable<Object>> update(
            Dpns dpn,
            Contexts context,
            DefaultRegisterClientInput clientId,
            OpIdentifier operationId
    ) throws Exception {
        Collection<Callable<Object>> tasks = new ArrayList<>();
        CacheManager cacheManager = CacheManager.getInstance(clientId.tenantId());
        // check if dpns exists and if there is a DPN registered for the wanted identifier.
        if (!cacheManager.dpnsCache.get(dpn.dpnId()).isPresent()) {
            // throw exception if DPN ID is not registered.
            throw new RuntimeException("DPN ID is not registered to the topology.");
        }

        // handle only 3GPP instructions.
        if (!(context.instructions().instrType() instanceof Instr3GppMob)) {
            throw new RuntimeException("No 3GPP instructions where given.");
        }

        // from DPN ID find the Network and Node Identifiers
        Optional<String> key = cacheManager.dpnsCache.get(dpn.dpnId())
                .map(node -> node.nodeId() + "/" + node.networkId());
        if (!key.isPresent()) {
            throw new RuntimeException("DPN does not have node and network ID defined.");
        }

        // get DPN Topic from Node/Network pair
        Short topic_id = getTopicFromNode(key.get());
        if (topic_id == null) {
            throw new RuntimeException("Could not find Topic ID");
        }

        if (!(context.dl().mobilityTunnelParameters().mobprofileParameters() instanceof ThreegppTunnel)) {
            throw new RuntimeException("mobprofileParameters are not instance of ThreegppTunnel");
        }

        Instr3GppMob instr3GppMob = (Instr3GppMob) context.instructions().instrType();
        String commands = Bits.toString(instr3GppMob.instr3GppMob().bits());

        Ip4Address s1u_enodeb_ipv4 = Ip4Address.valueOf(context.ul().tunnelLocalAddress().toString()),
                s1u_sgw_ipv4 = Ip4Address.valueOf(context.ul().tunnelRemoteAddress().toString());

        long s1u_enb_gtpu_teid = ((ThreegppTunnel) context.dl().mobilityTunnelParameters().mobprofileParameters()).tunnelIdentifier(),
                cId = clientId.clientId().fpcIdentity().union().int64(),
                contextId = context.contextId().fpcIdentity().union().int64();

        BigInteger opId = operationId.uint64();

        DefaultContexts convertContext = convertContext(context);
        if (commands.contains("downlink")) {
            if (context.dl().lifetime() >= 0L) {
                tasks.add(Executors.callable(() -> {
                    dpnCommunicationService.modify_bearer(
                            topic_id,
                            s1u_sgw_ipv4,
                            s1u_enb_gtpu_teid,
                            s1u_enodeb_ipv4,
                            contextId,
                            cId,
                            opId
                    );

                    ModelObjectId modelObjectId = defaultTenantBuilder()
                            .addChild(DefaultFpcMobility.class)
                            .build();
                    updateNode(convertContext, modelObjectId);
                    cacheManager.contextsCache.put(convertContext.contextId(), Optional.of(convertContext));
                }));
            } else {
                // TODO delete bearer
            }
        }
        if (commands.contains("uplink")) {
            if (context.ul().lifetime() >= 0L) {
                tasks.add(Executors.callable(() -> {
                    dpnCommunicationService.modify_bearer(
                            topic_id,
                            s1u_sgw_ipv4,
                            s1u_enb_gtpu_teid,
                            s1u_enodeb_ipv4,
                            contextId,
                            cId,
                            opId
                    );

                    ModelObjectId modelObjectId = defaultTenantBuilder()
                            .addChild(DefaultFpcMobility.class)
                            .build();
                    updateNode(convertContext, modelObjectId);
                    cacheManager.contextsCache.put(convertContext.contextId(), Optional.of(convertContext));
                }));
            } else {
                // TODO delete bearer
            }
        }
        return tasks;
    }

    @Override
    public DefaultConfigureOutput configureUpdate(
            CreateOrUpdate update,
            DefaultRegisterClientInput clientInfo,
            OpIdentifier operationId
    ) throws Exception {
        DefaultConfigureOutput configureOutput = new DefaultConfigureOutput();
        Collection<Callable<Object>> tasks = new ArrayList<>();

        DefaultCommonSuccess defaultCommonSuccess = new DefaultCommonSuccess();
        for (Contexts context : update.contexts()) {
            // add updated context to response.
            defaultCommonSuccess.addToContexts(context);

            // check if contexts are populated and if they include the wanted context to update.
            if (!CacheManager.getInstance(clientInfo.tenantId()).contextsCache.get(context.contextId()).isPresent()) {
                // throw exception if wanted context does not exist.
                throw new RuntimeException("Context doesn't exist. Please issue create operation..");
            }

            for (Dpns dpn : context.dpns()) {
                update(dpn, context, clientInfo, operationId);
            }
        }

        // execute all tasks.
        ExecutorService executor = Executors.newWorkStealingPool();
        executor.invokeAll(tasks).forEach(
                future -> {
                    try {
                        future.get();
                    } catch (Exception e) {
                        log.error(ExceptionUtils.getFullStackTrace(e));
                        throw new RuntimeException(e);
                    }
                }
        );

        configureOutput.resultType(defaultCommonSuccess);
        configureOutput.result(Result.of(ResultEnum.OK));
        return configureOutput;
    }

    private Collection<Callable<Object>> delete(
            Dpns dpn,
            DefaultContexts context,
            DefaultRegisterClientInput clientId,
            OpIdentifier operationId,
            String target
    ) throws Exception {
        Collection<Callable<Object>> tasks = new ArrayList<>();
        CacheManager cacheManager = CacheManager.getInstance(clientId.tenantId());
        // check if dpns exists and if there is a DPN registered for the wanted identifier.
        if (!cacheManager.dpnsCache.get(dpn.dpnId()).isPresent()) {
            // throw exception if DPN ID is not registered.
            throw new RuntimeException("DPN ID is not registered to the topology.");
        }

        // from DPN ID find the Network and Node Identifiers
        Optional<String> key = cacheManager.dpnsCache.get(dpn.dpnId())
                .map(node -> node.nodeId() + "/" + node.networkId());
        if (!key.isPresent()) {
            throw new RuntimeException("DPN does not have node and network ID defined.");
        }

        // find DPN Topic from Node/Network ID pair.
        Short topic_id = getTopicFromNode(key.get());
        if (topic_id == null) {
            throw new RuntimeException("Could not find Topic ID");
        }

        if (!(context.ul().mobilityTunnelParameters().mobprofileParameters() instanceof ThreegppTunnel)) {
            throw new RuntimeException("mobprofileParameters are not instance of ThreegppTunnel");
        }

        Long teid = ((ThreegppTunnel) context.ul().mobilityTunnelParameters().mobprofileParameters()).tunnelIdentifier();
        long client_id = clientId.clientId().fpcIdentity().union().int64();
        BigInteger op_id = operationId.uint64();

        // TODO figure out what is going on.
        if (target.endsWith("ul") || target.endsWith("dl")) {
            // TODO delete bearer
        } else {
            tasks.add(Executors.callable(() -> {
                dpnCommunicationService.delete_session(
                        topic_id,
                        context.contextId().fpcIdentity().union().int64(),
                        client_id,
                        op_id
                );

                ContextsKeys contextsKeys = new ContextsKeys();
                contextsKeys.contextId(context.contextId());

                ResourceId resourceVal = getResourceVal(defaultTenantBuilder()
                        .addChild(DefaultFpcMobility.class)
                        .addChild(DefaultContexts.class, contextsKeys)
                        .build());

                dynamicConfigService.deleteNode(resourceVal);
                cacheManager.contextsCache.put(context.contextId(), Optional.empty());
            }));
        }
        return tasks;
    }

    @Override
    public DefaultConfigureOutput configureDelete(
            DeleteOrQuery delete,
            DefaultRegisterClientInput clientInfo,
            OpIdentifier operationId
    ) throws Exception {
        DefaultConfigureOutput configureOutput = new DefaultConfigureOutput();
        Collection<Callable<Object>> tasks = new ArrayList<>();

        DefaultDeleteSuccess defaultDeleteSuccess = new DefaultDeleteSuccess();
        for (Targets target : delete.targets()) {
            defaultDeleteSuccess.addToTargets(target);
            // parse context id.
            String targetStr = target.target().toString(),
                    s = StringUtils.substringBetween(targetStr, "contexts=", "/"),
                    trgt = s != null ? s : StringUtils.substringAfter(targetStr, "contexts=");

            // find context that this target is about.
            FpcContextId fpcContextId = FpcContextId.of(FpcIdentity.fromString(trgt));
            Optional<DefaultContexts> defaultContexts = CacheManager.getInstance(clientInfo.tenantId()).contextsCache.get(fpcContextId);
            if (!defaultContexts.isPresent()) {
                throw new RuntimeException("Context doesn't exist. Please issue create operation..");
            }

            DefaultContexts context = defaultContexts.get();
            for (Dpns dpn : context.dpns()) {
                tasks = delete(dpn, context, clientInfo, operationId, targetStr);
            }
        }

        // execute all tasks
        ExecutorService executor = Executors.newWorkStealingPool();
        executor.invokeAll(tasks).forEach(
                future -> {
                    try {
                        future.get();
                    } catch (Exception e) {
                        log.error(ExceptionUtils.getFullStackTrace(e));
                        throw new RuntimeException(e);
                    }
                }
        );

        configureOutput.resultType(defaultDeleteSuccess);
        configureOutput.result(Result.of(ResultEnum.OK));

        return configureOutput;
    }

    @Override
    public DefaultConfigureDpnOutput configureDpnAdd(DefaultConfigureDpnInput input) throws Exception {
        DefaultConfigureDpnOutput defaultConfigureDpnOutput = new DefaultConfigureDpnOutput();
        boolean done = false;

        final FpcDpnId fpcDpnId = input.inputDpnId(),
                abstractDpnId = input.abstractDpnId();
        if (fpcDpnId == null || abstractDpnId == null) {
            throw new RuntimeException("DPN and Abstract DPN must be provided.");
        }

        for (CacheManager cacheManager : CacheManager.cacheInfo.values()) {
            final Optional<DefaultDpns> optionalDpn = cacheManager.dpnsCache.get(fpcDpnId),
                    optionalvDpn = cacheManager.dpnsCache.get(abstractDpnId);

            if (!optionalDpn.isPresent() || !optionalvDpn.isPresent()) {
                continue;
            }

            if (optionalDpn.map(DefaultDpns::yangAutoPrefixAbstract).orElse(false) ||
                    !optionalvDpn.map(DefaultDpns::yangAutoPrefixAbstract).orElse(false)) {
                continue;
            }

            final DefaultDpns dpn = optionalDpn.get(),
                    vdpn = optionalvDpn.get();

            if (vdpn.dpnIds() == null) {
                vdpn.dpnIds(new ArrayList<>());
            }

            if (!vdpn.dpnIds().contains(dpn.dpnId())) {
                // TODO copy contexts
                vdpn.addToDpnIds(dpn.dpnId());
                done = true;
            }
        }

        if (!done) {
            throw new RuntimeException("Could not process");
        }

        defaultConfigureDpnOutput.resultType(new DefaultEmptyCase());
        defaultConfigureDpnOutput.result(Result.of(ResultEnum.OK));

        return defaultConfigureDpnOutput;
    }

    @Override
    public DefaultConfigureDpnOutput configureDpnRemove(DefaultConfigureDpnInput input) throws Exception {
        DefaultConfigureDpnOutput defaultConfigureDpnOutput = new DefaultConfigureDpnOutput();
        boolean done = false;

        final FpcDpnId fpcDpnId = input.inputDpnId(),
                abstractDpnId = input.abstractDpnId();
        if (fpcDpnId == null || abstractDpnId == null) {
            throw new RuntimeException("DPN and Abstract DPN must be provided.");
        }

        for (CacheManager cacheManager : CacheManager.cacheInfo.values()) {
            final Optional<DefaultDpns> optionalDpn = cacheManager.dpnsCache.get(fpcDpnId),
                    optionalvDpn = cacheManager.dpnsCache.get(abstractDpnId);
            if (!optionalDpn.isPresent() || !optionalvDpn.isPresent()) {
                throw new RuntimeException("DPN or Abstract DPN specified does not exists.");
            }

            if (optionalDpn.map(DefaultDpns::yangAutoPrefixAbstract).orElse(false) ||
                    !optionalvDpn.map(DefaultDpns::yangAutoPrefixAbstract).orElse(false)) {
                throw new RuntimeException("Requested DPN is Abstract or requested Abstract DPN is not abstract.");
            }

            final DefaultDpns dpn = optionalDpn.get(),
                    vdpn = optionalvDpn.get();

            if (vdpn.dpnIds() == null || !vdpn.dpnIds().contains(dpn.dpnId())) {
                throw new RuntimeException("DPN is not part of the vDPN");
            }

            vdpn.dpnIds().remove(dpn.dpnId());
            // TODO remove contexts
            done = true;
        }

        if (!done) {
            throw new RuntimeException("Could not process");
        }

        DefaultCommonDeleteSuccess defaultCommonDeleteSuccess = new DefaultCommonDeleteSuccess();
        defaultConfigureDpnOutput.resultType(defaultCommonDeleteSuccess);
        defaultConfigureDpnOutput.result(Result.of(ResultEnum.OK));

        return defaultConfigureDpnOutput;
    }

    /**
     * Accumulates events to allow processing after a desired number of
     * events were accumulated.
     */
    private class InternalEventAccumulator extends AbstractAccumulator<DynamicConfigEvent> {

        /**
         * Constructs the event accumulator with timer and event limit.
         */
        private InternalEventAccumulator() {
            super(new Timer(TIMER), MAX_EVENTS, MAX_BATCH_MS, MAX_IDLE_MS);
        }

        @Override
        public void processItems(List<DynamicConfigEvent> events) {
            events.forEach(
                    event -> {
                        checkNotNull(event, EVENT_NULL);
                        switch (event.type()) {
                            case NODE_ADDED:
                            case NODE_DELETED:
                            case NODE_UPDATED:
                            case NODE_REPLACED:
//                                List<NodeKey> nodeKeys = event.subject().nodeKeys();
//                                if (nodeKeys.size() >= 4) {
//                                    NodeKey nodeKey = nodeKeys.get(3);
//                                    if (nodeKey.schemaId().name().equals("dpns") && nodeKey instanceof ListKey) {
//                                        Object dpnId = ((ListKey) nodeKey).keyLeafs().get(0).leafValue();
//                                        cacheManager.dpnsCache.invalidate(FpcDpnId.fromString(dpnId.toString()));
//                                    } else if (nodeKey.schemaId().name().equals("contexts") && nodeKey instanceof ListKey) {
//                                        Object contextId = ((ListKey) nodeKey).keyLeafs().get(0).leafValue();
//                                        cacheManager.contextsCache.invalidate(FpcContextId.fromString(contextId.toString()));
//                                    }
//                                }
                                break;
                            default:
                                log.warn(UNKNOWN_EVENT, event.type());
                                break;
                        }
                    }
            );
        }

    }

    /**
     * Representation of internal listener, listening for dynamic config event.
     */
    private class InternalConfigListener implements DynamicConfigListener {
        /**
         * Returns true if the event resource id points to the root level node
         * only and event is for addition and deletion; false otherwise.
         *
         * @param event config event
         * @return true if event is supported; false otherwise
         */
        private boolean isSupported(DynamicConfigEvent event) {
            ResourceId rsId = event.subject();
            List<NodeKey> storeKeys = rsId.nodeKeys();
            List<NodeKey> regKeys = tenants.nodeKeys();
            // store[0] = tenants, reg[0] = /
            if (storeKeys != null) {
                int storeSize = storeKeys.size();
                if (storeSize >= 4) {
                    return storeKeys.get(0).equals(regKeys.get(1));
                }
            }
            return false;
        }

        @Override
        public boolean isRelevant(DynamicConfigEvent event) {
            return isSupported(event);
        }

        @Override
        public void event(DynamicConfigEvent event) {
            accumulator.add(event);
        }
    }
}
