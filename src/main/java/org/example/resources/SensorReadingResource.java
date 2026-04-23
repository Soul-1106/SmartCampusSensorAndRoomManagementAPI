package org.example.resources;

import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.example.error.ErrorResponse;
import org.example.error.Exceptions.SensorUnavailableException;
import org.example.model.Sensor;
import org.example.model.SensorReading;
import org.example.store.DataStore;

public class SensorReadingResource {
    private final String sensorId;
    private final DataStore dataStore = DataStore.getInstance();
    private final UriInfo uriInfo;

    public SensorReadingResource(String sensorId, UriInfo uriInfo) {
        this.sensorId = sensorId;
        this.uriInfo = uriInfo;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "Not Found", "Sensor not found"))
                    .build();
        }
        return Response.ok(dataStore.getSensorReadings(sensorId)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "Not Found", "Sensor not found"))
                    .build();
        }

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus()) || "OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor is unavailable to accept readings.");
        }
        
        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Bad Request", "Reading data is required"))
                    .build();
        }

        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        dataStore.addSensorReading(sensorId, reading);
        sensor.setCurrentValue(reading.getValue());

        return Response.created(uriInfo.getAbsolutePathBuilder().path(reading.getId()).build())
                .entity(reading)
                .build();
    }
}
