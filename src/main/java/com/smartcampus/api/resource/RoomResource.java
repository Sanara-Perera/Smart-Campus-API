package com.smartcampus.api.resource;

import com.smartcampus.api.exception.ResourceNotFoundException;
import com.smartcampus.api.exception.RoomNotEmptyException;
import com.smartcampus.api.model.Room;
import com.smartcampus.api.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Part 2 — Room Resource
 *
 * Manages the /api/v1/rooms collection.
 *
 * Endpoints:
 *   GET    /api/v1/rooms          → list all rooms
 *   POST   /api/v1/rooms          → create a new room
 *   GET    /api/v1/rooms/{roomId} → get one room by id
 *   DELETE /api/v1/rooms/{roomId} → delete a room (blocked if sensors still assigned)
 *
 * JAX-RS LIFECYCLE NOTE:
 * A new instance of this class is created for EVERY incoming HTTP request.
 * This is why we do NOT store rooms as instance variables — they would be
 * lost after each request. All persistent data lives in the singleton DataStore.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    @Context
    private UriInfo uriInfo;

    // -------------------------------------------------------------------------
    // GET /api/v1/rooms
    // Returns the full list of all rooms in the system.
    //
    // DESIGN NOTE — IDs vs Full Objects:
    // We return full Room objects (not just IDs) because:
    //   - Clients need room details (name, capacity) to display useful information
    //   - Returning only IDs would force the client to make N extra GET requests
    //     for each room — wasting bandwidth and time (the "N+1 request problem")
    //   - For very large datasets (thousands of rooms), pagination would be
    //     introduced, but for campus scale, full objects are appropriate.
    // -------------------------------------------------------------------------
    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(store.getAllRooms());
        return Response.ok(roomList).build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/rooms
    // Creates a new room from the JSON body.
    // Returns 201 Created with the new room and a Location header.
    // -------------------------------------------------------------------------
    @POST
    public Response createRoom(Room room) {

        // Validate required fields
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Room 'id' is required.\"}")
                    .build();
        }
        if (room.getName() == null || room.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Room 'name' is required.\"}")
                    .build();
        }

        // Reject if a room with this ID already exists
        if (store.roomExists(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"A room with id '" + room.getId() + "' already exists.\"}")
                    .build();
        }

        // Ensure sensorIds list starts empty (clients cannot pre-assign sensors via room creation)
        room.setSensorIds(new ArrayList<>());

        store.saveRoom(room);

        // Build the URI of the newly created resource for the Location header
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(room.getId())
                .build();

        // 201 Created — standard response for successful resource creation
        return Response
                .created(location)
                .entity(room)
                .build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/rooms/{roomId}
    // Returns a single room by its ID, or 404 if not found.
    // -------------------------------------------------------------------------
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRoomById(roomId);

        if (room == null) {
            throw new ResourceNotFoundException("Room", roomId);
        }

        return Response.ok(room).build();
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/rooms/{roomId}
    //
    // Deletes a room — but ONLY if it has no sensors assigned.
    //
    // IDEMPOTENCY:
    // DELETE is idempotent: repeated calls produce the same end state (room gone).
    // First call → removes room, returns 200 OK.
    // Second call → room is already gone, returns 404 Not Found.
    // The server state is identical after both calls (room does not exist),
    // but the response codes differ — this is correct and expected REST behaviour.
    // Idempotency means the EFFECT is the same, not that the response is the same.
    //
    // BUSINESS RULE — Safety Guard:
    // A room cannot be deleted while it still has sensors assigned to it.
    // Doing so would leave orphaned Sensor records pointing to a non-existent room.
    // The client must first reassign or delete all sensors, then delete the room.
    // -------------------------------------------------------------------------
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoomById(roomId);

        // 404 if room doesn't exist
        if (room == null) {
            throw new ResourceNotFoundException("Room", roomId);
        }

        // 409 Conflict if sensors are still assigned — throws RoomNotEmptyException
        // which is caught by RoomNotEmptyExceptionMapper → returns HTTP 409
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId, room.getSensorIds().size());
        }

        store.deleteRoom(roomId);

        // 200 OK with confirmation message
        return Response.ok()
                .entity("{\"message\":\"Room '" + roomId + "' has been successfully deleted.\"}")
                .build();
    }
}
