package org.onosproject.fpcagent.helpers;


import com.google.common.collect.Maps;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.Ip4Prefix;
import org.onosproject.fpcagent.workers.ZMQSBPublisherManager;
import org.onosproject.fpcagent.workers.ZMQSBSubscriberManager;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.ClientIdentifier;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.OpIdentifier;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.yangautoprefixnotify.value.DefaultDownlinkDataNotification;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.yangautoprefixnotify.value.DownlinkDataNotification;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcDpnId;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcIdentity;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.fpcidentity.FpcIdentityUnion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;

import static org.onosproject.fpcagent.helpers.Converter.*;

/**
 * DPDK DPN API over ZeroMQ.
 */
public class DpnApi {
    protected static final Logger log = LoggerFactory.getLogger(DpnApi.class);
    /**
     * Topic for broadcasting
     */
    private static byte BROADCAST_TOPIC = 0b0000_0000;
    private static byte CREATE_SESSION_TYPE = 0b0000_0001;
    private static byte MODIFY_DL_BEARER_TYPE = 0b0000_0010;
    private static byte DELETE_SESSION_TYPE = 0b0000_0011;
    private static byte MODIFY_UL_BEARER_TYPE = 0b0000_0100;
    private static byte CREATE_UL_BEARER_TYPE = 0b0000_0101;
    private static byte CREATE_DL_BEARER_TYPE = 0b0000_0110;
    private static byte DELETE_BEARER_TYPE = 0b0000_0110;
    private static byte HELLO = 0b0000_1000;
    private static byte BYE = 0b0000_1001;
    private static byte SEND_ADC_TYPE = 0b001_0001;
    private static byte DDN_ACK = 0b0000_0110;

    private static byte DPN_HELLO = 0b0000_0001;
    private static byte DPN_BYE = 0b0000_0010;
    private static byte DOWNLINK_DATA_NOTIFICATION = 0b0000_0101;
    private static byte DPN_STATUS_INDICATION = 0b0000_1100;
    private static byte DPN_OVERLOAD_INDICATION = 0b0000_0101;
    private static byte DPN_REPLY = 0b0000_0100;
    private static String DOWNLINK_DATA_NOTIFICATION_STRING = "Downlink-Data-Notification";
    private static final Map<String, FpcDpnId> uplinkDpnMap;
    private static final Map<String, Short> topicToNodeMap;

    static {
        uplinkDpnMap = Maps.newConcurrentMap();
        topicToNodeMap = Maps.newConcurrentMap();
    }

