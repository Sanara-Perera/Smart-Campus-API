package com.smartcampus.api.exception;

/**
 * Thrown when a DELETE /rooms/{id} is attempted but the room still has sensors.
 *
 * Business rule: a room cannot be deleted while sensors are deployed in it,
 * as that would create orphaned sensor records with no parent room.
 *
 * Mapped to HTTP 409 Conflict by RoomNotEmptyExceptionMapper.
 */
public class RoomNotEmptyException extends RuntimeException {

    private final String roomId;
    private final int sensorCount;

    public RoomNotEmptyException(String roomId, int sensorCount) {
        super("Room " + roomId + " cannot be deleted because it still has "
                + sensorCount + " sensor(s) assigned to it.");
        this.roomId = roomId;
        this.sensorCount = sensorCount;
    }

    public String getRoomId() { return roomId; }
    public int getSensorCount() { return sensorCount; }
}
