package com.smartcampus.api.resource;

import com.smartcampus.api.exception.SensorUnavailableException;
import com.smartcampus.api.model.Sensor;
import com.smartcampus.api.model.SensorReading;
import com.smartcampus.api.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Part 4 — Sensor Reading Sub-Resource
 *
 * Handles historical reading data for a specific sensor.
 * Accessed via the sub-resource locator in SensorResource:
 *
 *   GET  /api/v1/sensors/{sensorId}/readings       → fetch all readings for this sensor
 *   POST /api/v1/sensors/{sensorId}/readings       → record a new reading
 *   GET  /api/v1/sensors/{sensorId}/readings/{rid} → fetch one reading by its id
 *
 * This class is NOT annotated with @Path at the class level — the path
 * is established by the parent SensorResource locator method.
 *
 * It is also NOT registered directly with JAX-RS (no @Provider). It is
 * instantiated programmatically by SensorResource.getReadingResource().
 *
 * IMPORTANT SIDE EFFECT on POST:
 * Every time a new reading is successfully recorded, the parent Sensor's
 * currentValue field is updated to match — ensuring data consistency across
 * the API. A client querying GET /sensors/{id} will always see the latest value.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final DataStore store = DataStore.getInstance();
    private final String sensorId;

    @Context
    private UriInfo uriInfo;

    /**
     * Constructor receives the sensorId from the parent locator method.
     * This gives the sub-resource its context: "I manage readings for sensor X."
     */
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/sensors/{sensorId}/readings
    // Returns the full reading history for this sensor.
    // -------------------------------------------------------------------------
    @GET
    public Response getAllReadings() {
        List<SensorReading> readingList = store.getReadingsForSensor(sensorId);
        return Response.ok(readingList).build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/sensors/{sensorId}/readings/{readingId}
    // Returns a single reading by its ID.
    // -------------------------------------------------------------------------
    @GET
    @Path("/{readingId}")
    public Response getReadingById(@PathParam("readingId") String readingId) {
        List<SensorReading> readings = store.getReadingsForSensor(sensorId);

        return readings.stream()
                .filter(r -> r.getId().equals(readingId))
                .findFirst()
                .map(r -> Response.ok(r).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"Reading '" + readingId + "' not found for sensor '" + sensorId + "'.\"}")
                        .build());
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/sensors/{sensorId}/readings
    //
    // Records a new sensor measurement.
    //
    // BUSINESS RULE — Sensor Status Check:
    // If the sensor's status is "MAINTENANCE", the sensor is physically
    // disconnected and cannot record data. We throw SensorUnavailableException
    // which the mapper converts to HTTP 403 Forbidden.
    //
    // SIDE EFFECT — currentValue Sync:
    // A successful POST updates the parent Sensor's currentValue field to match
    // this reading's value. This keeps GET /sensors/{id} always showing the
    // most recent measurement without a separate update call.
    //
    // The server generates a UUID for the reading ID and sets the timestamp
    // to now if the client hasn't provided them.
    // -------------------------------------------------------------------------
    @POST
    public Response addReading(SensorReading reading) {

        // Validate the reading value was provided
        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Request body is required.\"}")
                    .build();
        }

        // Fetch the sensor to check its status
        Sensor sensor = store.getSensorById(sensorId);

        // MAINTENANCE check → 403 Forbidden via SensorUnavailableExceptionMapper
        if (sensor.isInMaintenance()) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }

        // Server-side generation of ID and timestamp if not provided by client
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Persist reading AND update sensor's currentValue (side effect in DataStore)
        store.addReading(sensorId, reading);

        // Build Location header for the new reading
        URI location = (uriInfo != null)
                ? uriInfo.getAbsolutePathBuilder().path(reading.getId()).build()
                : URI.create("/api/v1/sensors/" + sensorId + "/readings/" + reading.getId());

        return Response.created(location).entity(reading).build();
    }
}
