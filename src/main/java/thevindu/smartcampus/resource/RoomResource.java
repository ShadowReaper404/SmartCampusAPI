/* Author: Thevindu Sithujaya | 5COSC022W Smart Campus API */
package thevindu.smartcampus.resource;

import thevindu.smartcampus.exception.RoomNotEmptyException;
import thevindu.smartcampus.model.Room;
import thevindu.smartcampus.storage.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/rooms
    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(store.getRooms().values());
        return Response.ok(roomList).build();
    }

    // POST /api/v1/rooms
    @POST
    public Response createRoom(Room room) {
        // Guard against null body (Jackson failed to parse)
        if (room == null) {
            return Response.status(400).entity(error("Request body is missing or not valid JSON.")).build();
        }
        if (room.getId() == null || room.getId().isBlank()) {
            return Response.status(400).entity(error("Room 'id' is required.")).build();
        }
        if (room.getName() == null || room.getName().isBlank()) {
            return Response.status(400).entity(error("Room 'name' is required.")).build();
        }
        if (store.getRoom(room.getId()) != null) {
            return Response.status(409).entity(error("Room '" + room.getId() + "' already exists.")).build();
        }
        store.addRoom(room);
        return Response.status(201).entity(room).build();
    }

    // GET /api/v1/rooms/{roomId}
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) {
            return Response.status(404).entity(error("Room '" + roomId + "' not found.")).build();
        }
        return Response.ok(room).build();
    }

    // DELETE /api/v1/rooms/{roomId}
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) {
            return Response.status(404).entity(error("Room '" + roomId + "' not found.")).build();
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId, room.getSensorIds().size());
        }
        store.deleteRoom(roomId);
        return Response.noContent().build();
    }

    private Map<String, String> error(String message) {
        Map<String, String> map = new HashMap<>();
        map.put("error", message);
        return map;
    }
}
