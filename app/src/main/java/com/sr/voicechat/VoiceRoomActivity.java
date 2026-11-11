package com.sr.voicechat;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sr.voicechat.adapters.GiftAdapter;
import com.sr.voicechat.models.GiftModel;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoiceRoomActivity extends AppCompatActivity {
private ImageButton btnPower;
private Button btnMicToggle, btnChatOption, btnGiftBox, btnGameBox;
private TextView roomTitleText, roomInfoText, textOnlineCount, roomNoticeBarText;
private ImageView btnOnlineList, roomHeaderProfileImg, btnRoomTools, themeBackgroundImg;
private LinearLayout btnInRoomRanking;
private boolean isMuted = false;
private ListView chatListView;
private ArrayList<String> chatMessages;
private ArrayAdapter<String> chatAdapter;
private SharedPreferences prefs;
private DatabaseReference databaseReference, roomMetaRef, userDatabaseRef, messagesRef, adminsRef;
private FirebaseAuth mAuth;
private String roomOwnerId = "", currentUserId = "";
private String currentThemeName = "Night Purple";
private static final int PICK_IMAGE_REQUEST = 101;
private EditText dialogInputName, dialogInputBio;
private long roomJoinTimestamp = 0;
private List<String> roomAdminList = new ArrayList<>();
private String rankTab = "Daily";
private String rankType = "Wealth";
private List<RankItem> rankCache = new ArrayList<>();
private String selectedGiftTargetUid = "";
private String selectedGiftTargetName = "All";
private int giftQuantity = 1;
private String giftCategory = "Gift";
private long myWalletCoins = 0L;
private TextView walletTextInGift = null;
private static final double EXCHANGE_RATE = 0.30;
private String myDisplayName = "User";
private String myCountry = "";
private String myGender = "Male";
private String myLanguage = "English";

private String getMyDisplayName() {
String n = prefs.getString("USER_NAME", "");
if (n != null && !n.isEmpty()) return n;
if (myDisplayName != null && !myDisplayName.isEmpty()) return myDisplayName;
return "User";
}

private void saveMyNameGlobal(String inputName) {
if (inputName == null) return;
String newName = inputName.trim();
if (newName.isEmpty()) return;
myDisplayName = newName;
prefs.edit().putString("USER_NAME", newName).apply();
userDatabaseRef.child(currentUserId).child("name").setValue(newName);
databaseReference.child("online_users").child(currentUserId).setValue(newName);
final String finalName = newName;
for (int i = 0; i < 10; i++) {
final int idx = i;
databaseReference.child("seats").child("seat_" + idx).addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
String uid = s.child("userId").getValue(String.class);
if (uid != null && uid.equals(currentUserId)) databaseReference.child("seats").child("seat_" + idx).child("userName").setValue(finalName);
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
}

private void showEditMyProfileDialog() {
AlertDialog.Builder b = new AlertDialog.Builder(this);
b.setTitle("Edit My Profile");
LinearLayout layout = new LinearLayout(this);
layout.setOrientation(LinearLayout.VERTICAL);
layout.setPadding(50, 30, 50, 30);
EditText inputName = new EditText(this);
inputName.setHint("Enter your name...");
inputName.setText(getMyDisplayName());
inputName.setTextColor(Color.BLACK);
inputName.setBackgroundColor(Color.WHITE);
inputName.setPadding(25, 20, 25, 20);
layout.addView(inputName);
TextView gLabel = new TextView(this);
gLabel.setText("Gender:");
gLabel.setPadding(0, 20, 0, 8);
layout.addView(gLabel);
android.widget.Spinner genderSpin = new android.widget.Spinner(this);
String[] genders = {"Male", "Female"};
genderSpin.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, genders));
if (myGender != null && myGender.toLowerCase().contains("female")) genderSpin.setSelection(1);
layout.addView(genderSpin);
TextView cLabel = new TextView(this);
cLabel.setText("Country:");
cLabel.setPadding(0, 20, 0, 8);
layout.addView(cLabel);
android.widget.Spinner countrySpin = new android.widget.Spinner(this);
String[] countries = {"Bangladesh","India","Pakistan","Nepal","Sri Lanka","USA","UK","Canada","Australia","Saudi Arabia","UAE","Qatar","Kuwait","Oman","Malaysia","Indonesia","Philippines","Germany","France"};
countrySpin.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, countries));
int sel = 0;
for (int i = 0; i < countries.length; i++) { if (countries[i].equalsIgnoreCase(myCountry)) { sel = i; break; } }
countrySpin.setSelection(sel);
layout.addView(countrySpin);
EditText inputBio = new EditText(this);
inputBio.setHint("Enter Bio...");
inputBio.setTextColor(Color.BLACK);
inputBio.setBackgroundColor(Color.WHITE);
inputBio.setPadding(25, 20, 25, 20);
LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
bp.topMargin = 20; inputBio.setLayoutParams(bp);
layout.addView(inputBio);
b.setView(layout);
b.setPositiveButton("Save", (d, w) -> {
String nn = inputName.getText().toString().trim();
String newGender = genders[genderSpin.getSelectedItemPosition()];
String newCountry = countries[countrySpin.getSelectedItemPosition()];
String newBio = inputBio.getText().toString().trim();
if (!nn.isEmpty()) saveMyNameGlobal(nn);
myGender = newGender;
myCountry = newCountry;
prefs.edit().putString("USER_COUNTRY", newCountry).apply();
prefs.edit().putString("USER_GENDER", newGender).apply();
userDatabaseRef.child(currentUserId).child("gender").setValue(newGender);
userDatabaseRef.child(currentUserId).child("country").setValue(newCountry);
userDatabaseRef.child(currentUserId).child("bio").setValue(newBio);
Toast.makeText(this, "Saved: " + newCountry + " " + getCountryFlag(newCountry), Toast.LENGTH_SHORT).show();
});
b.setNegativeButton("Cancel", (d, w) -> d.dismiss());
b.show();
}

