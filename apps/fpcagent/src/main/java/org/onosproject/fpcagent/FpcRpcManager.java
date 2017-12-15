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

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.felix.scr.annotations.*;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.Ip4Prefix;
import org.onosproject.config.DynamicConfigService;
import org.onosproject.config.DynamicConfigStore;
import org.onosproject.fpcagent.protocols.DpnCommunicationService;
import org.onosproject.fpcagent.protocols.DpnNgicCommunicator;
import org.onosproject.fpcagent.protocols.DpnP4Communicator;
import org.onosproject.fpcagent.util.CacheManager;
import org.onosproject.fpcagent.util.FpcUtil;
import org.onosproject.net.device.DeviceStore;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.DefaultConnectionInfo;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.P4DpnControlProtocol;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.ZmqDpnControlProtocol;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.connectioninfo.ConnectionsKeys;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.connectioninfo.DefaultConnections;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.deregisterclient.DefaultDeregisterClientOutput;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.registerclient.DefaultRegisterClientInput;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.registerclient.DefaultRegisterClientOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.IetfDmmFpcagentService;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.*;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configure.DefaultConfigureInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configure.DefaultConfigureOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configurebundles.DefaultConfigureBundlesInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configurebundles.DefaultConfigureBundlesOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configurebundles.configurebundlesoutput.Bundles;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configurebundles.configurebundlesoutput.DefaultBundles;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configuredpn.DefaultConfigureDpnInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configuredpn.DefaultConfigureDpnOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.instructions.Instructions;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.instructions.instructions.instrtype.Instr3GppMob;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.CreateOrUpdate;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.DeleteOrQuery;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.payload.Contexts;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.result.ResultEnum;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbody.resulttype.DefaultCommonSuccess;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbody.resulttype.DefaultDeleteSuccess;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbody.resulttype.DefaultErr;
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
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcDpnControlProtocol;
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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.*;

import static org.onosproject.fpcagent.util.Converter.convertContext;
import static org.onosproject.fpcagent.util.FpcUtil.*;

