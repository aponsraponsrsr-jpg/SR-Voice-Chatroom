package com.sr.voicechat;

public class SalaryPolicy {
    // SR VOICE CHAT ROOM - OFFICIAL HOST SALARY POLICY 1-22
    // Tumi je chobi dicha, oi number gulai ekhane

    public static final int[] LEVELS = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22};

    public static final long[] TARGET_COINS = {
        288000L, 588000L, 1190000L, 1780000L, 3280000L, 5780000L, 7880000L,
        9980000L, 13850000L, 18850000L, 26500000L, 34500000L, 43500000L,
        56500000L, 76500000L, 88500000L, 115500000L, 155000000L, 210000000L,
        288000000L, 350000000L, 460000000L
    };

    // Host pabe - 80%
    public static final double[] ANCHOR_SALARY = {
        1, 2, 4.5, 7, 14, 24, 32, 40, 56, 76, 104, 132, 168, 220, 292, 348, 440, 584, 752, 1024, 1240, 1600
    };

    // Agency pabe - 20% - Host dekhbe na
    public static final double[] AGENCY_COMMISSION = {
        0.5, 1, 1.5, 2, 3, 6, 8, 10, 14, 19, 26, 33, 42, 55, 73, 87, 110, 146, 188, 256, 310, 400
    };

    // Total
    public static final double[] TOTAL_SALARY = {
        1.5, 3, 6, 9, 17, 30, 40, 50, 70, 95, 130, 165, 210, 275, 365, 435, 550, 730, 940, 1280, 1550, 2000
    };

    public static String money(double d) {
        if (d == (long) d) return "$" + (long) d;
        return "$" + d;
    }
}