@Override protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_voice_room);
prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
mAuth = FirebaseAuth.getInstance();
currentUserId = prefs.getString("MY_9_DIGIT_ID", "");
if (currentUserId.isEmpty()) {
int r = (int) (Math.random() * 900000000) + 100000000;
currentUserId = String.valueOf(r);
prefs.edit().putString("MY_9_DIGIT_ID", currentUserId).apply();
}
String saved = prefs.getString("USER_NAME", "");
if (saved != null && !saved.isEmpty()) myDisplayName = saved;
String savedC = prefs.getString("USER_COUNTRY", "");
if (savedC != null && !savedC.isEmpty()) myCountry = savedC;
String savedG = prefs.getString("USER_GENDER", "");
if (savedG != null && !savedG.isEmpty()) myGender = savedG;
roomOwnerId = getIntent().getStringExtra("ROOM_OWNER_ID");
if (roomOwnerId == null || roomOwnerId.isEmpty()) roomOwnerId = currentUserId;
roomJoinTimestamp = System.currentTimeMillis();
databaseReference = FirebaseDatabase.getInstance().getReference("rooms").child("room_" + roomOwnerId);
roomMetaRef = databaseReference.child("meta");
messagesRef = databaseReference.child("messages");
adminsRef = databaseReference.child("admins");
userDatabaseRef = FirebaseDatabase.getInstance().getReference("users");
syncLatestUserProfile();
listenToRoomDetails();
listenToRoomAdmins();
listenToRoomTheme();
listenToMyWallet();
setupThemeBackgroundImage();
btnPower = findViewById(R.id.btnPower);
roomTitleText = findViewById(R.id.roomTitleText);
roomInfoText = findViewById(R.id.roomInfoText);
textOnlineCount = findViewById(R.id.textOnlineCount);
btnOnlineList = findViewById(R.id.btnOnlineList);
roomHeaderProfileImg = findViewById(R.id.roomHeaderProfileImg);
btnInRoomRanking = findViewById(R.id.btnInRoomRanking);
roomNoticeBarText = findViewById(R.id.roomNoticeBarText);
btnRoomTools = findViewById(R.id.btnRoomTools);
if (btnPower != null) btnPower.setOnClickListener(v -> showExitDialog());
if (btnRoomTools != null) {
btnRoomTools.setOnClickListener(v -> {
boolean isOwner = currentUserId.equals(roomOwnerId);
boolean isAdmin = roomAdminList.contains(currentUserId);
if (!isOwner && !isAdmin) { Toast.makeText(this, "Only Owner / Admin can access Tools!", Toast.LENGTH_SHORT).show(); return; }
showIconToolsDialog(isOwner);
});
}
if (btnInRoomRanking != null) btnInRoomRanking.setOnClickListener(v -> showPremiumRankingDialog());
View.OnClickListener headerClick = v -> showPremiumRoomInfoAndMembersDialog();
if (roomTitleText != null) roomTitleText.setOnClickListener(headerClick);
if (roomHeaderProfileImg != null) roomHeaderProfileImg.setOnClickListener(headerClick);
if (roomInfoText != null) roomInfoText.setOnClickListener(headerClick);
if (btnOnlineList != null) btnOnlineList.setOnClickListener(v -> showScrollableOnlineMembersDialog());
chatListView = findViewById(R.id.chatListView);
chatMessages = new ArrayList<>();
chatAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chatMessages);
if (chatListView != null) chatListView.setAdapter(chatAdapter);
setupSeatInteractions();
listenToFirebaseSeats();
listenToOnlineMembers();
listenToRealtimeChatMessages();
btnMicToggle = findViewById(R.id.btnMicToggle);
if (btnMicToggle != null) btnMicToggle.setOnClickListener(v -> { isMuted = !isMuted; btnMicToggle.setText(isMuted ? "Muted" : "Mic"); });
btnChatOption = findViewById(R.id.btnChatOption);
if (btnChatOption != null) btnChatOption.setOnClickListener(v -> showChatDialog());
btnGiftBox = findViewById(R.id.btnGiftBox);
if (btnGiftBox != null) btnGiftBox.setOnClickListener(v -> showNewGiftPanel());
btnGameBox = findViewById(R.id.btnGameBox);
if (btnGameBox != null) btnGameBox.setOnClickListener(v -> showRewardEventDialog());
}

@Override public void onBackPressed() { showExitDialog(); }

private void showExitDialog() {
AlertDialog.Builder b = new AlertDialog.Builder(this);
b.setTitle("Exit Room?");
b.setMessage("Exit room or Minimize to Home?");
b.setPositiveButton("Exit Room", (d, w) -> { d.dismiss(); leaveRoomAndFinish(); });
b.setNeutralButton("Minimize", (d, w) -> {
d.dismiss(); leaveRoomKeepHome();
Intent home = new Intent(VoiceRoomActivity.this, HomeActivity.class);
home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
startActivity(home); finish();
});
b.setNegativeButton("Cancel", (d, w) -> d.dismiss());
b.show();
}

