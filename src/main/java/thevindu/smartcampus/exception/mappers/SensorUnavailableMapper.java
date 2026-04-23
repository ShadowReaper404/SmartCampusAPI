/* Author: Thevindu Sithujaya | 5COSC022W Smart Campus API */
package thevindu.smartcampus.exception.mappers;

import thevindu.smartcampus.exception.SensorUnavailableException;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Provider
public class SensorUnavailableMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "SENSOR_UNAVAILABLE");
        body.put("message", ex.getMessage());
        body.put("sensorId", ex.getSensorId());
        return Response.status(403).type(MediaType.APPLICATION_JSON).entity(body).build();
    }
}
