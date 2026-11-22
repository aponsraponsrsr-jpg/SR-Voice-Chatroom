package com.sr.voicechat;

public class SalaryPolicy {
    // SR VOICE CHAT ROOM - OFFICIAL HOST SALARY POLICY 1-22

    public static final int[] LEVELS = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22};

    public static final long[] TARGET_COINS = {
        288000L, 588000L, 1190000L, 1780000L, 3280000L, 5780000L, 7880000L,
        9980000L, 13850000L, 18850000L, 26500000L, 34500000L, 43500000L,
        56500000L, 76500000L, 88500000L, 115500000L, 155000000L, 210000000L,
        288000000L, 350000000L, 490000000L
    };

    // Host pabe - Host sudhu eitai dekhbe - Host Total = Eitai
    public static final double[] ANCHOR_SALARY = {
        1, 2, 4.5, 7, 14, 24, 32, 40, 56, 76, 104, 132, 168, 220, 292, 348, 440, 584, 752, 1024, 1240, 1600
    };

    // Agency pabe - Host dekhbe na, sudhu Agency dekhbe - AgencyPolicy te use hobe
    public static final double[] AGENCY_COMMISSION = {
        0.5, 1, 1.5, 2, 3, 6, 8, 10, 14, 19, 26, 33, 42, 55, 73, 87, 110, 146, 188, 256, 310, 400
    };

    // Total - Agency er jonno - Host + Agency - Host Page e eita use hobe NA
    public static final double[] TOTAL_SALARY = {
        1.5, 3, 6, 9, 17, 30, 40, 50, 70, 95, 130, 165, 210, 275, 365, 435, 550, 730, 940, 1280, 1550, 2000
    };

    public static String money(double d) {
        if (d == (long) d) return "$" + (long) d;
        return "$" + d;
    }

    // =====================================================
    // ORIGINAL ID vs DISPLAY ID - GOLDEN RULE - GLOBAL - ALL USERS
    // ORIGINAL_PROFILE_ID = First Saved ID - Gmail Login er somoy je ID paise - 9 Digit
    // Eita Firebase e thakbe - UID er sathe jukto thakbe - Counting A to Z eita diyei cholbe
    // DISPLAY_PROFILE_ID = Future Official Short ID - 1 / 2 / 3 / 5 Digit - Sudhu Show / Beauty er jonno
    // Display Change holeo Original Change hobe na - Counting Change hobe na
    // =====================================================
    public static String getOriginalId(android.content.SharedPreferences prefs) {
        String orig = prefs.getString("ORIGINAL_PROFILE_ID", "");
        if (orig != null && !orig.isEmpty()) return orig;
        // First time - ekhonkar Profile ID tai Original hoye jabe - ekbar save hole ar change hobe na
        String cur = prefs.getString("MY_PROFILE_ID", "");
        if (cur == null || cur.isEmpty()) cur = prefs.getString("MY_9_DIGIT_ID", "");
        if (cur == null) cur = "";
        if (!cur.isEmpty()) prefs.edit().putString("ORIGINAL_PROFILE_ID", cur).apply();
        return cur;
    }
    public static String getDisplayId(android.content.SharedPreferences prefs) {
        String disp = prefs.getString("DISPLAY_PROFILE_ID", "");
        if (disp != null && !disp.isEmpty()) return disp;
        // Display na thakle Original tai dekhabe
        return getOriginalId(prefs);
    }
    // Counting er jonno sob somoy Original ID use korbo - Display ID kokhono na
    public static String getCountingId(android.content.SharedPreferences prefs) {
        return getOriginalId(prefs);
    }

    // =====================================================
    // MONTHLY RESET - Month End = Data ZERO
    // Firebase Path: users/{OriginalID}/monthly/{2026-11}/receivedCoins
    // Month Change holei Key Change hobe: 2026-11 -> 2026-12
    // Notun Key te kono Data nai = Realtime theke Automatic 0 dekhabe
    // Puran maser Data Delete hobe na - History hisebe theke jabe
    // =====================================================
    public static String getCurrentMonthKey() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int y = c.get(java.util.Calendar.YEAR);
        int m = c.get(java.util.Calendar.MONTH) + 1;
        return y + "-" + (m < 10 ? "0" + m : "" + m);
    }

    // Firebase e ei path e save / load hobe - Original ID diye
    // Example: users/123456789/monthly/2026-11/receivedCoins
    public static String getMonthlyPath(String hostId) {
        return "users/" + hostId + "/monthly/" + getCurrentMonthKey() + "/receivedCoins";
    }

    public static boolean isSameMonth(String savedMonth) {
        if (savedMonth == null) return false;
        return savedMonth.equals(getCurrentMonthKey());
    }

    // --- STRICT RULE: Pura target fill na hole 0$ ---
    // Example: 287,999 = 0$, 288,000 = 1$
    public static int getAchievedLevel(long receivedCoins) {
        int level = 0;
        for (int i = 0; i < TARGET_COINS.length; i++) {
            if (receivedCoins >= TARGET_COINS[i]) {
                level = i + 1;
            } else {
                break;
            }
        }
        return level;
    }

    public static double getHostSalary(long receivedCoins) {
        int lvl = getAchievedLevel(receivedCoins);
        if (lvl == 0) return 0;
        return ANCHOR_SALARY[lvl - 1];
    }

    public static double getAgencyCommission(long receivedCoins) {
        int lvl = getAchievedLevel(receivedCoins);
        if (lvl == 0) return 0;
        return AGENCY_COMMISSION[lvl - 1];
    }

    // ===== 1 POLICY - 2 KAAJ =====
    // Host er jonno - Sudhu Host Salary - Host Page e eita use hobe
    public static double getHostOnlySalary(long receivedCoins) {
        return getHostSalary(receivedCoins);
    }

    // FIX - Host er Total = Host Salary Only - Commission jog hobe na
    // HostPolicy / HostWorkData / HostCenter - eikhane eita use hobe
    // Level 1 - Host Total = $1 - $1.5 na
    public static double getHostTotalSalary(long receivedCoins) {
        return getHostSalary(receivedCoins);
    }

    // Agency er jonno - Full - Host + Agency + Total
    // AgencyDashboard / AgencyPolicy - eikhane eita use hobe - Host dekhbe na
    public static double getTotalSalary(long receivedCoins) {
        int lvl = getAchievedLevel(receivedCoins);
        if (lvl == 0) return 0;
        return TOTAL_SALARY[lvl - 1];
    }

    // Agency Total alada kore - Agency Page e lagle
    public static double getAgencyTotalSalary(long receivedCoins) {
        return getTotalSalary(receivedCoins);
    }

    public static long getNextTarget(long receivedCoins) {
        int lvl = getAchievedLevel(receivedCoins);
        if (lvl >= TARGET_COINS.length) return -1; // Max Level Complete
        return TARGET_COINS[lvl];
    }

    public static long getRemainingToNext(long receivedCoins) {
        long next = getNextTarget(receivedCoins);
        if (next == -1) return 0;
        return next - receivedCoins;
    }
}
