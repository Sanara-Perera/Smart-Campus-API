package com.smartcampus.api.model;

/**
 * Represents an IoT sensor deployed in a campus room.
 *
 * A Sensor belongs to exactly one Room (via roomId).
 * It has a status that governs whether it can accept new readings:
 *   - ACTIVE      → fully operational, accepts readings
 *   - MAINTENANCE → temporarily down, cannot accept readings (throws 403)
 *   - OFFLINE     → decommissioned
 *
 * The currentValue field is always kept in sync with the most recent
 * SensorReading posted for this sensor.
 *
 * Example JSON:
 * {
 *   "id": "TEMP-001",
 *   "type": "Temperature",
 *   "status": "ACTIVE",
 *   "currentValue": 22.5,
 *   "roomId": "LIB-301"
 * }
 */
public class Sensor {

    /** Unique identifier, e.g., "TEMP-001" */
    private String id;

    /** Category: "Temperature", "CO2", "Occupancy", "Lighting", etc. */
    private String type;

    /** Current operational state: "ACTIVE", "MAINTENANCE", or "OFFLINE" */
    private String status;

    /** The most recent measurement recorded by this sensor */
    private double currentValue;

    /** Foreign key: the id of the Room this sensor is installed in */
    private String roomId;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Default no-arg constructor required by Jackson */
    public Sensor() {}

    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    /**
     * Helper: returns true if this sensor is in MAINTENANCE mode.
     * Used by SensorReadingResource to block new readings.
     */
    public boolean isInMaintenance() {
        return "MAINTENANCE".equalsIgnoreCase(this.status);
    }
}
