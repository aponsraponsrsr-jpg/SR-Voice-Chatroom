package com.sr.voicechat;

public class UserModel {
    public String firebaseUid = "";
    public String my9DigitId = "";
    public String name = "User";
    public String gender = "Male"; // Male / Female - LOCKED, kokhono change hobe na
    public String country = "Bangladesh"; // LOCKED
    public String birthday = ""; // LOCKED
    public String photoBase64 = "";
    public String level = "1";
    public long coins = 0;
    public String giftCount = "0";
    public String followCount = "0";
    public String fansCount = "0";
    public String visitorCount = "0";
    public String familyName = ""; // Khali thakle = Not joined the family
    public String medalCount = "0";
    public String supporterName = "";
    public String headwearCount = "0";
    public String carCount = "0";
    public String agency = ""; // Agency / Host
    public boolean speaking = false;
    public boolean isOnline = false;

    public UserModel() {}

    public UserModel(String name, boolean speaking) {
        this.name = name;
        this.speaking = speaking;
    }

    public String getName() { return name; }
    public boolean isSpeaking() { return speaking; }
    public boolean isMale() { return gender != null && gender.equalsIgnoreCase("Male"); }

    // Flag - Country onujayi - Permanent
    public String getFlag() {
        if (country == null) return "🌍";
        String c = country.toLowerCase();
        if (c.contains("bangladesh") || c.contains("bengal")) return "🇧🇩";
        if (c.contains("india")) return "🇮🇳";
        if (c.contains("pakistan")) return "🇵🇰";
        if (c.contains("nepal")) return "🇳🇵";
        if (c.contains("sri lanka")) return "🇱🇰";
        if (c.contains("indonesia")) return "🇮🇩";
        if (c.contains("philippines")) return "🇵🇭";
        if (c.contains("saudi")) return "🇸🇦";
        if (c.contains("uae") || c.contains("emirates")) return "🇦🇪";
        if (c.contains("usa") || c.contains("america")) return "🇺🇸";
        if (c.contains("uk") || c.contains("england")) return "🇬🇧";
        if (c.contains("turkey")) return "🇹🇷";
        if (c.contains("egypt")) return "🇪🇬";
        return "🌍";
    }

    // Gender Icon - GOL GOL NA - Male ♂️ Blue / Female ♀️ Pink - Permanent
    public String getGenderIcon() {
        return isMale() ? "♂️" : "♀️";
    }

    public String getFamilyDisplay() {
        if (familyName == null || familyName.isEmpty()) return "Not joined the family";
        return familyName;
    }
}
