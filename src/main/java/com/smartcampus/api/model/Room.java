package com.smartcampus.api.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a physical room on the university campus.
 *
 * A Room is the top-level resource. It contains zero or more Sensors.
 * The sensorIds list acts as a foreign-key relationship: each string
 * in the list is the id of a Sensor that is deployed in this room.
 *
 * Example JSON representation:
 * {
 *   "id": "LIB-301",
 *   "name": "Library Quiet Study",
 *   "capacity": 50,
 *   "sensorIds": ["TEMP-001", "CO2-007"]
 * }
 */
public class Room {

    /** Unique identifier, e.g., "LIB-301" */
    private String id;

    /** Human-readable room name, e.g., "Library Quiet Study" */
    private String name;

    /** Maximum occupancy allowed for safety regulations */
    private int capacity;

    /** IDs of sensors currently deployed in this room */
    private List<String> sensorIds = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Default no-arg constructor required by Jackson for JSON deserialization */
    public Room() {}

    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<String> getSensorIds() {
        return sensorIds;
    }

    public void setSensorIds(List<String> sensorIds) {
        this.sensorIds = sensorIds;
    }

    /** Convenience method to add a sensor ID to this room */
    public void addSensorId(String sensorId) {
        if (!this.sensorIds.contains(sensorId)) {
            this.sensorIds.add(sensorId);
        }
    }

    /** Convenience method to remove a sensor ID from this room */
    public void removeSensorId(String sensorId) {
        this.sensorIds.remove(sensorId);
    }
}
