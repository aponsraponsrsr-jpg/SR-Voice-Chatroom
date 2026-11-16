package com.sr.voicechat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// HOST POLICY - READ ONLY - NO EDIT
// Host sees ONLY: Target Coins + Host Salary (Anchor 80%)
// Host NEVER sees: Agency Commission, Total Salary
// All data from Database (Company Control) - User cannot change Dollar
public class HostPolicyActivity extends AppCompatActivity {
    private LinearLayout listBox;
    private TextView tvMyProgress, tvMyLevel, tvMySalary;
    private String myUid = "";

    // OFFICIAL POLICY - Company Fixed - From your Image
    // Level, TargetCoins, HostSalary(Anchor 80%)
    private final int[][] POLICY = {
        {1, 288000, 1},
        {2, 588000, 2},
        {3, 1190000, 4},
        {4, 1780000, 7},
        {5, 3280000, 14},
        {6, 5780000, 24},
        {7, 7880000, 32},
        {8, 9980000, 40},
        {9, 13850000, 56},
        {10, 18850000, 76},
        {11, 26500000, 104},
        {12, 34500000, 132},
        {13, 43500000, 168},
        {14, 56500000, 220},
        {15, 76500000, 292},
        {16, 88500000, 348},
        {17, 115500000, 440},
        {18, 155000000, 584},
        {19, 210000000, 752},
        {20, 288000000, 1024},
        {21, 350000000, 1240},
        {22, 460000000, 1600}
    };
    // For .5 values: Level 1 is $1, Level 3 is $4.5 - handled in display
    private final String[] HOST_SALARY_TEXT = {
        "$1", "$2", "$4.5", "$7", "$14", "$24", "$32", "$40", "$56", "$76",
        "$104", "$132", "$168", "$220", "$292", "$348", "$440", "$584", "$752", "$1024", "$1240", "$1600"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        if (fu != null) myUid = fu.getUid();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 28, 24, 28);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("🎯 HOST SALARY POLICY");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Official Policy 1-22 • Monthly Settlement\nCompany Database থেকে হিসাব হবে • কেউ বদলাতে পারবে না");
        sub.setTextColor(Color.parseColor("#B0BEC5"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 18);
        root.addView(sub);

        // MY PROGRESS BOX - From Database
        LinearLayout myBox = new LinearLayout(this);
        myBox.setOrientation(LinearLayout.VERTICAL);
        myBox.setPadding(26, 22, 26, 22);
        GradientDrawable myBg = new GradientDrawable();
        myBg.setColor(Color.parseColor("#1B1035"));
        myBg.setCornerRadius(22);
        myBg.setStroke(1, Color.parseColor("#FFD700"));
        myBox.setBackground(myBg);
        LinearLayout.LayoutParams myLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myLp.setMargins(0, 0, 0, 18);
        myBox.setLayoutParams(myLp);
        root.addView(myBox);

        tvMyProgress = new TextView(this);
        tvMyProgress.setText("My Received: Loading...");
        tvMyProgress.setTextColor(Color.WHITE);
        tvMyProgress.setTypeface(null, Typeface.BOLD);
        tvMyProgress.setTextSize(15);
        myBox.addView(tvMyProgress);

        tvMyLevel = new TextView(this);
        tvMyLevel.setText("My Level: -");
        tvMyLevel.setTextColor(Color.parseColor("#FFD700"));
        tvMyLevel.setTypeface(null, Typeface.BOLD);
        tvMyLevel.setTextSize(15);
        myBox.addView(tvMyLevel);

        tvMySalary = new TextView(this);
        tvMySalary.setText("My Host Salary: $0");
        tvMySalary.setTextColor(Color.parseColor("#00E676"));
        tvMySalary.setTypeface(null, Typeface.BOLD);
        tvMySalary.setTextSize(16);
        myBox.addView(tvMySalary);

        // HEADER ROW
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding(18, 14, 18, 14);
        GradientDrawable hBg = new GradientDrawable();
        hBg.setColor(Color.parseColor("#0F5132"));
        hBg.setCornerRadius(14);
        header.setBackground(hBg);
        root.addView(header);
        addHeaderCell(header, "Level", 1);
        addHeaderCell(header, "Target Coins", 2);
        addHeaderCell(header, "Host Salary", 2);
        // NOTE: NO Agency Commission column here - Host must NOT see it

        listBox = new LinearLayout(this);
        listBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(listBox);

        for (int i = 0; i < POLICY.length; i++) {
            addRow(i, POLICY[i][0], POLICY[i][1], HOST_SALARY_TEXT[i]);
        }

        TextView note = new TextView(this);
        note.setText("• Target Complete হলেই Salary Counting হবে\n• 30% Exchange Coin আলাদা ভাবে Wallet এ যাবে\n• এই পলিসি Company থেকে আসে, Host বদলাতে পারবে না");
        note.setTextColor(Color.parseColor("#B0BEC5"));
        note.setTextSize(12);
        note.setPadding(0, 18, 0, 0);
        root.addView(note);

        setContentView(scroll);
        loadMyProgress();
    }

