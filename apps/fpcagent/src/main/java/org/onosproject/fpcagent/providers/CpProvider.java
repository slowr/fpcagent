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

package org.onosproject.fpcagent.providers;

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.ChassisId;
import org.onosproject.net.*;
import org.onosproject.net.device.DefaultDeviceDescription;
import org.onosproject.net.device.DeviceDescription;
import org.onosproject.net.device.DeviceProviderRegistry;
import org.onosproject.net.device.DeviceProviderService;
import org.onosproject.net.provider.AbstractProvider;
import org.onosproject.net.provider.ProviderId;
import org.slf4j.Logger;

import static org.onosproject.net.DeviceId.deviceId;
import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 */
@Component(immediate = true)
@Service
public class CpProvider extends AbstractProvider implements CpProviderService {

    private static final Logger log = getLogger(CpProvider.class);
    private final InternalDeviceListener listener = new InternalDeviceListener();

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected DeviceProviderRegistry providerRegistry;

    private DeviceProviderService providerService;

    public CpProvider() {
        super(new ProviderId("cp", "org.onosproject.providers.cp"));
    }

    @Activate
    public void activate() {
        providerService = providerRegistry.register(this);
        log.info("FPC Device Provider Started");
    }

    @Deactivate
    public void deactivate() {
        providerRegistry.unregister(this);
        providerService = null;
        log.info("FPC Device Provider Stopped");
    }

    public InternalDeviceListener getListener() {
        return listener;
    }

    @Override
    public void triggerProbe(DeviceId deviceId) {

    }

    @Override
    public void roleChanged(DeviceId deviceId, MastershipRole newRole) {

    }

    @Override
    public boolean isReachable(DeviceId deviceId) {
        return true;
    }

    @Override
    public void changePortState(DeviceId deviceId, PortNumber portNumber, boolean enable) {

    }

    public class InternalDeviceListener implements CpDeviceListener {

        @Override
        public void deviceAdded(String id, String address) {
            DeviceId deviceId = deviceId("cp:" + id);
            ChassisId chassisId = new ChassisId(deviceId.hashCode());

            Device.Type type = Device.Type.OTHER;
            SparseAnnotations annotations = DefaultAnnotations.builder()
                    .set(AnnotationKeys.NAME, id)
                    .set(AnnotationKeys.PROTOCOL, "RESTCONF")
                    .set(AnnotationKeys.MANAGEMENT_ADDRESS, address)
                    .build();
            DeviceDescription descriptionBase = new DefaultDeviceDescription(deviceId.uri(), type, "cp", "0.1", "0.1", id, chassisId),
                    description = new DefaultDeviceDescription(descriptionBase, annotations);

            providerService.deviceConnected(deviceId, description);
        }

        @Override
        public void deviceRemoved(String id) {
            providerService.deviceDisconnected(deviceId("cp:" + id));
        }
    }
}
