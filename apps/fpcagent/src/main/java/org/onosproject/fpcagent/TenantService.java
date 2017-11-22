package org.onosproject.fpcagent;

import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.ClientIdentifier;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.Tenant;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcIdentity;

import java.util.Optional;

public interface TenantService {
    void addTenant(FpcIdentity tenantId);

    Optional<Tenant> getTenant(FpcIdentity tenantId);

    Optional<Tenant> getTenant(ClientIdentifier clientId);

    Optional<Tenant> registerClient(ClientIdentifier clientId, FpcIdentity tenantId);

    Tenant deregisterClient(ClientIdentifier clientId);
}
