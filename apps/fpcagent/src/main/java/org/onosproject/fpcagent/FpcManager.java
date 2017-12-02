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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.felix.scr.annotations.*;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.Ip4Prefix;
import org.onosproject.config.DynamicConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.fpcagent.helpers.ConfigHelper;
import org.onosproject.fpcagent.helpers.DpnApi;
import org.onosproject.fpcagent.workers.ZMQSBPublisherManager;
import org.onosproject.fpcagent.workers.ZMQSBSubscriberManager;
import org.onosproject.net.config.*;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.IetfDmmFpcagentOpParam;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.IetfDmmFpcagentService;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.DefaultTenants;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.Result;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configure.DefaultConfigureInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configure.DefaultConfigureOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configuredpn.DefaultConfigureDpnInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configuredpn.DefaultConfigureDpnOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.instructions.instructions.instrtype.Instr3GppMob;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opheader.OpTypeEnum;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.CreateOrUpdate;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.DeleteOrQuery;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.payload.Contexts;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.result.ResultEnum;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbody.resulttype.DefaultCommonSuccess;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbodydpn.resulttype.DefaultEmptyCase;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.mobilityinfo.mobprofileparameters.ThreegppTunnel;
import org.onosproject.yang.gen.v1.ietfdmmthreegpp.rev20160803.ietfdmmthreegpp.threegppinstr.Bits;
import org.onosproject.yang.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static org.onosproject.fpcagent.helpers.Converter.getFpcIdentity;

/**
 * Fpc Manager.
 */
@Component(immediate = true)
@Service
public class FpcManager implements IetfDmmFpcagentService, FpcService {
    static final Logger log = LoggerFactory.getLogger(FpcManager.class);

    private static final String FPC_APP_ID = "org.onosproject.fpcagent";
    private static final Class<FpcConfig> CONFIG_CLASS = FpcConfig.class;
    private final InternalNetworkConfigListener configListener =
            new InternalNetworkConfigListener();

    /* Services */
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TenantService tenantService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    ModelConverter modelConverter;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    DynamicConfigService dynamicConfigService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private RpcRegistry rpcRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private NetworkConfigRegistry registry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private NetworkConfigService configService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private CoreService coreService;

    /* Variables */
    private ApplicationId appId;
    private FpcConfig fpcConfig;
    private IetfDmmFpcagentOpParam fpcData;

    private HashMap<String, ArrayList<Contexts>> sessionContextsMap = Maps.newHashMap();
    private HashMap<String, String> nodeNetworkMap = Maps.newHashMap();

    /* Config */
    private ConfigFactory<ApplicationId, FpcConfig> fpcConfigConfigFactory =
            new ConfigFactory<ApplicationId, FpcConfig>(
                    SubjectFactories.APP_SUBJECT_FACTORY, FpcConfig.class, "fpcagent") {
                @Override
                public FpcConfig createConfig() {
                    return new FpcConfig();
                }
            };

    @Activate
    protected void activate() {
        appId = coreService.registerApplication(FPC_APP_ID);
        configService.addListener(configListener);
        registry.registerConfigFactory(fpcConfigConfigFactory);

        fpcData = new IetfDmmFpcagentOpParam();

        rpcRegistry.registerRpcService(this);

        tenantService.addTenant(getFpcIdentity.apply("default"));

        DefaultTenants defaultTenants = new DefaultTenants();
        fpcData.tenants(defaultTenants);
        defaultTenants.addToTenant(tenantService.getTenant(getFpcIdentity.apply("default")).get());

        ResourceData dataNode = modelConverter.createDataNode(
                DefaultModelObjectData.builder()
                        .addModelObject(defaultTenants)
                        .build()
        );


        log.info("resourceData {} ", dataNode.dataNodes().size());
        dataNode.dataNodes().forEach(
                node -> dynamicConfigService.createNode(dataNode.resourceId(), node)
        );

        log.info("FPC Agent Started");
    }

    @Deactivate
    protected void deactivate() {
        configService.removeListener(configListener);
        registry.unregisterConfigFactory(fpcConfigConfigFactory);

        // TODO check if null before closing
        ZMQSBSubscriberManager.getInstance().close();
        ZMQSBPublisherManager.getInstance().close();

        rpcRegistry.unregisterRpcService(this);

        log.info("FPC Agent Stopped");
    }

