package com.sr.voicechat;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

public class FruitPartyActivity extends AppCompatActivity {

    TextView txtCoins, txtTimer, txtResult;
    LinearLayout layoutRooms;
    long myCoins = 1000;

    // FINAL 8 GHOR - Tomar Video Hisab
    String[] bangla = {"আপেল", "তরমুজ", "কলা", "স্ট্রবেরি", "বেগুন", "কিউই", "চেরি", "লেবু"};
    int[] multi = {40, 20, 10, 10, 5, 5, 5, 5};
    int[] weight = {5, 10, 20, 20, 35, 35, 35, 35};
    int[] myBets = new int[8];
    int selectedBet = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit_party);
        txtCoins = findViewById(R.id.txtCoins);
        txtTimer = findViewById(R.id.txtTimer);
        txtResult = findViewById(R.id.txtResult);
        layoutRooms = findViewById(R.id.layoutRooms);
        updateCoins();
        createRooms();
        startRound();
    }

    void updateCoins(){ txtCoins.setText("Coins: " + myCoins); }

    void createRooms(){
        layoutRooms.removeAllViews();
        for(int i=0;i<8;i++){
            int idx=i;
            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setPadding(16,16,16,16);
            box.setBackgroundResource(android.R.drawable.btn_default);
            TextView tvName = new TextView(this);
            tvName.setText(bangla[i] + " - " + multi[i] + "x");
            tvName.setTextSize(18);
            TextView tvMyBet = new TextView(this);
            tvMyBet.setText("My Bet: 0");
            Button btnBet = new Button(this);
            btnBet.setText("Bet " + selectedBet);
            btnBet.setOnClickListener(v -> placeBet(idx, tvMyBet));
            box.addView(tvName);
            box.addView(tvMyBet);
            box.addView(btnBet);
            layoutRooms.addView(box);
        }
        LinearLayout betRow = new LinearLayout(this);
        betRow.setOrientation(LinearLayout.HORIZONTAL);
        int[] amounts = {10, 50, 100, 500};
        for(int a: amounts){
            Button b = new Button(this);
            b.setText(a+"");
            b.setOnClickListener(v -> { selectedBet=a; createRooms(); });
            betRow.addView(b);
        }
        layoutRooms.addView(betRow);
    }

    int countBetRooms(){ int c=0; for(int b: myBets) if(b>0) c++; return c; }

    void placeBet(int idx, TextView tvMyBet){
        if(myBets[idx]==0 && countBetRooms()>=6){
            Toast.makeText(this, "Max 6 ta ghore bet korte parba!", Toast.LENGTH_LONG).show();
            return;
        }
        if(myCoins < selectedBet){ Toast.makeText(this, "Coins kom!", Toast.LENGTH_SHORT).show(); return; }
        myCoins -= selectedBet;
        myBets[idx] += selectedBet;
        tvMyBet.setText("My Bet: " + myBets[idx]);
        updateCoins();
    }

    void startRound(){
        Arrays.fill(myBets, 0);
        txtResult.setText("Bet koro! Max 6 ghore - 20 sec");
        new CountDownTimer(20000, 1000){
            public void onTick(long m){ txtTimer.setText("Time: " + m/1000 + "s"); }
            public void onFinish(){
                txtTimer.setText("Time Up!");
                int winIdx = pickWinner();
                txtResult.setText("WINNER: " + bangla[winIdx] + " - " + multi[winIdx] + "x");
                Toast.makeText(FruitPartyActivity.this, "Winner: " + bangla[winIdx], Toast.LENGTH_LONG).show();
                payWinner(winIdx);
                new android.os.Handler().postDelayed(() -> { createRooms(); startRound(); }, 3000);
            }
        }.start();
    }

    int pickWinner(){
        int total=0; for(int w: weight) total+=w;
        int r = new Random().nextInt(total);
        int sum=0;
        for(int i=0;i<8;i++){ sum+=weight[i]; if(r<sum) return i; }
        return 0;
    }

    void payWinner(int winIdx){
        int bet = myBets[winIdx];
        if(bet>0){
            int winAmount = bet * multi[winIdx];
            myCoins += winAmount;
            updateCoins();
            Toast.makeText(this, "Tumi jitla: " + winAmount + " coins!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Harla, porer round e try koro", Toast.LENGTH_SHORT).show();
        }
    }
}
