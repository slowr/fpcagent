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
import org.onosproject.config.DynamicConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.fpcagent.helpers.ConfigHelper;
import org.onosproject.fpcagent.helpers.DpnApi;
import org.onosproject.fpcagent.workers.ZMQSBPublisherManager;
import org.onosproject.fpcagent.workers.ZMQSBSubscriberManager;
import org.onosproject.net.config.*;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.IetfDmmFpcagentService;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.ErrorTypeId;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.Result;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configure.DefaultConfigureInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configure.DefaultConfigureOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configuredpn.DefaultConfigureDpnInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configuredpn.DefaultConfigureDpnOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.CreateOrUpdate;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.DeleteOrQuery;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.payload.Contexts;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.result.ResultEnum;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbody.resulttype.DefaultErr;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbodydpn.resulttype.DefaultEmptyCase;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.mobilityinfo.mobprofileparameters.ThreegppTunnel;
import org.onosproject.yang.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static org.onosproject.fpcagent.FpcUtil.*;

/**
 * Fpc Manager.
 */
@Component(immediate = true)
@Service
public class FpcManager implements IetfDmmFpcagentService, FpcService {
    private static final Logger log = LoggerFactory.getLogger(FpcManager.class);

    private static final Class<FpcConfig> CONFIG_CLASS = FpcConfig.class;
    private final InternalNetworkConfigListener configListener =
            new InternalNetworkConfigListener();

    /* Services */
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private TenantService tenantService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private ModelConverter modelConverter;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private DynamicConfigService dynamicConfigService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private RpcRegistry rpcRegistry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private NetworkConfigRegistry registry;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private NetworkConfigService configService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private CoreService coreService;

    /* Variables */
    private FpcConfig fpcConfig;
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
        coreService.registerApplication(FPC_APP_ID);
        configService.addListener(configListener);
        registry.registerConfigFactory(fpcConfigConfigFactory);

        rpcRegistry.registerRpcService(this);

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
                            helper.dpnSubscriberUri(),
                            helper.zmqBroadcastAll(),
                            helper.zmqBroadcastControllers(),
                            helper.zmqBroadcastDpns(),
                            helper.nodeId(),
                            helper.networkId()
                    );

                    ZMQSBPublisherManager.createInstance(
                            helper.dpnPublisherUri(),
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
        DefaultConfigureDpnOutput output = new DefaultConfigureDpnOutput();
        output.result(Result.of(ResultEnum.OK));
        output.resultType(new DefaultEmptyCase());

        try {
            tenantService.getModelObjects(rpcInput.data(), configureDpnResourceId).forEach(
                    modelObject -> {
                        DefaultConfigureDpnInput input = (DefaultConfigureDpnInput) modelObject;
                        String dpnId = input.inputDpnId().fpcIdentity().union().string();
                        switch (input.operation().enumeration()) {
                            case ADD:
                                log.info("Adding DPN {}", dpnId);
                                // TODO
                                break;
                            case REMOVE:
                                log.info("Removing DPN {}", dpnId);
                                // TODO
                                break;
                        }
                    });
        } catch (Exception e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }

        ResourceData dataNode = modelConverter.createDataNode(
                DefaultModelObjectData.builder().addModelObject(output).build()
        );
        return new RpcOutput(RpcOutput.Status.RPC_SUCCESS, dataNode.dataNodes().get(0));
    }

    @Override
    public RpcOutput configure(RpcInput rpcInput) {
        DefaultConfigureOutput configureOutput = new DefaultConfigureOutput();

        try {
            for (ModelObject modelObject : tenantService.getModelObjects(rpcInput.data(), configureResourceId)) {
                DefaultConfigureInput input = (DefaultConfigureInput) modelObject;
                switch (input.opType()) {
                    case CREATE:
                        configureOutput = tenantService.configureCreate(
                                (CreateOrUpdate) input.opBody(),
                                input.clientId(),
                                input.opId()
                        );
                        break;
                    case UPDATE:
                        configureOutput = tenantService.configureUpdate(
                                (CreateOrUpdate) input.opBody(),
                                input.clientId(),
                                input.opId()
                        );
                        break;
                    case QUERY:
                        break;
                    case DELETE:
                        if (input.opBody() instanceof DeleteOrQuery) {
                            DeleteOrQuery deleteOrQuery = (DeleteOrQuery) input.opBody();
                            // TODO: move to tenant service
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
                configureOutput.opId(input.opId());
            }
        } catch (Exception e) {
            DefaultErr defaultErr = new DefaultErr();
            defaultErr.errorInfo(ExceptionUtils.getFullStackTrace(e));
            defaultErr.errorTypeId(ErrorTypeId.of(0));
            configureOutput.resultType(defaultErr);
            configureOutput.result(Result.of(ResultEnum.ERR));

            log.error(ExceptionUtils.getFullStackTrace(e));
        } finally {
            tenantService.getTenants().ifPresent(tenants -> tenantService.updateNode(tenants));
        }

        ResourceData dataNode = modelConverter.createDataNode(
                DefaultModelObjectData.builder()
                        .addModelObject(configureOutput)
                        .build()
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
