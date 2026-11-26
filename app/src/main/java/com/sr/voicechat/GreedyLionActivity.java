package com.sr.voicechat;

import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class GreedyLionActivity extends AppCompatActivity {

    TextView roundTxt, balanceTxt, statusTxt, resultTxt, myBetTxt, todayTxt;
    TextView[] betViews = new TextView[8];
    int[] bets = new int[8];
    int chip = 100000;
    int balance = 10000000;
    int round = 1;
    int timeLeft = 30;
    boolean bettingOpen = true;
    Handler handler = new Handler();
    String[] names = {"Strawberry","Chicken","Octopus","Corn","Shark","Cabbage","Grapes","Meat"};
    String[] emojis = {"🍓","🍗","🐙","🌽","🦈","🥬","🍇","🥩"};
    String[] mults = {"x5","x45","x25","x5","x15","x5","x5 Hot","x10"};
    int hotIndex = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(createUI());
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int)(getResources().getDisplayMetrics().heightPixels * 0.85));
            getWindow().setGravity(Gravity.BOTTOM);
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        startRound();
    }

    LinearLayout createUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF00C2E8);
        root.setPadding(16,16,16,16);

        TextView banner = new TextView(this);
        banner.setText("🦁 GREEDY LION • LIVE");
        banner.setTextSize(20);
        banner.setGravity(Gravity.CENTER);
        banner.setTextColor(0xFFFFD700);
        banner.setBackgroundColor(0xFF0A2A6B);
        banner.setPadding(10,24,10,24);
        root.addView(banner);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER);

        roundTxt = new TextView(this);
        roundTxt.setText("Round " + round);
        roundTxt.setTextSize(16);
        roundTxt.setTextColor(0xFFFFFFFF);
        roundTxt.setPadding(0,0,20,0);

        TextView music = new TextView(this);
        music.setText("🎵");
        music.setTextSize(20);
        music.setPadding(0,0,16,0);

        TextView help = new TextView(this);
        help.setText("?");
        help.setTextSize(20);
        help.setPadding(0,0,16,0);
        help.setTextColor(0xFFFFFFFF);

        TextView rank = new TextView(this);
        rank.setText("🏆");
        rank.setTextSize(20);

        top.addView(roundTxt);
        top.addView(music);
        top.addView(help);
        top.addView(rank);
        root.addView(top);

        balanceTxt = new TextView(this);
        balanceTxt.setText("Balance: " + balance + " Coins");
        balanceTxt.setGravity(Gravity.CENTER);
        balanceTxt.setTextColor(0xFFFFFFFF);
        balanceTxt.setTextSize(16);
        root.addView(balanceTxt);

        statusTxt = new TextView(this);
        statusTxt.setText("Tap a slot to place Bet");
        statusTxt.setGravity(Gravity.CENTER);
        statusTxt.setTextColor(0xFFFFFF00);
        root.addView(statusTxt);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        grid.setRowCount(3);

        for (int i = 0; i < 8; i++) {
            final int idx = i;
            TextView cell = new TextView(this);
            cell.setText(emojis[i] + "\n" + names[i] + "\n" + mults[i] + "\n0");
            cell.setGravity(Gravity.CENTER);
            cell.setTextSize(13);
            cell.setPadding(8,16,8,16);
            cell.setBackgroundColor(0xFFFFF3D6);
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = 0;
            p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            p.columnSpec = GridLayout.spec(i % 3, 1f);
            p.rowSpec = GridLayout.spec(i / 3);
            p.setMargins(6,6,6,6);
            cell.setLayoutParams(p);
            cell.setOnClickListener(v -> placeBet(idx));
            betViews[i] = cell;
            grid.addView(cell);
        }

        TextView lion = new TextView(this);
        lion.setText("🦁\n" + timeLeft);
        lion.setGravity(Gravity.CENTER);
        lion.setTextSize(22);
        lion.setBackgroundColor(0xFFFFD700);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.columnSpec = GridLayout.spec(1, 1f);
        lp.rowSpec = GridLayout.spec(1);
        lp.setMargins(6,6,6,6);
        lion.setLayoutParams(lp);
        grid.addView(lion);

        root.addView(grid);

        resultTxt = new TextView(this);
        resultTxt.setText("Result: No result yet");
        resultTxt.setTextColor(0xFFFFFFFF);
        root.addView(resultTxt);

        myBetTxt = new TextView(this);
        myBetTxt.setText("My Total Bet: 0 • Chip: 100K");
        myBetTxt.setTextColor(0xFFFFFFFF);
        root.addView(myBetTxt);

        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        String[] chipLabels = {"1K","5K","10K","100K"};
        int[] chipVals = {1000,5000,10000,100000};
        for (int i=0;i<4;i++){
            final int v = chipVals[i];
            final String label = chipLabels[i];
            Button b = new Button(this);
            b.setText(label);
            b.setOnClickListener(vv -> { chip = v; myBetTxt.setText("My Total Bet: " + getTotalBet() + " • Chip: " + label); });
            chips.addView(b);
        }
        root.addView(chips);

        todayTxt = new TextView(this);
        todayTxt.setText("Today's Earnings: 0");
        root.addView(todayTxt);

        Button auto = new Button(this);
        auto.setText("AUTO BET: OFF");
        auto.setOnClickListener(v -> Toast.makeText(this,"Auto Bet coming soon",Toast.LENGTH_SHORT).show());
        root.addView(auto);

        Button back = new Button(this);
        back.setText("BACK");
        back.setOnClickListener(v -> finish());
        root.addView(back);

        return root;
    }

    void placeBet(int idx){
        if(!bettingOpen){ Toast.makeText(this,"Wait for next Round",Toast.LENGTH_SHORT).show(); return; }
        if(balance < chip){ Toast.makeText(this,"Not enough coins",Toast.LENGTH_SHORT).show(); return; }
        bets[idx] += chip;
        balance -= chip;
        updateUI();
    }

    int getTotalBet(){
        int t=0; for(int b: bets) t+=b; return t;
    }

    void updateUI(){
        balanceTxt.setText("Balance: " + balance + " Coins");
        for(int i=0;i<8;i++){
            betViews[i].setText(emojis[i] + "\n" + names[i] + "\n" + mults[i] + "\n" + bets[i]);
        }
        myBetTxt.setText("My Total Bet: " + getTotalBet() + " • Chip: " + chip);
    }

    void startRound(){
        bettingOpen = true;
        timeLeft = 30;
        roundTxt.setText("Round " + round);
        statusTxt.setText("Tap a slot to place Bet");
        hotIndex = (hotIndex + 1) % 8;
        handler.postDelayed(new Runnable(){
            @Override public void run(){
                timeLeft--;
                if(timeLeft > 0){
                    handler.postDelayed(this, 1000);
                } else {
                    finishRound();
                }
            }
        }, 1000);
    }

    void finishRound(){
        bettingOpen = false;
        int winner = (int)(Math.random()*8);
        int winAmount = bets[winner] * getMult(winner);
        balance += winAmount;
        resultTxt.setText("Result: " + emojis[winner] + " " + names[winner] + " WIN " + winAmount);
        statusTxt.setText("No Bet - Wait for next Round");
        todayTxt.setText("Today's Earnings: " + winAmount);
        for(int i=0;i<8;i++) bets[i]=0;
        updateUI();
        round++;
        handler.postDelayed(() -> startRound(), 3000);
    }

    int getMult(int i){
        int[] m = {5,45,25,5,15,5,5,10};
        return m[i];
    }
}
