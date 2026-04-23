/* Author: Thevindu Sithujaya | 5COSC022W Smart Campus API */
package thevindu.smartcampus.resource;

import thevindu.smartcampus.exception.SensorUnavailableException;
import thevindu.smartcampus.model.Sensor;
import thevindu.smartcampus.model.SensorReading;
import thevindu.smartcampus.storage.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings
    @GET
    public Response getReadings() {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(404).entity(error("Sensor '" + sensorId + "' not found.")).build();
        }
        List<SensorReading> history = store.getReadings(sensorId);
        return Response.ok(history).build();
    }

    // POST /api/v1/sensors/{sensorId}/readings
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(404).entity(error("Sensor '" + sensorId + "' not found.")).build();
        }
        // 403 if sensor is under maintenance
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }
        if (reading == null) {
            return Response.status(400).entity(error("Request body is missing.")).build();
        }
        // Always create a fresh reading with auto UUID + timestamp
        SensorReading newReading = new SensorReading(reading.getValue());
        store.addReading(sensorId, newReading);

        // Update sensor's currentValue to reflect latest reading
        sensor.setCurrentValue(newReading.getValue());

        return Response.status(201).entity(newReading).build();
    }

    private Map<String, String> error(String msg) {
        Map<String, String> map = new HashMap<>();
        map.put("error", msg);
        return map;
    }
}
