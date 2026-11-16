package com.sr.voicechat;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

// SUPER PANEL - ONLY OWNER CONTROLS - Opens after Owner Login
// This Panel writes data in SAME format as your old AgencyDashboardActivity expects:
// agencies / {FirebaseUID} / hosts, name, percent
// users / {FirebaseUID} / isAgency, agencyBadge, agencyPercent
// So your old Dashboard will open Auto, no change needed in Dashboard.
public class SuperPanelActivity extends AppCompatActivity {
    private EditText etTargetId, etAgencyName;
    private TextView tvStatus;
    private LinearLayout agencyListBox;
    private DatabaseReference usersRef, agenciesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        agenciesRef = FirebaseDatabase.getInstance().getReference("agencies");

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 30, 28, 30);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("👑 SUPER PANEL");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Only Company Owner Controls Here\nMake Agency / Set % / Remove Agency");
        sub.setTextColor(Color.parseColor("#B0BEC5"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 24);
        root.addView(sub);

        TextView lbl1 = new TextView(this);
        lbl1.setText("Agency Owner 9-Digit ID");
        lbl1.setTextColor(Color.WHITE);
        lbl1.setTextSize(14);
        lbl1.setTypeface(null, Typeface.BOLD);
        root.addView(lbl1);

        etTargetId = new EditText(this);
        etTargetId.setHint("Example: 7125645");
        etTargetId.setHintTextColor(Color.parseColor("#757575"));
        etTargetId.setTextColor(Color.WHITE);
        etTargetId.setTextSize(16);
        etTargetId.setPadding(24, 22, 24, 22);
        GradientDrawable etBg = new GradientDrawable();
        etBg.setColor(Color.parseColor("#1B1035"));
        etBg.setCornerRadius(18);
        etBg.setStroke(1, Color.parseColor("#FFD700"));
        etTargetId.setBackground(etBg);
        LinearLayout.LayoutParams etLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        etLp.setMargins(0, 12, 0, 12);
        etTargetId.setLayoutParams(etLp);
        root.addView(etTargetId);

        TextView lbl2 = new TextView(this);
        lbl2.setText("Agency Name");
        lbl2.setTextColor(Color.WHITE);
        lbl2.setTextSize(14);
        lbl2.setTypeface(null, Typeface.BOLD);
        lbl2.setPadding(0, 8, 0, 0);
        root.addView(lbl2);

        etAgencyName = new EditText(this);
        etAgencyName.setHint("Example: SR Agency");
        etAgencyName.setHintTextColor(Color.parseColor("#757575"));
        etAgencyName.setTextColor(Color.WHITE);
        etAgencyName.setTextSize(16);
        etAgencyName.setPadding(24, 22, 24, 22);
        etAgencyName.setBackground(etBg);
        LinearLayout.LayoutParams etLp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        etLp2.setMargins(0, 12, 0, 12);
        etAgencyName.setLayoutParams(etLp2);
        root.addView(etAgencyName);

        LinearLayout percentRow = new LinearLayout(this);
        percentRow.setOrientation(LinearLayout.HORIZONTAL);
        percentRow.setGravity(Gravity.CENTER);
        root.addView(percentRow);
        percentRow.addView(makePercentBtn("40% - Make Agency", 40));
        percentRow.addView(makePercentBtn("30% - Make Agency", 30));

        LinearLayout percentRow2 = new LinearLayout(this);
        percentRow2.setOrientation(LinearLayout.HORIZONTAL);
        percentRow2.setGravity(Gravity.CENTER);
        root.addView(percentRow2);
        percentRow2.addView(makePercentBtn("20% - Make Agency", 20));
        percentRow2.addView(makePercentBtn("10% - Make Agency", 10));

        tvStatus = new TextView(this);
        tvStatus.setText("Status: Ready");
        tvStatus.setTextColor(Color.parseColor("#FFD700"));
        tvStatus.setTextSize(13);
        tvStatus.setPadding(0, 18, 0, 12);
        root.addView(tvStatus);

        TextView listTitle = new TextView(this);
        listTitle.setText("All Agency List");
        listTitle.setTextColor(Color.WHITE);
        listTitle.setTextSize(16);
        listTitle.setTypeface(null, Typeface.BOLD);
        listTitle.setPadding(0, 16, 0, 12);
        root.addView(listTitle);

        agencyListBox = new LinearLayout(this);
        agencyListBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(agencyListBox);

        setContentView(scroll);
        loadAgencyList();
    }

    private TextView makePercentBtn(String label, int percent) {
        TextView btn = new TextView(this);
        btn.setText(label);
        btn.setTextColor(Color.BLACK);
        btn.setTextSize(12);
        btn.setTypeface(null, Typeface.BOLD);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(14, 20, 14, 20);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#FFD700"));
        bg.setCornerRadius(18);
        btn.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        lp.setMargins(6, 6, 6, 6);
        btn.setLayoutParams(lp);
        btn.setOnClickListener(v -> makeAgency(percent));
        return btn;
    }

    private void makeAgency(int percent) {
        String targetId = etTargetId.getText().toString().trim();
        String agencyName = etAgencyName.getText().toString().trim();
        if (targetId.isEmpty()) {
            Toast.makeText(this, "Enter 9-Digit ID First", Toast.LENGTH_SHORT).show();
            return;
        }
        if (agencyName.isEmpty()) agencyName = "SR Agency";
        final String finalAgencyName = agencyName;
        tvStatus.setText("Status: Searching ID " + targetId + "...");
        usersRef.orderByChild("my9DigitId").equalTo(targetId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists()) {
                    for (DataSnapshot child : snap.getChildren()) applyAgency(child, targetId, finalAgencyName, percent);
                    return;
                }
                usersRef.orderByChild("ID").equalTo(targetId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap2) {
                        if (snap2.exists()) {
                            for (DataSnapshot child : snap2.getChildren()) applyAgency(child, targetId, finalAgencyName, percent);
                            return;
                        }
                        usersRef.child(targetId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onDataChange(@NonNull DataSnapshot snap3) {
                                if (snap3.exists()) applyAgency(snap3, targetId, finalAgencyName, percent);
                                else {
                                    tvStatus.setText("Status: ID Not Found: " + targetId);
                                    Toast.makeText(SuperPanelActivity.this, "ID Not Found - User must Login once", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override public void onCancelled(@NonNull DatabaseError e) {}
                        });
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void applyAgency(DataSnapshot child, String targetId, String agencyName, int percent) {
        String firebaseUid = child.getKey();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("isAgency", true);
        userMap.put("agencyBadge", "AGENCY");
        userMap.put("agencyPercent", percent);
        userMap.put("agencyName", agencyName);
        userMap.put("agencyOwnerId", firebaseUid);
        userMap.put("agencyCreatedAt", System.currentTimeMillis());
        usersRef.child(firebaseUid).updateChildren(userMap);

        Map<String, Object> agencyMap = new HashMap<>();
        agencyMap.put("agencyName", agencyName);
        agencyMap.put("agencyOwnerId", firebaseUid);
        agencyMap.put("agencyOwner9DigitId", targetId);
        agencyMap.put("percent", percent);
        agencyMap.put("agencyPercent", percent);
        agencyMap.put("totalHosts", 0);
        agencyMap.put("totalEarnings", 0);
        agenciesRef.child(firebaseUid).updateChildren(agencyMap).addOnCompleteListener(t -> {
            tvStatus.setText("Status: Agency Created - ID " + targetId + " - " + agencyName + " - " + percent + "%");
            Toast.makeText(this, "Agency Created: " + targetId + " - Badge Added Auto", Toast.LENGTH_LONG).show();
            loadAgencyList();
        });
    }

    private void loadAgencyList() {
        if (agencyListBox == null) return;
        agencyListBox.removeAllViews();
        usersRef.orderByChild("isAgency").equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) {
                    TextView empty = new TextView(SuperPanelActivity.this);
                    empty.setText("No Agency Yet");
                    empty.setTextColor(Color.parseColor("#B0BEC5"));
                    empty.setTextSize(13);
                    agencyListBox.addView(empty);
                    return;
                }
                for (DataSnapshot child : snap.getChildren()) {
                    String uid = child.getKey();
                    String id = child.child("my9DigitId").getValue() != null ? String.valueOf(child.child("my9DigitId").getValue()) : uid;
                    String name = child.child("name").getValue() != null ? String.valueOf(child.child("name").getValue()) : "User";
                    String aName = child.child("agencyName").getValue() != null ? String.valueOf(child.child("agencyName").getValue()) : "SR Agency";
                    Object pctObj = child.child("agencyPercent").getValue();
                    String pct = pctObj != null ? String.valueOf(pctObj) : "0";

                    LinearLayout row = new LinearLayout(SuperPanelActivity.this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding(20, 18, 20, 18);
                    GradientDrawable bg = new GradientDrawable();
                    bg.setColor(Color.parseColor("#1B1035"));
                    bg.setCornerRadius(16);
                    row.setBackground(bg);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.setMargins(0, 0, 0, 10);
                    row.setLayoutParams(lp);
                    agencyListBox.addView(row);

                    LinearLayout left = new LinearLayout(SuperPanelActivity.this);
                    left.setOrientation(LinearLayout.VERTICAL);
                    left.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                    row.addView(left);
                    TextView t1 = new TextView(SuperPanelActivity.this);
                    t1.setText("🏷️ " + aName + " - " + name);
                    t1.setTextColor(Color.parseColor("#FFD700"));
                    t1.setTypeface(null, Typeface.BOLD);
                    t1.setTextSize(14);
                    left.addView(t1);
                    TextView t2 = new TextView(SuperPanelActivity.this);
                    t2.setText("ID: " + id + " | " + pct + "% | UID: " + uid);
                    t2.setTextColor(Color.WHITE);
                    t2.setTextSize(11);
                    left.addView(t2);

                    TextView btnRemove = new TextView(SuperPanelActivity.this);
                    btnRemove.setText("Remove");
                    btnRemove.setTextColor(Color.parseColor("#FF4081"));
                    btnRemove.setTypeface(null, Typeface.BOLD);
                    btnRemove.setPadding(16, 10, 16, 10);
                    btnRemove.setOnClickListener(v -> {
                        new AlertDialog.Builder(SuperPanelActivity.this)
                            .setTitle("Remove Agency?")
                            .setMessage("Remove Agency from ID " + id + "? Badge will be removed auto.")
                            .setPositiveButton("Remove", (d, w) -> {
                                usersRef.child(uid).child("isAgency").setValue(false);
                                usersRef.child(uid).child("agencyBadge").setValue("");
                                agenciesRef.child(uid).removeValue();
                                Toast.makeText(SuperPanelActivity.this, "Agency Removed", Toast.LENGTH_SHORT).show();
                                loadAgencyList();
                            })
                            .setNegativeButton("Cancel", null).show();
                    });
                    row.addView(btnRemove);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
}
