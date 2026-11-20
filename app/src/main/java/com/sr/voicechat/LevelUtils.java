package com.sr.voicechat;

import android.graphics.Color;

public class LevelUtils {

    // ===== LEVEL FORMULA - FINAL LOCK - 1 TO 150 - CHANGE HOY NAI =====
    public static int calculateLevel(long totalCoins) {
        if (totalCoins < 500000L) {
            return 1;
        }
        long currentRequirement = 500000L;
        int level = 1;
        for (int i = 2; i <= 150; i++) {
            if (i <= 20) currentRequirement += 10000000L;
            else if (i <= 50) currentRequirement += 25000000L;
            else if (i <= 100) currentRequirement += 50000000L;
            else currentRequirement += 100000000L;
            if (totalCoins >= currentRequirement) level = i;
            else break;
        }
        return Math.min(level, 150);
    }

    public static long getRequirementForLevel(int level) {
        if (level <= 1) return 500000L;
        if (level > 150) level = 150;
        long req = 500000L;
        for (int i = 2; i <= level; i++) {
            if (i <= 20) req += 10000000L;
            else if (i <= 50) req += 25000000L;
            else if (i <= 100) req += 50000000L;
            else req += 100000000L;
        }
        return req;
    }

    public static long getCoinsToNextLevel(int level, long totalCoins) {
        if (level >= 150) return 0;
        long next = getRequirementForLevel(level + 1);
        long left = next - totalCoins;
        return left < 0 ? 0 : left;
    }

    public static int getLevelProgressPercent(int level, long totalCoins) {
        if (level >= 150) return 100;
        long cur = getRequirementForLevel(level);
        long nxt = getRequirementForLevel(level + 1);
        if (nxt <= cur) return 100;
        long done = totalCoins - cur;
        if (done < 0) done = 0;
        int pct = (int) (done * 100 / (nxt - cur));
        if (pct < 0) pct = 0;
        if (pct > 100) pct = 100;
        return pct;
    }

    public static String getLevelBadgeText(int level) {
        return "⭐ Lv." + level;
    }

    // ===== NEW TITLE - COLOR ER SATHE MILAY =====
    public static String getLevelTitle(int level) {
        if (level <= 10) return "Silver Star";
        if (level <= 20) return "Blue Star";
        if (level <= 30) return "Purple Dream";
        if (level <= 40) return "Emerald King";
        if (level <= 50) return "Fire Star";
        if (level <= 60) return "Royal Mix";
        if (level <= 70) return "Ocean King";
        if (level <= 80) return "Purple Emperor";
        if (level <= 90) return "Golden King";
        if (level <= 100) return "Golden Coffee King";
        if (level <= 110) return "Pink Queen";
        if (level <= 120) return "Coffee Orange King";
        if (level <= 130) return "Purple Pink Dream";
        if (level <= 140) return "Chocolate Gold King";
        return "Legend 150 👑";
    }

    // ===== NEW GORGEOUS COLOR - TOMAR AGER 3 TA COLOR BAD =====
    // Single color - Text / Dot er jonno
    public static int getLevelColor(int level) {
        int[] c = getLevelColors(level);
        if (c.length >= 2) return c[1];
        return c[0];
    }

    // Gradient - 3 ta color - Badge / Card / Background e Wow lagbe
    public static int[] getLevelColors(int level) {
        if (level <= 10) {
            // 1-10 Silver - Chokchoke Silver
            return new int[]{Color.parseColor("#F5F5F5"), Color.parseColor("#C0C0C0"), Color.parseColor("#9E9E9E")};
        }
        if (level <= 20) {
            // 11-20 Blue Paste / Cyan - Thanda Blue
            return new int[]{Color.parseColor("#80DEEA"), Color.parseColor("#00BCD4"), Color.parseColor("#0097A7")};
        }
        if (level <= 30) {
            // 21-30 Light Purple - Halka Beguni
            return new int[]{Color.parseColor("#E1BEE7"), Color.parseColor("#CE93D8"), Color.parseColor("#AB47BC")};
        }
        if (level <= 40) {
            // 31-40 Emerald Green - Natural Green
            return new int[]{Color.parseColor("#A5D6A7"), Color.parseColor("#00C853"), Color.parseColor("#00796B")};
        }
        if (level <= 50) {
            // 41-50 Orange - Komola
            return new int[]{Color.parseColor("#FFCC80"), Color.parseColor("#FF9800"), Color.parseColor("#F57C00")};
        }
        if (level <= 60) {
            // 51-60 Purple + Orange Mix - Royal Mix
            return new int[]{Color.parseColor("#BA68C8"), Color.parseColor("#FF7043"), Color.parseColor("#FFA726")};
        }
        if (level <= 70) {
            // 61-70 Blue + Purple Mix - Ocean King
            return new int[]{Color.parseColor("#64B5F6"), Color.parseColor("#7E57C2"), Color.parseColor("#5C6BC0")};
        }
        if (level <= 80) {
            // 71-80 Full Purple - Deep Purple
            return new int[]{Color.parseColor("#CE93D8"), Color.parseColor("#9C27B0"), Color.parseColor("#4A148C")};
        }
        if (level <= 90) {
            // 81-90 Golden - Pure Gold
            return new int[]{Color.parseColor("#FFE082"), Color.parseColor("#FFD700"), Color.parseColor("#FF8F00")};
        }
        if (level <= 100) {
            // 91-100 Golden + Coffee / Chocolate Mix
            return new int[]{Color.parseColor("#FFD700"), Color.parseColor("#A1887F"), Color.parseColor("#6D4C41")};
        }
        if (level <= 110) {
            // 101-110 Natural Pink - Misti Pink
            return new int[]{Color.parseColor("#F8BBD0"), Color.parseColor("#F06292"), Color.parseColor("#EC407A")};
        }
        if (level <= 120) {
            // 111-120 Coffee to Orange Mix
            return new int[]{Color.parseColor("#BCAAA4"), Color.parseColor("#8D6E63"), Color.parseColor("#FF7043")};
        }
        if (level <= 130) {
            // 121-130 Purple to Pink Mix
            return new int[]{Color.parseColor("#BA68C8"), Color.parseColor("#E040FB"), Color.parseColor("#F06292")};
        }
        if (level <= 140) {
            // 131-140 Chocolate + Gold
            return new int[]{Color.parseColor("#D7CCC8"), Color.parseColor("#6D4C41"), Color.parseColor("#FFD700")};
        }
        // 141-150 Legend - Gold + Pink + Purple - Sobcheye Gorgeous
        return new int[]{Color.parseColor("#FFEA00"), Color.parseColor("#FF4081"), Color.parseColor("#7C4DFF")};
    }

    public static String getLevelTargetCoinsFormatted(int level) {
        long coins = getRequirementForLevel(level);
        return formatCoins(coins);
    }

    public static String formatCoins(long coins) {
        if (coins >= 1000000000L) {
            double v = coins / 1000000000.0;
            return (v == (long) v ? String.valueOf((long) v) : String.valueOf(v)) + "B Coins";
        }
        if (coins >= 1000000L) {
            double v = coins / 1000000.0;
            return (v == (long) v ? String.valueOf((long) v) : String.valueOf(v)) + "M Coins";
        }
        if (coins >= 1000L) {
            return (coins / 1000L) + "K Coins";
        }
        return coins + " Coins";
    }

    public static int calculateWealthLevel(long totalSending) {
        return calculateLevel(totalSending);
    }

    public static int calculateCharmLevel(long totalReceiving) {
        return calculateLevel(totalReceiving);
    }
}