private void leaveRoomKeepHome() {
databaseReference.child("online_users").child(currentUserId).removeValue();
for (int i = 0; i < 10; i++) {
final int idx = i;
databaseReference.child("seats").child("seat_" + idx).addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
String uid = s.child("userId").getValue(String.class);
if (uid != null && uid.equals(currentUserId)) databaseReference.child("seats").child("seat_" + idx).removeValue();
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
}

private void leaveRoomAndFinish() {
databaseReference.child("online_users").child(currentUserId).removeValue();
leaveRoomKeepHome();
finishAffinity();
}

private void listenToMyWallet() {
if (currentUserId.isEmpty()) return;
userDatabaseRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
Long coins = s.child("coins").getValue(Long.class);
Long avail = s.child("availableBalance").getValue(Long.class);
Long bal = s.child("balance").getValue(Long.class);
Long oldW = s.child("walletCoins").getValue(Long.class);
long w = 0L;
if (coins != null) w = coins; else if (avail != null) w = avail; else if (bal != null) w = bal; else if (oldW != null) w = oldW;
myWalletCoins = w;
if (walletTextInGift != null) walletTextInGift.setText("Coins: " + formatCoins(w));
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}

private String formatCoins(long v) { return String.format("%,d", v); }

private void deductMyCoins(long totalCost) {
userDatabaseRef.child(currentUserId).child("coins").addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
Long cur = s.getValue(Long.class); long now = (cur != null ? cur : 0L) - totalCost; if (now < 0) now = 0;
userDatabaseRef.child(currentUserId).child("coins").setValue(now);
userDatabaseRef.child(currentUserId).child("walletCoins").setValue(now);
userDatabaseRef.child(currentUserId).child("availableBalance").setValue(now);
myWalletCoins = now; if (walletTextInGift != null) walletTextInGift.setText("Coins: " + formatCoins(now));
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
userDatabaseRef.child(currentUserId).child("sendingCoins").addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
Long cur = s.getValue(Long.class); long v = (cur != null ? cur : 0L) + totalCost;
userDatabaseRef.child(currentUserId).child("sendingCoins").setValue(v);
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}

private void addReceiverCoins30(String targetUid, long gets) {
userDatabaseRef.child(targetUid).child("coins").addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
Long cur = s.getValue(Long.class); long now = (cur != null ? cur : 0L) + gets;
userDatabaseRef.child(targetUid).child("coins").setValue(now);
userDatabaseRef.child(targetUid).child("walletCoins").setValue(now);
userDatabaseRef.child(targetUid).child("availableBalance").setValue(now);
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
userDatabaseRef.child(targetUid).child("receivingCoins").addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
Long cur = s.getValue(Long.class); long v = (cur != null ? cur : 0L) + gets;
userDatabaseRef.child(targetUid).child("receivingCoins").setValue(v);
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}

private void handleSelfSend(long totalCost, long gets) {
userDatabaseRef.child(currentUserId).child("coins").addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
Long cur = s.getValue(Long.class); long base = (cur != null ? cur : 0L); long now = base - totalCost + gets; if (now < 0) now = 0;
userDatabaseRef.child(currentUserId).child("coins").setValue(now);
userDatabaseRef.child(currentUserId).child("walletCoins").setValue(now);
userDatabaseRef.child(currentUserId).child("availableBalance").setValue(now);
myWalletCoins = now; if (walletTextInGift != null) walletTextInGift.setText("Coins: " + formatCoins(now));
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
userDatabaseRef.child(currentUserId).child("sendingCoins").addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
Long cur = s.getValue(Long.class); long v = (cur != null ? cur : 0L) + totalCost;
userDatabaseRef.child(currentUserId).child("sendingCoins").setValue(v);
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
userDatabaseRef.child(currentUserId).child("receivingCoins").addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
Long cur = s.getValue(Long.class); long v = (cur != null ? cur : 0L) + gets;
userDatabaseRef.child(currentUserId).child("receivingCoins").setValue(v);
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}

private void showQuantityPopup(Button btnQtyPopup, GiftModel selectedGift) {
AlertDialog.Builder qb = new AlertDialog.Builder(this);
qb.setTitle("Select Quantity (1 to 10)");
LinearLayout layout = new LinearLayout(this);
layout.setOrientation(LinearLayout.VERTICAL); layout.setPadding(40, 30, 40, 20);
TextView label = new TextView(this); label.setText("Select 1, 3, 5, 10 or type below"); label.setTextSize(13); label.setGravity(Gravity.CENTER); label.setPadding(0, 0, 0, 15); layout.addView(label);
LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER);
int[] presets = {1, 3, 5, 10};
for (int q : presets) {
Button b = new Button(this); b.setText("x" + q); b.setTypeface(null, android.graphics.Typeface.BOLD);
LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); lp.setMargins(6, 0, 6, 0); b.setLayoutParams(lp);
if (q == giftQuantity) { b.setBackgroundColor(Color.parseColor("#FFD700")); b.setTextColor(Color.BLACK); } else { b.setBackgroundColor(Color.parseColor("#2A1B4E")); b.setTextColor(Color.WHITE); }
row.addView(b);
}
layout.addView(row);
TextView cl = new TextView(this); cl.setText("Custom - Type yourself (1 to 10):"); cl.setTextSize(13); cl.setPadding(0, 20, 0, 8); layout.addView(cl);
EditText inputCustom = new EditText(this); inputCustom.setHint("Ex: 2, 4, 6, 7, 8, 9"); inputCustom.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); inputCustom.setTextColor(Color.BLACK); inputCustom.setBackgroundColor(Color.WHITE); inputCustom.setPadding(25, 20, 25, 20); layout.addView(inputCustom);
qb.setView(layout);
qb.setPositiveButton("OK - Custom Apply", (d, w) -> {
String txt = inputCustom.getText().toString().trim();
if (!txt.isEmpty()) { try { int cq = Integer.parseInt(txt); if (cq < 1) cq = 1; if (cq > 10) { Toast.makeText(this, "Max 10 allowed!", Toast.LENGTH_SHORT).show(); cq = 10; } giftQuantity = cq; btnQtyPopup.setText("x" + giftQuantity); } catch (Exception e) { Toast.makeText(this, "Enter correct number!", Toast.LENGTH_SHORT).show(); } }
});
qb.setNegativeButton("Cancel", (d, w) -> d.dismiss());
AlertDialog dialog = qb.create(); dialog.show();
for (int i = 0; i < row.getChildCount(); i++) { View child = row.getChildAt(i); if (child instanceof Button) { Button bb = (Button) child; bb.setOnClickListener(v -> { String t = bb.getText().toString().replace("x", ""); try { giftQuantity = Integer.parseInt(t); } catch (Exception ignored) {} btnQtyPopup.setText("x" + giftQuantity); dialog.dismiss(); }); } }
}

