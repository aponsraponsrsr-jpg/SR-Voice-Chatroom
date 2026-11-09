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
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
    private TextView textMeName, textMeId, txtProfileLevel, txtAgencyBadge, txtHostBadge;
    private LinearLayout btnVipStatus, btnWalletRecharge, btnHostCenter, btnHostPolicy, btnLevelCenter;
    private LinearLayout toolStore, toolDress, toolBirthday;
    
    private SharedPreferences prefs;
    private DatabaseReference userDatabaseRef, agencyRef;
    private String currentUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        try {
            prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
            currentUserId = prefs.getString("MY_9_DIGIT_ID", "493859069");
            userDatabaseRef = FirebaseDatabase.getInstance().getReference("users");
            agencyRef = FirebaseDatabase.getInstance().getReference("agencies");

            imgMeProfile = findViewById(R.id.imgMeProfile);
            textMeName = findViewById(R.id.textMeName);
            textMeId = findViewById(R.id.textMeId);
            txtProfileLevel = findViewById(R.id.txtProfileLevel); 
            
            txtAgencyBadge = findViewById(R.id.txtAgencyBadge);
            txtHostBadge = findViewById(R.id.txtHostBadge);

            btnEditProfile = findViewById(R.id.btnEditProfile);
            btnVipStatus = findViewById(R.id.btnVipStatus);
            btnWalletRecharge = findViewById(R.id.btnWalletRecharge);
            btnHostCenter = findViewById(R.id.btnHostCenter);
            btnHostPolicy = findViewById(R.id.btnHostPolicy);
            btnLevelCenter = findViewById(R.id.btnLevelCenter); 

            toolStore = findViewById(R.id.toolStore);
            toolDress = findViewById(R.id.toolDress);
            toolBirthday = findViewById(R.id.toolBirthday);

            loadUserData();
            syncUserLevelFromFirebase();
            checkUserBadgesAndAgencyStatus();
            checkSuperAdminAccessAndShowPanel(); // সুপার অ্যাডমিন জিমেইল সিকিউরিটি চেক

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
                    Intent intent = new Intent(MeActivity.this, AgencyDashboardActivity.class);
                    intent.putExtra("AGENCY_OWNER_ID", currentUserId);
                    intent.putExtra("AGENCY_NAME", "SR Agency");
                    startActivity(intent);
                });
            }

            if (btnHostPolicy != null) {
                btnHostPolicy.setOnClickListener(v -> showPolicyDialog());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkUserBadgesAndAgencyStatus() {
        if (currentUserId == null || currentUserId.isEmpty()) return;

        agencyRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String agencyName = snapshot.child("agencyName").getValue(String.class);
                    if (txtAgencyBadge != null) {
                        txtAgencyBadge.setVisibility(View.VISIBLE);
                        txtAgencyBadge.setText(" 🏛️ " + (agencyName != null ? agencyName : "Agency Owner") + " ");
                    }
                } else {
                    if (txtAgencyBadge != null) txtAgencyBadge.setVisibility(View.GONE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        agencyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isHost = false;
                for (DataSnapshot agencySnap : snapshot.getChildren()) {
                    if (agencySnap.child("hosts").child(currentUserId).exists()) {
                        isHost = true;
                        break;
                    }
                }

                if (isHost) {
                    if (txtHostBadge != null) {
                        txtHostBadge.setVisibility(View.VISIBLE);
                        txtHostBadge.setText(" ⭐ Official Host ");
                    }
                } else {
                    if (txtHostBadge != null) txtHostBadge.setVisibility(View.GONE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // 👑 সুপার অ্যাডমিন জিমেইল সিকিউরিটি চেক (সম্পূর্ণ ছোট হাতের অক্ষরে)
    private void checkSuperAdminAccessAndShowPanel() {
        if (prefs == null) return;
        
        String ownerGmail = "aponsraponsrsr@gmail.com"; // সম্পূর্ণ ছোট হাতের জিমেইল
        
        String loggedInEmail = prefs.getString("USER_EMAIL", "");
        if (loggedInEmail.isEmpty()) {
            loggedInEmail = prefs.getString("email", "");
        }
        if (loggedInEmail.isEmpty()) {
            loggedInEmail = prefs.getString("GOOGLE_EMAIL", "");
        }

        // ছোট হাতের অক্ষরে রূপান্তর করে নিখুঁতভাবে ম্যাচিং করা
        boolean isOwnerOrSuperAdmin = loggedInEmail.trim().toLowerCase().equals(ownerGmail);

        if (isOwnerOrSuperAdmin) {
            if (textMeName != null) {
                textMeName.setOnClickListener(v -> showSuperAdminMasterControlDialog());
            }
        } else {
            if (textMeName != null) {
                textMeName.setOnClickListener(null);
            }
        }
    }

    private void showSuperAdminMasterControlDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("👑 Super Admin - Master Control Panel");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 30);

        final EditText inputOwnerId = new EditText(this);
        inputOwnerId.setHint("Enter Owner ID for Agency...");
        layout.addView(inputOwnerId);

        final EditText inputAgencyName = new EditText(this);
        inputAgencyName.setHint("Enter Agency Name...");
        layout.addView(inputAgencyName);

        View divider = new View(this);
        divider.setMinimumHeight(15);
        layout.addView(divider);

        final EditText inputTargetUser = new EditText(this);
        inputTargetUser.setHint("Target User ID (Old ID)...");
        layout.addView(inputTargetUser);

        final EditText inputNewShortId = new EditText(this);
        inputNewShortId.setHint("New Short/Unique ID (e.g. 10)...");
        layout.addView(inputNewShortId);

        builder.setView(layout);

        builder.setPositiveButton("Create Agency", null);
        builder.setNeutralButton("Make Short ID", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button btnCreateAgency = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button btnMakeShortId = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            btnCreateAgency.setOnClickListener(v -> {
                String ownerId = inputOwnerId.getText().toString().trim();
                String agencyName = inputAgencyName.getText().toString().trim();

                if (!ownerId.isEmpty() && !agencyName.isEmpty()) {
                    createAgencyInFirebase(ownerId, agencyName);
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Owner ID and Agency Name required!", Toast.LENGTH_SHORT).show();
                }
            });

            btnMakeShortId.setOnClickListener(v -> {
                String targetUser = inputTargetUser.getText().toString().trim();
                String newId = inputNewShortId.getText().toString().trim();

                if (!targetUser.isEmpty() && !newId.isEmpty()) {
                    updateUserShortIdInFirebase(targetUser, newId);
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Target ID and New Short ID required!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void createAgencyInFirebase(String ownerUserId, String agencyName) {
        DatabaseReference targetAgencyRef = agencyRef.child(ownerUserId);
        
        java.util.Map<String, Object> agencyMap = new java.util.HashMap<>();
        agencyMap.put("agencyOwnerId", ownerUserId);
        agencyMap.put("agencyName", agencyName);
        agencyMap.put("createdAt", System.currentTimeMillis());
        agencyMap.put("status", "Active");

        targetAgencyRef.setValue(agencyMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DatabaseReference userRef = userDatabaseRef.child(ownerUserId);
                userRef.child("isAgencyOwner").setValue(true);
                userRef.child("agencyName").setValue(agencyName);

                Toast.makeText(this, "Agency '" + agencyName + "' Created Successfully!", Toast.LENGTH_LONG).show();
                
                Intent intent = new Intent(MeActivity.this, AgencyDashboardActivity.class);
                intent.putExtra("AGENCY_OWNER_ID", ownerUserId);
                intent.putExtra("AGENCY_NAME", agencyName);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Failed to create agency!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserShortIdInFirebase(String oldUserId, String newShortId) {
        if (oldUserId == null || oldUserId.trim().isEmpty() || newShortId == null || newShortId.trim().isEmpty()) {
            Toast.makeText(this, "Old ID and New ID cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (oldUserId.equals(newShortId)) {
            Toast.makeText(this, "Old ID and New ID cannot be the same!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference oldUserRef = userDatabaseRef.child(oldUserId);
        DatabaseReference newUserRef = userDatabaseRef.child(newShortId);

        newUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot newSnapshot) {
                if (newSnapshot.exists()) {
                    Toast.makeText(MeActivity.this, "Error: New Short ID (" + newShortId + ") already exists!", Toast.LENGTH_LONG).show();
                } else {
                    oldUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot oldSnapshot) {
                            if (oldSnapshot.exists()) {
                                java.util.Map<String, Object> userDataMap = new java.util.HashMap<>();
                                for (DataSnapshot child : oldSnapshot.getChildren()) {
                                    userDataMap.put(child.getKey(), child.getValue());
                                }
                                
                                userDataMap.put("userId", newShortId);

                                newUserRef.setValue(userDataMap).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        oldUserRef.removeValue().addOnCompleteListener(removeTask -> {
                                            if (removeTask.isSuccessful()) {
                                                if (oldUserId.equals(currentUserId)) {
                                                    prefs.edit().putString("MY_9_DIGIT_ID", newShortId).apply();
                                                    currentUserId = newShortId;
                                                }

                                                Toast.makeText(MeActivity.this, "Success! ID Shortened to: " + newShortId, Toast.LENGTH_LONG).show();
                                                loadUserData();
                                            } else {
                                                Toast.makeText(MeActivity.this, "New ID created, but old ID removal failed!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        String errorMsg = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                        Toast.makeText(MeActivity.this, "Failed to create New ID: " + errorMsg, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } else {
                                Toast.makeText(MeActivity.this, "Target Old ID (" + oldUserId + ") not found in database!", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MeActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MeActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void syncUserLevelFromFirebase() {
        if (currentUserId != null && !currentUserId.isEmpty() && userDatabaseRef != null) {
            try {
                userDatabaseRef.child(currentUserId).child("sendingCoins").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            long spentCoins = 0;
                            if (snapshot.exists() && snapshot.getValue(Long.class) != null) {
                                spentCoins = snapshot.getValue(Long.class);
                            }
                            int currentLevel = LevelUtils.calculateLevel(spentCoins);
                            if (txtProfileLevel != null) {
                                txtProfileLevel.setText("  |  ⭐ Lv." + currentLevel);
                                txtProfileLevel.setTextColor(LevelUtils.getLevelColor(currentLevel));
                            }
                        } catch (Exception ignored) {}
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            } catch (Exception ignored) {}
        }
    }

    private void loadUserData() {
        try {
            if (prefs == null) return;
            String name = prefs.getString("USER_NAME", "Sr-Apon");
            String userId = prefs.getString("MY_9_DIGIT_ID", "493859069");
            String encodedImage = prefs.getString("USER_PROFILE_IMAGE", prefs.getString("USER_IMAGE", ""));

            if (textMeName != null) textMeName.setText(name);
            if (textMeId != null) textMeId.setText("ID: " + userId);

            if (imgMeProfile != null && encodedImage != null && !encodedImage.isEmpty()) {
                byte[] decodedString = android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedByte != null) {
                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), decodedByte);
                    circularBitmapDrawable.setCircular(true);
                    imgMeProfile.setImageDrawable(circularBitmapDrawable);
                }
            }
        } catch (Exception ignored) {}
    }

    private void showLevelCenterDialog() {
        try {
            BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
            LinearLayout root = new LinearLayout(this);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setPadding(35, 35, 35, 35);

            GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}
            );
            bg.setCornerRadii(new float[]{45, 45, 45, 45, 0, 0, 0, 0});
            root.setBackground(bg);

            LinearLayout topBar = new LinearLayout(this);
            topBar.setOrientation(LinearLayout.HORIZONTAL);
            topBar.setGravity(Gravity.CENTER_VERTICAL);
            topBar.setPadding(0, 0, 0, 20);

            TextView title = new TextView(this);
            title.setText("🏆 SR Level Center (Lv.1 - Lv.150)");
            title.setTextColor(Color.parseColor("#FFD700"));
            title.setTextSize(18);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            title.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
            topBar.addView(title);

            Button btnClose = new Button(this);
            btnClose.setText("✕");
            btnClose.setBackgroundColor(Color.TRANSPARENT);
            btnClose.setTextColor(Color.WHITE);
            btnClose.setTextSize(16);
            btnClose.setOnClickListener(v -> dialog.dismiss());
            topBar.addView(btnClose);

            root.addView(topBar);

            ScrollView scrollView = new ScrollView(this);
            scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1400));
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

                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                p.setMargins(0, 0, 0, 14);
                row.setLayoutParams(p);

                int levelColor = LevelUtils.getLevelColor(lvl);
                String badgeText = LevelUtils.getLevelBadgeText(lvl);
                String targetCoins = LevelUtils.getLevelTargetCoinsFormatted(lvl);

                TextView badge = new TextView(this);
                badge.setText(badgeText);
                badge.setTextColor(Color.WHITE);
                badge.setTextSize(14);
                badge.setTypeface(null, android.graphics.Typeface.BOLD);
                badge.setGravity(Gravity.CENTER);
                badge.setPadding(20, 12, 20, 12);

                GradientDrawable bBg = new GradientDrawable();
                bBg.setColor(levelColor);
                bBg.setCornerRadius(25);
                badge.setBackground(bBg);
                badge.setMinWidth(110);
                row.addView(badge);

                LinearLayout textStack = new LinearLayout(this);
                textStack.setOrientation(LinearLayout.VERTICAL);
                textStack.setPadding(20, 0, 0, 0);
                LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
                textStack.setLayoutParams(tp);

                TextView titleTxt = new TextView(this);
                titleTxt.setText("Level Badge Tier " + lvl);
                titleTxt.setTextColor(Color.WHITE);
                titleTxt.setTextSize(15);
                titleTxt.setTypeface(null, android.graphics.Typeface.BOLD);
                textStack.addView(titleTxt);

                TextView targetTxt = new TextView(this);
                targetTxt.setText("Target: " + targetCoins);
                targetTxt.setTextColor(Color.parseColor("#00E676"));
                targetTxt.setTextSize(12);
                textStack.addView(targetTxt);

                row.addView(textStack);
                listContainer.addView(row);
            }

            scrollView.addView(listContainer);
            root.addView(scrollView);

            dialog.setContentView(root);
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 1600);
                window.setGravity(Gravity.BOTTOM);
            }
            dialog.show();
        } catch (Exception ignored) {}
    }

    private void showPolicyDialog() {
        try {
            final Dialog policyDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            policyDialog.setContentView(R.layout.dialog_policy);
            policyDialog.show();
        } catch (Exception ignored) {}
    }

    private void showWalletRechargeDialog() {
        try {
            WalletRechargeDialog rechargeDialog = new WalletRechargeDialog();
            rechargeDialog.show(getSupportFragmentManager(), "WalletRechargeDialog");
        } catch (Exception ignored) {}
    }

    private void showVipDialog() {
        try {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_vip_status);
            dialog.show();
        } catch (Exception ignored) {}
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }
}
