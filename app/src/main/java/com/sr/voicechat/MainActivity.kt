package com.sr.voicechat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQ_ID = 22
    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.INTERNET,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.ACCESS_NETWORK_STATE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // পারমিশন চেক করা
        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID)
        }

        // চ্যাটরুম জয়েন করার বাটন
        val joinButton = findViewById<Button>(R.id.joinButton)
        joinButton?.setOnClickListener {
            Toast.makeText(this, "এস আর ভয়েস চ্যাটরুমে স্বাগতম!", Toast.LENGTH_SHORT).show()
        }

        // ওয়ালেট ও ওনার কন্ট্রোল বাটন
        val btnCheckWallet = findViewById<Button>(R.id.btnCheckWallet)
        btnCheckWallet?.setOnClickListener {
            Toast.makeText(this, "ওয়ালের ও কন্ট্রোল প্যানেল লোড হচ্ছে...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissions(): Boolean {
        for (permission in REQUESTED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }
}
