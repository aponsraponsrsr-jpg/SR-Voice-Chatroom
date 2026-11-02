package com.sr.voicechat;

public class RoomModel {
    String name, desc;
    int users;
    public RoomModel(String name, String desc, int users) {
        this.name = name; this.desc = desc; this.users = users;
    }
    public String getName() { return name; }
    public String getDesc() { return desc; }
    public int getUsers() { return users; }
}
