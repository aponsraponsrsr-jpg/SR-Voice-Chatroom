package com.sr.voicechat;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;

public class HostCenterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        String name = prefs.getString("USER_NAME", "User");
        String id = prefs.getString("MY_9_DIGIT_ID", "");
        String agency = prefs.getString("AGENCY_NAME", "SR Agency");

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("Host Center");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        root.addView(title);

        // Uporer Card - Nam + ID + Agency
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.VERTICAL);
        top.setPadding(30, 28, 30, 28);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1B1035"));
        bg.setCornerRadius(24);
        top.setBackground(bg);
        root.addView(top);

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(Color.WHITE);
        tvName.setTextSize(18);
        tvName.setTypeface(null, Typeface.BOLD);
        top.addView(tvName);

        TextView tvId = new TextView(this);
        tvId.setText("ID: " + id + "  |  Agency: " + agency);
        tvId.setTextColor(Color.parseColor("#FFD700"));
        tvId.setTextSize(13);
        top.addView(tvId);

        TextView tvBal = new TextView(this);
        tvBal.setText("My Dollar: $0  -  Work Data te dekhte parba");
        tvBal.setTextColor(Color.parseColor("#B0BEC5"));
        tvBal.setTextSize(12);
        tvBal.setPadding(0, 12, 0, 0);
        top.addView(tvBal);

        // Option 1 - Host Policy
        root.addView(makeRow("📜  Host Policy", "Official Policy 1-22 - Target + Salary", v -> {
            startActivity(new Intent(this, HostPolicyActivity.class));
        }));

        // Option 2 - Work Data / My Center
        root.addView(makeRow("📊  Work Data", "Koto Target hoiche, Koto Dollar hoiche - ekhane dekhba", v -> {
            startActivity(new Intent(this, HostWorkDataActivity.class));
        }));

        TextView note = new TextView(this);
        note.setText("\nHost Center e sudhu ei 2 ta option thakbe.");
        note.setTextColor(Color.parseColor("#B0BEC5"));
        note.setTextSize(12);
        note.setGravity(Gravity.CENTER);
        root.addView(note);

        setContentView(scroll);
    }

    private LinearLayout makeRow(String t1, String t2, android.view.View.OnClickListener click) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(30, 26, 30, 26);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1B1035"));
        bg.setCornerRadius(20);
        row.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 20, 0, 0);
        row.setLayoutParams(lp);
        row.setOnClickListener(click);
        TextView a = new TextView(this);
        a.setText(t1);
        a.setTextColor(Color.WHITE);
        a.setTextSize(16);
        a.setTypeface(null, Typeface.BOLD);
        row.addView(a);
        TextView b = new TextView(this);
        b.setText(t2);
        b.setTextColor(Color.parseColor("#B0BEC5"));
        b.setTextSize(12);
        row.addView(b);
        return row;
    }
}
