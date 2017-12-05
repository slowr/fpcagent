package org.onosproject.fpcagent;

import com.google.common.annotations.Beta;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.ClientIdentifier;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.DefaultTenants;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.OpIdentifier;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configure.DefaultConfigureOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.CreateOrUpdate;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.DeleteOrQuery;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.DefaultTenant;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcIdentity;
import org.onosproject.yang.model.*;

import java.util.List;
import java.util.Optional;

@Beta
public interface TenantService {
    /**
     * Returns the root level node for Tenants.
     * Tenants is an interface that includes a List of Tenant objects.
     *
     * @return Optional Tenants
     */
    Optional<DefaultTenants> getTenants();

    /**
     * Creates a Node inside the Dynamic Configuration Store.
     *
     * @param innerModelObject inner model object to create
     * @param modelObjectId    Model Object ID
     */
    void createNode(InnerModelObject innerModelObject, ModelObjectId modelObjectId);

    /**
     * Updates a Node inside the Dynamic Configuration Store.
     *
     * @param innerModelObject inner model object to update
     * @param modelObjectId    Model Object ID
     */
    void updateNode(InnerModelObject innerModelObject, ModelObjectId modelObjectId);

    /**
     * Converts DataNode to a ModelObject.
     *
     * @param dataNode DataNode
     * @param appId    Resource Identifier
     * @return Model Object
     */
    List<ModelObject> getModelObjects(DataNode dataNode, ResourceId appId);

    /**
     * Returns the Default Tenant.
     *
     * @return Optional Default Tenant
     */
    Optional<DefaultTenant> getDefaultTenant();

    /**
     * Get Tenant by its Identifier.
     *
     * @param tenantId Tenant Identifier
     * @return Optional Tenant
     */
    Optional<DefaultTenant> getTenant(FpcIdentity tenantId);

    Optional<DefaultTenant> getTenant(ClientIdentifier clientId);

    Optional<DefaultTenant> registerClient(ClientIdentifier clientId, FpcIdentity tenantId);

    Optional<DefaultTenant> deregisterClient(ClientIdentifier clientId);

    /**
     * Handles create configure operations that are invoked through RPC.
     *
     * @param create      RPC Input converted
     * @param clientId    Client Identifier
     * @param operationId Operation Identifier
     * @return Result of the configuration
     */
    DefaultConfigureOutput configureCreate(
            CreateOrUpdate create,
            ClientIdentifier clientId,
            OpIdentifier operationId
    );

    /**
     * Handles update configure operations that are invoked through RPC.
     *
     * @param update      RPC Input converted
     * @param clientId    Client Identifier
     * @param operationId Operation Identifier
     * @return Result of the configuration
     */
    DefaultConfigureOutput configureUpdate(
            CreateOrUpdate update,
            ClientIdentifier clientId,
            OpIdentifier operationId
    );

    /**
     * Handles delete configure operations that are invoked through RPC.
     *
     * @param delete      RPC Input converted
     * @param clientId    Client Identifier
     * @param operationId Operation Identifier
     * @return Result of the configuration
     */
    DefaultConfigureOutput configureDelete(
            DeleteOrQuery delete,
            ClientIdentifier clientId,
            OpIdentifier operationId
    );
}
