package com.sr.voicechat;

import java.util.ArrayList;
import java.util.List;

public class VipModel {
    private String vipName;
    private String coinsText;
    private String buyText;
    private String badgeEmoji;
    private String frameName;
    private String bubbleName;
    private String entryEffectName;
    private String tigerType;
    private String tigerEmoji;
    private String validity;
    private String topColor;
    private String bottomColor;

    public VipModel(String vipName, String coinsText, String buyText, String badgeEmoji,
                    String frameName, String bubbleName, String entryEffectName,
                    String tigerType, String tigerEmoji, String validity,
                    String topColor, String bottomColor) {
        this.vipName = vipName;
        this.coinsText = coinsText;
        this.buyText = buyText;
        this.badgeEmoji = badgeEmoji;
        this.frameName = frameName;
        this.bubbleName = bubbleName;
        this.entryEffectName = entryEffectName;
        this.tigerType = tigerType;
        this.tigerEmoji = tigerEmoji;
        this.validity = validity;
        this.topColor = topColor;
        this.bottomColor = bottomColor;
    }

    public String getVipName() { return vipName; }
    public String getVipLevel() { return coinsText; }
    public String getCoinsText() { return coinsText; }
    public String getBuyText() { return buyText; }
    public String getBadgeEmoji() { return badgeEmoji; }
    public String getFrameName() { return frameName; }
    public String getBubbleName() { return bubbleName; }
    public String getEntryEffectName() { return entryEffectName; }
    public String getTigerType() { return tigerType; }
    public String getTigerEmoji() { return tigerEmoji; }
    public String getValidity() { return validity; }
    public String getTopColor() { return topColor; }
    public String getBottomColor() { return bottomColor; }

    // PREMIUM LIST - VIP 1 to VIP 7 - No Old Normal
    public static List<VipModel> createPremiumList() {
        List<VipModel> list = new ArrayList<>();
        list.add(new VipModel("VIP 1", "2,000,000 Coins", "Buy - 2,000,000 Coins (Coming Soon)", "👑", "Black Royal Frame", "Black Bubble", "Black Tiger Entry", "Black Tiger", "🐅", "7 / 15 / 30 Days", "#1A1A2E", "#FFD700"));
        list.add(new VipModel("VIP 2", "5,000,000 Coins", "Buy - 5,000,000 Coins (Coming Soon)", "👑", "White Royal Frame", "White Bubble", "White Tiger Entry", "White Tiger", "🐯", "7 / 15 / 30 Days", "#FFFFFF", "#00BCD4"));
        list.add(new VipModel("VIP 3", "8,000,000 Coins", "Buy - 8,000,000 Coins (Coming Soon)", "👑", "Golden Frame", "Golden Bubble", "Golden Tiger Entry", "Golden Tiger", "🐅", "7 / 15 / 30 Days", "#FFD700", "#FF8C00"));
        list.add(new VipModel("VIP 4", "15,000,000 Coins", "Buy - 15,000,000 Coins (Coming Soon)", "👑", "Fire Frame", "Fire Bubble", "Fire Tiger Entry", "Fire Tiger", "🔥", "7 / 15 / 30 Days", "#FF5722", "#FFC107"));
        list.add(new VipModel("VIP 5", "20,000,000 Coins", "Buy - 20,000,000 Coins (Coming Soon)", "👑", "Royal Purple Frame", "Royal Bubble", "Royal Tiger Entry", "Royal Tiger", "👑", "7 / 15 / 30 Days", "#9C27B0", "#FFD700"));
        list.add(new VipModel("VIP 6", "30,000,000 Coins", "Buy - 30,000,000 Coins (Coming Soon)", "👑", "Diamond Frame", "Diamond Bubble", "Diamond Tiger Entry", "Diamond Tiger", "💎", "7 / 15 / 30 Days", "#00BCD4", "#FFFFFF"));
        list.add(new VipModel("VIP 7", "50,000,000 Coins", "Buy - 50,000,000 Coins (Coming Soon)", "👑", "King Frame", "King Bubble", "King Tiger Entry", "King Tiger", "👑", "7 / 15 / 30 Days", "#FFD700", "#FF4081"));
        return list;
    }
}
