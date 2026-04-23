/* Author: Thevindu Sithujaya | 5COSC022W Smart Campus API */
package thevindu.smartcampus.storage;

import thevindu.smartcampus.model.Room;
import thevindu.smartcampus.model.Sensor;
import thevindu.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory data store — acts as the "database" for this project.
 *
 * WHY ConcurrentHashMap?
 * JAX-RS creates a new resource instance per request (by default).
 * Multiple requests can arrive simultaneously. If we used a plain HashMap,
 * two threads could corrupt the data at the same time.
 * ConcurrentHashMap is thread-safe and prevents race conditions.
 *
 * This class uses the Singleton pattern so every resource class
 * shares the exact same data.
 */
public class DataStore {

    // ── Singleton setup ──────────────────────────────────────────────────────
    private static final DataStore INSTANCE = new DataStore();
    public static DataStore getInstance() { return INSTANCE; }
    private DataStore() { seedData(); } // private constructor

    // ── Storage Maps ─────────────────────────────────────────────────────────
    // Key = entity ID, Value = entity object
    private final Map<String, Room>   rooms   = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // Readings are stored per sensor: sensorId -> list of readings
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // ── Seed Data (so the API has something to show on startup) ───────────────
    private void seedData() {
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Science Lab", 30);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);

        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE", 412.0, "LAB-101");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);

        // Link sensors to rooms
        r1.getSensorIds().add(s1.getId());
        r2.getSensorIds().add(s2.getId());

        // Empty reading lists for each sensor
        readings.put(s1.getId(), new ArrayList<>());
        readings.put(s2.getId(), new ArrayList<>());
    }

    // ── Room methods ─────────────────────────────────────────────────────────
    public Map<String, Room> getRooms() { return rooms; }

    public Room getRoom(String id) { return rooms.get(id); }

    public void addRoom(Room room) { rooms.put(room.getId(), room); }

    public boolean deleteRoom(String id) {
        if (!rooms.containsKey(id)) return false;
        rooms.remove(id);
        return true;
    }

    // ── Sensor methods ───────────────────────────────────────────────────────
    public Map<String, Sensor> getSensors() { return sensors; }

    public Sensor getSensor(String id) { return sensors.get(id); }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        readings.put(sensor.getId(), new ArrayList<>()); // init empty readings list
    }

    // ── Reading methods ──────────────────────────────────────────────────────
    public List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }

    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}
