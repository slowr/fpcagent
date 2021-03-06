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

import com.google.common.collect.Sets;
import org.apache.felix.scr.annotations.*;
import org.onosproject.config.DynamicConfigEvent;
import org.onosproject.config.DynamicConfigListener;
import org.onosproject.config.DynamicConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.core.IdGenerator;
import org.onosproject.fpcagent.providers.DpnDeviceListener;
import org.onosproject.fpcagent.providers.DpnProviderService;
import org.onosproject.fpcagent.util.ConfigHelper;
import org.onosproject.fpcagent.workers.HTTPNotifier;
import org.onosproject.fpcagent.workers.ZMQSBPublisherManager;
import org.onosproject.fpcagent.workers.ZMQSBSubscriberManager;
import org.onosproject.net.config.*;
import org.onosproject.net.config.basics.SubjectFactories;
import org.onosproject.yang.model.ModelConverter;
import org.onosproject.yang.model.RpcRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.fpcagent.util.FpcUtil.FPC_APP_ID;

/**
 * Fpc Manager.
 */
@Component(immediate = true)
@Service
public class FpcManager implements FpcService {
    private static final Logger log = LoggerFactory.getLogger(FpcManager.class);

    private static final Class<FpcConfig> CONFIG_CLASS = FpcConfig.class;
    private final InternalNetworkConfigListener configListener =
            new InternalNetworkConfigListener();

    private final InternalConfigListener dynListener =
            new InternalConfigListener();

    /* Services */
    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private FpcRpcService fpcRpcService;

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

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private DpnProviderService dpnProviderService;

    /* Variables */
    private IdGenerator notificationIds;
    private FpcConfig fpcConfig;
    private boolean started = false;
    private HashSet<DpnDeviceListener> listeners = Sets.newHashSet();

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
        dynamicConfigService.addListener(dynListener);

        notificationIds = coreService.getIdGenerator("fpc-notification-ids");

        HTTPNotifier.getInstance().open();

        log.info("FPC Service Started");
    }

    @Deactivate
    protected void deactivate() {
        configService.removeListener(configListener);
        dynamicConfigService.removeListener(dynListener);
        registry.unregisterConfigFactory(fpcConfigConfigFactory);

        if (started) {
            ZMQSBSubscriberManager.getInstance().close();
            ZMQSBPublisherManager.getInstance().close();
            started = false;
        }

        HTTPNotifier.getInstance().close();

        log.info("FPC Service Stopped");
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
                            helper.nodeId(),
                            helper.networkId(),
                            dpnProviderService.getListener()
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
    public void addListener(DpnDeviceListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(DpnDeviceListener listener) {
        listeners.remove(listener);
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

    /**
     * Representation of internal listener, listening for dynamic config event.
     */
    private class InternalConfigListener implements DynamicConfigListener {

        @Override
        public void event(DynamicConfigEvent event) {
            checkNotNull(event, "Event cannot be NULL");
            log.debug("event {}", event);
        }
    }
}
