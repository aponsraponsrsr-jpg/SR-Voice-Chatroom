package com.sr.voicechat;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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

    // VIP 1-8 : Price in Coins - 30 Days
    private String[] vipEmoji = {"", "🐅", "🐯", "🦅", "🔥", "🐉", "👑", "🐲", "💜"};
    private String[] vipName = {"", "VIP 1 - Tiger", "VIP 2 - Tiger King", "VIP 3 - Eagle", "VIP 4 - Fire", "VIP 5 - Dragon", "VIP 6 - Crown King", "VIP 7 - Super Dragon", "VIP 8 - Royal Purple"};
    private long[] vipPrice = {0, 10000, 25000, 50000, 100000, 200000, 350000, 500000, 1000000};
    private String[] vipBenefits = {"",
        "• Special Tiger Badge\n• Room Entry Effect\n• VIP Chat Color",
        "• All VIP 1 Benefits\n• Tiger King Frame\n• VIP Gift Discount",
        "• All VIP 2 Benefits\n• Eagle Entry Effect\n• Profile Glow",
        "• All VIP 3 Benefits\n• Fire Entry Effect\n• VIP Room Priority",
        "• All VIP 4 Benefits\n• Dragon Entry Effect\n• Special Frame",
        "• All VIP 5 Benefits\n• Crown Entry Effect\n• Top List Priority",
        "• All VIP 6 Benefits\n• Super Dragon Effect\n• Exclusive Gifts",
        "• All VIP 7 Benefits\n• Royal Purple Frame\n• Max Honor in App"};

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        // ===== ORIGINAL ID LOCK - SHORT ID CHANGE KORLEO VIP JABE NA =====
        // 1. First try ORIGINAL_PROFILE_ID - jeta first login e paisilo
        myOriginalId = prefs.getString("ORIGINAL_PROFILE_ID", "");
        // 2. Na thakle MY_PROFILE_ID use koro + ORIGINAL hisabe save koro
        if (myOriginalId.isEmpty()) myOriginalId = prefs.getString("MY_PROFILE_ID", "");
        if (myOriginalId.isEmpty()) myOriginalId = prefs.getString("PROFILE_ID", "");
        if (myOriginalId.isEmpty()) myOriginalId = getIntent().getStringExtra("PROFILE_ID");
        if (myOriginalId == null) myOriginalId = "";
        // 3. Lock it - pore ar change hobe na
        if (!myOriginalId.isEmpty()) prefs.edit().putString("ORIGINAL_PROFILE_ID", myOriginalId).apply();

        myDisplayId = prefs.getString("DISPLAY_ID", myOriginalId);
        if (myDisplayId.isEmpty()) myDisplayId = prefs.getString("SHORT_ID", myOriginalId);
        if (myDisplayId.isEmpty()) myDisplayId = myOriginalId;

        myVipLevel = prefs.getInt("VIP_LEVEL", 0);
        if (getIntent() != null && getIntent().hasExtra("CURRENT_VIP")) myVipLevel = getIntent().getIntExtra("CURRENT_VIP", myVipLevel);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 20, 20, 30);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("👑 PREMIUM VIP CENTER");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 10, 0, 6);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("ID: " + myDisplayId + "  •  VIP with ORIGINAL ID - Short ID change korleo VIP thakbe");
        sub.setTextColor(Color.parseColor("#B0BEC5"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 0, 0, 16);
        root.addView(sub);

        txtCurrent = new TextView(this);
        txtCurrent.setText("My VIP: " + (myVipLevel >= 1 ? vipEmoji[myVipLevel] + " " + vipName[myVipLevel] : "No VIP Yet"));
        txtCurrent.setTextColor(Color.WHITE);
        txtCurrent.setTextSize(16);
        txtCurrent.setTypeface(null, Typeface.BOLD);
        txtCurrent.setGravity(Gravity.CENTER);
        txtCurrent.setPadding(20, 20, 20, 20);
        GradientDrawable curBg = new GradientDrawable();
        curBg.setColors(new int[]{Color.parseColor("#FFD700"), Color.parseColor("#FF6D00")});
        curBg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        curBg.setCornerRadius(24);
        txtCurrent.setBackground(curBg);
        root.addView(txtCurrent);

        TextView coinTxt = new TextView(this);
        coinTxt.setText("My Coins: Loading...");
        coinTxt.setTextColor(Color.parseColor("#FFD700"));
        coinTxt.setTextSize(14);
        coinTxt.setTypeface(null, Typeface.BOLD);
        coinTxt.setGravity(Gravity.CENTER);
        coinTxt.setPadding(0, 14, 0, 14);
        root.addView(coinTxt);

        // VIP 1-8 List
        for (int i = 1; i <= 8; i++) {
            final int lvl = i;
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(28, 24, 28, 24);
            GradientDrawable bg = new GradientDrawable();
            if (lvl == myVipLevel) bg.setColors(new int[]{Color.parseColor("#00C853"), Color.parseColor("#00796B")});
            else bg.setColors(new int[]{Color.parseColor("#3A0A4A"), Color.parseColor("#1A0B2E")});
            bg.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
            bg.setCornerRadius(24);
            card.setBackground(bg);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 14);
            card.setLayoutParams(lp);
            card.setOnClickListener(v -> showVipPopup(lvl));
            root.addView(card);

            TextView t1 = new TextView(this);
            t1.setText(vipEmoji[lvl] + " " + vipName[lvl] + (lvl == myVipLevel ? "  ✅ ACTIVE" : ""));
            t1.setTextColor(Color.WHITE);
            t1.setTextSize(17);
            t1.setTypeface(null, Typeface.BOLD);
            card.addView(t1);

            TextView t2 = new TextView(this);
            t2.setText("Price: " + vipPrice[lvl] + " Coins  •  30 Days  •  Tap to View >");
            t2.setTextColor(Color.parseColor("#FFD700"));
            t2.setTextSize(13);
            t2.setTypeface(null, Typeface.BOLD);
            card.addView(t2);
        }

        TextView note = new TextView(this);
        note.setText("VIP kinle 30 Din thakbe\nCoin diye kinte parba\nShort ID change korleo VIP jabe na - karon VIP ORIGINAL ID diye save hoy");
        note.setTextColor(Color.parseColor("#B0BEC5"));
        note.setTextSize(12);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, 10, 0, 0);
        root.addView(note);

        setContentView(scroll);
        loadMyData(coinTxt);
    }

    private void loadMyData(TextView coinTxt) {
        if (myOriginalId.isEmpty()) return;
        FirebaseDatabase.getInstance().getReference("users").child(myOriginalId)
            .addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    if (!snap.exists()) return;
                    try { myCoins = Long.parseLong(String.valueOf(snap.child("coins").getValue() == null ? "0" : snap.child("coins").getValue())); } catch (Exception e) { myCoins = 0; }
                    if (coinTxt != null) coinTxt.setText("My Coins: " + myCoins);
                    int v = 0;
                    try { v = Integer.parseInt(String.valueOf(snap.child("vipLevel").getValue() == null ? "0" : snap.child("vipLevel").getValue())); } catch (Exception ignored) {}
                    long exp = 0;
                    try { exp = Long.parseLong(String.valueOf(snap.child("vipExpireAt").getValue() == null ? "0" : snap.child("vipExpireAt").getValue())); } catch (Exception ignored) {}
                    if (exp > 0 && System.currentTimeMillis() > exp) v = 0;
                    if (v != myVipLevel) {
                        myVipLevel = v;
                        prefs.edit().putInt("VIP_LEVEL", v).apply();
                        if (txtCurrent != null) txtCurrent.setText("My VIP: " + (myVipLevel >= 1 ? vipEmoji[myVipLevel] + " " + vipName[myVipLevel] : "No VIP Yet"));
                    }
                    myVipExpire = exp;
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
    }

    // ===== DEMO ER MOTO POPUP =====
    private void showVipPopup(int lvl) {
        new AlertDialog.Builder(this)
            .setTitle(vipEmoji[lvl] + " " + vipName[lvl])
            .setMessage(vipBenefits[lvl] + "\n\nPrice: " + vipPrice[lvl] + " Coins\nValidity: 30 Days\n\nMy Coins: " + myCoins + "\nMy VIP: " + (myVipLevel >= 1 ? vipName[myVipLevel] : "None"))
            .setPositiveButton(lvl == myVipLevel ? "✅ Already Active" : "💰 Buy Now", (d, w) -> {
                if (lvl == myVipLevel) return;
                buyVip(lvl);
            })
            .setNegativeButton("Close", null)
            .show();
    }

    private void buyVip(int lvl) {
        if (myOriginalId.isEmpty()) { Toast.makeText(this, "ID pawa jay nai", Toast.LENGTH_SHORT).show(); return; }
        if (myCoins < vipPrice[lvl]) { Toast.makeText(this, "Coin kom ache! Need " + vipPrice[lvl] + " Coins", Toast.LENGTH_SHORT).show(); return; }
        new AlertDialog.Builder(this)
            .setTitle("Confirm Buy?")
            .setMessage(vipEmoji[lvl] + " " + vipName[lvl] + "\nPrice: " + vipPrice[lvl] + " Coins\n30 Days")
            .setPositiveButton("✅ Yes Buy", (d, w) -> {
                long newCoins = myCoins - vipPrice[lvl];
                long expire = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000);
                // SAVE WITH ORIGINAL ID ONLY - SHORT ID CHANGE KORLEO THAKBE
                FirebaseDatabase.getInstance().getReference("users").child(myOriginalId).child("coins").setValue(newCoins);
                FirebaseDatabase.getInstance().getReference("users").child(myOriginalId).child("vipLevel").setValue(lvl);
                FirebaseDatabase.getInstance().getReference("users").child(myOriginalId).child("vipExpireAt").setValue(expire);
                FirebaseDatabase.getInstance().getReference("vip_assignments").child(myOriginalId).child("vipLevel").setValue(lvl);
                FirebaseDatabase.getInstance().getReference("vip_assignments").child(myOriginalId).child("expireAt").setValue(expire);
                FirebaseDatabase.getInstance().getReference("vip_assignments").child(myOriginalId).child("originalProfileId").setValue(myOriginalId);
                FirebaseDatabase.getInstance().getReference("vip_assignments").child(myOriginalId).child("displayId").setValue(myDisplayId);
                myVipLevel = lvl;
                prefs.edit().putInt("VIP_LEVEL", lvl).apply();
                Toast.makeText(this, "🎉 " + vipName[lvl] + " Active!", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
