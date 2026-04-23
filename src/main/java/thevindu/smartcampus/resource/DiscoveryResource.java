/* Author: Thevindu Sithujaya | 5COSC022W Smart Campus API */
package thevindu.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Part 1 - Discovery Endpoint
 * GET http://localhost:8080/smart-campus-api/api/v1/
 */
@Path("/")
public class DiscoveryResource {

    @Context
    private UriInfo uriInfo;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {
        // Build base URL dynamically so it works on any machine/port
        String base = uriInfo.getBaseUri().toString(); // e.g. http://localhost:8080/smart-campus-api/api/v1/

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("api",     "Smart Campus Sensor & Room Management API");
        info.put("version", "1.0");
        info.put("contact", "thevindu@smartcampus.ac.uk");

        // HATEOAS links - dynamically built from actual server URL
        Map<String, String> links = new LinkedHashMap<>();
        links.put("rooms",   base + "rooms");
        links.put("sensors", base + "sensors");
        info.put("resources", links);

        return Response.ok(info).build();
    }
}
