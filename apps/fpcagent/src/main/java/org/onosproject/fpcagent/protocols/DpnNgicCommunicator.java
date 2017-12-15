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

package org.onosproject.fpcagent.protocols;

import org.onlab.packet.Ip4Address;
import org.onlab.packet.Ip4Prefix;
import org.onosproject.fpcagent.workers.ZMQSBPublisherManager;
import org.onosproject.fpcagent.workers.ZMQSBSubscriberManager;
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.ZmqDpnControlProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;

import static org.onosproject.fpcagent.util.Converter.*;

/**
 * DPDK DPN API over ZeroMQ for NGIC.
 */
public class DpnNgicCommunicator extends ZmqDpnControlProtocol implements DpnCommunicationService {
    protected static final Logger log = LoggerFactory.getLogger(DpnNgicCommunicator.class);

    /**
     * Broadcasts the GOODBYE message to all the DPNs
     */
    public static void send_goodbye_dpns(String nodeId, String networkId) {
        ByteBuffer bb = ByteBuffer.allocate(10 + nodeId.length() + networkId.length());
        bb.put(ReservedTopics.BROADCAST_DPNS.getType())
                .put(s11MsgType.CONTROLLER_STATUS_INDICATION.getType())
                .put(ZMQSBSubscriberManager.getInstance().getControllerTopic())
                .put(ControllerStatusIndication.GOODBYE.getType())
                .put(toUint32(ZMQSBSubscriberManager.getInstance().getControllerSourceId()))
                .put(toUint8((short) nodeId.length()))
                .put(nodeId.getBytes())
                .put(toUint8((short) networkId.length()))
                .put(networkId.getBytes());

        log.info("send_goodbye_dpns: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    /**
     * Broadcasts the HELLO message to all the DPNs
     */
    public static void send_hello_dpns(String nodeId, String networkId) {
        ByteBuffer bb = ByteBuffer.allocate(10 + nodeId.length() + networkId.length());
        bb.put(ReservedTopics.BROADCAST_DPNS.getType())
                .put(s11MsgType.CONTROLLER_STATUS_INDICATION.getType())
                .put(ZMQSBSubscriberManager.getInstance().getControllerTopic())
                .put(ControllerStatusIndication.HELLO.getType())
                .put(toUint32(ZMQSBSubscriberManager.getInstance().getControllerSourceId()))
                .put(toUint8((short) nodeId.length()))
                .put(nodeId.getBytes())
                .put(toUint8((short) networkId.length()))
                .put(networkId.getBytes());

        log.info("send_hello_dpns: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    public static void send_assign_conflict(String nodeId, String networkId) {
        ByteBuffer bb = ByteBuffer.allocate(9 + nodeId.length() + networkId.length());
        bb.put(ReservedTopics.BROADCAST_ALL.getType())
                .put(s11MsgType.ASSIGN_CONFLICT.getType())
                .put(ZMQSBSubscriberManager.getInstance().getControllerTopic())
                .put(toUint32(ZMQSBSubscriberManager.getInstance().getControllerSourceId()))
                .put(toUint8((short) nodeId.length()))
                .put(nodeId.getBytes())
                .put(toUint8((short) networkId.length()))
                .put(networkId.getBytes());

        log.info("send_assign_conflict: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    public static void send_assign_topic(String nodeId, String networkId, byte topic) {
        ByteBuffer bb = ByteBuffer.allocate(9 + nodeId.length() + networkId.length());
        bb.put(ReservedTopics.BROADCAST_ALL.getType())
                .put(s11MsgType.ASSIGN_TOPIC.getType())
                .put(topic)
                .put(toUint32(ZMQSBSubscriberManager.getInstance().getControllerSourceId()))
                .put(toUint8((short) nodeId.length()))
                .put(nodeId.getBytes())
                .put(toUint8((short) networkId.length()))
                .put(networkId.getBytes());

        log.info("send_assign_topic: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    public static void send_status_ack(String nodeId, String networkId, byte topic) {
        ByteBuffer bb = ByteBuffer.allocate(9 + nodeId.length() + networkId.length())
                .put(topic)
                .put(s11MsgType.DPN_STATUS_ACK.getType())
                .put(ZMQSBSubscriberManager.getInstance().getControllerTopic())
                .put(toUint32(ZMQSBSubscriberManager.getInstance().getControllerSourceId()))
                .put(toUint8((short) nodeId.length()))
                .put(nodeId.getBytes())
                .put(toUint8((short) networkId.length()))
                .put(networkId.getBytes());

        log.info("send_status_ack: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    @Override
    public void create_session(
            byte topic_id,
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
                .put(topic_id)
                .put(s11MsgType.CREATE_SESSION.getType())
                .put(toUint64(imsi))
                .put(toUint8(default_ebi))
                .put(toUint32(ue_ipv4.toInt()))
                .put(toUint32(s1u_sgw_teid))
                .put(toUint32(s1u_sgw_ipv4.toInt()))
                .put(toUint64(BigInteger.valueOf(session_id)))
                .put(ZMQSBSubscriberManager.getInstance().getControllerTopic())
                .put(toUint32(client_id))
                .put(toUint32(op_id.longValue()));

        log.debug("create_session: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    @Override
    public void modify_bearer(
            byte topic_id,
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
                .put(topic_id)
                .put(s11MsgType.UPDATE_MODIFY_BEARER.getType())
                .put(toUint32(s1u_sgw_ipv4.toInt()))
                .put(toUint32(s1u_enodeb_teid))
                .put(toUint32(s1u_enodeb_ipv4.toInt()))
                .put(toUint64(BigInteger.valueOf(session_id)))
                .put(ZMQSBSubscriberManager.getInstance().getControllerTopic())
                .put(toUint32(client_id))
                .put(toUint32(op_id.longValue()));

        log.debug("modify_bearer: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    @Override
    public void delete_session(
            byte topic_id,
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
                .put(topic_id)
                .put(s11MsgType.DELETE_SESSION.getType())
                .put(toUint64(BigInteger.valueOf(session_id)))
                .put(ZMQSBSubscriberManager.getInstance().getControllerTopic())
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
        bb.put(ZMQSBSubscriberManager.getInstance().getControllerTopic());

        log.info("send_ADC_rules: {}", bb.array());
        ZMQSBPublisherManager.getInstance().send(bb);
    }

    public enum s11MsgType {
        CREATE_SESSION(1) {
            @Override
            public String toString() {
                return "CREATE_SESSION";
            }
        },
        UPDATE_MODIFY_BEARER(2) {
            @Override
            public String toString() {
                return "UPDATE_MODIFY_BEARER";
            }
        },
        DELETE_SESSION(3) {
            @Override
            public String toString() {
                return "DELETE_SESSION";
            }
        },
        DPN_RESPONSE(4) {
            @Override
            public String toString() {
                return "DPN_RESPONSE";
            }
        },
        DDN(5) {
            @Override
            public String toString() {
                return "DDN";
            }
        },
        DDN_ACK(5) {
            @Override
            public String toString() {
                return "DDN_ACK";
            }
        },
        RESERVED_1(7) {
            @Override
            public String toString() {
                return "RESERVED_1";
            }
        },
        RESERVED_2(8) {
            @Override
            public String toString() {
                return "RESERVED_2";
            }
        },
        RESERVED_3(9) {
            @Override
            public String toString() {
                return "RESERVED_3";
            }
        },
        ASSIGN_TOPIC(10) {
            @Override
            public String toString() {
                return "ASSIGN_TOPIC";
            }
        },
        ASSIGN_CONFLICT(11) {
            @Override
            public String toString() {
                return "ASSIGN_CONFLICT";
            }
        },
        DPN_STATUS_INDICATION(12) {
            @Override
            public String toString() {
                return "DPN_STATUS_INDICATION";
            }
        },
        DPN_STATUS_ACK(13) {
            @Override
            public String toString() {
                return "DPN_STATUS_ACK";
            }
        },
        CONTROLLER_STATUS_INDICATION(14) {
            @Override
            public String toString() {
                return "CONTROLLER_STATUS_INDICATION";
            }
        },
        GENERATE_CDR(15) {
            @Override
            public String toString() {
                return "GENERATE_CDR";
            }
        },
        GENERATE_CDR_ACK(16) {
            @Override
            public String toString() {
                return "GENERATE_CDR_ACK";
            }
        },
        ADC_RULE(17) {
            @Override
            public String toString() {
                return "ADC_RULE";
            }
        },
        PCC_RULE(18) {
            @Override
            public String toString() {
                return "PCC_RULE";
            }
        },
        METER_RULE(19) {
            @Override
            public String toString() {
                return "METER_RULE";
            }
        },
        SDF_RULE(20) {
            @Override
            public String toString() {
                return "SDF_RULE";
            }
        };

        private byte type;

        s11MsgType(int type) {
            this.type = (byte) type;
        }

        public static s11MsgType getEnum(byte name) {
            Optional<s11MsgType> any = Arrays.stream(s11MsgType.values())
                    .filter(typeStr -> typeStr.type == name)
                    .findAny();
            if (any.isPresent()) {
                return any.get();
            }
            throw new IllegalArgumentException("No enum defined for string: " + name);
        }

        public byte getType() {
            return type;
        }
    }

    public enum DpnStatusIndication {
        HELLO(1) {
            @Override
            public String toString() {
                return "HELLO";
            }
        },
        GOODBYE(2) {
            @Override
            public String toString() {
                return "GOODBYE";
            }
        },
        OVERLOAD_START(3) {
            @Override
            public String toString() {
                return "OVERLOAD_START";
            }
        },
        OVERLOAD_STOP(4) {
            @Override
            public String toString() {
                return "OVERLOAD_STOP";
            }
        },
        MATERIAL_CHANGE(5) {
            @Override
            public String toString() {
                return "MATERIAL_CHANGE";
            }
        },
        RESTART(6) {
            @Override
            public String toString() {
                return "RESTART";
            }
        },
        OUT_OF_SERVICE(7) {
            @Override
            public String toString() {
                return "OUT_OF_SERVICE";
            }
        };

        private byte type;

        DpnStatusIndication(int type) {
            this.type = (byte) type;
        }

        public static DpnStatusIndication getEnum(byte name) {
            Optional<DpnStatusIndication> any = Arrays.stream(DpnStatusIndication.values())
                    .filter(typeStr -> typeStr.type == name)
                    .findAny();
            if (any.isPresent()) {
                return any.get();
            }
            throw new IllegalArgumentException("No enum defined for string: " + name);
        }

        public byte getType() {
            return type;
        }
    }

    public enum ControllerStatusIndication {
        HELLO(1) {
            @Override
            public String toString() {
                return "HELLO";
            }
        },
        GOODBYE(2) {
            @Override
            public String toString() {
                return "GOODBYE";
            }
        };

        private byte type;

        ControllerStatusIndication(int type) {
            this.type = (byte) type;
        }

        public byte getType() {
            return type;
        }
    }

    public enum ReservedTopics {
        BROADCAST_ALL(1) {
            @Override
            public String toString() {
                return "BROADCAST_ALL";
            }
        },
        BROADCAST_CONTROLLERS(2) {
            @Override
            public String toString() {
                return "BROADCAST_CONTROLLERS";
            }
        },
        BROADCAST_DPNS(3) {
            @Override
            public String toString() {
                return "BROADCAST_DPNS";
            }
        };

        private byte type;

        ReservedTopics(int type) {
            this.type = (byte) type;
        }

        public byte getType() {
            return type;
        }
    }

}