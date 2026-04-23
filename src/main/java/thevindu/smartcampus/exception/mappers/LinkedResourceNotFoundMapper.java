/* Author: Thevindu Sithujaya | 5COSC022W Smart Campus API */
package thevindu.smartcampus.exception.mappers;

import thevindu.smartcampus.exception.LinkedResourceNotFoundException;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Provider
public class LinkedResourceNotFoundMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "LINKED_RESOURCE_NOT_FOUND");
        body.put("message", ex.getMessage());
        body.put("missingRoomId", ex.getMissingRoomId());
        return Response.status(422).type(MediaType.APPLICATION_JSON).entity(body).build();
    }
}
