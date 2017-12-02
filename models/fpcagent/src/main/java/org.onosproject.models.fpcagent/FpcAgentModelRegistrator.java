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

package org.onosproject.models.fpcagent;

import com.google.common.collect.ImmutableMap;
import org.apache.felix.scr.annotations.Component;
import org.onosproject.yang.AbstractYangModelRegistrator;
import org.onosproject.yang.gen.v1.fpc.rev20150105.FpcService;
import org.onosproject.yang.gen.v1.fpcconfig.rev20160927.FpcConfig;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.IetfDmmFpcagent;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.IetfDmmFpcbase;
import org.onosproject.yang.gen.v1.ietfdmmfpcpmip.rev20160119.IetfDmmFpcPmip;
import org.onosproject.yang.gen.v1.ietfdmmfpcpolicyext.rev20160803.IetfDmmFpcPolicyext;
import org.onosproject.yang.gen.v1.ietfdmmthreegpp.rev20160803.IetfDmmThreegpp;
import org.onosproject.yang.gen.v1.ietfpmipqos.rev20160210.IetfPmipQos;
import org.onosproject.yang.gen.v1.ietftrafficselectortypes.rev20160114.IetfTrafficSelectorTypes;
import org.onosproject.yang.model.DefaultYangModuleId;
import org.onosproject.yang.model.YangModuleId;
import org.onosproject.yang.runtime.AppModuleInfo;
import org.onosproject.yang.runtime.DefaultAppModuleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Component(immediate = true)
public class FpcAgentModelRegistrator extends AbstractYangModelRegistrator {
    static final Logger log = LoggerFactory.getLogger(FpcAgentModelRegistrator.class);

    public FpcAgentModelRegistrator() {
        super(FpcAgentModelRegistrator.class, getAppInfo());
    }

    private static Map<YangModuleId, AppModuleInfo> getAppInfo() {
        Map<YangModuleId, AppModuleInfo> appInfo = new HashMap<>();
        appInfo.put(new DefaultYangModuleId("ietf-dmm-fpcagent", "2016-08-03"),
                new DefaultAppModuleInfo(IetfDmmFpcagent.class, null));
        appInfo.put(new DefaultYangModuleId("ietf-dmm-fpcbase", "2016-08-03"),
                new DefaultAppModuleInfo(IetfDmmFpcbase.class, null));
        appInfo.put(new DefaultYangModuleId("fpc", "2015-01-05"),
                new DefaultAppModuleInfo(FpcService.class, null));
        appInfo.put(new DefaultYangModuleId("fpc-config", "2016-09-27"),
                new DefaultAppModuleInfo(FpcConfig.class, null));
        appInfo.put(new DefaultYangModuleId("ietf-dmm-fpc-pmip", "2016-01-19"),
                new DefaultAppModuleInfo(IetfDmmFpcPmip.class, null));
        appInfo.put(new DefaultYangModuleId("ietf-dmm-fpc-policyext", "2016-08-03"),
                new DefaultAppModuleInfo(IetfDmmFpcPolicyext.class, null));
        appInfo.put(new DefaultYangModuleId("ietf-dmm-threegpp", "2016-08-03"),
                new DefaultAppModuleInfo(IetfDmmThreegpp.class, null));
        appInfo.put(new DefaultYangModuleId("ietf-pmip-qos", "2016-02-10"),
                new DefaultAppModuleInfo(IetfPmipQos.class, null));
        appInfo.put(new DefaultYangModuleId("ietf-traffic-selector-types", "2016-01-14"),
                new DefaultAppModuleInfo(IetfTrafficSelectorTypes.class, null));

        return ImmutableMap.copyOf(appInfo);
    }
}
