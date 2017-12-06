package org.onosproject.fpcagent;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.onosproject.core.ApplicationId;
import org.onosproject.fpcagent.util.ConfigHelper;
import org.onosproject.net.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class FpcConfig extends Config<ApplicationId> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public Optional<ConfigHelper> getConfig() {
        try {
            return Optional.of(mapper.treeToValue(object, ConfigHelper.class));
        } catch (JsonProcessingException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
        return Optional.empty();
    }
}

