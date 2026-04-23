package org.example.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.example.model.Room;
import org.example.model.Sensor;
import org.example.model.SensorReading;

public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    // In-memory data structures
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private DataStore() { }

    public static DataStore getInstance() { return INSTANCE; }

    // --- Room Operations ---
    public Map<String, Room> getRooms() { return rooms; }

    public void addRoom(Room room) { rooms.put(room.getId(), room); }

    public Room getRoom(String id) { return rooms.get(id); }

    public void removeRoom(String id) { rooms.remove(id); }

    // --- Sensor Operations ---
    public Map<String, Sensor> getSensors() { return sensors; }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        Room room = rooms.get(sensor.getRoomId());
        if (room != null && !room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }
        sensorReadings.putIfAbsent(sensor.getId(), new ArrayList<>());
    }

    public Sensor getSensor(String id) { return sensors.get(id); }

    public void removeSensor(String id) {
        Sensor sensor = sensors.get(id);
        if (sensor != null) {
            Room room = rooms.get(sensor.getRoomId());
            if (room != null) {
                room.getSensorIds().remove(id);
            }
            sensors.remove(id);
            sensorReadings.remove(id);
        }
    }

    public List<Sensor> getSensorsByType(String type) {
        return sensors.values().stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    public List<Sensor> getAllSensors() { return new ArrayList<>(sensors.values()); }

    // --- Sensor Reading Operations ---
    public void addSensorReading(String sensorId, SensorReading reading) {
        sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }

    public List<SensorReading> getSensorReadings(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, new ArrayList<>());
    }
}
