package com.smartcampus.api.exception;

/**
 * Thrown when a POST /sensors body references a roomId that does not exist.
 *
 * The request itself is structurally valid JSON, but it contains a reference
 * to a resource (a Room) that cannot be found. This is a semantic/business
 * logic error, not a "resource not found" URL error.
 *
 * Mapped to HTTP 422 Unprocessable Entity by LinkedResourceNotFoundExceptionMapper.
 *
 * WHY 422 AND NOT 404?
 * 404 means "the URL you requested does not exist."
 * 422 means "your request is syntactically valid but semantically broken."
 * The URL /api/v1/sensors is perfectly valid — the problem is inside the
 * JSON body (the roomId field points to a room that doesn't exist).
 * HTTP 422 is the correct semantic choice.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;

    public LinkedResourceNotFoundException(String resourceType, String resourceId) {
        super("Cannot create sensor: the referenced " + resourceType
                + " with id '" + resourceId + "' does not exist in the system.");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
}