private void showNewGiftPanel() {
BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(30, 20, 30, 20);
GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}); bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0}); root.setBackground(bg);
TextView title = new TextView(this); title.setText("Gift Center"); title.setTextColor(Color.parseColor("#FFD700")); title.setTextSize(18); title.setTypeface(null, android.graphics.Typeface.BOLD); title.setGravity(Gravity.CENTER); title.setPadding(0, 0, 0, 12); root.addView(title);
TextView targetInfo = new TextView(this); targetInfo.setText("To: " + selectedGiftTargetName + " - Tap a seat user below"); targetInfo.setTextColor(Color.WHITE); targetInfo.setTextSize(13); targetInfo.setGravity(Gravity.CENTER); targetInfo.setPadding(0, 0, 0, 10); root.addView(targetInfo);
HorizontalScrollView seatScroll = new HorizontalScrollView(this); seatScroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 180));
LinearLayout seatRow = new LinearLayout(this); seatRow.setOrientation(LinearLayout.HORIZONTAL); seatRow.setGravity(Gravity.CENTER_VERTICAL);
seatRow.addView(makeSeatAvatarView("All", "All", null, targetInfo, true));
databaseReference.child("seats").addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot snap) { for (int i = 0; i < 10; i++) { String k = "seat_" + i; if (!snap.hasChild(k)) continue; String n = snap.child(k).child("userName").getValue(String.class); String uid = snap.child(k).child("userId").getValue(String.class); String img = snap.child(k).child("userImage").getValue(String.class); if (n == null || n.isEmpty() || uid == null) continue; seatRow.addView(makeSeatAvatarView(n, uid, img, targetInfo, false)); } }
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
seatScroll.addView(seatRow); root.addView(seatScroll);
HorizontalScrollView catScroll = new HorizontalScrollView(this); catScroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 110));
LinearLayout catRow = new LinearLayout(this); catRow.setOrientation(LinearLayout.HORIZONTAL);
String[] cats = {"Gift", "Lucky", "CP", "Activity", "Flag", "Exclusive", "Bag"};
List<Button> catBtns = new ArrayList<>();
RecyclerView rv = new RecyclerView(this); rv.setLayoutManager(new GridLayoutManager(this, 4)); rv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 650));
final GiftModel[] holder = {getGiftsForCategory("Gift").get(0)};
Runnable refreshGifts = () -> { List<GiftModel> list = getGiftsForCategory(giftCategory); GiftAdapter ad = new GiftAdapter(list, gift -> { holder[0] = gift; }); rv.setAdapter(ad); for (Button b : catBtns) { boolean sel = b.getText().toString().equals(giftCategory); if (sel) { b.setBackgroundColor(Color.parseColor("#FFD700")); b.setTextColor(Color.BLACK); } else { b.setBackgroundColor(Color.parseColor("#2A1B4E")); b.setTextColor(Color.WHITE); } } };
for (String c : cats) { Button b = new Button(this); b.setText(c); b.setTextSize(12); b.setTypeface(null, android.graphics.Typeface.BOLD); LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(0, 0, 8, 0); b.setLayoutParams(lp); b.setOnClickListener(v -> { giftCategory = c; refreshGifts.run(); }); catRow.addView(b); catBtns.add(b); }
catScroll.addView(catRow); root.addView(catScroll); root.addView(rv); refreshGifts.run();
LinearLayout bottomBar = new LinearLayout(this); bottomBar.setOrientation(LinearLayout.HORIZONTAL); bottomBar.setGravity(Gravity.CENTER_VERTICAL); bottomBar.setPadding(0, 15, 0, 5);
walletTextInGift = new TextView(this); walletTextInGift.setText("Coins: " + formatCoins(myWalletCoins)); walletTextInGift.setTextColor(Color.parseColor("#00E676")); walletTextInGift.setTextSize(13); walletTextInGift.setTypeface(null, android.graphics.Typeface.BOLD); walletTextInGift.setPadding(14, 12, 14, 12);
GradientDrawable wBg = new GradientDrawable(); wBg.setColor(Color.parseColor("#2A1B4E")); wBg.setCornerRadius(25); walletTextInGift.setBackground(wBg);
LinearLayout.LayoutParams wLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f); walletTextInGift.setLayoutParams(wLp); bottomBar.addView(walletTextInGift);
Button btnSend = new Button(this); btnSend.setText("SEND"); btnSend.setBackgroundColor(Color.parseColor("#FF4081")); btnSend.setTextColor(Color.WHITE); btnSend.setTypeface(null, android.graphics.Typeface.BOLD); btnSend.setTextSize(13); LinearLayout.LayoutParams sendLp = new LinearLayout.LayoutParams(0, 110, 0.9f); sendLp.setMargins(8, 0, 8, 0); btnSend.setLayoutParams(sendLp); bottomBar.addView(btnSend);
Button btnQtyBottom = new Button(this); btnQtyBottom.setText("x" + giftQuantity); btnQtyBottom.setBackgroundColor(Color.parseColor("#FFD700")); btnQtyBottom.setTextColor(Color.BLACK); btnQtyBottom.setTypeface(null, android.graphics.Typeface.BOLD); btnQtyBottom.setTextSize(13); btnQtyBottom.setPadding(10, 10, 10, 10); LinearLayout.LayoutParams qLp = new LinearLayout.LayoutParams(0, 110, 0.5f); btnQtyBottom.setLayoutParams(qLp); btnQtyBottom.setOnClickListener(v -> showQuantityPopup(btnQtyBottom, holder[0])); bottomBar.addView(btnQtyBottom); root.addView(bottomBar);
btnSend.setOnClickListener(v -> {
if (holder[0] == null) return;
String priceStr = holder[0].getGiftPrice().replaceAll("[^0-9]", ""); long onePrice = 20L; if (!priceStr.isEmpty()) onePrice = Long.parseLong(priceStr);
long totalCost = onePrice * giftQuantity; long receiverGets = (long) (totalCost * EXCHANGE_RATE); if (receiverGets <= 0 && totalCost > 0) receiverGets = 1;
if (myWalletCoins < totalCost) { Toast.makeText(this, "Low balance! Need " + formatCoins(totalCost) + ", Have " + formatCoins(myWalletCoins), Toast.LENGTH_LONG).show(); return; }
boolean isSelf = !selectedGiftTargetUid.isEmpty() && !selectedGiftTargetUid.equals("All") && selectedGiftTargetUid.equals(currentUserId);
if (isSelf) handleSelfSend(totalCost, receiverGets); else { deductMyCoins(totalCost); if (selectedGiftTargetUid.equals("") || selectedGiftTargetUid.equals("All")) sendGiftToAll(receiverGets); else sendGiftToOne(selectedGiftTargetUid, receiverGets); }
triggerFlyingGiftAnimation(holder[0].getGiftName() + " x" + giftQuantity);
String msgId = messagesRef.push().getKey(); if (msgId != null) { Map<String, Object> m = new HashMap<>(); m.put("senderName", getCorrectBadgeIcon(currentUserId) + getMyDisplayName()); m.put("message", "sent gift [" + holder[0].getGiftName() + " x" + giftQuantity + " - Sent: " + formatCoins(totalCost) + " | Gets: " + formatCoins(receiverGets) + " (30%)] to " + selectedGiftTargetName); m.put("timestamp", System.currentTimeMillis()); messagesRef.child(msgId).setValue(m); }
Toast.makeText(this, "Sent: " + formatCoins(totalCost) + " | " + selectedGiftTargetName + " Gets: " + formatCoins(receiverGets) + " (30%)", Toast.LENGTH_LONG).show(); dialog.dismiss();
});
dialog.setContentView(root);
Window w = dialog.getWindow(); if (w != null) { int sh = getResources().getDisplayMetrics().heightPixels; w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (sh * 0.67)); w.setGravity(Gravity.BOTTOM); }
dialog.show();
}

