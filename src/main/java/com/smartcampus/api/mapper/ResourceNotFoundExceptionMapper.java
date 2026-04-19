package com.smartcampus.api.mapper;

import com.smartcampus.api.exception.ResourceNotFoundException;
import com.smartcampus.api.model.ErrorResponse;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Converts ResourceNotFoundException → HTTP 404 Not Found.
 */
@Provider
public class ResourceNotFoundExceptionMapper
        implements ExceptionMapper<ResourceNotFoundException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(ResourceNotFoundException ex) {
        ErrorResponse body = new ErrorResponse(
                404,
                "Not Found",
                ex.getMessage(),
                uriInfo.getAbsolutePath().getPath()
        );

        return Response
                .status(Response.Status.NOT_FOUND)           // 404
                .entity(body)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