    private void addHeaderCell(LinearLayout row, String txt, float weight) {
        TextView t = new TextView(this);
        t.setText(txt);
        t.setTextColor(Color.WHITE);
        t.setTypeface(null, Typeface.BOLD);
        t.setTextSize(13);
        t.setGravity(Gravity.CENTER);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight));
        row.addView(t);
    }

    private void addRow(int index, int level, int target, String salary) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(18, 16, 18, 16);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(index % 2 == 0 ? Color.parseColor("#1B1035") : Color.parseColor("#140B26"));
        bg.setCornerRadius(12);
        row.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 4, 0, 4);
        row.setLayoutParams(lp);
        listBox.addView(row);

        TextView t1 = new TextView(this);
        t1.setText("Level " + level);
        t1.setTextColor(Color.WHITE);
        t1.setTypeface(null, Typeface.BOLD);
        t1.setTextSize(13);
        t1.setGravity(Gravity.CENTER);
        t1.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        row.addView(t1);

        TextView t2 = new TextView(this);
        t2.setText(formatCoins(target));
        t2.setTextColor(Color.parseColor("#FFD700"));
        t2.setTypeface(null, Typeface.BOLD);
        t2.setTextSize(13);
        t2.setGravity(Gravity.CENTER);
        t2.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
        row.addView(t2);

        TextView t3 = new TextView(this);
        t3.setText(salary);
        t3.setTextColor(Color.parseColor("#00E676"));
        t3.setTypeface(null, Typeface.BOLD);
        t3.setTextSize(14);
        t3.setGravity(Gravity.CENTER);
        t3.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
        row.addView(t3);
    }

    // Load My Progress ONLY from Database - Read Only
    private void loadMyProgress() {
        if (myUid.isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(myUid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                long received = 0;
                if (snap.hasChild("totalReceivedCoins") && snap.child("totalReceivedCoins").getValue() != null) {
                    try { received = Long.parseLong(String.valueOf(snap.child("totalReceivedCoins").getValue())); } catch (Exception ignored) {}
                }
                // Find my level from Database counting
                int myLevel = 0;
                String mySalary = "$0";
                for (int i = 0; i < POLICY.length; i++) {
                    if (received >= POLICY[i][1]) {
                        myLevel = POLICY[i][0];
                        mySalary = HOST_SALARY_TEXT[i];
                    }
                }
                tvMyProgress.setText("My Received: " + formatCoins((int) received) + " Coins");
                tvMyLevel.setText("My Level: " + (myLevel == 0 ? "No Level Yet" : "Level " + myLevel));
                tvMySalary.setText("My Host Salary: " + mySalary);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private String formatCoins(int n) {
        return String.format("%,d", n);
    }
}