private void sendGiftToAll(long receiverGets) {
databaseReference.child("seats").addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot snap) { for (int i = 0; i < 10; i++) { String k = "seat_" + i; if (!snap.hasChild(k)) continue; Long cur = snap.child(k).child("giftTotal").getValue(Long.class); long nv = (cur != null ? cur : 0L) + receiverGets; databaseReference.child("seats").child(k).child("giftTotal").setValue(nv); String sid = snap.child(k).child("userId").getValue(String.class); if (sid == null || sid.isEmpty()) continue; if (sid.equals(currentUserId)) addCoinsToSelf(sid, receiverGets); else addReceiverCoins30(sid, receiverGets); } }
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}

private void addCoinsToSelf(String sid, long gets) {
userDatabaseRef.child(sid).child("coins").addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s2) { Long cc = s2.getValue(Long.class); long nv = (cc != null ? cc : 0L) + gets; userDatabaseRef.child(sid).child("coins").setValue(nv); }
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}

private void sendGiftToOne(String targetUid, long gets) {
databaseReference.child("seats").addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot snap) {
for (int i = 0; i < 10; i++) { String k = "seat_" + i; if (!snap.hasChild(k)) continue; String sid = snap.child(k).child("userId").getValue(String.class); if (!targetUid.equals(sid)) continue; Long cur = snap.child(k).child("giftTotal").getValue(Long.class); long nv = (cur != null ? cur : 0L) + gets; databaseReference.child("seats").child(k).child("giftTotal").setValue(nv); }
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
addReceiverCoins30(targetUid, gets);
}

