package org.onosproject.fpcagent.workers;

import javafx.util.Pair;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.onosproject.fpcagent.providers.DpnDeviceListener;
import org.onosproject.fpcagent.util.FpcUtil;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.yangautoprefixnotify.value.DownlinkDataNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import static org.onosproject.fpcagent.protocols.DpnNgicCommunicator.*;
import static org.onosproject.fpcagent.util.Converter.toInt;

public class ZMQSBSubscriberManager implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(ZMQSBSubscriberManager.class);
    private static int MIN_TOPIC_VAL = 4;
    private static int MAX_TOPIC_VAL = 255;
    private static ZMQSBSubscriberManager _instance = null;
    private final String address;
    private final String nodeId;
    private final String networkId;
    private Long controllerSourceId;
    private byte controllerTopic;
    private boolean run;
    private boolean conflictingTopic;

    private Future<?> broadcastAllWorker;
    private Future<?> broadcastControllersWorker;
    private Future<?> broadcastTopicWorker;
    private Future<?> generalWorker;

    private DpnDeviceListener dpnDeviceListener;

    protected ZMQSBSubscriberManager(String address, String nodeId, String networkId, DpnDeviceListener dpnDeviceListener) {
        this.address = address;
        this.run = true;
        this.nodeId = nodeId;
        this.networkId = networkId;
        this.conflictingTopic = false;
        this.controllerSourceId = (long) ThreadLocalRandom.current().nextInt(0, 65535);
        this.dpnDeviceListener = dpnDeviceListener;
    }

    public static ZMQSBSubscriberManager createInstance(String address, String nodeId, String networkId, DpnDeviceListener providerService) {
        if (_instance == null) {
            _instance = new ZMQSBSubscriberManager(address, nodeId, networkId, providerService);
        }
        return _instance;
    }

    public static ZMQSBSubscriberManager getInstance() {
        return _instance;
    }

    public byte getControllerTopic() {
        return controllerTopic;
    }

    public Long getControllerSourceId() {
        return controllerSourceId;
    }

    public void open() {
        broadcastAllWorker = Executors.newSingleThreadExecutor()
                .submit(new ZMQSubscriberWorker(ReservedTopics.BROADCAST_ALL.getType()));

        broadcastControllersWorker = Executors.newSingleThreadExecutor()
                .submit(new ZMQSubscriberWorker(ReservedTopics.BROADCAST_CONTROLLERS.getType()));

        broadcastTopicWorker = Executors.newSingleThreadExecutor()
                .submit(new AssignTopic());
    }

    @Override
    public void close() {
        send_goodbye_dpns(nodeId, networkId);
        run = false;
    }

    /**
     * Interrupts the BroadcastTopicworker if there is an Assign topic Conflict
     *
     * @param conflict - Flag to indicate conflict
     * @param subId    - Topic Id that caused the conflict
     */
    protected void BroadcastAllSubIdCallBack(boolean conflict, byte subId) {
        if (conflict && controllerTopic == subId) {
            this.conflictingTopic = true;
            broadcastTopicWorker.cancel(true);
        }
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

        if (controllerTopic == topic || (nodeId.equals(node_id) && networkId.equals(network_id))) {
            send_assign_conflict(nodeId, networkId);
        }
    }

    protected class ZMQSubscriberWorker implements Runnable {
        private final byte subscribedTopic;
        private ZContext ctx;

        ZMQSubscriberWorker(byte subscribedTopic) {
            this.subscribedTopic = subscribedTopic;
            this.ctx = new ZContext();
        }

//        /**
//         * Ensures the session id is an unsigned 64 bit integer
//         *
//         * @param sessionId - session id received from the DPN
//         * @return unsigned session id
//         */
//        private static BigInteger checkSessionId(BigInteger sessionId) {
//            if (sessionId.compareTo(BigInteger.ZERO) < 0) {
//                sessionId = sessionId.add(BigInteger.ONE.shiftLeft(64));
//            }
//            return sessionId;
//        }
//
//        /**
//         * Decodes a DownlinkDataNotification
//         *
//         * @param buf - message buffer
//         * @param key - Concatenation of node id + / + network id
//         * @return DownlinkDataNotification or null if it could not be successfully decoded
//         */
//        private static DownlinkDataNotification processDDN(byte[] buf, String key) {
//            DownlinkDataNotification ddnB = new DefaultDownlinkDataNotification();
//            ddnB.sessionId(checkSessionId(toBigInt(buf, 2)));
//            ddnB.notificationMessageType(DOWNLINK_DATA_NOTIFICATION_STRING);
//            ddnB.clientId(ClientIdentifier.of(FpcIdentity.of(FpcIdentityUnion.of(fromIntToLong(buf, 10)))));
//            ddnB.opId(OpIdentifier.of(BigInteger.valueOf(fromIntToLong(buf, 14))));
//            ddnB.notificationDpnId(uplinkDpnMap.get(key));
//            return ddnB;
//        }

        public Pair<Object, Object> decode(byte[] buf) {
            s11MsgType type;
            type = s11MsgType.getEnum(buf[1]);
            if (type.equals(s11MsgType.DDN)) {
                short nodeIdLen = buf[18];
                short networkIdLen = buf[19 + nodeIdLen];
                String key = new String(Arrays.copyOfRange(buf, 19, 19 + nodeIdLen)) + "/" + new String(Arrays.copyOfRange(buf, 20 + nodeIdLen, 20 + nodeIdLen + networkIdLen));
//                return uplinkDpnMap.get(key) == null ? null : new AbstractMap.SimpleEntry<>(uplinkDpnMap.get(key), processDDN(buf, key));
            } else if (type.equals(s11MsgType.DPN_STATUS_INDICATION)) {
                DpnStatusIndication status;

                short nodeIdLen = buf[8];
                short networkIdLen = buf[9 + nodeIdLen];
                String deviceId = new String(Arrays.copyOfRange(buf, 9, 9 + nodeIdLen)) + "/" + new String(Arrays.copyOfRange(buf, 10 + nodeIdLen, 10 + nodeIdLen + networkIdLen));

                status = DpnStatusIndication.getEnum(buf[3]);
                if (status.equals(DpnStatusIndication.HELLO)) {
                    log.info("Hello {} on topic {}", deviceId, buf[2]);

                    dpnDeviceListener.deviceAdded(deviceId, buf[2]);
                } else if (status.equals(DpnStatusIndication.GOODBYE)) {
                    log.info("Bye {}", deviceId);
                    dpnDeviceListener.deviceRemoved(deviceId);
                }
                return new Pair<>(status, deviceId);
            }
            return null;
        }

        @Override
        public void run() {
            ZMQ.Socket subscriber = this.ctx.createSocket(ZMQ.SUB);
            subscriber.connect(address);
            subscriber.subscribe(new byte[]{subscribedTopic});
            log.debug("Subscriber at {} / {}", address, subscribedTopic);
            while ((!Thread.currentThread().isInterrupted()) && run) {
                byte[] contents = subscriber.recv();
                byte topic = contents[0];
                s11MsgType messageType = s11MsgType.getEnum(contents[1]);
                log.info("Received {}", messageType);
                switch (topic) {
                    case 1:
                        if (messageType.equals(s11MsgType.ASSIGN_CONFLICT) &&
                                toInt(contents, 3) != controllerSourceId) {
                            BroadcastAllSubIdCallBack(true, contents[2]);
                        } else if (messageType.equals(s11MsgType.ASSIGN_TOPIC) &&
                                toInt(contents, 3) != controllerSourceId) {
                            SendAssignConflictMessage(contents);
                        }
                        break;
                    default:
                        Pair msg = decode(contents);
                        if (msg != null) {
                            Object key = msg.getKey();
                            if (key instanceof DownlinkDataNotification) {
                                // TODO handle DL notification
                            } else if (key instanceof DpnStatusIndication) {
                                if (key.equals(DpnStatusIndication.HELLO)) {
                                    byte dpnTopic = FpcUtil.getTopicFromNode(msg.getValue().toString());
                                    if (dpnTopic != -1) {
                                        send_status_ack(nodeId, networkId, dpnTopic);
                                    }
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
    protected class AssignTopic implements Runnable {
        private byte topic;

        public AssignTopic() {
            this.topic = (byte) ThreadLocalRandom.current().nextInt(MIN_TOPIC_VAL, MAX_TOPIC_VAL + 1);
        }

        @Override
        public void run() {
            try {
                send_assign_topic(nodeId, networkId, this.topic);
                log.debug("Thread sleeping: " + Thread.currentThread().getName());
                Thread.sleep(2000); // wait 10 sec before assigning topic
            } catch (InterruptedException e) {
                if (conflictingTopic) {
                    conflictingTopic = false;
                    this.topic = (byte) ThreadLocalRandom.current().nextInt(MIN_TOPIC_VAL, MAX_TOPIC_VAL + 1);
                    controllerTopic = this.topic;
                    this.run();
                    return;
                } else {
                    log.error(ExceptionUtils.getFullStackTrace(e));
                }
            }
            controllerTopic = this.topic;
            generalWorker = Executors.newSingleThreadExecutor().submit(new ZMQSubscriberWorker(this.topic));

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.error(ExceptionUtils.getFullStackTrace(e));
            }

            send_hello_dpns(nodeId, networkId);
        }

    }
}
