package com.smartcampus.api.mapper;

import com.smartcampus.api.exception.RoomNotEmptyException;
import com.smartcampus.api.model.ErrorResponse;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Converts RoomNotEmptyException → HTTP 409 Conflict JSON response.
 *
 * @Provider tells JAX-RS to register this class as an exception handler.
 * JAX-RS will automatically call toResponse() whenever a RoomNotEmptyException
 * is thrown anywhere in our resource classes.
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        ErrorResponse body = new ErrorResponse(
                409,
                "Conflict",
                ex.getMessage(),
                uriInfo.getAbsolutePath().getPath()
        );

        return Response
                .status(Response.Status.CONFLICT)            // 409
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
