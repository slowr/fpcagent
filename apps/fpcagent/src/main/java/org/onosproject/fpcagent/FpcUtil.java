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

package org.onosproject.fpcagent;

import com.google.common.collect.Maps;
import org.onosproject.restconf.utils.RestconfUtils;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.ClientIdentifier;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.OpIdentifier;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.yangautoprefixnotify.value.DefaultDownlinkDataNotification;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.yangautoprefixnotify.value.DownlinkDataNotification;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcDpnId;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcIdentity;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.fpcidentity.FpcIdentityUnion;
import org.onosproject.yang.model.DataNode;
import org.onosproject.yang.model.DefaultResourceData;
import org.onosproject.yang.model.ResourceData;
import org.onosproject.yang.model.ResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;

import static org.onosproject.fpcagent.helpers.Converter.fromIntToLong;
import static org.onosproject.fpcagent.helpers.Converter.toBigInt;

/**
 * Helper class which stores all the static variables.
 */
public class FpcUtil {
    protected static final Logger log = LoggerFactory.getLogger(FpcUtil.class);

    public static final int MAX_EVENTS = 1000;
    public static final int MAX_BATCH_MS = 5000;
    public static final int MAX_IDLE_MS = 1000;
    public static final String TIMER = "dynamic-config-fpcagent-timer";
    public static final String UNKNOWN_EVENT = "FPC Agent listener: unknown event: {}";
    public static final String EVENT_NULL = "Event cannot be null";
    public static final String FPC_APP_ID = "org.onosproject.fpcagent";

    private static final Map<String, FpcDpnId> uplinkDpnMap = Maps.newConcurrentMap();
    private static final Map<String, Short> nodeToTopicMap = Maps.newConcurrentMap();

    private static byte DPN_HELLO = 0b0000_0001;
    private static byte DPN_BYE = 0b0000_0010;
    private static byte DOWNLINK_DATA_NOTIFICATION = 0b0000_0101;
    private static byte DPN_STATUS_INDICATION = 0b0000_1100;
    private static byte DPN_OVERLOAD_INDICATION = 0b0000_0101;
    private static byte DPN_REPLY = 0b0000_0100;
    private static String DOWNLINK_DATA_NOTIFICATION_STRING = "Downlink-Data-Notification";

    // Resource ID for Configure DPN RPC command
    public static ResourceId configureDpnResourceId;
    // Resource ID for Configure RPC command
    public static ResourceId configureResourceId;
    // Resource ID for tenants data
    public static ResourceId tenantsResourceId;
    public static ResourceId registerClientResourceId;
    public static ResourceId deregisterClientResourceId;

    static {
        try {
            configureDpnResourceId = RestconfUtils.convertUriToRid(
                    new URI("/onos/restconf/operations/ietf-dmm-fpcagent:configure-dpn")
            );
            configureResourceId = RestconfUtils.convertUriToRid(
                    new URI("/onos/restconf/operations/ietf-dmm-fpcagent:configure")
            );
            tenantsResourceId = RestconfUtils.convertUriToRid(
                    new URI("/onos/restconf/data/ietf-dmm-fpcagent:tenants")
            );
            registerClientResourceId = RestconfUtils.convertUriToRid(
                    new URI("/onos/restconf/data/fpc:register-client")
            );
            deregisterClientResourceId = RestconfUtils.convertUriToRid(
                    new URI("/onos/restconf/data/fpc:deregister-client")
            );
        } catch (URISyntaxException ignored) {
        }
    }

    /**
     * Returns the resource data from the data node and the resource id.
     *
     * @param dataNode data node
     * @param resId    resource id
     * @return resource data
     */
    static ResourceData getResourceData(DataNode dataNode, ResourceId resId) {
        if (resId != null) {
            return DefaultResourceData.builder()
                    .addDataNode(dataNode)
                    .resourceId(resId)
                    .build();
        } else {
            return DefaultResourceData.builder()
                    .addDataNode(dataNode)
                    .build();
        }
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
                log.info("Hello {} on topic {}", key, buf[2]);
                nodeToTopicMap.put(key, (short) buf[2]);
            } else if (buf[3] == DPN_BYE) {
                status = DPNStatusIndication.Status.BYE;
                log.info("Bye {}", key);
                nodeToTopicMap.remove(key);
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
        return nodeToTopicMap.get(Key);
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