private View makeSeatAvatarView(String name, String uid, String imgBase64, TextView targetInfo, boolean isAll) {
LinearLayout col = new LinearLayout(this); col.setOrientation(LinearLayout.VERTICAL); col.setGravity(Gravity.CENTER); col.setPadding(12, 8, 12, 8);
ImageView av = new ImageView(this); LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(110, 110); av.setLayoutParams(ap); av.setScaleType(ImageView.ScaleType.CENTER_CROP);
if (imgBase64 != null && !imgBase64.isEmpty()) { try { byte[] b = android.util.Base64.decode(imgBase64, android.util.Base64.DEFAULT); Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length); if (bmp != null) { RoundedBitmapDrawable d = RoundedBitmapDrawableFactory.create(getResources(), bmp); d.setCircular(true); av.setImageDrawable(d); } else av.setImageResource(android.R.drawable.ic_menu_my_calendar); } catch (Exception e) { av.setImageResource(android.R.drawable.ic_menu_my_calendar); } } else av.setImageResource(android.R.drawable.ic_menu_my_calendar);
col.addView(av); TextView nt = new TextView(this); String sn = name; if (name.length() > 8) sn = name.substring(0, 8); nt.setText(sn); nt.setTextColor(Color.WHITE); nt.setTextSize(11); nt.setGravity(Gravity.CENTER); col.addView(nt);
col.setOnClickListener(v -> { if (isAll) selectedGiftTargetUid = ""; else selectedGiftTargetUid = uid; selectedGiftTargetName = name; if (targetInfo != null) targetInfo.setText("To: " + name + " - Selected"); Toast.makeText(this, "Selected: " + name, Toast.LENGTH_SHORT).show(); });
return col;
}

private List<GiftModel> getGiftsForCategory(String cat) {
List<GiftModel> list = new ArrayList<>();
if (cat.equals("Gift")) { list.add(new GiftModel("g1", "Rose", "20", R.drawable.ic_gift_flower, "Gift")); list.add(new GiftModel("g2", "Kiss", "66", R.drawable.ic_gift_ring, "Gift")); list.add(new GiftModel("g3", "Love", "100", R.drawable.ic_gift_diamond, "Gift")); list.add(new GiftModel("g4", "Cake", "199", R.drawable.ic_gift_car, "Gift")); list.add(new GiftModel("g5", "Crown", "500", R.drawable.ic_gift_crown, "Gift")); list.add(new GiftModel("g6", "Diamond", "9999", R.drawable.ic_gift_castle, "Gift")); }
else if (cat.equals("Lucky")) { list.add(new GiftModel("l1", "Lucky Star", "20", R.drawable.ic_gift_flower, "Lucky")); list.add(new GiftModel("l2", "Lucky 7", "77", R.drawable.ic_gift_ring, "Lucky")); list.add(new GiftModel("l3", "Lucky Wheel", "777", R.drawable.ic_gift_diamond, "Lucky")); }
else if (cat.equals("CP")) { list.add(new GiftModel("c1", "CP Heart", "99999", R.drawable.ic_gift_flower, "CP")); list.add(new GiftModel("c2", "CP Ring", "9999", R.drawable.ic_gift_ring, "CP")); }
else if (cat.equals("Activity")) { list.add(new GiftModel("a1", "Birthday Cake", "9999", R.drawable.ic_gift_flower, "Activity")); list.add(new GiftModel("a2", "Party", "999", R.drawable.ic_gift_car, "Activity")); }
else if (cat.equals("Flag")) { list.add(new GiftModel("f1", "Flag", "50", R.drawable.ic_gift_flower, "Flag")); list.add(new GiftModel("f2", "Country Flag", "100", R.drawable.ic_gift_ring, "Flag")); }
else if (cat.equals("Exclusive")) { list.add(new GiftModel("e1", "Exclusive Dragon", "250000", R.drawable.ic_gift_diamond, "Exclusive")); list.add(new GiftModel("e2", "Exclusive Crown", "1000000", R.drawable.ic_gift_crown, "Exclusive")); }
else if (cat.equals("Bag")) { list.add(new GiftModel("b1", "Gift Bag", "500", R.drawable.ic_gift_castle, "Bag")); list.add(new GiftModel("b2", "Luxury Bag", "5000", R.drawable.ic_gift_car, "Bag")); }
else { list.add(new GiftModel("g1", "White Tiger", "50000", R.drawable.ic_gift_flower, "Animal")); }
return list;
}

private void setupThemeBackgroundImage() {
try { ViewGroup root = findViewById(android.R.id.content); if (root == null) return; themeBackgroundImg = new ImageView(this); themeBackgroundImg.setScaleType(ImageView.ScaleType.CENTER_CROP); themeBackgroundImg.setAlpha(0.85f); FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); root.addView(themeBackgroundImg, 0); if (root.getChildCount() > 1) root.getChildAt(1).bringToFront(); } catch (Exception ignored) {}
}

private void applyRoomTheme(String c1, String c2, String fullText, String imageName) {
try { currentThemeName = fullText; int resId = getResources().getIdentifier(imageName, "drawable", getPackageName()); if (themeBackgroundImg != null) { if (resId != 0) { themeBackgroundImg.setImageResource(resId); themeBackgroundImg.setVisibility(View.VISIBLE); } else { themeBackgroundImg.setVisibility(View.GONE); View rv = findViewById(android.R.id.content); if (rv != null) { GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor(c1), Color.parseColor(c2)}); rv.setBackground(bg); } } } } catch (Exception ignored) {}
}

private void listenToRoomTheme() {
roomMetaRef.addValueEventListener(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) { String n = s.child("roomTheme").getValue(String.class); String c1 = s.child("roomThemeColor1").getValue(String.class); String c2 = s.child("roomThemeColor2").getValue(String.class); String img = s.child("roomThemeImage").getValue(String.class); if (n != null && !n.isEmpty() && c1 != null && c2 != null) { String im = "theme_night"; if (img != null) im = img; applyRoomTheme(c1, c2, n, im); } }
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}

private void showRoomThemeDialog() { Toast.makeText(this, "Theme - Coming Soon", Toast.LENGTH_SHORT).show(); }

