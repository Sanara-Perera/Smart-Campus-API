package com.smartcampus.api;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Application entry point.
 *
 * Starts an embedded Grizzly HTTP server hosting the Smart Campus JAX-RS API.
 * No external server (Tomcat, WildFly) required — just run this class.
 *
 * Base URI: http://localhost:8080/api/v1
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /** The full base URI where the API will be accessible */
    public static final String BASE_URI = "http://0.0.0.0:8080/api/v1/";

    public static void main(String[] args) throws IOException {

        // Build the URI object from our base URI string
        final URI uri = URI.create(BASE_URI);

        // Load our JAX-RS application configuration
        final ResourceConfig config = new SmartCampusApplication();

        // Start the Grizzly HTTP server with our config
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);

        LOGGER.info("==========================================================");
        LOGGER.info("  Smart Campus API is running!");
        LOGGER.info("  Base URL  : http://localhost:8080/api/v1");
        LOGGER.info("  Discovery : http://localhost:8080/api/v1/");
        LOGGER.info("  Rooms     : http://localhost:8080/api/v1/rooms");
        LOGGER.info("  Sensors   : http://localhost:8080/api/v1/sensors");
        LOGGER.info("  Press ENTER to stop the server...");
        LOGGER.info("==========================================================");

        // Keep the server running until user presses ENTER
        System.in.read();

        // Graceful shutdown
        server.shutdownNow();
        LOGGER.info("Server stopped.");
    }
}
