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
import org.onosproject.yang.gen.v1.fpc.rev20150105.fpc.P4DpnControlProtocol;

import java.math.BigInteger;

public class DpnP4Communicator extends P4DpnControlProtocol implements DpnCommunicationService {
    @Override
    public void create_session(byte topic_id, BigInteger imsi, Short default_ebi, Ip4Address ue_ipv4, Long s1u_sgw_teid, Ip4Address s1u_sgw_ipv4, Long session_id, Long client_id, BigInteger op_id) {

    }

    @Override
    public void modify_bearer(byte topic_id, Ip4Address s1u_sgw_ipv4, Long s1u_enodeb_teid, Ip4Address s1u_enodeb_ipv4, Long session_id, Long client_id, BigInteger op_id) {

    }

    @Override
    public void delete_session(byte topic_id, Long session_id, Long client_id, BigInteger op_id) {

    }

    @Override
    public void send_ADC_rules(Short topic, String domain_name, String ip, Short drop, Long rating_group, Long service_ID, String sponsor_ID) {

    }
}
