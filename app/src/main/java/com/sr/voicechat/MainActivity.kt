package com.sr.voicechat

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCheckWallet = findViewById<Button>(R.id.btnCheckWallet)
        btnCheckWallet.setOnClickListener {
            Toast.makeText(this, "ওনার কন্ট্রোল ও ওয়ালেট প্যানেল শীঘ্রই আসছে!", Toast.LENGTH_SHORT).show()
        }
    }
}
