package org.onosproject.fpcagent.helpers;


import org.onlab.packet.Ip4Address;
import org.onlab.packet.Ip4Prefix;
import org.onosproject.fpcagent.workers.ZMQSBPublisherManager;
import org.onosproject.fpcagent.workers.ZMQSBSubscriberManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import static org.onosproject.fpcagent.helpers.Converter.*;

/**
 * DPDK DPN API over ZeroMQ.
 */
public class DpnNgicCommunicator implements DpnCommunicationService {
    protected static final Logger log = LoggerFactory.getLogger(DpnNgicCommunicator.class);
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

    enum s11MsgType {
        CREATE_SESSION(1),
        MODIFY_BEARER(2),
        DELETE_SESSION(3),
        DPN_RESPONSE(4),
        DDN(5),
        ASSIGN_TOPIC(10),
        ASSIGN_CONFLICT(11),
        DPN_STATUS_INDICATION(12),
        DPN_STATUS_ACK(13),
        CONTROLLER_STATUS_INDICATION(14),
        ADC_RULE(17),
        PCC_RULE(18),
        METER_RULE(19),
        SDF_RULE(20);

        private byte type;

        s11MsgType(int type) {
            this.type = (byte) type;
        }

        public byte getType() {
            return type;
        }
    }

    @Override
    public void create_session(
            Short topicId,
            BigInteger imsi,
            Ip4Address ue_ip,
            Short default_ebi,
            Ip4Address s1u_sgw_gtpu_ipv4,
            Long s1u_sgw_gtpu_teid,
            Long clientIdentifier,
            BigInteger opIdentifier,
            Long sessionId
    ) {
        /* NGIC Create Session expected buffer:
            value: topic_id          bytes: 8
            value: type              bytes: 8
            value: imsi              bytes: 64
            value: default_ebi       bytes: 8
            value: ue_ipv4           bytes: 32
            value: s1u_sgw_teid      bytes: 32
            value: s1u_sgw_ipv4      bytes: 32
            value: session_id        bytes: 64
            value: controller_topic  bytes: 32
            value: client_id         bytes: 32
            value: op_id             bytes: 32
         */
        // TODO: check if subscriber is open.
        ByteBuffer bb = ByteBuffer.allocate(41)
                .put(toUint8(topicId))
                .put(s11MsgType.CREATE_SESSION.getType())
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

    @Override
    public void delete_session(
            Short dpn,
            Short del_default_ebi,
            Long s1u_sgw_gtpu_teid,
            Long clientIdentifier,
            BigInteger opIdentifier,
            Long sessionId
    ) {
        ByteBuffer bb = ByteBuffer.allocate(19)
                .put(toUint8(dpn))
                .put(s11MsgType.DELETE_SESSION.getType())
                .put(toUint64(BigInteger.valueOf(sessionId)))
                .put(toUint8(ZMQSBSubscriberManager.getControllerTopic()))
                .put(toUint32(clientIdentifier))
                .put(toUint32(opIdentifier.longValue()));

        log.info("delete_session: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    @Override
    public void create_bearer_ul(
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

    @Override
    public void create_bearer_dl(
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

    @Override
    public void modify_bearer_dl(
            Short topicId,
            Ip4Address s1u_enodeb_ipv4,
            Long s1u_enodeb_teid,
            Ip4Address s1u_sgw_ipv4,
            Long sessionId,
            Long clientId,
            BigInteger opId
    ) {
        /* NGIC Modify Session expected buffer:
            value: topic_id          bytes: 8
            value: type              bytes: 8
            value: s1u_enodeb_ipv4   bytes: 32
            value: s1u_enodeb_teid   bytes: 32
            value: s1u_sgw_ipv4      bytes: 32
            value: session_id        bytes: 64
            value: controller_topic  bytes: 8
            value: client_id         bytes: 32
            value: op_id             bytes: 32
         */
        ByteBuffer bb = ByteBuffer.allocate(32)
                .put(toUint8(topicId))
                .put(MODIFY_DL_BEARER_TYPE)
                .put(toUint32(s1u_sgw_ipv4.toInt()))
                .put(toUint32(s1u_enodeb_teid))
                .put(toUint32(s1u_enodeb_ipv4.toInt()))
                .put(toUint64(BigInteger.valueOf(sessionId)))
                .put(toUint8(ZMQSBSubscriberManager.getControllerTopic()))
                .put(toUint32(clientId))
                .put(toUint32(opId.longValue()));

        log.info("modify_bearer_dl: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    @Override
    public void modify_bearer_ul(
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

    @Override
    public void delete_bearer(
            Short dpnTopic,
            Long s1u_sgw_gtpu_teid) {
        ByteBuffer bb = ByteBuffer.allocate(7)
                .put(toUint8(dpnTopic))
                .put(DELETE_BEARER_TYPE)
                .put(toUint32(s1u_sgw_gtpu_teid));

        log.info("delete_bearer: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    @Override
    public void send_ADC_rules(Short topic,
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
}