package com.smartcampus.api.mapper;

import com.smartcampus.api.exception.LinkedResourceNotFoundException;
import com.smartcampus.api.model.ErrorResponse;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Converts LinkedResourceNotFoundException → HTTP 422 Unprocessable Entity.
 *
 * 422 is more semantically correct than 404 here because:
 *   - The URL /api/v1/sensors exists and is valid (no 404)
 *   - The JSON body is syntactically correct (no 400)
 *   - The problem is a broken reference INSIDE the body (422)
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        ErrorResponse body = new ErrorResponse(
                422,
                "Unprocessable Entity",
                ex.getMessage(),
                uriInfo.getAbsolutePath().getPath()
        );

        // 422 doesn't have a named constant in older JAX-RS; use fromStatusCode
        return Response
                .status(422)
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
