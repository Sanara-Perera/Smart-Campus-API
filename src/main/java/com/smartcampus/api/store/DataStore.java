package com.smartcampus.api.store;

import com.smartcampus.api.model.Room;
import com.smartcampus.api.model.Sensor;
import com.smartcampus.api.model.SensorReading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-Memory Data Store — the "database" of this application.
 *
 * WHY SINGLETON PATTERN?
 * JAX-RS creates a new instance of each Resource class for every HTTP request
 * (request-scoped lifecycle by default). That means if we stored our rooms/sensors
 * as instance variables inside the resource class, the data would be lost after
 * every single request. By using a Singleton DataStore, there is exactly ONE
 * instance shared by all resource instances and all threads — giving us persistent
 * in-memory state for the lifetime of the server process.
 *
 * WHY CONCURRENTHASHMAP?
 * Multiple HTTP requests can arrive simultaneously (concurrent threads). If we used
 * a plain HashMap, two threads writing at the same time could corrupt the data.
 * ConcurrentHashMap handles concurrent reads and writes safely without us needing
 * to write manual synchronization code.
 *
 * TRADE-OFF: All data is lost when the server stops. This is acceptable for this
 * coursework since database usage is explicitly prohibited.
 */
public class DataStore {

    // ---------------------------------------------------------------------------
    // Singleton Setup — "Initialization-on-demand holder" pattern (thread-safe)
    // ---------------------------------------------------------------------------

    private static class Holder {
        private static final DataStore INSTANCE = new DataStore();
    }

    /** Returns the single shared DataStore instance */
    public static DataStore getInstance() {
        return Holder.INSTANCE;
    }

    // ---------------------------------------------------------------------------
    // Storage Maps
    // ---------------------------------------------------------------------------

    /** All rooms keyed by their id */
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    /** All sensors keyed by their id */
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    /**
     * Historical readings for each sensor.
     * Key = sensorId, Value = list of SensorReading objects.
     * We use a ConcurrentHashMap, and individual lists are only written
     * under controlled conditions (one POST at a time per sensor).
     */
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    /** Private constructor prevents external instantiation */
    private DataStore() {
        loadSampleData();
    }

    // ---------------------------------------------------------------------------
    // Room Operations
    // ---------------------------------------------------------------------------

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Collection<Room> getAllRooms() {
        return rooms.values();
    }

    public Room getRoomById(String id) {
        return rooms.get(id);
    }

    public void saveRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public boolean deleteRoom(String id) {
        return rooms.remove(id) != null;
    }

    public boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    // ---------------------------------------------------------------------------
    // Sensor Operations
    // ---------------------------------------------------------------------------

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Collection<Sensor> getAllSensors() {
        return sensors.values();
    }

    public Sensor getSensorById(String id) {
        return sensors.get(id);
    }

    public void saveSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
    }

    public boolean deleteSensor(String id) {
        return sensors.remove(id) != null;
    }

    public boolean sensorExists(String id) {
        return sensors.containsKey(id);
    }

    // ---------------------------------------------------------------------------
    // SensorReading Operations
    // ---------------------------------------------------------------------------

    /**
     * Returns all readings for a given sensor.
     * Creates an empty list if none exist yet (lazy initialisation).
     */
    public List<SensorReading> getReadingsForSensor(String sensorId) {
        return readings.computeIfAbsent(sensorId, k -> new ArrayList<>());
    }

    /**
     * Appends a new reading to a sensor's history list.
     * Also updates the sensor's currentValue to match (data consistency).
     */
    public void addReading(String sensorId, SensorReading reading) {
        getReadingsForSensor(sensorId).add(reading);

        // Side-effect: keep the parent sensor's currentValue in sync
        Sensor sensor = sensors.get(sensorId);
        if (sensor != null) {
            sensor.setCurrentValue(reading.getValue());
        }
    }

    // ---------------------------------------------------------------------------
    // Sample Data — pre-populates the store so the API isn't empty on startup
    // ---------------------------------------------------------------------------

    /**
     * Loads a set of sample rooms and sensors so Postman/curl tests
     * can be run immediately without needing to POST data first.
     */
    private void loadSampleData() {

        // Sample Rooms
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Science Lab A", 30);
        Room r3 = new Room("HALL-001", "Main Lecture Hall", 200);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        // Sample Sensors
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE", 410.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-001",  "Occupancy",   "MAINTENANCE", 0.0, "LAB-101");
        Sensor s4 = new Sensor("TEMP-002", "Temperature", "ACTIVE", 19.8, "HALL-001");
        Sensor s5 = new Sensor("LIGHT-001","Lighting",    "OFFLINE", 0.0, "LAB-101");

        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);
        sensors.put(s4.getId(), s4);
        sensors.put(s5.getId(), s5);

        // Link sensors to rooms
        r1.addSensorId(s1.getId());
        r1.addSensorId(s2.getId());
        r2.addSensorId(s3.getId());
        r2.addSensorId(s5.getId());
        r3.addSensorId(s4.getId());

        // Sample readings for TEMP-001
        List<SensorReading> r1Readings = getReadingsForSensor("TEMP-001");
        r1Readings.add(new SensorReading("read-001", System.currentTimeMillis() - 60000, 21.0));
        r1Readings.add(new SensorReading("read-002", System.currentTimeMillis() - 30000, 22.0));
        r1Readings.add(new SensorReading("read-003", System.currentTimeMillis(),          22.5));
    }
}
