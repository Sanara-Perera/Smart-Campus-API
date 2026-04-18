package com.smartcampus.api.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * API Observability Filter — logs every incoming request and every outgoing response.
 *
 * WHY USE A FILTER INSTEAD OF MANUAL Logger.info() IN EVERY METHOD?
 *
 * Logging is a "cross-cutting concern" — something that applies uniformly to
 * every endpoint regardless of what the endpoint does. If we added a Logger.info()
 * call manually to every resource method, we would:
 *   1. Repeat the same boilerplate in dozens of places (violates DRY principle)
 *   2. Risk forgetting to add it to a new method
 *   3. Mix infrastructure concerns (logging) with business logic (room management)
 *   4. Have no single place to change the log format
 *
 * A JAX-RS filter is automatically applied to EVERY request/response by the
 * framework — one class, zero duplication, clean separation of concerns.
 * Enabling or disabling logging means changing one class, not hunting through
 * every resource method.
 *
 * This class implements BOTH filter interfaces so it handles the full
 * request → processing → response lifecycle:
 *
 *   [Client] → ContainerRequestFilter.filter()
 *           → Resource Method executes
 *           → ContainerResponseFilter.filter()
 *           → [Client]
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Called BEFORE the request reaches the resource method.
     * Logs the HTTP method (GET, POST, etc.) and the full URI.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String uri    = requestContext.getUriInfo().getAbsolutePath().toString();

        LOGGER.info(String.format("[REQUEST]  --> %s %s", method, uri));
    }

    /**
     * Called AFTER the resource method has produced a response.
     * Logs the final HTTP status code that is being sent back to the client.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        String method     = requestContext.getMethod();
        String uri        = requestContext.getUriInfo().getAbsolutePath().toString();
        int    statusCode = responseContext.getStatus();

        LOGGER.info(String.format("[RESPONSE] <-- %s %s | Status: %d", method, uri, statusCode));
    }
}
