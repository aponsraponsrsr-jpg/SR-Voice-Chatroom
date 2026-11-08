package com.sr.voicechat;

public class VipModel {
    private String vipName;
    private String vipLevel;
    private int vipBadgeRes;
    private int vipFrameRes;
    private int chatBubbleRes;
    private String entryEffectName;

    public VipModel(String vipName, String vipLevel, int vipBadgeRes, int vipFrameRes, int chatBubbleRes, String entryEffectName) {
        this.vipName = vipName;
        this.vipLevel = vipLevel;
        this.vipBadgeRes = vipBadgeRes;
        this.vipFrameRes = vipFrameRes;
        this.chatBubbleRes = chatBubbleRes;
        this.entryEffectName = entryEffectName;
    }

    public String getVipName() { return vipName; }
    public String getVipLevel() { return vipLevel; }
    public int getVipBadgeRes() { return vipBadgeRes; }
    public int getVipFrameRes() { return vipFrameRes; }
    public int getChatBubbleRes() { return chatBubbleRes; }
    public String getEntryEffectName() { return entryEffectName; }
}
