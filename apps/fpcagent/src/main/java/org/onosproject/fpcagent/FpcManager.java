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

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.felix.scr.annotations.*;
import org.onosproject.config.DynamicConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.fpcagent.util.ConfigHelper;
import org.onosproject.fpcagent.workers.ZMQSBPublisherManager;
import org.onosproject.fpcagent.workers.ZMQSBSubscriberManager;
import org.onosproject.net.config.*;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.connectioninfo.DefaultConnections;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.deregisterclient.DefaultDeregisterClientOutput;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.registerclient.DefaultRegisterClientInput;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.registerclient.DefaultRegisterClientOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.IetfDmmFpcagentService;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.ClientIdentifier;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.ErrorTypeId;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.Result;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configure.DefaultConfigureInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configure.DefaultConfigureOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configurebundles.DefaultConfigureBundlesInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configurebundles.DefaultConfigureBundlesOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configurebundles.configurebundlesoutput.Bundles;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configurebundles.configurebundlesoutput.DefaultBundles;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configuredpn.DefaultConfigureDpnInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configuredpn.DefaultConfigureDpnOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.CreateOrUpdate;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.DeleteOrQuery;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.result.ResultEnum;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbody.resulttype.DefaultErr;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbodydpn.resulttype.DefaultEmptyCase;
import org.onosproject.yang.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static org.onosproject.fpcagent.util.FpcUtil.*;

/**
 * Fpc Manager.
 */
