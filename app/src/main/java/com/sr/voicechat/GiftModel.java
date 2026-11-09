package com.sr.voicechat.models;

public class GiftModel {
    private String giftId;
    private String giftName;
    private String giftPrice;
    private int giftImageRes;
    private String giftCategory;

    public GiftModel(String giftId, String giftName, String giftPrice, int giftImageRes, String giftCategory) {
        this.giftId = giftId;
        this.giftName = giftName;
        this.giftPrice = giftPrice;
        this.giftImageRes = giftImageRes;
        this.giftCategory = giftCategory;
    }

    public String getGiftId() { return giftId; }
    public String getGiftName() { return giftName; }
    public String getGiftPrice() { return giftPrice; }
    public int getGiftImageRes() { return giftImageRes; }
    public String getGiftCategory() { return giftCategory; }
}