private void showIconToolsDialog(boolean isOwner) {
BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(35, 35, 35, 45);
GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}); bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0}); root.setBackground(bg);
TextView title = new TextView(this); title.setText("Basic Tools"); title.setTextColor(Color.parseColor("#FFD700")); title.setTextSize(18); title.setTypeface(null, android.graphics.Typeface.BOLD); title.setPadding(0, 0, 0, 15); root.addView(title);
Button editNameBtn = new Button(this); editNameBtn.setText("Edit My Profile"); editNameBtn.setBackgroundColor(Color.parseColor("#FFD700")); editNameBtn.setTextColor(Color.BLACK); editNameBtn.setOnClickListener(v -> { dialog.dismiss(); showEditMyProfileDialog(); }); root.addView(editNameBtn);
GridLayout grid = new GridLayout(this); grid.setColumnCount(4); grid.setUseDefaultMargins(true);
String[][] basic = {{"\uD83D\uDD12", "Lock", "Lock off"}, {"\u2699\uFE0F", "Set", "Room set"}, {"\u270F\uFE0F", "Edit", "Room Edit"}, {"\uD83E\uDDF9", "Clear", "Clear charm"}, {"\uD83D\uDCAC", "Chat", "Clear chat"}, {"\uD83C\uDFA4", "Mic", "Change mic"}, {"\uD83C\uDFA8", "Theme", "Mic Theme"}, {"\uD83D\uDC57", "Dress", "Dress"}, {"\uD83C\uDF40", "Lucky", "Lucky Sound"}, {"\uD83D\uDC65", "Voice", "Voice User"}, {"\uD83D\uDEAA", "Kick", "Kick out record"}, {"\uD83C\uDFC6", "Reward", "Reward Event"}};
for (String[] it : basic) { LinearLayout cell = makeToolIconCell(it[0], it[1], it[2], v -> { dialog.dismiss(); handleToolClick(it[2], isOwner); }); grid.addView(cell); }
root.addView(grid); dialog.setContentView(root); Window w = dialog.getWindow(); if (w != null) { w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1400); w.setGravity(Gravity.BOTTOM); } dialog.show();
}

private LinearLayout makeToolIconCell(String icon, String title, String key, View.OnClickListener l) {
LinearLayout cell = new LinearLayout(this); cell.setOrientation(LinearLayout.VERTICAL); cell.setGravity(Gravity.CENTER); cell.setPadding(10, 18, 10, 18); cell.setOnClickListener(l);
GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.parseColor("#2A1B4E")); bg.setCornerRadius(18); cell.setBackground(bg);
GridLayout.LayoutParams lp = new GridLayout.LayoutParams(); lp.width = 0; lp.height = ViewGroup.LayoutParams.WRAP_CONTENT; lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); lp.setMargins(6, 6, 6, 6); cell.setLayoutParams(lp);
TextView ic = new TextView(this); ic.setText(icon); ic.setTextSize(28); ic.setGravity(Gravity.CENTER); cell.addView(ic);
TextView lb = new TextView(this); lb.setText(title); lb.setTextColor(Color.WHITE); lb.setTextSize(14); lb.setGravity(Gravity.CENTER); lb.setTypeface(null, android.graphics.Typeface.BOLD); cell.addView(lb);
TextView sub = new TextView(this); sub.setText(key); sub.setTextColor(Color.parseColor("#B0A8D0")); sub.setTextSize(10); sub.setGravity(Gravity.CENTER); cell.addView(sub);
return cell;
}

private void handleToolClick(String tool, boolean isOwner) {
switch (tool) {
case "Lock off": showRoomLockPasswordDialog(); break;
case "Room set": if (isOwner) showEditRoomDialog(); else Toast.makeText(this, "Only Owner can edit", Toast.LENGTH_SHORT).show(); break;
case "Room Edit": if (isOwner) showEditRoomDialog(); else Toast.makeText(this, "Only Owner", Toast.LENGTH_SHORT).show(); break;
case "Clear charm": clearCharm(); break;
case "Clear chat": messagesRef.removeValue(); chatMessages.clear(); chatAdapter.notifyDataSetChanged(); Toast.makeText(this, "Chat Cleared", Toast.LENGTH_SHORT).show(); break;
case "Change mic": Toast.makeText(this, "Change Mic - Select Seat", Toast.LENGTH_SHORT).show(); break;
case "Mic Theme": showRoomThemeDialog(); break;
case "Dress": Toast.makeText(this, "Dress - Coming", Toast.LENGTH_SHORT).show(); break;
case "Lucky Sound": Toast.makeText(this, "Lucky Sound Played", Toast.LENGTH_SHORT).show(); break;
case "Voice User": showScrollableOnlineMembersDialog(); break;
case "Kick out record": showScrollableOnlineMembersDialog(); break;
case "Reward Event": showRewardEventDialog(); break;
}
}

private void clearCharm() {
databaseReference.child("seats").addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) { for (int i = 0; i < 10; i++) databaseReference.child("seats").child("seat_" + i).child("giftTotal").setValue(0L); Toast.makeText(VoiceRoomActivity.this, "Charm Cleared", Toast.LENGTH_SHORT).show(); }
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}

private long calcReward(long contribution) { if (contribution >= 3000000 && contribution < 10000000) return (long) (contribution * 0.05); if (contribution >= 10000000) return (long) (contribution * 0.10); return 0L; }

