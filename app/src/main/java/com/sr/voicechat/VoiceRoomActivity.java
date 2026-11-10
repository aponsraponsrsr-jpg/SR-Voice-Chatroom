package com.sr.voicechat;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
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
private TextView roomTitleText, roomInfoText, textOnlineCount;
private TextView roomNoticeBarText;
private ImageView btnOnlineList, roomHeaderProfileImg;
private ImageView btnRoomTools, themeBackgroundImg;
private LinearLayout btnInRoomRanking;
private boolean isMuted = false;
private ListView chatListView;
private ArrayList<String> chatMessages;
private ArrayAdapter<String> chatAdapter;
private SharedPreferences prefs;
private DatabaseReference databaseReference, roomMetaRef;
private DatabaseReference userDatabaseRef, messagesRef, adminsRef;
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
private String myLanguage = "English";
private String getMyDisplayName() {
String n = prefs.getString("USER_NAME", "");
if (n != null && !n.isEmpty()) return n;
if (myDisplayName != null && !myDisplayName.isEmpty()) {
return myDisplayName;
}
return "User";
}
private void saveMyNameGlobal(String newName) {
if (newName == null) return;
newName = newName.trim();
if (newName.isEmpty()) return;
myDisplayName = newName;
prefs.edit().putString("USER_NAME", newName).apply();
userDatabaseRef.child(currentUserId)
.child("name").setValue(newName);
databaseReference.child("online_users")
.child(currentUserId).setValue(newName);
for (int i = 0; i < 10; i++) {
final int idx = i;
databaseReference.child("seats").child("seat_" + idx)
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
String uid = s.child("userId").getValue(String.class);
if (uid != null && uid.equals(currentUserId)) {
databaseReference.child("seats")
.child("seat_" + idx)
.child("userName").setValue(newName);
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
}
private void showEditMyProfileDialog() {
AlertDialog.Builder b = new AlertDialog.Builder(this);
b.setTitle("Edit Profile - Global Name");
LinearLayout layout = new LinearLayout(this);
layout.setOrientation(LinearLayout.VERTICAL);
layout.setPadding(50, 30, 50, 30);
TextView hint = new TextView(this);
hint.setText("Type any name. It will show live.");
hint.setTextSize(13);
hint.setPadding(0, 0, 0, 15);
layout.addView(hint);
EditText input = new EditText(this);
input.setHint("Enter your name...");
input.setText(getMyDisplayName());
input.setTextColor(Color.BLACK);
input.setBackgroundColor(Color.WHITE);
input.setPadding(25, 20, 25, 20);
layout.addView(input);
b.setView(layout);
b.setPositiveButton("Save", (d, w) -> {
String nn = input.getText().toString().trim();
if (!nn.isEmpty()) {
saveMyNameGlobal(nn);
String m = "Name saved: ";
m += nn;
Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
}
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
roomOwnerId = getIntent().getStringExtra("ROOM_OWNER_ID");
if (roomOwnerId == null || roomOwnerId.isEmpty()) {
roomOwnerId = currentUserId;
}
roomJoinTimestamp = System.currentTimeMillis();
databaseReference = FirebaseDatabase.getInstance()
.getReference("rooms").child("room_" + roomOwnerId);
roomMetaRef = databaseReference.child("meta");
messagesRef = databaseReference.child("messages");
adminsRef = databaseReference.child("admins");
userDatabaseRef = FirebaseDatabase.getInstance()
.getReference("users");
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
if (btnPower != null) {
btnPower.setOnClickListener(v -> showExitDialog());
}
if (btnRoomTools != null) {
btnRoomTools.setOnClickListener(v -> {
boolean isOwner = currentUserId.equals(roomOwnerId);
boolean isAdmin = roomAdminList.contains(currentUserId);
if (!isOwner && !isAdmin) {
Toast.makeText(this,
"Only Owner / Admin can access Tools!",
Toast.LENGTH_SHORT).show();
return;
}
showIconToolsDialog(isOwner);
});
}
if (btnInRoomRanking != null) {
btnInRoomRanking.setOnClickListener(
v -> showPremiumRankingDialog());
}
View.OnClickListener headerClick =
v -> showPremiumRoomInfoAndMembersDialog();
if (roomTitleText != null) {
roomTitleText.setOnClickListener(headerClick);
}
if (roomHeaderProfileImg != null) {
roomHeaderProfileImg.setOnClickListener(headerClick);
}
if (roomInfoText != null) {
roomInfoText.setOnClickListener(headerClick);
}
if (btnOnlineList != null) {
btnOnlineList.setOnClickListener(
v -> showScrollableOnlineMembersDialog());
}
chatListView = findViewById(R.id.chatListView);
chatMessages = new ArrayList<>();
chatAdapter = new ArrayAdapter<>(this,
android.R.layout.simple_list_item_1, chatMessages);
if (chatListView != null) {
chatListView.setAdapter(chatAdapter);
}
setupSeatInteractions();
listenToFirebaseSeats();
listenToOnlineMembers();
listenToRealtimeChatMessages();
btnMicToggle = findViewById(R.id.btnMicToggle);
if (btnMicToggle != null) {
btnMicToggle.setOnClickListener(v -> {
isMuted = !isMuted;
btnMicToggle.setText(isMuted ? "Muted" : "Mic");
});
}
btnChatOption = findViewById(R.id.btnChatOption);
if (btnChatOption != null) {
btnChatOption.setOnClickListener(v -> showChatDialog());
}
btnGiftBox = findViewById(R.id.btnGiftBox);
if (btnGiftBox != null) {
btnGiftBox.setOnClickListener(v -> showNewGiftPanel());
}
btnGameBox = findViewById(R.id.btnGameBox);
if (btnGameBox != null) {
btnGameBox.setOnClickListener(
v -> showRewardEventDialog());
}
}
@Override public void onBackPressed() { showExitDialog(); }
private void showExitDialog() {
AlertDialog.Builder b = new AlertDialog.Builder(this);
b.setTitle("Exit Room?");
b.setMessage("Exit room or Minimize to Home?");
b.setPositiveButton("Exit Room", (d, w) -> {
leaveRoomAndFinish(); finishAffinity();
});
b.setNeutralButton("Minimize", (d, w) -> {
leaveRoomKeepHome(); finish();
});
b.setNegativeButton("Cancel", (d, w) -> d.dismiss());
b.show();
}
private void leaveRoomKeepHome() {
for (int i = 0; i < 10; i++) {
final int idx = i;
databaseReference.child("seats").child("seat_" + idx)
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
String uid = s.child("userId").getValue(String.class);
if (uid != null && uid.equals(currentUserId)) {
databaseReference.child("seats")
.child("seat_" + idx).removeValue();
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
}
private void leaveRoomAndFinish() {
databaseReference.child("online_users")
.child(currentUserId).removeValue();
leaveRoomKeepHome(); finish();
}
private void listenToMyWallet() {
if (currentUserId.isEmpty()) return;
userDatabaseRef.child(currentUserId)
.addValueEventListener(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
Long coins = s.child("coins").getValue(Long.class);
Long avail = s.child("availableBalance").getValue(Long.class);
Long bal = s.child("balance").getValue(Long.class);
Long oldW = s.child("walletCoins").getValue(Long.class);
long w = 0L;
if (coins != null) w = coins;
else if (avail != null) w = avail;
else if (bal != null) w = bal;
else if (oldW != null) w = oldW;
myWalletCoins = w;
if (walletTextInGift != null) {
String t = "Coins: ";
t += formatCoins(w);
walletTextInGift.setText(t);
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
private String formatCoins(long v) {
return String.format("%,d", v);
}
private void deductMyCoins(long totalCost) {
userDatabaseRef.child(currentUserId).child("coins")
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
Long cur = s.getValue(Long.class);
long now = (cur != null ? cur : 0L) - totalCost;
if (now < 0) now = 0;
userDatabaseRef.child(currentUserId)
.child("coins").setValue(now);
userDatabaseRef.child(currentUserId)
.child("walletCoins").setValue(now);
userDatabaseRef.child(currentUserId)
.child("availableBalance").setValue(now);
myWalletCoins = now;
if (walletTextInGift != null) {
String t = "Coins: ";
t += formatCoins(now);
walletTextInGift.setText(t);
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
userDatabaseRef.child(currentUserId).child("sendingCoins")
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
Long cur = s.getValue(Long.class);
long v = (cur != null ? cur : 0L) + totalCost;
userDatabaseRef.child(currentUserId)
.child("sendingCoins").setValue(v);
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
private void addReceiverCoins30(String targetUid, long gets) {
userDatabaseRef.child(targetUid).child("coins")
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
Long cur = s.getValue(Long.class);
long now = (cur != null ? cur : 0L) + gets;
userDatabaseRef.child(targetUid).child("coins").setValue(now);
userDatabaseRef.child(targetUid)
.child("walletCoins").setValue(now);
userDatabaseRef.child(targetUid)
.child("availableBalance").setValue(now);
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
userDatabaseRef.child(targetUid).child("receivingCoins")
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
Long cur = s.getValue(Long.class);
long v = (cur != null ? cur : 0L) + gets;
userDatabaseRef.child(targetUid)
.child("receivingCoins").setValue(v);
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
private void handleSelfSend(long totalCost, long gets) {
userDatabaseRef.child(currentUserId).child("coins")
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
Long cur = s.getValue(Long.class);
long base = (cur != null ? cur : 0L);
long now = base - totalCost + gets;
if (now < 0) now = 0;
userDatabaseRef.child(currentUserId)
.child("coins").setValue(now);
userDatabaseRef.child(currentUserId)
.child("walletCoins").setValue(now);
userDatabaseRef.child(currentUserId)
.child("availableBalance").setValue(now);
myWalletCoins = now;
if (walletTextInGift != null) {
String t = "Coins: ";
t += formatCoins(now);
walletTextInGift.setText(t);
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
userDatabaseRef.child(currentUserId).child("sendingCoins")
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
Long cur = s.getValue(Long.class);
long v = (cur != null ? cur : 0L) + totalCost;
userDatabaseRef.child(currentUserId)
.child("sendingCoins").setValue(v);
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
userDatabaseRef.child(currentUserId).child("receivingCoins")
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
Long cur = s.getValue(Long.class);
long v = (cur != null ? cur : 0L) + gets;
userDatabaseRef.child(currentUserId)
.child("receivingCoins").setValue(v);
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
private void applyFutureDollarPolicy(
String receiverUid, long gets, long total) {
}
private void showQuantityPopup(
Button btnQtyPopup, GiftModel selectedGift) {
AlertDialog.Builder qb = new AlertDialog.Builder(this);
qb.setTitle("Select Quantity (1 to 10)");
LinearLayout layout = new LinearLayout(this);
layout.setOrientation(LinearLayout.VERTICAL);
layout.setPadding(40, 30, 40, 20);
TextView label = new TextView(this);
label.setText("Select 1, 3, 5, 10 or type below");
label.setTextSize(13);
label.setGravity(Gravity.CENTER);
label.setPadding(0, 0, 0, 15);
layout.addView(label);
LinearLayout row = new LinearLayout(this);
row.setOrientation(LinearLayout.HORIZONTAL);
row.setGravity(Gravity.CENTER);
int[] presets = {1, 3, 5, 10};
for (int q : presets) {
Button b = new Button(this);
b.setText("x" + q);
b.setTypeface(null, android.graphics.Typeface.BOLD);
LinearLayout.LayoutParams lp =
new LinearLayout.LayoutParams(0,
ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
lp.setMargins(6, 0, 6, 0);
b.setLayoutParams(lp);
if (q == giftQuantity) {
b.setBackgroundColor(Color.parseColor("#FFD700"));
b.setTextColor(Color.BLACK);
} else {
b.setBackgroundColor(Color.parseColor("#2A1B4E"));
b.setTextColor(Color.WHITE);
}
row.addView(b);
}
layout.addView(row);
TextView cl = new TextView(this);
cl.setText("Custom - Type yourself (1 to 10):");
cl.setTextSize(13);
cl.setPadding(0, 20, 0, 8);
layout.addView(cl);
EditText inputCustom = new EditText(this);
inputCustom.setHint("Ex: 2, 4, 6, 7, 8, 9");
inputCustom.setInputType(
android.text.InputType.TYPE_CLASS_NUMBER);
inputCustom.setTextColor(Color.BLACK);
inputCustom.setBackgroundColor(Color.WHITE);
inputCustom.setPadding(25, 20, 25, 20);
layout.addView(inputCustom);
qb.setView(layout);
qb.setPositiveButton("OK - Custom Apply", (d, w) -> {
String txt = inputCustom.getText().toString().trim();
if (!txt.isEmpty()) {
try {
int cq = Integer.parseInt(txt);
if (cq < 1) cq = 1;
if (cq > 10) {
Toast.makeText(this, "Max 10 allowed!",
Toast.LENGTH_SHORT).show();
cq = 10;
}
giftQuantity = cq;
btnQtyPopup.setText("x" + giftQuantity);
} catch (Exception e) {
Toast.makeText(this, "Enter correct number!",
Toast.LENGTH_SHORT).show();
}
}
});
qb.setNegativeButton("Cancel", (d, w) -> d.dismiss());
AlertDialog dialog = qb.create();
dialog.show();
for (int i = 0; i < row.getChildCount(); i++) {
View child = row.getChildAt(i);
if (child instanceof Button) {
Button bb = (Button) child;
bb.setOnClickListener(v -> {
String t = bb.getText().toString().replace("x", "");
try { giftQuantity = Integer.parseInt(t); }
catch (Exception ignored) {}
btnQtyPopup.setText("x" + giftQuantity);
dialog.dismiss();
});
}
}
}
private void showNewGiftPanel() {
BottomSheetDialog dialog = new BottomSheetDialog(this,
R.style.Theme_Design_BottomSheetDialog);
LinearLayout root = new LinearLayout(this);
root.setOrientation(LinearLayout.VERTICAL);
root.setPadding(30, 20, 30, 20);
GradientDrawable bg = new GradientDrawable(
GradientDrawable.Orientation.TOP_BOTTOM,
new int[]{Color.parseColor("#1B1035"),
Color.parseColor("#0A0518")});
bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
root.setBackground(bg);
TextView title = new TextView(this);
title.setText("Gift Center");
title.setTextColor(Color.parseColor("#FFD700"));
title.setTextSize(18);
title.setTypeface(null, android.graphics.Typeface.BOLD);
title.setGravity(Gravity.CENTER);
title.setPadding(0, 0, 0, 12);
root.addView(title);
TextView targetInfo = new TextView(this);
String ti = "To: ";
ti += selectedGiftTargetName;
ti += " - Tap a seat user below";
targetInfo.setText(ti);
targetInfo.setTextColor(Color.WHITE);
targetInfo.setTextSize(13);
targetInfo.setGravity(Gravity.CENTER);
targetInfo.setPadding(0, 0, 0, 10);
root.addView(targetInfo);
HorizontalScrollView seatScroll = new HorizontalScrollView(this);
seatScroll.setLayoutParams(new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT, 180));
LinearLayout seatRow = new LinearLayout(this);
seatRow.setOrientation(LinearLayout.HORIZONTAL);
seatRow.setGravity(Gravity.CENTER_VERTICAL);
seatRow.addView(makeSeatAvatarView(
"All", "All", null, targetInfo, true));
databaseReference.child("seats")
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot snap) {
for (int i = 0; i < 10; i++) {
String k = "seat_" + i;
if (!snap.hasChild(k)) continue;
String n = snap.child(k).child("userName")
.getValue(String.class);
String uid = snap.child(k).child("userId")
.getValue(String.class);
String img = snap.child(k).child("userImage")
.getValue(String.class);
if (n == null || n.isEmpty() || uid == null) continue;
seatRow.addView(makeSeatAvatarView(
n, uid, img, targetInfo, false));
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
seatScroll.addView(seatRow);
root.addView(seatScroll);
HorizontalScrollView catScroll = new HorizontalScrollView(this);
catScroll.setLayoutParams(new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT, 110));
LinearLayout catRow = new LinearLayout(this);
catRow.setOrientation(LinearLayout.HORIZONTAL);
String[] cats = {"Gift", "Lucky", "CP",
"Activity", "Flag", "Exclusive", "Bag"};
List<Button> catBtns = new ArrayList<>();
RecyclerView rv = new RecyclerView(this);
rv.setLayoutManager(new GridLayoutManager(this, 4));
rv.setLayoutParams(new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT, 650));
final GiftModel[] holder =
{getGiftsForCategory("Gift").get(0)};
Runnable refreshGifts = () -> {
List<GiftModel> list = getGiftsForCategory(giftCategory);
GiftAdapter ad = new GiftAdapter(list, gift -> {
holder[0] = gift;
});
rv.setAdapter(ad);
for (Button b : catBtns) {
boolean sel = b.getText().toString()
.equals(giftCategory);
if (sel) {
b.setBackgroundColor(Color.parseColor("#FFD700"));
b.setTextColor(Color.BLACK);
} else {
b.setBackgroundColor(Color.parseColor("#2A1B4E"));
b.setTextColor(Color.WHITE);
}
}
};
for (String c : cats) {
Button b = new Button(this);
b.setText(c);
b.setTextSize(12);
b.setTypeface(null, android.graphics.Typeface.BOLD);
LinearLayout.LayoutParams lp =
new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.WRAP_CONTENT,
ViewGroup.LayoutParams.WRAP_CONTENT);
lp.setMargins(0, 0, 8, 0);
b.setLayoutParams(lp);
b.setOnClickListener(v -> {
giftCategory = c; refreshGifts.run();
});
catRow.addView(b); catBtns.add(b);
}
catScroll.addView(catRow);
root.addView(catScroll);
root.addView(rv);
refreshGifts.run();
LinearLayout bottomBar = new LinearLayout(this);
bottomBar.setOrientation(LinearLayout.HORIZONTAL);
bottomBar.setGravity(Gravity.CENTER_VERTICAL);
bottomBar.setPadding(0, 15, 0, 5);
walletTextInGift = new TextView(this);
String wTxt = "Coins: ";
wTxt += formatCoins(myWalletCoins);
walletTextInGift.setText(wTxt);
walletTextInGift.setTextColor(Color.parseColor("#00E676"));
walletTextInGift.setTextSize(13);
walletTextInGift.setTypeface(null,
android.graphics.Typeface.BOLD);
walletTextInGift.setPadding(14, 12, 14, 12);
GradientDrawable wBg = new GradientDrawable();
wBg.setColor(Color.parseColor("#2A1B4E"));
wBg.setCornerRadius(25);
walletTextInGift.setBackground(wBg);
LinearLayout.LayoutParams wLp =
new LinearLayout.LayoutParams(0,
ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f);
walletTextInGift.setLayoutParams(wLp);
bottomBar.addView(walletTextInGift);
Button btnSend = new Button(this);
btnSend.setText("SEND");
btnSend.setBackgroundColor(Color.parseColor("#FF4081"));
btnSend.setTextColor(Color.WHITE);
btnSend.setTypeface(null, android.graphics.Typeface.BOLD);
btnSend.setTextSize(13);
LinearLayout.LayoutParams sendLp =
new LinearLayout.LayoutParams(0, 110, 0.9f);
sendLp.setMargins(8, 0, 8, 0);
btnSend.setLayoutParams(sendLp);
bottomBar.addView(btnSend);
Button btnQtyBottom = new Button(this);
btnQtyBottom.setText("x" + giftQuantity);
btnQtyBottom.setBackgroundColor(Color.parseColor("#FFD700"));
btnQtyBottom.setTextColor(Color.BLACK);
btnQtyBottom.setTypeface(null, android.graphics.Typeface.BOLD);
btnQtyBottom.setTextSize(13);
btnQtyBottom.setPadding(10, 10, 10, 10);
LinearLayout.LayoutParams qLp =
new LinearLayout.LayoutParams(0, 110, 0.5f);
btnQtyBottom.setLayoutParams(qLp);
btnQtyBottom.setOnClickListener(
v -> showQuantityPopup(btnQtyBottom, holder[0]));
bottomBar.addView(btnQtyBottom);
root.addView(bottomBar);
btnSend.setOnClickListener(v -> {
if (holder[0] == null) return;
String priceStr = holder[0].getGiftPrice()
.replaceAll("[^0-9]", "");
long onePrice = 20L;
if (!priceStr.isEmpty()) {
onePrice = Long.parseLong(priceStr);
}
long totalCost = onePrice * giftQuantity;
long receiverGets = (long) (totalCost * EXCHANGE_RATE);
if (receiverGets <= 0 && totalCost > 0) receiverGets = 1;
if (myWalletCoins < totalCost) {
String low = "Low balance! Need ";
low += formatCoins(totalCost);
low += ", Have ";
low += formatCoins(myWalletCoins);
Toast.makeText(this, low, Toast.LENGTH_LONG).show();
return;
}
boolean isSelf = !selectedGiftTargetUid.isEmpty()
&& !selectedGiftTargetUid.equals("All")
&& selectedGiftTargetUid.equals(currentUserId);
if (isSelf) {
handleSelfSend(totalCost, receiverGets);
} else {
deductMyCoins(totalCost);
if (selectedGiftTargetUid.equals("")
|| selectedGiftTargetUid.equals("All")) {
sendGiftToAll(receiverGets);
} else {
sendGiftToOne(selectedGiftTargetUid, receiverGets);
}
}
String an = holder[0].getGiftName();
an += " x";
an += giftQuantity;
triggerFlyingGiftAnimation(an);
String msgId = messagesRef.push().getKey();
if (msgId != null) {
Map<String, Object> m = new HashMap<>();
String sn = getCorrectBadgeIcon(currentUserId);
sn += getMyDisplayName();
m.put("senderName", sn);
String giftMsg = "sent gift [";
giftMsg += holder[0].getGiftName();
giftMsg += " x";
giftMsg += giftQuantity;
giftMsg += " - Sent: ";
giftMsg += formatCoins(totalCost);
giftMsg += " | Gets: ";
giftMsg += formatCoins(receiverGets);
giftMsg += " (30%)] to ";
giftMsg += selectedGiftTargetName;
m.put("message", giftMsg);
m.put("timestamp", System.currentTimeMillis());
messagesRef.child(msgId).setValue(m);
}
String toastMsg = "Sent: ";
toastMsg += formatCoins(totalCost);
toastMsg += " | ";
toastMsg += selectedGiftTargetName;
toastMsg += " Gets: ";
toastMsg += formatCoins(receiverGets);
toastMsg += " (30%)";
Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
dialog.dismiss();
});
dialog.setContentView(root);
Window w = dialog.getWindow();
if (w != null) {
int sh = getResources().getDisplayMetrics().heightPixels;
w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
(int) (sh * 0.67));
w.setGravity(Gravity.BOTTOM);
}
dialog.show();
}
private void sendGiftToAll(long receiverGets) {
databaseReference.child("seats")
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot snap) {
for (int i = 0; i < 10; i++) {
String k = "seat_" + i;
if (!snap.hasChild(k)) continue;
Long cur = snap.child(k).child("giftTotal")
.getValue(Long.class);
long nv = (cur != null ? cur : 0L) + receiverGets;
databaseReference.child("seats").child(k)
.child("giftTotal").setValue(nv);
String sid = snap.child(k).child("userId")
.getValue(String.class);
if (sid == null || sid.isEmpty()) continue;
if (sid.equals(currentUserId)) {
addCoinsToSelf(sid, receiverGets);
} else {
addReceiverCoins30(sid, receiverGets);
}
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
private void addCoinsToSelf(String sid, long gets) {
userDatabaseRef.child(sid).child("coins")
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s2) {
Long cc = s2.getValue(Long.class);
long nv = (cc != null ? cc : 0L) + gets;
userDatabaseRef.child(sid).child("coins").setValue(nv);
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
private void sendGiftToOne(String targetUid, long gets) {
databaseReference.child("seats")
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot snap) {
for (int i = 0; i < 10; i++) {
String k = "seat_" + i;
if (!snap.hasChild(k)) continue;
String sid = snap.child(k).child("userId")
.getValue(String.class);
if (!targetUid.equals(sid)) continue;
Long cur = snap.child(k).child("giftTotal")
.getValue(Long.class);
long nv = (cur != null ? cur : 0L) + gets;
databaseReference.child("seats").child(k)
.child("giftTotal").setValue(nv);
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
addReceiverCoins30(targetUid, gets);
}
private View makeSeatAvatarView(String name, String uid,
String imgBase64, TextView targetInfo, boolean isAll) {
LinearLayout col = new LinearLayout(this);
col.setOrientation(LinearLayout.VERTICAL);
col.setGravity(Gravity.CENTER);
col.setPadding(12, 8, 12, 8);
ImageView av = new ImageView(this);
LinearLayout.LayoutParams ap =
new LinearLayout.LayoutParams(110, 110);
av.setLayoutParams(ap);
av.setScaleType(ImageView.ScaleType.CENTER_CROP);
if (imgBase64 != null && !imgBase64.isEmpty()) {
try {
byte[] b = android.util.Base64.decode(
imgBase64, android.util.Base64.DEFAULT);
Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
if (bmp != null) {
RoundedBitmapDrawable d =
RoundedBitmapDrawableFactory.create(getResources(), bmp);
d.setCircular(true);
av.setImageDrawable(d);
} else {
av.setImageResource(android.R.drawable.ic_menu_my_calendar);
}
} catch (Exception e) {
av.setImageResource(android.R.drawable.ic_menu_my_calendar);
}
} else {
av.setImageResource(android.R.drawable.ic_menu_my_calendar);
}
col.addView(av);
TextView nt = new TextView(this);
String shortName = name;
if (name.length() > 8) shortName = name.substring(0, 8);
nt.setText(shortName);
nt.setTextColor(Color.WHITE);
nt.setTextSize(11);
nt.setGravity(Gravity.CENTER);
col.addView(nt);
col.setOnClickListener(v -> {
if (isAll) selectedGiftTargetUid = "";
else selectedGiftTargetUid = uid;
selectedGiftTargetName = name;
if (targetInfo != null) {
String t = "To: ";
t += name;
t += " - Selected";
targetInfo.setText(t);
}
String s = "Selected: ";
s += name;
Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
});
return col;
}
private List<GiftModel> getGiftsForCategory(String cat) {
List<GiftModel> list = new ArrayList<>();
if (cat.equals("Gift")) {
list.add(new GiftModel("g1", "Rose", "20",
R.drawable.ic_gift_flower, "Gift"));
list.add(new GiftModel("g2", "Kiss", "66",
R.drawable.ic_gift_ring, "Gift"));
list.add(new GiftModel("g3", "Love", "100",
R.drawable.ic_gift_diamond, "Gift"));
list.add(new GiftModel("g4", "Cake", "199",
R.drawable.ic_gift_car, "Gift"));
list.add(new GiftModel("g5", "Crown", "500",
R.drawable.ic_gift_crown, "Gift"));
list.add(new GiftModel("g6", "Diamond", "9999",
R.drawable.ic_gift_castle, "Gift"));
} else if (cat.equals("Lucky")) {
list.add(new GiftModel("l1", "Lucky Star", "20",
R.drawable.ic_gift_flower, "Lucky"));
list.add(new GiftModel("l2", "Lucky 7", "77",
R.drawable.ic_gift_ring, "Lucky"));
list.add(new GiftModel("l3", "Lucky Wheel", "777",
R.drawable.ic_gift_diamond, "Lucky"));
} else if (cat.equals("CP")) {
list.add(new GiftModel("c1", "CP Heart", "99999",
R.drawable.ic_gift_flower, "CP"));
list.add(new GiftModel("c2", "CP Ring", "9999",
R.drawable.ic_gift_ring, "CP"));
} else if (cat.equals("Activity")) {
list.add(new GiftModel("a1", "Birthday Cake", "9999",
R.drawable.ic_gift_flower, "Activity"));
list.add(new GiftModel("a2", "Party", "999",
R.drawable.ic_gift_car, "Activity"));
} else if (cat.equals("Flag")) {
list.add(new GiftModel("f1", "Flag", "50",
R.drawable.ic_gift_flower, "Flag"));
list.add(new GiftModel("f2", "Country Flag", "100",
R.drawable.ic_gift_ring, "Flag"));
} else if (cat.equals("Exclusive")) {
list.add(new GiftModel("e1", "Exclusive Dragon", "250000",
R.drawable.ic_gift_diamond, "Exclusive"));
list.add(new GiftModel("e2", "Exclusive Crown", "1000000",
R.drawable.ic_gift_crown, "Exclusive"));
} else if (cat.equals("Bag")) {
list.add(new GiftModel("b1", "Gift Bag", "500",
R.drawable.ic_gift_castle, "Bag"));
list.add(new GiftModel("b2", "Luxury Bag", "5000",
R.drawable.ic_gift_car, "Bag"));
} else {
list.add(new GiftModel("g1", "White Tiger", "50000",
R.drawable.ic_gift_flower, "Animal"));
}
return list;
}
private void setupThemeBackgroundImage() {
try {
ViewGroup root = findViewById(android.R.id.content);
if (root == null) return;
themeBackgroundImg = new ImageView(this);
themeBackgroundImg.setScaleType(
ImageView.ScaleType.CENTER_CROP);
themeBackgroundImg.setAlpha(0.85f);
FrameLayout.LayoutParams lp =
new FrameLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT,
ViewGroup.LayoutParams.MATCH_PARENT);
root.addView(themeBackgroundImg, 0);
if (root.getChildCount() > 1) {
root.getChildAt(1).bringToFront();
}
} catch (Exception ignored) {}
}
private void applyRoomTheme(String c1, String c2,
String fullText, String imageName) {
try {
currentThemeName = fullText;
int resId = getResources().getIdentifier(
imageName, "drawable", getPackageName());
if (themeBackgroundImg != null) {
if (resId != 0) {
themeBackgroundImg.setImageResource(resId);
themeBackgroundImg.setVisibility(View.VISIBLE);
} else {
themeBackgroundImg.setVisibility(View.GONE);
View rv = findViewById(android.R.id.content);
if (rv != null) {
GradientDrawable bg = new GradientDrawable(
GradientDrawable.Orientation.TOP_BOTTOM,
new int[]{Color.parseColor(c1),
Color.parseColor(c2)});
rv.setBackground(bg);
}
}
}
} catch (Exception ignored) {}
}
private void listenToRoomTheme() {
roomMetaRef.addValueEventListener(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
String n = s.child("roomTheme").getValue(String.class);
String c1 = s.child("roomThemeColor1").getValue(String.class);
String c2 = s.child("roomThemeColor2").getValue(String.class);
String img = s.child("roomThemeImage").getValue(String.class);
if (n != null && !n.isEmpty() && c1 != null && c2 != null) {
String im = "theme_night";
if (img != null) im = img;
applyRoomTheme(c1, c2, n, im);
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
private void showRoomThemeDialog() {
Toast.makeText(this, "Theme - Coming Soon",
Toast.LENGTH_SHORT).show();
}
private void showIconToolsDialog(boolean isOwner) {
BottomSheetDialog dialog = new BottomSheetDialog(this,
R.style.Theme_Design_BottomSheetDialog);
LinearLayout root = new LinearLayout(this);
root.setOrientation(LinearLayout.VERTICAL);
root.setPadding(35, 35, 35, 45);
GradientDrawable bg = new GradientDrawable(
GradientDrawable.Orientation.TOP_BOTTOM,
new int[]{Color.parseColor("#1B1035"),
Color.parseColor("#0A0518")});
bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
root.setBackground(bg);
TextView title = new TextView(this);
title.setText("Basic Tools");
title.setTextColor(Color.parseColor("#FFD700"));
title.setTextSize(18);
title.setTypeface(null, android.graphics.Typeface.BOLD);
title.setPadding(0, 0, 0, 15);
root.addView(title);
Button editNameBtn = new Button(this);
editNameBtn.setText("Edit My Name - Global");
editNameBtn.setBackgroundColor(Color.parseColor("#FFD700"));
editNameBtn.setTextColor(Color.BLACK);
editNameBtn.setOnClickListener(v -> {
dialog.dismiss();
showEditMyProfileDialog();
});
root.addView(editNameBtn);
GridLayout grid = new GridLayout(this);
grid.setColumnCount(4);
grid.setUseDefaultMargins(true);
String[][] basic = {
{"\uD83D\uDD12", "Lock", "Lock off"},
{"\u2699\uFE0F", "Set", "Room set"},
{"\u270F\uFE0F", "Edit", "Room Edit"},
{"\uD83E\uDDF9", "Clear", "Clear charm"},
{"\uD83D\uDCAC", "Chat", "Clear chat"},
{"\uD83C\uDFA4", "Mic", "Change mic"},
{"\uD83C\uDFA8", "Theme", "Mic Theme"},
{"\uD83D\uDC57", "Dress", "Dress"},
{"\uD83C\uDF40", "Lucky", "Lucky Sound"},
{"\uD83D\uDC65", "Voice", "Voice User"},
{"\uD83D\uDEAA", "Kick", "Kick out record"},
{"\uD83C\uDFC6", "Reward", "Reward Event"}
};
for (String[] it : basic) {
LinearLayout cell = makeToolIconCell(
it[0], it[1], it[2], v -> {
dialog.dismiss();
handleToolClick(it[2], isOwner);
});
grid.addView(cell);
}
root.addView(grid);
dialog.setContentView(root);
Window w = dialog.getWindow();
if (w != null) {
w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1400);
w.setGravity(Gravity.BOTTOM);
}
dialog.show();
}
private LinearLayout makeToolIconCell(String icon, String title,
String key, View.OnClickListener l) {
LinearLayout cell = new LinearLayout(this);
cell.setOrientation(LinearLayout.VERTICAL);
cell.setGravity(Gravity.CENTER);
cell.setPadding(10, 18, 10, 18);
cell.setOnClickListener(l);
GradientDrawable bg = new GradientDrawable();
bg.setColor(Color.parseColor("#2A1B4E"));
bg.setCornerRadius(18);
cell.setBackground(bg);
GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
lp.width = 0;
lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
lp.setMargins(6, 6, 6, 6);
cell.setLayoutParams(lp);
TextView ic = new TextView(this);
ic.setText(icon);
ic.setTextSize(28);
ic.setGravity(Gravity.CENTER);
cell.addView(ic);
TextView lb = new TextView(this);
lb.setText(title);
lb.setTextColor(Color.WHITE);
lb.setTextSize(14);
lb.setGravity(Gravity.CENTER);
lb.setTypeface(null, android.graphics.Typeface.BOLD);
cell.addView(lb);
TextView sub = new TextView(this);
sub.setText(key);
sub.setTextColor(Color.parseColor("#B0A8D0"));
sub.setTextSize(10);
sub.setGravity(Gravity.CENTER);
cell.addView(sub);
return cell;
}
private void handleToolClick(String tool, boolean isOwner) {
switch (tool) {
case "Lock off": showRoomLockPasswordDialog(); break;
case "Room set":
if (isOwner) showEditRoomDialog();
else Toast.makeText(this, "Only Owner can edit",
Toast.LENGTH_SHORT).show();
break;
case "Room Edit":
if (isOwner) showEditRoomDialog();
else Toast.makeText(this, "Only Owner",
Toast.LENGTH_SHORT).show();
break;
case "Clear charm": clearCharm(); break;
case "Clear chat":
messagesRef.removeValue();
chatMessages.clear();
chatAdapter.notifyDataSetChanged();
Toast.makeText(this, "Chat Cleared",
Toast.LENGTH_SHORT).show();
break;
case "Change mic":
Toast.makeText(this, "Change Mic - Select Seat",
Toast.LENGTH_SHORT).show();
break;
case "Mic Theme": showRoomThemeDialog(); break;
case "Dress":
Toast.makeText(this, "Dress - Coming",
Toast.LENGTH_SHORT).show();
break;
case "Lucky Sound":
Toast.makeText(this, "Lucky Sound Played",
Toast.LENGTH_SHORT).show();
break;
case "Voice User": showScrollableOnlineMembersDialog(); break;
case "Kick out record": showScrollableOnlineMembersDialog(); break;
case "Reward Event": showRewardEventDialog(); break;
}
}
private void clearCharm() {
databaseReference.child("seats")
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
for (int i = 0; i < 10; i++) {
databaseReference.child("seats")
.child("seat_" + i).child("giftTotal").setValue(0L);
}
Toast.makeText(VoiceRoomActivity.this,
"Charm Cleared", Toast.LENGTH_SHORT).show();
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
private long calcReward(long contribution) {
if (contribution >= 3000000 && contribution < 10000000) {
return (long) (contribution * 0.05);
}
if (contribution >= 10000000) {
return (long) (contribution * 0.10);
}
return 0L;
}
private void showRewardEventDialog() {
BottomSheetDialog dialog = new BottomSheetDialog(this,
R.style.Theme_Design_BottomSheetDialog);
LinearLayout root = new LinearLayout(this);
root.setOrientation(LinearLayout.VERTICAL);
root.setPadding(35, 35, 35, 45);
GradientDrawable bg = new GradientDrawable(
GradientDrawable.Orientation.TOP_BOTTOM,
new int[]{Color.parseColor("#0A0518"),
Color.parseColor("#1B1035")});
bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
root.setBackground(bg);
TextView title = new TextView(this);
title.setText("Room Reward Event");
title.setTextColor(Color.parseColor("#FFD700"));
title.setTextSize(20);
title.setTypeface(null, android.graphics.Typeface.BOLD);
title.setGravity(Gravity.CENTER);
title.setPadding(0, 0, 0, 8);
root.addView(title);
TextView rule = new TextView(this);
rule.setText("3M - 10M = 5%  |  10M+ = 10%");
rule.setTextColor(Color.WHITE);
rule.setTextSize(13);
rule.setGravity(Gravity.CENTER);
rule.setPadding(0, 0, 0, 10);
root.addView(rule);
TextView weeklyTxt = new TextView(this);
weeklyTxt.setText("Weekly Contribution - Loading...");
weeklyTxt.setTextColor(Color.WHITE);
weeklyTxt.setTextSize(16);
weeklyTxt.setTypeface(null, android.graphics.Typeface.BOLD);
weeklyTxt.setGravity(Gravity.CENTER);
weeklyTxt.setPadding(0, 15, 0, 8);
root.addView(weeklyTxt);
TextView availTxt = new TextView(this);
availTxt.setText("Available Reward: 0");
availTxt.setTextColor(Color.parseColor("#00E676"));
availTxt.setTextSize(15);
availTxt.setTypeface(null, android.graphics.Typeface.BOLD);
availTxt.setGravity(Gravity.CENTER);
availTxt.setPadding(0, 0, 0, 15);
root.addView(availTxt);
Button btnClaim = new Button(this);
btnClaim.setText("Claim Rewards");
btnClaim.setBackgroundColor(Color.parseColor("#FF4081"));
btnClaim.setTextColor(Color.WHITE);
btnClaim.setOnClickListener(v -> {
Toast.makeText(this, "Claimed!",
Toast.LENGTH_SHORT).show();
});
root.addView(btnClaim);
ScrollView scroll = new ScrollView(this);
scroll.setLayoutParams(new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT, 700));
scroll.setFillViewport(true);
LinearLayout table = new LinearLayout(this);
table.setOrientation(LinearLayout.VERTICAL);
table.setPadding(0, 20, 0, 0);
long[] tiers = {3000000L, 6000000L, 10000000L,
14000000L, 20000000L, 30000000L, 40000000L,
50000000L, 70000000L, 90000000L, 100000000L};
for (long t : tiers) {
TextView row = new TextView(this);
String rt = formatCoins(t);
rt += "  ->  ";
rt += formatCoins(calcReward(t));
if (t < 10000000) rt += " (5%)";
else rt += " (10%)";
row.setText(rt);
row.setTextColor(Color.WHITE);
row.setTextSize(14);
row.setGravity(Gravity.CENTER);
row.setPadding(0, 8, 0, 8);
table.addView(row);
}
scroll.addView(table);
root.addView(scroll);
databaseReference.child("seats")
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
long total = 0;
for (DataSnapshot ds : s.getChildren()) {
Long g = ds.child("giftTotal").getValue(Long.class);
if (g != null) total += g;
}
String wt = "Weekly Contribution - ";
wt += formatCoins(total);
weeklyTxt.setText(wt);
String at = "Available Reward: ";
at += formatCoins(calcReward(total));
availTxt.setText(at);
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
dialog.setContentView(root);
Window w = dialog.getWindow();
if (w != null) {
w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1500);
w.setGravity(Gravity.BOTTOM);
}
dialog.show();
}
private void showChatDialog() {
final Dialog chatDialog = new Dialog(this,
R.style.Theme_Design_BottomSheetDialog);
chatDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
chatDialog.setContentView(R.layout.dialog_bottom_chat);
Window window = chatDialog.getWindow();
if (window != null) {
window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
ViewGroup.LayoutParams.WRAP_CONTENT);
window.setGravity(Gravity.BOTTOM);
window.setSoftInputMode(
WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
}
EditText inputMsg = chatDialog.findViewById(R.id.editRoomMessage);
Button btnSendMsg = chatDialog.findViewById(R.id.btnSendRoomMessage);
if (btnSendMsg != null && inputMsg != null) {
btnSendMsg.setOnClickListener(view -> {
String message = inputMsg.getText().toString().trim();
if (!message.isEmpty()) {
String msgId = messagesRef.push().getKey();
if (msgId != null) {
Map<String, Object> m = new HashMap<>();
String sn = getCorrectBadgeIcon(currentUserId);
sn += getMyDisplayName();
m.put("senderName", sn);
m.put("senderId", currentUserId);
m.put("message", message);
m.put("timestamp", System.currentTimeMillis());
messagesRef.child(msgId).setValue(m);
}
inputMsg.setText("");
chatDialog.dismiss();
}
});
}
chatDialog.show();
}
private void showPremiumRankingDialog() {
BottomSheetDialog dialog = new BottomSheetDialog(this,
R.style.Theme_Design_BottomSheetDialog);
LinearLayout root = new LinearLayout(this);
root.setOrientation(LinearLayout.VERTICAL);
root.setPadding(30, 30, 30, 30);
GradientDrawable bg = new GradientDrawable(
GradientDrawable.Orientation.TOP_BOTTOM,
new int[]{Color.parseColor("#1B1035"),
Color.parseColor("#0A0518")});
bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
root.setBackground(bg);
TextView title = new TextView(this);
title.setText("Room Contribution Ranking");
title.setTextColor(Color.parseColor("#FFD700"));
title.setTextSize(18);
title.setTypeface(null, android.graphics.Typeface.BOLD);
title.setGravity(Gravity.CENTER);
title.setPadding(0, 0, 0, 15);
root.addView(title);
LinearLayout typeRow = new LinearLayout(this);
typeRow.setOrientation(LinearLayout.HORIZONTAL);
typeRow.setGravity(Gravity.CENTER);
Button bWealth = new Button(this);
bWealth.setText("Wealth");
Button bCharm = new Button(this);
bCharm.setText("Charm");
typeRow.addView(bWealth);
typeRow.addView(bCharm);
root.addView(typeRow);
LinearLayout tabRow = new LinearLayout(this);
tabRow.setOrientation(LinearLayout.HORIZONTAL);
tabRow.setGravity(Gravity.CENTER);
tabRow.setPadding(0, 12, 0, 12);
Button bDay = new Button(this);
bDay.setText("Daily");
Button bWeek = new Button(this);
bWeek.setText("Weekly");
Button bMonth = new Button(this);
bMonth.setText("Monthly");
tabRow.addView(bDay);
tabRow.addView(bWeek);
tabRow.addView(bMonth);
root.addView(tabRow);
LinearLayout podium = new LinearLayout(this);
podium.setOrientation(LinearLayout.HORIZONTAL);
podium.setGravity(Gravity.CENTER);
podium.setPadding(0, 5, 0, 15);
root.addView(podium);
ScrollView scroll = new ScrollView(this);
scroll.setLayoutParams(new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT, 900));
LinearLayout listContainer = new LinearLayout(this);
listContainer.setOrientation(LinearLayout.VERTICAL);
scroll.addView(listContainer);
root.addView(scroll);
Runnable load = () -> {
String field = "sendingCoins";
if (rankType.equals("Charm")) field = "receivingCoins";
final String f = field;
userDatabaseRef.orderByChild(f).limitToLast(20)
.addValueEventListener(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot snap) {
rankCache.clear();
for (DataSnapshot ds : snap.getChildren()) {
String n = ds.child("name").getValue(String.class);
Long c = ds.child(f).getValue(Long.class);
String img = ds.child("profileImage")
.getValue(String.class);
if (n == null) n = "User";
long cv = 0L;
if (c != null) cv = c;
rankCache.add(new RankItem(n, cv, img, ds.getKey()));
}
Collections.sort(rankCache,
(a, b) -> Long.compare(b.coins, a.coins));
podium.removeAllViews();
listContainer.removeAllViews();
for (int i = 0; i < rankCache.size(); i++) {
RankItem it = rankCache.get(i);
if (i < 3) {
LinearLayout col = new LinearLayout(
VoiceRoomActivity.this);
col.setOrientation(LinearLayout.VERTICAL);
col.setGravity(Gravity.CENTER);
col.setPadding(10, 5, 10, 5);
TextView top = new TextView(VoiceRoomActivity.this);
String tt = "Top ";
tt += (i + 1);
top.setText(tt);
top.setTextColor(Color.parseColor("#FFD700"));
top.setTypeface(null,
android.graphics.Typeface.BOLD);
top.setGravity(Gravity.CENTER);
col.addView(top);
TextView nm = new TextView(VoiceRoomActivity.this);
nm.setText(it.name);
nm.setTextColor(Color.WHITE);
nm.setGravity(Gravity.CENTER);
col.addView(nm);
TextView sc = new TextView(VoiceRoomActivity.this);
sc.setText(formatCoins(it.coins) + " Coins");
sc.setTextColor(Color.parseColor("#00E676"));
sc.setGravity(Gravity.CENTER);
col.addView(sc);
podium.addView(col);
}
String r = String.valueOf(i + 1);
String nm2 = (i + 1) + ". ";
nm2 += it.name;
String sc2 = formatCoins(it.coins) + " Coins";
String col2 = "#FFFFFF";
if (i == 0) col2 = "#FFD700";
addRankUserRow(listContainer, r, nm2, sc2, col2);
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
};
bWealth.setOnClickListener(v -> {
rankType = "Wealth"; load.run();
});
bCharm.setOnClickListener(v -> {
rankType = "Charm"; load.run();
});
bDay.setOnClickListener(v -> {
rankTab = "Daily"; load.run();
});
bWeek.setOnClickListener(v -> {
rankTab = "Weekly"; load.run();
});
bMonth.setOnClickListener(v -> {
rankTab = "Monthly"; load.run();
});
load.run();
dialog.setContentView(root);
Window w = dialog.getWindow();
if (w != null) {
int sh = getResources().getDisplayMetrics().heightPixels;
w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
(int) (sh * 0.85));
w.setGravity(Gravity.BOTTOM);
}
dialog.show();
}
private void addRankUserRow(LinearLayout container,
String rank, String name, String score, String badgeHex) {
Context ctx = container.getContext();
LinearLayout row = new LinearLayout(ctx);
row.setOrientation(LinearLayout.HORIZONTAL);
row.setPadding(20, 20, 20, 20);
row.setGravity(Gravity.CENTER_VERTICAL);
GradientDrawable rBg = new GradientDrawable();
rBg.setColor(Color.parseColor("#1B1035"));
rBg.setCornerRadius(16);
row.setBackground(rBg);
LinearLayout.LayoutParams p =
new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT,
ViewGroup.LayoutParams.WRAP_CONTENT);
p.setMargins(0, 0, 0, 12);
row.setLayoutParams(p);
TextView rt = new TextView(ctx);
String tt = "Top ";
tt += rank;
rt.setText(tt);
rt.setTextColor(Color.parseColor(badgeHex));
rt.setTypeface(null, android.graphics.Typeface.BOLD);
rt.setMinWidth(90);
row.addView(rt);
LinearLayout stack = new LinearLayout(ctx);
stack.setOrientation(LinearLayout.VERTICAL);
stack.setLayoutParams(new LinearLayout.LayoutParams(
0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
TextView nt = new TextView(ctx);
nt.setText(name);
nt.setTextColor(Color.WHITE);
stack.addView(nt);
TextView st = new TextView(ctx);
st.setText(score);
st.setTextColor(Color.parseColor("#00E676"));
stack.addView(st);
row.addView(stack);
container.addView(row);
}
static class RankItem {
String name; long coins; String image; String uid;
RankItem(String n, long c, String i, String u) {
name = n; coins = c; image = i; uid = u;
}
}
private void triggerFlyingGiftAnimation(String giftName) {
try {
ViewGroup rv = findViewById(android.R.id.content);
if (rv == null) return;
final TextView fv = new TextView(this);
String t = "Gift ";
t += giftName;
fv.setText(t);
fv.setTextColor(Color.parseColor("#FFD700"));
fv.setTextSize(20);
fv.setTypeface(null, android.graphics.Typeface.BOLD);
fv.setGravity(Gravity.CENTER);
fv.setBackgroundColor(Color.parseColor("#88000000"));
fv.setPadding(30, 20, 30, 20);
FrameLayout.LayoutParams params =
new FrameLayout.LayoutParams(
ViewGroup.LayoutParams.WRAP_CONTENT,
ViewGroup.LayoutParams.WRAP_CONTENT);
params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
params.setMargins(0, 0, 0, 300);
rv.addView(fv, params);
TranslateAnimation anim = new TranslateAnimation(
Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, 0f,
Animation.ABSOLUTE, 0f, Animation.ABSOLUTE, -1000f);
anim.setDuration(2000);
anim.setFillAfter(true);
anim.setAnimationListener(new Animation.AnimationListener() {
@Override public void onAnimationStart(Animation a) {}
@Override public void onAnimationEnd(Animation a) {
rv.removeView(fv);
}
@Override public void onAnimationRepeat(Animation a) {}
});
fv.startAnimation(anim);
} catch (Exception ignored) {}
}
private void listenToRoomAdmins() {
adminsRef.addValueEventListener(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
roomAdminList.clear();
for (DataSnapshot ds : s.getChildren()) {
if (Boolean.TRUE.equals(ds.getValue(Boolean.class))) {
roomAdminList.add(ds.getKey());
}
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
private String getCorrectBadgeIcon(String uid) {
if (uid == null || uid.isEmpty()) return "";
if (uid.equals(roomOwnerId)) return "Owner ";
if (roomAdminList.contains(uid)) return "Admin ";
return "";
}
private void listenToRealtimeChatMessages() {
messagesRef.addChildEventListener(new ChildEventListener() {
@Override public void onChildAdded(@NonNull DataSnapshot snap,
@Nullable String prev) {
String sn = snap.child("senderName").getValue(String.class);
String msg = snap.child("message").getValue(String.class);
Long t = snap.child("timestamp").getValue(Long.class);
if (t != null && t < roomJoinTimestamp) return;
if (sn != null && msg != null) {
String full = sn;
full += ": ";
full += msg;
if (!chatMessages.contains(full)) {
chatMessages.add(full);
chatAdapter.notifyDataSetChanged();
if (chatListView != null) {
chatListView.setSelection(chatMessages.size() - 1);
}
}
}
}
@Override public void onChildChanged(
@NonNull DataSnapshot s, @Nullable String p) {}
@Override public void onChildRemoved(@NonNull DataSnapshot s) {
chatMessages.clear();
chatAdapter.notifyDataSetChanged();
}
@Override public void onChildMoved(
@NonNull DataSnapshot s, @Nullable String p) {}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
private void showRoomLockPasswordDialog() {
AlertDialog.Builder b = new AlertDialog.Builder(this);
b.setTitle("Room Lock");
LinearLayout layout = new LinearLayout(this);
layout.setOrientation(LinearLayout.VERTICAL);
layout.setPadding(50, 30, 50, 30);
final EditText input = new EditText(this);
input.setHint("Enter Room Password...");
input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
| android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
layout.addView(input);
b.setView(layout);
b.setPositiveButton("Lock Room", (d, w) -> {
String pw = input.getText().toString().trim();
if (!pw.isEmpty()) {
roomMetaRef.child("roomPassword").setValue(pw);
roomMetaRef.child("isLocked").setValue(true);
Toast.makeText(this, "Room locked!",
Toast.LENGTH_SHORT).show();
}
});
b.setNeutralButton("Unlock Room", (d, w) -> {
roomMetaRef.child("roomPassword").removeValue();
roomMetaRef.child("isLocked").setValue(false);
Toast.makeText(this, "Room Unlocked!",
Toast.LENGTH_SHORT).show();
});
b.setNegativeButton("Cancel", (d, w) -> d.cancel());
b.show();
}
private void showScrollableOnlineMembersDialog() {
BottomSheetDialog dialog = new BottomSheetDialog(this,
R.style.Theme_Design_BottomSheetDialog);
LinearLayout layout = new LinearLayout(this);
layout.setOrientation(LinearLayout.VERTICAL);
layout.setPadding(35, 35, 35, 35);
GradientDrawable bg = new GradientDrawable(
GradientDrawable.Orientation.TOP_BOTTOM,
new int[]{Color.parseColor("#1B1035"),
Color.parseColor("#0A0518")});
bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
layout.setBackground(bg);
TextView title = new TextView(this);
title.setText("Online Members");
title.setTextColor(Color.parseColor("#00E676"));
title.setTextSize(18);
title.setTypeface(null, android.graphics.Typeface.BOLD);
title.setGravity(Gravity.CENTER);
title.setPadding(0, 0, 0, 15);
layout.addView(title);
ScrollView sv = new ScrollView(this);
sv.setLayoutParams(new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT, 1100));
sv.setFillViewport(true);
LinearLayout container = new LinearLayout(this);
container.setOrientation(LinearLayout.VERTICAL);
sv.addView(container);
layout.addView(sv);
databaseReference.child("online_users")
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot snap) {
container.removeAllViews();
int serial = 1;
for (DataSnapshot ds : snap.getChildren()) {
String mn = ds.getValue(String.class);
String mu = ds.getKey();
if (mn == null) continue;
final int sNum = serial;
final String fName = mn;
final String fUid = mu;
userDatabaseRef.child(mu)
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot us) {
String img = us.child("profileImage")
.getValue(String.class);
String realName = us.child("name")
.getValue(String.class);
String showName = fName;
if (realName != null && !realName.isEmpty()) {
showName = realName;
}
LinearLayout row = new LinearLayout(
VoiceRoomActivity.this);
row.setOrientation(LinearLayout.HORIZONTAL);
row.setPadding(18, 18, 18, 18);
row.setGravity(Gravity.CENTER_VERTICAL);
GradientDrawable rBg = new GradientDrawable();
rBg.setColor(Color.parseColor("#2A1B4E"));
rBg.setCornerRadius(16);
row.setBackground(rBg);
LinearLayout.LayoutParams lp =
new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT,
ViewGroup.LayoutParams.WRAP_CONTENT);
lp.setMargins(0, 0, 0, 10);
row.setLayoutParams(lp);
ImageView av = new ImageView(VoiceRoomActivity.this);
LinearLayout.LayoutParams ap =
new LinearLayout.LayoutParams(110, 110);
ap.setMargins(0, 0, 15, 0);
av.setLayoutParams(ap);
av.setScaleType(ImageView.ScaleType.CENTER_CROP);
if (img != null && !img.isEmpty()) {
try {
byte[] b = android.util.Base64.decode(
img, android.util.Base64.DEFAULT);
Bitmap bmp = BitmapFactory.decodeByteArray(
b, 0, b.length);
if (bmp != null) {
RoundedBitmapDrawable rd =
RoundedBitmapDrawableFactory.create(
getResources(), bmp);
rd.setCircular(true);
av.setImageDrawable(rd);
} else {
av.setImageResource(
android.R.drawable.ic_menu_my_calendar);
}
} catch (Exception e) {
av.setImageResource(
android.R.drawable.ic_menu_my_calendar);
}
} else {
av.setImageResource(
android.R.drawable.ic_menu_my_calendar);
}
row.addView(av);
String badge = "Member ";
if (fUid != null && fUid.equals(roomOwnerId)) {
badge = "Owner ";
} else if (roomAdminList.contains(fUid)) {
badge = "Admin ";
}
TextView nt = new TextView(VoiceRoomActivity.this);
String ntTxt = sNum + ". ";
ntTxt += badge;
ntTxt += showName;
ntTxt += " (ID: ";
ntTxt += fUid;
ntTxt += ")";
nt.setText(ntTxt);
nt.setTextColor(Color.WHITE);
nt.setTextSize(14);
nt.setTypeface(null, android.graphics.Typeface.BOLD);
row.addView(nt);
final String fn2 = showName;
row.setOnClickListener(v -> {
dialog.dismiss();
fetchUserDataAndShowProfile(fn2, fUid, img);
});
container.addView(row);
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
serial++;
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
dialog.setContentView(layout);
Window w = dialog.getWindow();
if (w != null) {
int sh = getResources().getDisplayMetrics().heightPixels;
w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
(int) (sh * 0.75));
w.setGravity(Gravity.BOTTOM);
}
dialog.show();
}
private void syncLatestUserProfile() {
if (currentUserId.isEmpty()) return;
userDatabaseRef.child(currentUserId)
.addValueEventListener(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
if (s.exists()) {
String dbName = s.child("name").getValue(String.class);
String dbImg = s.child("profileImage")
.getValue(String.class);
String dbCountry = s.child("country")
.getValue(String.class);
String dbLang = s.child("language")
.getValue(String.class);
if (dbName != null && !dbName.isEmpty()) {
myDisplayName = dbName;
prefs.edit().putString("USER_NAME", dbName).apply();
}
if (dbImg != null) {
prefs.edit().putString("USER_IMAGE", dbImg).apply();
prefs.edit().putString("USER_PROFILE_IMAGE", dbImg).apply();
}
if (dbCountry != null && !dbCountry.isEmpty()) {
myCountry = dbCountry;
}
if (dbLang != null && !dbLang.isEmpty()) {
myLanguage = dbLang;
}
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
private void listenToRoomDetails() {
roomMetaRef.addValueEventListener(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
String rn = s.child("roomName").getValue(String.class);
String rb = s.child("roomBio").getValue(String.class);
String oi = s.child("roomOwnerImage").getValue(String.class);
if (rn != null && !rn.isEmpty()) {
if (roomTitleText != null) roomTitleText.setText(rn);
} else {
if (roomTitleText != null) {
roomTitleText.setText("SR Voice Room");
}
}
if (roomInfoText != null) {
String it = "ID: ";
it += roomOwnerId;
roomInfoText.setText(it);
}
if (roomNoticeBarText != null) {
String bio = "Welcome to my voice room!";
if (rb != null && !rb.isEmpty()) bio = rb;
String nt = "Notice: ";
nt += bio;
roomNoticeBarText.setText(nt);
}
if (roomHeaderProfileImg != null
&& oi != null && !oi.isEmpty()) {
try {
byte[] d = android.util.Base64.decode(
oi, android.util.Base64.DEFAULT);
Bitmap bmp = BitmapFactory.decodeByteArray(d, 0, d.length);
if (bmp != null) {
RoundedBitmapDrawable rd =
RoundedBitmapDrawableFactory.create(getResources(), bmp);
rd.setCircular(true);
roomHeaderProfileImg.setImageDrawable(rd);
}
} catch (Exception e) {
roomHeaderProfileImg.setImageResource(
android.R.drawable.ic_menu_my_calendar);
}
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
private void showEditRoomDialog() {
AlertDialog.Builder b = new AlertDialog.Builder(this);
b.setTitle("Room Settings");
LinearLayout layout = new LinearLayout(this);
layout.setOrientation(LinearLayout.VERTICAL);
layout.setPadding(50, 20, 50, 20);
Button btnPic = new Button(this);
btnPic.setText("Change Room Profile Picture");
btnPic.setOnClickListener(v -> {
Intent intent = new Intent(Intent.ACTION_PICK,
MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
startActivityForResult(intent, PICK_IMAGE_REQUEST);
});
layout.addView(btnPic);
dialogInputName = new EditText(this);
dialogInputName.setHint("Enter Room Name...");
layout.addView(dialogInputName);
dialogInputBio = new EditText(this);
dialogInputBio.setHint("Enter Room Bio...");
layout.addView(dialogInputBio);
b.setView(layout);
b.setPositiveButton("Save", (d, w) -> {
String nn = dialogInputName.getText().toString().trim();
String nb = dialogInputBio.getText().toString().trim();
if (!nn.isEmpty()) roomMetaRef.child("roomName").setValue(nn);
if (!nb.isEmpty()) roomMetaRef.child("roomBio").setValue(nb);
Toast.makeText(this, "Room settings updated!",
Toast.LENGTH_SHORT).show();
});
b.setNegativeButton("Cancel", (d, w) -> d.cancel());
b.show();
}
@Override protected void onActivityResult(int requestCode,
int resultCode, @Nullable Intent data) {
super.onActivityResult(requestCode, resultCode, data);
if (requestCode == PICK_IMAGE_REQUEST
&& resultCode == RESULT_OK
&& data != null && data.getData() != null) {
Uri uri = data.getData();
try {
InputStream is = getContentResolver().openInputStream(uri);
Bitmap bmp = BitmapFactory.decodeStream(is);
if (bmp != null) {
Bitmap resized = Bitmap.createScaledBitmap(bmp, 150, 150, true);
ByteArrayOutputStream baos = new ByteArrayOutputStream();
resized.compress(Bitmap.CompressFormat.JPEG, 70, baos);
String enc = android.util.Base64.encodeToString(
baos.toByteArray(), android.util.Base64.DEFAULT);
roomMetaRef.child("roomOwnerImage").setValue(enc);
Toast.makeText(this, "Room Profile Picture Updated!",
Toast.LENGTH_SHORT).show();
}
} catch (Exception e) {
Toast.makeText(this, "Failed to load image",
Toast.LENGTH_SHORT).show();
}
}
}
private void listenToOnlineMembers() {
String un = getMyDisplayName();
databaseReference.child("online_users")
.child(currentUserId).setValue(un);
databaseReference.child("online_users")
.child(currentUserId).onDisconnect().removeValue();
databaseReference.child("online_users")
.addValueEventListener(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
long c = s.getChildrenCount();
if (textOnlineCount != null) {
textOnlineCount.setText(String.valueOf(c));
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
private void showPremiumRoomInfoAndMembersDialog() {
BottomSheetDialog dialog = new BottomSheetDialog(this,
R.style.Theme_Design_BottomSheetDialog);
LinearLayout root = new LinearLayout(this);
root.setOrientation(LinearLayout.VERTICAL);
root.setPadding(35, 35, 35, 35);
GradientDrawable bg = new GradientDrawable(
GradientDrawable.Orientation.TOP_BOTTOM,
new int[]{Color.parseColor("#1B1035"),
Color.parseColor("#0A0518")});
bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
root.setBackground(bg);
LinearLayout tabRow = new LinearLayout(this);
tabRow.setOrientation(LinearLayout.HORIZONTAL);
tabRow.setGravity(Gravity.CENTER);
tabRow.setPadding(0, 0, 0, 15);
Button tabProfile = new Button(this);
tabProfile.setText("Profile");
tabProfile.setBackgroundColor(Color.parseColor("#FFD700"));
tabProfile.setTextColor(Color.BLACK);
tabProfile.setLayoutParams(new LinearLayout.LayoutParams(
0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
Button tabMembers = new Button(this);
tabMembers.setText("Members");
tabMembers.setBackgroundColor(Color.parseColor("#2A1B4E"));
tabMembers.setTextColor(Color.WHITE);
tabMembers.setLayoutParams(new LinearLayout.LayoutParams(
0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
tabRow.addView(tabProfile);
tabRow.addView(tabMembers);
root.addView(tabRow);
ScrollView scroll = new ScrollView(this);
scroll.setLayoutParams(new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT, 1250));
scroll.setFillViewport(true);
LinearLayout profileBox = new LinearLayout(this);
profileBox.setOrientation(LinearLayout.VERTICAL);
LinearLayout card = new LinearLayout(this);
card.setOrientation(LinearLayout.VERTICAL);
card.setPadding(25, 25, 25, 25);
GradientDrawable cardBg = new GradientDrawable();
cardBg.setColor(Color.parseColor("#2A1B4E"));
cardBg.setCornerRadius(20);
card.setBackground(cardBg);
TextView ot = new TextView(this);
String otTxt = "Room: ";
otTxt += (roomTitleText != null
? roomTitleText.getText().toString() : "SR Voice Room");
ot.setText(otTxt);
ot.setTextColor(Color.parseColor("#FFD700"));
ot.setTextSize(17);
ot.setTypeface(null, android.graphics.Typeface.BOLD);
card.addView(ot);
TextView oid = new TextView(this);
String oidTxt = "ID: ";
oidTxt += roomOwnerId;
oid.setText(oidTxt);
oid.setTextColor(Color.parseColor("#00E676"));
oid.setTextSize(13);
oid.setPadding(0, 6, 0, 8);
card.addView(oid);
TextView owner = new TextView(this);
String owTxt = "RoomOwner: ";
owTxt += getMyDisplayName();
owTxt += " (ID:";
owTxt += roomOwnerId;
owTxt += ")";
owner.setText(owTxt);
owner.setTextColor(Color.WHITE);
owner.setTextSize(14);
card.addView(owner);
TextView lang = new TextView(this);
String lgTxt = "Language: ";
lgTxt += myLanguage;
lang.setText(lgTxt);
lang.setTextColor(Color.WHITE);
lang.setTextSize(14);
lang.setPadding(0, 8, 0, 0);
card.addView(lang);
TextView country = new TextView(this);
String coTxt = "Country: ";
coTxt += myCountry;
country.setText(coTxt);
country.setTextColor(Color.WHITE);
country.setTextSize(14);
card.addView(country);
TextView tag = new TextView(this);
tag.setText("Tag: Friends");
tag.setTextColor(Color.WHITE);
tag.setTextSize(14);
card.addView(tag);
profileBox.addView(card);
TextView bannerTitle = new TextView(this);
bannerTitle.setText("PREMIUM BANNER - Tap to Open Reward");
bannerTitle.setTextColor(Color.parseColor("#FFD700"));
bannerTitle.setTextSize(15);
bannerTitle.setTypeface(null, android.graphics.Typeface.BOLD);
bannerTitle.setGravity(Gravity.CENTER);
bannerTitle.setPadding(0, 20, 0, 10);
profileBox.addView(bannerTitle);
Button banner = new Button(this);
banner.setText("REWARD EVENT - Weekly Claim");
banner.setBackgroundColor(Color.parseColor("#FFD700"));
banner.setTextColor(Color.BLACK);
banner.setTypeface(null, android.graphics.Typeface.BOLD);
banner.setPadding(0, 25, 0, 25);
banner.setOnClickListener(v -> {
dialog.dismiss();
showRewardEventDialog();
});
profileBox.addView(banner);
Button editBtn = new Button(this);
editBtn.setText("Edit My Name");
editBtn.setBackgroundColor(Color.parseColor("#7C4DFF"));
editBtn.setTextColor(Color.WHITE);
editBtn.setOnClickListener(v -> {
dialog.dismiss();
showEditMyProfileDialog();
});
profileBox.addView(editBtn);
LinearLayout membersBox = new LinearLayout(this);
membersBox.setOrientation(LinearLayout.VERTICAL);
membersBox.setVisibility(View.GONE);
TextView mHead = new TextView(this);
mHead.setText("Members: Owner / Admin / Members");
mHead.setTextColor(Color.parseColor("#FFD700"));
mHead.setTextSize(15);
mHead.setTypeface(null, android.graphics.Typeface.BOLD);
mHead.setPadding(0, 0, 0, 12);
membersBox.addView(mHead);
databaseReference.child("online_users")
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot snap) {
membersBox.removeAllViews();
membersBox.addView(mHead);
int serial = 1;
for (DataSnapshot ds : snap.getChildren()) {
String un = ds.getValue(String.class);
String uid = ds.getKey();
if (un == null) continue;
LinearLayout row = new LinearLayout(
VoiceRoomActivity.this);
row.setOrientation(LinearLayout.HORIZONTAL);
row.setPadding(18, 18, 18, 18);
row.setGravity(Gravity.CENTER_VERTICAL);
GradientDrawable rBg = new GradientDrawable();
rBg.setColor(Color.parseColor("#2A1B4E"));
rBg.setCornerRadius(16);
row.setBackground(rBg);
LinearLayout.LayoutParams lp =
new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT,
ViewGroup.LayoutParams.WRAP_CONTENT);
lp.setMargins(0, 0, 0, 10);
row.setLayoutParams(lp);
String badge = "Member ";
if (uid != null && uid.equals(roomOwnerId)) {
badge = "Owner ";
} else if (roomAdminList.contains(uid)) {
badge = "Admin ";
}
TextView nt = new TextView(VoiceRoomActivity.this);
String ntTxt = serial + ". ";
ntTxt += badge;
ntTxt += un;
ntTxt += " (ID: ";
ntTxt += uid;
ntTxt += ")";
nt.setText(ntTxt);
nt.setTextColor(Color.WHITE);
nt.setTextSize(14);
nt.setTypeface(null, android.graphics.Typeface.BOLD);
row.addView(nt);
final String fn = un;
final String fu = uid;
row.setOnClickListener(v -> {
dialog.dismiss();
fetchUserDataAndShowProfile(fn, fu, null);
});
membersBox.addView(row);
serial++;
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
scroll.addView(profileBox);
root.addView(scroll);
tabProfile.setOnClickListener(v -> {
tabProfile.setBackgroundColor(Color.parseColor("#FFD700"));
tabProfile.setTextColor(Color.BLACK);
tabMembers.setBackgroundColor(Color.parseColor("#2A1B4E"));
tabMembers.setTextColor(Color.WHITE);
scroll.removeAllViews();
scroll.addView(profileBox);
});
tabMembers.setOnClickListener(v -> {
tabMembers.setBackgroundColor(Color.parseColor("#FFD700"));
tabMembers.setTextColor(Color.BLACK);
tabProfile.setBackgroundColor(Color.parseColor("#2A1B4E"));
tabProfile.setTextColor(Color.WHITE);
scroll.removeAllViews();
scroll.addView(membersBox);
});
dialog.setContentView(root);
Window w = dialog.getWindow();
if (w != null) {
int sh = getResources().getDisplayMetrics().heightPixels;
w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
(int) (sh * 0.85));
w.setGravity(Gravity.BOTTOM);
}
dialog.show();
}
private void listenToFirebaseSeats() {
int[] imgIds = {R.id.seatImg1, R.id.seatImg2, R.id.seatImg3,
R.id.seatImg4, R.id.seatImg5, R.id.seatImg6, R.id.seatImg7,
R.id.seatImg8, R.id.seatImg9, R.id.seatImg10};
int[] txtIds = {R.id.seatTxt1, R.id.seatTxt2, R.id.seatTxt3,
R.id.seatTxt4, R.id.seatTxt5, R.id.seatTxt6, R.id.seatTxt7,
R.id.seatTxt8, R.id.seatTxt9, R.id.seatTxt10};
int[] giftIds = {R.id.seatGiftTxt1, R.id.seatGiftTxt2,
R.id.seatGiftTxt3, R.id.seatGiftTxt4, R.id.seatGiftTxt5,
R.id.seatGiftTxt6, R.id.seatGiftTxt7, R.id.seatGiftTxt8,
R.id.seatGiftTxt9, R.id.seatGiftTxt10};
databaseReference.child("seats")
.addValueEventListener(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot snap) {
for (int i = 0; i < 10; i++) {
ImageView si = findViewById(imgIds[i]);
TextView st = findViewById(txtIds[i]);
TextView sg = findViewById(giftIds[i]);
String k = "seat_" + i;
if (snap.hasChild(k)) {
String name = snap.child(k).child("userName")
.getValue(String.class);
String imgB = snap.child(k).child("userImage")
.getValue(String.class);
Long gt = snap.child(k).child("giftTotal")
.getValue(Long.class);
if (st != null) {
String dn = String.valueOf(i + 1);
if (i == 0) dn = "Host";
if (name != null && !name.isEmpty()) dn = name;
st.setText(dn);
}
if (sg != null) {
if (gt != null && gt > 0) sg.setText(gt + " Coins");
else sg.setText("");
}
if (si != null && imgB != null && !imgB.isEmpty()) {
try {
byte[] d = android.util.Base64.decode(
imgB, android.util.Base64.DEFAULT);
Bitmap bmp = BitmapFactory.decodeByteArray(d, 0, d.length);
if (bmp != null) {
RoundedBitmapDrawable rd =
RoundedBitmapDrawableFactory.create(getResources(), bmp);
rd.setCircular(true);
si.setImageDrawable(rd);
si.setPadding(0, 0, 0, 0);
}
} catch (Exception e) {
si.setImageResource(android.R.drawable.ic_menu_my_calendar);
}
}
} else {
if (st != null) {
if (i == 0) st.setText("Host");
else st.setText(String.valueOf(i + 1));
}
if (sg != null) sg.setText("");
if (si != null) {
si.setImageResource(android.R.drawable.ic_menu_my_calendar);
si.setPadding(12, 12, 12, 12);
}
}
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
private void setupSeatInteractions() {
int[] cardIds = {R.id.seatCard1, R.id.seatCard2, R.id.seatCard3,
R.id.seatCard4, R.id.seatCard5, R.id.seatCard6, R.id.seatCard7,
R.id.seatCard8, R.id.seatCard9, R.id.seatCard10};
for (int i = 0; i < cardIds.length; i++) {
final int idx = i;
final int num = i + 1;
View c = findViewById(cardIds[i]);
if (c != null) {
c.setOnClickListener(v -> {
String k = "seat_" + idx;
databaseReference.child("seats").child(k)
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot snap) {
String on = snap.child("userName").getValue(String.class);
String ou = snap.child("userId").getValue(String.class);
boolean occ = on != null && !on.isEmpty();
List<String> opts = new ArrayList<>();
if (occ) {
String o1 = "View Profile (ID: ";
o1 += ou;
o1 += ")";
opts.add(o1);
opts.add("Leave Mic");
} else {
opts.add("Take Mic");
}
opts.add("Cancel");
CharSequence[] arr = opts.toArray(new CharSequence[0]);
AlertDialog.Builder b = new AlertDialog.Builder(
VoiceRoomActivity.this);
String t = "Seat ";
t += num;
b.setTitle(t);
b.setItems(arr, (d, w) -> {
String sel = arr[w].toString();
if (sel.contains("Take Mic")) {
takeSeat(idx, k);
} else if (sel.contains("View Profile")) {
String imgU = snap.child("userImage")
.getValue(String.class);
fetchUserDataAndShowProfile(on, ou, imgU);
} else if (sel.contains("Leave Mic")) {
databaseReference.child("seats").child(k).removeValue();
Toast.makeText(VoiceRoomActivity.this,
"Left seat", Toast.LENGTH_SHORT).show();
}
});
b.show();
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
});
}
}
}
private void takeSeat(int seatIndex, String seatKey) {
String userName = getMyDisplayName();
String userImageUriStr = prefs.getString("USER_PROFILE_IMAGE",
prefs.getString("USER_IMAGE", ""));
for (int i = 0; i < 10; i++) {
final int oldIndex = i;
databaseReference.child("seats").child("seat_" + i)
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
String sUserId = s.child("userId")
.getValue(String.class);
if (sUserId != null
&& sUserId.equals(currentUserId)) {
databaseReference.child("seats")
.child("seat_" + oldIndex).removeValue();
}
}
@Override public void onCancelled(@NonNull DatabaseError e) {}
});
}
databaseReference.child("seats").child(seatKey)
.child("userId").setValue(currentUserId);
databaseReference.child("seats").child(seatKey)
.child("userName").setValue(userName);
databaseReference.child("seats").child(seatKey)
.child("userImage").setValue(userImageUriStr);
databaseReference.child("seats").child(seatKey)
.child("giftTotal").setValue(0L);
databaseReference.child("seats").child(seatKey)
.onDisconnect().removeValue();
Toast.makeText(this, "Joined seat successfully!",
Toast.LENGTH_SHORT).show();
}
private void fetchUserDataAndShowProfile(
String un, String uid, String img) {
if (uid == null || uid.isEmpty()) {
showUserProfileDialog(un, uid, img,
0L, 0L, "M", "BD");
return;
}
userDatabaseRef.child(uid)
.addListenerForSingleValueEvent(new ValueEventListener() {
@Override public void onDataChange(@NonNull DataSnapshot s) {
long send = 0L;
long recv = 0L;
String gender = "M";
String country = "BD";
String realName = un;
if (s.exists()) {
Long a = s.child("sendingCoins").getValue(Long.class);
Long b = s.child("receivingCoins")
.getValue(Long.class);
String dg = s.child("gender").getValue(String.class);
String dc = s.child("country").getValue(String.class);
String dn = s.child("name").getValue(String.class);
if (a != null) send = a;
if (b != null) recv = b;
if (dg != null) {
String lo = dg.toLowerCase();
if (lo.contains("female") || lo.contains("girl")) {
gender = "F";
}
}
if (dc != null && !dc.isEmpty()) country = dc;
if (dn != null && !dn.isEmpty()) realName = dn;
}
showUserProfileDialog(realName, uid, img,
send, recv, gender, country);
}
@Override public void onCancelled(@NonNull DatabaseError e) {
showUserProfileDialog(un, uid, img,
0L, 0L, "M", "BD");
}
});
}
private void showUserProfileDialog(String un, String uid,
String img, long send, long recv,
String gender, String country) {
BottomSheetDialog d = new BottomSheetDialog(this,
R.style.Theme_Design_BottomSheetDialog);
LinearLayout layout = new LinearLayout(this);
layout.setOrientation(LinearLayout.VERTICAL);
layout.setPadding(40, 50, 40, 60);
GradientDrawable bg = new GradientDrawable(
GradientDrawable.Orientation.TOP_BOTTOM,
new int[]{Color.parseColor("#2A1B4E"),
Color.parseColor("#0A0518")});
bg.setCornerRadii(new float[]{60, 60, 60, 60, 0, 0, 0, 0});
layout.setBackground(bg);
ImageView big = new ImageView(this);
LinearLayout.LayoutParams ip =
new LinearLayout.LayoutParams(340, 340);
ip.gravity = Gravity.CENTER_HORIZONTAL;
ip.setMargins(0, 0, 0, 20);
big.setLayoutParams(ip);
big.setScaleType(ImageView.ScaleType.CENTER_CROP);
if (img != null && !img.isEmpty()) {
try {
byte[] b = android.util.Base64.decode(
img, android.util.Base64.DEFAULT);
Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
if (bmp != null) {
RoundedBitmapDrawable rd =
RoundedBitmapDrawableFactory.create(
getResources(), bmp);
rd.setCircular(true);
big.setImageDrawable(rd);
} else {
big.setImageResource(
android.R.drawable.ic_menu_my_calendar);
}
} catch (Exception e) {
big.setImageResource(
android.R.drawable.ic_menu_my_calendar);
}
} else {
big.setImageResource(android.R.drawable.ic_menu_my_calendar);
}
layout.addView(big);
TextView nt = new TextView(this);
String ntTxt = "User";
if (un != null) ntTxt = un;
ntTxt += " ";
ntTxt += gender;
ntTxt += " ";
ntTxt += country;
nt.setText(ntTxt);
nt.setTextColor(Color.WHITE);
nt.setTextSize(22);
nt.setTypeface(null, android.graphics.Typeface.BOLD);
nt.setGravity(Gravity.CENTER);
nt.setPadding(0, 0, 0, 8);
layout.addView(nt);
String did = "493859069";
if (uid != null && !uid.isEmpty()) did = uid;
TextView idT = new TextView(this);
String idTxt = "ID: ";
idTxt += did;
idT.setText(idTxt);
idT.setTextColor(Color.parseColor("#00E676"));
idT.setTextSize(16);
idT.setGravity(Gravity.CENTER);
idT.setPadding(0, 0, 0, 12);
final String fDid = did;
idT.setOnClickListener(v -> {
ClipboardManager cm = (ClipboardManager)
getSystemService(Context.CLIPBOARD_SERVICE);
ClipData clip = ClipData.newPlainText("User ID", fDid);
if (cm != null) {
cm.setPrimaryClip(clip);
String cmMsg = "ID Copied: ";
cmMsg += fDid;
Toast.makeText(this, cmMsg, Toast.LENGTH_SHORT).show();
}
});
layout.addView(idT);
int sl = LevelUtils.calculateLevel(send);
int rl = LevelUtils.calculateLevel(recv);
LinearLayout lr = new LinearLayout(this);
lr.setOrientation(LinearLayout.HORIZONTAL);
lr.setGravity(Gravity.CENTER);
lr.setPadding(0, 0, 0, 20);
String b1 = "Send Lv.";
b1 += sl;
lr.addView(createSoftBadgeView(b1,
LevelUtils.getLevelColor(sl)));
String b2 = "Recv Lv.";
b2 += rl;
lr.addView(createSoftBadgeView(b2,
LevelUtils.getLevelColor(rl)));
layout.addView(lr);
LinearLayout ar = new LinearLayout(this);
ar.setOrientation(LinearLayout.HORIZONTAL);
ar.setGravity(Gravity.CENTER);
Button bf = new Button(this);
bf.setText("Follow");
bf.setBackgroundColor(Color.parseColor("#FFD700"));
bf.setTextColor(Color.BLACK);
bf.setTypeface(null, android.graphics.Typeface.BOLD);
LinearLayout.LayoutParams p1 =
new LinearLayout.LayoutParams(0, 130, 1.0f);
p1.setMargins(0, 0, 8, 0);
bf.setLayoutParams(p1);
bf.setOnClickListener(v -> {
if (uid == null || uid.equals(currentUserId)) {
Toast.makeText(this,
"You cannot follow yourself!",
Toast.LENGTH_SHORT).show();
return;
}
userDatabaseRef.child(uid).child("followers")
.child(currentUserId).setValue(true);
userDatabaseRef.child(currentUserId)
.child("following").child(uid).setValue(true);
bf.setText("Following");
String fMsg = "Followed ";
fMsg += un;
Toast.makeText(this, fMsg, Toast.LENGTH_SHORT).show();
});
ar.addView(bf);
Button bgift = new Button(this);
bgift.setText("Gift");
bgift.setBackgroundColor(Color.parseColor("#FF4081"));
bgift.setTextColor(Color.WHITE);
bgift.setTypeface(null, android.graphics.Typeface.BOLD);
LinearLayout.LayoutParams p2 =
new LinearLayout.LayoutParams(0, 130, 1.0f);
p2.setMargins(8, 0, 0, 0);
bgift.setLayoutParams(p2);
bgift.setOnClickListener(v -> {
d.dismiss();
showNewGiftPanel();
});
ar.addView(bgift);
layout.addView(ar);
Button bfull = new Button(this);
bfull.setText("Full Profile");
bfull.setBackgroundColor(Color.parseColor("#2A1B4E"));
bfull.setTextColor(Color.parseColor("#FFD700"));
LinearLayout.LayoutParams pv =
new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT, 120);
pv.setMargins(0, 15, 0, 0);
bfull.setLayoutParams(pv);
bfull.setOnClickListener(v -> {
d.dismiss();
showFullProfileInside(un, uid, img,
send, recv, gender, country);
});
layout.addView(bfull);
if (uid != null && uid.equals(currentUserId)) {
Button bed = new Button(this);
bed.setText("Edit My Name");
bed.setBackgroundColor(Color.parseColor("#FFD700"));
bed.setTextColor(Color.BLACK);
LinearLayout.LayoutParams pe =
new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT, 120);
pe.setMargins(0, 15, 0, 0);
bed.setLayoutParams(pe);
bed.setOnClickListener(v -> {
d.dismiss();
showEditMyProfileDialog();
});
layout.addView(bed);
}
d.setContentView(layout);
Window w = d.getWindow();
if (w != null) {
w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1750);
w.setGravity(Gravity.BOTTOM);
}
d.show();
}
private void showFullProfileInside(String un, String uid,
String img, long send, long recv,
String gender, String country) {
BottomSheetDialog d = new BottomSheetDialog(this,
R.style.Theme_Design_BottomSheetDialog);
LinearLayout layout = new LinearLayout(this);
layout.setOrientation(LinearLayout.VERTICAL);
layout.setPadding(40, 40, 40, 60);
GradientDrawable bg = new GradientDrawable(
GradientDrawable.Orientation.TOP_BOTTOM,
new int[]{Color.parseColor("#1B1035"),
Color.parseColor("#0A0518")});
bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
layout.setBackground(bg);
TextView title = new TextView(this);
String tt = "Full Profile - ";
tt += un;
title.setText(tt);
title.setTextColor(Color.parseColor("#FFD700"));
title.setTextSize(19);
title.setTypeface(null, android.graphics.Typeface.BOLD);
title.setGravity(Gravity.CENTER);
title.setPadding(0, 0, 0, 20);
layout.addView(title);
TextView info = new TextView(this);
String it = "ID: ";
it += uid;
it += "\nGender: ";
it += gender;
it += " Country: ";
it += country;
it += "\nSend Level: ";
it += LevelUtils.calculateLevel(send);
it += " Recv Level: ";
it += LevelUtils.calculateLevel(recv);
info.setText(it);
info.setTextColor(Color.WHITE);
info.setTextSize(15);
info.setGravity(Gravity.CENTER);
info.setPadding(0, 0, 0, 20);
layout.addView(info);
Button bc = new Button(this);
bc.setText("Back to Room");
bc.setBackgroundColor(Color.parseColor("#FFD700"));
bc.setTextColor(Color.BLACK);
bc.setOnClickListener(v -> d.dismiss());
layout.addView(bc);
d.setContentView(layout);
Window w = d.getWindow();
if (w != null) {
w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1600);
w.setGravity(Gravity.BOTTOM);
}
d.show();
}
private View createSoftBadgeView(String text, int colorInt) {
TextView badge = new TextView(this);
badge.setText(text);
badge.setTextColor(Color.WHITE);
badge.setTextSize(12);
badge.setTypeface(null, android.graphics.Typeface.BOLD);
badge.setPadding(22, 10, 22, 10);
GradientDrawable bg = new GradientDrawable();
bg.setColor(colorInt);
bg.setCornerRadius(30);
badge.setBackground(bg);
LinearLayout.LayoutParams lp =
new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.WRAP_CONTENT,
ViewGroup.LayoutParams.WRAP_CONTENT);
lp.setMargins(0, 0, 16, 0);
badge.setLayoutParams(lp);
return badge;
}
private void showFullLevelCenterDialog() {
BottomSheetDialog dialog = new BottomSheetDialog(this,
R.style.Theme_Design_BottomSheetDialog);
LinearLayout root = new LinearLayout(this);
root.setOrientation(LinearLayout.VERTICAL);
root.setPadding(35, 35, 35, 35);
GradientDrawable bg = new GradientDrawable(
GradientDrawable.Orientation.TOP_BOTTOM,
new int[]{Color.parseColor("#1B1035"),
Color.parseColor("#0A0518")});
bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
root.setBackground(bg);
TextView title = new TextView(this);
title.setText("SR Level Center (Lv.1 - Lv.150)");
title.setTextColor(Color.parseColor("#FFD700"));
title.setTextSize(18);
title.setTypeface(null, android.graphics.Typeface.BOLD);
title.setGravity(Gravity.CENTER);
title.setPadding(0, 0, 0, 20);
root.addView(title);
ScrollView scrollView = new ScrollView(this);
scrollView.setLayoutParams(new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT, 1400));
scrollView.setFillViewport(true);
LinearLayout listContainer = new LinearLayout(this);
listContainer.setOrientation(LinearLayout.VERTICAL);
for (int lvl = 1; lvl <= 150; lvl++) {
LinearLayout row = new LinearLayout(this);
row.setOrientation(LinearLayout.HORIZONTAL);
row.setPadding(25, 20, 25, 20);
row.setGravity(Gravity.CENTER_VERTICAL);
GradientDrawable rBg = new GradientDrawable();
rBg.setColor(Color.parseColor("#2A1B4E"));
rBg.setCornerRadius(20);
row.setBackground(rBg);
LinearLayout.LayoutParams p =
new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT,
ViewGroup.LayoutParams.WRAP_CONTENT);
p.setMargins(0, 0, 0, 14);
row.setLayoutParams(p);
TextView badge = new TextView(this);
badge.setText(LevelUtils.getLevelBadgeText(lvl));
badge.setTextColor(Color.WHITE);
badge.setTextSize(14);
badge.setTypeface(null, android.graphics.Typeface.BOLD);
badge.setGravity(Gravity.CENTER);
badge.setPadding(20, 12, 20, 12);
GradientDrawable bBg = new GradientDrawable();
bBg.setColor(LevelUtils.getLevelColor(lvl));
bBg.setCornerRadius(25);
badge.setBackground(bBg);
badge.setMinWidth(110);
row.addView(badge);
TextView targetTxt = new TextView(this);
String tt = "Level ";
tt += lvl;
tt += " - Target: ";
tt += LevelUtils.getLevelTargetCoinsFormatted(lvl);
targetTxt.setText(tt);
targetTxt.setTextColor(Color.parseColor("#00E676"));
targetTxt.setTextSize(13);
targetTxt.setPadding(20, 0, 0, 0);
row.addView(targetTxt);
listContainer.addView(row);
}
scrollView.addView(listContainer);
root.addView(scrollView);
dialog.setContentView(root);
Window window = dialog.getWindow();
if (window != null) {
window.setLayout(
ViewGroup.LayoutParams.MATCH_PARENT, 1600);
window.setGravity(Gravity.BOTTOM);
}
dialog.show();
}
}
