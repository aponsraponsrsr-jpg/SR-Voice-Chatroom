package com.sr.voicechat;

public class RoomModel {
    private String roomId;
    private String roomName;
    private String hostId;
    private long createdAt;
    private int participantCount;

    public RoomModel() {
        // Required for Firebase
    }

    public RoomModel(String roomId, String roomName, String hostId) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.hostId = hostId;
        this.createdAt = System.currentTimeMillis();
        this.participantCount = 0;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }
}
