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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import org.onosproject.fpcagent.TenantManager;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.DefaultTenant;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.tenant.fpcmobility.DefaultContexts;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.tenant.fpctopology.DefaultDpns;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcContextId;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcDpnId;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

/**
 * Cache Manager.
 */
public class CacheManager {

    // Cacher per different Tenant
    public static ConcurrentMap<FpcIdentity, CacheManager> cacheInfo = Maps.newConcurrentMap();
    private final Logger log = LoggerFactory.getLogger(getClass());
    public LoadingCache<FpcContextId, Optional<DefaultContexts>> contextsCache;
    public LoadingCache<FpcDpnId, Optional<DefaultDpns>> dpnsCache;

    private static TenantManager tenantManager;

    private CacheManager(FpcIdentity identity) {
        contextsCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(
                        new CacheLoader<FpcContextId, Optional<DefaultContexts>>() {
                            @Override
                            public Optional<DefaultContexts> load(FpcContextId fpcContextId) throws Exception {
                                try {
                                    Optional<DefaultTenant> defaultTenant = tenantManager.getTenant(identity);
                                    if (defaultTenant.isPresent()) {
                                        DefaultTenant tenant = defaultTenant.get();
                                        log.debug("tenant {}", defaultTenant);
                                        if (tenant.fpcMobility().contexts() != null) {
                                            return tenant.fpcMobility().contexts().stream()
                                                    .filter(contexts -> contexts.contextId().equals(fpcContextId))
                                                    .findFirst()
                                                    .map(c -> (DefaultContexts) c);
                                        }
                                    }
                                } catch (Exception e) {
                                    // let store to populate and retry
                                    Thread.sleep(1000);
                                    return load(fpcContextId);
                                }
                                return Optional.empty();
                            }
                        }
                );

        dpnsCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(
                        new CacheLoader<FpcDpnId, Optional<DefaultDpns>>() {
                            @Override
                            public Optional<DefaultDpns> load(FpcDpnId fpcDpnId) throws Exception {
                                try {
                                    Optional<DefaultTenant> defaultTenant = tenantManager.getTenant(identity);
                                    if (defaultTenant.isPresent()) {
                                        DefaultTenant tenant = defaultTenant.get();
                                        log.debug("tenant {}", tenant);
                                        if (tenant.fpcTopology().dpns() != null) {
                                            return tenant.fpcTopology().dpns().stream()
                                                    .filter(dpns -> dpns.dpnId().equals(fpcDpnId))
                                                    .findFirst()
                                                    .map(d -> (DefaultDpns) d);
                                        }
                                    }
                                } catch (Exception e) {
                                    // let store to populate and retry
                                    Thread.sleep(1000);
                                    return load(fpcDpnId);
                                }
                                return Optional.empty();
                            }
                        }
                );
    }

    public static CacheManager getInstance(FpcIdentity identity) {
        cacheInfo.putIfAbsent(identity, new CacheManager(identity));
        return cacheInfo.get(identity);
    }

    public static void addManager(TenantManager manager) {
        tenantManager = manager;
    }
}