private void showRewardEventDialog() {
BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(35, 35, 35, 45);
GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#0A0518"), Color.parseColor("#1B1035")}); bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0}); root.setBackground(bg);
TextView title = new TextView(this); title.setText("Room Reward Event"); title.setTextColor(Color.parseColor("#FFD700")); title.setTextSize(20); title.setTypeface(null, android.graphics.Typeface.BOLD); title.setGravity(Gravity.CENTER); title.setPadding(0, 0, 0, 8); root.addView(title);
TextView rule = new TextView(this); rule.setText("3M - 10M = 5%  |  10M+ = 10%"); rule.setTextColor(Color.WHITE); rule.setTextSize(13); rule.setGravity(Gravity.CENTER); rule.setPadding(0, 0, 0, 10); root.addView(rule);
TextView weeklyTxt = new TextView(this); weeklyTxt.setText("Weekly Contribution - Loading..."); weeklyTxt.setTextColor(Color.WHITE); weeklyTxt.setTextSize(16); weeklyTxt.setTypeface(null, android.graphics.Typeface.BOLD); weeklyTxt.setGravity(Gravity.CENTER); weeklyTxt.setPadding(0, 15, 0, 8); root.addView(weeklyTxt);
TextView availTxt = new TextView(this); availTxt.setText("Available Reward: 0"); availTxt.setTextColor(Color.parseColor("#00E676")); availTxt.setTextSize(15); availTxt.setTypeface(null, android.graphics.Typeface.BOLD); availTxt.setGravity(Gravity.CENTER); availTxt.setPadding(0, 0, 0, 15); root.addView(availTxt);
Button btnClaim = new Button(this); btnClaim.setText("Claim Rewards"); btnClaim.setBackgroundColor(Color.parseColor("#FF4081")); btnClaim.setTextColor(Color.WHITE); btnClaim.setOnClickListener(v -> Toast.makeText(this, "Claimed!", Toast.LENGTH_SHORT).show()); root.addView(btnClaim);
ScrollView scroll = new ScrollView(this); scroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 700)); scroll.setFillViewport(true);
LinearLayout table = new LinearLayout(this); table.setOrientation(LinearLayout.VERTICAL); table.setPadding(0, 20, 0, 0);
long[] tiers = {3000000L, 6000000L, 10000000L, 14000000L, 20000000L, 30000000L, 40000000L, 50000000L, 70000000L, 90000000L, 100000000L};
for (long t : tiers) { TextView row = new TextView(this); String rt = formatCoins(t) + "  ->  " + formatCoins(calcReward(t)); if (t < 10000000) rt += " (5%)"; else rt += " (10%)"; row.setText(rt); row.setTextColor(Color.WHITE); row.setTextSize(14); row.setGravity(Gravity.CENTER); row.setPadding(0, 8, 0, 8); table.addView(row); }
scroll.addView(table); root.addView(scroll);
databaseReference.child("seats").addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) { long total = 0; for (DataSnapshot ds : s.getChildren()) { Long g = ds.child("giftTotal").getValue(Long.class); if (g != null) total += g; } weeklyTxt.setText("Weekly Contribution - " + formatCoins(total)); availTxt.setText("Available Reward: " + formatCoins(calcReward(total))); }
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
dialog.setContentView(root); Window w = dialog.getWindow(); if (w != null) { w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1500); w.setGravity(Gravity.BOTTOM); } dialog.show();
}

private void showChatDialog() {
final Dialog chatDialog = new Dialog(this, R.style.Theme_Design_BottomSheetDialog); chatDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); chatDialog.setContentView(R.layout.dialog_bottom_chat);
Window window = chatDialog.getWindow(); if (window != null) { window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); window.setGravity(Gravity.BOTTOM); window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); }
EditText inputMsg = chatDialog.findViewById(R.id.editRoomMessage); Button btnSendMsg = chatDialog.findViewById(R.id.btnSendRoomMessage);
if (btnSendMsg != null && inputMsg != null) { btnSendMsg.setOnClickListener(view -> { String message = inputMsg.getText().toString().trim(); if (!message.isEmpty()) { String msgId = messagesRef.push().getKey(); if (msgId != null) { Map<String, Object> m = new HashMap<>(); m.put("senderName", getCorrectBadgeIcon(currentUserId) + getMyDisplayName()); m.put("senderId", currentUserId); m.put("message", message); m.put("timestamp", System.currentTimeMillis()); messagesRef.child(msgId).setValue(m); } inputMsg.setText(""); chatDialog.dismiss(); } }); }
chatDialog.show();
}

private void showPremiumRankingDialog() {
BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(30, 30, 30, 30);
GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}); bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0}); root.setBackground(bg);
TextView title = new TextView(this); title.setText("Room Contribution Ranking"); title.setTextColor(Color.parseColor("#FFD700")); title.setTextSize(18); title.setTypeface(null, android.graphics.Typeface.BOLD); title.setGravity(Gravity.CENTER); title.setPadding(0, 0, 0, 15); root.addView(title);
LinearLayout typeRow = new LinearLayout(this); typeRow.setOrientation(LinearLayout.HORIZONTAL); typeRow.setGravity(Gravity.CENTER); Button bWealth = new Button(this); bWealth.setText("Wealth"); Button bCharm = new Button(this); bCharm.setText("Charm"); typeRow.addView(bWealth); typeRow.addView(bCharm); root.addView(typeRow);
LinearLayout tabRow = new LinearLayout(this); tabRow.setOrientation(LinearLayout.HORIZONTAL); tabRow.setGravity(Gravity.CENTER); tabRow.setPadding(0, 12, 0, 12); Button bDay = new Button(this); bDay.setText("Daily"); Button bWeek = new Button(this); bWeek.setText("Weekly"); Button bMonth = new Button(this); bMonth.setText("Monthly"); tabRow.addView(bDay); tabRow.addView(bWeek); tabRow.addView(bMonth); root.addView(tabRow);
LinearLayout podium = new LinearLayout(this); podium.setOrientation(LinearLayout.HORIZONTAL); podium.set
