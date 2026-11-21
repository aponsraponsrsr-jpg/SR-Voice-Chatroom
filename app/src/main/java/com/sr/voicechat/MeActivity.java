package com.sr.voicechat;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class MeActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private TextView txtName, txtId, txtGenderCountry, txtFollow, txtFans, txtVisitor, txtWalletCoins;
    private SharedPreferences prefs;
    private String firebaseUid = "";
    private String myAnyId = "";
    private String myProfileId = "";
    private String myUidResolved = "";
    private DatabaseReference userRef;
    private String followCount = "0", fansCount = "0", visitorCount = "0";
    private LinearLayout agencyToolView = null;
    private LinearLayout hostCenterRowView = null;
    private LinearLayout levelToolView = null;
    private boolean isAgencyActive = false;
    private boolean isHostActive = false;
    private boolean inviteDialogShown = false;
    private boolean rtdbListenerStarted = false;
    private boolean vipAssignmentListenerStarted = false;
    private TextView txtVipBadge;
    private TextView txtSvipStatus;
    private TextView txtVipStatus;
    private int myVipLevel = 0;
    private TextView txtWealthBadge = null;
    private TextView txtCharmBadge = null;
    private LinearLayout levelIconsRow = null;
    private long myTotalSending = 0;
    private long myTotalReceiving = 0;
    private int myWealthLevel = 1;
    private int myCharmLevel = 1;
    private LinearLayout sellerWalletBox = null;
    private TextView txtSellerBalance = null;
    private boolean isSellerActive = false;
    private long sellerBusinessCoins = 0;
    private boolean sellerListenerStarted = false;
    private String cachedPhoto = "";

    private BroadcastReceiver profileReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            showFromPrefs();
            loadMeData();
        }
    };

    private void updateLevelBadge(){
        if(txtWealthBadge==null || txtCharmBadge==null) return;
        txtWealthBadge.setText("💎 " + myWealthLevel);
        GradientDrawable bgW = new GradientDrawable();
        try { bgW.setColors(LevelUtils.getLevelColors(myWealthLevel)); } catch(Exception e){ bgW.setColors(new int[]{Color.parseColor("#2196F3"), Color.parseColor("#00BCD4")}); }
        bgW.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        bgW.setCornerRadius(20);
        txtWealthBadge.setBackground(bgW);
        txtWealthBadge.setTextColor(Color.WHITE);
        txtWealthBadge.setVisibility(View.VISIBLE);
        txtWealthBadge.setOnClickListener(v->showLevelDialog());
        txtCharmBadge.setText("💖 " + myCharmLevel);
        GradientDrawable bgC = new GradientDrawable();
        try { bgC.setColors(LevelUtils.getLevelColors(myCharmLevel)); } catch(Exception e){ bgC.setColors(new int[]{Color.parseColor("#E91E63"), Color.parseColor("#FF4081")}); }
        bgC.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        bgC.setCornerRadius(20);
        txtCharmBadge.setBackground(bgC);
        txtCharmBadge.setTextColor(Color.WHITE);
        txtCharmBadge.setVisibility(View.VISIBLE);
        txtCharmBadge.setOnClickListener(v->showLevelDialog());
        if(levelIconsRow!=null) levelIconsRow.setVisibility(View.VISIBLE);
    }
    private void showLevelDialog(){
        try{
            AlertDialog.Builder b=new AlertDialog.Builder(this);
            ScrollView sv=new ScrollView(this); sv.setPadding(24,24,24,24);
            sv.setBackgroundColor(Color.parseColor("#0A0518"));
            LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
            sv.addView(root);
            TextView title=new TextView(this); title.setText("⭐ My Level - 1 to 150"); title.setTextColor(Color.WHITE); title.setTextSize(22); title.setTypeface(null,Typeface.BOLD); title.setGravity(Gravity.CENTER); title.setPadding(0,0,0,16); root.addView(title);
            root.addView(makeLevelCard("💰 Wealth Level", myWealthLevel, myTotalSending, "Sending"));
            root.addView(makeLevelCard("💎 Charm Level", myCharmLevel, myTotalReceiving, "Receiving"));
            TextView allTitle=new TextView(this); allTitle.setText("🏆 All Levels 1-150 - 10 Level Por Por Color Change"); allTitle.setTextColor(Color.parseColor("#FFD700")); allTitle.setTextSize(15); allTitle.setTypeface(null,Typeface.BOLD); allTitle.setPadding(8,20,8,8); root.addView(allTitle);
            int[] starts={1,11,21,31,41,51,61,71,81,91,101,111,121,131,141};
            for(int s: starts){
                int lvlForColor = s+5; if(lvlForColor>150) lvlForColor=150;
                LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(20,14,20,14);
                GradientDrawable bg=new GradientDrawable(); bg.setColors(LevelUtils.getLevelColors(lvlForColor)); bg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT); bg.setCornerRadius(20); row.setBackground(bg);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(0,0,0,10); row.setLayoutParams(lp);
                TextView icon=new TextView(this); icon.setText("💎 "+s); icon.setTextColor(Color.WHITE); icon.setTextSize(12); icon.setTypeface(null,Typeface.BOLD); icon.setPadding(12,6,12,6);
                GradientDrawable iconBg=new GradientDrawable(); iconBg.setColors(LevelUtils.getLevelColors(lvlForColor)); iconBg.setOrientation(GradientDrawable.Orientation.TL_BR); iconBg.setCornerRadius(16); icon.setBackground(iconBg);
                LinearLayout.LayoutParams iconLp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); iconLp.setMargins(0,0,12,0); icon.setLayoutParams(iconLp); row.addView(icon);
                String range = (s==141 ? "Lv.141-150" : "Lv."+s+"-"+(s+9));
                TextView t=new TextView(this); t.setText(range+" • "+LevelUtils.getLevelTitle(lvlForColor)); t.setTextColor(Color.WHITE); t.setTextSize(14); t.setTypeface(null,Typeface.BOLD); t.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1)); row.addView(t);
                TextView check=new TextView(this); check.setText((myWealthLevel>=s?"✅":"")); check.setTextColor(Color.WHITE); row.addView(check);
                root.addView(row);
            }
            TextView note=new TextView(this); note.setText("Wealth = Gift Sending • Charm = Gift Receiving\nGift মারলেই Level Auto বাড়বে • Max Lv.150"); note.setTextColor(Color.parseColor("#B0BEC5")); note.setTextSize(12); note.setGravity(Gravity.CENTER); note.setPadding(0,16,0,0); root.addView(note);
            b.setView(sv); b.setPositiveButton("Close",null); b.show();
        }catch(Exception e){ Toast.makeText(this,"Level - Coming Soon",Toast.LENGTH_SHORT).show(); }
    }
    private LinearLayout makeLevelCard(String label,int lvl,long exp,String type){
        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(28,24,28,24);
        GradientDrawable bg=new GradientDrawable(); bg.setColors(LevelUtils.getLevelColors(lvl)); bg.setOrientation(GradientDrawable.Orientation.TL_BR); bg.setCornerRadius(28); card.setBackground(bg);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(0,0,0,16); card.setLayoutParams(lp);
        TextView t1=new TextView(this); t1.setText(label); t1.setTextColor(Color.WHITE); t1.setTextSize(18); t1.setTypeface(null,Typeface.BOLD); card.addView(t1);
        TextView t2=new TextView(this); t2.setText("Lv."+lvl+" • "+LevelUtils.getLevelTitle(lvl)); t2.setTextColor(Color.WHITE); t2.setTextSize(15); t2.setTypeface(null,Typeface.BOLD); card.addView(t2);
        TextView t3=new TextView(this); t3.setText(type+": "+LevelUtils.formatCoins(exp)+" • Target: "+LevelUtils.getLevelTargetCoinsFormatted(lvl)); t3.setTextColor(Color.WHITE); t3.setTextSize(13); card.addView(t3);
        int pct = LevelUtils.getLevelProgressPercent(lvl, exp);
        ProgressBar pb=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal); pb.setMax(100); pb.setProgress(pct); pb.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,28)); card.addView(pb);
        TextView t4=new TextView(this);
        if(lvl>=150) t4.setText("MAX LEVEL 👑 - Legend 150!");
        else t4.setText("Next Lv."+(lvl+1)+" - Need "+LevelUtils.formatCoins(LevelUtils.getCoinsToNextLevel(lvl,exp))+" • "+pct+"%");
        t4.setTextColor(Color.WHITE); t4.setTextSize(12); card.addView(t4);
        return card;
    }
    private String getMyAnyIdFromPrefs() {
        String v = prefs.getString("MY_PROFILE_ID", "");
        if (v == null || v.isEmpty()) v = prefs.getString("PROFILE_ID", "");
        if (v == null || v.isEmpty()) v = prefs.getString("MY_ANY_ID", "");
        if (v == null || v.isEmpty()) v = prefs.getString("MY_9_DIGIT_ID", "");
        if (v == null) v = "";
        return v.trim();
    }
    private void saveMyAnyId(String id) {
        if (id == null || id.isEmpty()) return;
        myAnyId = id.trim();
        myProfileId = myAnyId;
        prefs.edit().putString("MY_PROFILE_ID", myAnyId).putString("PROFILE_ID", myAnyId).putString("MY_ANY_ID", myAnyId).putString("MY_9_DIGIT_ID", myAnyId).apply();
    }
    private String safeStr(DataSnapshot s, String k) {
        if (s.hasChild(k) && s.child(k).getValue() != null) return String.valueOf(s.child(k).getValue()).trim();
        return "";
    }
    private boolean profileIdMatches(DataSnapshot u, String target) {
        if (target == null || target.isEmpty()) return false;
        String t = target.trim().toLowerCase();
        String[] fields = new String[]{ safeStr(u, "profileId"), safeStr(u, "anyId"), safeStr(u, "myId"), safeStr(u, "idNumber"), safeStr(u, "displayId"), safeStr(u, "userId"), safeStr(u, "srId"), safeStr(u, "my9DigitId"), safeStr(u, "my9DigitID"), safeStr(u, "profileID") };
        for (String f : fields) { if (!f.isEmpty() && f.toLowerCase().equals(t)) return true; }
        return false;
    }
    private long getMaxSellerBalance(DataSnapshot snap) {
        long b1 = 0, b2 = 0, b3 = 0, b4 = 0;
        try { Object v = snap.child("businessCoins").getValue(); if (v != null) b1 = Long.parseLong(String.valueOf(v)); } catch (Exception ignored) {}
        try { Object v = snap.child("sellerCoinBalance").getValue(); if (v != null) b2 = Long.parseLong(String.valueOf(v)); } catch (Exception ignored) {}
        try { Object v = snap.child("coinBalance").getValue(); if (v != null) b3 = Long.parseLong(String.valueOf(v)); } catch (Exception ignored) {}
        try { Object v = snap.child("businessBalance").getValue(); if (v != null) b4 = Long.parseLong(String.valueOf(v)); } catch (Exception ignored) {}
        return Math.max(Math.max(b1, b2), Math.max(b3, b4));
    }
    private long getLongVal(DataSnapshot s,String k){ try{ Object v=s.child(k).getValue(); if(v==null) return 0; if(v instanceof Number) return ((Number)v).longValue(); return Long.parseLong(String.valueOf(v).replace(",","").trim()); }catch(Exception e){ return 0; } }
    private String getCachedPhoto(){
        if(cachedPhoto!=null && !cachedPhoto.isEmpty() && !cachedPhoto.equals("null")) return cachedPhoto;
        String p=prefs.getString("USER_PHOTO","");
        if(p==null||p.isEmpty()||p.equals("null")) p=prefs.getString("PROFILE_PHOTO","");
        if(p==null||p.isEmpty()||p.equals("null")) p=prefs.getString("USER_PHOTO_URL","");
        if(p!=null && !p.isEmpty() && !p.equals("null")) cachedPhoto=p;
        return cachedPhoto==null?"":cachedPhoto;
    }
    private void savePhotoPermanent(String photo){
        if(photo==null||photo.isEmpty()||photo.equals("null")) return;
        cachedPhoto=photo;
        prefs.edit().putString("USER_PHOTO",photo).putString("PROFILE_PHOTO",photo).putString("USER_PHOTO_URL",photo).apply();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) firebaseUid = user.getUid();
        if (firebaseUid.isEmpty()) firebaseUid = prefs.getString("FIREBASE_UID", "");
        myAnyId = getMyAnyIdFromPrefs();
        myProfileId = myAnyId;
        myUidResolved = firebaseUid;
        cachedPhoto=getCachedPhoto();
        if (!myAnyId.isEmpty()) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(myAnyId);
        } else if (!firebaseUid.isEmpty()) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUid);
        }
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(root);
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(30, 40, 30, 25);
        header.setBackgroundColor(Color.parseColor("#1B1035"));
        root.addView(header);
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(topRow);
        CardView cardAvatar = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(170, 170);
        cardParams.setMargins(0, 0, 25, 0);
        cardAvatar.setLayoutParams(cardParams);
        cardAvatar.setRadius(85);
        cardAvatar.setCardElevation(10);
        cardAvatar.setCardBackgroundColor(Color.parseColor("#FFD700"));
        cardAvatar.setPreventCornerOverlap(false);
        cardAvatar.setUseCompatPadding(false);
        cardAvatar.setOnClickListener(v -> goProfile());
        cardAvatar.setOnLongClickListener(v -> {
            startActivity(new Intent(MeActivity.this, SuperPanelLoginActivity.class));
            return true;
        });
        imgAvatar = new ImageView(this);
        imgAvatar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imgAvatar.setImageResource(android.R.drawable.ic_menu_my_calendar);
        imgAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imgAvatar.setBackgroundColor(Color.TRANSPARENT);
        cardAvatar.addView(imgAvatar);
        topRow.addView(cardAvatar);
        imgAvatar.setOnLongClickListener(v -> {
            startActivity(new Intent(MeActivity.this, SuperPanelLoginActivity.class));
            return true;
        });
        LinearLayout nameBox = new LinearLayout(this);
        nameBox.setOrientation(LinearLayout.VERTICAL);
        nameBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        nameBox.setOnClickListener(v -> goProfile());
        topRow.addView(nameBox);
        txtName = new TextView(this);
        txtName.setText(prefs.getString("USER_NAME", "User"));
        txtName.setTextColor(Color.WHITE);
        txtName.setTextSize(20);
        txtName.setTypeface(null, Typeface.BOLD);
        nameBox.addView(txtName);
        LinearLayout idRow = new LinearLayout(this);
        idRow.setOrientation(LinearLayout.HORIZONTAL);
        idRow.setGravity(Gravity.CENTER_VERTICAL);
        nameBox.addView(idRow);
        txtId = new TextView(this);
        txtId.setText("ID: " + myAnyId);
        txtId.setTextColor(Color.parseColor("#FFD700"));
        txtId.setTextSize(14);
        txtId.setTypeface(null, Typeface.BOLD);
        idRow.addView(txtId);
        TextView txtCopy = new TextView(this);
        txtCopy.setText("  📋 Copy");
        txtCopy.setTextColor(Color.parseColor("#FFD700"));
        txtCopy.setTextSize(13);
        txtCopy.setTypeface(null, Typeface.BOLD);
        txtCopy.setOnClickListener(v -> copyId());
        idRow.addView(txtCopy);
        txtGenderCountry = new TextView(this);
        txtGenderCountry.setText(getGenderCountryText());
        txtGenderCountry.setTextColor(Color.parseColor("#B0BEC5"));
        txtGenderCountry.setTextSize(12);
        nameBox.addView(txtGenderCountry);
        levelIconsRow = new LinearLayout(this);
        levelIconsRow.setOrientation(LinearLayout.HORIZONTAL);
        levelIconsRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams iconsLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        iconsLp.setMargins(0,10,0,0);
        levelIconsRow.setLayoutParams(iconsLp);
        nameBox.addView(levelIconsRow);
        txtVipBadge = new TextView(this);
        txtVipBadge.setText("");
        txtVipBadge.setTextColor(Color.parseColor("#0A0518"));
        txtVipBadge.setTextSize(11);
        txtVipBadge.setTypeface(null, Typeface.BOLD);
        txtVipBadge.setVisibility(View.GONE);
        txtVipBadge.setPadding(14,6,14,6);
        txtVipBadge.setGravity(Gravity.CENTER);
        levelIconsRow.addView(txtVipBadge);
        txtWealthBadge = new TextView(this);
        txtWealthBadge.setText("💎 1");
        txtWealthBadge.setTextColor(Color.WHITE);
        txtWealthBadge.setTextSize(11);
        txtWealthBadge.setTypeface(null, Typeface.BOLD);
        txtWealthBadge.setGravity(Gravity.CENTER);
        txtWealthBadge.setPadding(14,6,14,6);
        LinearLayout.LayoutParams wLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wLp.setMargins(8,0,0,0);
        txtWealthBadge.setLayoutParams(wLp);
        txtWealthBadge.setOnClickListener(v->showLevelDialog());
        levelIconsRow.addView(txtWealthBadge);
        txtCharmBadge = new TextView(this);
        txtCharmBadge.setText("💖 1");
        txtCharmBadge.setTextColor(Color.WHITE);
        txtCharmBadge.setTextSize(11);
        txtCharmBadge.setTypeface(null, Typeface.BOLD);
        txtCharmBadge.setGravity(Gravity.CENTER);
        txtCharmBadge.setPadding(14,6,14,6);
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cLp.setMargins(8,0,0,0);
        txtCharmBadge.setLayoutParams(cLp);
        txtCharmBadge.setOnClickListener(v->showLevelDialog());
        levelIconsRow.addView(txtCharmBadge);
        TextView btnProfileArrow = new TextView(this);
        btnProfileArrow.setText(">");
        btnProfileArrow.setTextSize(26);
        btnProfileArrow.setTypeface(null, Typeface.BOLD);
        btnProfileArrow.setTextColor(Color.parseColor("#FFD700"));
        btnProfileArrow.setPadding(20, 20, 20, 20);
        btnProfileArrow.setOnClickListener(v -> goProfile());
        topRow.addView(btnProfileArrow);
        LinearLayout copyIdBar = new LinearLayout(this);
        copyIdBar.setOrientation(LinearLayout.HORIZONTAL);
        copyIdBar.setBackgroundColor(Color.parseColor("#140B26"));
        copyIdBar.setPadding(30, 15, 30, 15);
        copyIdBar.setOnClickListener(v -> copyId());
        TextView tvCopyTitle = new TextView(this);
        tvCopyTitle.setText("Copy ID");
        tvCopyTitle.setTextColor(Color.parseColor("#FFD700"));
        tvCopyTitle.setTextSize(14);
        tvCopyTitle.setTypeface(null, Typeface.BOLD);
        copyIdBar.addView(tvCopyTitle);
        root.addView(copyIdBar);
        LinearLayout countRow = new LinearLayout(this);
        countRow.setOrientation(LinearLayout.HORIZONTAL);
        countRow.setGravity(Gravity.CENTER);
        countRow.setBackgroundColor(Color.parseColor("#1B1035"));
        countRow.setPadding(15, 20, 15, 20);
        root.addView(countRow);
        txtFollow = addCountItem(countRow, "0", "Follow", () -> showCountDialog("Follow", followCount));
        txtFans = addCountItem(countRow, "0", "Fans", () -> showCountDialog("Fans", fansCount));
        txtVisitor = addCountItem(countRow, "0", "Visitor", () -> showCountDialog("Visitor", visitorCount));
        LinearLayout menuContainer = new LinearLayout(this);
        menuContainer.setOrientation(LinearLayout.VERTICAL);
        menuContainer.setPadding(20, 20, 20, 30);
        root.addView(menuContainer);
        LinearLayout svipBox = new LinearLayout(this);
        svipBox.setOrientation(LinearLayout.VERTICAL);
        svipBox.setPadding(30, 28, 30, 28);
        GradientDrawable svipBg = new GradientDrawable();
        svipBg.setColors(new int[]{Color.parseColor("#FFD700"), Color.parseColor("#FF9800")});
        svipBg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        svipBg.setCornerRadius(24);
        svipBox.setBackground(svipBg);
        LinearLayout.LayoutParams svipLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        svipLp.setMargins(0, 0, 0, 14);
        svipBox.setLayoutParams(svipLp);
        svipBox.setOnClickListener(v -> showVipListDialog());
        menuContainer.addView(svipBox);
        TextView svipT1 = new TextView(this);
        svipT1.setText("SVIP CENTER");
        svipT1.setTextColor(Color.parseColor("#0A0518"));
        svipT1.setTextSize(19);
        svipT1.setTypeface(null, Typeface.BOLD);
        svipBox.addView(svipT1);
        txtSvipStatus = new TextView(this);
        txtSvipStatus.setText("SVIP 1-7  •  Tap to View  >");
        txtSvipStatus.setTextColor(Color.parseColor("#3A2A00"));
        txtSvipStatus.setTextSize(13);
        svipBox.addView(txtSvipStatus);
        LinearLayout walletBox = new LinearLayout(this);
        walletBox.setOrientation(LinearLayout.HORIZONTAL);
        walletBox.setGravity(Gravity.CENTER_VERTICAL);
        walletBox.setPadding(28, 24, 28, 24);
        GradientDrawable walletBg = new GradientDrawable();
        walletBg.setColor(Color.parseColor("#2A1B4E"));
        walletBg.setCornerRadius(24);
        walletBox.setBackground(walletBg);
        LinearLayout.LayoutParams walletLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        walletLp.setMargins(0, 0, 0, 14);
        walletBox.setLayoutParams(walletLp);
        walletBox.setOnClickListener(v -> openWalletDialog());
        menuContainer.addView(walletBox);
        LinearLayout walletLeft = new LinearLayout(this);
        walletLeft.setOrientation(LinearLayout.VERTICAL);
        walletLeft.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        walletBox.addView(walletLeft);
        TextView walletTitle = new TextView(this);
        walletTitle.setText("💰 My Wallet");
        walletTitle.setTextColor(Color.WHITE);
        walletTitle.setTextSize(16);
        walletTitle.setTypeface(null, Typeface.BOLD);
        walletLeft.addView(walletTitle);
        txtWalletCoins = new TextView(this);
        txtWalletCoins.setText("Coins: 0");
        txtWalletCoins.setTextColor(Color.parseColor("#FFD700"));
        txtWalletCoins.setTextSize(14);
        txtWalletCoins.setTypeface(null, Typeface.BOLD);
        walletLeft.addView(txtWalletCoins);
        TextView walletArrow = new TextView(this);
        walletArrow.setText("View Bill  >");
        walletArrow.setTextColor(Color.parseColor("#FFD700"));
        walletArrow.setTextSize(13);
        walletArrow.setTypeface(null, Typeface.BOLD);
        walletBox.addView(walletArrow);
        sellerWalletBox = new LinearLayout(this);
        sellerWalletBox.setOrientation(LinearLayout.HORIZONTAL);
        sellerWalletBox.setGravity(Gravity.CENTER_VERTICAL);
        sellerWalletBox.setPadding(28, 24, 28, 24);
        GradientDrawable sellerBg = new GradientDrawable();
        sellerBg.setColors(new int[]{Color.parseColor("#00C853"), Color.parseColor("#00796B")});
        sellerBg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        sellerBg.setCornerRadius(24);
        sellerWalletBox.setBackground(sellerBg);
        LinearLayout.LayoutParams sellerLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sellerLp.setMargins(0, 0, 0, 14);
        sellerWalletBox.setLayoutParams(sellerLp);
        sellerWalletBox.setVisibility(View.GONE);
        sellerWalletBox.setOnClickListener(v -> {
            if (!isSellerActive) return;
            Intent i = new Intent(MeActivity.this, SellerBusinessWalletActivity.class);
            i.putExtra("SELLER_PROFILE_ID", myProfileId);
            i.putExtra("SELLER_ANY_ID", myProfileId);
            i.putExtra("SELLER_9DIGIT_ID", myProfileId);
            i.putExtra("PROFILE_ID", myProfileId);
            startActivity(i);
        });
        menuContainer.addView(sellerWalletBox);
        LinearLayout sellerLeft = new LinearLayout(this);
        sellerLeft.setOrientation(LinearLayout.VERTICAL);
        sellerLeft.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        sellerWalletBox.addView(sellerLeft);
        TextView sellerTitle = new TextView(this);
        sellerTitle.setText("🏪 Seller Business Wallet");
        sellerTitle.setTextColor(Color.WHITE);
        sellerTitle.setTextSize(16);
        sellerTitle.setTypeface(null, Typeface.BOLD);
        sellerLeft.addView(sellerTitle);
        txtSellerBalance = new TextView(this);
        txtSellerBalance.setText("Business Coins: 0");
        txtSellerBalance.setTextColor(Color.WHITE);
        txtSellerBalance.setTextSize(14);
        txtSellerBalance.setTypeface(null, Typeface.BOLD);
        sellerLeft.addView(txtSellerBalance);
        TextView sellerArrow = new TextView(this);
        sellerArrow.setText("Sell Coins  >");
        sellerArrow.setTextColor(Color.WHITE);
        sellerArrow.setTextSize(13);
        sellerArrow.setTypeface(null, Typeface.BOLD);
        sellerWalletBox.addView(sellerArrow);
        LinearLayout vipRow = new LinearLayout(this);
        vipRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams vipRowLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vipRowLp.setMargins(0, 0, 0, 14);
        vipRow.setLayoutParams(vipRowLp);
        menuContainer.addView(vipRow);
        LinearLayout vipBox = new LinearLayout(this);
        vipBox.setOrientation(LinearLayout.VERTICAL);
        vipBox.setGravity(Gravity.CENTER);
        vipBox.setPadding(25, 20, 25, 20);
        GradientDrawable vipBg = new GradientDrawable();
        vipBg.setColors(new int[]{Color.parseColor("#3A0A4A"), Color.parseColor("#1A0B2E")});
        vipBg.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        vipBg.setCornerRadius(24);
        vipBox.setBackground(vipBg);
        vipBox.setOnClickListener(v -> showVipListDialog());
        LinearLayout.LayoutParams vipLp = new LinearLayout.LayoutParams(0, 180, 1);
        vipLp.setMargins(0, 0, 10, 0);
        vipBox.setLayoutParams(vipLp);
        vipRow.addView(vipBox);
        TextView vipT1 = new TextView(this);
        vipT1.setText("🐅 VIP CENTER");
        vipT1.setTextColor(Color.parseColor("#FFD700"));
        vipT1.setTextSize(17);
        vipT1.setTypeface(null, Typeface.BOLD);
        vipT1.setGravity(Gravity.CENTER);
        vipBox.addView(vipT1);
        txtVipStatus = new TextView(this);
        txtVipStatus.setText("Tap to View >");
        txtVipStatus.setTextColor(Color.WHITE);
        txtVipStatus.setTextSize(12);
        txtVipStatus.setGravity(Gravity.CENTER);
        vipBox.addView(txtVipStatus);
        LinearLayout rechargeBox = makeBigCard("🎁 Recharge", "Top up >", "#FF6D00", Color.WHITE);
        rechargeBox.setOnClickListener(v -> showRechargeLocked());
        LinearLayout.LayoutParams recLp = new LinearLayout.LayoutParams(0, 180, 1);
        recLp.setMargins(10, 0, 0, 0);
        rechargeBox.setLayoutParams(recLp);
        vipRow.addView(rechargeBox);
        TextView toolsTitle = new TextView(this);
        toolsTitle.setText("General Tools");
        toolsTitle.setTextColor(Color.parseColor("#FFD700"));
        toolsTitle.setTextSize(14);
        toolsTitle.setTypeface(null, Typeface.BOLD);
        toolsTitle.setPadding(10, 15, 10, 10);
        menuContainer.addView(toolsTitle);
        LinearLayout toolsRow = new LinearLayout(this);
        toolsRow.setOrientation(LinearLayout.HORIZONTAL);
        toolsRow.setGravity(Gravity.CENTER);
        toolsRow.setBackgroundColor(Color.parseColor("#1B1035"));
        toolsRow.setPadding(15, 22, 15, 22);
        GradientDrawable toolsBg = new GradientDrawable();
        toolsBg.setColor(Color.parseColor("#1B1035"));
        toolsBg.setCornerRadius(24);
        toolsRow.setBackground(toolsBg);
        LinearLayout.LayoutParams toolsLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        toolsLp.setMargins(0, 0, 0, 16);
        toolsRow.setLayoutParams(toolsLp);
        menuContainer.addView(toolsRow);
        addToolItem(toolsRow, "🛍️", "Store", v -> openStore());
        addToolItem(toolsRow, "👗", "Dress");
        addToolItem(toolsRow, "🎂", "Birthday");
        levelToolView = addToolItem(toolsRow, "📊", "Level", v -> showLevelDialog());
        agencyToolView = addToolItem(toolsRow, "🏛️", "Agency", v -> openAgency());
        if (agencyToolView != null) agencyToolView.setVisibility(View.GONE);
        TextView serviceTitle = new TextView(this);
        serviceTitle.setText("My Services");
        serviceTitle.setTextColor(Color.parseColor("#FFD700"));
        serviceTitle.setTextSize(14);
        serviceTitle.setTypeface(null, Typeface.BOLD);
        serviceTitle.setPadding(10, 5, 10, 10);
        menuContainer.addView(serviceTitle);
        LinearLayout listBox = new LinearLayout(this);
        listBox.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable listBg = new GradientDrawable();
        listBg.setColor(Color.parseColor("#1B1035"));
        listBg.setCornerRadius(24);
        listBox.setBackground(listBg);
        menuContainer.addView(listBox);
        addMenuRow(listBox, "🎯 Newbie Mission");
        addMenuRow(listBox, "👥 Invite Friend");
        addMenuRow(listBox, "🏅 Medal");
        addMenuRow(listBox, "👨‍👩‍👧 Family");
        hostCenterRowView = addMenuRow(listBox, "🎤 Host Center", v -> openHostCenter());
        if (hostCenterRowView != null) hostCenterRowView.setVisibility(View.GONE);
        addMenuRow(listBox, "📅 Daily Tasks");
        addMenuRow(listBox, "⚙️ Setting");
        LinearLayout logoutLayout = new LinearLayout(this);
        logoutLayout.setOrientation(LinearLayout.HORIZONTAL);
        logoutLayout.setGravity(Gravity.CENTER);
        logoutLayout.setPadding(20, 28, 20, 28);
        GradientDrawable logoutBg = new GradientDrawable();
        logoutBg.setColor(Color.parseColor("#2A1B4E"));
        logoutBg.setCornerRadius(24);
        logoutLayout.setBackground(logoutBg);
        LinearLayout.LayoutParams logoutLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        logoutLp.setMargins(0, 25, 0, 40);
        logoutLayout.setLayoutParams(logoutLp);
        TextView btnLogout = new TextView(this);
        btnLogout.setText("🚪 Log Out");
        btnLogout.setTextColor(Color.parseColor("#FF4081"));
        btnLogout.setTextSize(16);
        btnLogout.setTypeface(null, Typeface.BOLD);
        logoutLayout.addView(btnLogout);
        logoutLayout.setOnClickListener(v -> {
            prefs.edit().putBoolean("IS_LOGGED_IN", false).apply();
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(MeActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
        menuContainer.addView(logoutLayout);
        setContentView(scroll);
        try {
            IntentFilter filter = new IntentFilter("com.sr.voicechat.PROFILE_UPDATED");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(profileReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(profileReceiver, filter);
            }
        } catch (Exception ignored) {}
        showFromPrefs();
        applyRoleVisibility();
        applySellerVisibility();
        loadMeData();
        listenVipAssignments();
        listenFirestoreRole();
        listenSellerStatus();
    }
    private void listenSellerStatus() {
        if (sellerListenerStarted) return;
        sellerListenerStarted = true;
        if (!myProfileId.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("sellers").child(myProfileId)
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) { if (snap.exists()) applySellerSnap(snap); }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
        }
        if (!firebaseUid.isEmpty() && !firebaseUid.equals(myProfileId)) {
            FirebaseDatabase.getInstance().getReference("sellers").child(firebaseUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) { if (snap.exists()) applySellerSnap(snap); }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
        }
        if (!firebaseUid.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("users").child(firebaseUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        if (!snap.exists()) return;
                        boolean flag = getBool(snap, "isSeller", false) || getBool(snap, "showSellerWallet", false);
                        if (!flag) return;
                        String pid = safeStr(snap, "profileId");
                        if (pid.isEmpty()) pid = safeStr(snap, "anyId");
                        if (!pid.isEmpty() && myProfileId.isEmpty()) saveMyAnyId(pid);
                        long bal = getMaxSellerBalance(snap);
                        if (bal > 0) applySellerBalance(bal, true);
                        else applySellerBalance(0, true);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
        }
        if (!myProfileId.isEmpty() && !myProfileId.equals(firebaseUid)) {
            FirebaseDatabase.getInstance().getReference("users").child(myProfileId)
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        if (!snap.exists()) return;
                        boolean flag = getBool(snap, "isSeller", false) || getBool(snap, "showSellerWallet", false);
                        if (!flag) return;
                        long bal = getMaxSellerBalance(snap);
                        if (bal > 0) applySellerBalance(bal, true);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
        }
    }
    private void applySellerBalance(long bal, boolean active) {
        if (bal > sellerBusinessCoins) sellerBusinessCoins = bal;
        else if (sellerBusinessCoins == 0) sellerBusinessCoins = bal;
        if (active) isSellerActive = true;
        prefs.edit().putBoolean("IS_SELLER", isSellerActive).putLong("SELLER_BUSINESS_COINS", sellerBusinessCoins).apply();
        applySellerVisibility();
    }
    private void hideSellerWallet() {
        isSellerActive = false;
        sellerBusinessCoins = 0;
        prefs.edit().putBoolean("IS_SELLER", false).putLong("SELLER_BUSINESS_COINS", 0).apply();
        applySellerVisibility();
    }
    private void applySellerSnap(DataSnapshot snap) {
        try {
            boolean isSeller = getBool(snap, "isSeller", false) || getBool(snap, "showSellerWallet", false);
            if (snap.exists()) isSeller = true;
            String status = getStr(snap, "status", "").toLowerCase();
            boolean active = isSeller && !"blocked".equals(status) && !"rejected".equals(status) && !"pending".equals(status);
            if (!active) return;
            long bal = getMaxSellerBalance(snap);
            applySellerBalance(bal, true);
        } catch (Exception ignored) {}
    }
    private void applySellerVisibility() {
        boolean show = isSellerActive;
        if (sellerWalletBox != null) sellerWalletBox.setVisibility(show ? View.VISIBLE : View.GONE);
        if (txtSellerBalance != null) txtSellerBalance.setText("Business Coins: " + sellerBusinessCoins);
    }
    private void showVipListDialog() {
        try {
            List<VipModel> list = VipModel.createPremiumList();
            android.app.Dialog dialog = new android.app.Dialog(this);
            dialog.setContentView(R.layout.dialog_vip_list);
            RecyclerView rv = dialog.findViewById(R.id.recyclerVipList);
            if (rv != null) {
                rv.setLayoutManager(new LinearLayoutManager(this));
                VipAdapter adapter = new VipAdapter(this, list);
                rv.setAdapter(adapter);
            }
            Button btnClose = dialog.findViewById(R.id.btnCloseVipDialog);
            if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "VIP - Coming Soon", Toast.LENGTH_SHORT).show();
        }
    }
    private void listenVipAssignments() {
        if (vipAssignmentListenerStarted) return;
        vipAssignmentListenerStarted = true;
        if (!myAnyId.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("vip_assignments").child(myAnyId)
                .addValueEventListener(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        if (snap.exists()) { applyVipSnap(snap); return; }
                        listenVipByFirebaseUid();
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) { listenVipByFirebaseUid(); }
                });
        } else {
            listenVipByFirebaseUid();
        }
    }
    private void listenVipByFirebaseUid() {
        if (firebaseUid.isEmpty() || firebaseUid.equals(myAnyId)) return;
        FirebaseDatabase.getInstance().getReference("vip_assignments").child(firebaseUid)
            .addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    if (snap.exists()) applyVipSnap(snap);
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
    }
    private void applyVipSnap(DataSnapshot snap) {
        try {
            String lvlStr = snap.child("vipLevel").getValue() == null ? "0" : String.valueOf(snap.child("vipLevel").getValue());
            int v = 0;
            try { v = Integer.parseInt(lvlStr); } catch (Exception ignored) {}
            long expire = 0;
            try { Object ex = snap.child("expireAt").getValue(); if (ex != null) expire = Long.parseLong(String.valueOf(ex)); } catch (Exception ignored) {}
            if (expire == 0) { try { Object ex2 = snap.child("expireTs").getValue(); if (ex2 != null) expire = Long.parseLong(String.valueOf(ex2)); } catch (Exception ignored) {} }
            if (expire > 0 && System.currentTimeMillis() > expire) v = 0;
            if (v == 0 && myVipLevel > 0 && expire == 0) return;
            if (v != myVipLevel) {
                myVipLevel = v;
                prefs.edit().putInt("VIP_LEVEL", v).apply();
                updateVipUI();
            } else updateVipUI();
        } catch (Exception ignored) {}
    }
    private void updateVipUI() {
        if (txtVipBadge == null) return;
        if (myVipLevel >= 1 && myVipLevel <= 8) {
            String[] emojis = {"", "🐅", "🐯", "🦅", "🔥", "🐉", "👑", "🐲", "💜"};
            txtVipBadge.setText(emojis[myVipLevel] + " VIP " + myVipLevel);
            GradientDrawable bg=new GradientDrawable();
            bg.setColors(new int[]{Color.parseColor("#FFD700"), Color.parseColor("#FF9800")});
            bg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
            bg.setCornerRadius(20);
            txtVipBadge.setBackground(bg);
            txtVipBadge.setTextColor(Color.parseColor("#0A0518"));
            txtVipBadge.setVisibility(View.VISIBLE);
            if (txtVipStatus != null) txtVipStatus.setText("✅ VIP " + myVipLevel + " Active\nTap to View >");
            if (txtSvipStatus != null) txtSvipStatus.setText("✅ VIP " + myVipLevel + "  •  Tap to View  >");
        } else {
            txtVipBadge.setVisibility(View.GONE);
            if (txtVipStatus != null) txtVipStatus.setText("Tap to View >");
            if (txtSvipStatus != null) txtSvipStatus.setText("SVIP 1-7  •  Tap to View  >");
        }
    }
    private void copyId() {
        if (myAnyId.isEmpty()) return;
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("ID", myAnyId));
        Toast.makeText(this, "ID Copied: " + myAnyId, Toast.LENGTH_SHORT).show();
    }
    private void openStore() {
        try {
            Intent i = new Intent(MeActivity.this, StoreActivity.class);
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Store khulte parchi na", Toast.LENGTH_SHORT).show();
        }
    }
    // ===== NEW - GLOBAL - Profile ID = Counting ID =====
    private void openAgency() {
        if (!isAgencyActive) { Toast.makeText(this, "You are not an Agency yet", Toast.LENGTH_SHORT).show(); return; }
        String uid = firebaseUid;
        if (uid.isEmpty()) { FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser(); if (fu != null) uid = fu.getUid(); }
        if (uid.isEmpty()) uid = prefs.getString("FIREBASE_UID", "");
        Intent i = new Intent(this, AgencyDashboardActivity.class);
        i.putExtra("AGENCY_OWNER_ID", uid);
        i.putExtra("AGENCY_UID", uid);
        i.putExtra("AGENCY_PROFILE_ID", myProfileId);
        i.putExtra("PROFILE_ID", myProfileId);
        i.putExtra("AGENCY_NAME", prefs.getString("AGENCY_NAME", "SR Agency"));
        startActivity(i);
    }
    private void openHostCenter() {
        if (!isHostActive) { Toast.makeText(this, "You are not a Host yet", Toast.LENGTH_SHORT).show(); return; }
        try {
            Intent i = new Intent(this, HostCenterActivity.class);
            i.putExtra("HOST_UID", firebaseUid);
            i.putExtra("HOST_PROFILE_ID", myProfileId);
            i.putExtra("HOST_ANY_ID", myProfileId);
            i.putExtra("PROFILE_ID", myProfileId);
            i.putExtra("HOST_AGENCY_ID", prefs.getString("HOST_AGENCY_ID", ""));
            i.putExtra("HOST_AGENCY_NAME", prefs.getString("HOST_AGENCY_NAME", ""));
            startActivity(i);
        } catch (Exception e) {
            try { Intent i2 = new Intent(this, HostPolicyActivity.class);
                i2.putExtra("HOST_PROFILE_ID", myProfileId);
                i2.putExtra("PROFILE_ID", myProfileId);
                startActivity(i2);
            }
            catch (Exception e2) { Toast.makeText(this, "Host Center - Coming Soon", Toast.LENGTH_SHORT).show(); }
        }
    }
    private void applyRoleVisibility() {
        boolean cacheAgency = prefs.getBoolean("IS_AGENCY", false);
        boolean cacheActive = prefs.getBoolean("AGENCY_ACTIVE", false);
        boolean rtdbAgency = prefs.getBoolean("RTDB_IS_AGENCY", false);
        boolean showAgency = (cacheAgency && cacheActive) || (rtdbAgency && cacheActive) || (cacheAgency && rtdbAgency);
        if(cacheAgency && prefs.getBoolean("AGENCY_EVER", false)) showAgency = cacheActive || true;
        boolean showHost = prefs.getBoolean("IS_HOST", false) && prefs.getBoolean("HOST_ACTIVE", false);
        isAgencyActive = showAgency; isHostActive = showHost;
        if (agencyToolView != null) agencyToolView.setVisibility(showAgency ? View.VISIBLE : View.GONE);
        if (hostCenterRowView != null) hostCenterRowView.setVisibility(showHost ? View.VISIBLE : View.GONE);
        if (txtGenderCountry != null) txtGenderCountry.setText(getGenderCountryText());
    }
    private void listenFirestoreRole() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            if (!myAnyId.isEmpty()) {
                db.collection("agencies").document(myAnyId).addSnapshotListener((snap, e) -> {
                    if (e != null) return;
                    if (snap != null && snap.exists()) {
                        Object st = snap.get("status"); String status = st == null ? "" : String.valueOf(st).toLowerCase();
                        boolean active = !"blocked".equals(status) && !"rejected".equals(status) && !"pending".equals(status);
                        if (status.isEmpty()) active = true;
                        prefs.edit().putBoolean("IS_AGENCY", true).putBoolean("AGENCY_ACTIVE", active).putBoolean("AGENCY_EVER", true).apply();
                        Object nm = snap.get("name"); if (nm != null) prefs.edit().putString("AGENCY_NAME", String.valueOf(nm)).apply();
                    } else {
                        boolean rtdbAgency = prefs.getBoolean("RTDB_IS_AGENCY", false);
                        boolean ever = prefs.getBoolean("AGENCY_EVER", false);
                        if (!rtdbAgency && !ever) prefs.edit().putBoolean("IS_AGENCY", false).putBoolean("AGENCY_ACTIVE", false).apply();
                    }
                    applyRoleVisibility();
                });
                if (!firebaseUid.isEmpty()) {
                    db.collection("agencies").document(firebaseUid).addSnapshotListener((snap, e) -> {
                        if (e != null) return;
                        if (snap != null && snap.exists()) {
                            Object st = snap.get("status"); String status = st == null ? "" : String.valueOf(st).toLowerCase();
                            boolean active = !"blocked".equals(status) && !"rejected".equals(status);
                            prefs.edit().putBoolean("IS_AGENCY", true).putBoolean("AGENCY_ACTIVE", active).putBoolean("AGENCY_EVER", true).apply();
                            applyRoleVisibility();
                        }
                    });
                    db.collection("agencies").whereEqualTo("ownerUid", firebaseUid).addSnapshotListener((snaps, e) -> {
                        if (e != null || snaps == null) return;
                        if (!snaps.isEmpty()) { prefs.edit().putBoolean("IS_AGENCY", true).putBoolean("AGENCY_ACTIVE", true).putBoolean("AGENCY_EVER", true).apply(); applyRoleVisibility(); }
                    });
                }
            }
            if (!firebaseUid.isEmpty()) {
                db.collection("hosts").document(firebaseUid).addSnapshotListener((snap, e) -> {
                    if (e != null) return;
                    if (snap != null && snap.exists()) prefs.edit().putBoolean("IS_HOST", true).putBoolean("HOST_ACTIVE", true).apply();
                    else { boolean rtdbHost = prefs.getBoolean("RTDB_IS_HOST", false); if (!rtdbHost) prefs.edit().putBoolean("IS_HOST", false).putBoolean("HOST_ACTIVE", false).apply(); }
                    applyRoleVisibility();
                });
                db.collection("hosts").whereEqualTo("ownerUid", firebaseUid).addSnapshotListener((snaps, e) -> {
                    if (e != null || snaps == null) return;
                    if (!snaps.isEmpty()) { prefs.edit().putBoolean("IS_HOST", true).putBoolean("HOST_ACTIVE", true).apply(); applyRoleVisibility(); }
                });
                if (!myAnyId.isEmpty()) {
                    db.collection("hosts").document(myAnyId).addSnapshotListener((snap, e) -> {
                        if (e != null) return;
                        if (snap != null && snap.exists()) { prefs.edit().putBoolean("IS_HOST", true).putBoolean("HOST_ACTIVE", true).apply(); applyRoleVisibility(); }
                    });
                }
            }
        } catch (Exception ignored) {}
    }
    private void openWalletDialog() {
        try { WalletRechargeDialog dialog = new WalletRechargeDialog(); dialog.show(getSupportFragmentManager(), "WalletRechargeDialog"); }
        catch (Exception e) { Toast.makeText(this, "Wallet - Coming Soon", Toast.LENGTH_SHORT).show(); }
    }
    private void showRechargeLocked() {
        try { WalletRechargeDialog dialog = new WalletRechargeDialog(); dialog.show(getSupportFragmentManager(), "WalletRechargeDialog"); }
        catch (Exception e) { Toast.makeText(this, "Recharge - Coming Soon", Toast.LENGTH_SHORT).show(); }
    }
    @Override protected void onDestroy() { super.onDestroy(); try { unregisterReceiver(profileReceiver); } catch (Exception ignored) {} }
    private TextView addCountItem(LinearLayout parent, String num, String label, Runnable onClick) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER);
        box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        box.setOnClickListener(v -> onClick.run()); parent.addView(box);
        TextView tvNum = new TextView(this); tvNum.setText(num); tvNum.setTextColor(Color.WHITE); tvNum.setTextSize(18); tvNum.setTypeface(null, Typeface.BOLD); box.addView(tvNum);
        TextView tvLabel = new TextView(this); tvLabel.setText(label); tvLabel.setTextColor(Color.parseColor("#B0BEC5")); tvLabel.setTextSize(12); box.addView(tvLabel);
        return tvNum;
    }
    private LinearLayout makeBigCard(String title, String sub, String bgColor, int titleColor) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER); box.setPadding(25, 20, 25, 20);
        GradientDrawable bg = new GradientDrawable(); bg.setColor(Color.parseColor(bgColor)); bg.setCornerRadius(24); box.setBackground(bg);
        TextView t1 = new TextView(this); t1.setText(title); t1.setTextColor(titleColor); t1.setTextSize(17); t1.setTypeface(null, Typeface.BOLD); t1.setGravity(Gravity.CENTER); box.addView(t1);
        TextView t2 = new TextView(this); t2.setText(sub); t2.setTextColor(Color.parseColor("#FFFFFF")); t2.setTextSize(12); t2.setGravity(Gravity.CENTER); box.addView(t2);
        return box;
    }
    private LinearLayout addToolItem(LinearLayout parent, String icon, String label) { return addToolItem(parent, icon, label, v -> Toast.makeText(this, label + " - Coming Soon", Toast.LENGTH_SHORT).show()); }
    private LinearLayout addToolItem(LinearLayout parent, String icon, String label, View.OnClickListener click) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER);
        box.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        box.setOnClickListener(click); parent.addView(box);
        TextView iv = new TextView(this); iv.setText(icon); iv.setTextSize(30); iv.setGravity(Gravity.CENTER); box.addView(iv);
        TextView tv = new TextView(this); tv.setText(label); tv.setTextColor(Color.WHITE); tv.setTextSize(12); tv.setGravity(Gravity.CENTER); box.addView(tv);
        return box;
    }
    private LinearLayout addMenuRow(LinearLayout parent, String title) { return addMenuRow(parent, title, v -> Toast.makeText(this, title + " - Coming Soon", Toast.LENGTH_SHORT).show()); }
    private LinearLayout addMenuRow(LinearLayout parent, String title, View.OnClickListener click) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(30, 26, 30, 26); row.setOnClickListener(click); parent.addView(row);
        TextView t = new TextView(this); t.setText(title); t.setTextColor(Color.WHITE); t.setTextSize(15); t.setTypeface(null, Typeface.BOLD);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1)); row.addView(t);
        TextView arrow = new TextView(this); arrow.setText(">"); arrow.setTextColor(Color.parseColor("#B0BEC5")); arrow.setTextSize(16); arrow.setTypeface(null, Typeface.BOLD); row.addView(arrow);
        return row;
    }
    private void goProfile() {
        try { Intent i = new Intent(MeActivity.this, PublicProfileActivity.class); i.putExtra("TARGET_ANY_ID", myAnyId); i.putExtra("TARGET_9DIGIT_ID", myAnyId); i.putExtra("TARGET_NAME", prefs.getString("USER_NAME", "User")); startActivity(i); }
        catch (Exception e) { Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show(); }
    }
    private void showCountDialog(String type, String count) { new AlertDialog.Builder(this).setTitle(type + " - " + count).setMessage("Your " + type + " list will appear here.\n\nOnly you can see this.").setPositiveButton("OK", null).show(); }
    private String getCountryShortGlobal(String c){
        if(c==null||c.isEmpty()) return "";
        String cl=c.trim().toLowerCase();
        if(cl.contains("bangladesh")||cl.equals("bd")||cl.contains("🇧🇩")) return "🇧🇩 BD";
        if(cl.contains("india")||cl.equals("in")) return "🇮🇳 IN";
        if(cl.contains("pakistan")||cl.equals("pk")) return "🇵🇰 PK";
        if(cl.contains("nepal")||cl.equals("np")) return "🇳🇵 NP";
        if(cl.contains("sri lanka")||cl.contains("srilanka")) return "🇱🇰 LK";
        if(cl.contains("bhutan")) return "🇧🇹 BT";
        if(cl.contains("afghanistan")) return "🇦🇫 AF";
        if(cl.contains("myanmar")||cl.contains("burma")) return "🇲🇲 MM";
        if(cl.contains("thailand")) return "🇹🇭 TH";
        if(cl.contains("malaysia")) return "🇲🇾 MY";
        if(cl.contains("indonesia")) return "🇮🇩 ID";
        if(cl.contains("singapore")) return "🇸🇬 SG";
        if(cl.contains("philippines")) return "🇵🇭 PH";
        if(cl.contains("vietnam")) return "🇻🇳 VN";
        if(cl.contains("cambodia")) return "🇰🇭 KH";
        if(cl.contains("laos")) return "🇱🇦 LA";
        if(cl.contains("china")) return "🇨🇳 CN";
        if(cl.contains("hong kong")) return "🇭🇰 HK";
        if(cl.contains("taiwan")) return "🇹🇼 TW";
        if(cl.contains("japan")) return "🇯🇵 JP";
        if(cl.contains("korea")) return "🇰🇷 KR";
        if(cl.contains("mongolia")) return "🇲🇳 MN";
        if(cl.contains("united arab")||cl.contains("uae")||cl.contains("dubai")||cl.contains("abu dhabi")) return "🇦🇪 AE";
        if(cl.contains("saudi")) return "🇸🇦 SA";
        if(cl.contains("qatar")) return "🇶🇦 QA";
        if(cl.contains("kuwait")) return "🇰🇼 KW";
        if(cl.contains("oman")) return "🇴🇲 OM";
        if(cl.contains("bahrain")) return "🇧🇭 BH";
        if(cl.contains("yemen")) return "🇾🇪 YE";
        if(cl.contains("jordan")) return "🇯🇴 JO";
        if(cl.contains("lebanon")) return "🇱🇧 LB";
        if(cl.contains("iraq")) return "🇮🇶 IQ";
        if(cl.contains("iran")) return "🇮🇷 IR";
        if(cl.contains("israel")) return "🇮🇱 IL";
        if(cl.contains("palestine")) return "🇵🇸 PS";
        if(cl.contains("turkey")||cl.contains("turkiye")) return "🇹🇷 TR";
        if(cl.contains("united states")||cl.contains("america")||cl.equals("us")||cl.equals("usa")) return "🇺🇸 US";
        if(cl.contains("canada")) return "🇨🇦 CA";
        if(cl.contains("mexico")) return "🇲🇽 MX";
        if(cl.contains("brazil")) return "🇧🇷 BR";
        if(cl.contains("argentina")) return "🇦🇷 AR";
        if(cl.contains("chile")) return "🇨🇱 CL";
        if(cl.contains("colombia")) return "🇨🇴 CO";
        if(cl.contains("peru")) return "🇵🇪 PE";
        if(cl.contains("venezuela")) return "🇻🇪 VE";
        if(cl.contains("united kingdom")||cl.contains("uk")||cl.contains("england")||cl.contains("london")||cl.contains("britain")) return "🇬🇧 UK";
        if(cl.contains("ireland")) return "🇮🇪 IE";
        if(cl.contains("germany")) return "🇩🇪 DE";
        if(cl.contains("france")) return "🇫🇷 FR";
        if(cl.contains("italy")) return "🇮🇹 IT";
        if(cl.contains("spain")) return "🇪🇸 ES";
        if(cl.contains("portugal")) return "🇵🇹 PT";
        if(cl.contains("netherlands")||cl.contains("holland")) return "🇳🇱 NL";
        if(cl.contains("belgium")) return "🇧🇪 BE";
        if(cl.contains("switzerland")) return "🇨🇭 CH";
        if(cl.contains("austria")) return "🇦🇹 AT";
        if(cl.contains("sweden")) return "🇸🇪 SE";
        if(cl.contains("norway")) return "🇳🇴 NO";
        if(cl.contains("denmark")) return "🇩🇰 DK";
        if(cl.contains("finland")) return "🇫🇮 FI";
        if(cl.contains("poland")) return "🇵🇱 PL";
        if(cl.contains("greece")) return "🇬🇷 GR";
        if(cl.contains("ukraine")) return "🇺🇦 UA";
        if(cl.contains("russia")) return "🇷🇺 RU";
        if(cl.contains("uzbekistan")) return "🇺🇿 UZ";
        if(cl.contains("kazakhstan")) return "🇰🇿 KZ";
        if(cl.contains("kyrgyzstan")) return "🇰🇬 KG";
        if(cl.contains("tajikistan")) return "🇹🇯 TJ";
        if(cl.contains("turkmenistan")) return "🇹🇲 TM";
        if(cl.contains("australia")) return "🇦🇺 AU";
        if(cl.contains("new zealand")) return "🇳🇿 NZ";
        if(cl.contains("fiji")) return "🇫🇯 FJ";
        if(cl.contains("egypt")) return "🇪🇬 EG";
        if(cl.contains("morocco")) return "🇲🇦 MA";
        if(cl.contains("algeria")) return "🇩🇿 DZ";
        if(cl.contains("tunisia")) return "🇹🇳 TN";
        if(cl.contains("libya")) return "🇱🇾 LY";
        if(cl.contains("sudan")) return "🇸🇩 SD";
        if(cl.contains("ethiopia")) return "🇪🇹 ET";
        if(cl.contains("kenya")) return "🇰🇪 KE";
        if(cl.contains("nigeria")) return "🇳🇬 NG";
        if(cl.contains("ghana")) return "🇬🇭 GH";
        if(cl.contains("south africa")) return "🇿🇦 ZA";
        if(cl.contains("angola")) return "🇦🇴 AO";
        if(cl.contains("zimbabwe")) return "🇿🇼 ZW";
        return "🌍 " + c.trim();
    }
    private String getGenderIconPremium(String g){
        if(g==null) return "";
        String gl=g.trim().toLowerCase();
        if(gl.contains("female")||gl.contains("girl")||gl.contains("woman")||gl.equals("f")||gl.equals("lady")) return "♀️";
        if(gl.contains("male")||gl.contains("boy")||gl.contains("man")||gl.equals("m")) return "♂️";
        return "";
    }
    private String getGenderCountryText() {
        String g = prefs.getString("USER_GENDER", "");
        String c = prefs.getString("USER_COUNTRY", "");
        String countryShort = getCountryShortGlobal(c);
        String genderIcon = getGenderIconPremium(g);
        String base = "";
        if (!genderIcon.isEmpty() && !countryShort.isEmpty()) base = genderIcon + " | " + countryShort;
        else if (!countryShort.isEmpty()) base = countryShort;
        else if (!genderIcon.isEmpty()) base = genderIcon;
        else if (!g.isEmpty()) base = g;
        else base = "Complete Your Profile";
        boolean showAgency = prefs.getBoolean("IS_AGENCY", false) && prefs.getBoolean("AGENCY_ACTIVE", false);
        if(!showAgency && prefs.getBoolean("AGENCY_EVER", false) && prefs.getBoolean("IS_AGENCY", false)) showAgency=true;
        if (showAgency) base = base + "  |  🏷️ AGENCY";
        if (prefs.getBoolean("IS_SELLER", false)) base = base + "  |  🏪 SELLER";
        return base;
    }
    @Override protected void onResume() { super.onResume(); showFromPrefs(); applyRoleVisibility(); applySellerVisibility(); loadMeData(); }
    private void showFromPrefs() {
        String name = prefs.getString("USER_NAME", "User");
        String photo = getCachedPhoto();
        myAnyId = getMyAnyIdFromPrefs(); myProfileId = myAnyId; myVipLevel = prefs.getInt("VIP_LEVEL", 0);
        myWealthLevel = prefs.getInt("WEALTH_LEVEL", 1);
        myCharmLevel = prefs.getInt("CHARM_LEVEL", 1);
        if (myWealthLevel < 1) myWealthLevel = 1;
        if (myCharmLevel < 1) myCharmLevel = 1;
        myTotalSending = prefs.getLong("TOTAL_SENDING", 0);
        myTotalReceiving = prefs.getLong("TOTAL_RECEIVING", 0);
        if (txtName != null) txtName.setText(name);
        if (txtId != null) txtId.setText("ID: " + myAnyId);
        if (txtGenderCountry != null) txtGenderCountry.setText(getGenderCountryText());
        updateVipUI(); updateLevelBadge();
        if(!photo.isEmpty()) showPhoto(photo, imgAvatar);
    }
    private void loadMeData() {
        if (userRef == null) return; if (rtdbListenerStarted) return; rtdbListenerStarted = true;
        userRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                try {
                    if (!snap.exists()) {
                        if (!firebaseUid.isEmpty() && !firebaseUid.equals(myAnyId)) {
                            userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUid);
                            rtdbListenerStarted = false;
                            loadMeData();
                        }
                        return;
                    }
                    String name = getStr(snap, "name", prefs.getString("USER_NAME", "User"));
                    String id = getStr(snap, "profileId", "");
                    if (id.isEmpty()) id = getStr(snap, "anyId", "");
                    if (id.isEmpty()) id = getStr(snap, "myId", "");
                    if (id.isEmpty()) id = getStr(snap, "idNumber", "");
                    if (id.isEmpty()) id = getStr(snap, "my9DigitId", "");
                    if (id.isEmpty()) id = getStr(snap, "nineDigitId", "");
                    if (id.isEmpty()) id = getStr(snap, "srId", "");
                    if (id.isEmpty()) id = getStr(snap, "userId", "");
                    if (id.isEmpty()) id = snap.getKey() == null ? myAnyId : snap.getKey();
                    String country = getStr(snap, "country", prefs.getString("USER_COUNTRY", ""));
                    String gender = getStr(snap, "gender", prefs.getString("USER_GENDER", ""));
                    String photo = getStr(snap, "photoUrl", "");
                    if (photo.isEmpty()) photo = getStr(snap, "photo", "");
                    if (photo.isEmpty()) photo = getStr(snap, "photoBase64", "");
                    followCount = getStr(snap, "followCount", "0"); fansCount = getStr(snap, "fansCount", "0"); visitorCount = getStr(snap, "visitorCount", "0");
                    String coins = getStr(snap, "coins", "0");
                    if (coins.equals("0")) coins = getStr(snap, "walletCoins", "0");
                    if (coins.equals("0")) coins = getStr(snap, "sendingCoins", "0");
                    long sending = Math.max(getLongVal(snap,"sending"), getLongVal(snap,"totalSending"));
                    long receiving = Math.max(getLongVal(snap,"receiving"), getLongVal(snap,"totalReceiving"));
                    receiving = Math.max(receiving, Math.max(getLongVal(snap,"targetCoins"), getLongVal(snap,"totalTargetCoins")));
                    myTotalSending = sending; myTotalReceiving = receiving;
                    int wealth = LevelUtils.calculateWealthLevel(sending);
                    int charm = LevelUtils.calculateCharmLevel(receiving);
                    if (getLongVal(snap,"wealthLevel") > wealth) wealth = (int) getLongVal(snap,"wealthLevel");
                    if (getLongVal(snap,"charmLevel") > charm) charm = (int) getLongVal(snap,"charmLevel");
                    if (wealth < 1) wealth = 1; if (wealth > 150) wealth = 150;
                    if (charm < 1) charm = 1; if (charm > 150) charm = 150;
                    myWealthLevel = wealth; myCharmLevel = charm;
                    int vip = 0; try { String vipStr = getStr(snap, "vipLevel", "0"); vip = Integer.parseInt(vipStr); } catch (Exception ignored) {}
                    if (myVipLevel > vip) vip = myVipLevel;
                    myVipLevel = vip;
                    String role = getStr(snap, "role", "user").toLowerCase();
                    boolean isAgency = getBool(snap, "isAgency", false);
                    String agencyStatus = getStr(snap, "agencyStatus", "").toLowerCase();
                    String agencyName = getStr(snap, "agencyName", "SR Agency");
                    String agencyPercent = getStr(snap, "agencyPercent", "0");
                    if (agencyPercent.equals("0")) agencyPercent = getStr(snap, "agencyCommission", "0");
                    boolean isHost = getBool(snap, "isHost", false);
                    String hostStatus = getStr(snap, "hostStatus", "").toLowerCase();
                    String agencyIdForHost = getStr(snap, "hostAgencyId", "");
                    if (agencyIdForHost.isEmpty()) agencyIdForHost = getStr(snap, "agencyId", "");
                    String hostAgencyName = getStr(snap, "hostAgencyName", "");
                    boolean agencyActive = isAgency && !"blocked".equals(agencyStatus) && !"rejected".equals(agencyStatus);
                    boolean hostActive = isHost && !"blocked".equals(hostStatus) && !"rejected".equals(hostStatus);
                    if (!id.isEmpty()) { saveMyAnyId(id); }
                    String finalPhoto = photo;
                    if(finalPhoto==null || finalPhoto.isEmpty() || finalPhoto.equals("null")) finalPhoto = getCachedPhoto();
                    else savePhotoPermanent(finalPhoto);
                    boolean everAgency = prefs.getBoolean("AGENCY_EVER", false) || isAgency;
                    boolean finalIsAgency = isAgency || (everAgency && prefs.getBoolean("IS_AGENCY", false));
                    boolean finalAgencyActive = agencyActive || (everAgency && prefs.getBoolean("AGENCY_ACTIVE", false) && !agencyStatus.equals("blocked") && !agencyStatus.equals("rejected"));
                    prefs.edit().putString("USER_NAME", name).putString("USER_COUNTRY", country).putString("USER_GENDER", gender)
                        .putBoolean("RTDB_IS_AGENCY", isAgency).putBoolean("RTDB_IS_HOST", isHost)
                        .putBoolean("IS_AGENCY", finalIsAgency).putBoolean("AGENCY_ACTIVE", finalAgencyActive).putBoolean("AGENCY_EVER", everAgency)
                        .putString("AGENCY_NAME", agencyName).putString("AGENCY_PERCENT", agencyPercent)
                        .putBoolean("IS_HOST", isHost).putBoolean("HOST_ACTIVE", hostActive)
                        .putString("HOST_AGENCY_ID", agencyIdForHost).putString("HOST_AGENCY_NAME", hostAgencyName)
                        .putString("USER_ROLE", role).putInt("VIP_LEVEL", vip)
                        .putInt("WEALTH_LEVEL", wealth).putInt("CHARM_LEVEL", charm)
                        .putString("USER_LEVEL", String.valueOf(wealth))
                        .putLong("TOTAL_SENDING", sending).putLong("TOTAL_RECEIVING", receiving).apply();
                    if(finalPhoto!=null && !finalPhoto.isEmpty() && !finalPhoto.equals("null")) prefs.edit().putString("USER_PHOTO", finalPhoto).putString("PROFILE_PHOTO", finalPhoto).apply();
                    if (txtName != null) txtName.setText(name);
                    if (txtId != null) txtId.setText("ID: " + myAnyId);
                    if (txtGenderCountry != null) txtGenderCountry.setText(getGenderCountryText());
                    if (txtFollow != null) txtFollow.setText(followCount);
                    if (txtFans != null) txtFans.setText(fansCount);
                    if (txtVisitor != null) txtVisitor.setText(visitorCount);
                    if (txtWalletCoins != null) txtWalletCoins.setText("Coins: " + coins);
                    if(finalPhoto!=null && !finalPhoto.isEmpty()) showPhoto(finalPhoto, imgAvatar);
                    applyRoleVisibility(); updateVipUI(); updateLevelBadge();
                    boolean pending = getBool(snap, "pendingHostInvite", false);
                    String pendingAgency = getStr(snap, "pendingAgencyName", "");
                    String pendingAgencyId = getStr(snap, "pendingAgencyId", "");
                    if (pending && !inviteDialogShown && !isHost) { inviteDialogShown = true; showHostInviteDialog(pendingAgency, pendingAgencyId); }
                } catch (Exception ignored) {}
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
    private void showHostInviteDialog(String agencyName, String agencyId) {
        new AlertDialog.Builder(this).setTitle("🎤 Host Invite").setMessage((agencyName.isEmpty() ? "Agency" : agencyName) + " tomake Host hisabe invite koreche.\n\nAccept korle tomar Me page e Host Center chole asbe.")
            .setCancelable(false).setPositiveButton("✅ Accept", (d, w) -> acceptHostInvite(agencyId, agencyName)).setNegativeButton("❌ Reject", (d, w) -> rejectHostInvite()).show();
    }
    private void acceptHostInvite(String agencyId, String agencyName) {
        if (firebaseUid.isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(myAnyId.isEmpty() ? firebaseUid : myAnyId);
        ref.child("isHost").setValue(true); ref.child("hostStatus").setValue("active"); ref.child("role").setValue("host");
        ref.child("hostAgencyId").setValue(agencyId); ref.child("hostAgencyName").setValue(agencyName);
        ref.child("pendingHostInvite").setValue(false); ref.child("pendingAgencyId").setValue(null); ref.child("pendingAgencyName").setValue(null);
        FirebaseDatabase.getInstance().getReference("hosts").child(firebaseUid).child("ownerUid").setValue(firebaseUid);
        FirebaseDatabase.getInstance().getReference("hosts").child(firebaseUid).child("agencyId").setValue(agencyId);
        FirebaseDatabase.getInstance().getReference("hosts").child(firebaseUid).child("agencyName").setValue(agencyName);
        FirebaseDatabase.getInstance().getReference("hosts").child(firebaseUid).child("status").setValue("active");
        FirebaseDatabase.getInstance().getReference("hosts").child(firebaseUid).child("createdAt").setValue(System.currentTimeMillis());
        FirebaseDatabase.getInstance().getReference("hostInvites").orderByChild("hostUid").equalTo(firebaseUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot s) { for (DataSnapshot c : s.getChildren()) { if ("pending".equals(String.valueOf(c.child("status").getValue()))) c.getRef().child("status").setValue("accepted"); } }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
        Toast.makeText(this, "✅ Host Confirm hoye geche!", Toast.LENGTH_SHORT).show(); loadMeData();
    }
    private void rejectHostInvite() {
        if (firebaseUid.isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(myAnyId.isEmpty() ? firebaseUid : myAnyId);
        ref.child("pendingHostInvite").setValue(false); ref.child("pendingAgencyId").setValue(null); ref.child("pendingAgencyName").setValue(null);
        Toast.makeText(this, "Invite Reject kora hoyeche", Toast.LENGTH_SHORT).show();
    }
    private void showPhoto(String photo, ImageView iv) {
        if (iv == null || photo == null || photo.isEmpty() || photo.equals("null")) return;
        try { if (photo.startsWith("http")) Glide.with(this).load(photo).circleCrop().into(iv); else showBase64Image(photo, iv); } catch (Exception ignored) {} }
    private void showBase64Image(String base64, ImageView iv) {
        if (iv == null || base64 == null || base64.isEmpty()) return;
        try { byte[] bytes = Base64.decode(base64, Base64.DEFAULT); BitmapFactory.Options opt = new BitmapFactory.Options(); opt.inSampleSize = 2; Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opt); if (b != null) Glide.with(this).load(b).circleCrop().into(iv); } catch (Exception ignored) {} }
    private String getStr(DataSnapshot s, String k, String def) { if (s.hasChild(k) && s.child(k).getValue() != null) return String.valueOf(s.child(k).getValue()); return def; }
    private boolean getBool(DataSnapshot s, String k, boolean def) { if (s.hasChild(k) && s.child(k).getValue() != null) { Object o = s.child(k).getValue(); if (o instanceof Boolean) return (Boolean) o; return "true".equalsIgnoreCase(String.valueOf(o)); } return def; }
}
