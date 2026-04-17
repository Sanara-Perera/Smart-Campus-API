package com.smartcampus.api.model;

/**
 * Standard error response body returned by all Exception Mappers.
 *
 * Every error in the API — whether 404, 409, 422, 403, or 500 — is returned
 * in this consistent JSON shape so clients always know where to look:
 *
 * {
 *   "status": 409,
 *   "error": "Conflict",
 *   "message": "Room LIB-301 cannot be deleted because it still has 2 sensor(s) assigned.",
 *   "path": "/api/v1/rooms/LIB-301"
 * }
 *
 * Consistent error responses are an important mark of a professional API.
 */
public class ErrorResponse {

    /** The HTTP status code as a number (e.g., 404, 409) */
    private int status;

    /** Short name for the error type (e.g., "Not Found", "Conflict") */
    private String error;

    /** Human-readable explanation of what went wrong */
    private String message;

    /** The URI path that triggered this error */
    private String path;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public ErrorResponse() {}

    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    // -------------------------------------------------------------------------
    // Getters and Setters
    // -------------------------------------------------------------------------

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}
