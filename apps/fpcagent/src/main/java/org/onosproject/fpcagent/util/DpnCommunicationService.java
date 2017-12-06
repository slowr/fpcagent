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

import java.math.BigInteger;

/**
 * Communication Service which sends packets to desired DPNs.
 */
public interface DpnCommunicationService {

    /**
     * Creates Mobility Session.
     *
     * @param topic_id     - DPN Topic ID
     * @param imsi         - IMSI identifier
     * @param default_ebi  - EBI
     * @param ue_ipv4      - UE IPv4 Address
     * @param s1u_sgw_teid - SGW Tunnel Identifier
     * @param s1u_sgw_ipv4 - SGW IPv4
     * @param session_id   - Context Identifier
     * @param client_id    - Client Identifier
     * @param op_id        - Operation Identifier
     */
    void create_session(
            Short topic_id,
            BigInteger imsi,
            Short default_ebi,
            Ip4Address ue_ipv4,
            Long s1u_sgw_teid,
            Ip4Address s1u_sgw_ipv4,
            Long session_id,
            Long client_id,
            BigInteger op_id
    );

    /**
     * Modifies Bearer.
     *
     * @param topic_id        - DPN Topic ID
     * @param s1u_sgw_ipv4    - SGW IPv4 Address
     * @param s1u_enodeb_teid - ENodeB Tunnel Identifier
     * @param s1u_enodeb_ipv4 - ENodeB IPv4 Address
     * @param session_id      - Context Identifier
     * @param client_id       - Client Identifier
     * @param op_id           - Operation Identifier
     */
    void modify_bearer(
            Short topic_id,
            Ip4Address s1u_sgw_ipv4,
            Long s1u_enodeb_teid,
            Ip4Address s1u_enodeb_ipv4,
            Long session_id,
            Long client_id,
            BigInteger op_id
    );

    /**
     * Deletes Mobility Session.
     *
     * @param topic_id   - DPN Topic ID
     * @param session_id - Context Identifier
     * @param client_id  - Client Identifier
     * @param op_id      - Operation Identifier
     */
    void delete_session(
            Short topic_id,
            Long session_id,
            Long client_id,
            BigInteger op_id
    );

    /**
     * Creates the byte buffer to send ADC rules over ZMQ
     *
     * @param topic        - DPN Topic ID
     * @param domain_name  - domain
     * @param ip           - ipaddress/ipprefix (i.e. 127.0.0.1/32)
     * @param drop         - Drop if 1
     * @param rating_group - Rating Group
     * @param service_ID   - Service ID
     * @param sponsor_ID   - Sponsor ID
     */
    void send_ADC_rules(
            Short topic,
            String domain_name,
            String ip,
            Short drop,
            Long rating_group,
            Long service_ID,
            String sponsor_ID
    );
}
