package com.smartcampus.api.mapper;

import com.smartcampus.api.exception.SensorUnavailableException;
import com.smartcampus.api.model.ErrorResponse;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Converts SensorUnavailableException → HTTP 403 Forbidden.
 *
 * The server understood the request but is refusing to process it
 * because the sensor's current state (MAINTENANCE) does not allow
 * new readings to be recorded.
 */
@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(SensorUnavailableException ex) {
        ErrorResponse body = new ErrorResponse(
                403,
                "Forbidden",
                ex.getMessage(),
                uriInfo.getAbsolutePath().getPath()
        );

        return Response
                .status(Response.Status.FORBIDDEN)           // 403
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
