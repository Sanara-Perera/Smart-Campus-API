package com.smartcampus.api.mapper;

import com.smartcampus.api.model.ErrorResponse;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global "safety net" exception mapper — catches ANY unhandled exception.
 *
 * This is the last line of defence. If an unexpected error occurs
 * (NullPointerException, IndexOutOfBoundsException, etc.) and no other
 * ExceptionMapper handles it, this mapper intercepts it and returns a
 * clean HTTP 500 Internal Server Error in JSON format.
 *
 * CRITICAL SECURITY NOTE:
 * We log the full stack trace SERVER-SIDE (so developers can debug it)
 * but we NEVER send the stack trace to the client. Exposing stack traces
 * to external consumers is a serious security vulnerability because it
 * reveals: package names, class names, library versions, internal logic
 * flow, file paths — all of which help an attacker plan targeted exploits.
 *
 * The client only receives a generic "something went wrong" message.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable ex) {
        // Log the full details server-side so developers can investigate
        LOGGER.log(Level.SEVERE,
                "Unhandled exception on path: " + (uriInfo != null ? uriInfo.getAbsolutePath() : "unknown"),
                ex);

        // Return a generic, safe message to the client — NO stack trace, NO internal details
        ErrorResponse body = new ErrorResponse(
                500,
                "Internal Server Error",
                "An unexpected error occurred. Please contact the system administrator.",
                uriInfo != null ? uriInfo.getAbsolutePath().getPath() : "/api/v1"
        );

        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)  // 500
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
