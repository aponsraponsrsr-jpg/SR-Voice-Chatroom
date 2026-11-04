package com.sr.voicechat;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class VoiceRoomActivity extends AppCompatActivity {

    private ImageButton btnPower;
    private Button btnMicToggle, btnChatOption, btnGiftBox, btnGameBox;
    private boolean isMuted = false;

    private ListView chatListView;
    private ArrayList<String> chatMessages;
    private ArrayAdapter<String> chatAdapter;
    private SharedPreferences prefs;

    // ফায়ারবেস ডেটাবেস রেফারেন্স
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        // ফায়ারবেস রিয়েলটাইম ডেটাবেস ইনিশিয়ালাইজেশন
        databaseReference = FirebaseDatabase.getInstance().getReference("rooms").child("room_01");

        // ১. পাওয়ার / এক্সিট বাটন
        btnPower = findViewById(R.id.btnPower);
        if (btnPower != null) {
            btnPower.setOnClickListener(v -> {
                CharSequence[] options = {"Minimize", "Exit Room"};
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("Room Options");
                builder.setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        moveTaskToBack(true);
                    } else {
                        finish();
                    }
                });
                builder.show();
            });
        }

        // ২. স্ক্রিন চ্যাট লিস্ট ইনিশিয়ালাইজেশন
        chatListView = findViewById(R.id.chatListView);
        chatMessages = new ArrayList<>();
        chatMessages.add("🌟 System: SR Voice Chatroom এ সবাইকে স্বাগতম!");
        chatMessages.add("💬 গ্লোবাল পার্মানেন্ট সিট এবং প্রোফাইল সিঙ্ক্রোনাইজেশন সক্রিয় রয়েছে।");

        chatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatMessages);
        if (chatListView != null) {
            chatListView.setAdapter(chatAdapter);
        }

        // ৩. সিট ইন্টারঅ্যাকশন এবং রিয়েল-টাইম সিঙ্ক লজিক
        setupSeatInteractions();
        listenToFirebaseSeats();

        // ৪. নিচের অ্যাকশন বার বাটনগুলো
        btnMicToggle = findViewById(R.id.btnMicToggle);
        if (btnMicToggle != null) {
            btnMicToggle.setOnClickListener(v -> {
                isMuted = !isMuted;
                if (isMuted) {
                    btnMicToggle.setText("🔇 Muted");
                    Toast.makeText(this, "Mic Muted", Toast.LENGTH_SHORT).show();
                } else {
                    btnMicToggle.setText("🎙️ Mic");
                    Toast.makeText(this, "Mic Unmuted", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnChatOption = findViewById(R.id.btnChatOption);
        if (btnChatOption != null) {
            btnChatOption.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("💬 Send Message to Room");

                final EditText input = new EditText(VoiceRoomActivity.this);
                input.setHint("Type your message here...");
                builder.setView(input);

                builder.setPositiveButton("Send", (dialog, which) -> {
                    String message = input.getText().toString().trim();
                    String userName = prefs.getString("USER_NAME", "User");
                    if (!message.isEmpty()) {
                        chatMessages.add("👤 " + userName + ": " + message);
                        chatAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Message sent!", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                builder.show();
            });
        }

        btnGiftBox = findViewById(R.id.btnGiftBox);
        if (btnGiftBox != null) {
            btnGiftBox.setOnClickListener(v -> {
                CharSequence[] gifts = {"🌹 Rose (10 Coins)", "👑 Crown (500 Coins)", "💎 Diamond (1000 Coins)"};
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("🎁 Send Gift");
                builder.setItems(gifts, (dialog, which) -> {
                    chatMessages.add("🎁 Gift Sent: " + gifts[which]);
                    chatAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Successfully Sent Gift!", Toast.LENGTH_SHORT).show();
                });
                builder.show();
            });
        }

        btnGameBox = findViewById(R.id.btnGameBox);
        if (btnGameBox != null) {
            btnGameBox.setOnClickListener(v -> {
                CharSequence[] games = {"🎮 Ludo Game", "🎯 Lucky Wheel", "🎲 Dice Battle"};
                AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                builder.setTitle("🎮 Voice Room Games");
                builder.setItems(games, (dialog, which) -> {
                    Toast.makeText(this, "Starting " + games[which] + "...", Toast.LENGTH_SHORT).show();
                });
                builder.show();
            });
        }
    }

    // ফায়ারবেস থেকে গ্লোবালি সিটের ছবি (Base64 থেকে ডিকোড করে) ও নাম রেন্ডার করার লজিক
    private void listenToFirebaseSeats() {
        int[] seatImgIds = {
            R.id.seatImg1, R.id.seatImg2, R.id.seatImg3, R.id.seatImg4, R.id.seatImg5,
            R.id.seatImg6, R.id.seatImg7, R.id.seatImg8, R.id.seatImg9, R.id.seatImg10
        };

        int[] seatTxtIds = {
            R.id.seatTxt1, R.id.seatTxt2, R.id.seatTxt3, R.id.seatTxt4, R.id.seatTxt5,
            R.id.seatTxt6, R.id.seatTxt7, R.id.seatTxt8, R.id.seatTxt9, R.id.seatTxt10
        };

        databaseReference.child("seats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < 10; i++) {
                    ImageView seatImg = findViewById(seatImgIds[i]);
                    TextView seatTxt = findViewById(seatTxtIds[i]);
                    String seatKey = "seat_" + i;

                    if (snapshot.hasChild(seatKey)) {
                        String name = snapshot.child(seatKey).child("userName").getValue(String.class);
                        String imageBase64 = snapshot.child(seatKey).child("userImage").getValue(String.class);

                        // নাম সেট করা
                        if (seatTxt != null) {
                            seatTxt.setText(name != null ? name : (i == 0 ? "Host" : String.valueOf(i + 1)));
                        }

                        // Base64 ইমেজ ডিকোড করে গোল প্রোফাইল পিকচার গ্লোবালি শো করা
                        if (seatImg != null && imageBase64 != null && !imageBase64.isEmpty()) {
                            try {
                                byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                seatImg.setImageBitmap(decodedByte);
                                seatImg.setPadding(0, 0, 0, 0);
                                seatImg.setImageTintList(null);
                            } catch (Exception e) {
                                seatImg.setImageResource(android.R.drawable.ic_menu_my_calendar);
                                seatImg.setPadding(12, 12, 12, 12);
                            }
                        }
                    } else {
                        // সিট খালি থাকলে ডিফল্ট অবস্থা
                        if (seatTxt != null) {
                            seatTxt.setText(i == 0 ? "Host" : String.valueOf(i + 1));
                        }
                        if (seatImg != null) {
                            seatImg.setImageResource(android.R.drawable.ic_menu_my_calendar);
                            seatImg.setPadding(12, 12, 12, 12);
                            seatImg.setImageTintList(null);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // কোনো এরর হ্যান্ডলিং
            }
        });
    }

    // সিটে ক্লিক করে নিজের নাম ও ছবি (Base64 এ কনভার্ট করে) ফায়ারবেসে গ্লোবালি পাঠানোর লজিক
    private void setupSeatInteractions() {
        int[] seatCardIds = {
            R.id.seatCard1, R.id.seatCard2, R.id.seatCard3, R.id.seatCard4, R.id.seatCard5,
            R.id.seatCard6, R.id.seatCard7, R.id.seatCard8, R.id.seatCard9, R.id.seatCard10
        };

        for (int i = 0; i < seatCardIds.length; i++) {
            final int seatIndex = i;
            final int seatNumber = i + 1;
            View seatContainer = findViewById(seatCardIds[i]);

            if (seatContainer != null) {
                seatContainer.setOnClickListener(v -> {
                    String seatTitle = (seatNumber == 1) ? "Host Seat" : "Seat " + seatNumber;
                    CharSequence[] seatOptions = {"Take Mic (" + seatTitle + ")", "Leave Mic"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(VoiceRoomActivity.this);
                    builder.setTitle("🎙️ " + seatTitle + " Options");
                    builder.setItems(seatOptions, (dialog, which) -> {
                        String userName = prefs.getString("USER_NAME", "User");
                        String userImageUriStr = prefs.getString("USER_IMAGE", "");
                        String seatKey = "seat_" + seatIndex;

                        if (which == 0) {
                            // ছবিটিকে Base64 স্ট্রিংয়ে কনভার্ট করার প্রসেস
                            String imageBase64 = "";
                            if (!userImageUriStr.isEmpty()) {
                                try {
                                    Uri uri = Uri.parse(userImageUriStr);
                                    InputStream inputStream = getContentResolver().openInputStream(uri);
                                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                    
                                    // সাইজ অপ্টিমাইজ করার জন্য রিসাইজ করা যেতে পারে
                                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 150, 150, true);
                                    
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                                    byte[] imageBytes = baos.toByteArray();
                                    imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                                } catch (Exception e) {
                                    imageBase64 = "";
                                }
                            }

                            // ফায়ারবেসে পার্মানেন্টলি নাম ও ছবির Base64 ডেটা গ্লোবালি সেভ করা
                            databaseReference.child("seats").child(seatKey).child("userName").setValue(userName);
                            databaseReference.child("seats").child(seatKey).child("userImage").setValue(imageBase64);

                            Toast.makeText(VoiceRoomActivity.this, "Joined " + seatTitle + " successfully!", Toast.LENGTH_SHORT).show();
                            chatMessages.add("🎙️ System: " + userName + " took " + seatTitle);
                            chatAdapter.notifyDataSetChanged();
                        } else {
                            // Leave Mic: ফায়ারবেস থেকে সিট খালি করা
                            databaseReference.child("seats").child(seatKey).removeValue();

                            Toast.makeText(VoiceRoomActivity.this, "Left " + seatTitle, Toast.LENGTH_SHORT).show();
                            chatMessages.add("❌ System: " + userName + " left " + seatTitle);
                            chatAdapter.notifyDataSetChanged();
                        }
                    });
                    builder.show();
                });
            }
        }
    }
}
