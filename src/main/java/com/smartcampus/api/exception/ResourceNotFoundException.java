package com.smartcampus.api.exception;

/**
 * Thrown when a GET or DELETE targets a specific resource by ID that doesn't exist.
 *
 * Examples:
 *   GET  /api/v1/rooms/FAKE-999   → room not found
 *   GET  /api/v1/sensors/FAKE-001 → sensor not found
 *
 * Mapped to HTTP 404 Not Found by ResourceNotFoundExceptionMapper.
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;
    private final String resourceId;

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " with id '" + resourceId + "' was not found.");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
}
