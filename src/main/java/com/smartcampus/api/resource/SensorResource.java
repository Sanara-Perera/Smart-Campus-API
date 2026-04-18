package com.smartcampus.api.resource;

import com.smartcampus.api.exception.LinkedResourceNotFoundException;
import com.smartcampus.api.exception.ResourceNotFoundException;
import com.smartcampus.api.model.Room;
import com.smartcampus.api.model.Sensor;
import com.smartcampus.api.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Part 3 — Sensor Resource
 *
 * Manages the /api/v1/sensors collection.
 *
 * Endpoints:
 *   GET  /api/v1/sensors          → list all sensors (optional ?type= filter)
 *   POST /api/v1/sensors          → register a new sensor (validates roomId exists)
 *   GET  /api/v1/sensors/{id}     → get one sensor by id
 *
 * Sub-Resource Locator (Part 4):
 *   ANY  /api/v1/sensors/{id}/readings → delegates to SensorReadingResource
 *
 * ABOUT @Consumes(APPLICATION_JSON):
 * The @Consumes annotation tells JAX-RS that our POST method only accepts
 * requests where the Content-Type header is "application/json".
 * If a client sends "text/plain" or "application/xml", JAX-RS automatically
 * rejects the request with HTTP 415 Unsupported Media Type — before our code
 * even runs. This protects us from malformed or unexpected input formats.
 *
 * ABOUT @QueryParam vs @PathParam for filtering:
 * We use GET /sensors?type=CO2 (query parameter) rather than GET /sensors/type/CO2 (path).
 * Query parameters are the correct REST idiom for filtering/searching a collection because:
 *   - They are OPTIONAL: /sensors and /sensors?type=CO2 both work
 *   - Path segments imply a unique resource identity; CO2 is not a resource, it's a filter
 *   - Multiple filters compose cleanly: ?type=CO2&status=ACTIVE
 *   - Path-based filtering (/sensors/type/CO2/status/ACTIVE) becomes unreadable
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    @Context
    private UriInfo uriInfo;

    // -------------------------------------------------------------------------
    // GET /api/v1/sensors
    // GET /api/v1/sensors?type=CO2
    //
    // Returns all sensors. If the optional "type" query parameter is provided,
    // only sensors whose type matches (case-insensitive) are returned.
    // -------------------------------------------------------------------------
    @GET
    public Response getAllSensors(@QueryParam("type") String typeFilter) {

        List<Sensor> result = store.getAllSensors().stream()
                .filter(s -> typeFilter == null || s.getType().equalsIgnoreCase(typeFilter))
                .collect(Collectors.toList());

        return Response.ok(result).build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/sensors/{sensorId}
    // Returns a single sensor by ID, or 404 if not found.
    // -------------------------------------------------------------------------
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensorById(sensorId);

        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor", sensorId);
        }

        return Response.ok(sensor).build();
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/sensors
    //
    // Registers a new sensor.
    //
    // INTEGRITY CHECK: the roomId in the request body MUST refer to a room
    // that already exists. If not, we throw LinkedResourceNotFoundException
    // which the mapper converts to HTTP 422 Unprocessable Entity.
    //
    // Side effect: the sensor's ID is added to its parent room's sensorIds list.
    // -------------------------------------------------------------------------
    @POST
    public Response createSensor(Sensor sensor) {

        // Validate required fields
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Sensor 'id' is required.\"}")
                    .build();
        }
        if (sensor.getType() == null || sensor.getType().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Sensor 'type' is required.\"}")
                    .build();
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Sensor 'roomId' is required.\"}")
                    .build();
        }

        // Check sensor ID uniqueness
        if (store.sensorExists(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"A sensor with id '" + sensor.getId() + "' already exists.\"}")
                    .build();
        }

        // REFERENTIAL INTEGRITY: verify the referenced room actually exists.
        // If not → LinkedResourceNotFoundException → 422 Unprocessable Entity
        Room room = store.getRoomById(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }

        // Default status to ACTIVE if not provided
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        // Persist the sensor
        store.saveSensor(sensor);

        // Maintain the bidirectional link: add this sensor's ID to the room's list
        room.addSensorId(sensor.getId());

        // Build Location header URI
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(sensor.getId())
                .build();

        // 201 Created
        return Response.created(location).entity(sensor).build();
    }

    // -------------------------------------------------------------------------
    // DELETE /api/v1/sensors/{sensorId}
    // Removes a sensor and unlinks it from its parent room.
    // -------------------------------------------------------------------------
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensorById(sensorId);

        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor", sensorId);
        }

        // Remove the sensor ID from the parent room's list (clean up the link)
        Room room = store.getRoomById(sensor.getRoomId());
        if (room != null) {
            room.removeSensorId(sensorId);
        }

        store.deleteSensor(sensorId);

        return Response.ok()
                .entity("{\"message\":\"Sensor '" + sensorId + "' has been successfully deleted.\"}")
                .build();
    }

    // -------------------------------------------------------------------------
    // Part 4 — Sub-Resource Locator
    //
    // ANY request to /api/v1/sensors/{sensorId}/readings is handed off to
    // SensorReadingResource. This method is called a "sub-resource locator"
    // because it doesn't handle the request itself — it LOCATES and RETURNS
    // the class that will handle it.
    //
    // BENEFITS OF THE SUB-RESOURCE LOCATOR PATTERN:
    //   1. Single Responsibility: SensorResource manages sensors; reading
    //      history is delegated to its own dedicated class.
    //   2. Complexity management: nesting all reading endpoints inside this
    //      class would make it enormous. Delegation keeps it focused.
    //   3. Reusability: SensorReadingResource could in theory be reused
    //      from multiple parent resources.
    //   4. Testability: each resource class can be unit tested independently.
    //
    // Note: no HTTP method annotation (@GET, @POST) here — that's what makes
    // it a locator rather than a regular resource method.
    // -------------------------------------------------------------------------
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {

        // Verify the sensor exists before delegating
        Sensor sensor = store.getSensorById(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor", sensorId);
        }

        // Return an instance of the sub-resource, passing the sensorId context
        return new SensorReadingResource(sensorId);
    }
}
