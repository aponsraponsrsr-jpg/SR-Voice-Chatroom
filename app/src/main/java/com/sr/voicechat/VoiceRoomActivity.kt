package com.sr.voicechat

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class VoiceRoomActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_room)

        val editRoomButton = findViewById<Button>(R.id.editRoomButton)
        editRoomButton.setOnClickListener {
            Toast.makeText(this, "Room Edit & Seat Management Panel", Toast.LENGTH_SHORT).show()
        }
    }
}
