package com.sr.voicechat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
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
import java.text.NumberFormat;
import java.util.Locale;

public class HostWorkDataActivity extends AppCompatActivity {
    private TextView tvReceived, tvLevel, tvSalary, tvNext;

    // Tomar Policy - HostPolicyActivity er sathe milano
    private final long[] TARGETS = {288000L,588000L,1190000L,1780000L,3280000L,5780000L,7880000L,9980000L,13850000L,18850000L,26500000L,34500000L,43500000L,56500000L,76500000L,88500000L,115500000L,155000000L,210000000L,288000000L,350000000L,460000000L};
    private final String[] SALARY = {"$1","$2","$4.5","$7","$14","$24","$32","$40","$56","$76","$104","$132","$168","$220","$292","$348","$440","$584","$752","$1024","$1240","$1600"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 24, 24, 24);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("📊 Work Data");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        root.addView(title);

        tvReceived = makeBig(root, "My Received: Loading...");
        tvLevel = makeBig(root, "My Level: -");
        tvSalary = makeBig(root, "My Salary: $0");
        tvNext = makeBig(root, "Next Target: -");

        TextView note = new TextView(this);
        note.setText("\n• Ekhane sudhu tomar nijer Target dekhba\n• Agency Commission ekhane dekhba na");
        note.setTextColor(Color.parseColor("#B0BEC5"));
        note.setTextSize(12);
        note.setGravity(Gravity.CENTER);
        root.addView(note);

        setContentView(scroll);
        loadData();
    }

    private TextView makeBig(LinearLayout root, String t) {
        TextView tv = new TextView(this);
        tv.setText(t);
        tv.setTextColor(Color.parseColor("#FFD700"));
        tv.setTextSize(16);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(24, 24, 24, 24);
        tv.setBackgroundColor(Color.parseColor("#1B1035"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 16);
        tv.setLayoutParams(lp);
        root.addView(tv);
        return tv;
    }

    private void loadData() {
        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        if (fu == null) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(fu.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                long received = 0;
                if (snap.hasChild("totalReceivedCoins") && snap.child("totalReceivedCoins").getValue() != null) {
                    try { received = Long.parseLong(String.valueOf(snap.child("totalReceivedCoins").getValue())); } catch (Exception ignored) {}
                }
                int myLevel = 0;
                String mySalary = "$0";
                for (int i = 0; i < TARGETS.length; i++) {
                    if (received >= TARGETS[i]) { myLevel = i + 1; mySalary = SALARY[i]; }
                }
                String fmt = NumberFormat.getNumberInstance(Locale.US).format(received);
                tvReceived.setText("My Received: " + fmt + " Coins");
                tvLevel.setText("My Level: " + (myLevel == 0 ? "No Level Yet" : "Level " + myLevel));
                tvSalary.setText("My Salary: " + mySalary);
                if (myLevel < 22) {
                    long nextTarget = TARGETS[myLevel];
                    long left = nextTarget - received;
                    tvNext.setText("Next Target: Level " + (myLevel + 1) + " - " + NumberFormat.getNumberInstance(Locale.US).format(nextTarget) + " Coins\nBaki: " + NumberFormat.getNumberInstance(Locale.US).format(left) + " Coins");
                } else {
                    tvNext.setText("Next Target: Max Level Done! 🎉");
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }
}
