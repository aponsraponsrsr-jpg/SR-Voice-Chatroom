package com.sr.voicechat

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class VoiceRoomActivity : AppCompatActivity() {

    private lateinit var roomTitleText: TextView
    private lateinit var editRoomButton: Button
    private lateinit var seat1Btn: Button
    private lateinit var seat2Btn: Button
    private lateinit var seat3Btn: Button
    private lateinit var seat4Btn: Button
    private lateinit var seat5Btn: Button
    private lateinit var seat6Btn: Button
    private lateinit var chatBtn: Button
    private lateinit var serialBtn: Button
    private lateinit var giftBtn: Button

    private var isMuted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_room)

        roomTitleText = findViewById(R.id.roomTitleText)
        editRoomButton = findViewById(R.id.editRoomButton)
        seat1Btn = findViewById(R.id.seat1Btn)
        seat2Btn = findViewById(R.id.seat2Btn)
        seat3Btn = findViewById(R.id.seat3Btn)
        seat4Btn = findViewById(R.id.seat4Btn)
        seat5Btn = findViewById(R.id.seat5Btn)
        seat6Btn = findViewById(R.id.seat6Btn)
        chatBtn = findViewById(R.id.chatBtn)
        serialBtn = findViewById(R.id.serialBtn)
        giftBtn = findViewById(R.id.giftBtn)

        // 1. Room Name Edit (Owner Control)
        editRoomButton.setOnClickListener {
            showEditRoomDialog()
        }

        // 2. Host / Seat 1 Control
        seat1Btn.setOnClickListener {
            Toast.makeText(this, "Host Seat (Global Control Enabled)", Toast.LENGTH_SHORT).show()
        }

        // 3. Seat 2 Interaction (Mute/Unmute Logic)
        seat2Btn.setOnClickListener {
            showSeatControlDialog()
        }

        // Other seats click listeners
        seat3Btn.setOnClickListener {
            Toast.makeText(this, "Seat 3 clicked", Toast.LENGTH_SHORT).show()
        }
        seat4Btn.setOnClickListener {
            Toast.makeText(this, "Seat 4 clicked", Toast.LENGTH_SHORT).show()
        }
        seat5Btn.setOnClickListener {
            Toast.makeText(this, "Seat 5 clicked", Toast.LENGTH_SHORT).show()
        }
        seat6Btn.setOnClickListener {
            Toast.makeText(this, "Seat 6 clicked", Toast.LENGTH_SHORT).show()
        }

        // 4. Chat Button
        chatBtn.setOnClickListener {
            Toast.makeText(this, "Room Chat Panel Opened", Toast.LENGTH_SHORT).show()
        }

        // 5. Serial Button
        serialBtn.setOnClickListener {
            Toast.makeText(this, "Added to speaking queue (Serial)", Toast.LENGTH_SHORT).show()
        }

        // 6. Gift Box Button
        giftBtn.setOnClickListener {
            Toast.makeText(this, "Gift Box opened! Send gifts to speakers.", Toast.LENGTH_SHORT).show()
        }
    }

    // Dialog to change Room Name
    private fun showEditRoomDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Room Name")

        val input = EditText(this)
        input.setText(roomTitleText.text.toString())
        builder.setView(input)

        builder.setPositiveButton("Update", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                val newName = input.text.toString()
                if (newName.isNotEmpty()) {
                    roomTitleText.text = newName
                    Toast.makeText(this@VoiceRoomActivity, "Room name updated successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        })
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    // Dialog for Seat Control (Mute/Unmute/Kick)
    private fun showSeatControlDialog() {
        val options = arrayOf("Mute / Unmute Mic", "Lock Seat", "Step Down")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seat Management")
        builder.getItems(options, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when (which) {
                    0 -> {
                        isMuted = !isMuted
                        val status = if (isMuted) "Muted by Admin" else "Unmuted"
                        Toast.makeText(this@VoiceRoomActivity, "Seat 2 is now $status", Toast.LENGTH_SHORT).show()
                    }
                    1 -> Toast.makeText(this@VoiceRoomActivity, "Seat Locked", Toast.LENGTH_SHORT).show()
                    2 -> Toast.makeText(this@VoiceRoomActivity, "User moved to audience", Toast.LENGTH_SHORT).show()
                }
            }
        })
        builder.show()
    }
}
