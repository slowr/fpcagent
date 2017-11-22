package org.onosproject.fpcagent.workers;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configure.ConfigureInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configurebundles.ConfigureBundlesInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.eventderegister.EventDeregisterInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.eventregister.EventRegisterInput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.probe.ProbeInput;
import org.onosproject.yang.model.RpcInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class NBWorkManager implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(NBWorkManager.class);

    // TODO: add store
    private static NBWorkManager _instance = null;
    private final BlockingQueue<Object> blockingQueue;
    private final int poolSize;
    private boolean run;

    protected NBWorkManager(int poolSize) {
        this.blockingQueue = new LinkedBlockingQueue<>();
        this.poolSize = poolSize;
        this.run = true;
    }

    public static NBWorkManager createInstance(int poolSize) {
        if (_instance == null) {
            _instance = new NBWorkManager(poolSize);
        }
        return _instance;
    }

    public static NBWorkManager getInstance() {
        return _instance;
    }

    public void submit(RpcInput input) {
        try {
            blockingQueue.put(input);
        } catch (InterruptedException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
    }

    public void open() {
        ExecutorService executorService = Executors.newFixedThreadPool(this.poolSize);
        executorService.submit(() -> {
            while ((!Thread.currentThread().isInterrupted()) && run) {
                try {
                    Object o = blockingQueue.take();
                    if (o instanceof ConfigureInput) {

                    } else if (o instanceof ConfigureBundlesInput) {

                    } else if (o instanceof EventRegisterInput) {

                    } else if (o instanceof EventDeregisterInput) {

                    } else if (o instanceof ProbeInput) {

                    }
                } catch (InterruptedException e) {
                    log.error(ExceptionUtils.getFullStackTrace(e));
                }
            }
        });
    }

    @Override
    public void close() throws Exception {
        run = false;
    }
}
