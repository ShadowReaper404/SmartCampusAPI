/* Author: Thevindu Sithujaya | 5COSC022W Smart Campus API */
package thevindu.smartcampus.exception.mappers;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable ex) {
        System.err.println("[UNHANDLED ERROR] " + ex.getClass().getName() + ": " + ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "INTERNAL_SERVER_ERROR");
        body.put("message", "An unexpected error occurred. Please contact the administrator.");
        return Response.status(500).type(MediaType.APPLICATION_JSON).entity(body).build();
    }
}
