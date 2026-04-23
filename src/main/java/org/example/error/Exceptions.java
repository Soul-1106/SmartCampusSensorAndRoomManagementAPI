package org.example.error;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

public class Exceptions {

    public static class RoomNotEmptyException extends RuntimeException {
        public RoomNotEmptyException(String message) { super(message); }
    }

    @Provider
    public static class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
        @Override
        public Response toResponse(RoomNotEmptyException exception) {
            ErrorResponse error = new ErrorResponse(409, "Conflict", exception.getMessage());
            return Response.status(Response.Status.CONFLICT)
                    .entity(error)
                    .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    public static class LinkedResourceNotFoundException extends RuntimeException {
        public LinkedResourceNotFoundException(String message) { super(message); }
    }

    @Provider
    public static class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
        @Override
        public Response toResponse(LinkedResourceNotFoundException exception) {
            ErrorResponse error = new ErrorResponse(422, "Unprocessable Entity", exception.getMessage());
            return Response.status(422)
                    .entity(error)
                    .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    public static class SensorUnavailableException extends RuntimeException {
        public SensorUnavailableException(String message) { super(message); }
    }

    @Provider
    public static class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
        @Override
        public Response toResponse(SensorUnavailableException exception) {
            ErrorResponse error = new ErrorResponse(403, "Forbidden", exception.getMessage());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(error)
                    .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    @Provider
    public static class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
        private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

        @Override
        public Response toResponse(Throwable exception) {
            LOGGER.log(Level.SEVERE, "Unhandled server exception", exception);
            ErrorResponse error = new ErrorResponse(500, "Internal Server Error", "An unexpected error occurred");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error)
                    .type(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                    .build();
        }
    }
}
