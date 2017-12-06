package org.onosproject.fpcagent.workers;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.onosproject.fpcagent.util.FpcUtil;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.yangautoprefixnotify.value.DownlinkDataNotification;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcDpnId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import static org.onosproject.fpcagent.util.Converter.*;

public class ZMQSBSubscriberManager implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(ZMQSBSubscriberManager.class);
    private static int MIN_TOPIC_VAL = 4;
    private static int MAX_TOPIC_VAL = 255;
    private static byte ASSIGN_ID = 0b0000_1010;
    private static byte ASSIGN_CONFLICT = 0b0000_1011;
    private static byte HELLO_REPLY = 0b0000_1101;
    private static byte CONTROLLER_STATUS_INDICATION = 0b0000_1110;
    private static byte HELLO = 0b0000_0001;
    private static byte GOODBYE = 0b0000_0010;
    private static ZMQSBSubscriberManager _instance = null;
    private static Long controllerSourceId;
    private static Short subscriberId;
    private final String address;
    private final Short broadcastAllId;
    private final Short broadcastControllersId;
    private final Short broadcastDpnsId;
    private final String nodeId;
    private final String networkId;
    private boolean run;
    private boolean conflictingTopic;
    private Future<?> broadcastAllWorker;
    private Future<?> broadcastControllersWorker;
    private Future<?> broadcastTopicWorker;
    private Future<?> generalWorker;

    protected ZMQSBSubscriberManager(String address, String broadcastAllId, String broadcastControllersId,
                                     String broadcastDpnsId, String nodeId, String networkId) {
        this.address = address;
        this.run = true;

        this.nodeId = nodeId;
        this.networkId = networkId;
        this.broadcastAllId = Short.parseShort(broadcastAllId);
        this.broadcastControllersId = Short.parseShort(broadcastControllersId);
        this.broadcastDpnsId = Short.parseShort(broadcastDpnsId);

        this.conflictingTopic = false;
        controllerSourceId = (long) ThreadLocalRandom.current().nextInt(0, 65535);
    }

    public static ZMQSBSubscriberManager createInstance(String address, String broadcastAllId,
                                                        String broadcastControllersId, String broadcastDpnsId,
                                                        String nodeId, String networkId) {
        if (_instance == null) {
            _instance = new ZMQSBSubscriberManager(address, broadcastAllId, broadcastControllersId,
                    broadcastDpnsId, nodeId, networkId);
        }
        return _instance;
    }

    public static ZMQSBSubscriberManager getInstance() {
        return _instance;
    }

    public static Short getControllerTopic() {
        return subscriberId;
    }

    public static Long getControllerSourceId() {
        return controllerSourceId;
    }

    public void open() {
        short subscriberId = (short) ThreadLocalRandom.current().nextInt(MIN_TOPIC_VAL, MAX_TOPIC_VAL + 1);

        broadcastAllWorker = Executors.newSingleThreadExecutor()
                .submit(new ZMQSubscriberWorker(broadcastAllId));

        broadcastControllersWorker = Executors.newSingleThreadExecutor()
                .submit(new ZMQSubscriberWorker(broadcastControllersId));

        broadcastTopicWorker = Executors.newSingleThreadExecutor()
                .submit(new BroadcastTopic(subscriberId));
    }

    @Override
    public void close() {
        run = false;
    }

    /**
     * Interrupts the BroadcastTopicworker if there is an Assign topic Conflict
     *
     * @param conflict - Flag to indicate conflict
     * @param subId    - Topic Id that caused the conflict
     */
    protected void BroadcastAllSubIdCallBack(boolean conflict, Short subId) {
        if (conflict && subscriberId.equals(subId)) {
            this.conflictingTopic = true;
            broadcastTopicWorker.cancel(true);
        }
    }

    /**
     * Broadcasts the GOODBYE message to all the DPNs
     */
    public void sendGoodbyeToDpns() {
        ByteBuffer bb = ByteBuffer.allocate(10 + nodeId.length() + networkId.length());
        bb.put(toUint8(broadcastDpnsId))
                .put(CONTROLLER_STATUS_INDICATION)
                .put(toUint8(subscriberId))
                .put(GOODBYE)
                .put(toUint32(controllerSourceId))
                .put(toUint8((short) nodeId.length()))
                .put(nodeId.getBytes())
                .put(toUint8((short) networkId.length()))
                .put(networkId.getBytes());

        log.info("sendGoodbyeToDpns: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    /**
     * Broadcasts an Assign Conflict message
     *
     * @param contents - byte array received over the southbound.
     */
    protected void SendAssignConflictMessage(byte[] contents) {
        byte topic = contents[2];
        short nodeIdLen = contents[7];
        short networkIdLen = contents[8 + nodeIdLen];
        String node_id = new String(Arrays.copyOfRange(contents, 8, 8 + nodeIdLen));
        String network_id = new String(Arrays.copyOfRange(contents, 9 + nodeIdLen, 9 + nodeIdLen + networkIdLen));

        if (toUint8(subscriberId) == topic || (nodeId.equals(node_id) && networkId.equals(network_id))) {
            ByteBuffer bb = ByteBuffer.allocate(9 + nodeId.length() + networkId.length());
            bb.put(toUint8(broadcastAllId))
                    .put(ASSIGN_CONFLICT)
                    .put(topic)
                    .put(toUint32(controllerSourceId))
                    .put(toUint8((short) nodeId.length()))
                    .put(nodeId.getBytes())
                    .put(toUint8((short) networkId.length()))
                    .put(networkId.getBytes());

            log.info("SendAssignConflictMessage: {}", bb.array());
            ZMQSBPublisherManager.getInstance().send(bb);
        }
    }

    protected class ZMQSubscriberWorker implements Runnable {
        private final Short subscriberId;
        private ZContext ctx;

        ZMQSubscriberWorker(Short subscriberId) {
            this.subscriberId = subscriberId;
            this.ctx = new ZContext();
        }

        /**
         * Sends a reply to a DPN Hello
         *
         * @param dpnStatus - DPN Status Indication message received from the DPN
         */
        protected void sendHelloReply(FpcUtil.DPNStatusIndication dpnStatus) {
            if (FpcUtil.getTopicFromNode(dpnStatus.getKey()) != null) {
                ByteBuffer bb = ByteBuffer.allocate(9 + nodeId.length() + networkId.length())
                        .put(toUint8(FpcUtil.getTopicFromNode(dpnStatus.getKey())))
                        .put(HELLO_REPLY)
                        .put(toUint8(ZMQSBSubscriberManager.getControllerTopic()))
                        .put(toUint32(ZMQSBSubscriberManager.getControllerSourceId()))
                        .put(toUint8((short) nodeId.length()))
                        .put(nodeId.getBytes())
                        .put(toUint8((short) networkId.length()))
                        .put(networkId.getBytes());

                log.info("sendHelloReply: {}", bb.array());
                ZMQSBPublisherManager.getInstance().send(bb);
            }
        }

        @Override
        public void run() {
            ZMQ.Socket subscriber = this.ctx.createSocket(ZMQ.SUB);
            subscriber.connect(address);
            subscriber.subscribe(new byte[]{toUint8(subscriberId)});
            log.debug("Subscriber at {} / {}", address, subscriberId);
            while ((!Thread.currentThread().isInterrupted()) && run) {
                byte[] contents = subscriber.recv();
                byte topic = contents[0];
                byte messageType = contents[1];
                log.debug("Received {}", contents);
                switch (topic) {
                    case 1:
                        if (messageType == ASSIGN_CONFLICT && toInt(contents, 3) != controllerSourceId) {
                            BroadcastAllSubIdCallBack(true, (short) contents[2]);
                        } else if (messageType == ASSIGN_ID && toInt(contents, 3) != controllerSourceId) {
                            SendAssignConflictMessage(contents);
                        }
                        break;
                    default:
                        Map.Entry<FpcDpnId, Object> entry = FpcUtil.decode(contents);
                        if (entry != null) {
                            if (entry.getValue() instanceof DownlinkDataNotification) {
                                // TODO handle DL notification
                            } else if (entry.getValue() instanceof FpcUtil.DPNStatusIndication) {
                                FpcUtil.DPNStatusIndication dpnStatus = (FpcUtil.DPNStatusIndication) entry.getValue();
                                if (dpnStatus.getStatus() == FpcUtil.DPNStatusIndication.Status.HELLO) {
                                    sendHelloReply(dpnStatus);
                                }
                            }
                        }
                }
            }
            subscriber.disconnect(address);
            subscriber.close();
            ctx.destroySocket(subscriber);
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                log.error(ExceptionUtils.getFullStackTrace(e));
            }
        }
    }

    /**
     * Class to broadcast a topic for the controller
     */
    protected class BroadcastTopic implements Runnable {
        private Short topic;

        /**
         * Constructor
         *
         * @param topic - Topic to broadcast
         */
        public BroadcastTopic(Short topic) {
            this.topic = topic;
        }

        /**
         * Broadcasts the topic
         */
        private void broadcastTopic() {
            ByteBuffer bb = ByteBuffer.allocate(9 + nodeId.length() + networkId.length());
            bb.put(toUint8(broadcastAllId))
                    .put(ASSIGN_ID)
                    .put(toUint8(this.topic))
                    .put(toUint32(controllerSourceId))
                    .put(toUint8((short) nodeId.length()))
                    .put(nodeId.getBytes())
                    .put(toUint8((short) networkId.length()))
                    .put(networkId.getBytes());

            log.info("broadcastTopic: {}", bb.array());
            ZMQSBPublisherManager.getInstance().send(bb);
        }

        /**
         * Broadcasts the HELLO message to all the DPNs
         */
        private void sendHelloToDpns() {
            ByteBuffer bb = ByteBuffer.allocate(10 + nodeId.length() + networkId.length());
            bb.put(toUint8(broadcastDpnsId))
                    .put(CONTROLLER_STATUS_INDICATION)
                    .put(toUint8(subscriberId))
                    .put(HELLO)
                    .put(toUint32(controllerSourceId))
                    .put(toUint8((short) nodeId.length()))
                    .put(nodeId.getBytes())
                    .put(toUint8((short) networkId.length()))
                    .put(networkId.getBytes());


            log.info("sendHelloToDpns: {}", bb.array());
            ZMQSBPublisherManager.getInstance().send(bb);
        }

        @Override
        public void run() {
            try {
                this.broadcastTopic();
                log.debug("Thread sleeping: " + Thread.currentThread().getName());
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                if (conflictingTopic) {
                    conflictingTopic = false;
                    this.topic = (short) ThreadLocalRandom.current().nextInt(MIN_TOPIC_VAL, MAX_TOPIC_VAL + 1);
                    subscriberId = this.topic;
                    this.run();
                    return;
                } else {
                    log.error(ExceptionUtils.getFullStackTrace(e));
                }
            }
            subscriberId = this.topic;
            log.info("Topic Id: " + this.topic);
            generalWorker = Executors.newSingleThreadExecutor().submit(new ZMQSubscriberWorker(this.topic));

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.error(ExceptionUtils.getFullStackTrace(e));
            }
            sendHelloToDpns();
        }

    }
}
