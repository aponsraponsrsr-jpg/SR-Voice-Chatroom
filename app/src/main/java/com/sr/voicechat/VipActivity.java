package com.sr.voicechat;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VipActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private String myOriginalId = ""; // ORIGINAL PROFILE ID - NEVER CHANGE
    private String myDisplayId = "";
    private int myVipLevel = 0;
    private long myCoins = 0;
    private long myVipExpire = 0;
    private TextView txtCurrent;

    // ===== SETTINGS - PORIBORTON KORTE CHAILE EKHANEI KORBA =====
    private static final boolean BUY_ENABLED = false; // false = Can't Buy / Contact Official, true = Buy chalu hobe
    private static final long VIP_VALIDITY_DAYS = 30L; // 30 Din - chaile 20 korte parba

    // ===== VIP 1-8 Auto Threshold - Sending + Receiving - FINAL =====
    private static final long[] AUTO_VIP_THRESHOLD = {
        0,
        2000000L,   // VIP 1 = 2M
        4000000L,   // VIP 2 = 4M
        7500000L,   // VIP 3 = 7.5M
        15000000L,  // VIP 4 = 15M
        22000000L,  // VIP 5 = 22M
        28000000L,  // VIP 6 = 28M
        35000000L,  // VIP 7 = 35M
        45000000L   // VIP 8 = 45M - chaile 40000000L koro
    };

    // ===== DEMO HUBOHU - VIP 1-8 =====
    private String[] vipPriceDollar = {"", "$15/30 days", "$50/30 days", "$99.99/30 days", "$300/30 days", "$700/30 days", "$1500/30 days", "$3000/30 days", "$5000/30 days"};
    private String[] vipBonus = {"",
        "VIP 1 can get a total of extra 972000 coins by signing in for 30 days",
        "VIP 2 can get a total of extra 3240000 coins by signing in for 30 days. Enjoy 3x Exp user experience acceleration every day.",
        "VIP 3 can get a total of extra 6480000 coins by signing in for 30 days. Enjoy 10x Exp user experience acceleration every day.",
        "VIP 4 can get a total of extra 20250000 coins by signing in for 30 days. Enjoy 10x Exp user experience acceleration every day.",
        "VIP 5 can get a total of extra 40500000 coins by signing in for 30 days. Enjoy 15x Exp user experience acceleration every day.",
        "VIP 6 can get a total of extra 101250000 coins by signing in for 30 days. Enjoy 15x Exp user experience acceleration every day.",
        "VIP 7 can get a total of extra 202500000 coins by signing in for 30 days. Enjoy 20x Exp user experience acceleration every day.",
        "VIP 8 can get a total of extra 324000000 coins by signing in for 30 days. Enjoy 20x Exp user experience acceleration every day."
    };
    private String[] vipEmoji = {"", "🦄", "🐺", "🦁", "🐺", "🦁", "🦁", "🐲", "🔥"};
    private String[] vipName = {"", "VIP 1 - Ice Horse", "VIP 2 - Golden Wolf", "VIP 3 - Green Lion", "VIP 4 - Purple Wolf King", "VIP 5 - Blue Gold Lion", "VIP 6 - Pink Gold Lion", "VIP 7 - Red Gold Dragon Lion", "VIP 8 - Golden Fire Phoenix"};
    private String[] vipBgColor = {"", "#0A2A3A", "#3A2A00", "#0A3A1A", "#2A0A4A", "#0A2A5A", "#4A0A3A", "#5A0A0A", "#5A3A00"};
    private String[] vipAccent = {"", "#00E5FF", "#FFD700", "#00FF88", "#B388FF", "#448AFF", "#FF4081", "#FF1744", "#FFC400"};

    // ===== NOBLE IDENTIFICATION - 14 ITEMS - DEMO ORDER =====
    private String[] nobleItems = {
        "VIP title",
        "Exclusive avatar\nframe",
        "Chat bubble",
        "Awesome\nentrance show",
        "Microphone\neffect",
        "Special Gift\nTassel",
        "Profile card\nbackground",
        "Rare cars",
        "nickname\nstreamer",
        "Special gift\nfloating screen",
        "Page skin",
        "All server\nannouncement",
        "Regional\nfloating screen",
        "Room cover\nframe"
    };
    private String[] nobleDesc = {
        "The VIP title will be displayed on the public screen and information card in the room. The higher the VIP level, the more distinguished the status.",
        "VIP users have exclusive avatar frames. The higher the VIP level, the cooler the effect.",
        "VIP has chat bubble decorations, and has eye-catching bubbles when speaking in rooms and private messages.",
        "VIP has an awesome cool entrance show effect, which is triggered when entering the room and attracts the attention of the audience.",
        "VIP has exclusive microphone effects. The higher the VIP level, the cooler the effect.",
        "VIPs have special tassels when giving gifts. Others can notice the gift you give at a glance.",
        "VIP has a Distinguished Card, and the effect is displayed on the information card in the room. The higher the VIP level, the cooler the effect.",
        "VIP owns a rare car, which triggers effects when entering the room. It is full of domineering and stunning the audience.",
        "VIP's nickname will have a streamer effect in the room, personal homepage, private message list, etc., gaining a noble status and distinguishing it from others!",
        "When VIP sends a VIP-level gift, the special gift floating screen will be triggered.",
        "VIP's personal homepage will have a unique skin, and anyone who enters the homepage will immediately feel your noble!",
        "After you obtain VIP 7 for more than 7 days at a time, a floating screen announcement will be opened and be seen by all users.",
        "Regional floating screen - your gift / entry will show in region.",
        "Room cover frame - your room will have premium cover frame. Full noble identity."
    };
    // Kotota Open Thakbe - VIP1=3, VIP2=4, VIP3=5, VIP4=6, VIP5=7, VIP6=8, VIP7=12, VIP8=14
    private int[] unlockCount = {0, 3, 4, 5, 6, 7, 8, 12, 14};

    private int selectedVip = 1;
    private LinearLayout tabLayout;
    private TextView emblemTv, emblemNameTv, nobleTitleTv, priceTv, bonusTv;
    private GridLayout nobleGrid;
    private LinearLayout mainBg;
    private ObjectAnimator emblemAnim;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        // ===== ORIGINAL ID LOCK - SHORT ID CHANGE KORLEO VIP JABE NA =====
        myOriginalId = prefs.getString("ORIGINAL_PROFILE_ID", "");
        if (myOriginalId.isEmpty()) myOriginalId = prefs.getString("MY_PROFILE_ID", "");
        if (myOriginalId.isEmpty()) myOriginalId = prefs.getString("PROFILE_ID", "");
        if (myOriginalId.isEmpty()) myOriginalId = getIntent().getStringExtra("PROFILE_ID");
        if (myOriginalId == null) myOriginalId = "";
        if (!myOriginalId.isEmpty()) prefs.edit().putString("ORIGINAL_PROFILE_ID", myOriginalId).apply();

        myDisplayId = prefs.getString("DISPLAY_ID", myOriginalId);
        if (myDisplayId.isEmpty()) myDisplayId = prefs.getString("SHORT_ID", myOriginalId);
        if (myDisplayId.isEmpty()) myDisplayId = myOriginalId;

        myVipLevel = prefs.getInt("VIP_LEVEL", 0);
        if (getIntent() != null && getIntent().hasExtra("CURRENT_VIP")) myVipLevel = getIntent().getIntExtra("CURRENT_VIP", myVipLevel);
        selectedVip = myVipLevel >= 1 ? myVipLevel : 1;

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        mainBg = new LinearLayout(this);
        mainBg.setOrientation(LinearLayout.VERTICAL);
        mainBg.setPadding(24, 24, 24, 30);
        scroll.addView(mainBg);
        setContentView(scroll);

        TextView title = new TextView(this);
        title.setText("VIP");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 10, 0, 6);
        mainBg.addView(title);

        TextView sub = new TextView(this);
        sub.setText("ID: " + myDisplayId + "  •  VIP with ORIGINAL ID - Short ID change korleo VIP thakbe");
        sub.setTextColor(Color.parseColor("#B0BEC5"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 12);
        mainBg.addView(sub);

        txtCurrent = new TextView(this);
        txtCurrent.setText("My VIP: " + (myVipLevel >= 1 ? vipEmoji[myVipLevel] + " " + vipName[myVipLevel] : "No VIP Yet"));
        txtCurrent.setTextColor(Color.WHITE);
        txtCurrent.setTextSize(15);
        txtCurrent.setTypeface(null, Typeface.BOLD);
        txtCurrent.setGravity(Gravity.CENTER);
        txtCurrent.setPadding(20, 20, 20, 20);
        GradientDrawable curBg = new GradientDrawable();
        curBg.setColors(new int[]{Color.parseColor("#FFD700"), Color.parseColor("#FF6D00")});
        curBg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        curBg.setCornerRadius(24);
        txtCurrent.setBackground(curBg);
        mainBg.addView(txtCurrent);

        TextView autoTxt = new TextView(this);
        autoTxt.setText("Sending + Receiving = Auto VIP for 30 Days\nVIP1=2M • VIP2=4M • VIP3=7.5M • VIP4=15M • VIP5=22M • VIP6=28M • VIP7=35M • VIP8=45M");
        autoTxt.setTextColor(Color.parseColor("#FFD700"));
        autoTxt.setTextSize(12);
        autoTxt.setTypeface(null, Typeface.BOLD);
        autoTxt.setGravity(Gravity.CENTER);
        autoTxt.setPadding(0, 12, 0, 8);
        mainBg.addView(autoTxt);

        // ===== TOP SERIAL - VIP 1-8 - Horizontal Scroll =====
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        tabLayout = new LinearLayout(this);
        tabLayout.setOrientation(LinearLayout.HORIZONTAL);
        tabLayout.setGravity(Gravity.CENTER);
        hsv.addView(tabLayout);
        mainBg.addView(hsv);

        for (int i = 1; i <= 8; i++) {
            final int lvl = i;
            Button b = new Button(this);
            b.setText("VIP " + i);
            b.setTextSize(15);
            b.setTypeface(null, Typeface.BOLD);
            b.setTextColor(Color.WHITE);
            b.setAllCaps(false);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(6, 6, 6, 6);
            b.setLayoutParams(lp);
            b.setOnClickListener(v -> { selectedVip = lvl; refreshUI(); });
            tabLayout.addView(b);
        }

        // ===== EMBLEM - BIG - PREMIUM - CHOKCHOK / NORA =====
        emblemTv = new TextView(this);
        emblemTv.setTextSize(84);
        emblemTv.setGravity(Gravity.CENTER);
        emblemTv.setPadding(0, 26, 0, 0);
        mainBg.addView(emblemTv);

        emblemNameTv = new TextView(this);
        emblemNameTv.setTextSize(19);
        emblemNameTv.setTypeface(null, Typeface.BOLD);
        emblemNameTv.setTextColor(Color.WHITE);
        emblemNameTv.setGravity(Gravity.CENTER);
        emblemNameTv.setPadding(0, 6, 0, 0);
        mainBg.addView(emblemNameTv);

        nobleTitleTv = new TextView(this);
        nobleTitleTv.setTextSize(17);
        nobleTitleTv.setTypeface(null, Typeface.BOLD);
        nobleTitleTv.setTextColor(Color.parseColor("#FFD700"));
        nobleTitleTv.setGravity(Gravity.CENTER);
        nobleTitleTv.setPadding(0, 24, 0, 12);
        mainBg.addView(nobleTitleTv);

        nobleGrid = new GridLayout(this);
        nobleGrid.setColumnCount(3);
        nobleGrid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        mainBg.addView(nobleGrid);

        bonusTv = new TextView(this);
        bonusTv.setTextColor(Color.parseColor("#FFD700"));
        bonusTv.setTextSize(13);
        bonusTv.setGravity(Gravity.CENTER);
        bonusTv.setPadding(0, 20, 0, 8);
        mainBg.addView(bonusTv);

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER);
        bottom.setPadding(0, 14, 0, 10);

        priceTv = new TextView(this);
        priceTv.setTextSize(18);
        priceTv.setTypeface(null, Typeface.BOLD);
        priceTv.setTextColor(Color.WHITE);
        priceTv.setPadding(0, 0, 30, 0);
        bottom.addView(priceTv);

        Button cantBuy = new Button(this);
        cantBuy.setText("Can't buy");
        cantBuy.setTextColor(Color.WHITE);
        cantBuy.setAllCaps(false);
        cantBuy.setBackgroundColor(Color.GRAY);
        cantBuy.setOnClickListener(v -> showContactOfficial());
        bottom.addView(cantBuy);
        mainBg.addView(bottom);

        TextView hideNote = new TextView(this);
        hideNote.setText("Exclusive Privileges - Coming Soon");
        hideNote.setTextColor(Color.parseColor("#666666"));
        hideNote.setTextSize(11);
        hideNote.setGravity(Gravity.CENTER);
        hideNote.setPadding(0, 8, 0, 0);
        hideNote.setVisibility(View.GONE);
        mainBg.addView(hideNote);

        refreshUI();
        loadMyData();
    }

    private void refreshUI() {
        for (int i = 0; i < tabLayout.getChildCount(); i++) {
            Button b = (Button) tabLayout.getChildAt(i);
            int lvl = i + 1;
            if (lvl == selectedVip) {
                b.setTextColor(Color.BLACK);
                GradientDrawable d = new GradientDrawable();
                d.setColors(new int[]{Color.parseColor("#FFD700"), Color.parseColor("#FF6D00")});
                d.setCornerRadius(30);
                b.setBackground(d);
            } else {
                b.setTextColor(Color.WHITE);
                GradientDrawable d = new GradientDrawable();
                d.setColor(Color.parseColor("#222222"));
                d.setCornerRadius(30);
                b.setBackground(d);
            }
        }

        mainBg.setBackgroundColor(Color.parseColor(vipBgColor[selectedVip]));
        emblemTv.setText(vipEmoji[selectedVip]);
        emblemNameTv.setText(vipName[selectedVip]);
        nobleTitleTv.setText("Noble identification\n" + unlockCount[selectedVip] + "/14");
        priceTv.setText(vipPriceDollar[selectedVip]);
        bonusTv.setText(vipBonus[selectedVip]);

        if (emblemAnim != null) emblemAnim.cancel();
        emblemAnim = ObjectAnimator.ofFloat(emblemTv, "scaleX", 1f, 1.08f, 1f);
        emblemAnim.setDuration(1200);
        emblemAnim.setRepeatCount(ObjectAnimator.INFINITE);
        emblemAnim.start();

        nobleGrid.removeAllViews();
        for (int i = 0; i < nobleItems.length; i++) {
            boolean unlocked = i < unlockCount[selectedVip];
            TextView item = new TextView(this);
            item.setText((unlocked ? "✨\n" : "🔒\n") + nobleItems[i]);
            item.setTextSize(11);
            item.setTypeface(null, Typeface.BOLD);
            item.setGravity(Gravity.CENTER);
            item.setPadding(10, 18, 10, 18);
            if (unlocked) {
                item.setTextColor(Color.WHITE);
                GradientDrawable bg = new GradientDrawable();
                bg.setColors(new int[]{Color.parseColor("#3A0A4A"), Color.parseColor("#1A0B2E")});
                bg.setOrientation(GradientDrawable.Orientation.TL_BR);
                bg.setCornerRadius(18);
                bg.setStroke(2, Color.parseColor(vipAccent[selectedVip]));
                item.setBackground(bg);
            } else {
                item.setTextColor(Color.parseColor("#777777"));
                GradientDrawable bg = new GradientDrawable();
                bg.setColor(Color.parseColor("#111111"));
                bg.setCornerRadius(18);
                bg.setStroke(1, Color.parseColor("#333333"));
                item.setBackground(bg);
            }
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = 0;
            p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            p.setMargins(8, 8, 8, 8);
            item.setLayoutParams(p);
            final int idx = i;
            final boolean un = unlocked;
            item.setOnClickListener(v -> {
                if (!un) {
                    new AlertDialog.Builder(VipActivity.this)
                        .setTitle("🔒 Locked")
                        .setMessage(nobleItems[idx] + "\n\nThis privilege is locked.\nIt will open when you reach higher VIP.\n\n" + vipName[selectedVip] + " has " + unlockCount[selectedVip] + "/14 unlocked.")
                        .setPositiveButton("OK", null).show();
                } else {
                    new AlertDialog.Builder(VipActivity.this)
                        .setTitle("✨ " + nobleItems[idx].replace("\n", " "))
                        .setMessage(nobleDesc[idx])
                        .setPositiveButton("OK", null).show();
                }
            });
            nobleGrid.addView(item);
        }
    }

    private void showContactOfficial() {
        if (BUY_ENABLED) {
            Toast.makeText(this, "Buy system future e chalu hobe", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("Contact Official")
            .setMessage("You cannot buy VIP with Dollar / Coins.\n\nVIP is only given by Official / Company.\n\nSending + Receiving:\nVIP1=2M • VIP2=4M • VIP3=7.5M • VIP4=15M\nVIP5=22M • VIP6=28M • VIP7=35M • VIP8=45M\n\nReach target, you will get VIP automatically for 30 Days from Firebase.\n\nPlease Contact Official.")
            .setPositiveButton("OK", null)
            .show();
    }

    private void loadMyData() {
        if (myOriginalId.isEmpty()) return;
        FirebaseDatabase.getInstance().getReference("users").child(myOriginalId)
            .addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    if (!snap.exists()) return;
                    try { myCoins = Long.parseLong(String.valueOf(snap.child("coins").getValue() == null ? "0" : snap.child("coins").getValue())); } catch (Exception e) { myCoins = 0; }
                    int v = 0;
                    try { v = Integer.parseInt(String.valueOf(snap.child("vipLevel").getValue() == null ? "0" : snap.child("vipLevel").getValue())); } catch (Exception ignored) {}
                    long exp = 0;
                    try { exp = Long.parseLong(String.valueOf(snap.child("vipExpireAt").getValue() == null ? "0" : snap.child("vipExpireAt").getValue())); } catch (Exception ignored) {}
                    if (exp > 0 && System.currentTimeMillis() > exp) v = 0;
                    if (v != myVipLevel) {
                        myVipLevel = v;
                        prefs.edit().putInt("VIP_LEVEL", v).apply();
                        selectedVip = myVipLevel >= 1 ? myVipLevel : selectedVip;
                        if (txtCurrent != null) txtCurrent.setText("My VIP: " + (myVipLevel >= 1 ? vipEmoji[myVipLevel] + " " + vipName[myVipLevel] : "No VIP Yet"));
                        refreshUI();
                    }
                    myVipExpire = exp;
                    checkAutoVip(snap);
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
    }

    private void checkAutoVip(DataSnapshot snap) {
        try {
            long sent = 0, recv = 0;
            if (snap.child("totalSent").exists()) sent = Long.parseLong(String.valueOf(snap.child("totalSent").getValue()));
            if (snap.child("totalReceived").exists()) recv = Long.parseLong(String.valueOf(snap.child("totalReceived").getValue()));
            if (sent == 0 && snap.child("totalSending").exists()) sent = Long.parseLong(String.valueOf(snap.child("totalSending").getValue()));
            if (recv == 0 && snap.child("totalReceiving").exists()) recv = Long.parseLong(String.valueOf(snap.child("totalReceiving").getValue()));
            long total = sent + recv;

            // Serial by Serial - Koto Level Pabe
            int autoLevel = 0;
            for (int i = 8; i >= 1; i--) {
                if (total >= AUTO_VIP_THRESHOLD[i]) { autoLevel = i; break; }
            }

            // Jodi notun Level boro hoy + VIP nai / Expire hoye gese - tahole Auto VIP dibo
            if (autoLevel > 0 && autoLevel > myVipLevel) {
                long expire = System.currentTimeMillis() + (VIP_VALIDITY_DAYS * 24 * 60 * 60 * 1000);
                FirebaseDatabase.getInstance().getReference("users").child(myOriginalId).child("vipLevel").setValue(autoLevel);
                FirebaseDatabase.getInstance().getReference("users").child(myOriginalId).child("vipExpireAt").setValue(expire);
                FirebaseDatabase.getInstance().getReference("vip_assignments").child(myOriginalId).child("vipLevel").setValue(autoLevel);
                FirebaseDatabase.getInstance().getReference("vip_assignments").child(myOriginalId).child("expireAt").setValue(expire);
                FirebaseDatabase.getInstance().getReference("vip_assignments").child(myOriginalId).child("originalProfileId").setValue(myOriginalId);
                FirebaseDatabase.getInstance().getReference("vip_assignments").child(myOriginalId).child("displayId").setValue(myDisplayId);
                FirebaseDatabase.getInstance().getReference("vip_assignments").child(myOriginalId).child("autoBy").setValue("Sending+Receiving " + total);
                Toast.makeText(this, "🎉 Auto VIP " + autoLevel + " Active for 30 Days!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception ignored) {}
    }
}
