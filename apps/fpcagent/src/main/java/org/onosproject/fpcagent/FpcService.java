package org.onosproject.fpcagent;

import org.onosproject.fpcagent.dto.configure.Configure;
import org.onosproject.fpcagent.dto.dpn.AddDpn;
import org.onosproject.fpcagent.helpers.ConfigHelper;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.deregisterclient.DeregisterClientInput;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.registerclient.RegisterClientInput;

import java.util.Optional;

public interface FpcService {

    Optional<ConfigHelper> getConfig();

    void configure(Configure configure);

    void registerClient(RegisterClientInput input);

    void deregisterClient(DeregisterClientInput input);

    void addDpn(AddDpn addDpn);

    void deleteDpn(String dpn);
}
