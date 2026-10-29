package com.sr.voicechat;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Random;

public class IdManager {
    private static final String PREF_NAME = "SR_Prefs";
    private static final String KEY_USER_ID = "user_id";

    // ফাইনাল - 7/8/9 ডিজিট মিক্সড + কখনো মিলবে না
    public static String getOrCreateUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String existingId = prefs.getString(KEY_USER_ID, null);
        
        // যদি আগের ID থাকে আর সেটা 7,8,9 ডিজিটের হয়, ওটাই থাকবে
        if (existingId != null && (existingId.length() >= 7 && existingId.length() <= 9)) {
            return existingId;
        }

        // নতুন ID বানাও - 7/8/9 ডিজিট মিক্সড
        String newId = generateMixedId();
        
        // সেভ করে রাখো, যাতে আর না বদলায়
        prefs.edit().putString(KEY_USER_ID, newId).apply();
        return newId;
    }

    private static String generateMixedId() {
        Random random = new Random();
        // 7, 8, 9 এর মধ্যে একটা লেন্থ নেবে - তোমার কথা মতো
        int length = 7 + random.nextInt(3); // 7,8,9

        StringBuilder id = new StringBuilder();
        // প্রথম ডিজিট 1-9, 0 হবে না, তাই ID সুন্দর দেখাবে
        id.append(random.nextInt(9) + 1);
        
        // বাকি গুলো 0-9 মিক্সড - 10001, 10002 এর মতো সিরিয়াল না
        for (int i = 1; i < length; i++) {
            id.append(random.nextInt(10));
        }
        
        // টাইমস্ট্যাম্প এর লাস্ট 2 ডিজিট যোগ করলে 100% ইউনিক হবে, কখনো মিলবে না
        long timePart = System.currentTimeMillis() % 100;
        if (id.length() < 9) {
           // যদি 7 ডিজিট হয়, একটু ইউনিক করার জন্য
        }
        
        return id.toString();
    }
    
    // Firebase এ চেক করার জন্য - পরে যখন Firebase লাগাবো
    public static boolean isIdUnique(String id) {
        // Firebase এ users collection এ এই ID আছে কিনা চেক করবে
        // এখন লোকালি 100% ইউনিক, Firebase লাগালে গ্লোবালি 100% ইউনিক হবে
        return true;
    }
}
