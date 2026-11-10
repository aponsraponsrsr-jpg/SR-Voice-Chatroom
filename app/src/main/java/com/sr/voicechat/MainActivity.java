package com.sr.voicechat;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class MainActivity extends AppCompatActivity {
private ImageView btnPlus;
private SharedPreferences prefs;
private String currentUserId = "";
private String myName = "User";
private DatabaseReference roomsRef;
private DatabaseReference usersRef;
@Override protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_main);
prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
currentUserId = prefs.getString("MY_9_DIGIT_ID", "");
if (currentUserId.isEmpty()) {
currentUserId = prefs.getString("FIREBASE_UID", "");
}
if (currentUserId.isEmpty()) {
int r = (int) (Math.random() * 900000000) + 100000000;
currentUserId = String.valueOf(r);
prefs.edit().putString("MY_9_DIGIT_ID", currentUserId).apply();
}
myName = prefs.getString("USER_NAME", "User");
roomsRef = FirebaseDatabase.getInstance().getReference("rooms");
usersRef = FirebaseDatabase.getInstance().getReference("users");
btnPlus = findViewById(R.id.btnPlus);
if (btnPlus == null) {
btnPlus = findViewById(R.id.btnCreateRoom);
}
if (btnPlus != null) {
btnPlus.setOnClickListener(v -> onPlusClicked());
}
setupBottomNav();
}
private void setupBottomNav() {
try {
LinearLayout navHome = findViewById(R.id.navHome);
LinearLayout navMoment = findViewById(R.id.navMoment);
LinearLayout navInbox = findViewById(R.id.navInbox);
LinearLayout navMe = findViewById(R.id.navMe);
if (navMe != null) {
navMe.setOnClickListener(v -> {
Intent i = new Intent(MainActivity.this, ProfileActivity.class);
startActivity(i);
});
}
} catch (Exception ignored) {}
}
private void onPlusClicked() {
boolean isDone = prefs.getBoolean("ROOM_SETUP_DONE", false);
String savedRoom = prefs.getString("MY_ROOM_NAME", "");
if (!isDone || savedRoom.isEmpty()) {
showFirstTimeRoomSetupDialog();
} else {
createRoomAndOpen();
}
}
private void showFirstTimeRoomSetupDialog() {
AlertDialog.Builder b = new AlertDialog.Builder(this);
b.setTitle("Setup Your Room - One Time Only");
LinearLayout layout = new LinearLayout(this);
layout.setOrientation(LinearLayout.VERTICAL);
layout.setPadding(50, 30, 50, 10);
TextView t1 = new TextView(this);
t1.setText("Room Name");
t1.setTextSize(13);
layout.addView(t1);
EditText inputRoomName = new EditText(this);
inputRoomName.setHint("Ex: Apon SR Room");
String oldRoom = prefs.getString("MY_ROOM_NAME", "");
if (!oldRoom.isEmpty()) {
inputRoomName.setText(oldRoom);
} else {
String def = myName;
def += " Room";
inputRoomName.setText(def);
}
inputRoomName.setPadding(25, 20, 25, 20);
layout.addView(inputRoomName);
TextView t2 = new TextView(this);
t2.setText("Announcement / Notice");
t2.setTextSize(13);
t2.setPadding(0, 20, 0, 0);
layout.addView(t2);
EditText inputAnn = new EditText(this);
inputAnn.setHint("Ex: Welcome to my voice room!");
String oldAnn = prefs.getString("MY_ROOM_ANNOUNCEMENT", "");
if (!oldAnn.isEmpty()) {
inputAnn.setText(oldAnn);
}
inputAnn.setPadding(25, 20, 25, 20);
layout.addView(inputAnn);
TextView t3 = new TextView(this);
t3.setText("Your Profile Bio");
t3.setTextSize(13);
t3.setPadding(0, 20, 0, 0);
layout.addView(t3);
EditText inputBio = new EditText(this);
inputBio.setHint("Ex: I love music and friends");
String oldBio = prefs.getString("USER_BIO", "");
if (!oldBio.isEmpty()) {
inputBio.setText(oldBio);
}
inputBio.setPadding(25, 20, 25, 20);
layout.addView(inputBio);
TextView note = new TextView(this);
note.setText("One time only. After Save, permanent.");
note.setTextSize(11);
note.setPadding(0, 15, 0, 0);
layout.addView(note);
b.setView(layout);
b.setCancelable(false);
b.setPositiveButton("Save And Open Room", (d, w) -> {
String roomName = inputRoomName.getText().toString().trim();
String ann = inputAnn.getText().toString().trim();
String bio = inputBio.getText().toString().trim();
if (roomName.isEmpty()) {
Toast.makeText(this, "Enter Room Name",
Toast.LENGTH_SHORT).show();
showFirstTimeRoomSetupDialog();
return;
}
if (ann.isEmpty()) {
ann = "Welcome to my voice room!";
}
if (bio.isEmpty()) {
bio = "Hello Friends!";
}
saveRoomSetupPermanent(roomName, ann, bio);
});
b.setNegativeButton("Cancel", (d, w) -> d.dismiss());
b.show();
}
private void saveRoomSetupPermanent(String roomName,
String ann, String bio) {
prefs.edit().putString("MY_ROOM_NAME", roomName).apply();
prefs.edit().putString("MY_ROOM_ANNOUNCEMENT", ann).apply();
prefs.edit().putString("USER_BIO", bio).apply();
prefs.edit().putBoolean("ROOM_SETUP_DONE", true).apply();
String roomKey = "room_";
roomKey += currentUserId;
DatabaseReference meta = roomsRef.child(roomKey).child("meta");
meta.child("roomName").setValue(roomName);
meta.child("roomBio").setValue(ann);
meta.child("roomAnnouncement").setValue(ann);
meta.child("roomOwnerId").setValue(currentUserId);
meta.child("roomOwnerName").setValue(myName);
meta.child("roomOwnerBio").setValue(bio);
meta.child("isLocked").setValue(false);
meta.child("createdAt").setValue(System.currentTimeMillis());
usersRef.child(currentUserId).child("roomName").setValue(roomName);
usersRef.child(currentUserId).child("roomAnnouncement").setValue(ann);
usersRef.child(currentUserId).child("roomBio").setValue(bio);
usersRef.child(currentUserId).child("bio").setValue(bio);
usersRef.child(currentUserId).child("hasRoom").setValue(true);
Toast.makeText(this, "Room Saved Successfully!",
Toast.LENGTH_SHORT).show();
createRoomAndOpen();
}
private void createRoomAndOpen() {
String roomName = prefs.getString("MY_ROOM_NAME", "");
if (roomName.isEmpty()) {
roomName = myName;
roomName += " Room";
}
String roomKey = "room_";
roomKey += currentUserId;
DatabaseReference meta = roomsRef.child(roomKey).child("meta");
meta.child("roomName").setValue(roomName);
meta.child("roomOwnerId").setValue(currentUserId);
meta.child("roomOwnerName").setValue(myName);
meta.child("isActive").setValue(true);
meta.child("lastActive").setValue(System.currentTimeMillis());
Toast.makeText(this, "Room Created Successfully!",
Toast.LENGTH_SHORT).show();
Intent i = new Intent(MainActivity.this, VoiceRoomActivity.class);
i.putExtra("ROOM_OWNER_ID", currentUserId);
i.putExtra("ROOM_NAME", roomName);
startActivity(i);
}
}
