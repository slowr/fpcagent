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

import org.onosproject.restconf.utils.RestconfUtils;
import org.onosproject.yang.model.DataNode;
import org.onosproject.yang.model.DefaultResourceData;
import org.onosproject.yang.model.ResourceData;
import org.onosproject.yang.model.ResourceId;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Helper class which stores all the static variables.
 */
class FpcUtil {
    static final int MAX_EVENTS = 1000;
    static final int MAX_BATCH_MS = 5000;
    static final int MAX_IDLE_MS = 1000;
    static final String TIMER = "dynamic-config-fpcagent-timer";
    static final String UNKNOWN_EVENT = "FPC Agent listener: unknown event: {}";
    static final String EVENT_NULL = "Event cannot be null";
    static final String FPC_APP_ID = "org.onosproject.fpcagent";

    // Resource ID for Configure DPN RPC command
    static ResourceId configureDpnResourceId;
    // Resource ID for Configure RPC command
    static ResourceId configureResourceId;
    // Resource ID for tenants data
    static ResourceId tenantsResourceId;
    static ResourceId registerClientResourceId;
    static ResourceId deregisterClientResourceId;

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
}