    private void init() {
        fpcConfig.getConfig().ifPresent(
                helper -> {
                    ZMQSBSubscriberManager.createInstance(
                            helper.dpnListenerUri(),
                            helper.zmqBroadcastAll(),
                            helper.zmqBroadcastControllers(),
                            helper.zmqBroadcastDpns(),
                            helper.nodeId(),
                            helper.networkId()
                    );

                    ZMQSBPublisherManager.createInstance(
                            helper.dpnClientUri(),
                            helper.dpnClientThreads()
                    );

                    ZMQSBPublisherManager.getInstance().open();
                    ZMQSBSubscriberManager.getInstance().open();
                }
        );
    }

    @Override
    public Optional<ConfigHelper> getConfig() {
        return fpcConfig != null ? fpcConfig.getConfig() : Optional.empty();
    }

    @Override
    public RpcOutput configureDpn(RpcInput rpcInput) {
        log.info("RPC configure {}", rpcInput);

        DefaultConfigureDpnOutput defaultConfigureDpnOutput = new DefaultConfigureDpnOutput();
        defaultConfigureDpnOutput.result(Result.of(ResultEnum.OK));
        defaultConfigureDpnOutput.resultType(new DefaultEmptyCase());

        try {
            ResourceData data = DefaultResourceData.builder()
                    .addDataNode(rpcInput.data())
                    .resourceId(
                            ResourceId.builder()
                                    .addBranchPointSchema("configureDpn", "urn:ietf:params:xml:ns:yang:fpcagent")
                                    .build())
                    .build();

            ModelObjectData model = modelConverter.createModel(data);
            model.modelObjects().parallelStream().forEach(
                    modelObject -> {
                        DefaultConfigureDpnInput input = (DefaultConfigureDpnInput) modelObject;
                        String dpnId = input.inputDpnId().fpcIdentity().union().string();
                        switch (input.operation().enumeration()) {
                            case ADD:
                                log.info("Adding DPN {}", dpnId);
                                nodeNetworkMap.put(dpnId, dpnId + "/" + dpnId);
                                break;
                            case REMOVE:
                                log.info("Removing DPN {}", dpnId);
                                nodeNetworkMap.remove(dpnId);
                                break;
                        }
                    });
        } catch (Exception e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }

        ResourceData dataNode = modelConverter.createDataNode(
                DefaultModelObjectData.builder().addModelObject(defaultConfigureDpnOutput).build()
        );
        return new RpcOutput(RpcOutput.Status.RPC_SUCCESS, dataNode.dataNodes().get(0));
    }

