package com.sr.voicechat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import java.util.*;

public class FruitPartyActivity extends AppCompatActivity {
    private TextView txtCoins, txtResult, txtHistory;
    private GridView fruitGrid;
    private Button btnBet10, btnBet50, btnBet100, btnSpin;
    private SharedPreferences prefs;
    private DatabaseReference userRef, historyRef;
    private String myId = "", firebaseUid = "";
    private int selectedBet = 10;
    private boolean isSpinning = false;
    private String[] fruits = {"🍎","🍊","🍋","🍇","🍓","🍒"};
    private int[] fruitMultipliers = {2, 3, 5, 8, 10, 20};

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit_party);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        myId = prefs.getString("MY_9_DIGIT_ID", "");
        firebaseUid = prefs.getString("FIREBASE_UID", "");
        txtCoins = findViewById(R.id.txtCoins);
        txtResult = findViewById(R.id.txtResult);
        txtHistory = findViewById(R.id.txtHistory);
        btnBet10 = findViewById(R.id.btnBet10);
        btnBet50 = findViewById(R.id.btnBet50);
        btnBet100 = findViewById(R.id.btnBet100);
        btnSpin = findViewById(R.id.btnSpin);

        if (firebaseUid.isEmpty()) { Toast.makeText(this, "Login করো", Toast.LENGTH_SHORT).show(); finish(); return; }
        userRef = FirebaseDatabase.getInstance().getReference("users").child(firebaseUid);
        historyRef = FirebaseDatabase.getInstance().getReference("game_history").child(firebaseUid);

        // LIVE COIN - Firebase থেকে আসবে, ফোন থেকে না
        userRef.child("coins").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot s) {
                Long c = s.getValue(Long.class);
                if (c == null) c = 0L;
                txtCoins.setText("🪙 Coins: " + c);
            }
            @Override public void onCancelled(DatabaseError e) {}
        });

        // HISTORY - শেষ 5টা খেলা
        historyRef.limitToLast(5).addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot s) {
                StringBuilder sb = new StringBuilder("📜 History:\n");
                for (DataSnapshot d : s.getChildren()) {
                    String f = d.child("fruit").getValue(String.class);
                    Long bet = d.child("bet").getValue(Long.class);
                    Long win = d.child("win").getValue(Long.class);
                    String res = d.child("result").getValue(String.class);
                    sb.append(f).append(" Bet:").append(bet).append(" -> ").append(res).append(" Win:").append(win).append("\n");
                }
                txtHistory.setText(sb.toString());
            }
            @Override public void onCancelled(DatabaseError e) {}
        });

        btnBet10.setOnClickListener(v -> { selectedBet = 10; txtResult.setText("Bet: 10"); });
        btnBet50.setOnClickListener(v -> { selectedBet = 50; txtResult.setText("Bet: 50"); });
        btnBet100.setOnClickListener(v -> { selectedBet = 100; txtResult.setText("Bet: 100"); });
        btnSpin.setOnClickListener(v -> playSecureGame());
    }

    private void playSecureGame() {
        if (isSpinning) return;
        isSpinning = true;
        txtResult.setText("🎲 Spinning...");

        // 1. RESULT - Random, Server Logic এর মত - User ঠিক করতে পারবে না
        Random random = new Random();
        int fruitIndex = random.nextInt(fruits.length);
        // 45% Win, 55% Loss - Company এর লাভ থাকবে
        boolean isWin = random.nextInt(100) < 45;
        int winAmount = 0;
        if (isWin) winAmount = selectedBet * fruitMultipliers[fruitIndex];

        int finalWin = winAmount;
        boolean finalIsWin = isWin;
        int finalFruit = fruitIndex;

        // 2. TRANSACTION - Company এর খাতায় একসাথে কাটা + জমা
        userRef.child("coins").runTransaction(new Transaction.Handler() {
            @Override public Transaction.Result doTransaction(MutableData currentData) {
                Long coins = currentData.getValue(Long.class);
                if (coins == null) coins = 0L;
                if (coins < selectedBet) return Transaction.abort(); // টাকা নাই, খেলতে পারবে না
                coins = coins - selectedBet; // Bet কাটা
                if (finalIsWin) coins = coins + finalWin; // জিতলে জমা
                currentData.setValue(coins);
                return Transaction.success(currentData);
            }
            @Override public void onComplete(DatabaseError error, boolean committed, DataSnapshot s) {
                isSpinning = false;
                if (!committed) {
                    Toast.makeText(FruitPartyActivity.this, "Coins কম আছে!", Toast.LENGTH_SHORT).show();
                    txtResult.setText("Coins কম আছে");
                    return;
                }
                // 3. HISTORY - Firebase এ লিখে রাখা, User মুছতে পারবে না
                Map<String, Object> h = new HashMap<>();
                h.put("fruit", fruits[finalFruit]);
                h.put("bet", selectedBet);
                h.put("win", finalWin);
                h.put("result", finalIsWin ? "WIN" : "LOSS");
                h.put("time", System.currentTimeMillis());
                h.put("userId", myId);
                historyRef.push().setValue(h);

                if (finalIsWin) txtResult.setText("🎉 WIN! " + fruits[finalFruit] + " +" + finalWin);
                else txtResult.setText("😢 LOSS! " + fruits[finalFruit] + " -" + selectedBet);
            }
        });
    }
}
