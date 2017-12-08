/*
 * Copyright 2017-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.fpcagent.util;

import org.onlab.packet.Ip4Address;
import org.onlab.packet.Ip4Prefix;
import org.onosproject.fpcagent.workers.ZMQSBPublisherManager;
import org.onosproject.fpcagent.workers.ZMQSBSubscriberManager;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.ZmqDpnControlProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import static org.onosproject.fpcagent.util.Converter.*;

/**
 * DPDK DPN API over ZeroMQ for NGIC.
 */
public class DpnNgicCommunicator extends ZmqDpnControlProtocol implements DpnCommunicationService {
    protected static final Logger log = LoggerFactory.getLogger(DpnNgicCommunicator.class);

    @Override
    public void create_session(
            Short topic_id,
            BigInteger imsi,
            Short default_ebi,
            Ip4Address ue_ipv4,
            Long s1u_sgw_teid,
            Ip4Address s1u_sgw_ipv4,
            Long session_id,
            Long client_id,
            BigInteger op_id
    ) {
        /* NGIC Create Session expected buffer:
            uint8_t topic_id;
	        uint8_t type;
            struct create_session_t {
                uint64_t imsi;
                uint8_t  default_ebi;
                uint32_t ue_ipv4;
                uint32_t s1u_sgw_teid;
                uint32_t s1u_sgw_ipv4;
                uint64_t session_id;
                uint8_t  controller_topic;
                uint32_t client_id;
                uint32_t op_id;
            } create_session_msg;
         */
        // TODO: check if subscriber is open.
        ByteBuffer bb = ByteBuffer.allocate(41)
                .put(toUint8(topic_id))
                .put(s11MsgType.CREATE_SESSION.getType())
                .put(toUint64(imsi))
                .put(toUint8(default_ebi))
                .put(toUint32(ue_ipv4.toInt()))
                .put(toUint32(s1u_sgw_teid))
                .put(toUint32(s1u_sgw_ipv4.toInt()))
                .put(toUint64(BigInteger.valueOf(session_id)))
                .put(toUint8(ZMQSBSubscriberManager.getControllerTopic()))
                .put(toUint32(client_id))
                .put(toUint32(op_id.longValue()));

        log.debug("create_session: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    @Override
    public void modify_bearer(
            Short topic_id,
            Ip4Address s1u_sgw_ipv4,
            Long s1u_enodeb_teid,
            Ip4Address s1u_enodeb_ipv4,
            Long session_id,
            Long client_id,
            BigInteger op_id
    ) {
        /*
           NGIC Modify Session expected buffer:
           	uint8_t topic_id;
	        uint8_t type;
	        struct modify_bearer_t {
			    uint32_t s1u_sgw_ipv4;
			    uint32_t s1u_enodeb_teid;
			    uint32_t s1u_enodeb_ipv4;
			    uint64_t session_id;
			    uint8_t  controller_topic;
			    uint32_t client_id;
			    uint32_t op_id;
		    } modify_bearer_msg;
         */
        ByteBuffer bb = ByteBuffer.allocate(32)
                .put(toUint8(topic_id))
                .put(s11MsgType.MODIFY_BEARER.getType())
                .put(toUint32(s1u_sgw_ipv4.toInt()))
                .put(toUint32(s1u_enodeb_teid))
                .put(toUint32(s1u_enodeb_ipv4.toInt()))
                .put(toUint64(BigInteger.valueOf(session_id)))
                .put(toUint8(ZMQSBSubscriberManager.getControllerTopic()))
                .put(toUint32(client_id))
                .put(toUint32(op_id.longValue()));

        log.debug("modify_bearer: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    @Override
    public void delete_session(
            Short topic_id,
            Long session_id,
            Long client_id,
            BigInteger op_id
    ) {
        /*
            NGIC Delete Session expected buffer:
            uint8_t topic_id;
	        uint8_t type;
            struct delete_session_t {
			    uint64_t session_id;
			    uint8_t  controller_topic;
			    uint32_t client_id;
			    uint32_t op_id;
		    } delete_session_msg;
         */
        ByteBuffer bb = ByteBuffer.allocate(19)
                .put(toUint8(topic_id))
                .put(s11MsgType.DELETE_SESSION.getType())
                .put(toUint64(BigInteger.valueOf(session_id)))
                .put(toUint8(ZMQSBSubscriberManager.getControllerTopic()))
                .put(toUint32(client_id))
                .put(toUint32(op_id.longValue()));

        log.debug("delete_session: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    @Override
    public void send_ADC_rules(
            Short topic,
            String domain_name,
            String ip,
            Short drop,
            Long rating_group,
            Long service_ID, String sponsor_ID
    ) {
        // TODO take a look for this function. Not tested.
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
                .put(s11MsgType.ADC_RULE.getType())
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
     * Following the NGIC message types.
     *
     * This type structure is defined in NGIC at interface/zmq/zmqsub.h:51
     */
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
}