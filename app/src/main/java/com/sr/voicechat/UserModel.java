package com.sr.voicechat;

public class UserModel {
    String name;
    boolean speaking;
    public UserModel(String name, boolean speaking) {
        this.name = name;
        this.speaking = speaking;
    }
    public String getName() { return name; }
    public boolean isSpeaking() { return speaking; }
}
