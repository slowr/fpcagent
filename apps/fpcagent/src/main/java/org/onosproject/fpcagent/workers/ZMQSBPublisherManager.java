package org.onosproject.fpcagent.workers;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.nio.ByteBuffer;
import java.util.concurrent.*;

public class ZMQSBPublisherManager implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(ZMQSBPublisherManager.class);
    private static ZMQSBPublisherManager _instance = null;
    private final ZContext ctx;
    private final String address;
    private final BlockingQueue<ByteBuffer> blockingQueue;
    private final int poolSize;
    private boolean run;

    protected ZMQSBPublisherManager(String address, int poolSize) {
        this.ctx = new ZContext();
        this.address = address;
        this.run = true;
        this.blockingQueue = new LinkedBlockingQueue<>();
        this.poolSize = poolSize;
    }

    public static ZMQSBPublisherManager createInstance(String address, int poolSize) {
        if (_instance == null) {
            _instance = new ZMQSBPublisherManager(address, poolSize);
        }
        return _instance;
    }

    public static ZMQSBPublisherManager getInstance() {
        return _instance;
    }

    public void send(ByteBuffer buf) {
        try {
            blockingQueue.put(buf);
        } catch (InterruptedException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
    }

    public void open() {
        ExecutorService executorService = Executors.newFixedThreadPool(this.poolSize);
        executorService.submit(() -> {
            ZMQ.Socket socket = ctx.createSocket(ZMQ.PUB);
            socket.connect(address);
            while ((!Thread.currentThread().isInterrupted()) && run) {
                try {
                    byte[] array = blockingQueue.take().array();
                    socket.send(array);
                } catch (InterruptedException e) {
                    log.error(ExceptionUtils.getFullStackTrace(e));
                }
            }
        });
    }

    @Override
    public void close() {
        run = false;
    }
}
