package com.sr.voicechat.models;

import java.util.ArrayList;
import java.util.List;

public class GiftModel {
    private String giftId;
    private String giftName;
    private String giftPrice;
    private int giftImageRes;
    private String giftCategory;
    private String svgaUrl;
    private boolean isPremium;

    // OLD Constructor - আগের Code যাতে না ভাঙে - এইটা রাখলাম
    public GiftModel(String giftId, String giftName, String giftPrice, int giftImageRes, String giftCategory) {
        this.giftId = giftId;
        this.giftName = giftName;
        this.giftPrice = giftPrice;
        this.giftImageRes = giftImageRes;
        this.giftCategory = giftCategory;
        this.svgaUrl = "";
        this.isPremium = false;
    }

    // NEW Constructor - Premium SVGA Gift এর জন্য
    public GiftModel(String giftId, String giftName, String giftPrice, int giftImageRes, String giftCategory, String svgaUrl, boolean isPremium) {
        this.giftId = giftId;
        this.giftName = giftName;
        this.giftPrice = giftPrice;
        this.giftImageRes = giftImageRes;
        this.giftCategory = giftCategory;
        this.svgaUrl = svgaUrl;
        this.isPremium = isPremium;
    }

    public String getGiftId() { return giftId; }
    public String getGiftName() { return giftName; }
    public String getGiftPrice() { return giftPrice; }
    public int getGiftImageRes() { return giftImageRes; }
    public String getGiftCategory() { return giftCategory; }
    public String getSvgaUrl() { return svgaUrl; }
    public boolean isPremium() { return isPremium; }

    // PREMIUM GIFT LIST - 4 GIFTS - Gift Box এ দেখা যাবে
    public static List<GiftModel> createPremiumList() {
        List<GiftModel> list = new ArrayList<>();
        // 1. Rose - 1,000 Coins
        list.add(new GiftModel("gift_rose", "Rose", "1000", 0, "Premium",
            "https://raw.githubusercontent.com/Arifur-Rahaman-Arif/SR-Voice-Chatroom/main/rose.svga", true));
        // 2. Angel - 5,000 Coins
        list.add(new GiftModel("gift_angel", "Angel", "5000", 0, "Premium",
            "https://raw.githubusercontent.com/Arifur-Rahaman-Arif/SR-Voice-Chatroom/main/angel-1.svga", true));
        // 3. Rocket - 10,000 Coins
        list.add(new GiftModel("gift_rocket", "Rocket", "10000", 0, "Premium",
            "https://raw.githubusercontent.com/Arifur-Rahaman-Arif/SR-Voice-Chatroom/main/Rocket.svga", true));
        // 4. Porsche Car - 20,000 Coins
        list.add(new GiftModel("gift_car", "Porsche Car", "20000", 0, "Premium",
            "https://raw.githubusercontent.com/Arifur-Rahaman-Arif/SR-Voice-Chatroom/main/posche.svga", true));
        return list;
    }
}
