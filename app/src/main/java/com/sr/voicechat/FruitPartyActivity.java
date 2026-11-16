package com.sr.voicechat;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;
import java.util.*;

public class FruitPartyActivity extends AppCompatActivity {

    TextView tvCountdown, tvJackpot, tvMine, tvTodayWin, tvResultHistory, tvRound;
    DatabaseReference userRef, gameRef;
    String userId = "user_123";
    long myCoins = 0;
    long todayWin = 0;
    long jackpot = 25374655;
    long selectedChip = 1000;
    long[] bets = new long[8];
    int[] odds = {5, 5, 5, 40, 5, 20, 10, 10};
    String[] names = {"Lemon","Raspberry","Kiwi","Apple","Plum","Watermelon","Banana","Strawberry"};
    int round = 1102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_fruit_party);

        tvCountdown = findViewById(R.id.tvCountdown);
        tvJackpot = findViewById(R.id.tvJackpot);
        tvMine = findViewById(R.id.tvMine);
        tvTodayWin = findViewById(R.id.tvTodayWin);
        tvResultHistory = findViewById(R.id.tvResultHistory);
        tvRound = findViewById(R.id.tvRound);

        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        gameRef = FirebaseDatabase.getInstance().getReference("fruitParty").child("currentRound");

        userRef.child("coins").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snap) {
                if (snap.getValue() != null) {
                    myCoins = snap.getValue(Long.class);
                    tvMine.setText("Mine: " + myCoins);
                }
            }
            @Override public void onCancelled(DatabaseError e) {}
        });

        findViewById(R.id.btnChip1K).setOnClickListener(v -> selectedChip = 1000);
        findViewById(R.id.btnChip10K).setOnClickListener(v -> selectedChip = 10000);
        findViewById(R.id.btnChip100K).setOnClickListener(v -> selectedChip = 100000);
        findViewById(R.id.btnChip1M).setOnClickListener(v -> selectedChip = 1000000);

        int[] cellIds = {R.id.cellLemon, R.id.cellRaspberry, R.id.cellKiwi, R.id.cellApple,
                         R.id.cellPlum, R.id.cellWatermelon, R.id.cellBanana, R.id.cellStrawberry};
        for (int i = 0; i < 8; i++) {
            int idx = i;
            findViewById(cellIds[i]).setOnClickListener(v -> {
                if (myCoins < selectedChip) {
                    Toast.makeText(this, "Balance kom!", Toast.LENGTH_SHORT).show();
                    return;
                }
                int betCount = 0;
                for (long b : bets) if (b > 0) betCount++;
                if (bets[idx] == 0 && betCount >= 6) {
                    Toast.makeText(this, "Max 6 ta ghore bet!", Toast.LENGTH_SHORT).show();
                    return;
                }
                bets[idx] += selectedChip;
                long newCoins = myCoins - selectedChip;
                userRef.child("coins").setValue(newCoins);
                gameRef.child("jackpot").setValue(jackpot + selectedChip / 10);
                Toast.makeText(this, names[idx] + " e " + selectedChip + " bet! Coin katlo.", Toast.LENGTH_SHORT).show();
            });
        }

        findViewById(R.id.btnAuto).setOnClickListener(v -> {
            Toast.makeText(this, "Auto On", Toast.LENGTH_SHORT).show();
        });

        startRound();
    }

    void startRound() {
        new CountDownTimer(20000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }
            public void onFinish() {
                int winIndex = new Random().nextInt(8);
                long winAmount = bets[winIndex] * odds[winIndex];
                if (winAmount > 0) {
                    long newCoins = myCoins + winAmount;
                    userRef.child("coins").setValue(newCoins);
                    todayWin += winAmount;
                    tvTodayWin.setText("Today Win: " + todayWin);
                    Toast.makeText(FruitPartyActivity.this, "WIN! " + names[winIndex] + " - " + winAmount, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(FruitPartyActivity.this, "Result: " + names[winIndex] + " - Lose, Coin katlo", Toast.LENGTH_SHORT).show();
                }
                tvResultHistory.setText("Result: " + names[winIndex] + " | Win: " + winAmount);
                Arrays.fill(bets, 0);
                round++;
                tvRound.setText("Round: " + round);
                startRound();
            }
        }.start();
    }
}
