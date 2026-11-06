package com.sr.voicechat.models; // তোমার প্রজেক্টের সঠিক প্যাকেজ নামটি এখানে দিয়ে দিও

public class GiftModel {
    private String giftName;
    private String giftPrice;
    private int giftIconResId;
    private String category; // "gift", "cp", "lucky"

    public GiftModel(String giftName, String giftPrice, int giftIconResId, String category) {
        this.giftName = giftName;
        this.giftPrice = giftPrice;
        this.giftIconResId = giftIconResId;
        this.category = category;
    }

    public String getGiftName() { return giftName; }
    public String getGiftPrice() { return giftPrice; }
    public int getGiftIconResId() { return giftIconResId; }
    public String getCategory() { return category; }
}
