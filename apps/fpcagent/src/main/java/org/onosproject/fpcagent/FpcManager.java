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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.felix.scr.annotations.*;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.Ip4Prefix;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.fpcagent.dto.configure.Context;
import org.onosproject.fpcagent.dto.dpn.AddDpn;
import org.onosproject.fpcagent.helpers.ConfigHelper;
import org.onosproject.fpcagent.helpers.DpnApi;
import org.onosproject.fpcagent.workers.ZMQSBPublisherManager;
import org.onosproject.fpcagent.workers.ZMQSBSubscriberManager;
import org.onosproject.net.config.*;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.deregisterclient.DeregisterClientInput;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.registerclient.RegisterClientInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configure.ConfigureInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.instructions.instructions.instrtype.Instr3GppMob;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opheader.OpTypeEnum;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.CreateOrUpdate;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.DeleteOrQuery;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.payload.Contexts;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.mobilityinfo.mobprofileparameters.ThreegppTunnel;
import org.onosproject.yang.gen.v1.ietfdmmthreegpp.rev20160803.ietfdmmthreegpp.threegppinstr.Bits;
import org.onosproject.yang.model.RpcInput;
import org.onosproject.yang.model.RpcOutput;
import org.onosproject.yang.model.RpcRegistry;
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
public class FpcManager implements Configure, FpcService {
    static final Logger log = LoggerFactory.getLogger(FpcManager.class);

    private static final String FPC_APP_ID = "org.onosproject.fpcagent";
    private static final Class<FpcConfig> CONFIG_CLASS = FpcConfig.class;
    private final InternalNetworkConfigListener configListener =
            new InternalNetworkConfigListener();

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TenantService tenantService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private RpcRegistry rpcRegistry;

    /* Services */
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private NetworkConfigRegistry registry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private NetworkConfigService configService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private CoreService coreService;

//    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
//    protected ModelConverter modelConverter;
//
//    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
//    protected DynamicConfigService dynamicConfigService;

    /* Variables */
    private ApplicationId appId;
    private FpcConfig fpcConfig;

    private HashMap<String, ArrayList<Contexts>> sessionContextsMap = Maps.newHashMap();
    private HashMap<String, ArrayList<Context>> sessionContextsRestMap = Maps.newHashMap();
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

        rpcRegistry.registerRpcService(this);

        tenantService.addTenant(getFpcIdentity.apply("default"));

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

