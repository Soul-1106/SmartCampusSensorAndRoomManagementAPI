package org.example;

import org.example.Exceptions.LinkedResourceNotFoundException;
import org.example.Exceptions.RoomNotEmptyException;
import org.example.Exceptions.SensorUnavailableException;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Resources {

    // --- Part 1.2: The "Discovery" Endpoint ---
    @Path("/")
    public static class DiscoveryResource {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Map<String, Object> discovery() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("version", "v1");
            metadata.put("contact", "admin@smartcampus.ac.uk");
            
            Map<String, String> links = new HashMap<>();
            links.put("rooms", "/api/v1/rooms");
            links.put("sensors", "/api/v1/sensors");
            metadata.put("links", links);
            return metadata;
        }
    }

    // --- Part 2: Room Management  ---
    @Path("/rooms")
    public static class SensorRoomResource {
        private final DataStore dataStore = DataStore.getInstance();

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Collection<Room> getRooms() {
            return dataStore.getRooms().values();
        }

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response createRoom(Room room) {
            if (room.getId() == null || room.getId().trim().isEmpty()) {
                room.setId(UUID.randomUUID().toString());
            }
            dataStore.addRoom(room);
            return Response.status(Response.Status.CREATED).entity(room).build();
        }

        @GET
        @Path("/{roomId}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response getRoom(@PathParam("roomId") String roomId) {
            Room room = dataStore.getRoom(roomId);
            if (room == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(room).build();
        }

        // --- Part 2.2: Room Deletion & Safety Logic ---
        @DELETE
        @Path("/{roomId}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response deleteRoom(@PathParam("roomId") String roomId) {
            Room room = dataStore.getRoom(roomId);
            if (room == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            if (!room.getSensorIds().isEmpty()) {
                throw new RoomNotEmptyException("Cannot delete room; it contains active sensors.");
            }
            dataStore.removeRoom(roomId);
            return Response.noContent().build();
        }
    }

    // --- Part 4.2: Historical Data Management ---
    public static class SensorReadingResource {
        private final String sensorId;
        private final DataStore dataStore = DataStore.getInstance();

        public SensorReadingResource(String sensorId) {
            this.sensorId = sensorId;
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public List<SensorReading> getReadings() {
            return dataStore.getSensorReadings(sensorId);
        }

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response addReading(SensorReading reading) {
            Sensor sensor = dataStore.getSensor(sensorId);
            if (sensor == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Sensor not found").build();
            }

            if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus()) || "OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
                throw new SensorUnavailableException("Sensor is unavailable to accept readings.");
            }

            if (reading.getId() == null) {
                reading.setId(UUID.randomUUID().toString());
            }
            if (reading.getTimestamp() == 0) {
                reading.setTimestamp(System.currentTimeMillis());
            }

            dataStore.addSensorReading(sensorId, reading);
            sensor.setCurrentValue(reading.getValue());

            return Response.status(Response.Status.CREATED).entity(reading).build();
        }
    }

    // --- Part 3: Sensor Operations & Linking ---
    @Path("/sensors")
    public static class SensorResource {
        private final DataStore dataStore = DataStore.getInstance();

        // --- Part 3.2: Filtered Retrieval & Search ---
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Collection<Sensor> getSensors(@QueryParam("type") String type) {
            if (type != null && !type.trim().isEmpty()) {
                return dataStore.getSensorsByType(type);
            }
            return dataStore.getAllSensors();
        }

        // --- Part 3.1: Sensor Resource & Integrity validation ---
        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response createSensor(Sensor sensor) {
            if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Room ID cannot be empty").build();
            }
            Room room = dataStore.getRoom(sensor.getRoomId());
            if (room == null) {
                throw new LinkedResourceNotFoundException("The specified roomId does not exist.");
            }
            if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
                sensor.setId(UUID.randomUUID().toString());
            }
            dataStore.addSensor(sensor);
            return Response.status(Response.Status.CREATED).entity(sensor).build();
        }

        @GET
        @Path("/{sensorId}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response getSensor(@PathParam("sensorId") String sensorId) {
            Sensor sensor = dataStore.getSensor(sensorId);
            if (sensor == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(sensor).build();
        }

        @DELETE
        @Path("/{sensorId}")
        @Produces(MediaType.APPLICATION_JSON)
        public Response deleteSensor(@PathParam("sensorId") String sensorId) {
            Sensor sensor = dataStore.getSensor(sensorId);
            if (sensor == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            dataStore.removeSensor(sensorId);
            return Response.noContent().build();
        }

        // --- Part 4.1: The Sub-Resource Locator Pattern ---
        @Path("/{sensorId}/readings")
        public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
            return new SensorReadingResource(sensorId);
        }
    }
}
