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

    // VIP Number - 1 to 8
    public int getVipNumber() {
        try {
            return Integer.parseInt(vipName.replaceAll("[^0-9]", ""));
        } catch (Exception e) { return 1; }
    }

    // PREMIUM STAGE COLOR - Rooh Style Curtain
    public String getStageColor() { return topColor; }

    // PREMIUM LIST - VIP 1 to VIP 8 - OLD NORMAL OUT, PREMIUM IN
    public static List<VipModel> createPremiumList() {
        List<VipModel> list = new ArrayList<>();
        // VIP 1 - White Lion - White + Gold Frame
        list.add(new VipModel("VIP 1", "2,000,000 Coins", "Buy - 2,000,000 Coins", "👑", "White Lion Frame", "White Lion Bubble", "White Lion Entry", "White Lion", "🦁", "7 / 15 / 30 Days", "#FFFFFF", "#FFD700"));
        // VIP 2 - Black Lion - Black + Gold
        list.add(new VipModel("VIP 2", "5,000,000 Coins", "Buy - 5,000,000 Coins", "👑", "Black Lion Frame", "Black Lion Bubble", "Black Lion Entry", "Black Lion", "🦁", "7 / 15 / 30 Days", "#1A1A2E", "#FFD700"));
        // VIP 3 - Golden Tiger - Gold
        list.add(new VipModel("VIP 3", "8,000,000 Coins", "Buy - 8,000,000 Coins", "👑", "Golden Tiger Frame", "Golden Bubble", "Golden Tiger Entry", "Golden Tiger", "🐅", "7 / 15 / 30 Days", "#FFD700", "#FF8C00"));
        // VIP 4 - Fire Tiger - Orange Fire
        list.add(new VipModel("VIP 4", "15,000,000 Coins", "Buy - 15,000,000 Coins", "👑", "Fire Tiger Frame", "Fire Bubble", "Fire Tiger Entry", "Fire Tiger", "🔥", "7 / 15 / 30 Days", "#FF5722", "#FFC107"));
        // VIP 5 - Black Eagle - Black + Gold - তোমার দেওয়া Frame
        list.add(new VipModel("VIP 5", "20,000,000 Coins", "Buy - 20,000,000 Coins", "👑", "Black Eagle Frame", "Black Gold Bubble", "Black Eagle Entry", "Black Eagle", "🦅", "7 / 15 / 30 Days", "#000000", "#FFD700"));
        // VIP 6 - Pink Diamond - Pink + Violet - তোমার দেওয়া Frame
        list.add(new VipModel("VIP 6", "30,000,000 Coins", "Buy - 30,000,000 Coins", "👑", "Pink Diamond Frame", "Pink Bubble", "Pink Diamond Entry", "Pink Diamond", "💎", "7 / 15 / 30 Days", "#FF4081", "#9C27B0"));
        // VIP 7 - Fire Lion King - Fire Gold - তোমার দেওয়া Frame - সবচেয়ে Premium
        list.add(new VipModel("VIP 7", "50,000,000 Coins", "Buy - 50,000,000 Coins", "👑", "Fire Lion King Frame", "Fire Gold Bubble", "Fire Lion Entry", "Fire Lion", "🔥🦁", "7 / 15 / 30 Days", "#FF5722", "#FFD700"));
        // VIP 8 - Purple Dragon - NEW - TOP 3 Lion Style
        list.add(new VipModel("VIP 8", "80,000,000 Coins", "Buy - 80,000,000 Coins", "👑", "Purple Dragon Frame", "Purple Dragon Bubble", "Purple Dragon Entry", "Purple Dragon", "🐉", "7 / 15 / 30 Days", "#6A1B9A", "#FFD700"));
        return list;
    }
}
