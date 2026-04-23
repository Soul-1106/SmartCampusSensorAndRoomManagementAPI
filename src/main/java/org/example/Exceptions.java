package org.example;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Exceptions {

    // --- Room Not Empty Exception & Mapper ---
    public static class RoomNotEmptyException extends RuntimeException {
        public RoomNotEmptyException(String message) { super(message); }
    }

    // --- Part 5.1: Resource Conflict (409) ---
    @Provider
    public static class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
        @Override
        public Response toResponse(RoomNotEmptyException exception) {
            ErrorResponse error = new ErrorResponse(409, exception.getMessage());
            return Response.status(Response.Status.CONFLICT)
                    .entity(error)
                    .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    // --- Linked Resource Not Found Exception & Mapper ---
    public static class LinkedResourceNotFoundException extends RuntimeException {
        public LinkedResourceNotFoundException(String message) { super(message); }
    }

    // --- Part 5.2: Dependency Validation (422 Unprocessable Entity) ---
    @Provider
    public static class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
        @Override
        public Response toResponse(LinkedResourceNotFoundException exception) {
            ErrorResponse error = new ErrorResponse(422, exception.getMessage());
            return Response.status(422)
                    .entity(error)
                    .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    // --- Sensor Unavailable Exception & Mapper ---
    public static class SensorUnavailableException extends RuntimeException {
        public SensorUnavailableException(String message) { super(message); }
    }

    // --- Part 5.3: State Constraint (403 Forbidden) ---
    @Provider
    public static class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
        @Override
        public Response toResponse(SensorUnavailableException exception) {
            ErrorResponse error = new ErrorResponse(403, exception.getMessage());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(error)
                    .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    // --- Global Exception Mapper ---
    // --- Part 5.4: The Global Safety Net (500) ---
    @Provider
    public static class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
        private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

        @Override
        public Response toResponse(Throwable exception) {
            LOGGER.log(Level.SEVERE, "Unhandled server exception", exception);
            ErrorResponse error = new ErrorResponse(500, "Internal Server Error");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                    .build();
        }
    }
}
