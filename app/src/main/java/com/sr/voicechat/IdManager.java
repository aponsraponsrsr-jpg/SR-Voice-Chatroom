package com.sr.voicechat;

import java.util.Random;

public class IdManager {
    // সর্বনিম্ন ৭ ডিজিট বা তার বেশি থেকে আইডি জেনারেট করার ফাংশন
    public static String generateRandomRoomOrUserId() {
        // ১০,০০,০০০ থেকে ৯৯,৯৯,৯৯৯ পর্যন্ত র্যান্ডম ৭ ডিজিটের আইডি তৈরি করবে
        long minId = 1000000L;
        long maxId = 9999999L;
        long randomValue = minId + (long)(new Random().nextDouble() * (maxId - minId));
        return String.valueOf(randomValue);
    }
}