    /**
     * Creates Mobility Session
     *
     * @param dpn               - DPN
     * @param imsi              - IMSI
     * @param ue_ip             - Session IP Address
     * @param default_ebi       - Default EBI
     * @param s1u_sgw_gtpu_ipv4 - SGW GTP-U IPv4 Address
     * @param s1u_sgw_gtpu_teid - SGW GTP-U TEID
     * @param clientIdentifier  - Client Identifier
     * @param opIdentifier      - Operation Identifier
     * @param sessionId         - Session Id
     */
    public static void create_session(
            Short dpn,
            BigInteger imsi,
            Ip4Address ue_ip,
            Short default_ebi,
            Ip4Address s1u_sgw_gtpu_ipv4,
            Long s1u_sgw_gtpu_teid,
            Long clientIdentifier,
            BigInteger opIdentifier,
            Long sessionId
    ) {
        // TODO: check if subscriber is open.
        ByteBuffer bb = ByteBuffer.allocate(41)
                .put(toUint8(dpn))
                .put(CREATE_SESSION_TYPE)
                .put(toUint64(imsi))
                .put(toUint8(default_ebi))
                .put(toUint32(ue_ip.toInt()))
                .put(toUint32(s1u_sgw_gtpu_teid))
                .put(toUint32(s1u_sgw_gtpu_ipv4.toInt()))
                .put(toUint64(BigInteger.valueOf(sessionId)))
                .put(toUint8(ZMQSBSubscriberManager.getControllerTopic()))
                .put(toUint32(clientIdentifier))
                .put(toUint32(opIdentifier.longValue()));

        log.info("create_session: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    /**
     * Modify Downlink Bearer
     *
     * @param dpn               - DPN
     * @param s1u_sgw_gtpu_teid - SGW GTP-U TEID
     * @param s1u_enb_gtpu_ipv4 - ENodeB GTP-U IPv4 Address
     * @param s1u_enb_gtpu_teid - ENodeB GTP-U TEID
     * @param clientIdentifier  - Client Identifier
     * @param opIdentifier      - Operation Identifier
     */
    public static void modify_bearer_dl(
            Short dpn,
            Long s1u_sgw_gtpu_teid,
            Ip4Address s1u_enb_gtpu_ipv4,
            Long s1u_enb_gtpu_teid,
            Long clientIdentifier,
            BigInteger opIdentifier
    ) {
        ByteBuffer bb = ByteBuffer.allocate(23)
                .put(toUint8(dpn))
                .put(MODIFY_DL_BEARER_TYPE)
                .put(toUint32(s1u_enb_gtpu_ipv4.toInt()))
                .put(toUint32(s1u_enb_gtpu_teid))
                .put(toUint32(s1u_sgw_gtpu_teid))
                .put(toUint32(clientIdentifier))
                .put(toUint32(opIdentifier.longValue()));

        log.info("modify_bearer_dl: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    /**
     * DeleteOrQuery Mobility Session.
     *
     * @param dpn               - DPN
     * @param del_default_ebi   - Default EBI
     * @param s1u_sgw_gtpu_teid - SGW GTP-U TEID
     * @param clientIdentifier  - Client Identifier
     * @param opIdentifier      - Operation Identifier
     * @param sessionId         - Session Id
     */
    public static void delete_session(
            Short dpn,
            Short del_default_ebi,
            Long s1u_sgw_gtpu_teid,
            Long clientIdentifier,
            BigInteger opIdentifier,
            Long sessionId
    ) {
        ByteBuffer bb = ByteBuffer.allocate(19)
                .put(toUint8(dpn))
                .put(DELETE_SESSION_TYPE)
                .put(toUint64(BigInteger.valueOf(sessionId)))
                .put(toUint8(ZMQSBSubscriberManager.getControllerTopic()))
                .put(toUint32(clientIdentifier))
                .put(toUint32(opIdentifier.longValue()));

        log.info("delete_session: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    /**
     * Create Uplink Bearer.
     *
     * @param dpn               - DPN
     * @param imsi              - IMSI
     * @param default_ebi       - Default EBI
     * @param dedicated_ebi     - Dedicated EBI
     * @param s1u_sgw_gtpu_ipv4 - SGW GTP-U IPv4 Address
     * @param s1u_sgw_gtpu_teid - SGW GTP-U TEID
     */
    public static void create_bearer_ul(
            Short dpn,
            BigInteger imsi,
            Short default_ebi,
            Short dedicated_ebi,
            Ip4Address s1u_sgw_gtpu_ipv4,
            Long s1u_sgw_gtpu_teid
    ) {
        ByteBuffer bb = ByteBuffer.allocate(21)
                .put(toUint8(dpn))
                .put(CREATE_UL_BEARER_TYPE)
                .put(toUint64(imsi))
                .put(toUint8(default_ebi))
                .put(toUint8(dedicated_ebi))
                .put(toUint32(s1u_sgw_gtpu_ipv4.toInt()))
                .put(toUint32(s1u_sgw_gtpu_teid));

        log.info("create_bearer_ul: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    /**
     * Create Downlink Bearer.
     *
     * @param dpn               - DPN
     * @param dedicated_ebi     - Default EBI
     * @param s1u_sgw_gtpu_teid - SGW GTP-U TEID
     * @param s1u_enb_gtpu_ipv4 - ENodeB GTP-U IPv4 Address
     * @param s1u_enb_gtpu_teid - ENodeB GTP-U TEID
     */
    public static void create_bearer_dl(
            Short dpn,
            Short dedicated_ebi,
            Long s1u_sgw_gtpu_teid,
            Ip4Address s1u_enb_gtpu_ipv4,
            Long s1u_enb_gtpu_teid
    ) {
        ByteBuffer bb = ByteBuffer.allocate(16)
                .put(toUint8(dpn))
                .put(CREATE_DL_BEARER_TYPE)
                .put(toUint8(dedicated_ebi))
                .put(toUint32(s1u_sgw_gtpu_teid))
                .put(toUint32(s1u_enb_gtpu_ipv4.toInt()))
                .put(toUint32(s1u_enb_gtpu_teid));

        log.info("create_bearer_dl: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    /**
     * Modify Downlink Bearer.
     *
     * @param dpn               - DPN
     * @param s1u_sgw_gtpu_ipv4 - SGW GTP-U IPv4 Address
     * @param s1u_enb_gtpu_teid - ENodeB TEID
     * @param s1u_enb_gtpu_ipv4 - ENodeB GTP-U IPv4 Address
     * @param clientIdentifier  - Client Identifier
     * @param opIdentifier      - Operation Identifier
     * @param sessionId         - Session Id
     */
    public static void modify_bearer_dl(
            Short dpn,
            Ip4Address s1u_enb_gtpu_ipv4,
            Long s1u_enb_gtpu_teid,
            Ip4Address s1u_sgw_gtpu_ipv4,
            Long clientIdentifier,
            BigInteger opIdentifier,
            Long sessionId
    ) {
        ByteBuffer bb = ByteBuffer.allocate(32)
                .put(toUint8(dpn))
                .put(MODIFY_DL_BEARER_TYPE)
                .put(toUint32(s1u_sgw_gtpu_ipv4.toInt()))
                .put(toUint32(s1u_enb_gtpu_teid))
                .put(toUint32(s1u_enb_gtpu_ipv4.toInt()))
                .put(toUint64(BigInteger.valueOf(sessionId)))
                .put(toUint8(ZMQSBSubscriberManager.getControllerTopic()))
                .put(toUint32(clientIdentifier))
                .put(toUint32(opIdentifier.longValue()));

        log.info("modify_bearer_dl: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    /**
     * Modify Uplink Bearer.
     *
     * @param dpn               - DPN
     * @param s1u_enb_gtpu_ipv4 - ENodeB GTP-U IPv4 Address
     * @param s1u_enb_gtpu_teid - ENodeB GTP-U TEID
     * @param s1u_sgw_gtpu_teid - SGW GTP-U TEID
     */
    public static void modify_bearer_ul(
            Short dpn,
            Ip4Address s1u_enb_gtpu_ipv4,
            Long s1u_enb_gtpu_teid,
            Long s1u_sgw_gtpu_teid
    ) {
        ByteBuffer bb = ByteBuffer.allocate(15)
                .put(toUint8(dpn))
                .put(MODIFY_UL_BEARER_TYPE)
                .put(toUint32(s1u_enb_gtpu_ipv4.toInt()))
                .put(toUint32(s1u_enb_gtpu_teid))
                .put(toUint32(s1u_sgw_gtpu_teid));

        log.info("modify_bearer_ul: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    /**
     * DeleteOrQuery Bearer.
     *
     * @param dpnTopic          - DPN
     * @param s1u_sgw_gtpu_teid - SGW GTP-U TEID
     */
    public static void delete_bearer(
            Short dpnTopic,
            Long s1u_sgw_gtpu_teid) {
        ByteBuffer bb = ByteBuffer.allocate(7)
                .put(toUint8(dpnTopic))
                .put(DELETE_BEARER_TYPE)
                .put(toUint32(s1u_sgw_gtpu_teid));

        log.info("delete_bearer: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    /**
     * Creates the byte buffer to send ADC rules over ZMQ
     *
     * @param topic        - DPN Topic
     * @param domain_name  - domain
     * @param ip           - ipaddress/ipprefix (i.e. 127.0.0.1/32)
     * @param drop         - Drop if 1
     * @param rating_group - Rating Group
     * @param service_ID   - Service ID
     * @param sponsor_ID   - Sponsor ID
     */
    public static void send_ADC_rules(Short topic,
                               String domain_name, String ip,
                               Short drop, Long rating_group,
                               Long service_ID, String sponsor_ID) {
        Ip4Prefix ip_prefix = null;
        if (ip != null) {
            ip_prefix = Ip4Prefix.valueOf(ip);
        }
        Short selector_type = (short) (domain_name != null ? 0 : ip_prefix != null ? 2 : ip_prefix.address() != null ? 1 : 255);
        if (selector_type == 255) {
            log.warn("Domain/IP not found, failed to send rules");
            return;
        }
        ByteBuffer bb = ByteBuffer.allocate(200);
        bb.put(toUint8(topic))
                .put(SEND_ADC_TYPE)
                .put(toUint8(selector_type));
        if (selector_type == 0) {
            bb.put(toUint8((short) domain_name.length()))
                    .put(domain_name.getBytes());
        }
        if ((selector_type == 1) || (selector_type == 2)) {
            int ip_address_long = ip_prefix.address().toInt();
            bb.put(toUint32(ip_address_long));
        }
        if (selector_type == 2) {
            bb.put(toUint16(ip_prefix.prefixLength()));
        }
        if (drop != null)
            bb.put(toUint8(drop));
        if (rating_group != null)
            bb.put(toUint32(rating_group));
        if (service_ID != null)
            bb.put(toUint32(service_ID));
        if (sponsor_ID != null && (short) sponsor_ID.length() > 0) {
            bb.put(toUint8((short) sponsor_ID.length()))
                    .put(sponsor_ID.getBytes());
        }
        bb.put(toUint8(ZMQSBSubscriberManager.getControllerTopic()));

        log.info("send_ADC_rules: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    /**
     * Ensures the session id is an unsigned 64 bit integer
     *
     * @param sessionId - session id received from the DPN
     * @return unsigned session id
     */
    private static BigInteger checkSessionId(BigInteger sessionId) {
        if (sessionId.compareTo(BigInteger.ZERO) < 0) {
            sessionId = sessionId.add(BigInteger.ONE.shiftLeft(64));
        }
        return sessionId;
    }

    /**
     * Decodes a DownlinkDataNotification
     *
     * @param buf - message buffer
     * @param key - Concatenation of node id + / + network id
     * @return DownlinkDataNotification or null if it could not be successfully decoded
     */
    private static DownlinkDataNotification processDDN(byte[] buf, String key) {
        DownlinkDataNotification ddnB = new DefaultDownlinkDataNotification();
        ddnB.sessionId(checkSessionId(toBigInt(buf, 2)));
        ddnB.notificationMessageType(DOWNLINK_DATA_NOTIFICATION_STRING);
        ddnB.clientId(ClientIdentifier.of(FpcIdentity.of(FpcIdentityUnion.of(fromIntToLong(buf, 10)))));
        ddnB.opId(OpIdentifier.of(BigInteger.valueOf(fromIntToLong(buf, 14))));
        ddnB.notificationDpnId(uplinkDpnMap.get(key));
        return ddnB;
    }

    /**
     * Decodes a DPN message.
     *
     * @param buf - message buffer
     * @return - A pair with the DPN Id and decoded Object
     */
    public static Map.Entry<FpcDpnId, Object> decode(byte[] buf) {
        if (buf[1] == DPN_REPLY) {
            return null;
        } else if (buf[1] == DOWNLINK_DATA_NOTIFICATION) {
            short nodeIdLen = buf[18];
            short networkIdLen = buf[19 + nodeIdLen];
            String key = new String(Arrays.copyOfRange(buf, 19, 19 + nodeIdLen)) + "/" + new String(Arrays.copyOfRange(buf, 20 + nodeIdLen, 20 + nodeIdLen + networkIdLen));
            return uplinkDpnMap.get(key) == null ? null : new AbstractMap.SimpleEntry<>(uplinkDpnMap.get(key), processDDN(buf, key));
        } else if (buf[1] == DPN_STATUS_INDICATION) {
            DPNStatusIndication.Status status = null;

            short nodeIdLen = buf[8];
            short networkIdLen = buf[9 + nodeIdLen];
            String key = new String(Arrays.copyOfRange(buf, 9, 9 + nodeIdLen)) + "/" + new String(Arrays.copyOfRange(buf, 10 + nodeIdLen, 10 + nodeIdLen + networkIdLen));
            if (buf[3] == DPN_OVERLOAD_INDICATION) {
                status = DPNStatusIndication.Status.OVERLOAD_INDICATION;
            } else if (buf[3] == DPN_HELLO) {
                status = DPNStatusIndication.Status.HELLO;
                topicToNodeMap.put(key, (short) buf[2]);
            } else if (buf[3] == DPN_BYE) {
                status = DPNStatusIndication.Status.BYE;
                topicToNodeMap.remove(key);
            }
            return new AbstractMap.SimpleEntry<>(uplinkDpnMap.get(key), new DPNStatusIndication(status, key));
        }
        return null;
    }

    /**
     * Gets the mapping for node id / network id to ZMQ Topic
     *
     * @param Key - Concatenation of node id + / + network id
     * @return - ZMQ Topic
     */
    public static Short getTopicFromNode(String Key) {
        if (Key == null) return 1;
        Short aShort = topicToNodeMap.get(Key);
        return aShort != null ? aShort : (short) 1;
    }

    /**
     * Provides basic status changes,
     */
    public static class DPNStatusIndication {
        private final Status status;
        private final String key; //nodeId +"/"+ networkId
        /**
         * Node Reference of the DPN
         */
        public Short nodeRef;
        /**
         * Constructor providing the DPN and its associated Status.
         *
         * @param status - DPN Status
         * @param key    - Combination of node id and network id
         */
        public DPNStatusIndication(Status status,
                                   String key) {
            this.status = status;
            this.key = key;
        }

        /**
         * Provides DPN Status
         *
         * @return Status associated to the DPN.
         */
        public Status getStatus() {
            return status;
        }

        /**
         * Provides the DPN key - nodeId +"/"+ networkId
         *
         * @return FpcDpnId
         */
        public String getKey() {
            return this.key;
        }

        /**
         * Basic DPN Status
         */
        public enum Status {
            HELLO,
            BYE,
            OVERLOAD_INDICATION
        }
    }
}