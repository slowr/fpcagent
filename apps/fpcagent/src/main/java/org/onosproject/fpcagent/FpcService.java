package org.onosproject.fpcagent;

import org.onosproject.fpcagent.providers.DpnDeviceListener;
import org.onosproject.fpcagent.util.ConfigHelper;

import java.util.Optional;

public interface FpcService {

    void addListener(DpnDeviceListener listener);

    void removeListener(DpnDeviceListener listener);

    Optional<ConfigHelper> getConfig();
}
