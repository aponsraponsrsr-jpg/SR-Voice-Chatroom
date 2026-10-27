package com.sr.voicechat

import kotlin.random.Random

object IdManager {
    // সর্বনিম্ন ৭ ডিজিট বা তার বেশি থেকে আইডি জেনারেট করার ফাংশন (যেমন: ২৯৪৭০৮৪)
    fun generateRandomRoomOrUserId(): String {
        // ১০,০০,০০০ থেকে ৯৯,৯৯,৯৯৯ পর্যন্ত র্যান্ডম ৭ ডিজিটের আইডি তৈরি করবে
        val minId = 1000000L
        val maxId = 9999999L
        return Random.nextLong(minId, maxId).toString()
    }
}
