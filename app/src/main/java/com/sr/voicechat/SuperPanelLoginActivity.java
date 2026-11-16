package com.sr.voicechat;

import android.content.Intent;
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
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

// SUPER PANEL LOGIN - NOT TIED TO USER ID
// Owner can change ID anytime, Panel will still open with Gmail + Password
// No Chrome, No Website - All Inside App
public class SuperPanelLoginActivity extends AppCompatActivity {
    private EditText etGmail, etPassword;
    private TextView tvStatus;
    private DatabaseReference ownerRef;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        ownerRef = FirebaseDatabase.getInstance().getReference("ownerAuth");

        if (prefs.getBoolean("OWNER_LOGGED_IN", false)) {
            startActivity(new Intent(this, SuperPanelActivity.class));
            finish();
            return;
        }

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30, 40, 30, 30);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("🔐 OWNER LOGIN");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Super Panel opens with Owner Gmail + Password\nNot with your Game ID - ID can change, Owner never changes");
        sub.setTextColor(Color.parseColor("#B0BEC5"));
        sub.setTextSize(13);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 10, 0, 28);
        root.addView(sub);

        TextView lbl1 = new TextView(this);
        lbl1.setText("Owner Gmail");
        lbl1.setTextColor(Color.WHITE);
        lbl1.setTypeface(null, Typeface.BOLD);
        root.addView(lbl1);

        etGmail = new EditText(this);
        etGmail.setHint("Your Owner Gmail");
        etGmail.setHintTextColor(Color.parseColor("#757575"));
        etGmail.setTextColor(Color.WHITE);
        etGmail.setTextSize(15);
        etGmail.setPadding(24, 22, 24, 22);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1B1035"));
        bg.setCornerRadius(18);
        bg.setStroke(1, Color.parseColor("#FFD700"));
        etGmail.setBackground(bg);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp1.setMargins(0, 10, 0, 16);
        etGmail.setLayoutParams(lp1);
        root.addView(etGmail);

        TextView lbl2 = new TextView(this);
        lbl2.setText("Owner Password");
        lbl2.setTextColor(Color.WHITE);
        lbl2.setTypeface(null, Typeface.BOLD);
        root.addView(lbl2);

        etPassword = new EditText(this);
        etPassword.setHint("Your Owner Password");
        etPassword.setHintTextColor(Color.parseColor("#757575"));
        etPassword.setTextColor(Color.WHITE);
        etPassword.setTextSize(15);
        etPassword.setPadding(24, 22, 24, 22);
        etPassword.setBackground(bg);
        etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.setMargins(0, 10, 0, 20);
        etPassword.setLayoutParams(lp2);
        root.addView(etPassword);

        TextView btnLogin = new TextView(this);
        btnLogin.setText("Open Super Panel");
        btnLogin.setTextColor(Color.BLACK);
        btnLogin.setTypeface(null, Typeface.BOLD);
        btnLogin.setTextSize(16);
        btnLogin.setGravity(Gravity.CENTER);
        btnLogin.setPadding(20, 26, 20, 26);
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Color.parseColor("#FFD700"));
        btnBg.setCornerRadius(28);
        btnLogin.setBackground(btnBg);
        btnLogin.setOnClickListener(v -> doLogin());
        root.addView(btnLogin);

        TextView btnFirst = new TextView(this);
        btnFirst.setText("First Time? Set Owner Gmail + Password");
        btnFirst.setTextColor(Color.parseColor("#00B0FF"));
        btnFirst.setGravity(Gravity.CENTER);
        btnFirst.setPadding(0, 20, 0, 0);
        btnFirst.setOnClickListener(v -> doFirstSetup());
        root.addView(btnFirst);

        tvStatus = new TextView(this);
        tvStatus.setText("Status: Enter Owner Gmail + Password");
        tvStatus.setTextColor(Color.parseColor("#FFD700"));
        tvStatus.setGravity(Gravity.CENTER);
        tvStatus.setPadding(0, 20, 0, 0);
        root.addView(tvStatus);

        setContentView(scroll);
    }

    private void doLogin() {
        String gmail = etGmail.getText().toString().trim().toLowerCase();
        String pass = etPassword.getText().toString().trim();
        if (gmail.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Enter Gmail + Password", Toast.LENGTH_SHORT).show();
            return;
        }
        tvStatus.setText("Status: Checking...");
        ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) {
                    tvStatus.setText("Status: No Owner Set Yet - Click First Time Setup");
                    Toast.makeText(SuperPanelLoginActivity.this, "No Owner Yet - First Setup First", Toast.LENGTH_LONG).show();
                    return;
                }
                String savedGmail = snap.child("ownerGmail").getValue() != null ? String.valueOf(snap.child("ownerGmail").getValue()).toLowerCase() : "";
                String savedPass = snap.child("ownerPassword").getValue() != null ? String.valueOf(snap.child("ownerPassword").getValue()) : "";
                if (gmail.equals(savedGmail) && pass.equals(savedPass)) {
                    prefs.edit().putBoolean("OWNER_LOGGED_IN", true).putString("OWNER_GMAIL", gmail).apply();
                    Toast.makeText(SuperPanelLoginActivity.this, "Welcome Owner - Panel Opening", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SuperPanelLoginActivity.this, SuperPanelActivity.class));
                    finish();
                } else {
                    tvStatus.setText("Status: Wrong Gmail or Password");
                    Toast.makeText(SuperPanelLoginActivity.this, "Wrong Gmail or Password", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void doFirstSetup() {
        String gmail = etGmail.getText().toString().trim().toLowerCase();
        String pass = etPassword.getText().toString().trim();
        if (gmail.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Enter Gmail + New Password First", Toast.LENGTH_SHORT).show();
            return;
        }
        ownerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists() && snap.child("ownerGmail").getValue() != null) {
                    tvStatus.setText("Status: Owner Already Set - Login Only");
                    Toast.makeText(SuperPanelLoginActivity.this, "Owner Already Set - Use Login", Toast.LENGTH_LONG).show();
                    return;
                }
                Map<String, Object> map = new HashMap<>();
                map.put("ownerGmail", gmail);
                map.put("ownerPassword", pass);
                map.put("createdAt", System.currentTimeMillis());
                ownerRef.setValue(map).addOnCompleteListener(t -> {
                    prefs.edit().putBoolean("OWNER_LOGGED_IN", true).putString("OWNER_GMAIL", gmail).apply();
                    Toast.makeText(SuperPanelLoginActivity.this, "Owner Setup Done - Panel Opening", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(SuperPanelLoginActivity.this, SuperPanelActivity.class));
                    finish();
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
}
