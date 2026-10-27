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
    private var currentUserId: String = ""
    private var currentRoomId: String = ""

    // সিটগুলোর অবস্থা ট্র্যাক করার জন্য (কে কোন সিটে বসেছে)
    private val seatOccupants = mutableMapOf<Int, String>()

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

        // আইডি জেনারেট এবং রুম ও ইউজার আইডি এক করা
        currentUserId = IdManager.generateRandomRoomOrUserId()
        currentRoomId = currentUserId
        roomTitleText.text = "SR Voice Room (ID: $currentRoomId)"

        Toast.makeText(this, "Welcome! Your ID: $currentUserId", Toast.LENGTH_LONG).show()

        // 1. Room Profile / Name & Theme Edit
        editRoomButton.setOnClickListener {
            showEditRoomDialog()
        }

        // 2. Seat Click Listeners (Sitting & Interaction System)
        seat1Btn.setOnClickListener { handleSeatClick(1, seat1Btn, "Host") }
        seat2Btn.setOnClickListener { handleSeatClick(2, seat2Btn, "Seat 2") }
        seat3Btn.setOnClickListener { handleSeatClick(3, seat3Btn, "Seat 3") }
        seat4Btn.setOnClickListener { handleSeatClick(4, seat4Btn, "Seat 4") }
        seat5Btn.setOnClickListener { handleSeatClick(5, seat5Btn, "Seat 5") }
        seat6Btn.setOnClickListener { handleSeatClick(6, seat6Btn, "Seat 6") }

        // 3. Chat Button
        chatBtn.setOnClickListener {
            Toast.makeText(this, "Room Chat & Entry Effect Panel Opened", Toast.LENGTH_SHORT).show()
        }

        // 4. Serial Button
        serialBtn.setOnClickListener {
            Toast.makeText(this, "Added to speaking queue (Serial)", Toast.LENGTH_SHORT).show()
        }

        // 5. Gift Box Button
        giftBtn.setOnClickListener {
            Toast.makeText(this, "Gift Box opened! Send gifts to speakers.", Toast.LENGTH_SHORT).show()
        }
    }

    // সিটে বসা এবং ম্যানেজ করার লজিক
    private fun handleSeatClick(seatNumber: Int, seatButton: Button, seatName: String) {
        val occupant = seatOccupants[seatNumber]
        if (occupant == null) {
            // যদি সিট খালি থাকে, তবে ইউজার এই সিটে বসবে
            seatOccupants[seatNumber] = currentUserId
            seatButton.text = "User: ${currentUserId.takeLast(4)}"
            Toast.makeText(this, "You sat on $seatName", Toast.LENGTH_SHORT).show()
        } else {
            // যদি সিট বুক করা থাকে, তবে ম্যানেজমেন্ট অপশন দেখাবে (মাইক মিউট, আনসিট ইত্যাদি)
            showSeatControlDialog(seatNumber, seatButton, seatName)
        }
    }

    // Dialog to change Room Profile & Themes
    private fun showEditRoomDialog() {
        val options = arrayOf("Change Room Name", "Change Room Theme (Background)", "Set Entry Effect")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Room Profile & Customization")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> showChangeNameDialog()
                1 -> showThemeSelectionDialog()
                2 -> Toast.makeText(this, "Entry effect updated successfully!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.show()
    }

    // রুমের নাম পরিবর্তনের ডায়ালগ
    private fun showChangeNameDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Room Name")
        val input = EditText(this)
        input.hint = "Enter new room title"
        builder.setView(input)

        builder.setPositiveButton("Update") { _, _ ->
            val newName = input.text.toString()
            if (newName.isNotEmpty()) {
                roomTitleText.text = "$newName (ID: $currentRoomId)"
                Toast.makeText(this, "Room profile updated!", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    // রুম থিম বা ব্যাকগ্রাউন্ড পরিবর্তনের ডায়ালগ
    private fun showThemeSelectionDialog() {
        val themes = arrayOf("Classic Dark Theme", "Neon Party Theme", "Royal Gold Theme", "Cool Blue Theme")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Room Theme")
        builder.setItems(themes) { _, which ->
            val selectedTheme = themes[which]
            Toast.makeText(this, "Theme applied: $selectedTheme", Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }

    // সিট কন্ট্রোল ডায়ালগ (Mute/Unmute / Step Down)
    private fun showSeatControlDialog(seatNumber: Int, seatButton: Button, seatName: String) {
        val options = arrayOf("Mute / Unmute Mic", "Step Down (Leave Seat)", "Lock Seat")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("$seatName Management")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    isMuted = !isMuted
                    val status = if (isMuted) "Muted" else "Unmuted"
                    Toast.makeText(this, "$seatName mic is $status", Toast.LENGTH_SHORT).show()
                }
                1 -> {
                    // সিট খালি করে দেওয়া
                    seatOccupants.remove(seatNumber)
                    seatButton.text = seatName
                    Toast.makeText(this, "You left $seatName", Toast.LENGTH_SHORT).show()
                }
                2 -> Toast.makeText(this, "$seatName is locked by admin", Toast.LENGTH_SHORT).show()
            }
        }
        builder.show()
    }
}
