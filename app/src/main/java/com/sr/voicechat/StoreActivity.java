package com.sr.voicechat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StoreActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private String my9DigitId = "";
    private int myVipLevel = 0;
    private LinearLayout shopBox, mineBox;
    private TextView tabMine, tabShop;
    private boolean isMine = false;
    private String usingFrame = "";
    private String usingEntry = "";
    private String usingBubble = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        my9DigitId = prefs.getString("MY_9_DIGIT_ID", "");
        myVipLevel = prefs.getInt("VIP_LEVEL", 0);
        usingFrame = prefs.getString("USING_FRAME", "");
        usingEntry = prefs.getString("USING_ENTRY", "");
        usingBubble = prefs.getString("USING_BUBBLE", "");

        tabShop = findViewById(R.id.tabShop);
        tabMine = findViewById(R.id.tabMine);
        shopBox = findViewById(R.id.shopBox);
        mineBox = findViewById(R.id.mineBox);

        tabShop.setOnClickListener(v -> showShop());
        tabMine.setOnClickListener(v -> showMine());
        listenVip();
    }

    private void showShop() {
        isMine = false;
        shopBox.setVisibility(View.VISIBLE);
        mineBox.setVisibility(View.GONE);
        tabShop.setTextColor(0xFFFFD700);
        tabMine.setTextColor(0xFFFFFFFF);
    }

    private void showMine() {
        isMine = true;
        shopBox.setVisibility(View.GONE);
        mineBox.setVisibility(View.VISIBLE);
        tabShop.setTextColor(0xFFFFFFFF);
        tabMine.setTextColor(0xFFFFD700);
        buildMine();
    }

    private void listenVip() {
        if (my9DigitId.isEmpty()) return;
        FirebaseDatabase.getInstance().getReference("vip_assignments").child(my9DigitId)
            .addValueEventListener(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    if (!snap.exists()) return;
                    try {
                        int v = Integer.parseInt(String.valueOf(snap.child("vipLevel").getValue()));
                        long expire = 0;
                        try { expire = Long.parseLong(String.valueOf(snap.child("expireAt").getValue())); } catch (Exception ignored) {}
                        if (expire > 0 && System.currentTimeMillis() > expire) v = 0;
                        myVipLevel = v;
                        prefs.edit().putInt("VIP_LEVEL", v).apply();
                        if (isMine) buildMine();
                    } catch (Exception ignored) {}
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
    }

    private void buildMine() {
        mineBox.removeAllViews();
        if (myVipLevel <= 0) {
            TextView empty = new TextView(this);
            empty.setText("Mine is empty.\n\nTumi ekhono kono VIP pao nai.");
            empty.setTextColor(0xFFB0BEC5);
            empty.setTextSize(14);
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setPadding(20, 60, 20, 20);
            mineBox.addView(empty);
            return;
        }
        String vipName = getVipName(myVipLevel);
        String vipEmoji = getVipEmoji(myVipLevel);
        addSectionTitle("🏅 VIP Badge - Auto Applied");
        addMineCard(vipEmoji + " VIP " + myVipLevel + " • " + vipName + " Badge", "✅ Auto - Profile e lege geche", true, null);
        addSectionTitle("🖼️ Head Frame");
        String frameId = "VIP" + myVipLevel + "_FRAME";
        boolean frameUsing = frameId.equals(usingFrame);
        addMineCard(vipEmoji + " " + vipName + " Frame", frameUsing ? "USING" : "USE", frameUsing, () -> {
            if (frameUsing) { usingFrame = ""; prefs.edit().putString("USING_FRAME", "").apply(); Toast.makeText(this, "Frame Remove hoye geche", Toast.LENGTH_SHORT).show(); }
            else { usingFrame = frameId; prefs.edit().putString("USING_FRAME", frameId).apply(); Toast.makeText(this, "Frame USE hoye geche", Toast.LENGTH_SHORT).show(); }
            saveUsingToFirebase(); buildMine();
        });
        addSectionTitle("🚪 Entry Effect");
        String entryId = "VIP" + myVipLevel + "_ENTRY";
        boolean entryUsing = entryId.equals(usingEntry);
        addMineCard(vipEmoji + " " + vipName + " Entry", entryUsing ? "USING" : "USE", entryUsing, () -> {
            if (entryUsing) { usingEntry = ""; prefs.edit().putString("USING_ENTRY", "").apply(); Toast.makeText(this, "Entry Remove hoye geche", Toast.LENGTH_SHORT).show(); }
            else { usingEntry = entryId; prefs.edit().putString("USING_ENTRY", entryId).apply(); Toast.makeText(this, "Entry USE hoye geche", Toast.LENGTH_SHORT).show(); }
            saveUsingToFirebase(); buildMine();
        });
        addSectionTitle("💬 Chat Bubble");
        String bubbleId = "VIP" + myVipLevel + "_BUBBLE";
        boolean bubbleUsing = bubbleId.equals(usingBubble);
        addMineCard(vipEmoji + " " + vipName + " Bubble", bubbleUsing ? "USING" : "USE", bubbleUsing, () -> {
            if (bubbleUsing) { usingBubble = ""; prefs.edit().putString("USING_BUBBLE", "").apply(); Toast.makeText(this, "Bubble Remove hoye geche", Toast.LENGTH_SHORT).show(); }
            else { usingBubble = bubbleId; prefs.edit().putString("USING_BUBBLE", bubbleId).apply(); Toast.makeText(this, "Bubble USE hoye geche", Toast.LENGTH_SHORT).show(); }
            saveUsingToFirebase(); buildMine();
        });
    }

    private void saveUsingToFirebase() {
        if (my9DigitId.isEmpty()) return;
        try {
            FirebaseDatabase.getInstance().getReference("users").child(my9DigitId).child("usingFrame").setValue(usingFrame);
            FirebaseDatabase.getInstance().getReference("users").child(my9DigitId).child("usingEntry").setValue(usingEntry);
            FirebaseDatabase.getInstance().getReference("users").child(my9DigitId).child("usingBubble").setValue(usingBubble);
        } catch (Exception ignored) {}
    }

    private void addSectionTitle(String t) {
        TextView tv = new TextView(this);
        tv.setText(t);
        tv.setTextColor(0xFFFFD700);
        tv.setTextSize(14);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setPadding(10, 25, 10, 10);
        mineBox.addView(tv);
    }

    private void addMineCard(String title, String btnText, boolean isUsing, Runnable onClick) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(android.view.Gravity.CENTER_VERTICAL);
        card.setPadding(28, 24, 28, 24);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(0xFF2A1B4E);
        bg.setCornerRadius(24);
        card.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 12);
        card.setLayoutParams(lp);
        mineBox.addView(card);
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(15);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        card.addView(tv);
        TextView btn = new TextView(this);
        btn.setText(btnText);
        btn.setTextColor(isUsing ? 0xFF0A0518 : 0xFFFFD700);
        btn.setTextSize(13);
        btn.setTypeface(null, android.graphics.Typeface.BOLD);
        btn.setPadding(28, 14, 28, 14);
        android.graphics.drawable.GradientDrawable btnBg = new android.graphics.drawable.GradientDrawable();
        btnBg.setColor(isUsing ? 0xFFFFD700 : 0xFF1B1035);
        btnBg.setCornerRadius(20);
        btn.setBackground(btnBg);
        if (onClick != null) { btn.setOnClickListener(v -> onClick.run()); card.setOnClickListener(v -> onClick.run()); }
        card.addView(btn);
    }

    private String getVipName(int lvl) {
        String[] names = {"", "Black Tiger", "White Tiger", "Eagle Fire", "Phoenix Fire Bird", "Dragon", "Golden Dragon King", "Emperor Dragon", "Purple Dragon"};
        if (lvl >= 1 && lvl <= 8) return names[lvl];
        return "VIP";
    }

    private String getVipEmoji(int lvl) {
        String[] emojis = {"", "🐅", "🐯", "🦅", "🔥", "🐉", "👑", "🐲", "💜"};
        if (lvl >= 1 && lvl <= 8) return emojis[lvl];
        return "⭐";
    }
}