@Component(immediate = true)
@Service
public class FpcRpcManager implements FpcRpcService, IetfDmmFpcagentService, org.onosproject.yang.gen.v1.fpc.rev20150105.FpcService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private ModelConverter modelConverter;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private DynamicConfigService dynamicConfigService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private DynamicConfigStore dynamicConfigStore;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private RpcRegistry registry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private DeviceStore deviceStore;

    private ConcurrentMap<ClientIdentifier, DefaultRegisterClientInput> clientInfo = Maps.newConcurrentMap();
    // FIXME configurable
    private ExecutorService executorService = Executors.newFixedThreadPool(25);

    @Activate
    protected void activate() {
        init();
        registry.registerRpcService(this);
        log.info("FPC RPC Service Started");
    }

    @Deactivate
    protected void deactivate() {
        registry.unregisterRpcService(this);
        log.info("FPC RPC Service Stopped");
    }

    private void init() {
        FpcUtil.modelConverter = modelConverter;
        FpcUtil.dynamicConfigService = dynamicConfigService;
        FpcUtil.deviceStore = deviceStore;
        getResourceId();

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
    }

    @Override
    public DefaultConfigureOutput configureCreate(
            CreateOrUpdate create,
            DefaultRegisterClientInput clientInfo,
            OpIdentifier operationId
    ) throws Exception {
        DefaultConfigureOutput configureOutput = new DefaultConfigureOutput();
        Collection<Callable<Object>> tasks = new ArrayList<>();
        FpcIdentity tenantId = clientInfo.tenantId();
        DefaultCommonSuccess defaultCommonSuccess = new DefaultCommonSuccess();
        CacheManager cacheManager = CacheManager.getInstance(tenantId);

        for (Contexts context : create.contexts()) {
            Instructions instructions = context.instructions();
            if (instructions == null) {
                throw new RuntimeException("Instructions are empty.");
            }
            // add context to response.
            defaultCommonSuccess.addToContexts(context);

            // check if mobility exists and if the context id exists.
            if (CacheManager.getInstance(tenantId).contextsCache.get(context.contextId()).isPresent()) {
                // throw exception if trying to create a Context that already exists.
                throw new RuntimeException("Context tried to create already exists. Please issue update operation..");
            }

            for (Dpns dpn : context.dpns()) {
                Optional<DefaultDpns> optionalDpn = cacheManager.dpnsCache.get(dpn.dpnId());
                // check if dpns exists and if there is a DPN registered for the wanted identifier.
                if (!optionalDpn.isPresent()) {
                    // throw exception if DPN ID is not registered.
                    throw new RuntimeException("DPN ID is not registered to the topology.");
                }

                final DpnCommunicationService dpnCommunicationService;
                Class<? extends FpcDpnControlProtocol> controlProtocol = optionalDpn.get().controlProtocol();
                if (controlProtocol.isAssignableFrom(ZmqDpnControlProtocol.class)) {
                    dpnCommunicationService = new DpnNgicCommunicator();
                } else if (controlProtocol.isAssignableFrom(P4DpnControlProtocol.class)) {
                    dpnCommunicationService = new DpnP4Communicator();
                } else {
                    throw new RuntimeException("Control Protocol is not supported.");
                }

                // handle only 3GPP instructions.
                if (!(instructions.instrType() instanceof Instr3GppMob)) {
                    throw new RuntimeException("No 3GPP instructions where given.");
                }

                // from DPN ID find the Network and Node Identifiers
                Optional<String> key = optionalDpn.map(node -> node.nodeId() + "/" + node.networkId());
                if (!key.isPresent()) {
                    throw new RuntimeException("DPN does not have node and network ID defined.");
                }

                // get DPN Topic from Node/Network pair
                byte topic_id = getTopicFromNode(key.get());
                if (topic_id == -1) {
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
                Instr3GppMob instr3GppMob = (Instr3GppMob) instructions.instrType();
                String commands = Bits.toString(instr3GppMob.instr3GppMob().bits());

                Ip4Address s1u_enodeb_ipv4 = Ip4Address.valueOf(context.ul().tunnelLocalAddress().toString()),
                        s1u_sgw_ipv4 = Ip4Address.valueOf(context.ul().tunnelRemoteAddress().toString());

                long client_id = clientInfo.clientId().fpcIdentity().union().int64(),
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

                        ModelObjectId modelObjectId = tenantBuilder(tenantId)
                                .addChild(DefaultFpcMobility.class)
                                .build();
                        createNode(convertContext, modelObjectId);
                        cacheManager.contextsCache.put(convertContext.contextId(), Optional.of(convertContext));
                    }));

                    // FIXME why downlink is in session while uplink is not?
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

                            ModelObjectId modelObjectId = tenantBuilder(tenantId)
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

    @Override
    public DefaultConfigureOutput configureUpdate(
            CreateOrUpdate update,
            DefaultRegisterClientInput clientInfo,
            OpIdentifier operationId
    ) throws Exception {
        DefaultConfigureOutput configureOutput = new DefaultConfigureOutput();
        Collection<Callable<Object>> tasks = new ArrayList<>();
        FpcIdentity tenantId = clientInfo.tenantId();
        DefaultCommonSuccess defaultCommonSuccess = new DefaultCommonSuccess();
        CacheManager cacheManager = CacheManager.getInstance(tenantId);
        for (Contexts context : update.contexts()) {
            Instructions instructions = context.instructions();
            if (instructions == null) {
                throw new RuntimeException("Instructions are empty.");
            }
            // add updated context to response.
            defaultCommonSuccess.addToContexts(context);

            // check if contexts are populated and if they include the wanted context to update.
            if (!CacheManager.getInstance(tenantId).contextsCache.get(context.contextId()).isPresent()) {
                // throw exception if wanted context does not exist.
                throw new RuntimeException("Context doesn't exist. Please issue create operation..");
            }

            for (Dpns dpn : context.dpns()) {
                Optional<DefaultDpns> optionalDpn = cacheManager.dpnsCache.get(dpn.dpnId());
                // check if dpns exists and if there is a DPN registered for the wanted identifier.
                if (!optionalDpn.isPresent()) {
                    // throw exception if DPN ID is not registered.
                    throw new RuntimeException("DPN ID is not registered to the topology.");
                }

                final DpnCommunicationService dpnCommunicationService;
                Class<? extends FpcDpnControlProtocol> controlProtocol = optionalDpn.get().controlProtocol();
                if (controlProtocol.isAssignableFrom(ZmqDpnControlProtocol.class)) {
                    dpnCommunicationService = new DpnNgicCommunicator();
                } else if (controlProtocol.isAssignableFrom(P4DpnControlProtocol.class)) {
                    dpnCommunicationService = new DpnP4Communicator();
                } else {
                    throw new RuntimeException("Control Protocol is not supported.");
                }

                // handle only 3GPP instructions.
                if (!(instructions.instrType() instanceof Instr3GppMob)) {
                    throw new RuntimeException("No 3GPP instructions where given.");
                }

                // from DPN ID find the Network and Node Identifiers
                Optional<String> key = optionalDpn
                        .map(node -> node.nodeId() + "/" + node.networkId());
                if (!key.isPresent()) {
                    throw new RuntimeException("DPN does not have node and network ID defined.");
                }

                // get DPN Topic from Node/Network pair
                byte topic_id = getTopicFromNode(key.get());
                if (topic_id == -1) {
                    throw new RuntimeException("Could not find Topic ID");
                }

                if (!(context.dl().mobilityTunnelParameters().mobprofileParameters() instanceof ThreegppTunnel)) {
                    throw new RuntimeException("mobprofileParameters are not instance of ThreegppTunnel");
                }

                Instr3GppMob instr3GppMob = (Instr3GppMob) instructions.instrType();
                String commands = Bits.toString(instr3GppMob.instr3GppMob().bits());

                Ip4Address s1u_enodeb_ipv4 = Ip4Address.valueOf(context.ul().tunnelLocalAddress().toString()),
                        s1u_sgw_ipv4 = Ip4Address.valueOf(context.ul().tunnelRemoteAddress().toString());

                long s1u_enb_gtpu_teid = ((ThreegppTunnel) context.dl().mobilityTunnelParameters().mobprofileParameters()).tunnelIdentifier(),
                        cId = clientInfo.clientId().fpcIdentity().union().int64(),
                        contextId = context.contextId().fpcIdentity().union().int64();

                BigInteger opId = operationId.uint64();

                // TODO dpn.direction()

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

                            ModelObjectId modelObjectId = tenantBuilder(tenantId)
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

                            ModelObjectId modelObjectId = tenantBuilder(tenantId)
                                    .addChild(DefaultFpcMobility.class)
                                    .build();
                            updateNode(convertContext, modelObjectId);
                            cacheManager.contextsCache.put(convertContext.contextId(), Optional.of(convertContext));
                        }));
                    } else {
                        // TODO delete bearer
                    }
                }
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

    @Override
    public DefaultConfigureOutput configureDelete(
            DeleteOrQuery delete,
            DefaultRegisterClientInput clientInfo,
            OpIdentifier operationId
    ) throws Exception {
        DefaultConfigureOutput configureOutput = new DefaultConfigureOutput();
        Collection<Callable<Object>> tasks = new ArrayList<>();
        FpcIdentity tenantId = clientInfo.tenantId();
        CacheManager cacheManager = CacheManager.getInstance(tenantId);
        DefaultDeleteSuccess defaultDeleteSuccess = new DefaultDeleteSuccess();
        for (Targets target : delete.targets()) {
            defaultDeleteSuccess.addToTargets(target);
            // parse context id.
            String targetStr = target.target().toString(),
                    s = StringUtils.substringBetween(targetStr, "contexts=", "/"),
                    trgt = s != null ? s : StringUtils.substringAfter(targetStr, "contexts=");

            // find context that this target is about.
            FpcContextId fpcContextId = FpcContextId.of(FpcIdentity.fromString(trgt));
            Optional<DefaultContexts> defaultContexts = CacheManager.getInstance(tenantId).contextsCache.get(fpcContextId);
            if (!defaultContexts.isPresent()) {
                throw new RuntimeException("Context doesn't exist. Please issue create operation..");
            }

            DefaultContexts context = defaultContexts.get();
            for (Dpns dpn : context.dpns()) {
                Optional<DefaultDpns> optionalDpn = cacheManager.dpnsCache.get(dpn.dpnId());
                // check if dpns exists and if there is a DPN registered for the wanted identifier.
                if (!optionalDpn.isPresent()) {
                    // throw exception if DPN ID is not registered.
                    throw new RuntimeException("DPN ID is not registered to the topology.");
                }

                final DpnCommunicationService dpnCommunicationService;
                Class<? extends FpcDpnControlProtocol> controlProtocol = optionalDpn.get().controlProtocol();
                if (controlProtocol.isAssignableFrom(ZmqDpnControlProtocol.class)) {
                    dpnCommunicationService = new DpnNgicCommunicator();
                } else if (controlProtocol.isAssignableFrom(P4DpnControlProtocol.class)) {
                    dpnCommunicationService = new DpnP4Communicator();
                } else {
                    throw new RuntimeException("Control Protocol is not supported.");
                }

                // from DPN ID find the Network and Node Identifiers
                Optional<String> key = optionalDpn
                        .map(node -> node.nodeId() + "/" + node.networkId());
                if (!key.isPresent()) {
                    throw new RuntimeException("DPN does not have node and network ID defined.");
                }

                // find DPN Topic from Node/Network ID pair.
                byte topic_id = getTopicFromNode(key.get());
                if (topic_id == -1) {
                    throw new RuntimeException("Could not find Topic ID");
                }

                if (!(context.ul().mobilityTunnelParameters().mobprofileParameters() instanceof ThreegppTunnel)) {
                    throw new RuntimeException("mobprofileParameters are not instance of ThreegppTunnel");
                }

                Long teid = ((ThreegppTunnel) context.ul().mobilityTunnelParameters().mobprofileParameters()).tunnelIdentifier();
                long client_id = clientInfo.clientId().fpcIdentity().union().int64();
                BigInteger op_id = operationId.uint64();

                // TODO figure out what is going on.
                if (targetStr.endsWith("ul") || targetStr.endsWith("dl")) {
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

                        ResourceId resourceVal = getResourceVal(tenantBuilder(tenantId)
                                .addChild(DefaultFpcMobility.class)
                                .addChild(DefaultContexts.class, contextsKeys)
                                .build());

                        dynamicConfigService.deleteNode(resourceVal);
                        cacheManager.contextsCache.put(context.contextId(), Optional.empty());
                    }));
                }
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

    @Override
    public RpcOutput configureDpn(RpcInput rpcInput) {
        return CompletableFuture.supplyAsync(() -> {
            Instant start = Instant.now();
            DefaultConfigureDpnOutput configureDpnOutput = new DefaultConfigureDpnOutput();
            configureDpnOutput.result(Result.of(ResultEnum.OK));
            configureDpnOutput.resultType(new DefaultEmptyCase());
            RpcOutput.Status status = RpcOutput.Status.RPC_SUCCESS;
            try {
                for (ModelObject modelObject : getModelObjects(rpcInput.data(), configureDpn)) {
                    DefaultConfigureDpnInput input = (DefaultConfigureDpnInput) modelObject;
                    switch (input.operation().enumeration()) {
                        case ADD:
                            configureDpnOutput = configureDpnAdd(input);
                            break;
                        case REMOVE:
                            configureDpnOutput = configureDpnRemove(input);
                            break;
                    }
                }
            } catch (Exception e) {
                status = RpcOutput.Status.RPC_FAILURE;
                org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbodydpn.resulttype.DefaultErr defaultErr = new org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbodydpn.resulttype.DefaultErr();
                defaultErr.errorInfo(ExceptionUtils.getFullStackTrace(e));
                defaultErr.errorTypeId(ErrorTypeId.of(0));
                configureDpnOutput.resultType(defaultErr);
                configureDpnOutput.result(Result.of(ResultEnum.ERR));
                log.error(ExceptionUtils.getFullStackTrace(e));
            }
            ResourceData dataNode = modelConverter.createDataNode(
                    DefaultModelObjectData.builder()
                            .addModelObject(configureDpnOutput)
                            .build()
            );
            log.info("Time Elapsed {} ms", Duration.between(start, Instant.now()).toMillis());
            return new RpcOutput(status, dataNode.dataNodes().get(0));
        }, executorService).join();
    }

    @Override
    public RpcOutput configure(RpcInput rpcInput) {
        return CompletableFuture.supplyAsync(() -> {
            Instant start = Instant.now();
            DefaultConfigureOutput configureOutput = new DefaultConfigureOutput();
            RpcOutput.Status status = RpcOutput.Status.RPC_SUCCESS;
            try {
                for (ModelObject modelObject : getModelObjects(rpcInput.data(), configure)) {
                    DefaultConfigureInput input = (DefaultConfigureInput) modelObject;
                    if (!clientInfo.containsKey(input.clientId())) {
                        throw new RuntimeException("Client Identifier is not registered.");
                    }
                    switch (input.opType()) {
                        case CREATE:
                            configureOutput = configureCreate(
                                    (CreateOrUpdate) input.opBody(),
                                    clientInfo.get(input.clientId()),
                                    input.opId()
                            );
                            break;
                        case UPDATE:
                            configureOutput = configureUpdate(
                                    (CreateOrUpdate) input.opBody(),
                                    clientInfo.get(input.clientId()),
                                    input.opId()
                            );
                            break;
                        case QUERY:
                            break;
                        case DELETE:
                            configureOutput = configureDelete(
                                    (DeleteOrQuery) input.opBody(),
                                    clientInfo.get(input.clientId()),
                                    input.opId()
                            );
                            break;
                    }
                    configureOutput.opId(input.opId());
                }
            } catch (Exception e) {
                // if there is an exception respond with an error.
                status = RpcOutput.Status.RPC_FAILURE;
                DefaultErr defaultErr = new DefaultErr();
                defaultErr.errorInfo(ExceptionUtils.getFullStackTrace(e));
                defaultErr.errorTypeId(ErrorTypeId.of(0));
                configureOutput.resultType(defaultErr);
                configureOutput.result(Result.of(ResultEnum.ERR));
                log.error(ExceptionUtils.getFullStackTrace(e));
            }
            ResourceData dataNode = modelConverter.createDataNode(
                    DefaultModelObjectData.builder()
                            .addModelObject(configureOutput)
                            .build()
            );
            log.info("Time Elapsed {} ms", Duration.between(start, Instant.now()).toMillis());
            return new RpcOutput(status, dataNode.dataNodes().get(0));
        }, executorService).join();
    }

    @Override
    public RpcOutput configureBundles(RpcInput rpcInput) {
        return CompletableFuture.supplyAsync(() -> {
            Instant start = Instant.now();
            DefaultConfigureBundlesOutput configureBundlesOutput = new DefaultConfigureBundlesOutput();
            RpcOutput.Status status = RpcOutput.Status.RPC_SUCCESS;
            try {
                for (ModelObject modelObject : getModelObjects(rpcInput.data(), configureBundles)) {
                    DefaultConfigureBundlesInput input = (DefaultConfigureBundlesInput) modelObject;
                    if (!clientInfo.containsKey(input.clientId())) {
                        throw new RuntimeException("Client Identifier is not registered.");
                    }
                    for (org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configurebundles.configurebundlesinput.Bundles bundle : input.bundles()) {
                        DefaultConfigureOutput configureOutput = new DefaultConfigureOutput();
                        switch (bundle.opType()) {
                            case CREATE:
                                configureOutput = configureCreate(
                                        (CreateOrUpdate) bundle.opBody(),
                                        clientInfo.get(input.clientId()),
                                        bundle.opId()
                                );
                                break;
                            case UPDATE:
                                configureOutput = configureUpdate(
                                        (CreateOrUpdate) bundle.opBody(),
                                        clientInfo.get(input.clientId()),
                                        bundle.opId()
                                );
                                break;
                            case QUERY:
                                break;
                            case DELETE:
                                configureOutput = configureDelete(
                                        (DeleteOrQuery) bundle.opBody(),
                                        clientInfo.get(input.clientId()),
                                        bundle.opId()
                                );
                                break;
                        }
                        Bundles result = new DefaultBundles();
                        result.opId(bundle.opId());
                        result.result(configureOutput.result());
                        result.resultType(configureOutput.resultType());
                        configureBundlesOutput.addToBundles(result);
                    }
                }
            } catch (Exception e) {
                // if there is an exception respond with an error.
                status = RpcOutput.Status.RPC_FAILURE;
                log.error(ExceptionUtils.getFullStackTrace(e));
            }
            ResourceData dataNode = modelConverter.createDataNode(
                    DefaultModelObjectData.builder()
                            .addModelObject(configureBundlesOutput)
                            .build()
            );
            log.info("Time Elapsed {} ms", Duration.between(start, Instant.now()).toMillis());
            return new RpcOutput(status, dataNode.dataNodes().get(0));
        }, executorService).join();
    }

    @Override
    public RpcOutput eventRegister(RpcInput rpcInput) {
        Instant start = Instant.now();
        CompletableFuture.supplyAsync(() -> {
            // TODO implement
            return null;
        }, executorService);
        log.info("Time Elapsed {} ms", Duration.between(start, Instant.now()).toMillis());
        return null;
    }

    @Override
    public RpcOutput eventDeregister(RpcInput rpcInput) {
        Instant start = Instant.now();
        CompletableFuture.supplyAsync(() -> {
            // TODO implement
            return null;
        }, executorService);
        log.info("Time Elapsed {} ms", Duration.between(start, Instant.now()).toMillis());
        return null;
    }

    @Override
    public RpcOutput probe(RpcInput rpcInput) {
        Instant start = Instant.now();
        CompletableFuture.supplyAsync(() -> {
            // TODO implement
            return null;
        }, executorService);
        log.info("Time Elapsed {} ms", Duration.between(start, Instant.now()).toMillis());
        return null;
    }

    @Override
    public RpcOutput registerClient(RpcInput rpcInput) {
        return CompletableFuture.supplyAsync(() -> {
            Instant start = Instant.now();
            DefaultRegisterClientOutput registerClientOutput = new DefaultRegisterClientOutput();
            RpcOutput.Status status = RpcOutput.Status.RPC_SUCCESS;
            try {
                for (ModelObject modelObject : getModelObjects(rpcInput.data(), registerClient)) {
                    DefaultRegisterClientInput input = (DefaultRegisterClientInput) modelObject;
                    if (clientInfo.containsKey(input.clientId())) {
                        throw new RuntimeException("Client already registered.");
                    }
                    clientInfo.put(input.clientId(), input);
                    registerClientOutput.clientId(input.clientId());
                    registerClientOutput.supportedFeatures(input.supportedFeatures());
                    registerClientOutput.endpointUri(input.endpointUri());
                    registerClientOutput.supportsAckModel(input.supportsAckModel());
                    registerClientOutput.tenantId(input.tenantId());

                    DefaultConnections defaultConnections = new DefaultConnections();
                    defaultConnections.clientId(input.clientId().toString());

                    ModelObjectId modelObjectId = ModelObjectId.builder()
                            .addChild(DefaultConnectionInfo.class)
                            .build();
                    createNode(defaultConnections, modelObjectId);
                }
            } catch (Exception e) {
                // if there is an exception respond with an error.
                status = RpcOutput.Status.RPC_FAILURE;
                log.error(ExceptionUtils.getFullStackTrace(e));
            }

            ResourceData dataNode = modelConverter.createDataNode(
                    DefaultModelObjectData.builder()
                            .addModelObject(registerClientOutput)
                            .build()
            );
            log.info("Time Elapsed {} ms", Duration.between(start, Instant.now()).toMillis());
            return new RpcOutput(status, dataNode.dataNodes().get(0));
        }).join();
    }

    @Override
    public RpcOutput deregisterClient(RpcInput rpcInput) {
        return CompletableFuture.supplyAsync(() -> {
            Instant start = Instant.now();
            DefaultDeregisterClientOutput deregisterClientOutput = new DefaultDeregisterClientOutput();
            RpcOutput.Status status = RpcOutput.Status.RPC_SUCCESS;

            try {
                for (ModelObject modelObject : getModelObjects(rpcInput.data(), registerClient)) {
                    DefaultRegisterClientInput input = (DefaultRegisterClientInput) modelObject;
                    if (!clientInfo.containsKey(input.clientId())) {
                        throw new RuntimeException("Client does not exist.");
                    }
                    clientInfo.remove(input.clientId());
                    deregisterClientOutput.clientId(input.clientId());

                    DefaultConnections defaultConnections = new DefaultConnections();
                    defaultConnections.clientId(input.clientId().toString());

                    ConnectionsKeys connectionsKeys = new ConnectionsKeys();
                    connectionsKeys.clientId(input.clientId().toString());

                    ResourceId resourceVal = getResourceVal(ModelObjectId.builder()
                            .addChild(DefaultConnectionInfo.class)
                            .addChild(DefaultConnections.class, connectionsKeys)
                            .build());

                    dynamicConfigService.deleteNode(resourceVal);
                }
            } catch (Exception e) {
                // if there is an exception respond with an error.
                status = RpcOutput.Status.RPC_FAILURE;
                log.error(ExceptionUtils.getFullStackTrace(e));
            }

            ResourceData dataNode = modelConverter.createDataNode(
                    DefaultModelObjectData.builder()
                            .addModelObject(deregisterClientOutput)
                            .build()
            );
            log.info("Time Elapsed {} ms", Duration.between(start, Instant.now()).toMillis());
            return new RpcOutput(status, dataNode.dataNodes().get(0));
        }, executorService).join();
    }

}