    @Override
    public RpcOutput configure(RpcInput rpcInput) {
        log.info("RPC configure {}", rpcInput);

        DefaultConfigureOutput configureOutput = new DefaultConfigureOutput();
        configureOutput.result(Result.of(ResultEnum.OK));
        configureOutput.resultType(new DefaultCommonSuccess());

        try {
            ResourceData data = DefaultResourceData.builder()
                    .addDataNode(rpcInput.data())
                    .resourceId(
                            ResourceId.builder()
                                    .addBranchPointSchema("configure", "urn:ietf:params:xml:ns:yang:fpcagent")
                                    .build())
                    .build();

            ModelObjectData model = modelConverter.createModel(data);
            model.modelObjects().parallelStream().forEach(
                    modelObject -> {
                        DefaultConfigureInput input = (DefaultConfigureInput) modelObject;
                        configureOutput.opId(input.opId());
                        switch (input.opType()) {
                            case CREATE:
                            case UPDATE:
                                if (input.opBody() instanceof CreateOrUpdate) {
                                    log.info("Create Or Update");
                                    CreateOrUpdate createOrUpdate = (CreateOrUpdate) input.opBody();
                                    createOrUpdate.contexts().forEach(
                                            context -> {
                                                log.info("Context {}", context);
                                                String key = context.contextId().fpcIdentity().union().string();
                                                sessionContextsMap.computeIfAbsent(key, k -> new ArrayList<>()).add(context);
                                                context.dpns().forEach(
                                                        dpn -> {
                                                            if (context.instructions().instrType() instanceof Instr3GppMob) {
                                                                log.info("3GPP Instructions");
                                                                Instr3GppMob instr3GppMob = (Instr3GppMob) context.instructions().instrType();
                                                                String commands = Bits.toString(instr3GppMob.instr3GppMob().bits());

                                                                Ip4Address ulLocalAddress = Ip4Address.valueOf(context.ul().tunnelLocalAddress().toString()),
                                                                        dlRemoteAddress = Ip4Address.valueOf(context.dl().tunnelRemoteAddress().toString()),
                                                                        dlLocalAddress = Ip4Address.valueOf(context.dl().tunnelLocalAddress().toString());

                                                                long s1u_sgw_gtpu_teid, s1u_enb_gtpu_teid,
                                                                        clientId = input.clientId().fpcIdentity().union().int64(),
                                                                        contextId = context.contextId().fpcIdentity().union().int64();

                                                                BigInteger opId = input.opId().uint64(),
                                                                        imsi = context.imsi().uint64();

                                                                short ebi = context.ebi().uint8(),
                                                                        lbi = context.lbi().uint8();

                                                                Short dpnTopic = DpnApi.getTopicFromNode(nodeNetworkMap.get(dpn.dpnId().fpcIdentity().union().string()));

                                                                if (context.ul().mobilityTunnelParameters().mobprofileParameters() instanceof ThreegppTunnel) {
                                                                    s1u_sgw_gtpu_teid = ((ThreegppTunnel) context.ul().mobilityTunnelParameters().mobprofileParameters()).tunnelIdentifier();
                                                                } else {
                                                                    return;
                                                                }
                                                                if (context.dl().mobilityTunnelParameters().mobprofileParameters() instanceof ThreegppTunnel) {
                                                                    s1u_enb_gtpu_teid = ((ThreegppTunnel) context.dl().mobilityTunnelParameters().mobprofileParameters()).tunnelIdentifier();
                                                                } else {
                                                                    return;
                                                                }

                                                                if (input.opType().equals(OpTypeEnum.CREATE)) {
                                                                    if (commands.contains("session")) {
                                                                        log.info("CREATE session");
                                                                        DpnApi.create_session(
                                                                                dpnTopic,
                                                                                imsi,
                                                                                Ip4Prefix.valueOf(context.delegatingIpPrefixes().get(0).toString()).address(),
                                                                                ebi,
                                                                                ulLocalAddress,
                                                                                s1u_sgw_gtpu_teid,
                                                                                clientId,
                                                                                opId,
                                                                                contextId
                                                                        );

                                                                        if (commands.contains("downlink")) {
                                                                            log.info("CREATE session downlink");
                                                                            DpnApi.modify_bearer_dl(
                                                                                    dpnTopic,
                                                                                    s1u_sgw_gtpu_teid,
                                                                                    dlRemoteAddress,
                                                                                    s1u_enb_gtpu_teid,
                                                                                    clientId,
                                                                                    opId
                                                                            );
                                                                        }
                                                                    } else if (commands.contains("indirect-forward")) {
                                                                        // TODO - Modify API for Indirect Forwarding to/from another SGW
                                                                    } else if (commands.contains("uplink")) {
                                                                        log.info("CREATE uplink");
                                                                        DpnApi.create_bearer_ul(
                                                                                dpnTopic,
                                                                                imsi,
                                                                                lbi,
                                                                                ebi,
                                                                                ulLocalAddress,
                                                                                s1u_sgw_gtpu_teid
                                                                        );
                                                                    }
                                                                } else {
                                                                    if (commands.contains("downlink")) {
                                                                        log.info("UPDATE downlink");
                                                                        if (context.dl().lifetime() >= 0L) {
                                                                            DpnApi.modify_bearer_dl(
                                                                                    dpnTopic,
                                                                                    dlRemoteAddress,
                                                                                    s1u_enb_gtpu_teid,
                                                                                    dlLocalAddress,
                                                                                    clientId,
                                                                                    opId,
                                                                                    contextId
                                                                            );
                                                                        } else {
                                                                            DpnApi.delete_bearer(
                                                                                    dpnTopic,
                                                                                    s1u_enb_gtpu_teid
                                                                            );
                                                                        }
                                                                    }
                                                                    if (commands.contains("uplink")) {
                                                                        log.info("UPDATE uplink");
                                                                        if (context.ul().lifetime() >= 0L) {
                                                                            DpnApi.modify_bearer_ul(
                                                                                    dpnTopic,
                                                                                    ulLocalAddress,
                                                                                    s1u_enb_gtpu_teid,
                                                                                    s1u_sgw_gtpu_teid
                                                                            );
                                                                        } else {
                                                                            DpnApi.delete_bearer(
                                                                                    dpnTopic,
                                                                                    s1u_sgw_gtpu_teid
                                                                            );
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                );
                                            }
                                    );
                                }
                                break;
                            case QUERY:
                                break;
                            case DELETE:
                                if (input.opBody() instanceof DeleteOrQuery) {
                                    DeleteOrQuery deleteOrQuery = (DeleteOrQuery) input.opBody();
                                    log.info("Delete Or Query");

                                    deleteOrQuery.targets().forEach(
                                            target -> {
                                                log.info("target {}", target);
                                                String targetStr = target.target().union().string();
                                                sessionContextsMap.getOrDefault(targetStr, Lists.newArrayList()).parallelStream().forEach(
                                                        context -> {
                                                            log.info("context {}", context);
                                                            context.dpns().forEach(
                                                                    dpn -> {
                                                                        log.info("DPN {}", dpn);
                                                                        Long teid;
                                                                        if (context.ul().mobilityTunnelParameters().mobprofileParameters() instanceof ThreegppTunnel) {
                                                                            teid = ((ThreegppTunnel) context.ul().mobilityTunnelParameters().mobprofileParameters()).tunnelIdentifier();
                                                                        } else {
                                                                            return;
                                                                        }

                                                                        Short dpnTopic = DpnApi.getTopicFromNode(nodeNetworkMap.get(dpn.dpnId().fpcIdentity().union().string()));

                                                                        if (targetStr.endsWith("ul") || targetStr.endsWith("dl")) {
                                                                            log.info("DELETE Bearer");
                                                                            DpnApi.delete_bearer(
                                                                                    dpnTopic,
                                                                                    teid
                                                                            );
                                                                        } else {
                                                                            log.info("DELETE session");
                                                                            DpnApi.delete_session(
                                                                                    dpnTopic,
                                                                                    context.lbi().uint8(),
                                                                                    teid,
                                                                                    input.clientId().fpcIdentity().union().int64(),
                                                                                    input.opId().uint64(),
                                                                                    context.contextId().fpcIdentity().union().int64()
                                                                            );
                                                                        }
                                                                    }
                                                            );
                                                        }
                                                );
                                            }
                                    );
                                }
                                break;
                        }
                    }
            );


        } catch (Exception e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }

        ResourceData dataNode = modelConverter.createDataNode(
                DefaultModelObjectData.builder().addModelObject(configureOutput).build()
        );
        return new RpcOutput(RpcOutput.Status.RPC_SUCCESS, dataNode.dataNodes().get(0));
    }

    @Override
    public RpcOutput configureBundles(RpcInput rpcInput) {
        return null;
    }

    @Override
    public RpcOutput eventRegister(RpcInput rpcInput) {
        return null;
    }

    @Override
    public RpcOutput eventDeregister(RpcInput rpcInput) {
        return null;
    }

    @Override
    public RpcOutput probe(RpcInput rpcInput) {
        return null;
    }

//    @Override
//    public void registerClient(RegisterClientInput input) {
//        tenantService.registerClient(input.clientId(), input.tenantId());
//    }
//
//    @Override
//    public void deregisterClient(DeregisterClientInput input) {
//        tenantService.deregisterClient(input.clientId());
//    }

    private class InternalNetworkConfigListener implements NetworkConfigListener {

        @Override
        public void event(NetworkConfigEvent event) {
            switch (event.type()) {
                case CONFIG_REGISTERED:
                case CONFIG_UNREGISTERED: {
                    break;
                }
                case CONFIG_REMOVED: {
                    if (event.configClass() == CONFIG_CLASS) {
                        fpcConfig = null;
                    }
                    break;
                }
                case CONFIG_UPDATED:
                case CONFIG_ADDED: {
                    if (event.configClass() == CONFIG_CLASS) {
                        event.config().ifPresent(config -> {
                            fpcConfig = (FpcConfig) config;
                            init();
                        });
                    }
                    break;
                }
                default:
                    break;
            }
        }

    }
}
