package com.sr.voicechat;

import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GreedyLionActivity extends AppCompatActivity {
    // NO NAME, NO ID, NO SEAT - Global Game Only
    private String myId = "";
    private long myBalance = 0;
    private int selectedChip = 1000;
    private long[] bets = new long[8];
    private long totalBet = 0;
    private long todayEarn = 0;

    private String[] names = {"Chicken","Octopus","Shark","Meat","Grapes","Cabbage","Corn","Strawberry"};
    private String[] emojis = {"🍗","🐙","🦈","🥩","🍇","🥬","🌽","🍓"};
    private double[] mult = {45,25,15,10,5,5,5,5};

    private TextView tvBalance, tvTimer, tvResult, tvMyBet, tvEarn;
    private LinearLayout resultRow;
    private Button[] slotBtns = new Button[8];
    private TextView[] slotBetTv = new TextView[8];
    private Button[] chipBtns = new Button[4];
    private Handler handler = new Handler();
    private int timerSec = 30;
    private boolean bettingOpen = true;
    private boolean spinning = false;
    private boolean autoBet = false;
    private int roundNo = 0;
    private Random rnd = new Random();
    private List<Integer> history = new ArrayList<>();
    private int hotSlot = 4;

    private long getLongVal(DataSnapshot s, String k) {
        try { Object v = s.child(k).getValue(); if(v==null) return 0;
            if(v instanceof Number) return ((Number)v).longValue();
            return Long.parseLong(String.valueOf(v).replace(",","").trim());
        } catch(Exception e){ return 0; }
    }
    private String fmt(long v){ if(v>=1000) return (v/1000)+"K"; return String.valueOf(v); }
    private String fmtMult(double v){ if(v==(int)v) return "x"+(int)v; return "x"+v; }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.content.SharedPreferences prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        // ID sudhu database er jonno background e thakbe, screen e dekhabe NA
        myId = prefs.getString("PROFILE_ID","");
        if(myId==null||myId.isEmpty()) myId = prefs.getString("MY_9DIGIT_ID","");

        // POPUP - 75% to 85% - niche theke uthbe, full screen NA
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF1CB5E0);
        root.setPadding(12,12,12,12);

        // BANNER - Full Premium Banner, video moto
        TextView banner = new TextView(this);
        banner.setText("GREEDY LION  •  LIVE");
        banner.setTextSize(22); banner.setTextColor(0xFFFFD700);
        banner.setTypeface(null, android.graphics.Typeface.BOLD);
        banner.setGravity(Gravity.CENTER);
        banner.setPadding(0,8,0,8);
        banner.setBackgroundColor(0xFF0B3D91);
        root.addView(banner);

        // TOP BAR - Sudhu Round + Cup + Music + ?
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView tvRound = new TextView(this);
        tvRound.setText("Round: " + roundNo);
        tvRound.setTextSize(16); tvRound.setTextColor(0xFFFFFFFF);
        tvRound.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams lpr = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        top.addView(tvRound, lpr);
        Button btnMusic = new Button(this); btnMusic.setText("🎵");
        btnMusic.setOnClickListener(v -> Toast.makeText(this,"Sound On / Off",Toast.LENGTH_SHORT).show());
        top.addView(btnMusic);
        Button btnHelp = new Button(this); btnHelp.setText("?");
        btnHelp.setOnClickListener(v -> Toast.makeText(this,"How to Play",Toast.LENGTH_SHORT).show());
        top.addView(btnHelp);
        Button btnRank = new Button(this); btnRank.setText("🏆"); // Sudhu Cup, Ranking lekha NA
        btnRank.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFC107));
        btnRank.setOnClickListener(v -> Toast.makeText(this,"Ranking - Top Winners",Toast.LENGTH_SHORT).show());
        top.addView(btnRank);
        root.addView(top);

        // Balance - Sudhu Coin, kono Name / ID NA
        tvBalance = new TextView(this);
        tvBalance.setText("Balance: Loading...");
        tvBalance.setTextSize(17); tvBalance.setTextColor(0xFFFFFFFF);
        tvBalance.setTypeface(null, android.graphics.Typeface.BOLD);
        tvBalance.setGravity(Gravity.CENTER);
        root.addView(tvBalance);

        tvResult = new TextView(this);
        tvResult.setText("Tap a slot to place Bet");
        tvResult.setTextSize(14); tvResult.setTextColor(0xFFFFFFFF);
        tvResult.setGravity(Gravity.CENTER);
        root.addView(tvResult);

        // WHEEL
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3); grid.setRowCount(3);
        int[] order = {7,0,1,6,8,2,5,4,3};
        for(int idx=0; idx<9; idx++){
            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.VERTICAL);
            cell.setGravity(Gravity.CENTER); cell.setPadding(4,4,4,4);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width=0; lp.height=GridLayout.LayoutParams.WRAP_CONTENT;
            lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
            lp.rowSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);
            lp.setMargins(3,3,3,3);
            if(idx==4){
                cell.setBackgroundColor(0xFFFFD700);
                TextView lion = new TextView(this); lion.setText("🦁"); lion.setTextSize(48); lion.setGravity(Gravity.CENTER); cell.addView(lion);
                tvTimer = new TextView(this); tvTimer.setText("30"); tvTimer.setTextSize(20);
                tvTimer.setTypeface(null, android.graphics.Typeface.BOLD); tvTimer.setGravity(Gravity.CENTER); tvTimer.setBackgroundColor(0xFFFFFFFF); cell.addView(tvTimer);
            } else {
                int slot = order[idx];
                cell.setBackgroundColor(0xFFFFF3D6);
                TextView topRow = new TextView(this);
                topRow.setText(emojis[slot] + "  " + fmtMult(mult[slot]) + (slot==hotSlot ? " Hot" : ""));
                topRow.setTextSize(15); topRow.setTypeface(null, android.graphics.Typeface.BOLD);
                topRow.setTextColor(slot==hotSlot ? 0xFFE91E63 : 0xFFB8860B);
                topRow.setGravity(Gravity.CENTER); cell.addView(topRow);
                TextView n = new TextView(this); n.setText(names[slot]); n.setTextSize(10);
                n.setTypeface(null, android.graphics.Typeface.BOLD); n.setGravity(Gravity.CENTER); cell.addView(n);
                TextView betTv = new TextView(this); betTv.setText("0"); betTv.setTextSize(11); betTv.setGravity(Gravity.CENTER); cell.addView(betTv); slotBetTv[slot]=betTv;
                Button b = new Button(this); b.setText(""); // BET lekha NA
                b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF9800));
                final int fSlot = slot;
                b.setOnClickListener(v -> placeBet(fSlot));
                cell.addView(b); slotBtns[slot]=b;
            }
            grid.addView(cell, lp);
        }
        root.addView(grid);

        TextView tvResLbl = new TextView(this); tvResLbl.setText("Result");
        tvResLbl.setTextSize(13); tvResLbl.setTextColor(0xFFFFFFFF);
        tvResLbl.setTypeface(null, android.graphics.Typeface.BOLD); root.addView(tvResLbl);
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        resultRow = new LinearLayout(this); resultRow.setOrientation(LinearLayout.HORIZONTAL);
        hsv.addView(resultRow); root.addView(hsv); updateHistoryUI();

        tvMyBet = new TextView(this); tvMyBet.setText("My Total Bet: 0  •  Chip: 1K");
        tvMyBet.setTextSize(14); tvMyBet.setTextColor(0xFFFFFFFF);
        tvMyBet.setTypeface(null, android.graphics.Typeface.BOLD); tvMyBet.setGravity(Gravity.CENTER); root.addView(tvMyBet);

        LinearLayout chipRow = new LinearLayout(this); chipRow.setOrientation(LinearLayout.HORIZONTAL); chipRow.setGravity(Gravity.CENTER);
        int[] chips = {1000,5000,10000,100000}; String[] chipLabels = {"1K","5K","10K","100K"};
        for(int i=0;i<chips.length;i++){
            int c = chips[i]; Button cb = new Button(this); cb.setText(chipLabels[i]);
            cb.setBackgroundTintList(android.content.res.ColorStateList.valueOf(c==selectedChip?0xFFFF9800:0xFFE91E63));
            cb.setTextColor(0xFFFFFFFF); cb.setTypeface(null, android.graphics.Typeface.BOLD);
            final int fi=i; cb.setOnClickListener(v -> {
                selectedChip=c; tvMyBet.setText("My Total Bet: "+totalBet+"  •  Chip: "+chipLabels[fi]);
                for(Button bb:chipBtns) bb.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE91E63));
                cb.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF9800));
            });
            chipBtns[i]=cb; chipRow.addView(cb);
        }
        root.addView(chipRow);

        LinearLayout bottom = new LinearLayout(this); bottom.setOrientation(LinearLayout.HORIZONTAL); bottom.setGravity(Gravity.CENTER);
        tvEarn = new TextView(this); tvEarn.setText("Today's Earnings: 0");
        tvEarn.setTextSize(12); tvEarn.setTextColor(0xFFFFFFFF);
        tvEarn.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams lpe = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        bottom.addView(tvEarn, lpe);
        Button btnAuto = new Button(this); btnAuto.setText("Auto Bet: OFF");
        btnAuto.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));
        btnAuto.setTextColor(0xFFFFFFFF);
        btnAuto.setOnClickListener(v -> { autoBet=!autoBet; btnAuto.setText(autoBet?"Auto Bet: ON":"Auto Bet: OFF"); });
        bottom.addView(btnAuto);
        Button btnBack = new Button(this); btnBack.setText("Back");
        btnBack.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF222222));
        btnBack.setTextColor(0xFFFFFFFF); btnBack.setOnClickListener(v -> finish()); bottom.addView(btnBack);
        root.addView(bottom);

        setContentView(root);
        loadWallet();
        startTimer();
    }

    private void updateHistoryUI(){
        resultRow.removeAllViews();
        if(history.isEmpty()){ TextView t=new TextView(this); t.setText("No result yet"); t.setTextColor(0xFFFFFFFF); resultRow.addView(t); return; }
        for(int i=history.size()-1;i>=0;i--){ int h=history.get(i); TextView t=new TextView(this); t.setText(emojis[h]); t.setTextSize(24); t.setPadding(10,4,10,4); t.setBackgroundColor(0xFFFFFFFF); resultRow.addView(t); }
    }
    private void loadWallet(){
        if(myId==null||myId.isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(myId);
        ref.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override public void onDataChange(@NonNull DataSnapshot s){
                long m=0; m=Math.max(m,getLongVal(s,"coins")); m=Math.max(m,getLongVal(s,"coinBalance"));
                myBalance=m; tvBalance.setText("Balance: " + myBalance + " Coins");
            }
            @Override public void onCancelled(@NonNull DatabaseError e){}
        });
    }
    private void placeBet(int slot){
        if(!bettingOpen){ Toast.makeText(this,"Betting Closed! Wait",Toast.LENGTH_SHORT).show(); return; }
        if(spinning) return;
        if(myBalance < selectedChip + totalBet){ Toast.makeText(this,"Not enough Coins!",Toast.LENGTH_SHORT).show(); return; }
        bets[slot]+=selectedChip; totalBet+=selectedChip;
        slotBetTv[slot].setText(fmt(bets[slot]));
        tvMyBet.setText("My Total Bet: " + totalBet + "  •  Chip: " + fmt(selectedChip));
        tvResult.setText(names[slot] + " (" + fmtMult(mult[slot]) + ") - Bet placed: " + fmt(selectedChip));
    }
    private void startTimer(){
        timerSec=30; bettingOpen=true;
        handler.postDelayed(new Runnable(){ @Override public void run(){
            if(timerSec>0){ tvTimer.setText(String.valueOf(timerSec)); timerSec--; handler.postDelayed(this,1000); }
            else { tvTimer.setText("..."); bettingOpen=false; spinWheel(); }
        }},0);
    }
    private void spinWheel(){
        if(totalBet==0){ tvResult.setText("No Bet - Wait for next Round"); handler.postDelayed(() -> resetRound(), 2000); return; }
        spinning=true; int winner=rnd.nextInt(8); int steps=20+winner;
        handler.postDelayed(new Runnable(){ int step=0; @Override public void run(){
            for(int i=0;i<8;i++) if(slotBtns[i]!=null) slotBtns[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF9800));
            int pos=step%8; if(slotBtns[pos]!=null) slotBtns[pos].setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF00FF00));
            step++; if(step<=steps) handler.postDelayed(this, step<15?120:260); else finishRound(pos);
        }},120);
    }
    private void finishRound(int winner){
        spinning=false; history.add(winner); if(history.size()>8) history.remove(0); updateHistoryUI();
        long betOnWin=bets[winner]; long winAmount=0;
        if(betOnWin>0){
            winAmount=(long)(betOnWin*mult[winner]);
            myBalance=myBalance-totalBet+winAmount; todayEarn+=winAmount-betOnWin;
            tvResult.setText("WIN! " + names[winner] + " " + fmtMult(mult[winner]) + " - +" + winAmount);
        } else {
            myBalance=myBalance-totalBet; todayEarn-=totalBet;
            tvResult.setText(names[winner] + " - " + totalBet);
        }
        tvBalance.setText("Balance: " + myBalance + " Coins");
        tvEarn.setText("Today's Earnings: " + todayEarn);
        updateWallet(); handler.postDelayed(() -> resetRound(), 4000);
    }
    private void updateWallet(){
        if(myId==null||myId.isEmpty()) return;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(myId);
        Map<String,Object> m=new HashMap<>(); m.put("coins",myBalance); m.put("coinBalance",myBalance); m.put("walletCoins",myBalance); m.put("sendingCoins",myBalance); ref.updateChildren(m);
    }
    private void resetRound(){
        roundNo++; hotSlot = rnd.nextInt(8);
        long[] oldBets=bets.clone(); totalBet=0;
        for(int i=0;i<8;i++){ bets[i]=0; if(slotBetTv[i]!=null) slotBetTv[i].setText("0"); }
        if(autoBet){
            for(int i=0;i<8;i++) if(oldBets[i]>0){ bets[i]=oldBets[i]; totalBet+=oldBets[i]; slotBetTv[i].setText(fmt(bets[i])); }
            tvMyBet.setText("My Total Bet: " + totalBet + " (Auto)");
        } else tvMyBet.setText("My Total Bet: 0  •  Chip: " + fmt(selectedChip));
        startTimer();
    }
    @Override protected void onDestroy(){ super.onDestroy(); handler.removeCallbacksAndMessages(null); }
}
