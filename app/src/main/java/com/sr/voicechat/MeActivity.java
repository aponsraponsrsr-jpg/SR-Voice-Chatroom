package com.sr.voicechat;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window; // 🌟 প্রয়োজনীয় ইমপোর্ট যুক্ত করা হয়েছে
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MeActivity extends AppCompatActivity {

    private ImageView imgMeProfile, btnEditProfile;
    private TextView textMeName, textMeId, txtProfileLevel;
    private LinearLayout btnVipStatus, btnWalletRecharge, btnHostCenter, btnHostPolicy, btnLevelCenter;
    private LinearLayout toolStore, toolDress, toolBirthday;
    
    private LinearLayout btnSrSuperPanel;
    
    private SharedPreferences prefs;
    private DatabaseReference userDatabaseRef;
    private String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        currentUserId = prefs.getString("MY_9_DIGIT_ID", "493859069");
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        imgMeProfile = findViewById(R.id.imgMeProfile);
        textMeName = findViewById(R.id.textMeName);
        textMeId = findViewById(R.id.textMeId);
        txtProfileLevel = findViewById(R.id.txtProfileLevel); 
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnVipStatus = findViewById(R.id.btnVipStatus);
        btnWalletRecharge = findViewById(R.id.btnWalletRecharge);
        btnHostCenter = findViewById(R.id.btnHostCenter);
        btnHostPolicy = findViewById(R.id.btnHostPolicy);
        btnLevelCenter = findViewById(R.id.btnLevelCenter); 

        toolStore = findViewById(R.id.toolStore);
        toolDress = findViewById(R.id.toolDress);
        toolBirthday = findViewById(R.id.toolBirthday);

        setupSrSuperPanelButton();

        loadUserData();
        syncUserLevelFromFirebase();

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                Intent intent = new Intent(MeActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });
        }

        if (toolStore != null) {
            toolStore.setOnClickListener(v -> Toast.makeText(this, "🛍️ Opening Store...", Toast.LENGTH_SHORT).show());
        }
        if (toolDress != null) {
            toolDress.setOnClickListener(v -> Toast.makeText(this, "👕 Opening Dress & Frames...", Toast.LENGTH_SHORT).show());
        }
        if (toolBirthday != null) {
            toolBirthday.setOnClickListener(v -> Toast.makeText(this, "🎂 Opening Birthday Gifts...", Toast.LENGTH_SHORT).show());
        }

        if (btnLevelCenter != null) {
            btnLevelCenter.setOnClickListener(v -> showLevelCenterDialog());
        }

        if (btnVipStatus != null) {
            btnVipStatus.setOnClickListener(v -> showVipDialog());
        }

        if (btnWalletRecharge != null) {
            btnWalletRecharge.setOnClickListener(v -> showWalletRechargeDialog());
        }

        if (btnHostCenter != null) {
            btnHostCenter.setOnClickListener(v -> {
                new AlertDialog.Builder(MeActivity.this)
                    .setTitle("🎙️ Host Center")
                    .setMessage("Check your streaming targets, live duration, and daily earnings.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            });
        }

        if (btnHostPolicy != null) {
            btnHostPolicy.setOnClickListener(v -> showPolicyDialog());
        }
    }

    private void setupSrSuperPanelButton() {
        DatabaseReference masterCheckRef = FirebaseDatabase.getInstance().getReference("super_admins").child(currentUserId);
        
        masterCheckRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isMasterOwner = (snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) 
                                        || currentUserId.equals("493859069") 
                                        || currentUserId.length() <= 2; 

                if (isMasterOwner) {
                    createAndShowSuperPanelButton();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void createAndShowSuperPanelButton() {
        LinearLayout parentLayout = findViewById(android.R.id.content);
        if (parentLayout != null && parentLayout.getChildAt(0) instanceof ViewGroup) {
            ViewGroup rootGroup = (ViewGroup) parentLayout.getChildAt(0);
            
            btnSrSuperPanel = new LinearLayout(this);
            btnSrSuperPanel.setOrientation(LinearLayout.HORIZONTAL);
            btnSrSuperPanel.setGravity(Gravity.CENTER_VERTICAL);
            btnSrSuperPanel.setPadding(35, 25, 35, 25);

            GradientDrawable panelBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#2A1B4E"), Color.parseColor("#4A2E80")}
            );
            panelBg.setCornerRadius(30);
            btnSrSuperPanel.setBackground(panelBg);

            ImageView logoIcon = new ImageView(this);
            logoIcon.setImageResource(android.R.drawable.ic_menu_manage);
            logoIcon.setColorFilter(Color.parseColor("#FFD700"));
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(70, 70);
            imgParams.setMargins(0, 0, 20, 0);
            logoIcon.setLayoutParams(imgParams);
            btnSrSuperPanel.addView(logoIcon);

            TextView panelText = new TextView(this);
            panelText.setText("👑 এসআর সুপার প্যানেল (Master CEO Panel)");
            panelText.setTextColor(Color.parseColor("#FFD700"));
            panelText.setTextSize(16);
            panelText.setTypeface(null, android.graphics.Typeface.BOLD);
            btnSrSuperPanel.addView(panelText);

            btnSrSuperPanel.setOnClickListener(v -> showSrSuperPanelDialog());

            if (rootGroup instanceof LinearLayout) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 
                    ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(40, 30, 40, 20);
                btnSrSuperPanel.setLayoutParams(params);
                
                ((LinearLayout) rootGroup).addView(btnSrSuperPanel, 1);
            }
        }
    }

    private void showSrSuperPanelDialog() {
        BottomSheetDialog superPanelDialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 60);
        
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}
        );
        bg.setCornerRadii(new float[]{40, 40, 40, 40, 0, 0, 0, 0});
        layout.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("👑 এসআর সুপার প্যানেল (SR Super Panel)");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);

        final EditText inputTargetId = new EditText(this);
        inputTargetId.setHint("Enter Target User 9-Digit ID...");
        inputTargetId.setHintTextColor(Color.parseColor("#888888"));
        inputTargetId.setTextColor(Color.WHITE);
        inputTargetId.setBackgroundColor(Color.parseColor("#2A1B4E"));
        inputTargetId.setPadding(30, 30, 30, 30);
        layout.addView(inputTargetId);

        final EditText inputCoinAmount = new EditText(this);
        inputCoinAmount.setHint("Enter Coin Amount (e.g., 500000)...");
        inputCoinAmount.setHintTextColor(Color.parseColor("#888888"));
        inputCoinAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        inputCoinAmount.setTextColor(Color.WHITE);
        inputCoinAmount.setBackgroundColor(Color.parseColor("#2A1B4E"));
        inputCoinAmount.setPadding(30, 30, 30, 30);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 20, 0, 30);
        inputCoinAmount.setLayoutParams(params);
        layout.addView(inputCoinAmount);

        Button btnAddCoins = new Button(this);
        btnAddCoins.setText("💎 Add Free Coins to User ID");
        btnAddCoins.setBackgroundColor(Color.parseColor("#00E676"));
        btnAddCoins.setTextColor(Color.BLACK);
        btnAddCoins.setTypeface(null, android.graphics.Typeface.BOLD);
        btnAddCoins.setOnClickListener(v -> {
            String targetId = inputTargetId.getText().toString().trim();
            String coinStr = inputCoinAmount.getText().toString().trim();

            if (targetId.isEmpty() || coinStr.isEmpty()) {
                Toast.makeText(this, "Please enter both User ID and Coin Amount!", Toast.LENGTH_SHORT).show();
                return;
            }

            long coinsToAdd = Long.parseLong(coinStr);
            DatabaseReference targetUserRef = FirebaseDatabase.getInstance().getReference("users").child(targetId);
            
            targetUserRef.child("totalSpentCoins").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long currentCoins = 0;
                    if (snapshot.exists() && snapshot.getValue(Long.class) != null) {
                        currentCoins = snapshot.getValue(Long.class);
                    }
                    long updatedTotal = currentCoins + coinsToAdd;
                    
                    targetUserRef.child("totalSpentCoins").setValue(updatedTotal);
                    Toast.makeText(MeActivity.this, "Successfully added " + coinsToAdd + " coins to ID: " + targetId, Toast.LENGTH_LONG).show();
                    superPanelDialog.dismiss();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MeActivity.this, "Failed to update database!", Toast.LENGTH_SHORT).show();
                }
            });
        });
        layout.addView(btnAddCoins);

        superPanelDialog.setContentView(layout);
        Window window = superPanelDialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
        }
        superPanelDialog.show();
    }

    private int calculateUserLevel(long totalCoins) {
        if (totalCoins < 3000000L) {
            return 1;
        }

        long currentRequirement = 3000000L;
        int level = 1;

        for (int i = 2; i <= 150; i++) {
            if (i <= 20) {
                currentRequirement += 5000000L; 
            } else if (i <= 50) {
                currentRequirement += 20000000L; 
            } else if (i <= 100) {
                currentRequirement += 25000000L; 
            } else {
                currentRequirement += 35000000L; 
            }

            if (totalCoins >= currentRequirement) {
                level = i;
            } else {
                break;
            }
        }
        return Math.min(level, 150);
    }

    private int getLevelColor(int level) {
        if (level <= 10) {
            return Color.parseColor("#C0C0C0"); // Silver
        } else if (level <= 20) {
            return Color.parseColor("#29B6F6"); // Light Blue
        } else if (level <= 50) {
            return Color.parseColor("#AB47BC"); // Royal Purple
        } else if (level <= 100) {
            return Color.parseColor("#FF4081"); // Diamond Pink
        } else {
            return Color.parseColor("#FFD700"); // Glowing Gold
        }
    }

    private void syncUserLevelFromFirebase() {
        if (!currentUserId.isEmpty()) {
            userDatabaseRef.child(currentUserId).child("totalSpentCoins").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long spentCoins = 0;
                    if (snapshot.exists() && snapshot.getValue(Long.class) != null) {
                        spentCoins = snapshot.getValue(Long.class);
                    }
                    int currentLevel = calculateUserLevel(spentCoins);
                    if (txtProfileLevel != null) {
                        txtProfileLevel.setText("  |  ⭐ Lv." + currentLevel);
                        txtProfileLevel.setTextColor(getLevelColor(currentLevel));
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void showLevelCenterDialog() {
        final Dialog levelDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 60, 40, 60);
        
        GradientDrawable bg = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#2A1B4E"), Color.parseColor("#120A2A")}
        );
        layout.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("🏆 SR Voice Chat - Level Center (Lv.1 - Lv.150)");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);

        TextView info = new TextView(this);
        info.setText("• Spend 1 coin = 1 experience point\n• Level 1 to 20: Progressive target scaling\n• Level 21 to 150: Advanced milestone scaling with 20M+ coin gaps\n• Unlock glowing gold, diamond pink & royal purple entry effects!");
        info.setTextColor(Color.WHITE);
        info.setTextSize(14);
        info.setPadding(20, 20, 20, 30);
        layout.addView(info);

        Button btnClose = new Button(this);
        btnClose.setText("Close");
        btnClose.setBackgroundColor(Color.parseColor("#FF4081"));
        btnClose.setTextColor(Color.WHITE);
        btnClose.setOnClickListener(v -> levelDialog.dismiss());
        layout.addView(btnClose);

        levelDialog.setContentView(layout);
        levelDialog.show();
    }

    private void showPolicyDialog() {
        final Dialog policyDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        policyDialog.setContentView(R.layout.dialog_policy);

        TextView btnClosePolicy = policyDialog.findViewById(R.id.btnClosePolicy);
        TextView textPolicyRoleBanner = policyDialog.findViewById(R.id.textPolicyRoleBanner);

        boolean isAgencyUser = prefs.getBoolean("IS_AGENCY_ROLE", true);

        if (!isAgencyUser && textPolicyRoleBanner != null) {
            textPolicyRoleBanner.setText("🎙️ Host Salary Target Chart (Level 1-22)");
        }

        if (btnClosePolicy != null) {
            btnClosePolicy.setOnClickListener(v -> policyDialog.dismiss());
        }

        policyDialog.show();
    }

    private void showWalletRechargeDialog() {
        final Dialog rechargeDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        rechargeDialog.setContentView(R.layout.dialog_wallet_recharge);

        TextView textUserBalance = rechargeDialog.findViewById(R.id.textUserBalance);
        TextView btnCloseRecharge = rechargeDialog.findViewById(R.id.btnCloseRecharge);

        String currentCoins = prefs.getString("USER_COINS", "0");
        if (textUserBalance != null) {
            textUserBalance.setText(currentCoins + " Coins");
        }

        if (btnCloseRecharge != null) {
            btnCloseRecharge.setOnClickListener(v -> rechargeDialog.dismiss());
        }

        View.OnClickListener packageClick = v -> {
            new AlertDialog.Builder(MeActivity.this)
                .setTitle("💎 Coin Recharge")
                .setMessage("Please contact SR Voice Chat Room CEO for coin recharge.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
        };

        LinearLayout pkg1 = rechargeDialog.findViewById(R.id.pkg1);
        LinearLayout pkg2 = rechargeDialog.findViewById(R.id.pkg2);
        LinearLayout pkg3 = rechargeDialog.findViewById(R.id.pkg3);
        LinearLayout pkg4 = rechargeDialog.findViewById(R.id.pkg4);

        if (pkg1 != null) pkg1.setOnClickListener(packageClick);
        if (pkg2 != null) pkg2.setOnClickListener(packageClick);
        if (pkg3 != null) pkg3.setOnClickListener(packageClick);
        if (pkg4 != null) pkg4.setOnClickListener(packageClick);

        rechargeDialog.show();
    }

    private void showVipDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_vip_status);
        dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        androidx.recyclerview.widget.RecyclerView recyclerVipList = dialog.findViewById(R.id.recyclerVipList);
        Button btnClose = dialog.findViewById(R.id.btnCloseVipDialog);

        recyclerVipList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        java.util.List<VipModel> vipList = new java.util.ArrayList<>();
        vipList.add(new VipModel("VIP 1", "2,000,000 Coins", 0, 0, 0, "Basic Sparkle"));
        vipList.add(new VipModel("VIP 2", "5,000,000 Coins", 0, 0, 0, "Glow Ring"));
        vipList.add(new VipModel("VIP 3", "8,000,000 Coins", 0, 0, 0, "Silver Wings"));
        vipList.add(new VipModel("VIP 4", "15,000,000 Coins", 0, 0, 0, "Golden Crown"));
        vipList.add(new VipModel("VIP 5", "20,000,000 Coins", 0, 0, 0, "Fire Tiger Entry"));
        vipList.add(new VipModel("VIP 6", "30,000,000 Coins", 0, 0, 0, "Royal Dragon"));
        vipList.add(new VipModel("VIP 7", "50,000,000 Coins", 0, 0, 0, "Supreme Lord Effect"));

        VipAdapter adapter = new VipAdapter(this, vipList);
        recyclerVipList.setAdapter(adapter);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        syncUserLevelFromFirebase();
    }

    private void loadUserData() {
        String name = prefs.getString("USER_NAME", "Sr-Apon");
        String userId = prefs.getString("MY_9_DIGIT_ID", "493859069");
        String encodedImage = prefs.getString("USER_PROFILE_IMAGE", prefs.getString("USER_IMAGE", ""));

        if (textMeName != null) {
            textMeName.setText(name);
        }
        if (textMeId != null) {
            textMeId.setText("ID: " + userId);
        }

        if (imgMeProfile != null && !encodedImage.isEmpty()) {
            try {
                byte[] decodedString = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedByte != null) {
                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), decodedByte);
                    circularBitmapDrawable.setCircular(true);
                    imgMeProfile.setImageDrawable(circularBitmapDrawable);
                }
            } catch (Exception e) {
                imgMeProfile.setImageResource(android.R.drawable.ic_menu_my_calendar);
            }
        }
    }
}
