package org.onosproject.fpcagent.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.fpcagent.FpcService;
import org.onosproject.fpcagent.dto.configure.Configure;
import org.onosproject.fpcagent.dto.dpn.AddDpn;
import org.onosproject.rest.AbstractWebResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

@Path("/")
public class AppWebResource extends AbstractWebResource {
    private static final Logger log = LoggerFactory.getLogger(AppWebResource.class);

    protected CoreService coreService = get(CoreService.class);
    private FpcService fpcService;

    public AppWebResource() {
        ApplicationId appId = coreService.getAppId("org.onosproject.fpcagent");
        fpcService = get(FpcService.class);
    }

    @POST
    @Path("configure")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response configure(InputStream stream) {
        try {
            ObjectNode node = (ObjectNode) mapper().readTree(stream);
            Configure configure = mapper().readValue(node.toString(), Configure.class);
            fpcService.configure(configure);
        } catch (IOException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
        return Response.ok().build();
    }

    @POST
    @Path("dpns/add_dpn")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addDpn(InputStream stream) {
        try {
            ObjectNode node = (ObjectNode) mapper().readTree(stream);
            AddDpn dpns = mapper().readValue(node.toString(), AddDpn.class);
            fpcService.addDpn(dpns);
        } catch (IOException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("dpns/{dpn}")
    public Response deleteDpn(@PathParam("dpn") String dpn) {
        if (dpn != null) {
            fpcService.deleteDpn(dpn);
            return Response.ok().build();
        }
        return Response.ok().build();
    }

    @POST
    @Path("register_client")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerClient(InputStream stream) {

        return Response.ok().build();
    }
}
