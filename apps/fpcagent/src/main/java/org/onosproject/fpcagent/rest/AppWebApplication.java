package org.onosproject.fpcagent.rest;

import org.onlab.rest.AbstractWebApplication;

import java.util.Set;

/**
 * Sample REST API web application.
 */
public class AppWebApplication extends AbstractWebApplication {
    @Override
    public Set<Class<?>> getClasses() {
        return getClasses(AppWebResource.class);
    }
}
