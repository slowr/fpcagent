package org.onosproject.fpcagent;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.felix.scr.annotations.*;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.Ip4Prefix;
import org.onlab.util.AbstractAccumulator;
import org.onlab.util.Accumulator;
import org.onosproject.config.DynamicConfigEvent;
import org.onosproject.config.DynamicConfigListener;
import org.onosproject.config.DynamicConfigService;
import org.onosproject.config.Filter;
import org.onosproject.fpcagent.helpers.DpnCommunicationService;
import org.onosproject.fpcagent.helpers.DpnNgicCommunicator;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.IetfDmmFpcagentOpParam;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.*;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.configure.DefaultConfigureOutput;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.instructions.instructions.instrtype.Instr3GppMob;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.CreateOrUpdate;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.opinput.opbody.DeleteOrQuery;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.payload.Contexts;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.result.ResultEnum;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbody.resulttype.DefaultCommonSuccess;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbody.resulttype.DefaultDeleteSuccess;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.resultbody.resulttype.DefaultErr;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.DefaultTenant;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.Tenant;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.tenant.DefaultFpcMobility;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.tenant.DefaultFpcPolicy;
import org.onosproject.yang.gen.v1.ietfdmmfpcagent.rev20160803.ietfdmmfpcagent.tenants.tenant.DefaultFpcTopology;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.FpcIdentity;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.fpccontext.Dpns;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.mobilityinfo.mobprofileparameters.ThreegppTunnel;
import org.onosproject.yang.gen.v1.ietfdmmfpcbase.rev20160803.ietfdmmfpcbase.targetsvalue.Targets;
import org.onosproject.yang.gen.v1.ietfdmmthreegpp.rev20160803.ietfdmmthreegpp.threegppinstr.Bits;
import org.onosproject.yang.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.onosproject.fpcagent.FpcUtil.*;
import static org.onosproject.fpcagent.helpers.Converter.convertContext;
import static org.onosproject.fpcagent.helpers.Converter.getFpcIdentity;