@Component(immediate = true)
@Service
public class FpcManager implements IetfDmmFpcagentService,
        FpcService,
        org.onosproject.yang.gen.v1.fpc.rev20150105.FpcService {
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
    private ConcurrentMap<ClientIdentifier, DefaultRegisterClientInput> clientInfo = Maps.newConcurrentMap();
    private boolean started = false;

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

        if (started) {
            ZMQSBSubscriberManager.getInstance().close();
            ZMQSBPublisherManager.getInstance().close();
        }

        rpcRegistry.unregisterRpcService(this);

        clientInfo.clear();

        log.info("FPC Agent Stopped");
    }

    /**
     * Initialize ZMQ Managers based on configuration.
     */
    private void init() {
        fpcConfig.getConfig().ifPresent(
                helper -> {
                    started = true;
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
        Stopwatch timer = Stopwatch.createStarted();
        DefaultConfigureDpnOutput configureDpnOutput = new DefaultConfigureDpnOutput();
        configureDpnOutput.result(Result.of(ResultEnum.OK));
        configureDpnOutput.resultType(new DefaultEmptyCase());
        RpcOutput.Status status = RpcOutput.Status.RPC_SUCCESS;
        try {
            for (ModelObject modelObject : tenantService.getModelObjects(rpcInput.data(), configureDpn)) {
                DefaultConfigureDpnInput input = (DefaultConfigureDpnInput) modelObject;
                switch (input.operation().enumeration()) {
                    case ADD:
                        configureDpnOutput = tenantService.configureDpnAdd(input);
                        break;
                    case REMOVE:
                        configureDpnOutput = tenantService.configureDpnRemove(input);
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
        log.debug("Time Elapsed {} ms", timer.stop().elapsed(TimeUnit.MILLISECONDS));
        return new RpcOutput(status, dataNode.dataNodes().get(0));
    }

    @Override
    public RpcOutput configure(RpcInput rpcInput) {
        Stopwatch timer = Stopwatch.createStarted();
        DefaultConfigureOutput configureOutput = new DefaultConfigureOutput();
        RpcOutput.Status status = RpcOutput.Status.RPC_SUCCESS;
        try {
            for (ModelObject modelObject : tenantService.getModelObjects(rpcInput.data(), configure)) {
                DefaultConfigureInput input = (DefaultConfigureInput) modelObject;
                switch (input.opType()) {
                    case CREATE:
                        configureOutput = tenantService.configureCreate(
                                (CreateOrUpdate) input.opBody(),
                                clientInfo.get(input.clientId()),
                                input.opId()
                        );
                        break;
                    case UPDATE:
                        configureOutput = tenantService.configureUpdate(
                                (CreateOrUpdate) input.opBody(),
                                clientInfo.get(input.clientId()),
                                input.opId()
                        );
                        break;
                    case QUERY:
                        break;
                    case DELETE:
                        configureOutput = tenantService.configureDelete(
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
        log.debug("Time Elapsed {} ms", timer.stop().elapsed(TimeUnit.MILLISECONDS));
        return new RpcOutput(status, dataNode.dataNodes().get(0));
    }

    @Override
    public RpcOutput configureBundles(RpcInput rpcInput) {
        Stopwatch timer = Stopwatch.createStarted();
        DefaultConfigureBundlesOutput configureBundlesOutput = new DefaultConfigureBundlesOutput();
        RpcOutput.Status status = RpcOutput.Status.RPC_SUCCESS;
        try {
            for (ModelObject modelObject : tenantService.getModelObjects(rpcInput.data(), configureBundles)) {
                DefaultConfigureBundlesInput input = (DefaultConfigureBundlesInput) modelObject;
                for (org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configurebundles.configurebundlesinput.Bundles bundle : input.bundles()) {
                    DefaultConfigureOutput configureOutput = new DefaultConfigureOutput();
                    switch (bundle.opType()) {
                        case CREATE:
                            configureOutput = tenantService.configureCreate(
                                    (CreateOrUpdate) bundle.opBody(),
                                    clientInfo.get(input.clientId()),
                                    bundle.opId()
                            );
                            break;
                        case UPDATE:
                            configureOutput = tenantService.configureUpdate(
                                    (CreateOrUpdate) bundle.opBody(),
                                    clientInfo.get(input.clientId()),
                                    bundle.opId()
                            );
                            break;
                        case QUERY:
                            break;
                        case DELETE:
                            configureOutput = tenantService.configureDelete(
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
        log.debug("Time Elapsed {} ms", timer.stop().elapsed(TimeUnit.MILLISECONDS));
        return new RpcOutput(status, dataNode.dataNodes().get(0));
    }

    @Override
    public RpcOutput eventRegister(RpcInput rpcInput) {
        Stopwatch timer = Stopwatch.createStarted();
        // TODO implement
        log.debug("Time Elapsed {} ms", timer.stop().elapsed(TimeUnit.MILLISECONDS));
        return null;
    }

    @Override
    public RpcOutput eventDeregister(RpcInput rpcInput) {
        Stopwatch timer = Stopwatch.createStarted();
        // TODO implement
        log.debug("Time Elapsed {} ms", timer.stop().elapsed(TimeUnit.MILLISECONDS));
        return null;
    }

    @Override
    public RpcOutput probe(RpcInput rpcInput) {
        Stopwatch timer = Stopwatch.createStarted();
        // TODO implement
        log.debug("Time Elapsed {} ms", timer.stop().elapsed(TimeUnit.MILLISECONDS));
        return null;
    }

    @Override
    public RpcOutput registerClient(RpcInput rpcInput) {
        Stopwatch timer = Stopwatch.createStarted();
        DefaultRegisterClientOutput registerClientOutput = new DefaultRegisterClientOutput();
        RpcOutput.Status status = RpcOutput.Status.RPC_SUCCESS;

        try {
            for (ModelObject modelObject : tenantService.getModelObjects(rpcInput.data(), registerClient)) {
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

                // TODO create node to DCS
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

        log.debug("Time Elapsed {} ms", timer.stop().elapsed(TimeUnit.MILLISECONDS));
        return new RpcOutput(status, dataNode.dataNodes().get(0));
    }

    @Override
    public RpcOutput deregisterClient(RpcInput rpcInput) {
        Stopwatch timer = Stopwatch.createStarted();
        DefaultDeregisterClientOutput deregisterClientOutput = new DefaultDeregisterClientOutput();
        RpcOutput.Status status = RpcOutput.Status.RPC_SUCCESS;

        try {
            for (ModelObject modelObject : tenantService.getModelObjects(rpcInput.data(), registerClient)) {
                DefaultRegisterClientInput input = (DefaultRegisterClientInput) modelObject;
                if (!clientInfo.containsKey(input.clientId())) {
                    throw new RuntimeException("Client does not exist.");
                }
                clientInfo.remove(input.clientId());
                deregisterClientOutput.clientId(input.clientId());

                DefaultConnections defaultConnections = new DefaultConnections();
                defaultConnections.clientId(input.clientId().toString());

                // TODO delete node from DCS
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

        log.debug("Time Elapsed {} ms", timer.stop().elapsed(TimeUnit.MILLISECONDS));
        return new RpcOutput(status, dataNode.dataNodes().get(0));
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
