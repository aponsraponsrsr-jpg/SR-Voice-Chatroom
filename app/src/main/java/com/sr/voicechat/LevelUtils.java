package com.sr.voicechat;

import android.graphics.Color;

public class LevelUtils {

    // ১. ইউজারের সেন্ডিং কয়েন থেকে লেভেল হিসাব করার ফাংশন (১ থেকে ১৫০ লেভেল)
    // এখানে কয়েনের টার্গেটগুলো অনেক বাড়িয়ে দেওয়া হয়েছে, যাতে প্রচুর সেন্ডিং করে লেভেল আপ করতে হয়।
    public static int calculateLevel(long totalCoins) {
        if (totalCoins < 500000L) { // ৫ লাখ কয়েনের নিচে Level 1
            return 1;
        }

        long currentRequirement = 500000L;
        int level = 1;

        for (int i = 2; i <= 150; i++) {
            if (i <= 20) {
                // লেভেল ১ থেকে ২০ পর্যন্ত: প্রতি লেভেলে ১০ মিলিয়ন (১ কোটি) করে টার্গেট বাড়বে
                currentRequirement += 10000000L;
            } else if (i <= 50) {
                // লেভেল ২১ থেকে ৫০ পর্যন্ত: প্রতি লেভেলে ২৫ মিলিয়ন (২.৫ কোটি) করে বাড়বে
                currentRequirement += 25000000L;
            } else if (i <= 100) {
                // লেভেল ৫১ থেকে ১০০ পর্যন্ত: প্রতি লেভেলে ৫০ মিলিয়ন (৫ কোটি) করে বাড়বে
                currentRequirement += 50000000L;
            } else {
                // লেভেল ১০শিল্প থেকে ১৫০ পর্যন্ত: প্রতি লেভেলে ১০০ মিলিয়ন (১০ কোটি) করে টার্গেট বাড়বে
                currentRequirement += 100000000L;
            }

            if (totalCoins >= currentRequirement) {
                level = i;
            } else {
                break;
            }
        }

        return Math.min(level, 150); // সর্বোচ্চ লেভেল ১৫০
    }

    // ২. লেভেল অনুযায়ী প্রিমিয়াম ব্যাজ টেক্সট রিটার্ন করবে
    public static String getLevelBadgeText(int level) {
        return "⭐ Lv." + level;
    }

    // ৩. লেভেল অনুযায়ী প্রিমিয়াম কালার কোড রিটার্ন করবে (ভিন্ন ভিন্ন টায়ারের জন্য)
    public static int getLevelColor(int level) {
        if (level <= 10) {
            return Color.parseColor("#C0C0C0"); // Silver (Lv. 1 - 10)
        } else if (level <= 30) {
            return Color.parseColor("#29B6F6"); // Cyan / Light Blue (Lv. 11 - 30)
        } else if (level <= 60) {
            return Color.parseColor("#AB47BC"); // Royal Purple (Lv. 31 - 60)
        } else if (level <= 100) {
            return Color.parseColor("#FF4081"); // Diamond Pink (Lv. 61 - 100)
        } else {
            return Color.parseColor("#FFD700"); // Glowing Gold (Lv. 101 - 150)
        }
    }

    // ৪. লেভেল সেন্টারের জন্য প্রতিটি লেভেলের কয়েন টার্গেট ফরম্যাট করার ফাংশন
    public static String getLevelTargetCoinsFormatted(int level) {
        long coins;
        if (level <= 10) coins = level * 500000L;
        else if (level <= 30) coins = level * 2000000L;
        else if (level <= 60) coins = level * 10000000L;
        else if (level <= 100) coins = level * 25000000L;
        else coins = level * 50000000L;

        if (coins >= 10000000L) {
            return (coins / 1000000L) + "M Coins";
        } else if (coins >= 1000L) {
            return (coins / 1000L) + "K Coins";
        }
        return coins + " Coins";
    }
}
