package org.example;


import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static final String BASE_URI = "http://localhost:8080/";

    // --- Part 5.5: API Request & Response Logging Filters ---
    @Provider
    public static class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
        private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            LOGGER.info("Incoming request: " + requestContext.getMethod() + " " + requestContext.getUriInfo().getRequestUri());
        }

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
            LOGGER.info("Outgoing response with HTTP status: " + responseContext.getStatus());
        }
    }

    // --- Part 1.1: Project & Application Configuration ---
    @ApplicationPath("/api/v1")
    public static class SmartCampusApplication extends ResourceConfig {
        public SmartCampusApplication() {
            // Register Resources
            register(Resources.DiscoveryResource.class);
            register(Resources.SensorRoomResource.class);
            register(Resources.SensorResource.class);

            // Register Exception Mappers
            register(Exceptions.RoomNotEmptyExceptionMapper.class);
            register(Exceptions.LinkedResourceNotFoundExceptionMapper.class);
            register(Exceptions.SensorUnavailableExceptionMapper.class);
            register(Exceptions.GlobalExceptionMapper.class);

            // Register Filters
            register(ApiLoggingFilter.class);
        }
    }

    public static HttpServer startServer() {
        final SmartCampusApplication rc = new SmartCampusApplication();
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) {
        try {
            final HttpServer server = startServer();
            Logger.getLogger(Main.class.getName()).info("Smart Campus API started at " + BASE_URI + "api/v1\nHit Enter to stop it...");
            System.in.read();
            server.shutdownNow();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
