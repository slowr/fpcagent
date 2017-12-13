package org.onosproject.fpcagent;

import com.google.common.annotations.Beta;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.registerclient.DefaultRegisterClientInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.OpIdentifier;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configure.DefaultConfigureOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configuredpn.DefaultConfigureDpnInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configuredpn.DefaultConfigureDpnOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.CreateOrUpdate;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.DeleteOrQuery;

@Beta
public interface FpcRpcService {

    /**
     * Handles create Configure operations that are invoked through RPC.
     *
     * @param create      RPC Input converted
     * @param clientInfo    Client Identifier
     * @param operationId Operation Identifier
     * @return Result of the configuration
     */
    DefaultConfigureOutput configureCreate(
            CreateOrUpdate create,
            DefaultRegisterClientInput clientInfo,
            OpIdentifier operationId
    ) throws Exception;

    /**
     * Handles update Configure operations that are invoked through RPC.
     *
     * @param update      RPC Input converted
     * @param clientInfo    Client Identifier
     * @param operationId Operation Identifier
     * @return Result of the configuration
     */
    DefaultConfigureOutput configureUpdate(
            CreateOrUpdate update,
            DefaultRegisterClientInput clientInfo,
            OpIdentifier operationId
    ) throws Exception;

    /**
     * Handles delete Configure operations that are invoked through RPC.
     *
     * @param delete      RPC Input converted
     * @param clientInfo    Client Identifier
     * @param operationId Operation Identifier
     * @return Result of the configuration
     */
    DefaultConfigureOutput configureDelete(
            DeleteOrQuery delete,
            DefaultRegisterClientInput clientInfo,
            OpIdentifier operationId
    ) throws Exception;

    DefaultConfigureDpnOutput configureDpnAdd(DefaultConfigureDpnInput input) throws Exception;

    DefaultConfigureDpnOutput configureDpnRemove(DefaultConfigureDpnInput input) throws Exception;
}
