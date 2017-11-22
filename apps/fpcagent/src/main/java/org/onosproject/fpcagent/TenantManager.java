package org.onosproject.fpcagent;

import com.google.common.collect.Maps;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.ClientIdentifier;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.payload.Contexts;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.DefaultTenant;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.Tenant;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.tenant.DefaultFpcMobility;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.tenant.DefaultFpcPolicy;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.tenant.DefaultFpcTopology;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.tenant.FpcTopology;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.tenant.fpctopology.Dpns;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcDpnId;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.onosproject.fpcagent.helpers.Converter.getFpcIdentity;

@Component(immediate = true)
@Service
public class TenantManager implements TenantService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<FpcIdentity, Tenant> tenantMap = Maps.newHashMap();
    private Map<ClientIdentifier, Tenant> clientIdMap = Maps.newHashMap();

    private Map<FpcDpnId, List<FpcDpnId>> vdpnDpnsMap = Maps.newConcurrentMap();
    private Map<FpcDpnId, Contexts> vdpnContextsMap = Maps.newConcurrentMap();

    @Activate
    protected void activate() {
        log.info("Tenant Service Started");
    }

    @Deactivate
    protected void deactivate() {
        log.info("Tenant Service Stopped");
    }

    public void addTenant(FpcIdentity tenantId) {
        Tenant tenant = new DefaultTenant();
        tenant.tenantId(tenantId);
        tenant.fpcTopology(new DefaultFpcTopology());
        tenant.fpcMobility(new DefaultFpcMobility());
        tenant.fpcPolicy(new DefaultFpcPolicy());

        tenantMap.put(tenantId, tenant);
    }

    public Optional<Tenant> getTenant(FpcIdentity tenantId) {
        return Optional.ofNullable(tenantMap.get(tenantId));
    }

    public Optional<Tenant> getTenant(ClientIdentifier clientId) {
        return Optional.ofNullable(clientIdMap.get(clientId));
    }

    public Optional<Tenant> registerClient(ClientIdentifier clientId, FpcIdentity tenantId) {
        return getTenant(tenantId).map(tenant -> clientIdMap.put(clientId, tenant));
    }

    public Tenant deregisterClient(ClientIdentifier clientId) {
        return clientIdMap.remove(clientId);
    }

    private boolean dpnIdExists(FpcIdentity tenantId, FpcDpnId fpcDpnId) {
        return getTenant(tenantId).map(
                tenant -> {
                    FpcTopology fpcTopology = tenant.fpcTopology();
                    return fpcTopology.dpns() != null &&
                            fpcTopology.dpns().stream().anyMatch(dpn -> dpn.dpnId().equals(fpcDpnId));
                }
        ).orElse(false);
    }

    private Optional<FpcDpnId> getDpnId(String nodeId, String networkId) {
        return getTenant(getFpcIdentity.apply("default")).map(
                tenant -> {
                    FpcTopology fpcTopology = tenant.fpcTopology();
                    if (fpcTopology.dpns() != null) {
                        return fpcTopology.dpns().stream()
                                .filter(dpn -> dpn.nodeId().equals(nodeId))
                                .filter(dpn -> dpn.networkId().equals(networkId))
                                .findFirst()
                                .map(Dpns::dpnId);
                    }
                    return Optional.<FpcDpnId>empty();
                }
        ).orElse(Optional.empty());
    }
}
