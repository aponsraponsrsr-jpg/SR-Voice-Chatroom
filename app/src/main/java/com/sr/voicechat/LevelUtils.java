package com.sr.voicechat;

import android.graphics.Color;

public class LevelUtils {

    // ১. ইউজারের স্পেন্ড করা টোটাল কয়েন থেকে লেভেল হিসাব করার ফাংশন (১ থেকে ১৫০ লেভেল)
    public static int calculateLevel(long totalCoins) {
        if (totalCoins < 3000000L) { // ৩ মিলিয়নের (৩০ লাখ) নিচে Level 1
            return 1;
        }

        long currentRequirement = 3000000L;
        int level = 1;

        for (int i = 2; i <= 150; i++) {
            if (i <= 20) {
                // লেভেল ১ থেকে ২০ পর্যন্ত: প্রতি লেভেলে ৫ মিলিয়ন (৫০ লাখ) করে টার্গেট বাড়বে
                currentRequirement += 5000000L;
            } else if (i <= 50) {
                // লেভেল ২১ থেকে ৫০ পর্যন্ত: প্রতি লেভেলে ১৫ মিলিয়ন (১.৫ কোটি) করে বাড়বে
                currentRequirement += 15000000L;
            } else if (i <= 100) {
                // লেভেল ৫১ থেকে ১০০ পর্যন্ত: প্রতি লেভেলে ২৫ মিলিয়ন (২.৫ কোটি) করে বাড়বে
                currentRequirement += 25000000L;
            } else {
                // লেভেল ১০১ থেকে ১৫০ পর্যন্ত: প্রতি লেভেলে ৫০ মিলিয়ন (৫ কোটি) করে টার্গেট বাড়বে
                currentRequirement += 50000000L;
            }

            if (totalCoins >= currentRequirement) {
                level = i;
            } else {
                break;
            }
        }

        return Math.min(level, 150); // সর্বোচ্চ লেভেল ১৫০
    }

    // ২. লেভেল অনুযায়ী প্রিমিয়াম ব্যাজ ও কালার কোড রিটার্ন করবে
    public static String getLevelBadgeText(int level) {
        return "⭐ Lv." + level;
    }

    public static int getLevelColor(int level) {
        if (level <= 10) {
            return Color.parseColor("#C0C0C0"); // Silver
        } else if (level <= 20) {
            return Color.parseColor("#29B6F6"); // Cyan / Light Blue
        } else if (level <= 50) {
            return Color.parseColor("#AB47BC"); // Royal Purple
        } else if (level <= 100) {
            return Color.parseColor("#FF4081"); // Diamond Pink
        } else {
            return Color.parseColor("#FFD700"); // Glowing Gold (101 - 150)
        }
    }
}
