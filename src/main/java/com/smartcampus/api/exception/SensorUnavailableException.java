package com.smartcampus.api.exception;

/**
 * Thrown when a POST /sensors/{id}/readings is attempted but the sensor's
 * status is "MAINTENANCE".
 *
 * A sensor in MAINTENANCE mode is physically disconnected or undergoing
 * servicing and cannot record new measurements.
 *
 * Mapped to HTTP 403 Forbidden by SensorUnavailableExceptionMapper.
 *
 * WHY 403 AND NOT 400?
 * 400 means the request itself was malformed.
 * 403 means the server understood the request perfectly but is refusing
 * to fulfil it due to a state/permission constraint. The sensor exists
 * and the request is valid — but it's forbidden to post readings right now.
 */
public class SensorUnavailableException extends RuntimeException {

    private final String sensorId;
    private final String currentStatus;

    public SensorUnavailableException(String sensorId, String currentStatus) {
        super("Sensor " + sensorId + " is currently in '" + currentStatus
                + "' state and cannot accept new readings.");
        this.sensorId = sensorId;
        this.currentStatus = currentStatus;
    }

    public String getSensorId() { return sensorId; }
    public String getCurrentStatus() { return currentStatus; }
}