@Component(immediate = true)
@Service
public class TenantManager implements TenantService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final IetfDmmFpcagentOpParam fpcAgentData = new IetfDmmFpcagentOpParam();

    private final Map<ClientIdentifier, Tenant> clientIdMap = Maps.newHashMap();

    private final InternalConfigListener listener = new InternalConfigListener();

    private final Accumulator<DynamicConfigEvent> accumulator = new InternalEventAccumulator();

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private ModelConverter modelConverter;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private DynamicConfigService dynamicConfigService;

    private DpnCommunicationService dpnCommunicationService;

    @Activate
    protected void activate() {
        dpnCommunicationService = new DpnNgicCommunicator();

        dynamicConfigService.addListener(listener);

        // Create the Default Tenant on activate
        DefaultTenants tenants = new DefaultTenants();

        DefaultTenant tenant = new DefaultTenant();
        tenant.tenantId(getFpcIdentity.apply("default"));

        tenant.fpcTopology(new DefaultFpcTopology());

        tenant.fpcPolicy(new DefaultFpcPolicy());

        tenant.fpcMobility(new DefaultFpcMobility());

        tenants.addToTenant(tenant);

        DefaultFpcAgentInfo fpcAgentInfo = new DefaultFpcAgentInfo();

        fpcAgentData.tenants(tenants);
        fpcAgentData.fpcAgentInfo(fpcAgentInfo);

        createNode(tenants);
        createNode(fpcAgentInfo);

        log.info("Tenant Service Started");
    }

    @Deactivate
    protected void deactivate() {
        dynamicConfigService.removeListener(listener);
        log.info("Tenant Service Stopped");
    }

    @Override
    public Optional<Tenant> getDefaultTenant() {
        return fpcAgentData.tenants().tenant().stream()
                .filter(tenant -> tenant.tenantId().equals(getFpcIdentity.apply("default")))
                .findFirst();
    }

    @Override
    public Optional<DefaultTenants> getTenants() {
        if (fpcAgentData.tenants() instanceof DefaultTenants) {
            return Optional.ofNullable((DefaultTenants) fpcAgentData.tenants());
        }
        return Optional.empty();
    }

    public Optional<Tenant> getTenant(FpcIdentity tenantId) {
        return fpcAgentData.tenants().tenant().stream()
                .filter(tenant -> tenant.tenantId().equals(tenantId))
                .findFirst();
    }

    public Optional<Tenant> getTenant(ClientIdentifier clientId) {
        return Optional.ofNullable(clientIdMap.get(clientId));
    }

    public Optional<Tenant> registerClient(ClientIdentifier clientId, FpcIdentity tenantId) {
        return getTenant(tenantId).map(tenant -> clientIdMap.put(clientId, tenant));
    }

    public Optional<Tenant> deregisterClient(ClientIdentifier clientId) {
        return Optional.ofNullable(clientIdMap.remove(clientId));
    }

    private ResourceData getResourceData(DataNode dataNode, ResourceId resId) {
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

    public List<ModelObject> getModelObjects(DataNode dataNode, ResourceId resourceId) {
        ResourceData data = getResourceData(dataNode, resourceId);
        ModelObjectData modelData = modelConverter.createModel(data);
        return modelData.modelObjects();
    }

    public void createNode(InnerModelObject innerModelObject) {
        ResourceData dataNode = modelConverter.createDataNode(
                DefaultModelObjectData.builder()
                        .addModelObject(innerModelObject)
                        .build()
        );
        dataNode.dataNodes().forEach(
                node -> dynamicConfigService.createNode(dataNode.resourceId(), node)
        );
    }

    public void updateNode(InnerModelObject innerModelObject) {
        ResourceData dataNode = modelConverter.createDataNode(
                DefaultModelObjectData.builder()
                        .addModelObject(innerModelObject)
                        .build()
        );
        dataNode.dataNodes().forEach(
                node -> dynamicConfigService.updateNode(dataNode.resourceId(), node)
        );
    }

    public DefaultConfigureOutput configureCreate(
            CreateOrUpdate create,
            ClientIdentifier clientId,
            OpIdentifier operationId
    ) {
        DefaultConfigureOutput configureOutput = new DefaultConfigureOutput();
        Collection<Callable<Object>> tasks = new ArrayList<>();
        DefaultCommonSuccess defaultCommonSuccess = new DefaultCommonSuccess();
        try {
            for (Contexts context : create.contexts()) {
                defaultCommonSuccess.addToContexts(context);

                if (getDefaultTenant().map(
                        tenant -> tenant.fpcMobility().contexts() != null && tenant.fpcMobility()
                                .contexts()
                                .stream()
                                .anyMatch(contexts -> contexts.contextId().equals(context.contextId()))
                ).orElse(false)) {
                    throw new IllegalStateException("Context already exists.");
                }

                for (Dpns dpn : context.dpns()) {
                    if (getDefaultTenant().map(
                            tenant -> tenant.fpcTopology().dpns() == null ||
                                    tenant.fpcTopology().dpns().stream()
                                            .noneMatch(dpns -> dpns.dpnId().equals(dpn.dpnId()))
                    ).orElse(false)) {
                        throw new IllegalStateException("DPN ID is not registered to the topology.");
                    }

                    if (context.instructions().instrType() instanceof Instr3GppMob) {
                        Instr3GppMob instr3GppMob = (Instr3GppMob) context.instructions().instrType();
                        String commands = Bits.toString(instr3GppMob.instr3GppMob().bits());

                        Ip4Address s1u_enodeb_ipv4 = Ip4Address.valueOf(context.ul().tunnelLocalAddress().toString()),
                                s1u_sgw_ipv4 = Ip4Address.valueOf(context.ul().tunnelRemoteAddress().toString()),
                                sgi_enodeb_ipv4 = Ip4Address.valueOf(context.dl().tunnelRemoteAddress().toString()),
                                sgi_sgw_ipv4 = Ip4Address.valueOf(context.dl().tunnelLocalAddress().toString());

                        long s1u_sgw_gtpu_teid, s1u_enb_gtpu_teid,
                                client_id = clientId.fpcIdentity().union().int64(),
                                session_id = context.contextId().fpcIdentity().union().int64();

                        BigInteger op_id = operationId.uint64(),
                                imsi = context.imsi().uint64();

                        short default_ebi = context.ebi().uint8(),
                                lbi = context.lbi().uint8();

                        Optional<String> key = getDefaultTenant().flatMap(tenant ->
                                tenant.fpcTopology()
                                        .dpns()
                                        .stream()
                                        .filter(dpns -> dpns.dpnId().equals(dpn.dpnId()))
                                        .findFirst().map(node -> node.nodeId() + "/" + node.networkId())
                        );

                        if (key.isPresent()) {
                            Short topic_id = FpcUtil.getTopicFromNode(key.get());

                            if (topic_id != null) {
                                if (context.ul().mobilityTunnelParameters().mobprofileParameters() instanceof ThreegppTunnel) {
                                    s1u_sgw_gtpu_teid = ((ThreegppTunnel) context.ul().mobilityTunnelParameters().mobprofileParameters()).tunnelIdentifier();
                                } else {
                                    throw new IllegalArgumentException("mobprofileParameters are not instance of ThreegppTunnel");
                                }
                                if (context.dl().mobilityTunnelParameters().mobprofileParameters() instanceof ThreegppTunnel) {
                                    s1u_enb_gtpu_teid = ((ThreegppTunnel) context.ul().mobilityTunnelParameters().mobprofileParameters()).tunnelIdentifier();
                                } else {
                                    throw new IllegalArgumentException("mobprofileParameters are not instance of ThreegppTunnel");
                                }


                                if (commands.contains("session")) {
                                    tasks.add(Executors.callable(() -> {
                                        dpnCommunicationService.create_session(
                                                topic_id,
                                                imsi,
                                                default_ebi,
                                                Ip4Prefix.valueOf(context.delegatingIpPrefixes().get(0).toString()).address(),
                                                s1u_sgw_gtpu_teid,
                                                s1u_sgw_ipv4,
                                                session_id,
                                                client_id,
                                                op_id
                                        );

                                        getDefaultTenant().ifPresent(
                                                tenant -> tenant.fpcMobility().addToContexts(convertContext(context))
                                        );
                                    }));

                                    if (commands.contains("downlink")) {
                                        tasks.add(Executors.callable(() -> {
                                            dpnCommunicationService.modify_bearer(
                                                    topic_id,
                                                    s1u_sgw_ipv4,
                                                    s1u_enb_gtpu_teid,
                                                    s1u_enodeb_ipv4,
                                                    session_id,
                                                    client_id,
                                                    op_id
                                            );

                                            getDefaultTenant().ifPresent(
                                                    tenant -> tenant.fpcMobility().addToContexts(convertContext(context))
                                            );
                                        }));
                                    }
                                } else if (commands.contains("indirect-forward")) {
                                    // TODO - Modify API for Indirect Forwarding to/from another SGW
                                } else if (commands.contains("uplink")) {
//                                    tasks.add(Executors.callable(() -> {
//                                        dpnCommunicationService.create_bearer_ul(
//                                                topic_id,
//                                                imsi,
//                                                lbi,
//                                                default_ebi,
//                                                ulLocalAddress,
//                                                s1u_sgw_gtpu_teid
//                                        );
//
//                                        getDefaultTenant().ifPresent(
//                                                tenant -> tenant.fpcMobility().addToContexts(convertContext(context))
//                                        );
//                                    }));
                                }
                            } else {
                                throw new IllegalArgumentException("Could not find Topic ID");
                            }
                        } else {
                            throw new IllegalArgumentException("DPN does not have node and network ID defined.");
                        }
                    }
                }
            }

            ExecutorService executor = Executors.newWorkStealingPool();
            executor.invokeAll(tasks).forEach(
                    future -> {
                        try {
                            future.get();
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }
            );

            configureOutput.resultType(defaultCommonSuccess);
            configureOutput.result(Result.of(ResultEnum.OK));
        } catch (Exception e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
            DefaultErr defaultErr = new DefaultErr();
            configureOutput.resultType(defaultErr);
            defaultErr.errorInfo(ExceptionUtils.getFullStackTrace(e));
            defaultErr.errorTypeId(ErrorTypeId.of(0));
        }

        return configureOutput;
    }

    public DefaultConfigureOutput configureUpdate(
            CreateOrUpdate update,
            ClientIdentifier clientId,
            OpIdentifier operationId
    ) {
        DefaultConfigureOutput configureOutput = new DefaultConfigureOutput();
        Collection<Callable<Object>> tasks = new ArrayList<>();

        try {
            DefaultCommonSuccess defaultCommonSuccess = new DefaultCommonSuccess();
            for (Contexts context : update.contexts()) {
                defaultCommonSuccess.addToContexts(context);

                if (getDefaultTenant().map(
                        tenant -> tenant.fpcMobility().contexts() == null ||
                                tenant.fpcMobility()
                                        .contexts()
                                        .parallelStream()
                                        .noneMatch(contexts -> contexts.contextId().equals(context.contextId()))
                ).orElse(false)) {
                    throw new IllegalStateException("Context doesn't exist.");
                }

                for (Dpns dpn : context.dpns()) {
                    if (getDefaultTenant().map(
                            tenant -> tenant.fpcTopology().dpns() == null ||
                                    tenant.fpcTopology().dpns()
                                            .stream()
                                            .noneMatch(dpns -> dpns.dpnId().equals(dpn.dpnId()))
                    ).orElse(false)) {
                        throw new IllegalStateException("DPN ID is not registered to the topology.");
                    }

                    if (context.instructions().instrType() instanceof Instr3GppMob) {
                        Instr3GppMob instr3GppMob = (Instr3GppMob) context.instructions().instrType();
                        String commands = Bits.toString(instr3GppMob.instr3GppMob().bits());

                        Ip4Address s1u_enodeb_ipv4 = Ip4Address.valueOf(context.ul().tunnelLocalAddress().toString()),
                                s1u_sgw_ipv4 = Ip4Address.valueOf(context.ul().tunnelRemoteAddress().toString()),
                                sgi_enodeb_ipv4 = Ip4Address.valueOf(context.dl().tunnelRemoteAddress().toString()),
                                sgi_sgw_ipv4 = Ip4Address.valueOf(context.dl().tunnelLocalAddress().toString());

                        long s1u_sgw_gtpu_teid, s1u_enb_gtpu_teid,
                                cId = clientId.fpcIdentity().union().int64(),
                                contextId = context.contextId().fpcIdentity().union().int64();

                        BigInteger opId = operationId.uint64();

                        Optional<String> key = getDefaultTenant().flatMap(tenant ->
                                tenant.fpcTopology()
                                        .dpns()
                                        .stream()
                                        .filter(dpns -> dpns.dpnId().equals(dpn.dpnId()))
                                        .findFirst().map(node -> node.nodeId() + "/" + node.networkId())
                        );

                        if (key.isPresent()) {
                            Short topic_id = FpcUtil.getTopicFromNode(key.get());

                            if (topic_id != null) {
                                if (context.ul().mobilityTunnelParameters().mobprofileParameters() instanceof ThreegppTunnel) {
                                    s1u_sgw_gtpu_teid = ((ThreegppTunnel) context.ul().mobilityTunnelParameters().mobprofileParameters()).tunnelIdentifier();
                                } else {
                                    throw new IllegalArgumentException("mobprofileParameters are not instance of ThreegppTunnel");
                                }
                                if (context.dl().mobilityTunnelParameters().mobprofileParameters() instanceof ThreegppTunnel) {
                                    s1u_enb_gtpu_teid = ((ThreegppTunnel) context.dl().mobilityTunnelParameters().mobprofileParameters()).tunnelIdentifier();
                                } else {
                                    throw new IllegalArgumentException("mobprofileParameters are not instance of ThreegppTunnel");
                                }

                                if (commands.contains("downlink")) {
                                    if (context.dl().lifetime() >= 0L) {
                                        tasks.add(Executors.callable(() ->
                                                dpnCommunicationService.modify_bearer(
                                                        topic_id,
                                                        s1u_sgw_ipv4,
                                                        s1u_enb_gtpu_teid,
                                                        s1u_enodeb_ipv4,
                                                        contextId,
                                                        cId,
                                                        opId
                                                )
                                        ));
                                    } else {
//                                        tasks.add(Executors.callable(() ->
//                                                dpnCommunicationService.delete_bearer(
//                                                        topic_id,
//                                                        s1u_enb_gtpu_teid
//                                                )
//                                        ));
                                    }
                                }
                                if (commands.contains("uplink")) {
                                    if (context.ul().lifetime() >= 0L) {
                                        tasks.add(Executors.callable(() ->
                                                dpnCommunicationService.modify_bearer(
                                                        topic_id,
                                                        s1u_sgw_ipv4,
                                                        s1u_enb_gtpu_teid,
                                                        s1u_enodeb_ipv4,
                                                        contextId,
                                                        cId,
                                                        opId
                                                )
                                        ));
                                    } else {
//                                        tasks.add(Executors.callable(() ->
//                                                dpnCommunicationService.delete_bearer(
//                                                        topic_id,
//                                                        s1u_sgw_gtpu_teid
//                                                )
//                                        ));
                                    }
                                }
                            } else {
                                throw new IllegalArgumentException("Could not find Topic ID");
                            }
                        } else {
                            throw new IllegalArgumentException("DPN does not have node and network ID defined.");
                        }
                    }
                }
            }

            ExecutorService executor = Executors.newWorkStealingPool();
            executor.invokeAll(tasks).forEach(
                    future -> {
                        try {
                            future.get();
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }
            );

            configureOutput.resultType(defaultCommonSuccess);
            configureOutput.result(Result.of(ResultEnum.OK));

            update.contexts().forEach(
                    contexts -> getDefaultTenant().ifPresent(
                            tenant -> tenant.fpcMobility().addToContexts(convertContext(contexts))
                    )
            );
        } catch (Exception e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
            DefaultErr defaultErr = new DefaultErr();
            defaultErr.errorInfo(ExceptionUtils.getFullStackTrace(e));
            defaultErr.errorTypeId(ErrorTypeId.of(0));
            configureOutput.resultType(defaultErr);
            configureOutput.result(Result.of(ResultEnum.ERR));
        }

        return configureOutput;
    }

    public DefaultConfigureOutput configureDelete(
            DeleteOrQuery delete,
            ClientIdentifier clientId,
            OpIdentifier operationId
    ) {
        DefaultConfigureOutput configureOutput = new DefaultConfigureOutput();
        Collection<Callable<Object>> tasks = new ArrayList<>();
        DefaultDeleteSuccess defaultDeleteSuccess = new DefaultDeleteSuccess();
        try {
            for (Targets target : delete.targets()) {
                defaultDeleteSuccess.addToTargets(target);
                String targetStr = target.target().toString(),
                        s = StringUtils.substringBetween(targetStr, "contexts=", "/"),
                        trgt = s != null ? s : StringUtils.substringAfter(targetStr, "contexts=");

                getDefaultTenant().map(
                        tenant -> {
                            if (tenant.fpcMobility().contexts() != null) {
                                return tenant.fpcMobility()
                                        .contexts()
                                        .stream()
                                        .filter(contexts -> contexts.contextId().toString().equals(trgt))
                                        .findFirst()
                                        .orElseThrow(
                                                () -> new IllegalArgumentException("Context doesn't exist.")
                                        );
                            }
                            throw new IllegalArgumentException("Contexts are empty.");
                        }
                ).ifPresent(
                        context -> context.dpns().forEach(
                                dpn -> {
                                    Optional<String> key = getDefaultTenant().flatMap(tenant ->
                                            tenant.fpcTopology()
                                                    .dpns()
                                                    .stream()
                                                    .filter(dpns -> dpns.dpnId().equals(dpn.dpnId()))
                                                    .findFirst().map(node -> node.nodeId() + "/" + node.networkId())
                                    );

                                    if (key.isPresent()) {
                                        Short topic_id = FpcUtil.getTopicFromNode(key.get());

                                        if (topic_id != null) {
                                            Long teid;
                                            if (context.ul().mobilityTunnelParameters().mobprofileParameters() instanceof ThreegppTunnel) {
                                                teid = ((ThreegppTunnel) context.ul().mobilityTunnelParameters().mobprofileParameters()).tunnelIdentifier();
                                            } else {
                                                throw new IllegalArgumentException("mobprofileParameters are not instance of ThreegppTunnel");
                                            }

                                            long client_id = clientId.fpcIdentity().union().int64();
                                            BigInteger op_id = operationId.uint64();

                                            if (targetStr.endsWith("ul") || targetStr.endsWith("dl")) {
//                                                tasks.add(Executors.callable(() -> {
//                                                    dpnCommunicationService.delete_bearer(
//                                                            topic_id,
//                                                            teid
//                                                    );
//
//                                                    context.dl(null);
//                                                    context.ul(null);
//                                                }));
                                            } else {
                                                tasks.add(Executors.callable(() -> {
                                                    dpnCommunicationService.delete_session(
                                                            topic_id,
                                                            context.contextId().fpcIdentity().union().int64(),
                                                            client_id,
                                                            op_id
                                                    );

                                                    getDefaultTenant().ifPresent(
                                                            tenant -> tenant.fpcMobility()
                                                                    .contexts()
                                                                    .remove(context)
                                                    );
                                                }));
                                            }

                                        } else {
                                            throw new IllegalArgumentException("Could not find Topic ID");
                                        }
                                    } else {
                                        throw new IllegalArgumentException("DPN does not have node and network ID defined.");
                                    }
                                }
                        )
                );

            }

            ExecutorService executor = Executors.newWorkStealingPool();
            executor.invokeAll(tasks).forEach(
                    future -> {
                        try {
                            future.get();
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }
            );

            configureOutput.resultType(defaultDeleteSuccess);
            configureOutput.result(Result.of(ResultEnum.OK));

        } catch (Exception e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
            DefaultErr defaultErr = new DefaultErr();
            configureOutput.resultType(defaultErr);
            defaultErr.errorInfo(ExceptionUtils.getFullStackTrace(e));
            defaultErr.errorTypeId(ErrorTypeId.of(0));
        }

        return configureOutput;
    }

    /**
     * Accumulates events to allow processing after a desired number of
     * events were accumulated.
     */
    private class InternalEventAccumulator extends AbstractAccumulator<DynamicConfigEvent> {

        /**
         * Constructs the event accumulator with timer and event limit.
         */
        private InternalEventAccumulator() {
            super(new Timer(TIMER), MAX_EVENTS, MAX_BATCH_MS, MAX_IDLE_MS);
        }

        @Override
        public void processItems(List<DynamicConfigEvent> events) {
            events.forEach(
                    event -> {
                        checkNotNull(event, EVENT_NULL);
                        switch (event.type()) {
                            case NODE_ADDED:
                            case NODE_DELETED:
                                Filter filter = Filter.builder().build();
                                DataNode node = dynamicConfigService.readNode(tenantsResourceId, filter);
                                getModelObjects(node, null).forEach(
                                        modelObject -> fpcAgentData.tenants((DefaultTenants) modelObject)
                                );
                                break;
                            case NODE_UPDATED:
                            case NODE_REPLACED:
                                break;
                            default:
                                log.warn(UNKNOWN_EVENT, event.type());
                                break;
                        }
                    }
            );
        }

    }

    /**
     * Representation of internal listener, listening for dynamic config event.
     */
    private class InternalConfigListener implements DynamicConfigListener {
        /**
         * Returns true if the event resource id points to the root level node
         * only and event is for addition and deletion; false otherwise.
         *
         * @param event config event
         * @return true if event is supported; false otherwise
         */
        private boolean isSupported(DynamicConfigEvent event) {
            ResourceId rsId = event.subject();
            List<NodeKey> storeKeys = rsId.nodeKeys();
            List<NodeKey> tenantKeys = tenantsResourceId.nodeKeys();
            return storeKeys.size() >= 2 && storeKeys.get(0).equals(tenantKeys.get(1));
        }

        @Override
        public boolean isRelevant(DynamicConfigEvent event) {
            return isSupported(event);
        }

        @Override
        public void event(DynamicConfigEvent event) {
            accumulator.add(event);
        }
    }
}
