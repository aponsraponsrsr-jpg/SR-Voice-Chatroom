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
import androidx.cardview.widget.CardView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.*;
import java.util.*;

public class VoiceRoomActivity extends AppCompatActivity {
    private String roomOwnerId="499506534", currentUserId="", my9DigitId="", myDisplayName="User", myCountry="Bangladesh", myLanguage="English", myTag="Friends", myGender="Male", myPhotoBase64="", currentThemeName="Sunset", selectedRoomLabel="Friends", galleryMode="", pendingRoomPhotoBase64="", currentRoomPhotoBase64="";
    private long myWalletCoins=0;
    private SharedPreferences prefs;
    private DatabaseReference databaseReference, roomMetaRef, adminsRef, messagesRef, userDatabaseRef;
    private TextView roomTitleText, roomNoticeBarText, roomInfoText, textOnlineCount, textRoomCoins, walletTextInGift, roomEditLogoText;
    private ImageView roomHeaderProfileImg, roomEditLogoView;
    private EditText inputChatMessage;
    private TextView btnChatOption;
    private LinearLayout chatListContainer, chatPopupBar;
    private ScrollView chatScrollView;
    private List<String> roomAdminList=new ArrayList<>();
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_voice_room);
        prefs=getSharedPreferences("SR_PREFS",MODE_PRIVATE);
        currentUserId=prefs.getString("USER_UID","");
        my9DigitId=prefs.getString("USER_9DIGIT_ID","");
        myDisplayName=prefs.getString("USER_NAME","User");
        myPhotoBase64=prefs.getString("USER_PHOTO_BASE64","");
        myCountry=prefs.getString("USER_COUNTRY","Bangladesh");
        myLanguage=prefs.getString("USER_LANGUAGE","English");
        myGender=prefs.getString("USER_GENDER","Male");
        String rid=getIntent().getStringExtra("ROOM_OWNER_ID");
        if(rid!=null&&!rid.isEmpty())roomOwnerId=rid;
        String a=getIntent().getStringExtra("MY_9DIGIT_ID");
        if(a!=null&&!a.isEmpty())my9DigitId=a;
        String b=getIntent().getStringExtra("MY_UID");
        if(b!=null&&!b.isEmpty())currentUserId=b;
        databaseReference=FirebaseDatabase.getInstance().getReference();
        userDatabaseRef=databaseReference.child("users");
        roomMetaRef=databaseReference.child("rooms").child(roomOwnerId).child("meta");
        adminsRef=databaseReference.child("rooms").child(roomOwnerId).child("admins");
        messagesRef=databaseReference.child("rooms").child(roomOwnerId).child("messages");
        roomTitleText=findViewById(R.id.roomTitleText);
        roomInfoText=findViewById(R.id.roomInfoText);
        roomNoticeBarText=findViewById(R.id.roomNoticeBarText);
        textOnlineCount=findViewById(R.id.textOnlineCount);
        textRoomCoins=findViewById(R.id.textRoomCoins);
        roomHeaderProfileImg=findViewById(R.id.roomHeaderProfileImg);
        chatListContainer=findViewById(R.id.chatListContainer);
        chatScrollView=findViewById(R.id.chatScrollView);
        chatPopupBar=findViewById(R.id.chatPopupBar);
        inputChatMessage=findViewById(R.id.inputChatMessage);
        btnChatOption=findViewById(R.id.btnChatOption);
        if(chatPopupBar!=null)chatPopupBar.setVisibility(View.GONE);
        galleryLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),r->{
            if(r.getResultCode()==RESULT_OK&&r.getData()!=null&&r.getData().getData()!=null){
                try{
                    Bitmap bmp=MediaStore.Images.Media.getBitmap(getContentResolver(),r.getData().getData());
                    java.io.ByteArrayOutputStream ba=new java.io.ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG,70,ba);
                    String b64=Base64.encodeToString(ba.toByteArray(),Base64.DEFAULT);
                    if("ROOM_LOGO".equals(galleryMode)){
                        pendingRoomPhotoBase64=b64;
                        if(roomEditLogoView!=null)roomEditLogoView.setImageBitmap(bmp);
                        if(roomEditLogoText!=null)roomEditLogoText.setVisibility(View.GONE);
                        Toast.makeText(this,"Room Photo Selected ✅",Toast.LENGTH_SHORT).show();
                    }
                }catch(Exception e){ Toast.makeText(this,"Pic nite pari nai",Toast.LENGTH_SHORT).show(); }
            }
        });
        if(roomHeaderProfileImg!=null)roomHeaderProfileImg.setOnClickListener(v->showRoomHeaderProfile());
        if(roomTitleText!=null)roomTitleText.setOnClickListener(v->showRoomHeaderProfile());
        if(roomInfoText!=null)roomInfoText.setOnClickListener(v->showRoomHeaderProfile());
        View bo=findViewById(R.id.btnOnlineList);
        if(bo!=null)bo.setOnClickListener(v->showOnlineListDialog());
        View bp=findViewById(R.id.btnPower);
        if(bp!=null)bp.setOnClickListener(v->showExitDialog());
        View br=findViewById(R.id.btnInRoomRanking);
        if(br!=null)br.setOnClickListener(v->showInRoomRankingDialog());
        View bg=findViewById(R.id.btnGiftBox);
        if(bg!=null)bg.setOnClickListener(v->showNewGiftPanel());
        View bga=findViewById(R.id.btnGame);
        if(bga!=null)bga.setOnClickListener(v->Toast.makeText(this,"Game - Coming Soon 🎮",Toast.LENGTH_SHORT).show());
        View bt=findViewById(R.id.btnRoomTools);
        if(bt!=null)bt.setOnClickListener(v->showIconToolsDialog(roomOwnerId.equals(my9DigitId)||roomOwnerId.equals(currentUserId)));
        View bm=findViewById(R.id.btnMicToggle);
        if(bm!=null)bm.setOnClickListener(v->Toast.makeText(this,"Mic - Coming Soon 🎤",Toast.LENGTH_SHORT).show());
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

    private void setDialog85(BottomSheetDialog d){Window w=d.getWindow();if(w!=null){int sh=getResources().getDisplayMetrics().heightPixels;w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(sh*0.85));w.setGravity(Gravity.BOTTOM);}}
    private void setDialog75(BottomSheetDialog d){Window w=d.getWindow();if(w!=null){int sh=getResources().getDisplayMetrics().heightPixels;w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(sh*0.75));w.setGravity(Gravity.BOTTOM);}}
    private android.app.Dialog makeFullScreenDialog(){android.app.Dialog d=new android.app.Dialog(this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);Window w=d.getWindow();if(w!=null){w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);w.setGravity(Gravity.BOTTOM);w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));}return d;}
    private String flagForCountry(String c){
        if(c==null)return "🌍";
        String s=c.toLowerCase();
        if(s.contains("bangladesh")||s.contains("bd"))return "🇧🇩";
        if(s.contains("india"))return "🇮🇳";
        if(s.contains("pakistan"))return "🇵🇰";
        if(s.contains("saudi"))return "🇸🇦";
        if(s.contains("oman"))return "🇴🇲";
        if(s.contains("uae")||s.contains("emirates"))return "🇦🇪";
        if(s.contains("usa")||s.contains("america"))return "🇺🇸";
        if(s.contains("uk")||s.contains("england"))return "🇬🇧";
        return "🌍";
    }
    private String genderIcon(String g){
        if(g==null)return "👤";
        String s=g.toLowerCase();
        if(s.contains("female")||s.contains("girl")||s.contains("f"))return "👧";
        return "👦";
    }
    private Bitmap decodeBase64(String b64){
        if(b64==null||b64.isEmpty())return null;
        try{ byte[] b=Base64.decode(b64,Base64.DEFAULT); return BitmapFactory.decodeByteArray(b,0,b.length); }catch(Exception e){return null;}
    }
    private void copyRoomId(){
        ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        if(cm!=null){ cm.setPrimaryClip(ClipData.newPlainText("Room ID",roomOwnerId)); Toast.makeText(this,"Room ID Copied: "+roomOwnerId+" ✅",Toast.LENGTH_SHORT).show(); }
    }
    private void ensureMyIdentity(){
        if(my9DigitId==null)my9DigitId="";
        if(my9DigitId.isEmpty())my9DigitId=prefs.getString("USER_9DIGIT_ID","");
        if(myDisplayName==null||myDisplayName.equals("User"))myDisplayName=prefs.getString("USER_NAME","User");
        if(myPhotoBase64==null||myPhotoBase64.isEmpty())myPhotoBase64=prefs.getString("USER_PHOTO_BASE64","");
        if(!my9DigitId.isEmpty()){
            userDatabaseRef.orderByChild("my9DigitId").equalTo(my9DigitId).addListenerForSingleValueEvent(new ValueEventListener(){
                @Override public void onDataChange(@NonNull DataSnapshot snap){
                    if(snap.exists()){
                        for(DataSnapshot u:snap.getChildren()){
                            currentUserId=u.getKey();
                            if(u.child("name").getValue()!=null)myDisplayName=String.valueOf(u.child("name").getValue());
                            if(u.child("photoBase64").getValue()!=null)myPhotoBase64=String.valueOf(u.child("photoBase64").getValue());
                            if(u.child("country").getValue()!=null)myCountry=String.valueOf(u.child("country").getValue());
                            if(u.child("gender").getValue()!=null)myGender=String.valueOf(u.child("gender").getValue());
                            prefs.edit().putString("USER_UID",currentUserId).putString("USER_NAME",myDisplayName).putString("USER_PHOTO_BASE64",myPhotoBase64).putString("USER_COUNTRY",myCountry).putString("USER_GENDER",myGender).apply();
                            pushOnlineNow(); break;
                        }
                    }else syncByUidFallback();
                }
                @Override public void onCancelled(@NonNull DatabaseError e){syncByUidFallback();}
            });
        }else syncByUidFallback();
    }
    private void syncByUidFallback(){
        if(currentUserId==null||currentUserId.isEmpty()){pushOnlineNow();return;}
        userDatabaseRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                if(s.exists()){
                    if(s.child("my9DigitId").getValue()!=null)my9DigitId=String.valueOf(s.child("my9DigitId").getValue());
                    if(s.child("name").getValue()!=null)myDisplayName=String.valueOf(s.child("name").getValue());
                    if(s.child("photoBase64").getValue()!=null)myPhotoBase64=String.valueOf(s.child("photoBase64").getValue());
                    if(s.child("country").getValue()!=null)myCountry=String.valueOf(s.child("country").getValue());
                    if(s.child("gender").getValue()!=null)myGender=String.valueOf(s.child("gender").getValue());
                    prefs.edit().putString("USER_9DIGIT_ID",my9DigitId).putString("USER_NAME",myDisplayName).putString("USER_PHOTO_BASE64",myPhotoBase64).apply();
                }
                pushOnlineNow();
            }
            @Override public void onCancelled(@NonNull DatabaseError e){pushOnlineNow();}
        });
    }
    private void syncLatestUserProfile(){
        if(currentUserId.isEmpty()&&my9DigitId.isEmpty())return;
        String key=!currentUserId.isEmpty()?currentUserId:my9DigitId;
        userDatabaseRef.child(key).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                if(s.exists()){
                    String n=s.child("name").getValue(String.class);
                    if(n!=null&&!n.isEmpty()){myDisplayName=n;prefs.edit().putString("USER_NAME",n).apply();}
                    String v=s.child("my9DigitId").getValue(String.class);
                    if(v!=null&&!v.isEmpty()){my9DigitId=v;prefs.edit().putString("USER_9DIGIT_ID",v).apply();}
                    String c=s.child("country").getValue(String.class);
                    if(c!=null&&!c.isEmpty())myCountry=c;
                    String l=s.child("language").getValue(String.class);
                    if(l!=null&&!l.isEmpty())myLanguage=l;
                    String g=s.child("gender").getValue(String.class);
                    if(g!=null&&!g.isEmpty())myGender=g;
                    String p=s.child("photoBase64").getValue(String.class);
                    if(p!=null&&!p.isEmpty()){myPhotoBase64=p;prefs.edit().putString("USER_PHOTO_BASE64",p).apply();}
                    pushOnlineNow();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
    }
    private void pushOnlineNow(){
        String key=my9DigitId;
        if(key==null||key.isEmpty())key=currentUserId;
        if(key==null||key.isEmpty())key=roomOwnerId;
        if(key==null||key.isEmpty()||databaseReference==null)return;
        String n=myDisplayName;
        if(n==null||n.isEmpty())n=prefs.getString("USER_NAME","User");
        if(n.equals("User")&&!key.isEmpty())n="User "+key;
        Map<String,Object> m=new HashMap<>();
        m.put("name",n);
        m.put("photoBase64",myPhotoBase64!=null?myPhotoBase64:"");
        m.put("level","0");
        m.put("country",myCountry);
        m.put("gender",myGender);
        m.put("my9DigitId",key);
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").child(key).setValue(m);
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").child(key).onDisconnect().removeValue();
        databaseReference.child("online_users").child(key).setValue(m);
        databaseReference.child("online_users").child(key).onDisconnect().removeValue();
    }
    private void listenToOnlineMembers(){
        pushOnlineNow();
        if(databaseReference==null)return;
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").addValueEventListener(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                if(textOnlineCount!=null&&s.exists()&&s.getChildrenCount()>0)textOnlineCount.setText(String.valueOf(s.getChildrenCount()));
                else if(textOnlineCount!=null&&s.exists())textOnlineCount.setText("1");
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
    }
    private void showChatPopup(){
        if(chatPopupBar!=null)chatPopupBar.setVisibility(View.VISIBLE);
        if(inputChatMessage!=null){
            inputChatMessage.requestFocus();
            InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm!=null)imm.showSoftInput(inputChatMessage,InputMethodManager.SHOW_IMPLICIT);
        }
    }
    private void hideChatPopup(){
        if(chatPopupBar!=null)chatPopupBar.setVisibility(View.GONE);
        if(inputChatMessage!=null){
            inputChatMessage.setText("");
            InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm!=null)imm.hideSoftInputFromWindow(inputChatMessage.getWindowToken(),0);
        }
    }
    private void setupChatPopup(){
        View b=findViewById(R.id.btnOpenChat);
        if(b!=null)b.setOnClickListener(v->showChatPopup());
        if(inputChatMessage!=null)inputChatMessage.setOnEditorActionListener((v,a,e)->{sendChatMessage();return true;});
        if(btnChatOption!=null)btnChatOption.setOnClickListener(v->sendChatMessage());
    }
    @Override public void onBackPressed(){
        if(chatPopupBar!=null&&chatPopupBar.getVisibility()==View.VISIBLE){ hideChatPopup(); return; }
        super.onBackPressed();
    }
    private void sendChatMessage(){
        if(inputChatMessage==null)return;
        String msg=inputChatMessage.getText().toString().trim();
        if(msg.isEmpty()||messagesRef==null)return;
        String n=myDisplayName;
        if(n==null||n.isEmpty())n=prefs.getString("USER_NAME","User");
        if(n.equals("User"))ensureMyIdentity();
        String id=my9DigitId;
        if(id==null||id.isEmpty())id=currentUserId;
        if(id==null||id.isEmpty())id=roomOwnerId;
        Map<String,Object> m=new HashMap<>();
        m.put("userId",id);
        m.put("userName",n);
        m.put("text",msg);
        m.put("photoBase64",myPhotoBase64!=null?myPhotoBase64:"");
        m.put("gender",myGender);
        m.put("country",myCountry);
        m.put("time",System.currentTimeMillis());
        messagesRef.push().setValue(m);
        hideChatPopup();
        pushOnlineNow();
    }
    private void listenToChatRealtime(){
        if(messagesRef==null||chatListContainer==null)return;
        messagesRef.limitToLast(100).addValueEventListener(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                chatListContainer.removeAllViews();
                if(!s.exists())return;
                for(DataSnapshot ds:s.getChildren()){
                    String uname=ds.child("userName").getValue(String.class);
                    if(uname==null)uname="User";
                    String txt=ds.child("text").getValue(String.class);
                    if(txt==null)txt="";
                    String photo=ds.child("photoBase64").getValue(String.class);
                    String co=ds.child("country").getValue(String.class);
                    LinearLayout row=new LinearLayout(VoiceRoomActivity.this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.TOP);
                    row.setPadding(4,6,4,6);
                    CardView cardAv=new CardView(VoiceRoomActivity.this);
                    LinearLayout.LayoutParams cardLp=new LinearLayout.LayoutParams(72,72);
                    cardLp.setMargins(0,4,8,0);
                    cardAv.setLayoutParams(cardLp);
                    cardAv.setRadius(36);
                    cardAv.setCardElevation(0);
                    cardAv.setCardBackgroundColor(Color.TRANSPARENT);
                    ImageView av=new ImageView(VoiceRoomActivity.this);
                    av.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
                    av.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    av.setImageResource(android.R.drawable.ic_menu_gallery);
                    Bitmap bmp=decodeBase64(photo);
                    if(bmp!=null)av.setImageBitmap(bmp);
                    cardAv.addView(av);
                    row.addView(cardAv);
                    LinearLayout box=new LinearLayout(VoiceRoomActivity.this);
                    box.setOrientation(LinearLayout.VERTICAL);
                    box.setPadding(10,0,0,0);
                    box.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));
                    row.addView(box);
                    TextView tvN=new TextView(VoiceRoomActivity.this);
                    tvN.setText(uname+"  Lv0  "+flagForCountry(co));
                    tvN.setTextColor(Color.parseColor("#3E2F00"));
                    tvN.setTextSize(12);
                    tvN.setTypeface(null,android.graphics.Typeface.BOLD);
                    box.addView(tvN);
                    TextView tvM=new TextView(VoiceRoomActivity.this);
                    tvM.setText(txt);
                    tvM.setTextColor(Color.parseColor("#222222"));
                    tvM.setTextSize(14);
                    box.addView(tvM);
                    chatListContainer.addView(row);
                }
                if(chatScrollView!=null)chatScrollView.post(()->chatScrollView.fullScroll(View.FOCUS_DOWN));
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
    }
    private void setupSeatInteractions(){
        for(int i=0;i<10;i++){
            final int idx=i;
            int res=getResources().getIdentifier("seat_"+idx,"id",getPackageName());
            View seat=findViewById(res);
            if(seat!=null){
                seat.setOnClickListener(new View.OnClickListener(){
                    @Override public void onClick(View v){
                        String myId=my9DigitId.isEmpty()?currentUserId:my9DigitId;
                        if(myId==null||myId.isEmpty())myId=roomOwnerId;
                        final String finalMyId = myId;
                        final String finalName = (myDisplayName!=null && !myDisplayName.isEmpty()) ? myDisplayName : "User";
                        final String finalPhoto = (myPhotoBase64!=null) ? myPhotoBase64 : "";
                        DatabaseReference seatsRootRef = databaseReference.child("rooms").child(roomOwnerId).child("seats");
                        seatsRootRef.addListenerForSingleValueEvent(new ValueEventListener(){
                            @Override public void onDataChange(@NonNull DataSnapshot allSeatsSnap){
                                String tempExistingKey = null;
                                for(DataSnapshot sSnap : allSeatsSnap.getChildren()){
                                    String uIdInSeat = sSnap.child("userId").getValue(String.class);
                                    if(uIdInSeat!=null && uIdInSeat.equals(finalMyId)){ tempExistingKey = sSnap.getKey(); break; }
                                }
                                final String existingSeatKey = tempExistingKey;
                                final DatabaseReference targetSeatRef = databaseReference.child("rooms").child(roomOwnerId).child("seats").child("seat_"+idx);
                                targetSeatRef.addListenerForSingleValueEvent(new ValueEventListener(){
                                    @Override public void onDataChange(@NonNull DataSnapshot targetSnap){
                                        if(!targetSnap.exists()){
                                            if(existingSeatKey!=null && !existingSeatKey.equals("seat_"+idx)){
                                                databaseReference.child("rooms").child(roomOwnerId).child("seats").child(existingSeatKey).removeValue();
                                            }
                                            Map<String,Object> m=new HashMap<>();
                                            m.put("userId", finalMyId);
                                            m.put("userName", finalName);
                                            m.put("photoBase64", finalPhoto);
                                            targetSeatRef.setValue(m);
                                            Toast.makeText(VoiceRoomActivity.this,"Seat "+(idx+1)+" এ বসেছেন ✅",Toast.LENGTH_SHORT).show();
                                        }else{
                                            String uid=targetSnap.child("userId").getValue(String.class);
                                            if(uid!=null && (uid.equals(currentUserId)||uid.equals(my9DigitId) || uid.equals(finalMyId))){
                                                targetSeatRef.removeValue();
                                                Toast.makeText(VoiceRoomActivity.this,"Seat থেকে উঠে গেছেন 🪑",Toast.LENGTH_SHORT).show();
                                            }else{ Toast.makeText(VoiceRoomActivity.this,"এই সিটে অন্য জন আছেন",Toast.LENGTH_SHORT).show(); }
                                        }
                                    }
                                    @Override public void onCancelled(@NonNull DatabaseError e){}
                                });
                            }
                            @Override public void onCancelled(@NonNull DatabaseError e){}
                        });
                    }
                });
            }
        }
    }
    private void listenToFirebaseSeats(){
        if(databaseReference==null)return;
        databaseReference.child("rooms").child(roomOwnerId).child("seats").addValueEventListener(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                for(int i=0;i<10;i++){
                    int imgId=getResources().getIdentifier("seatImg_"+i,"id",getPackageName());
                    View imgView=findViewById(imgId);
                    DataSnapshot ss=s.child("seat_"+i);
                    View sl=findViewById(getResources().getIdentifier("seat_"+i,"id",getPackageName()));
                    TextView nameTv = null;
                    if(sl instanceof LinearLayout){
                        LinearLayout ll=(LinearLayout)sl;
                        if(ll.getChildCount()>=2 && ll.getChildAt(1) instanceof TextView){ nameTv = (TextView)ll.getChildAt(1); }
                    }
                    if(ss.exists()){
                        String ph=ss.child("photoBase64").getValue(String.class);
                        String nm=ss.child("userName").getValue(String.class);
                        Bitmap bmp=decodeBase64(ph);
                        if(imgView instanceof TextView){
                            TextView tv = (TextView)imgView;
                            if(bmp!=null){ tv.setText(""); tv.setBackground(new android.graphics.drawable.BitmapDrawable(getResources(),bmp)); }
                            else{ tv.setText("👤"); tv.setBackgroundColor(Color.parseColor("#FFE9A8")); }
                        }
                        if(nameTv!=null){ nameTv.setText(nm!=null?nm:""); }
                    }else{
                        if(imgView instanceof TextView){
                            TextView tv = (TextView)imgView;
                            tv.setText("🪑"); tv.setBackgroundColor(Color.parseColor("#FFE9A8"));
                        }
                        if(nameTv!=null){ if(i==0) nameTv.setText("Host"); else nameTv.setText(""); }
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
    }
    private void listenToRoomDetails(){
        if(roomMetaRef==null)return;
        roomMetaRef.addValueEventListener(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                String rn=s.child("roomName").getValue(String.class);
                if(rn!=null&&!rn.isEmpty()&&roomTitleText!=null)roomTitleText.setText(rn);
                if(roomInfoText!=null)roomInfoText.setText("ID: "+roomOwnerId);
                String ann=s.child("announcement").getValue(String.class);
                if(roomNoticeBarText!=null){
                    if(ann==null||ann.isEmpty()||ann.equals("wlc")){ roomNoticeBarText.setVisibility(View.GONE); roomNoticeBarText.setText(""); }
                    else{ roomNoticeBarText.setVisibility(View.VISIBLE); roomNoticeBarText.setText(ann); }
                }
                String lb=s.child("roomLabel").getValue(String.class);
                if(lb!=null&&!lb.isEmpty())selectedRoomLabel=lb;
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
    }
    private void listenToRoomPhoto(){
        if(roomMetaRef==null)return;
        roomMetaRef.addValueEventListener(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                String rp=s.child("roomPhotoBase64").getValue(String.class);
                if(rp!=null&&!rp.isEmpty()){
                    currentRoomPhotoBase64=rp;
                    Bitmap bmp=decodeBase64(rp);
                    if(bmp!=null&&roomHeaderProfileImg!=null)roomHeaderProfileImg.setImageBitmap(bmp);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
    }
    private void listenToRoomTheme(){
        if(roomMetaRef==null)return;
        roomMetaRef.addValueEventListener(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                String n=s.child("roomTheme").getValue(String.class);
                if(n!=null&&!n.isEmpty())currentThemeName=n;
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
    }
    private void listenToRoomAdmins(){
        if(adminsRef==null)return;
        adminsRef.addValueEventListener(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                roomAdminList.clear();
                for(DataSnapshot ds:s.getChildren())if(Boolean.TRUE.equals(ds.getValue(Boolean.class)))roomAdminList.add(ds.getKey());
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
    }
    private void listenToMyWallet(){
        if(currentUserId.isEmpty())return;
        userDatabaseRef.child(currentUserId).addValueEventListener(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                Long c=s.child("coins").getValue(Long.class);
                long w=c!=null?c:0L;
                myWalletCoins=w;
                if(walletTextInGift!=null)walletTextInGift.setText("Coins: "+String.format("%,d",w));
                if(textRoomCoins!=null)textRoomCoins.setText("🪙 "+String.format("%,d",w));
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
    }
    private void showOnlineListDialog(){
        android.app.Dialog d=makeFullScreenDialog();
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(24,20,24,24);
        LinearLayout top=new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(top);
        TextView t=new TextView(this);
        t.setText("Online list");
        t.setTextColor(Color.parseColor("#222222"));
        t.setTextSize(19);
        t.setTypeface(null,android.graphics.Typeface.BOLD);
        t.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));
        top.addView(t);
        TextView cl=new TextView(this);
        cl.setText("✕");
        cl.setTextSize(22);
        cl.setTextColor(Color.parseColor("#888888"));
        cl.setPadding(10,10,10,10);
        cl.setOnClickListener(v->d.dismiss());
        top.addView(cl);
        ScrollView sv=new ScrollView(this);
        LinearLayout list=new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list);
        root.addView(sv);
        pushOnlineNow();
        databaseReference.child("rooms").child(roomOwnerId).child("online_users").addListenerForSingleValueEvent(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                list.removeAllViews();
                if(s.exists()&&s.getChildrenCount()>0){ for(DataSnapshot ds:s.getChildren())showOneOnlineUser(list,ds,d); }
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
        d.setContentView(root);
        d.show();
    }
    private void showOneOnlineUser(LinearLayout list,DataSnapshot ds,android.app.Dialog dialog){
        String k=ds.getKey();
        if(k==null)return;
        String n=ds.child("name").getValue(String.class);
        String p=ds.child("photoBase64").getValue(String.class);
        String co=ds.child("country").getValue(String.class);
        String ge=ds.child("gender").getValue(String.class);
        String lv=ds.child("level").getValue(String.class);
        addOnlineRow(list,k,n!=null?n:"User",p!=null?p:"",lv!=null?lv:"0",ge!=null?ge:"Male",co!=null?co:"",dialog);
    }
    private void addOnlineRow(LinearLayout list,String uid,String name,String photo,String level,String gender,String country,android.app.Dialog dialog){
        LinearLayout row=new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(6,14,6,14);
        CardView cardAv=new CardView(this);
        LinearLayout.LayoutParams cardLp=new LinearLayout.LayoutParams(110,110);
        cardLp.setMargins(0,0,12,0);
        cardAv.setLayoutParams(cardLp);
        cardAv.setRadius(55);
        cardAv.setCardElevation(0);
        cardAv.setCardBackgroundColor(Color.TRANSPARENT);
        ImageView av=new ImageView(this);
        av.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        av.setScaleType(ImageView.ScaleType.CENTER_CROP);
        av.setImageResource(android.R.drawable.ic_menu_gallery);
        Bitmap bmp=decodeBase64(photo);
        if(bmp!=null)av.setImageBitmap(bmp);
        cardAv.addView(av);
        row.addView(cardAv);
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(16,0,0,0);
        box.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));
        row.addView(box);
        TextView tn=new TextView(this);
        tn.setText(name+"  "+genderIcon(gender)+"  "+flagForCountry(country));
        tn.setTextColor(Color.parseColor("#222222"));
        tn.setTextSize(16);
        tn.setTypeface(null,android.graphics.Typeface.BOLD);
        box.addView(tn);
        TextView ti=new TextView(this);
        ti.setText("ID: "+uid);
        ti.setTextColor(Color.parseColor("#888888"));
        ti.setTextSize(13);
        box.addView(ti);
        list.addView(row);
    }

    // ===== 1 NO. KAJ - HEADER PROFILE + MEMBERS - FULL =====
    private void showRoomHeaderProfile(){
        android.app.Dialog dialog=makeFullScreenDialog();
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        LinearLayout tabRow=new LinearLayout(this);
        tabRow.setOrientation(LinearLayout.HORIZONTAL);
        tabRow.setGravity(Gravity.CENTER);
        tabRow.setPadding(0,16,0,0);
        root.addView(tabRow);
        TextView tabProfile=new TextView(this);
        tabProfile.setText("Profile");
        tabProfile.setTextSize(17);
        tabProfile.setTypeface(null,android.graphics.Typeface.BOLD);
        tabProfile.setTextColor(Color.parseColor("#222222"));
        tabProfile.setPadding(30,10,30,10);
        tabRow.addView(tabProfile);
        TextView tabMembers=new TextView(this);
        tabMembers.setText("Members");
        tabMembers.setTextSize(17);
        tabMembers.setTextColor(Color.parseColor("#999999"));
        tabMembers.setPadding(30,10,30,10);
        tabRow.addView(tabMembers);
        TextView cb=new TextView(this);
        cb.setText("✕");
        cb.setTextSize(20);
        cb.setTextColor(Color.parseColor("#999999"));
        cb.setPadding(20,10,20,10);
        cb.setOnClickListener(v->dialog.dismiss());
        tabRow.addView(cb);
        LinearLayout underlineBox=new LinearLayout(this);
        underlineBox.setOrientation(LinearLayout.HORIZONTAL);
        underlineBox.setGravity(Gravity.CENTER);
        View underline=new View(this);
        underline.setBackgroundColor(Color.parseColor("#FF9800"));
        underline.setLayoutParams(new LinearLayout.LayoutParams(140,6));
        underlineBox.addView(underline);
        root.addView(underlineBox);

        ScrollView svProfile=new ScrollView(this);
        LinearLayout pb=new LinearLayout(this);
        pb.setOrientation(LinearLayout.VERTICAL);
        pb.setPadding(20,10,20,20);
        svProfile.addView(pb);
        root.addView(svProfile);
        ScrollView svMembers=new ScrollView(this);
        LinearLayout mb=new LinearLayout(this);
        mb.setOrientation(LinearLayout.VERTICAL);
        mb.setPadding(20,10,20,20);
        svMembers.addView(mb);
        svMembers.setVisibility(View.GONE);
        root.addView(svMembers);

        // PROFILE
        LinearLayout top=new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setBackgroundColor(Color.parseColor("#FFFDE7"));
        top.setPadding(20,16,20,16);
        pb.addView(top);
        CardView cardRp=new CardView(this);
        LinearLayout.LayoutParams cardRpLp=new LinearLayout.LayoutParams(130,130);
        cardRpLp.setMargins(0,0,12,0);
        cardRp.setLayoutParams(cardRpLp);
        cardRp.setRadius(65);
        cardRp.setCardElevation(0);
        cardRp.setCardBackgroundColor(Color.TRANSPARENT);
        ImageView rp=new ImageView(this);
        rp.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        rp.setScaleType(ImageView.ScaleType.CENTER_CROP);
        rp.setImageResource(android.R.drawable.ic_menu_gallery);
        Bitmap br=decodeBase64(currentRoomPhotoBase64);
        if(br!=null)rp.setImageBitmap(br);
        cardRp.addView(rp);
        top.addView(cardRp);
        LinearLayout nb=new LinearLayout(this);
        nb.setOrientation(LinearLayout.VERTICAL);
        nb.setPadding(16,0,0,0);
        nb.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));
        top.addView(nb);
        String rn=roomTitleText!=null?roomTitleText.getText().toString():"Sr Official's Room";
        TextView trn=new TextView(this);
        trn.setText(rn);
        trn.setTextColor(Color.parseColor("#222222"));
        trn.setTextSize(19);
        trn.setTypeface(null,android.graphics.Typeface.BOLD);
        nb.addView(trn);
        LinearLayout idRow=new LinearLayout(this);
        idRow.setOrientation(LinearLayout.HORIZONTAL);
        idRow.setGravity(Gravity.CENTER_VERTICAL);
        nb.addView(idRow);
        TextView tri=new TextView(this);
        tri.setText("ID: "+roomOwnerId+"  ");
        tri.setTextColor(Color.parseColor("#888888"));
        tri.setTextSize(13);
        idRow.addView(tri);
        TextView copyTv=new TextView(this);
        copyTv.setText("📋 Copy");
        copyTv.setTextColor(Color.parseColor("#FF9800"));
        copyTv.setTextSize(13);
        copyTv.setTypeface(null,android.graphics.Typeface.BOLD);
        copyTv.setPadding(8,4,8,4);
        copyTv.setOnClickListener(v->copyRoomId());
        idRow.addView(copyTv);

        LinearLayout statsRow=new LinearLayout(this);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        statsRow.setWeightSum(3);
        statsRow.setPadding(0,20,0,10);
        pb.addView(statsRow);
        TextView tvFollowing=new TextView(this);
        TextView tvFans=new TextView(this);
        TextView tvVisitor=new TextView(this);
        statsRow.addView(makeStatCell(tvFollowing,"0\nFollowing"));
        statsRow.addView(makeStatCell(tvFans,"0\nFans"));
        statsRow.addView(makeStatCell(tvVisitor,"0\nVisitor"));

        TextView banner=new TextView(this);
        banner.setText("👑  Reward Event  👑");
        banner.setTextColor(Color.WHITE);
        banner.setTextSize(18);
        banner.setTypeface(null,android.graphics.Typeface.BOLD);
        banner.setGravity(Gravity.CENTER);
        banner.setPadding(0,30,0,30);
        GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{Color.parseColor("#7B1FA2"),Color.parseColor("#FF9800")});
        bg.setCornerRadius(24);
        banner.setBackground(bg);
        LinearLayout.LayoutParams blp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        blp.setMargins(0,10,0,16);
        banner.setLayoutParams(blp);
        pb.addView(banner);

        pb.addView(makeInfoRow("⭐","Level","Lv.0"));
        TextView ownerRowVal=new TextView(this);
        pb.addView(makeInfoRowWithView("👤","RoomOwner",ownerRowVal));
        pb.addView(makeInfoRow("🌍","Language",myLanguage!=null?myLanguage:"English"));
        pb.addView(makeInfoRow("🏳️","Country",myCountry!=null?myCountry:"Bangladesh"));
        pb.addView(makeInfoRow("🏷️","Tag",selectedRoomLabel!=null?selectedRoomLabel:"Friends"));

        Button bj=new Button(this);
        bj.setText("✨ Join This Room");
        bj.setTextColor(Color.WHITE);
        bj.setTextSize(15);
        bj.setAllCaps(false);
        bj.setTypeface(null,android.graphics.Typeface.BOLD);
        GradientDrawable jbg=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{Color.parseColor("#FFC107"),Color.parseColor("#FF9800")});
        jbg.setCornerRadius(60);
        bj.setBackground(jbg);
        LinearLayout.LayoutParams jl=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,120);
        jl.setMargins(0,24,0,0);
        bj.setLayoutParams(jl);
        pb.addView(bj);
        bj.setOnClickListener(v->{ joinThisRoomPermanently(); Toast.makeText(this,"Joined Successfully ✅",Toast.LENGTH_SHORT).show(); });

        // MEMBERS
        TextView countTv=new TextView(this);
        countTv.setText("Members: 1/5000    Admin: 1/20");
        countTv.setTextColor(Color.parseColor("#222222"));
        countTv.setTextSize(14);
        countTv.setTypeface(null,android.graphics.Typeface.BOLD);
        countTv.setPadding(0,4,0,10);
        mb.addView(countTv);
        EditText searchEt=new EditText(this);
        searchEt.setHint("Search ID");
        searchEt.setTextSize(14);
        searchEt.setPadding(24,20,24,20);
        searchEt.setBackgroundColor(Color.parseColor("#F5F5F5"));
        mb.addView(searchEt);
        LinearLayout memberList=new LinearLayout(this);
        memberList.setOrientation(LinearLayout.VERTICAL);
        mb.addView(memberList);

        tabProfile.setOnClickListener(v->{
            tabProfile.setTextColor(Color.parseColor("#222222")); tabProfile.setTypeface(null,android.graphics.Typeface.BOLD);
            tabMembers.setTextColor(Color.parseColor("#999999")); tabMembers.setTypeface(null,android.graphics.Typeface.NORMAL);
            svProfile.setVisibility(View.VISIBLE); svMembers.setVisibility(View.GONE);
        });
        tabMembers.setOnClickListener(v->{
            tabMembers.setTextColor(Color.parseColor("#222222")); tabMembers.setTypeface(null,android.graphics.Typeface.BOLD);
            tabProfile.setTextColor(Color.parseColor("#999999")); tabProfile.setTypeface(null,android.graphics.Typeface.NORMAL);
            svProfile.setVisibility(View.GONE); svMembers.setVisibility(View.VISIBLE);
        });

        loadHeaderProfileData(tvFollowing,tvFans,tvVisitor,ownerRowVal,countTv,memberList,searchEt);
        dialog.setContentView(root);
        dialog.show();
    }

    private LinearLayout makeStatCell(TextView tv,String label){
        LinearLayout c=new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setGravity(Gravity.CENTER);
        c.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));
        tv.setText(label); tv.setTextColor(Color.parseColor("#222222")); tv.setTextSize(14);
        tv.setTypeface(null,android.graphics.Typeface.BOLD); tv.setGravity(Gravity.CENTER);
        c.addView(tv); return c;
    }
    private LinearLayout makeInfoRow(String icon,String key,String val){
        LinearLayout r=new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL); r.setGravity(Gravity.CENTER_VERTICAL); r.setPadding(0,14,0,14);
        TextView a=new TextView(this);
        a.setText(icon+"  "+key); a.setTextColor(Color.parseColor("#333333")); a.setTextSize(15);
        a.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));
        r.addView(a);
        TextView b=new TextView(this);
        b.setText(val); b.setTextColor(Color.parseColor("#222222")); b.setTextSize(15);
        b.setTypeface(null,android.graphics.Typeface.BOLD);
        r.addView(b); return r;
    }
    private LinearLayout makeInfoRowWithView(String icon,String key,TextView valView){
        LinearLayout r=new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL); r.setGravity(Gravity.CENTER_VERTICAL); r.setPadding(0,14,0,14);
        TextView a=new TextView(this);
        a.setText(icon+"  "+key); a.setTextColor(Color.parseColor("#333333")); a.setTextSize(15);
        a.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));
        r.addView(a);
        valView.setText("OMG  (ID:"+roomOwnerId+")");
        valView.setTextColor(Color.parseColor("#222222")); valView.setTextSize(15);
        valView.setTypeface(null,android.graphics.Typeface.BOLD);
        r.addView(valView); return r;
    }
    private void addMemberSectionTitle(LinearLayout list,String title){
        TextView t=new TextView(this);
        t.setText(title); t.setTextColor(Color.parseColor("#FF9800")); t.setTextSize(14);
        t.setTypeface(null,android.graphics.Typeface.BOLD); t.setPadding(0,16,0,6);
        list.addView(t);
    }
    private void addMemberRow(LinearLayout list,String id,String name,String photo,String badge){
        LinearLayout row=new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(6,10,6,10);
        CardView card=new CardView(this);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(90,90);
        lp.setMargins(0,0,12,0);
        card.setLayoutParams(lp); card.setRadius(45); card.setCardElevation(0); card.setCardBackgroundColor(Color.TRANSPARENT);
        ImageView av=new ImageView(this);
        av.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        av.setScaleType(ImageView.ScaleType.CENTER_CROP);
        av.setImageResource(android.R.drawable.ic_menu_gallery);
        Bitmap bmp=decodeBase64(photo);
        if(bmp!=null)av.setImageBitmap(bmp);
        card.addView(av); row.addView(card);
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));
        row.addView(box);
        TextView tn=new TextView(this);
        tn.setText(name); tn.setTextColor(Color.parseColor("#222222")); tn.setTextSize(15);
        tn.setTypeface(null,android.graphics.Typeface.BOLD);
        box.addView(tn);
        TextView ti=new TextView(this);
        ti.setText("ID: "+id); ti.setTextColor(Color.parseColor("#888888")); ti.setTextSize(12);
        box.addView(ti);
        if(badge!=null&&!badge.isEmpty()){
            TextView tb=new TextView(this);
            tb.setText(badge); tb.setTextColor(Color.WHITE); tb.setTextSize(11);
            tb.setTypeface(null,android.graphics.Typeface.BOLD);
            tb.setPadding(14,6,14,6);
            GradientDrawable g=new GradientDrawable(); g.setColor(Color.parseColor("#FF9800")); g.setCornerRadius(30);
            tb.setBackground(g); row.addView(tb);
        }
        list.addView(row);
    }
    private void loadHeaderProfileData(TextView tvFollowing,TextView tvFans,TextView tvVisitor,TextView ownerRowVal,TextView countTv,LinearLayout memberList,EditText searchEt){
        if(databaseReference==null)return;
        // Owner name real
        userDatabaseRef.orderByChild("my9DigitId").equalTo(roomOwnerId).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                if(s.exists()){
                    for(DataSnapshot u:s.getChildren()){
                        String n=u.child("name").getValue(String.class);
                        if(n!=null&&!n.isEmpty()&&ownerRowVal!=null) ownerRowVal.setText(n+"  (ID:"+roomOwnerId+")");
                        break;
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
        // Members + Online
        databaseReference.child("rooms").child(roomOwnerId).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot roomSnap){
                long memberCount = roomSnap.child("members").exists() ? roomSnap.child("members").getChildrenCount() : 0;
                long onlineCount = roomSnap.child("online_users").exists() ? roomSnap.child("online_users").getChildrenCount() : 0;
                long adminCount = roomSnap.child("admins").exists() ? roomSnap.child("admins").getChildrenCount() : 1;
                if(memberCount==0) memberCount = Math.max(1, onlineCount);
                if(adminCount==0) adminCount = 1;
                if(tvFollowing!=null) tvFollowing.setText(memberCount+"\nFollowing");
                if(tvFans!=null) tvFans.setText(memberCount+"\nFans");
                if(tvVisitor!=null) tvVisitor.setText(onlineCount+"\nVisitor");
                if(countTv!=null) countTv.setText("Members: "+memberCount+"/5000    Admin: "+adminCount+"/20");

                // Build member map
                Map<String,Map<String,String>> all = new LinkedHashMap<>();
                for(DataSnapshot ds: roomSnap.child("members").getChildren()){
                    Map<String,String> m=new HashMap<>();
                    m.put("name", ds.child("userName").getValue(String.class)!=null?ds.child("userName").getValue(String.class):(ds.child("name").getValue(String.class)!=null?ds.child("name").getValue(String.class):"User"));
                    m.put("photo", ds.child("photoBase64").getValue(String.class)!=null?ds.child("photoBase64").getValue(String.class):"");
                    m.put("id", ds.getKey());
                    all.put(ds.getKey(), m);
                }
                for(DataSnapshot ds: roomSnap.child("online_users").getChildren()){
                    if(!all.containsKey(ds.getKey())){
                        Map<String,String> m=new HashMap<>();
                        m.put("name", ds.child("name").getValue(String.class)!=null?ds.child("name").getValue(String.class):"User");
                        m.put("photo", ds.child("photoBase64").getValue(String.class)!=null?ds.child("photoBase64").getValue(String.class):"");
                        m.put("id", ds.getKey());
                        all.put(ds.getKey(), m);
                    }
                }
                // Ensure Owner exists
                if(!all.containsKey(roomOwnerId)){
                    Map<String,String> m=new HashMap<>(); m.put("name","OMG"); m.put("photo",currentRoomPhotoBase64!=null?currentRoomPhotoBase64:""); m.put("id",roomOwnerId);
                    all.put(roomOwnerId, m);
                }
                final Map<String,Map<String,String>> finalAll = all;
                Runnable render = () -> {
                    memberList.removeAllViews();
                    String q = searchEt.getText().toString().trim();
                    // Owner
                    addMemberSectionTitle(memberList,"👑 Owner");
                    for(Map.Entry<String,Map<String,String>> e: finalAll.entrySet()){
                        if(e.getKey().equals(roomOwnerId)){
                            if(q.isEmpty()||e.getKey().contains(q)||e.getValue().get("name").toLowerCase().contains(q.toLowerCase()))
                                addMemberRow(memberList, e.getKey(), e.getValue().get("name"), e.getValue().get("photo"), "Owner");
                        }
                    }
                    // Admin
                    addMemberSectionTitle(memberList,"🛡️ Admin");
                    boolean hasAdmin=false;
                    for(String aid: roomAdminList){
                        if(finalAll.containsKey(aid) && !aid.equals(roomOwnerId)){
                            if(q.isEmpty()||aid.contains(q)) { addMemberRow(memberList, aid, finalAll.get(aid).get("name"), finalAll.get(aid).get("photo"), "Admin"); hasAdmin=true; }
                        }
                    }
                    if(!hasAdmin && q.isEmpty()){
                        // Owner is also admin fallback - don't duplicate, show note
                    }
                    // Members
                    addMemberSectionTitle(memberList,"👥 Room Members");
                    for(Map.Entry<String,Map<String,String>> e: finalAll.entrySet()){
                        if(!e.getKey().equals(roomOwnerId) && !roomAdminList.contains(e.getKey())){
                            if(q.isEmpty()||e.getKey().contains(q)||e.getValue().get("name").toLowerCase().contains(q.toLowerCase()))
                                addMemberRow(memberList, e.getKey(), e.getValue().get("name"), e.getValue().get("photo"), "");
                        }
                    }
                };
                render.run();
                searchEt.addTextChangedListener(new TextWatcher(){
                    @Override public void beforeTextChanged(CharSequence s,int st,int c,int a){}
                    @Override public void onTextChanged(CharSequence s,int st,int b,int c){ render.run(); }
                    @Override public void afterTextChanged(Editable s){}
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
    }

    private void joinThisRoomPermanently(){
        String id=my9DigitId;
        if(id==null||id.isEmpty())id=currentUserId;
        if(id==null||id.isEmpty())id=roomOwnerId;
        if(databaseReference==null)return;
        Map<String,Object> m=new HashMap<>();
        m.put("userId",id);
        m.put("userName",myDisplayName!=null?myDisplayName:"User");
        m.put("photoBase64",myPhotoBase64!=null?myPhotoBase64:"");
        m.put("country",myCountry);
        m.put("gender",myGender);
        databaseReference.child("rooms").child(roomOwnerId).child("members").child(id).setValue(m);
    }
    private void showExitDialog(){
        AlertDialog.Builder b=new AlertDialog.Builder(this);
        b.setTitle("Exit Room?");
        b.setMessage("Exit room or Minimize?");
        b.setPositiveButton("Exit",(d,w)->{d.dismiss();leaveRoomAndFinish();});
        b.setNeutralButton("Minimize",(d,w)->{
            d.dismiss();
            leaveRoomKeepHome();
            Intent home=new Intent(VoiceRoomActivity.this,MainActivity.class);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(home);
            finish();
        });
        b.setNegativeButton("Cancel",(d,w)->d.dismiss());
        b.show();
    }
    private void leaveRoomKeepHome(){
        String k=my9DigitId.isEmpty()?currentUserId:my9DigitId;
        if(k!=null&&!k.isEmpty()){ databaseReference.child("rooms").child(roomOwnerId).child("online_users").child(k).removeValue(); }
    }
    private void leaveRoomAndFinish(){
        leaveRoomKeepHome();
        Intent i=new Intent(VoiceRoomActivity.this,MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }
    private void showNewGiftPanel(){
        BottomSheetDialog d=new BottomSheetDialog(this,R.style.Theme_Design_BottomSheetDialog);
        LinearLayout r=new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setPadding(24,20,24,24);
        r.setBackgroundColor(Color.WHITE);
        TextView t=new TextView(this);
        t.setText("🎁 Gift Box");
        t.setTextColor(Color.parseColor("#222222"));
        t.setTextSize(18);
        t.setTypeface(null,android.graphics.Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        r.addView(t);
        walletTextInGift=new TextView(this);
        walletTextInGift.setText("Coins: "+myWalletCoins);
        walletTextInGift.setTextColor(Color.parseColor("#B8860B"));
        walletTextInGift.setGravity(Gravity.CENTER);
        walletTextInGift.setPadding(0,8,0,16);
        r.addView(walletTextInGift);
        String[][] g={{"🌹","Rose","10"},{"💎","Diamond","100"},{"🚗","Car","1000"},{"🏰","Castle","10000"}};
        for(String[] gg:g){
            Button btn=new Button(this);
            btn.setText(gg[0]+" "+gg[1]+" - 🪙 "+gg[2]);
            btn.setOnClickListener(v->{ Toast.makeText(this,gg[1]+" Sent ✅",Toast.LENGTH_SHORT).show(); d.dismiss(); });
            r.addView(btn);
        }
        d.setContentView(r);
        setDialog75(d);
        d.show();
    }
    private void showIconToolsDialog(boolean isOwner){
        BottomSheetDialog d=new BottomSheetDialog(this,R.style.Theme_Design_BottomSheetDialog);
        LinearLayout r=new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setPadding(28,20,28,28);
        r.setBackgroundColor(Color.parseColor("#FFF8E1"));
        TextView t=new TextView(this);
        t.setText("Room Settings");
        t.setTextColor(Color.parseColor("#222222"));
        t.setTextSize(18);
        t.setTypeface(null,android.graphics.Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0,0,0,16);
        r.addView(t);
        r.addView(createToolsGrid(new String[][]{{"🔒","Lock off"},{"⚙️","Room set"},{"✏️","Room Edit"},{"🧹","Clear charm"},{"🗑️","Clear chat"},{"🎤","Change mic"},{"🎨","Mic Theme"},{"👗","Dress"},{"🔔","Lucky Sound"},{"🎙️","Voice User"},{"🚪","Kick out record"}},d));
        d.setContentView(r);
        setDialog85(d);
        d.show();
    }
    private LinearLayout createToolsGrid(String[][] items,BottomSheetDialog dialog){
        LinearLayout grid=new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        for(int i=0;i<items.length;i+=4){
            LinearLayout row=new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setWeightSum(4);
            for(int j=0;j<4;j++){
                int idx=i+j;
                LinearLayout cell=new LinearLayout(this);
                cell.setOrientation(LinearLayout.VERTICAL);
                cell.setGravity(Gravity.CENTER);
                cell.setPadding(4,12,4,12);
                cell.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));
                if(idx<items.length){
                    TextView ic=new TextView(this);
                    ic.setText(items[idx][0]); ic.setTextSize(26); ic.setGravity(Gravity.CENTER);
                    cell.addView(ic);
                    TextView lb=new TextView(this);
                    lb.setText(items[idx][1]); lb.setTextColor(Color.parseColor("#333333")); lb.setTextSize(11);
                    lb.setGravity(Gravity.CENTER); lb.setPadding(0,6,0,0);
                    cell.addView(lb);
                    String act=items[idx][1];
                    cell.setOnClickListener(v->{
                        dialog.dismiss();
                        if(act.equals("Clear chat")&&messagesRef!=null){ messagesRef.removeValue(); Toast.makeText(this,"Chat Cleared!",Toast.LENGTH_SHORT).show(); }
                        else if(act.equals("Room set")||act.equals("Room Edit")){ showRoomEditDialog(); }
                        else Toast.makeText(this,act+" - Coming Soon",Toast.LENGTH_SHORT).show();
                    });
                }
                row.addView(cell);
            }
            grid.addView(row);
        }
        return grid;
    }
    private void showInRoomRankingDialog(){
        android.app.Dialog d=makeFullScreenDialog();
        LinearLayout r=new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setPadding(20,16,20,20);
        r.setBackgroundColor(Color.parseColor("#1B1035"));
        TextView t=new TextView(this);
        t.setText("🏆 In-Room Ranking");
        t.setTextColor(Color.WHITE);
        t.setTextSize(18);
        t.setTypeface(null,android.graphics.Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        r.addView(t);
        TextView c=new TextView(this);
        c.setText("✕ Close");
        c.setTextColor(Color.WHITE);
        c.setGravity(Gravity.CENTER);
        c.setPadding(0,20,0,0);
        c.setOnClickListener(v->d.dismiss());
        r.addView(c);
        d.setContentView(r);
        d.show();
    }
    private void showRoomEditDialog(){
        BottomSheetDialog dialog=new BottomSheetDialog(this,R.style.Theme_Design_BottomSheetDialog);
        ScrollView sv=new ScrollView(this);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28,16,28,28);
        root.setBackgroundColor(Color.parseColor("#FFFFFF"));
        sv.addView(root);
        TextView tt=new TextView(this);
        tt.setText("Room set");
        tt.setTextColor(Color.parseColor("#222222"));
        tt.setTextSize(18);
        tt.setTypeface(null,android.graphics.Typeface.BOLD);
        tt.setGravity(Gravity.CENTER);
        tt.setPadding(0,0,0,16);
        root.addView(tt);
        LinearLayout lb=new LinearLayout(this);
        lb.setOrientation(LinearLayout.VERTICAL);
        lb.setGravity(Gravity.CENTER);
        GradientDrawable lgb=new GradientDrawable();
        lgb.setColor(Color.parseColor("#FFF3E0"));
        lgb.setCornerRadius(30);
        lgb.setStroke(2,Color.parseColor("#FF9800"));
        lb.setBackground(lgb);
        lb.setPadding(20,20,20,20);
        LinearLayout.LayoutParams llp=new LinearLayout.LayoutParams(320,320);
        llp.gravity=Gravity.CENTER_HORIZONTAL;
        llp.setMargins(0,0,0,6);
        lb.setLayoutParams(llp);
        roomEditLogoView=new ImageView(this);
        roomEditLogoView.setLayoutParams(new LinearLayout.LayoutParams(260,260));
        roomEditLogoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        roomEditLogoView.setImageResource(android.R.drawable.ic_menu_gallery);
        lb.addView(roomEditLogoView);
        roomEditLogoText=new TextView(this);
        roomEditLogoText.setText("SR");
        roomEditLogoText.setTextColor(Color.parseColor("#FF9800"));
        roomEditLogoText.setTextSize(28);
        roomEditLogoText.setTypeface(null,android.graphics.Typeface.BOLD);
        roomEditLogoText.setGravity(Gravity.CENTER);
        lb.addView(roomEditLogoText);
        root.addView(lb);
        View.OnClickListener og=v->{
            galleryMode="ROOM_LOGO";
            Intent i=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(i);
        };
        lb.setOnClickListener(og);
        roomEditLogoView.setOnClickListener(og);
        Bitmap cur=decodeBase64(currentRoomPhotoBase64);
        if(cur!=null){ roomEditLogoView.setImageBitmap(cur); roomEditLogoText.setVisibility(View.GONE); pendingRoomPhotoBase64=currentRoomPhotoBase64; }
        TextView ln=new TextView(this);
        ln.setText("Room Name");
        ln.setTextColor(Color.parseColor("#333333"));
        ln.setTextSize(14);
        ln.setTypeface(null,android.graphics.Typeface.BOLD);
        root.addView(ln);
        LinearLayout nr=new LinearLayout(this);
        nr.setOrientation(LinearLayout.HORIZONTAL);
        nr.setGravity(Gravity.CENTER_VERTICAL);
        nr.setBackgroundColor(Color.parseColor("#F5F5F5"));
        nr.setPadding(16,4,16,4);
        root.addView(nr);
        EditText en=new EditText(this);
        en.setHint("Room Name");
        en.setTextColor(Color.parseColor("#222222"));
        en.setTextSize(15);
        en.setBackgroundColor(Color.TRANSPARENT);
        en.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f));
        String cn=roomTitleText!=null?roomTitleText.getText().toString():"";
        en.setText(cn);
        nr.addView(en);
        TextView la=new TextView(this);
        la.setText("Room announcement");
        la.setTextColor(Color.parseColor("#333333"));
        la.setTextSize(14);
        la.setTypeface(null,android.graphics.Typeface.BOLD);
        la.setPadding(0,20,0,8);
        root.addView(la);
        EditText ea=new EditText(this);
        ea.setHint("Write announcement...");
        ea.setTextColor(Color.parseColor("#222222"));
        ea.setTextSize(14);
        ea.setBackgroundColor(Color.parseColor("#F5F5F5"));
        ea.setPadding(20,20,20,20);
        ea.setMinLines(4);
        ea.setGravity(Gravity.TOP);
        ea.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        String ca=roomNoticeBarText!=null?roomNoticeBarText.getText().toString():"";
        ea.setText(ca);
        root.addView(ea);
        Button bs=new Button(this);
        bs.setText("Save");
        bs.setTextColor(Color.WHITE);
        bs.setTextSize(17);
        bs.setAllCaps(false);
        bs.setTypeface(null,android.graphics.Typeface.BOLD);
        GradientDrawable sb=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{Color.parseColor("#FFC107"),Color.parseColor("#FF9800")});
        sb.setCornerRadius(60);
        bs.setBackground(sb);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,130);
        sp.setMargins(0,28,0,0);
        bs.setLayoutParams(sp);
        root.addView(bs);
        bs.setOnClickListener(v->{
            String nn=en.getText().toString().trim();
            String na=ea.getText().toString().trim();
            if(nn.isEmpty()){ Toast.makeText(this,"Room Name likho",Toast.LENGTH_SHORT).show(); return; }
            Map<String,Object> map=new HashMap<>();
            map.put("roomName",nn);
            map.put("announcement",na);
            map.put("roomLabel",selectedRoomLabel);
            map.put("roomTheme",currentThemeName);
            if(pendingRoomPhotoBase64!=null&&!pendingRoomPhotoBase64.isEmpty()){
                map.put("roomPhotoBase64",pendingRoomPhotoBase64);
                currentRoomPhotoBase64=pendingRoomPhotoBase64;
                Bitmap bmp=decodeBase64(pendingRoomPhotoBase64);
                if(bmp!=null&&roomHeaderProfileImg!=null)roomHeaderProfileImg.setImageBitmap(bmp);
            }
            roomMetaRef.updateChildren(map);
            if(roomTitleText!=null)roomTitleText.setText(nn);
            if(roomNoticeBarText!=null){
                if(na.isEmpty())roomNoticeBarText.setVisibility(View.GONE);
                else{ roomNoticeBarText.setVisibility(View.VISIBLE); roomNoticeBarText.setText(na); }
            }
            Toast.makeText(this,"Room Saved Successfully ✅",Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialog.setContentView(sv);
        setDialog75(dialog);
        dialog.show();
    }
}
