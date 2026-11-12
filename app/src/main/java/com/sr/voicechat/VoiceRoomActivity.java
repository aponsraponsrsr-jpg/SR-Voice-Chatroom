package com.sr.voicechat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.*;
import java.util.*;

public class VoiceRoomActivity extends AppCompatActivity {

    private String roomOwnerId = "499506534", currentUserId = "", my9DigitId = "", myDisplayName = "User", myCountry = "Bangladesh", myLanguage = "English", myTag = "Friends", myGender = "Male", myPhotoBase64 = "", currentThemeName = "Sunset", selectedRoomLabel = "Friends", galleryMode = "", pendingRoomPhotoBase64 = "", pendingCustomBgBase64 = "", currentRoomPhotoBase64 = "";
    private long myWalletCoins = 0;
    private SharedPreferences prefs;
    private DatabaseReference databaseReference, roomMetaRef, adminsRef, messagesRef, userDatabaseRef;
    private TextView roomTitleText, roomNoticeBarText, roomInfoText, textOnlineCount, textRoomCoins, walletTextInGift, roomEditLogoText;
    private ImageView roomHeaderProfileImg, roomEditLogoView;
    private EditText inputChatMessage;
    private TextView btnChatOption;
    private LinearLayout chatListContainer, chatPopupBar;
    private ScrollView chatScrollView;
    private List<String> roomAdminList = new ArrayList<>();
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_room);
        prefs = getSharedPreferences("SR_PREFS", MODE_PRIVATE);
        currentUserId = prefs.getString("USER_UID", "");
        my9DigitId = prefs.getString("USER_9DIGIT_ID", "");
        myDisplayName = prefs.getString("USER_NAME", "User");
        myPhotoBase64 = prefs.getString("USER_PHOTO_BASE64", "");
        myCountry = prefs.getString("USER_COUNTRY", "Bangladesh");
        myLanguage = prefs.getString("USER_LANGUAGE", "English");
        myGender = prefs.getString("USER_GENDER", "Male");

        String rid = getIntent().getStringExtra("ROOM_OWNER_ID");
        if (rid != null && !rid.isEmpty()) roomOwnerId = rid;
        String myIdFromIntent = getIntent().getStringExtra("MY_9DIGIT_ID");
        if (myIdFromIntent != null && !myIdFromIntent.isEmpty()) my9DigitId = myIdFromIntent;
        String myUidFromIntent = getIntent().getStringExtra("MY_UID");
        if (myUidFromIntent != null && !myUidFromIntent.isEmpty()) currentUserId = myUidFromIntent;

        databaseReference = FirebaseDatabase.getInstance().getReference();
        userDatabaseRef = databaseReference.child("users");
        roomMetaRef = databaseReference.child("rooms").child(roomOwnerId).child("meta");
        adminsRef = databaseReference.child("rooms").child(roomOwnerId).child("admins");
        messagesRef = databaseReference.child("rooms").child(roomOwnerId).child("messages");

        roomTitleText = findViewById(R.id.roomTitleText);
        roomInfoText = findViewById(R.id.roomInfoText);
        roomNoticeBarText = findViewById(R.id.roomNoticeBarText);
        textOnlineCount = findViewById(R.id.textOnlineCount);
        textRoomCoins = findViewById(R.id.textRoomCoins);
        roomHeaderProfileImg = findViewById(R.id.roomHeaderProfileImg);
        chatListContainer = findViewById(R.id.chatListContainer);
        chatScrollView = findViewById(R.id.chatScrollView);
        chatPopupBar = findViewById(R.id.chatPopupBar);
        inputChatMessage = findViewById(R.id.inputChatMessage);
        btnChatOption = findViewById(R.id.btnChatOption);
        if (chatPopupBar != null) chatPopupBar.setVisibility(View.GONE);

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                try {
                    Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), result.getData().getData());
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    String b64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                    if ("ROOM_LOGO".equals(galleryMode)) {
                        pendingRoomPhotoBase64 = b64;
                        if (roomEditLogoView != null) roomEditLogoView.setImageBitmap(bmp);
                        if (roomEditLogoText != null) roomEditLogoText.setVisibility(View.GONE);
                        Toast.makeText(this, "Room Photo Selected ✅ - Save koro", Toast.LENGTH_SHORT).show();
                    } else if ("CUSTOM_BG".equals(galleryMode)) {
                        pendingCustomBgBase64 = b64;
                        Toast.makeText(this, "Background Selected ✅ - Room set e Save koro", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) { Toast.makeText(this, "Pic nite pari nai", Toast.LENGTH_SHORT).show(); }
            }
        });

        if (roomHeaderProfileImg != null) roomHeaderProfileImg.setOnClickListener(v -> showHeaderProfileMembersDialog());
        View btnOnline = findViewById(R.id.btnOnlineList);
        if (btnOnline != null) btnOnline.setOnClickListener(v -> showOnlineListDialog());
        View btnPower = findViewById(R.id.btnPower);
        if (btnPower != null) btnPower.setOnClickListener(v -> showExitDialog());
        View btnRank = findViewById(R.id.btnInRoomRanking);
        if (btnRank != null) btnRank.setOnClickListener(v -> showInRoomRankingDialog());
        View btnGift = findViewById(R.id.btnGiftBox);
        if (btnGift != null) btnGift.setOnClickListener(v -> showNewGiftPanel());
        View btnGame = findViewById(R.id.btnGame);
        if (btnGame != null) btnGame.setOnClickListener(v -> Toast.makeText(this, "Game - Coming Soon 🎮", Toast.LENGTH_SHORT).show());
        View btnTools = findViewById(R.id.btnRoomTools);
        if (btnTools != null) btnTools.setOnClickListener(v -> showIconToolsDialog(roomOwnerId.equals(my9DigitId) || roomOwnerId.equals(currentUserId)));
        View btnMic = findViewById(R.id.btnMicToggle);
        if (btnMic != null) btnMic.setOnClickListener(v -> Toast.makeText(this, "Mic - Coming Soon 🎤", Toast.LENGTH_SHORT).show());

        setupChatPopup();
        setupSeatInteractions();
        ensureMyIdentity();
        syncLatestUserProfile();
        listenToRoomDetails();
        listenToRoomPhoto();
        listenToRoomTheme();
        listenToRoomAdmins();
        listenToOnlineMembers();
        listenToFirebaseSeats();
        listenToChatRealtime();
        listenToMyWallet();
    }

    // ================= HELPERS =================
    private void setDialog85(BottomSheetDialog d){ Window w=d.getWindow(); if(w!=null){ int sh=getResources().getDisplayMetrics().heightPixels; w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(sh*0.85)); w.setGravity(Gravity.BOTTOM); } }
    private void setDialog75(BottomSheetDialog d){ Window w=d.getWindow(); if(w!=null){ int sh=getResources().getDisplayMetrics().heightPixels; w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(sh*0.75)); w.setGravity(Gravity.BOTTOM); } }
    private android.app.Dialog makeFullScreenDialog(){ android.app.Dialog d=new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen); Window w=d.getWindow(); if(w!=null){ w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); w.setGravity(Gravity.BOTTOM); w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)); } return d; }
    private String flagForCountry(String c){ if(c==null) return "🌍"; String s=c.toLowerCase(); if(s.contains("bangladesh")||s.contains("bd")) return "🇧🇩"; if(s.contains("india")) return "🇮🇳"; if(s.contains("pakistan")) return "🇵🇰"; if(s.contains("saudi")) return "🇸🇦"; if(s.contains("oman")) return "🇴🇲"; if(s.contains("uae")||s.contains("emirates")) return "🇦🇪"; if(s.contains("usa")||s.contains("america")) return "🇺🇸"; if(s.contains("uk")||s.contains("england")) return "🇬🇧"; return "🌍"; }
    private String genderIcon(String g){ if(g==null) return "👤"; String s=g.toLowerCase(); if(s.contains("female")||s.contains("girl")||s.contains("f")) return "👧"; return "👦"; }
    private Bitmap decodeBase64(String b64){ if(b64==null||b64.isEmpty()) return null; try{ byte[] b=Base64.decode(b64,Base64.DEFAULT); return BitmapFactory.decodeByteArray(b,0,b.length); }catch(Exception e){ return null; } }
    private void copyRoomId(){ ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE); if(cm!=null){ cm.setPrimaryClip(ClipData.newPlainText("Room ID", roomOwnerId)); Toast.makeText(this,"Room ID Copied: "+roomOwnerId+" ✅",Toast.LENGTH_SHORT).show(); } }

    // ================= IDENTITY =================
    private void ensureMyIdentity(){
        if(my9DigitId == null) my9DigitId = "";
        if(my9DigitId.isEmpty()) my9DigitId = prefs.getString("USER_9DIGIT_ID", "");
        if(myDisplayName == null || myDisplayName.equals("User")) myDisplayName = prefs.getString("USER_NAME", "User");
        if(myPhotoBase64 == null || myPhotoBase64.isEmpty()) myPhotoBase64 = prefs.getString("USER_PHOTO_BASE64", "");
        if(!my9DigitId.isEmpty()){
            userDatabaseRef.orderByChild("my9DigitId").equalTo(my9DigitId).addListenerForSingleValueEvent(new ValueEventListener(){
                @Override public void onDataChange(@NonNull DataSnapshot snap){
                    if(snap.exists()){
                        for(DataSnapshot u : snap.getChildren()){
                            currentUserId = u.getKey();
                            if(u.child("name").getValue()!=null) myDisplayName = String.valueOf(u.child("name").getValue());
                            if(u.child("photoBase64").getValue()!=null) myPhotoBase64 = String.valueOf(u.child("photoBase64").getValue());
                            if(u.child("country").getValue()!=null) myCountry = String.valueOf(u.child("country").getValue());
                            if(u.child("gender").getValue()!=null) myGender = String.valueOf(u.child("gender").getValue());
                            prefs.edit().putString("USER_UID", currentUserId).putString("USER_NAME", myDisplayName).putString("USER_PHOTO_BASE64", myPhotoBase64).putString("USER_COUNTRY", myCountry).putString("USER_GENDER", myGender).apply();
                            pushOnlineNow();
                            break;
                        }
                    } else { syncByUidFallback(); }
                }
                @Override public void onCancelled(@NonNull DatabaseError e){ syncByUidFallback(); }
            });
        } else { syncByUidFallback(); }
    }
    private void syncByUidFallback(){
        if(currentUserId == null || currentUserId.isEmpty()) { pushOnlineNow(); return; }
        userDatabaseRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                if(s.exists()){
                    if(s.child("my9DigitId").getValue()!=null) my9DigitId = String.valueOf(s.child("my9DigitId").getValue());
                    if(s.child("name").getValue()!=null) myDisplayName = String.valueOf(s.child("name").getValue());
                    if(s.child("photoBase64").getValue()!=null) myPhotoBase64 = String.valueOf(s.child("photoBase64").getValue());
                    if(s.child("country").getValue()!=null) myCountry = String.valueOf(s.child("country").getValue());
                    if(s.child("gender").getValue()!=null) myGender = String.valueOf(s.child("gender").getValue());
                    prefs.edit().putString("USER_9DIGIT_ID", my9DigitId).putString("USER_NAME", myDisplayName).putString("USER_PHOTO_BASE64", myPhotoBase64).apply();
                }
                pushOnlineNow();
            }
            @Override public void onCancelled(@NonNull DatabaseError e){ pushOnlineNow(); }
        });
    }
    private void syncLatestUserProfile(){
        if(currentUserId.isEmpty() && my9DigitId.isEmpty()) return;
        String key = !currentUserId.isEmpty() ? currentUserId : my9DigitId;
        userDatabaseRef.child(key).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                if(s.exists()){
                    String dbName=s.child("name").getValue(String.class); if(dbName!=null && !dbName.isEmpty()){ myDisplayName=dbName; prefs.edit().putString("USER_NAME",dbName).apply(); }
                    String v=s.child("my9DigitId").getValue(String.class); if(v!=null && !v.isEmpty()){ my9DigitId=v; prefs.edit().putString("USER_9DIGIT_ID",v).apply(); }
                    String dbCountry=s.child("country").getValue(String.class); if(dbCountry!=null && !dbCountry.isEmpty()){ myCountry=dbCountry; }
                    String dbLang=s.child("language").getValue(String.class); if(dbLang!=null && !dbLang.isEmpty()){ myLanguage=dbLang; }
                    String dbGender=s.child("gender").getValue(String.class); if(dbGender!=null && !dbGender.isEmpty()) myGender=dbGender;
                    String ph=s.child("photoBase64").getValue(String.class); if(ph!=null && !ph.isEmpty()){ myPhotoBase64=ph; prefs.edit().putString("USER_PHOTO_BASE64",ph).apply(); }
                    pushOnlineNow();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
    }

    // ================= ONLINE - LOCK KHOLA - USER HOLEO UTHBE =================
    private void pushOnlineNow(){
        String key = my9DigitId;
        if(key == null || key.isEmpty()) key = currentUserId;
        if(key == null || key.isEmpty()) key = roomOwnerId;
        if(key == null || key.isEmpty() || databaseReference == null) return;
        String nameToShow = myDisplayName;
        if(nameToShow == null || nameToShow.isEmpty()) nameToShow = prefs.getString("USER_NAME", "User");
        if(nameToShow.equals("User") && !key.isEmpty()) nameToShow = "User " + key;
        Map<String,Object> m = new HashMap<>();
        m.put("name", nameToShow);
        m.put("photoBase64", myPhotoBase64 != null ? myPhotoBase64 : "");
        m.put("level", "0");
        m.put("country", myCountry);
        m.put("gender", myGender);
        m.put("my9DigitId", key);
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").child(key).setValue(m);
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").child(key).onDisconnect().removeValue();
        databaseReference.child("online_users").child(key).setValue(m);
        databaseReference.child("online_users").child(key).onDisconnect().removeValue();
    }
    private void listenToOnlineMembers(){
        pushOnlineNow();
        if(databaseReference==null) return;
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").addValueEventListener(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                if(textOnlineCount!=null) textOnlineCount.setText(String.valueOf(s.getChildrenCount()));
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
    }

    // ================= CHAT - LOCK KHOLA - SMS SATHE SATHE JABE =================
    private void showChatPopup(){
        if(chatPopupBar!=null) chatPopupBar.setVisibility(View.VISIBLE);
        if(inputChatMessage!=null){ inputChatMessage.requestFocus(); InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); if(imm!=null) imm.showSoftInput(inputChatMessage, InputMethodManager.SHOW_IMPLICIT); }
    }
    private void hideChatPopup(){ if(chatPopupBar!=null) chatPopupBar.setVisibility(View.GONE); if(inputChatMessage!=null){ inputChatMessage.setText(""); InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); if(imm!=null) imm.hideSoftInputFromWindow(inputChatMessage.getWindowToken(),0); } }
    private void setupChatPopup(){
        View btnOpen=findViewById(R.id.btnOpenChat); if(btnOpen!=null) btnOpen.setOnClickListener(v->showChatPopup());
        if(inputChatMessage!=null){ inputChatMessage.setOnEditorActionListener((v,a,e)->{ sendChatMessage(); return true; }); }
        if(btnChatOption!=null) btnChatOption.setOnClickListener(v->sendChatMessage());
        TextView e=findViewById(R.id.btnEmoji); if(e!=null) e.setOnClickListener(v->Toast.makeText(this,"Emoji - Coming Soon 😊",Toast.LENGTH_SHORT).show());
        TextView g=findViewById(R.id.btnGallery); if(g!=null) g.setOnClickListener(v->Toast.makeText(this,"Gallery - Future te allow hobe 🖼️",Toast.LENGTH_SHORT).show());
        TextView c=findViewById(R.id.btnCamera); if(c!=null) c.setOnClickListener(v->Toast.makeText(this,"Camera - Future te allow hobe 📷",Toast.LENGTH_SHORT).show());
    }
    @Override public void onBackPressed(){ if(chatPopupBar!=null && chatPopupBar.getVisibility()==View.VISIBLE){ hideChatPopup(); return; } super.onBackPressed(); }
    private void sendChatMessage(){
        if(inputChatMessage==null) return; String msg=inputChatMessage.getText().toString().trim();
        if(msg.isEmpty()||messagesRef==null) return;
        String nameToSend = myDisplayName;
        if(nameToSend == null || nameToSend.isEmpty()) nameToSend = prefs.getString("USER_NAME", "User");
        if(nameToSend.equals("User")) ensureMyIdentity();
        String idToSend = my9DigitId;
        if(idToSend == null || idToSend.isEmpty()) idToSend = currentUserId;
        if(idToSend == null || idToSend.isEmpty()) idToSend = roomOwnerId;
        Map<String,Object> m=new HashMap<>(); m.put("userId", idToSend); m.put("userName", nameToSend); m.put("text", msg); m.put("photoBase64", myPhotoBase64 != null ? myPhotoBase64 : ""); m.put("gender", myGender); m.put("country", myCountry); m.put("time", System.currentTimeMillis()); messagesRef.push().setValue(m); hideChatPopup(); pushOnlineNow();
    }
    private void listenToChatRealtime(){ if(messagesRef==null||chatListContainer==null) return; messagesRef.limitToLast(100).addValueEventListener(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot s){ chatListContainer.removeAllViews(); if(!s.exists()) return; for(DataSnapshot ds:s.getChildren()){ String uname=ds.child("userName").getValue(String.class); if(uname==null) uname="User"; String txt=ds.child("text").getValue(String.class); if(txt==null) txt=""; String uid=ds.child("userId").getValue(String.class); if(uid==null) uid=""; String photo=ds.child("photoBase64").getValue(String.class); String g=ds.child("gender").getValue(String.class); String co=ds.child("country").getValue(String.class); LinearLayout row=new LinearLayout(VoiceRoomActivity.this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.TOP); row.setPadding(4,6,4,6); ImageView av=new ImageView(VoiceRoomActivity.this); av.setLayoutParams(new LinearLayout.LayoutParams(72,72)); av.setImageResource(android.R.drawable.ic_menu_gallery); Bitmap bmp=decodeBase64(photo); if(bmp!=null) av.setImageBitmap(bmp); row.addView(av); LinearLayout box=new LinearLayout(VoiceRoomActivity.this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(10,0,0,0); box.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f)); row.addView(box); TextView tvN=new TextView(VoiceRoomActivity.this); tvN.setText(uname+" "+genderIcon(g)+" "+flagForCountry(co)+" • ID: "+uid); tvN.setTextColor(Color.parseColor("#3E2F00")); tvN.setTextSize(12); tvN.setTypeface(null, android.graphics.Typeface.BOLD); box.addView(tvN); TextView tvM=new TextView(VoiceRoomActivity.this); tvM.setText(txt); tvM.setTextColor(Color.parseColor("#222222")); tvM.setTextSize(14); box.addView(tvM); chatListContainer.addView(row); } if(chatScrollView!=null) chatScrollView.post(()->chatScrollView.fullScroll(View.FOCUS_DOWN)); } @Override public void onCancelled(@NonNull DatabaseError e){} }); }

    // ================= SEATS =================
    private void setupSeatInteractions(){ for(int i=0;i<10;i++){ int idx=i; int resId=getResources().getIdentifier("seat_"+idx,"id",getPackageName()); View seat=findViewById(resId); if(seat!=null) seat.setOnClickListener(v->{ DatabaseReference seatRef=databaseReference.child("rooms").child(roomOwnerId).child("seats").child("seat_"+idx); seatRef.addListenerForSingleValueEvent(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot s){ if(!s.exists()){ Map<String,Object> m=new HashMap<>(); m.put("userId", my9DigitId.isEmpty()?currentUserId:my9DigitId); m.put("userName", myDisplayName); m.put("photoBase64", myPhotoBase64!=null?myPhotoBase64:""); seatRef.setValue(m); Toast.makeText(VoiceRoomActivity.this,"Seat "+(idx+1)+" nile ✅",Toast.LENGTH_SHORT).show(); } else { String uid=s.child("userId").getValue(String.class); if(uid!=null && (uid.equals(currentUserId)||uid.equals(my9DigitId))){ seatRef.removeValue(); } else { Toast.makeText(VoiceRoomActivity.this,"Ei seat e onno jon ache",Toast.LENGTH_SHORT).show(); } } } @Override public void onCancelled(@NonNull DatabaseError e){} }); }); } }
    private void listenToFirebaseSeats(){ if(databaseReference==null) return; databaseReference.child("rooms").child(roomOwnerId).child("seats").addValueEventListener(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot s){ for(int i=0;i<10;i++){ int seatImgId=getResources().getIdentifier("seatImg_"+i,"id",getPackageName()); TextView seatImg=findViewById(seatImgId); if(seatImg==null) continue; DataSnapshot seatSnap=s.child("seat_"+i); if(seatSnap.exists()){ String photo=seatSnap.child("photoBase64").getValue(String.class); Bitmap bmp=decodeBase64(photo); if(bmp!=null){ seatImg.setText(""); seatImg.setBackground(new android.graphics.drawable.BitmapDrawable(getResources(),bmp)); } else seatImg.setText("👤"); String nm=seatSnap.child("userName").getValue(String.class); View seatLayout=findViewById(getResources().getIdentifier("seat_"+i,"id",getPackageName())); if(seatLayout instanceof LinearLayout){ LinearLayout ll=(LinearLayout)seatLayout; if(ll.getChildCount()>=2 && ll.getChildAt(1) instanceof TextView) ((TextView)ll.getChildAt(1)).setText(nm!=null?nm:""); } } else { seatImg.setText("🪑"); seatImg.setBackgroundColor(Color.parseColor("#FFE9A8")); } } } @Override public void onCancelled(@NonNull DatabaseError e){} }); }

    // ================= ROOM DETAILS / PHOTO / THEME / ADMIN / WALLET =================
    private void listenToRoomDetails(){ if(roomMetaRef==null) return; roomMetaRef.addValueEventListener(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot s){ String rn=s.child("roomName").getValue(String.class); if(rn!=null && !rn.isEmpty() && roomTitleText!=null) roomTitleText.setText(rn); if(roomInfoText!=null) roomInfoText.setText("ID: "+roomOwnerId); String ann=s.child("announcement").getValue(String.class); if(roomNoticeBarText!=null){ if(ann==null || ann.isEmpty() || ann.equals("wlc")){ roomNoticeBarText.setVisibility(View.GONE); roomNoticeBarText.setText(""); } else { roomNoticeBarText.setVisibility(View.VISIBLE); roomNoticeBarText.setText(ann); } } String lb=s.child("roomLabel").getValue(String.class); if(lb!=null && !lb.isEmpty()) selectedRoomLabel=lb; } @Override public void onCancelled(@NonNull DatabaseError e){} }); }
    private void listenToRoomPhoto(){ if(roomMetaRef==null) return; roomMetaRef.addValueEventListener(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot s){ String rp=s.child("roomPhotoBase64").getValue(String.class); if(rp!=null && !rp.isEmpty()){ currentRoomPhotoBase64=rp; Bitmap bmp=decodeBase64(rp); if(bmp!=null && roomHeaderProfileImg!=null) roomHeaderProfileImg.setImageBitmap(bmp); } } @Override public void onCancelled(@NonNull DatabaseError e){} }); }
    private void listenToRoomTheme(){ if(roomMetaRef==null) return; roomMetaRef.addValueEventListener(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot s){ String n=s.child("roomTheme").getValue(String.class); if(n!=null && !n.isEmpty()) currentThemeName=n; } @Override public void onCancelled(@NonNull DatabaseError e){} }); }
    private void listenToRoomAdmins(){ if(adminsRef==null) return; adminsRef.addValueEventListener(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot s){ roomAdminList.clear(); for(DataSnapshot ds:s.getChildren()) if(Boolean.TRUE.equals(ds.getValue(Boolean.class))) roomAdminList.add(ds.getKey()); } @Override public void onCancelled(@NonNull DatabaseError e){} }); }
    private void listenToMyWallet(){ if(currentUserId.isEmpty()) return; userDatabaseRef.child(currentUserId).addValueEventListener(new ValueEventListener(){ @Override public void onDataChange(@NonNull DataSnapshot s){ Long coins=s.child("coins").getValue(Long.class); long w=(coins!=null)?coins:0L; myWalletCoins=w; if(walletTextInGift!=null) walletTextInGift.setText("Coins: "+String.format("%,d",w)); if(textRoomCoins!=null) textRoomCoins.setText("🪙 "+String.format("%,d",w)); } @Override public void onCancelled(@NonNull DatabaseError e){} }); }

    // ================= ONLINE LIST =================
    private void showOnlineListDialog(){
        android.app.Dialog dialog=makeFullScreenDialog();
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.WHITE); root.setPadding(24,20,24,24);
        LinearLayout top=new LinearLayout(this); top.setOrientation(LinearLayout.HORIZONTAL); top.setGravity(Gravity.CENTER_VERTICAL); root.addView(top);
        TextView title=new TextView(this); title.setText("Online list"); title.setTextColor(Color.parseColor("#222222")); title.setTextSize(19); title.setTypeface(null,android.graphics.Typeface.BOLD); title.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f)); top.addView(title);
        TextView close=new TextView(this); close.setText("✕"); close.setTextSize(22); close.setTextColor(Color.parseColor("#888888")); close.setPadding(10,10,10,10); close.setOnClickListener(v->dialog.dismiss()); top.addView(close);
        TextView sub=new TextView(this); sub.setText("Room e ekhon jara active ache"); sub.setTextColor(Color.parseColor("#999999")); sub.setTextSize(13); sub.setPadding(0,4,0,10); root.addView(sub);
        ScrollView sv=new ScrollView(this); LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); sv.addView(list); root.addView(sv);
        pushOnlineNow();
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").addValueEventListener(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                list.removeAllViews();
                if(!s.exists() || s.getChildrenCount()==0){
                    databaseReference.child("online_users").addListenerForSingleValueEvent(new ValueEventListener(){
                        @Override public void onDataChange(@NonNull DataSnapshot g){
                            if(!g.exists() || g.getChildrenCount()==0){ TextView tv=new TextView(VoiceRoomActivity.this); tv.setText("No one online"); tv.setTextColor(Color.parseColor("#AAAAAA")); tv.setGravity(Gravity.CENTER); tv.setPadding(0,40,0,40); list.addView(tv); return; }
                            for(DataSnapshot ds : g.getChildren()) showOneOnlineUser(list, ds, dialog);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError e){}
                    });
                    return;
                }
                for(DataSnapshot ds : s.getChildren()) showOneOnlineUser(list, ds, dialog);
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
        dialog.setContentView(root); dialog.show();
    }
    private void showOneOnlineUser(LinearLayout list, DataSnapshot ds, android.app.Dialog dialog){
        String uidKey = ds.getKey(); if(uidKey == null) return;
        String onName = ds.child("name").getValue(String.class);
        String onPhoto = ds.child("photoBase64").getValue(String.class);
        String onCountry = ds.child("country").getValue(String.class);
        String onGender = ds.child("gender").getValue(String.class);
        String onLevel = ds.child("level").getValue(String.class);
        userDatabaseRef.orderByChild("my9DigitId").equalTo(uidKey).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot us){
                String nm = onName != null ? onName : "User";
                String ph = onPhoto != null ? onPhoto : "";
                String co = onCountry != null ? onCountry : "";
                String ge = onGender != null ? onGender : "Male";
                String lv = onLevel != null ? onLevel : "0";
                if(us.exists()){ for(DataSnapshot u : us.getChildren()){ if(u.child("name").getValue()!=null) nm = String.valueOf(u.child("name").getValue()); if(u.child("photoBase64").getValue()!=null && !String.valueOf(u.child("photoBase64").getValue()).isEmpty()) ph = String.valueOf(u.child("photoBase64").getValue()); if(u.child("country").getValue()!=null) co = String.valueOf(u.child("country").getValue()); if(u.child("gender").getValue()!=null) ge = String.valueOf(u.child("gender").getValue()); if(u.child("level").getValue()!=null) lv = String.valueOf(u.child("level").getValue()); break; } }
                addOnlineRow(list, uidKey, nm, ph, lv, ge, co, dialog);
            }
            @Override public void onCancelled(@NonNull DatabaseError e){ addOnlineRow(list, uidKey, onName!=null?onName:"User", onPhoto!=null?onPhoto:"", "0", "Male", "", dialog); }
        });
    }
    private void addOnlineRow(LinearLayout list, String uid, String name, String photo, String level, String gender, String country, android.app.Dialog dialog){
        for(int i=0;i<list.getChildCount();i++){ View v=list.getChildAt(i); Object tag=v.getTag(); if(tag!=null && String.valueOf(tag).equals("ONLINE_"+uid)) return; }
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(6,14,6,14); row.setTag("ONLINE_"+uid);
        ImageView avatar=new ImageView(this); avatar.setLayoutParams(new LinearLayout.LayoutParams(110,110)); avatar.setImageResource(android.R.drawable.ic_menu_gallery); Bitmap bmp=decodeBase64(photo); if(bmp!=null) avatar.setImageBitmap(bmp); row.addView(avatar);
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(16,0,0,0); box.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f)); row.addView(box);
        TextView tvName=new TextView(this); tvName.setText(name+"  "+genderIcon(gender)+"  "+flagForCountry(country)); tvName.setTextColor(Color.parseColor("#222222")); tvName.setTextSize(16); tvName.setTypeface(null,android.graphics.Typeface.BOLD); box.addView(tvName);
        TextView tvId=new TextView(this); tvId.setText("ID: "+uid); tvId.setTextColor(Color.parseColor("#888888")); tvId.setTextSize(13); box.addView(tvId);
        LinearLayout lvRow=new LinearLayout(this); lvRow.setOrientation(LinearLayout.HORIZONTAL); lvRow.setPadding(0,6,0,0); box.addView(lvRow);
        TextView lv1=new TextView(this); lv1.setText(" Lv"+level+" "); lv1.setTextColor(Color.WHITE); lv1.setTextSize(12); lv1.setBackgroundColor(Color.parseColor("#FF9800")); lv1.setPadding(10,4,10,4); lvRow.addView(lv1);
        TextView lv2=new TextView(this); lv2.setText(" Lv0 "); lv2.setTextColor(Color.WHITE); lv2.setTextSize(12); lv2.setBackgroundColor(Color.parseColor("#9C27B0")); lv2.setPadding(10,4,10,4); LinearLayout.LayoutParams lp2=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT); lp2.setMargins(8,0,0,0); lv2.setLayoutParams(lp2); lvRow.addView(lv2);
        if(uid.equals(roomOwnerId)){ TextView badge=new TextView(this); badge.setText("👑 Owner"); badge.setTextColor(Color.parseColor("#FF9800")); badge.setTypeface(null,android.graphics.Typeface.BOLD); badge.setTextSize(12); row.addView(badge); }
        else if(roomAdminList.contains(uid)){ TextView badge=new TextView(this); badge.setText("⭐ Admin"); badge.setTextColor(Color.parseColor("#4CAF50")); badge.setTypeface(null,android.graphics.Typeface.BOLD); badge.setTextSize(12); row.addView(badge); }
        row.setOnClickListener(v->{ dialog.dismiss(); showMiniProfileCard(uid); }); list.addView(row);
    }

    // ================= EXIT / PROFILE / GIFT / TOOLS / RANKING / ROOM EDIT =================
    private void showExitDialog(){ AlertDialog.Builder b=new AlertDialog.Builder(this); b.setTitle("Exit Room?"); b.setMessage("Exit room or Minimize to Home?"); b.setPositiveButton("Exit Room",(d,w)->{ d.dismiss(); leaveRoomAndFinish(); }); b.setNeutralButton("Minimize",(d,w)->{ d.dismiss(); leaveRoomKeepHome(); Intent home=new Intent(VoiceRoomActivity.this,MainActivity.class); home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP); startActivity(home); finish(); }); b.setNegativeButton("Cancel",(d,w)->d.dismiss()); b.show(); }
    private void leaveRoomKeepHome(){ String key=my9DigitId.isEmpty()?currentUserId:my9DigitId; if(key!=null && !key.isEmpty()){ databaseReference.child("rooms").child(roomOwnerId).child("online_users").child(key).removeValue(); databaseReference.child("online_users").child(key).removeValue(); } }
    private void leaveRoomAndFinish(){ leaveRoomKeepHome(); Intent intent=new Intent(VoiceRoomActivity.this,MainActivity.class); intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP); startActivity(intent); finish(); }
    private void showHeaderProfileMembersDialog(){ showOnlineListDialog(); }
    private void showMiniProfileCard(String uid){
        BottomSheetDialog dialog=new BottomSheetDialog(this,R.style.Theme_Design_BottomSheetDialog); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER); root.setPadding(28,24,28,28); root.setBackgroundColor(Color.WHITE);
        TextView t=new TextView(this); t.setText("Profile\nID: "+uid); t.setTextColor(Color.parseColor("#222222")); t.setTextSize(16); t.setTypeface(null,android.graphics.Typeface.BOLD); t.setGravity(Gravity.CENTER); root.addView(t);
        Button b=new Button(this); b.setText("Copy ID"); b.setOnClickListener(v->{ ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE); if(cm!=null) cm.setPrimaryClip(ClipData.newPlainText("ID",uid)); Toast.makeText(this,"Copied ✅",Toast.LENGTH_SHORT).show(); dialog.dismiss(); }); root.addView(b);
        dialog.setContentView(root); setDialog75(dialog); dialog.show();
    }
    private void showNewGiftPanel(){
        BottomSheetDialog dialog=new BottomSheetDialog(this,R.style.Theme_Design_BottomSheetDialog); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,20,24,24); root.setBackgroundColor(Color.WHITE);
        TextView title=new TextView(this); title.setText("🎁 Gift Box"); title.setTextColor(Color.parseColor("#222222")); title.setTextSize(18); title.setTypeface(null,android.graphics.Typeface.BOLD); title.setGravity(Gravity.CENTER); root.addView(title);
        walletTextInGift=new TextView(this); walletTextInGift.setText("Coins: "+myWalletCoins); walletTextInGift.setTextColor(Color.parseColor("#B8860B")); walletTextInGift.setGravity(Gravity.CENTER); walletTextInGift.setPadding(0,8,0,16); root.addView(walletTextInGift);
        String[][] gifts={{"🌹","Rose","10"},{"💎","Diamond","100"},{"🚗","Car","1000"},{"🏰","Castle","10000"}};
        for(String[] g:gifts){ Button btn=new Button(this); btn.setText(g[0]+" "+g[1]+" - 🪙 "+g[2]); btn.setOnClickListener(v->{ Toast.makeText(this,g[1]+" Sent ✅",Toast.LENGTH_SHORT).show(); dialog.dismiss(); }); root.addView(btn); }
        dialog.setContentView(root); setDialog75(dialog); dialog.show();
    }
    private void showIconToolsDialog(boolean isOwner){
        BottomSheetDialog dialog=new BottomSheetDialog(this,R.style.Theme_Design_BottomSheetDialog); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(28,20,28,28); root.setBackgroundColor(Color.parseColor("#FFF8E1"));
        TextView title=new TextView(this); title.setText("Room Settings"); title.setTextColor(Color.parseColor("#222222")); title.setTextSize(18); title.setTypeface(null,android.graphics.Typeface.BOLD); title.setGravity(Gravity.CENTER); title.setPadding(0,0,0,16); root.addView(title);
        TextView basic=new TextView(this); basic.setText("Basic Tools"); basic.setTextColor(Color.parseColor("#888888")); basic.setTextSize(13); basic.setTypeface(null,android.graphics.Typeface.BOLD); basic.setPadding(4,0,0,8); root.addView(basic);
        root.addView(createToolsGrid(new String[][]{{"🔒","Lock off"},{"⚙️","Room set"},{"✏️","Room Edit"},{"🧹","Clear charm"},{"🗑️","Clear chat"},{"🎤","Change mic"},{"🎨","Mic Theme"},{"👗","Dress"},{"🔔","Lucky Sound"},{"🎙️","Voice User"},{"🚪","Kick out record"}},dialog));
        TextView other=new TextView(this); other.setText("Other Tools"); other.setTextColor(Color.parseColor("#888888")); other.setTextSize(13); other.setTypeface(null,android.graphics.Typeface.BOLD); other.setPadding(4,20,0,8); root.addView(other);
        root.addView(createToolsGrid(new String[][]{{"▶️","YouTube"},{"🍀","Lucky float"},{"⚔️","Room PK"},{"📡","Broadcast"}},dialog));
        dialog.setContentView(root); setDialog85(dialog); dialog.show();
    }
    private LinearLayout createToolsGrid(String[][] items,BottomSheetDialog dialog){ LinearLayout grid=new LinearLayout(this); grid.setOrientation(LinearLayout.VERTICAL); for(int i=0;i<items.length;i+=4){ LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setWeightSum(4); for(int j=0;j<4;j++){ int idx=i+j; LinearLayout cell=new LinearLayout(this); cell.setOrientation(LinearLayout.VERTICAL); cell.setGravity(Gravity.CENTER); cell.setPadding(4,12,4,12); cell.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f)); if(idx<items.length){ TextView icon=new TextView(this); icon.setText(items[idx][0]); icon.setTextSize(26); icon.setGravity(Gravity.CENTER); cell.addView(icon); TextView label=new TextView(this); label.setText(items[idx][1]); label.setTextColor(Color.parseColor("#333333")); label.setTextSize(11); label.setGravity(Gravity.CENTER); label.setPadding(0,6,0,0); cell.addView(label); String action=items[idx][1]; cell.setOnClickListener(v->{ dialog.dismiss(); if(action.equals("Clear chat") && messagesRef!=null){ messagesRef.removeValue(); Toast.makeText(this,"Chat Cleared!",Toast.LENGTH_SHORT).show(); } else if(action.equals("Room set")||action.equals("Room Edit")){ showRoomEditDialog(); } else Toast.makeText(this,action+" - Coming Soon",Toast.LENGTH_SHORT).show(); }); } row.addView(cell); } grid.addView(row); } return grid; }
    private void showInRoomRankingDialog(){
        android.app.Dialog dialog=makeFullScreenDialog(); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(20,16,20,20); root.setBackgroundColor(Color.parseColor("#1B1035"));
        TextView t=new TextView(this); t.setText("🏆 In-Room Ranking"); t.setTextColor(Color.WHITE); t.setTextSize(18); t.setTypeface(null,android.graphics.Typeface.BOLD); t.setGravity(Gravity.CENTER); root.addView(t);
        TextView c=new TextView(this); c.setText("✕ Close"); c.setTextColor(Color.WHITE); c.setGravity(Gravity.CENTER); c.setPadding(0,20,0,0); c.setOnClickListener(v->dialog.dismiss()); root.addView(c);
        TextView info=new TextView(this); info.setText("Day / Week / Month - Coming Soon"); info.setTextColor(Color.parseColor("#AAAAAA")); info.setGravity(Gravity.CENTER); info.setPadding(0,20,0,0); root.addView(info);
        dialog.setContentView(root); dialog.show();
    }
    private void showRoomEditDialog(){
        BottomSheetDialog dialog=new BottomSheetDialog(this,R.style.Theme_Design_BottomSheetDialog); ScrollView sv=new ScrollView(this); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(28,16,28,28); root.setBackgroundColor(Color.parseColor("#FFFFFF")); sv.addView(root);
        TextView topTitle=new TextView(this); topTitle.setText("Room set"); topTitle.setTextColor(Color.parseColor("#222222")); topTitle.setTextSize(18); topTitle.setTypeface(null,android.graphics.Typeface.BOLD); topTitle.setGravity(Gravity.CENTER); topTitle.setPadding(0,0,0,16); root.addView(topTitle);
        LinearLayout logoBox=new LinearLayout(this); logoBox.setOrientation(LinearLayout.VERTICAL); logoBox.setGravity(Gravity.CENTER); GradientDrawable logoBg=new GradientDrawable(); logoBg.setColor(Color.parseColor("#FFF3E0")); logoBg.setCornerRadius(30); logoBg.setStroke(2,Color.parseColor("#FF9800")); logoBox.setBackground(logoBg); logoBox.setPadding(20,20,20,20); LinearLayout.LayoutParams lpLogo=new LinearLayout.LayoutParams(320,320); lpLogo.gravity=Gravity.CENTER_HORIZONTAL; lpLogo.setMargins(0,0,0,6); logoBox.setLayoutParams(lpLogo);
        roomEditLogoView=new ImageView(this); roomEditLogoView.setLayoutParams(new LinearLayout.LayoutParams(260,260)); roomEditLogoView.setScaleType(ImageView.ScaleType.CENTER_CROP); roomEditLogoView.setImageResource(android.R.drawable.ic_menu_gallery); logoBox.addView(roomEditLogoView);
        roomEditLogoText=new TextView(this); roomEditLogoText.setText("SR"); roomEditLogoText.setTextColor(Color.parseColor("#FF9800")); roomEditLogoText.setTextSize(28); roomEditLogoText.setTypeface(null,android.graphics.Typeface.BOLD); roomEditLogoText.setGravity(Gravity.CENTER); logoBox.addView(roomEditLogoText); root.addView(logoBox);
        TextView tapHint=new TextView(this); tapHint.setText("Tap logo to change - GROUP PHOTO ALADA, ID PHOTO ALADA"); tapHint.setTextColor(Color.parseColor("#888888")); tapHint.setTextSize(12); tapHint.setGravity(Gravity.CENTER); tapHint.setPadding(0,0,0,20); root.addView(tapHint);
        View.OnClickListener openGalleryLogo=v->{ galleryMode="ROOM_LOGO"; Intent i=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI); galleryLauncher.launch(i); }; logoBox.setOnClickListener(openGalleryLogo); roomEditLogoView.setOnClickListener(openGalleryLogo);
        Bitmap curRoomBmp=decodeBase64(currentRoomPhotoBase64); if(curRoomBmp!=null){ roomEditLogoView.setImageBitmap(curRoomBmp); roomEditLogoText.setVisibility(View.GONE); pendingRoomPhotoBase64=currentRoomPhotoBase64; }
        TextView lblName=new TextView(this); lblName.setText("Room Name"); lblName.setTextColor(Color.parseColor("#333333")); lblName.setTextSize(14); lblName.setTypeface(null,android.graphics.Typeface.BOLD); root.addView(lblName);
        LinearLayout nameRow=new LinearLayout(this); nameRow.setOrientation(LinearLayout.HORIZONTAL); nameRow.setGravity(Gravity.CENTER_VERTICAL); nameRow.setBackgroundColor(Color.parseColor("#F5F5F5")); nameRow.setPadding(16,4,16,4); root.addView(nameRow);
        EditText etName=new EditText(this); etName.setHint("Room Name"); etName.setTextColor(Color.parseColor("#222222")); etName.setTextSize(15); etName.setBackgroundColor(Color.TRANSPARENT); etName.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f)); String curName=roomTitleText!=null?roomTitleText.getText().toString():""; etName.setText(curName); nameRow.addView(etName);
        TextView tvNameCount=new TextView(this); tvNameCount.setText(curName.length()+"/20"); tvNameCount.setTextColor(Color.parseColor("#999999")); tvNameCount.setTextSize(12); nameRow.addView(tvNameCount); etName.addTextChangedListener(new TextWatcher(){ @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){} @Override public void onTextChanged(CharSequence s,int st,int b,int c){ tvNameCount.setText(s.length()+"/20"); } @Override public void afterTextChanged(Editable s){} });
        TextView lblAnn=new TextView(this); lblAnn.setText("Room announcement"); lblAnn.setTextColor(Color.parseColor("#333333")); lblAnn.setTextSize(14); lblAnn.setTypeface(null,android.graphics.Typeface.BOLD); lblAnn.setPadding(0,20,0,8); root.addView(lblAnn);
        EditText etAnn=new EditText(this); etAnn.setHint("Write announcement..."); etAnn.setTextColor(Color.parseColor("#222222")); etAnn.setTextSize(14); etAnn.setBackgroundColor(Color.parseColor("#F5F5F5")); etAnn.setPadding(20,20,20,20); etAnn.setMinLines(4); etAnn.setGravity(Gravity.TOP); etAnn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)); String curAnn=roomNoticeBarText!=null?roomNoticeBarText.getText().toString():""; etAnn.setText(curAnn); root.addView(etAnn);
        TextView lblLabel=new TextView(this); lblLabel.setText("Room label"); lblLabel.setTextColor(Color.parseColor("#333333")); lblLabel.setTextSize(14); lblLabel.setTypeface(null,android.graphics.Typeface.BOLD); lblLabel.setPadding(0,20,0,8); root.addView(lblLabel);
        LinearLayout labelRow=new LinearLayout(this); labelRow.setOrientation(LinearLayout.HORIZONTAL); labelRow.setWeightSum(4); root.addView(labelRow); String[] labels={"Friends","Game","Music","Chat"}; List<TextView> labelViews=new ArrayList<>(); for(String lb:labels){ TextView tv=new TextView(this); tv.setText(lb); tv.setTextSize(13); tv.setGravity(Gravity.CENTER); tv.setPadding(10,18,10,18); GradientDrawable gd=new GradientDrawable(); gd.setCornerRadius(40); gd.setColor(lb.equals(selectedRoomLabel)?Color.parseColor("#FFC107"):Color.parseColor("#E0E0E0")); tv.setBackground(gd); tv.setTextColor(lb.equals(selectedRoomLabel)?Color.WHITE:Color.parseColor("#555555")); tv.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f)); labelViews.add(tv); tv.setOnClickListener(v->{ selectedRoomLabel=lb; for(int i=0;i<labels.length;i++){ TextView t2=labelViews.get(i); GradientDrawable g2=new GradientDrawable(); g2.setCornerRadius(40); boolean sel=labels[i].equals(lb); g2.setColor(sel?Color.parseColor("#FFC107"):Color.parseColor("#E0E0E0")); t2.setBackground(g2); t2.setTextColor(sel?Color.WHITE:Color.parseColor("#555555")); } }); LinearLayout.LayoutParams mp=(LinearLayout.LayoutParams)tv.getLayoutParams(); mp.setMargins(4,0,4,0); tv.setLayoutParams(mp); labelRow.addView(tv); }
        TextView lblTheme=new TextView(this); lblTheme.setText("Room theme"); lblTheme.setTextColor(Color.parseColor("#333333")); lblTheme.setTextSize(14); lblTheme.setTypeface(null,android.graphics.Typeface.BOLD); lblTheme.setPadding(0,20,0,8); root.addView(lblTheme);
        LinearLayout themeRow=new LinearLayout(this); themeRow.setOrientation(LinearLayout.HORIZONTAL); themeRow.setGravity(Gravity.CENTER_VERTICAL); themeRow.setBackgroundColor(Color.parseColor("#F5F5F5")); themeRow.setPadding(16,16,16,16); root.addView(themeRow); TextView tvThemeName=new TextView(this); tvThemeName.setText(currentThemeName); tvThemeName.setTextColor(Color.parseColor("#222222")); tvThemeName.setTextSize(14); tvThemeName.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f)); themeRow.addView(tvThemeName); TextView tvThemeArrow=new TextView(this); tvThemeArrow.setText("🖼️  >"); tvThemeArrow.setTextSize(18); themeRow.addView(tvThemeArrow); themeRow.setOnClickListener(v->showRoomThemeDialog());
        Button btnSave=new Button(this); btnSave.setText("Save"); btnSave.setTextColor(Color.WHITE); btnSave.setTextSize(17); btnSave.setAllCaps(false); btnSave.setTypeface(null,android.graphics.Typeface.BOLD); GradientDrawable saveBg=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{Color.parseColor("#FFC107"),Color.parseColor("#FF9800")}); saveBg.setCornerRadius(60); btnSave.setBackground(saveBg); LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,130); sp.setMargins(0,28,0,0); btnSave.setLayoutParams(sp); root.addView(btnSave);
        btnSave.setOnClickListener(v->{ String newName=etName.getText().toString().trim(); String newAnn=etAnn.getText().toString().trim(); if(newName.isEmpty()){ Toast.makeText(this,"Room Name likho",Toast.LENGTH_SHORT).show(); return; } Map<String,Object> map=new HashMap<>(); map.put("roomName",newName); map.put("announcement",newAnn); map.put("roomLabel",selectedRoomLabel); map.put("roomTheme",currentThemeName); if(pendingRoomPhotoBase64!=null && !pendingRoomPhotoBase64.isEmpty()){ map.put("roomPhotoBase64",pendingRoomPhotoBase64); currentRoomPhotoBase64=pendingRoomPhotoBase64; Bitmap bmp=decodeBase64(pendingRoomPhotoBase64); if(bmp!=null && roomHeaderProfileImg!=null) roomHeaderProfileImg.setImageBitmap(bmp); } if(pendingCustomBgBase64!=null && !pendingCustomBgBase64.isEmpty()){ if(myWalletCoins<100000){ Toast.makeText(this,"Not enough coins - 100000 gold lagbe",Toast.LENGTH_SHORT).show(); return; } map.put("customBgBase64",pendingCustomBgBase64); userDatabaseRef.child(currentUserId).child("coins").setValue(myWalletCoins-100000); pendingCustomBgBase64=""; } roomMetaRef.updateChildren(map); if(roomTitleText!=null) roomTitleText.setText(newName); if(roomNoticeBarText!=null){ if(newAnn.isEmpty()) roomNoticeBarText.setVisibility(View.GONE); else { roomNoticeBarText.setVisibility(View.VISIBLE); roomNoticeBarText.setText(newAnn); } } Toast.makeText(this,"Room Saved Successfully ✅",Toast.LENGTH_SHORT).show(); dialog.dismiss(); });
        dialog.setContentView(sv); setDialog75(dialog); dialog.show();
    }
    private void showRoomThemeDialog(){
        BottomSheetDialog dialog=new BottomSheetDialog(this,R.style.Theme_Design_BottomSheetDialog); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.parseColor("#111111")); root.setPadding(20,16,20,20);
        TextView title=new TextView(this); title.setText("Background"); title.setTextColor(Color.WHITE); title.setTextSize(18); title.setTypeface(null,android.graphics.Typeface.BOLD); title.setGravity(Gravity.CENTER); title.setPadding(0,0,0,12); root.addView(title);
        LinearLayout gridBox=new LinearLayout(this); gridBox.setOrientation(LinearLayout.VERTICAL); root.addView(gridBox);
        String[][] items={{"🌅 Sunset","Free"},{"🏖️ Beach","Free"},{"🦁 Lion","99999"},{"🐉 Dragon","59999"}};
        for(String[] it:items){ Button b=new Button(this); b.setText(it[0]+" - 🪙 "+it[1]); b.setOnClickListener(v->{ currentThemeName=it[0]; roomMetaRef.child("roomTheme").setValue(it[0]); Toast.makeText(this,it[0]+" Selected ✅",Toast.LENGTH_SHORT).show(); dialog.dismiss(); }); gridBox.addView(b); }
        Button mine=new Button(this); mine.setText("☁️ Upload My Background - 🪙 100000"); mine.setOnClickListener(v->{ galleryMode="CUSTOM_BG"; Intent i=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI); galleryLauncher.launch(i); dialog.dismiss(); }); gridBox.addView(mine);
        dialog.setContentView(root); setDialog75(dialog); dialog.show();
    }
}
