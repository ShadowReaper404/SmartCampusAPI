/* Author: Thevindu Sithujaya | 5COSC022W Smart Campus API */
package thevindu.smartcampus.resource;

import thevindu.smartcampus.exception.LinkedResourceNotFoundException;
import thevindu.smartcampus.model.Room;
import thevindu.smartcampus.model.Sensor;
import thevindu.smartcampus.storage.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/sensors  (optional ?type=CO2 filter)
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>(store.getSensors().values());
        if (type != null && !type.isBlank()) {
            result.removeIf(s -> !s.getType().equalsIgnoreCase(type));
        }
        return Response.ok(result).build();
    }

    // POST /api/v1/sensors
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null) {
            return Response.status(400).entity(error("Request body is missing or not valid JSON.")).build();
        }
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(400).entity(error("Sensor 'id' is required.")).build();
        }
        // Verify roomId exists - throws 422 if not
        if (sensor.getRoomId() == null || store.getRoom(sensor.getRoomId()) == null) {
            throw new LinkedResourceNotFoundException(sensor.getRoomId());
        }
        if (store.getSensor(sensor.getId()) != null) {
            return Response.status(409).entity(error("Sensor '" + sensor.getId() + "' already exists.")).build();
        }
        if (sensor.getStatus() == null) sensor.setStatus("ACTIVE");

        store.addSensor(sensor);

        // Link sensor to its room
        Room room = store.getRoom(sensor.getRoomId());
        if (!room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }
        return Response.status(201).entity(sensor).build();
    }

    // GET /api/v1/sensors/{sensorId}
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(404).entity(error("Sensor '" + sensorId + "' not found.")).build();
        }
        return Response.ok(sensor).build();
    }

    // Sub-resource locator for readings
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    private Map<String, String> error(String msg) {
        Map<String, String> map = new HashMap<>();
        map.put("error", msg);
        return map;
    }
}
