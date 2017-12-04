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

package org.onosproject.fpcagent.helpers;

import org.onlab.packet.Ip4Address;

import java.math.BigInteger;

public interface DpnCommunicationService {
    /**
     * Creates Mobility Session
     *
     * @param topicId           - DPN
     * @param imsi              - IMSI
     * @param ue_ip             - Session IP Address
     * @param default_ebi       - Default EBI
     * @param s1u_sgw_gtpu_ipv4 - SGW GTP-U IPv4 Address
     * @param s1u_sgw_gtpu_teid - SGW GTP-U TEID
     * @param clientIdentifier  - Client Identifier
     * @param opIdentifier      - Operation Identifier
     * @param sessionId         - Session Id
     */
    void create_session(
            Short topicId,
            BigInteger imsi,
            Ip4Address ue_ip,
            Short default_ebi,
            Ip4Address s1u_sgw_gtpu_ipv4,
            Long s1u_sgw_gtpu_teid,
            Long clientIdentifier,
            BigInteger opIdentifier,
            Long sessionId
    );

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
    void delete_session(
            Short dpn,
            Short del_default_ebi,
            Long s1u_sgw_gtpu_teid,
            Long clientIdentifier,
            BigInteger opIdentifier,
            Long sessionId
    );

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
    void create_bearer_ul(
            Short dpn,
            BigInteger imsi,
            Short default_ebi,
            Short dedicated_ebi,
            Ip4Address s1u_sgw_gtpu_ipv4,
            Long s1u_sgw_gtpu_teid
    );

    /**
     * Create Downlink Bearer.
     *
     * @param dpn               - DPN
     * @param dedicated_ebi     - Default EBI
     * @param s1u_sgw_gtpu_teid - SGW GTP-U TEID
     * @param s1u_enb_gtpu_ipv4 - ENodeB GTP-U IPv4 Address
     * @param s1u_enb_gtpu_teid - ENodeB GTP-U TEID
     */
    void create_bearer_dl(
            Short dpn,
            Short dedicated_ebi,
            Long s1u_sgw_gtpu_teid,
            Ip4Address s1u_enb_gtpu_ipv4,
            Long s1u_enb_gtpu_teid
    );

    /**
     * Modify Downlink Bearer.
     *  @param topicId               - DPN
     * @param s1u_enodeb_ipv4 - ENodeB GTP-U IPv4 Address
     * @param s1u_enodeb_teid - ENodeB TEID
     * @param s1u_sgw_ipv4 - SGW GTP-U IPv4 Address
     * @param sessionId         - Session Id
     * @param clientId      - Operation Identifier
     * @param opId         - Session Id
     */
    void modify_bearer_dl(
            Short topicId,
            Ip4Address s1u_enodeb_ipv4,
            Long s1u_enodeb_teid,
            Ip4Address s1u_sgw_ipv4,
            Long sessionId,
            Long clientId,
            BigInteger opId
    );

    /**
     * Modify Uplink Bearer.
     *
     * @param dpn               - DPN
     * @param s1u_enb_gtpu_ipv4 - ENodeB GTP-U IPv4 Address
     * @param s1u_enb_gtpu_teid - ENodeB GTP-U TEID
     * @param s1u_sgw_gtpu_teid - SGW GTP-U TEID
     */
    void modify_bearer_ul(
            Short dpn,
            Ip4Address s1u_enb_gtpu_ipv4,
            Long s1u_enb_gtpu_teid,
            Long s1u_sgw_gtpu_teid
    );

    /**
     * DeleteOrQuery Bearer.
     *
     * @param dpnTopic          - DPN
     * @param s1u_sgw_gtpu_teid - SGW GTP-U TEID
     */
    void delete_bearer(
            Short dpnTopic,
            Long s1u_sgw_gtpu_teid
    );

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
    void send_ADC_rules(
            Short topic,
            String domain_name, String ip,
            Short drop, Long rating_group,
            Long service_ID, String sponsor_ID
    );
}