    public void init() {

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

    @Override
    public RpcOutput configureDpn(RpcInput rpcInput) {
        return null;
    }

    @Override
    public RpcOutput configure(RpcInput rpcInput) {
        ConfigureInput input = (ConfigureInput) rpcInput;
        switch (input.opType()) {
            case CREATE:
            case UPDATE:
                if (input.opBody() instanceof CreateOrUpdate) {
                    CreateOrUpdate createOrUpdate = (CreateOrUpdate) input.opBody();
                    createOrUpdate.contexts().forEach(
                            context -> {
                                String key = context.contextId().fpcIdentity().union().string();
                                sessionContextsMap.computeIfAbsent(key, k -> new ArrayList<>())
                                        .add(context);
                                context.dpns().forEach(
                                        dpn -> {
                                            if (input.instructions().instrType() instanceof Instr3GppMob) {

                                                Instr3GppMob instr3GppMob = (Instr3GppMob) input.instructions().instrType();
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

                    deleteOrQuery.targets().forEach(
                            target -> {
                                String targetStr = target.target().union().string();
                                sessionContextsMap.getOrDefault(targetStr, Lists.newArrayList()).parallelStream().forEach(
                                        context -> context.dpns().forEach(
                                                dpn -> {
                                                    Long teid;
                                                    if (context.ul().mobilityTunnelParameters().mobprofileParameters() instanceof ThreegppTunnel) {
                                                        teid = ((ThreegppTunnel) context.ul().mobilityTunnelParameters().mobprofileParameters()).tunnelIdentifier();
                                                    } else {
                                                        return;
                                                    }

                                                    Short dpnTopic = DpnApi.getTopicFromNode(nodeNetworkMap.get(dpn.dpnId().fpcIdentity().union().string()));

                                                    if (targetStr.endsWith("ul") || targetStr.endsWith("dl")) {
                                                        DpnApi.delete_bearer(
                                                                dpnTopic,
                                                                teid
                                                        );
                                                    } else {
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
                                        )
                                );
                            }
                    );
                }
                break;
        }

        return null;
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

    @Override
    public RpcOutput registerClient(RpcInput rpcInput) {
        return null;
    }

    @Override
    public RpcOutput deregisterClient(RpcInput rpcInput) {
        return null;
    }

    @Override
    public void configure(org.onosproject.fpcagent.dto.configure.Configure configure) {
        org.onosproject.fpcagent.dto.configure.ConfigureInput configureInput = configure.getConfigureInput();
        if (configureInput.getOp_type().equals("create") || configureInput.getOp_type().equals("update")) {

            configureInput.getContexts().forEach(
                    context -> {
                        String key = String.valueOf(context.getContext_id());
                        sessionContextsRestMap.computeIfAbsent(key, k -> new ArrayList<>()).add(context);
                        context.getDpns().forEach(dpn -> {
                                    try {
                                        String commands = context.getInstructions().getInstr_3gpp_mob();

                                        Ip4Address ulLocalAddress = Ip4Address.valueOf(context.getUl().getTunnel_local_address()),
                                                dlRemoteAddress = Ip4Address.valueOf(context.getDl().getTunnel_remote_address());

                                        long s1u_sgw_gtpu_teid, s1u_enb_gtpu_teid,
                                                clientId = Long.parseLong(configureInput.getClient_id()),
                                                contextId = context.getContext_id();

                                        BigInteger opId = BigInteger.valueOf(Long.parseLong(configureInput.getOp_id())),
                                                imsi = BigInteger.valueOf(Long.parseLong(context.getImsi()));

                                        short ebi = Short.parseShort(context.getEbi()),
                                                lbi = Short.parseShort(context.getLbi());

                                        Short dpnTopic = DpnApi.getTopicFromNode(nodeNetworkMap.get(dpn.getDpn_id()));


                                        s1u_sgw_gtpu_teid = Long.parseLong(context.getUl().getMobility_tunnel_parameters().getTunnel_identifier());
                                        s1u_enb_gtpu_teid = Long.parseLong(context.getDl().getMobility_tunnel_parameters().getTunnel_identifier());

                                        if (configureInput.getOp_type().equals("create")) {
                                            if (commands.contains("session")) {
                                                DpnApi.create_session(
                                                        dpnTopic,
                                                        imsi,
                                                        Ip4Prefix.valueOf(context.getDelegating_ip_prefixes().get(0)).address(),
                                                        ebi,
                                                        ulLocalAddress,
                                                        s1u_sgw_gtpu_teid,
                                                        clientId,
                                                        opId,
                                                        contextId
                                                );


                                                if (commands.contains("downlink")) {
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
                                                DpnApi.delete_bearer(
                                                        dpnTopic,
                                                        s1u_enb_gtpu_teid
                                                );
                                            }
                                            if (commands.contains("uplink")) {
                                                DpnApi.delete_bearer(
                                                        dpnTopic,
                                                        s1u_sgw_gtpu_teid
                                                );
                                            }
                                        }
                                    } catch (Exception e) {
                                        log.error(ExceptionUtils.getFullStackTrace(e));
                                    }
                                }
                        );
                    }
            );

        } else if (configureInput.getOp_type().equals("delete") || configureInput.getOp_type().equals("query")) {
            configureInput.getTargets().forEach(
                    target -> {
                        String targetStr = target.getTarget();
                        String s = StringUtils.substringBetween(targetStr, "contexts/", "/");
                        if (s == null) {
                            s = StringUtils.substringAfter(targetStr, "contexts/");
                        }
                        sessionContextsRestMap.getOrDefault(s, Lists.newArrayList()).forEach(
                                context -> context.getDpns().forEach(
                                        dpn -> {
                                            Long teid = Long.valueOf(context.getUl().getMobility_tunnel_parameters().getTunnel_identifier());
                                            Short dpnTopic = DpnApi.getTopicFromNode(nodeNetworkMap.get(dpn.getDpn_id()));

                                            if (targetStr.endsWith("ul") || targetStr.endsWith("dl")) {
                                                DpnApi.delete_bearer(
                                                        dpnTopic,
                                                        teid
                                                );
                                            } else {
                                                DpnApi.delete_session(
                                                        dpnTopic,
                                                        Short.valueOf(context.getLbi()),
                                                        teid,
                                                        Long.valueOf(configureInput.getClient_id()),
                                                        BigInteger.valueOf(Long.parseLong(configureInput.getOp_id())),
                                                        Long.valueOf(context.getContext_id())
                                                );
                                            }
                                        }
                                )
                        );
                    }
            );
        }
    }

    @Override
    public void registerClient(RegisterClientInput input) {
        tenantService.registerClient(input.clientId(), input.tenantId());
    }

    @Override
    public void deregisterClient(DeregisterClientInput input) {
        tenantService.deregisterClient(input.clientId());
    }

    @Override
    public void addDpn(AddDpn addDpn) {
        addDpn.getDpns().forEach(
                dpn -> {
                    try {
                        log.info("Addind DPN {}", dpn.getDpn_id());
                        nodeNetworkMap.put(dpn.getDpn_id(), dpn.getNode_id() + "/" + dpn.getNetwork_id());
                    } catch (Exception e) {
                        log.error(ExceptionUtils.getFullStackTrace(e));
                    }
                }
        );
    }

    @Override
    public void deleteDpn(String dpn) {
        log.info("Removing DPN {}", dpn);
        nodeNetworkMap.remove(dpn);
    }
}
