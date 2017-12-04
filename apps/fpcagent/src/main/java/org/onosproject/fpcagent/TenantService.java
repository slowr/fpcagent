package org.onosproject.fpcagent;

import com.google.common.annotations.Beta;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.ClientIdentifier;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.DefaultTenants;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.OpIdentifier;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configure.DefaultConfigureOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.CreateOrUpdate;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.Tenant;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcIdentity;
import org.onosproject.yang.model.DataNode;
import org.onosproject.yang.model.InnerModelObject;
import org.onosproject.yang.model.ModelObject;
import org.onosproject.yang.model.ResourceId;

import java.util.List;
import java.util.Optional;

@Beta
public interface TenantService {
    Optional<DefaultTenants> getTenants();

    void createNode(InnerModelObject innerModelObject);

    void updateNode(InnerModelObject innerModelObject);

    List<ModelObject> getModelObjects(DataNode dataNode, ResourceId appId);

    Optional<Tenant> getDefaultTenant();

    Optional<Tenant> getTenant(FpcIdentity tenantId);

    Optional<Tenant> getTenant(ClientIdentifier clientId);

    Optional<Tenant> registerClient(ClientIdentifier clientId, FpcIdentity tenantId);

    Optional<Tenant> deregisterClient(ClientIdentifier clientId);

    DefaultConfigureOutput configureCreate(
            CreateOrUpdate create,
            ClientIdentifier clientId,
            OpIdentifier operationId
    );

    DefaultConfigureOutput configureUpdate(
            CreateOrUpdate create,
            ClientIdentifier clientId,
            OpIdentifier operationId
    );
}
