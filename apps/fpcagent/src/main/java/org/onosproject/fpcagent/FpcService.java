package org.onosproject.fpcagent;

import org.onosproject.fpcagent.helpers.ConfigHelper;

import java.util.Optional;

public interface FpcService {

    Optional<ConfigHelper> getConfig();
}
