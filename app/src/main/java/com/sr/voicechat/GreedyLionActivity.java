package com.sr.voicechat;

import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class GreedyLionActivity extends AppCompatActivity {

    TextView roundTxt, balanceTxt, walletNameTxt, statusTxt, resultTxt, todayTxt, lionTimerTxt;
    TextView userBetNameTxt, userBetCoinsTxt, userBetTotalTxt, rankingPointsTxt;
    TextView[] betViews = new TextView[8];
    TextView[] chipViews = new TextView[4];
    LinearLayout[] boothViews = new LinearLayout[8];
    int[] bets = new int[8];
    int chip = 1000;
    int selectedChipIndex = 0;
    int balance = 141;
    int myBetThisRound = 0; // Upore box e sudhu plus hobe - Real Balance na
    int rankingPoints = 0; // Ranking er jonno
    int totalToday = 0;
    String userName = "Chanchal";
    int round = 926;
    int timeLeft = 30;
    boolean bettingOpen = true;
    int lightIndex = 0;
    Handler handler = new Handler();
    Handler lightHandler = new Handler();
    FrameLayout rootFrame;
    View drawingOverlay, resultPopup, rankingPopup, introPopup, winBanner, historyPopup;
    ArrayList<String> myHistoryList = new ArrayList<>();

    String[] names = {"Chicken","Octopus","Shark","Meat","Grapes","Cabbage","Corn","Strawberry"};
    String[] emojis = {"🍗","🐙","🦈","🥩","🍇","🥬","🌽","🍓"};
    String[] mults = {"x45","x25","x15","x10","x5","x5","x5","x5"};
    // 0-3 Meat, 4-7 Veg - Salad/Pizza Rule er jonno
    boolean[] isMeat = {true,true,true,true,false,false,false,false};
    int hotIndex = 4;
    String[] history = {"🍓","🍇","🌽","🥩","🍓","🦈","🌽","🍓"};

    GradientDrawable roundBg(int color, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); return d; }
    GradientDrawable roundStroke(int color, int strokeColor, int strokeW, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setStroke(strokeW, strokeColor); d.setCornerRadius(radius); return d; }
    GradientDrawable circleBg(int color) { GradientDrawable d = new GradientDrawable(); d.setShape(GradientDrawable.OVAL); d.setColor(color); return d; }
    GradientDrawable circleStroke(int color, int strokeColor, int strokeW) { GradientDrawable d = new GradientDrawable(); d.setShape(GradientDrawable.OVAL); d.setColor(color); d.setStroke(strokeW, strokeColor); return d; }

    void loadWallet() {
        SharedPreferences prefs = getSharedPreferences("user_wallet", MODE_PRIVATE);
        userName = prefs.getString("user_name", "Chanchal");
        balance = prefs.getInt("wallet_coins", 141);
        rankingPoints = prefs.getInt("ranking_points", 0);
        totalToday = prefs.getInt("total_today", 0);
    }
    void saveWallet() {
        getSharedPreferences("user_wallet", MODE_PRIVATE).edit().putInt("wallet_coins", balance).putString("user_name", userName).putInt("ranking_points", rankingPoints).putInt("total_today", totalToday).apply();
        updateWalletUI();
    }
    void updateWalletUI() {
        if (walletNameTxt != null) walletNameTxt.setText(userName);
        if (balanceTxt != null) balanceTxt.setText("" + balance);
        // Uporer Box - User Profile + My Bet - Sudhu Plus Hobe
        if (userBetNameTxt != null) userBetNameTxt.setText(userName);
        if (userBetCoinsTxt != null) userBetCoinsTxt.setText("🪙 " + myBetThisRound);
        if (userBetTotalTxt != null) userBetTotalTxt.setText("My Bet: " + myBetThisRound);
        if (todayTxt != null) todayTxt.setText("🪙 " + totalToday);
        if (rankingPointsTxt != null) rankingPointsTxt.setText("Points: " + rankingPoints);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadWallet();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        rootFrame = new FrameLayout(this);
        rootFrame.addView(createUI());
        setContentView(rootFrame);
        updateWalletUI(); updateChipUI();
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int)(getResources().getDisplayMetrics().heightPixels * 0.92));
            getWindow().setGravity(Gravity.BOTTOM);
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        startRound(); startLight();
    }

    void updateChipUI(){
        for(int i=0;i<4;i++){
            if(chipViews[i]==null) continue;
            if(i==selectedChipIndex) chipViews[i].setBackground(roundStroke(0xFFFFD700, 0xFFFFFFFF, 6, 50));
            else if(i==0) chipViews[i].setBackground(roundBg(0xFFDAA520, 50));
            else chipViews[i].setBackground(roundBg(0xFFFF69B4, 50));
        }
    }

    void startLight() {
        lightHandler.postDelayed(new Runnable(){
            @Override public void run(){
                lightIndex = (lightIndex + 1) % 8;
                for(int i=0;i<8;i++){
                    if(boothViews[i]==null) continue;
                    if(i==lightIndex) boothViews[i].setBackground(roundStroke(0xFFFFFF99, 0xFFFFD700, 8, 60));
                    else boothViews[i].setBackground(roundBg(0xFFFFF3D6, 60));
                }
                int speed = timeLeft <= 5 ? 80 : 300;
                lightHandler.postDelayed(this, speed);
            }
        }, 300);
    }

    FrameLayout.LayoutParams popupParams(){
        return new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    void showDrawing() {
        if(drawingOverlay != null) return;
        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL); lay.setGravity(Gravity.CENTER);
        lay.setBackgroundColor(0x88000000); lay.setLayoutParams(popupParams());
        TextView t = new TextView(this); t.setText("Drawing in progress, please wait"); t.setTextColor(0xFFFFFFFF); t.setTextSize(18); t.setGravity(Gravity.CENTER);
        lay.addView(t); drawingOverlay = lay; rootFrame.addView(lay);
    }
    void hideDrawing() { if(drawingOverlay != null){ rootFrame.removeView(drawingOverlay); drawingOverlay = null; } }

    void showResultPopup(int winner, int winAmount, int totalBet, String special) {
        hideResultPopup();
        LinearLayout bg = new LinearLayout(this); bg.setOrientation(LinearLayout.VERTICAL); bg.setGravity(Gravity.CENTER); bg.setBackgroundColor(0xAA000000);
        bg.setLayoutParams(popupParams());
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER);
        box.setBackground(roundBg(0xFF8B0000, 30)); box.setPadding(20,20,20,20);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.bottomMargin = 140; box.setLayoutParams(bp);
        TextView lion = new TextView(this); lion.setText("🦁  " + emojis[winner]); lion.setTextSize(52); lion.setGravity(Gravity.CENTER);
        TextView r = new TextView(this); r.setText("Round " + round + " Result " + special); r.setTextColor(0xFFFFFFFF); r.setTextSize(16); r.setGravity(Gravity.CENTER);
        TextView e = new TextView(this); e.setText("Current Earnings:  " + winAmount); e.setTextColor(0xFFFFFF00); e.setTextSize(15); e.setGravity(Gravity.CENTER);
        TextView b = new TextView(this); b.setText("Current Bet:  " + totalBet); b.setTextColor(0xFFFFFFFF); b.setTextSize(15); b.setGravity(Gravity.CENTER);
        TextView rank = new TextView(this); rank.setText("Ranking in this Round:"); rank.setTextColor(0xFFFFFFFF); rank.setTextSize(13); rank.setGravity(Gravity.CENTER);
        box.addView(lion); box.addView(r); box.addView(e); box.addView(b); box.addView(rank);
        bg.addView(box); bg.setOnClickListener(v -> hideResultPopup());
        resultPopup = bg; rootFrame.addView(bg);
    }
    void hideResultPopup() { if(resultPopup != null){ rootFrame.removeView(resultPopup); resultPopup = null; } }

    void showWinBanner(int winAmount) {
        hideWinBanner();
        TextView banner = new TextView(this);
        banner.setText("🏆 Win  💜 " + userName + " x" + winAmount);
        banner.setTextColor(0xFF4B0082); banner.setTextSize(18); banner.setGravity(Gravity.CENTER);
        banner.setBackground(roundStroke(0xFFFFD700, 0xFFFFFFFF, 4, 30)); banner.setPadding(20,12,20,12);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(20, 20, 20, 0); lp.gravity = Gravity.TOP; banner.setLayoutParams(lp);
        winBanner = banner; rootFrame.addView(banner);
        handler.postDelayed(() -> hideWinBanner(), 3000);
    }
    void hideWinBanner() { if(winBanner != null){ rootFrame.removeView(winBanner); winBanner = null; } }

    void showMyHistory(){
        hideMyHistory();
        LinearLayout bg = new LinearLayout(this); bg.setOrientation(LinearLayout.VERTICAL); bg.setGravity(Gravity.CENTER); bg.setBackgroundColor(0xAA000000);
        bg.setLayoutParams(popupParams());
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(roundBg(0xFFFFF3D6, 30)); box.setPadding(16,16,16,16);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.bottomMargin = 140; box.setLayoutParams(bp);
        TextView t = new TextView(this); t.setText("MyHistory - " + userName); t.setGravity(Gravity.CENTER); t.setTextSize(18); box.addView(t);
        if(myHistoryList.isEmpty()){
            TextView empty = new TextView(this); empty.setText("No history yet.\nPlay a round to see your bets here."); empty.setGravity(Gravity.CENTER); empty.setPadding(0,16,0,16); box.addView(empty);
        } else {
            for(String h : myHistoryList){ TextView tv = new TextView(this); tv.setText(h); tv.setPadding(0,6,0,6); tv.setTextSize(13); box.addView(tv); }
        }
        TextView total = new TextView(this); total.setText("Ranking Points: " + rankingPoints + " | Total Today: 🪙 " + totalToday); total.setGravity(Gravity.CENTER); total.setPadding(0,10,0,0); box.addView(total);
        TextView close = new TextView(this); close.setText("✕ Close"); close.setGravity(Gravity.CENTER); close.setPadding(0,10,0,0); close.setOnClickListener(v -> hideMyHistory()); box.addView(close);
        bg.addView(box); bg.setOnClickListener(v -> hideMyHistory()); historyPopup = bg; rootFrame.addView(bg);
    }
    void hideMyHistory(){ if(historyPopup != null){ rootFrame.removeView(historyPopup); historyPopup = null; } }

    void showRanking() {
        hideRanking();
        LinearLayout bg = new LinearLayout(this); bg.setOrientation(LinearLayout.VERTICAL); bg.setGravity(Gravity.CENTER); bg.setBackgroundColor(0xAA000000);
        bg.setLayoutParams(popupParams());
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(roundBg(0xFFFFF3D6, 30)); box.setPadding(16,16,16,16);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels * 0.92), ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.bottomMargin = 140; box.setLayoutParams(bp);
        TextView title = new TextView(this); title.setText("Daily Rankings    Weekly Rankings"); title.setGravity(Gravity.CENTER); title.setTextSize(15); box.addView(title);
        LinearLayout tabs = new LinearLayout(this); tabs.setOrientation(LinearLayout.HORIZONTAL); tabs.setGravity(Gravity.CENTER); tabs.setPadding(0,10,0,10);
        TextView today = new TextView(this); today.setText("  Today  "); today.setBackground(roundBg(0xFFFFD700, 30)); today.setPadding(20,10,20,10);
        TextView yest = new TextView(this); yest.setText("  Yesterday  "); yest.setBackground(roundBg(0xFFFFFFFF, 30)); yest.setPadding(20,10,20,10);
        tabs.addView(today); tabs.addView(yest); box.addView(tabs);
        TextView head = new TextView(this); head.setText("Rank      Name           Points      Rewards"); head.setGravity(Gravity.CENTER); head.setTextSize(13); box.addView(head);
        // Ranking e User er Point dekhabe - Demo er moto
        String[][] data = {{"1","Ozzy","767,728","43,850"},{"2","profes.","680,000","17,540"},{"3","...","623,864","8,770"},{"4","","525,000","5,262"},{"5","","500,000","3,508"}};
        for(String[] row : data){ TextView tv = new TextView(this); tv.setText(row[0] + "        " + row[1] + "        " + row[2] + "        " + row[3]); tv.setGravity(Gravity.CENTER); tv.setTextSize(13); tv.setPadding(0,6,0,6); box.addView(tv); }
        TextView me = new TextView(this); me.setText("10+        " + userName + "        " + rankingPoints + "        0"); me.setGravity(Gravity.CENTER); me.setTextSize(13); me.setPadding(0,6,0,6); me.setBackground(roundBg(0xFFFFD700, 20)); box.addView(me);
        TextView time = new TextView(this); time.setText("Time Remaining for Settlement: 08:33:30"); time.setGravity(Gravity.CENTER); time.setTextSize(12); time.setPadding(0,10,0,0); box.addView(time);
        TextView close = new TextView(this); close.setText("✕ Close"); close.setGravity(Gravity.CENTER); close.setPadding(0,10,0,0); close.setOnClickListener(v -> hideRanking()); box.addView(close);
        bg.addView(box); bg.setOnClickListener(v -> hideRanking()); rankingPopup = bg; rootFrame.addView(bg);
    }
    void hideRanking() { if(rankingPopup != null){ rootFrame.removeView(rankingPopup); rankingPopup = null; } }

    void showIntro() {
        hideIntro();
        LinearLayout bg = new LinearLayout(this); bg.setOrientation(LinearLayout.VERTICAL); bg.setGravity(Gravity.CENTER); bg.setBackgroundColor(0xAA000000);
        bg.setLayoutParams(popupParams());
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(roundBg(0xFFFFF3D6, 30)); box.setPadding(20,20,20,20);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.bottomMargin = 140; box.setLayoutParams(bp);
        TextView t = new TextView(this); t.setText("Game Instructions"); t.setGravity(Gravity.CENTER); t.setTextSize(18);
        TextView body = new TextView(this);
        body.setText("1. Select the amount to bet, then choose your favorite food to place a bet\n\n2. The result will be revealed after each betting round ends\n\n3. If the result matches the food you bet on, you will receive a corresponding multiple of coins as a reward\n\n4. Users participating in the game will continuously increase the official prize pool. When the prize pool reaches a certain value, there is a chance to draw pizza or salad.\n\n5. If salad is drawn, all vegetable-based foods will win.\n🍇 🥬 🌽 🍓\n\n6. If pizza is drawn, all meat-based foods will win.\n🍗 🐙 🦈 🥩");
        body.setTextSize(13); body.setPadding(0,12,0,0);
        TextView close = new TextView(this); close.setText("✕ Close"); close.setGravity(Gravity.CENTER); close.setPadding(0,12,0,0); close.setOnClickListener(v -> hideIntro());
        box.addView(t); box.addView(body); box.addView(close);
        bg.addView(box); bg.setOnClickListener(v -> hideIntro()); introPopup = bg; rootFrame.addView(bg);
    }
    void hideIntro() { if(introPopup != null){ rootFrame.removeView(introPopup); introPopup = null; } }

    LinearLayout createUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF00C2E8);

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(16,10,16,6);
        TextView back = new TextView(this); back.setText("⬅"); back.setTextSize(24); back.setTextColor(0xFFFFFFFF); back.setPadding(0,0,16,0); back.setOnClickListener(v -> finish());
        roundTxt = new TextView(this); roundTxt.setText("Round: " + round); roundTxt.setTextSize(14); roundTxt.setTextColor(0xFF333333);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); roundTxt.setLayoutParams(tp);
        TextView music = new TextView(this); music.setText("🎵"); music.setTextSize(22); music.setPadding(0,0,16,0);
        music.setOnClickListener(v -> Toast.makeText(this,"Music On/Off",Toast.LENGTH_SHORT).show());
        TextView help = new TextView(this); help.setText("❓"); help.setTextSize(22); help.setOnClickListener(v -> showIntro());
        top.addView(back); top.addView(roundTxt); top.addView(music); top.addView(help); root.addView(top);

        LinearLayout rankRow = new LinearLayout(this); rankRow.setOrientation(LinearLayout.HORIZONTAL); rankRow.setPadding(16,0,16,6);
        TextView rank = new TextView(this); rank.setText("🏆\nRanking"); rank.setGravity(Gravity.CENTER); rank.setTextSize(13); rank.setTextColor(0xFFFFD700);
        rank.setOnClickListener(v -> showRanking()); rankRow.addView(rank); root.addView(rankRow);

        // ===== ZOOM SYSTEM - CHAKA CHOTO - 980 -> 920 - PREMIUM =====
        FrameLayout wheel = new FrameLayout(this);
        wheel.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 920));
        TextView wheelRing = new TextView(this);
        FrameLayout.LayoutParams ringLp = new FrameLayout.LayoutParams(660, 660);
        ringLp.leftMargin = 210; ringLp.topMargin = 110; ringLp.gravity = Gravity.CENTER_HORIZONTAL;
        wheelRing.setLayoutParams(ringLp);
        wheelRing.setBackground(circleStroke(0x00FFFFFF, 0xFFFFD700, 12));
        wheel.addView(wheelRing);

        int centerX = 540, centerY = 470, radius = 350;
        for (int i = 0; i < 8; i++) {
            final int idx = i;
            double angle = Math.toRadians(-90 + i * 45);
            int x = (int)(centerX + radius * Math.cos(angle)) - 100;
            int y = (int)(centerY + radius * Math.sin(angle)) - 80;
            LinearLayout booth = new LinearLayout(this);
            booth.setOrientation(LinearLayout.VERTICAL); booth.setGravity(Gravity.CENTER);
            booth.setBackground(roundBg(0xFFFFF3D6, 60)); booth.setPadding(6,6,6,6);
            FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(200, 210);
            ip.leftMargin = x; ip.topMargin = y; booth.setLayoutParams(ip);
            TextView e = new TextView(this); e.setText(emojis[i]); e.setTextSize(32); e.setGravity(Gravity.CENTER);
            TextView m = new TextView(this); m.setText(mults[i]); m.setTextSize(18); m.setGravity(Gravity.CENTER); m.setTextColor(0xFFFF8C00);
            TextView n = new TextView(this); n.setText(names[i]); n.setTextSize(10); n.setGravity(Gravity.CENTER);
            TextView b = new TextView(this); b.setText("Bet: 0"); b.setTextSize(11); b.setGravity(Gravity.CENTER);
            booth.addView(e); booth.addView(m); booth.addView(n); booth.addView(b);
            if (i == hotIndex) {
                TextView hot = new TextView(this); hot.setText("Hot"); hot.setTextColor(0xFFFFFFFF); hot.setBackground(roundBg(0xFFFF0000, 20)); hot.setTextSize(11); hot.setGravity(Gravity.CENTER); hot.setPadding(14,4,14,4);
                booth.addView(hot);
            }
            booth.setOnClickListener(v -> placeBet(idx));
            betViews[i] = b; boothViews[i] = booth; wheel.addView(booth);
        }

        // ===== VITORE CIRCLE - GOL - 300 -> 260 - JUM OUT =====
        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL); center.setGravity(Gravity.CENTER);
        center.setBackground(circleStroke(0xFFFFD700, 0xFFB8860B, 8)); center.setPadding(8,8,8,8);
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(260, 260);
        cp.leftMargin = 410; cp.topMargin = 310; center.setLayoutParams(cp);
        TextView lion = new TextView(this); lion.setText("🦁"); lion.setTextSize(46); lion.setGravity(Gravity.CENTER);
        lionTimerTxt = new TextView(this); lionTimerTxt.setText("30"); lionTimerTxt.setTextSize(22); lionTimerTxt.setGravity(Gravity.CENTER); lionTimerTxt.setTextColor(0xFF8B4513);
        lionTimerTxt.setBackground(circleStroke(0xFFFFF3D6, 0xFFFFD700, 6)); lionTimerTxt.setPadding(16,4,16,4);
        center.addView(lion); center.addView(lionTimerTxt); wheel.addView(center); root.addView(wheel);

        LinearLayout extraRow = new LinearLayout(this);
        extraRow.setOrientation(LinearLayout.HORIZONTAL); extraRow.setGravity(Gravity.CENTER); extraRow.setPadding(16,0,16,0);
        String[] extra = {"🥗 salad", "🍕 pizza"};
        for(String ex : extra){
            TextView exv = new TextView(this); exv.setText(ex); exv.setGravity(Gravity.CENTER); exv.setTextSize(15);
            exv.setBackground(roundBg(0xFFFFF3D6, 50)); exv.setPadding(20,14,20,14);
            LinearLayout.LayoutParams exp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            exp.setMargins(12,0,12,0); exv.setLayoutParams(exp);
            extraRow.addView(exv);
        }
        root.addView(extraRow);

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.VERTICAL);
        bottom.setBackground(roundBg(0xFFD32F2F, 30)); bottom.setPadding(16,12,16,12);

        // ===== USER PROFILE BOX - MY BET - SUDHU PLUS HOBE =====
        LinearLayout myRow = new LinearLayout(this);
        myRow.setOrientation(LinearLayout.HORIZONTAL); myRow.setPadding(0,6,0,10);
        LinearLayout userBox = new LinearLayout(this);
        userBox.setOrientation(LinearLayout.HORIZONTAL); userBox.setGravity(Gravity.CENTER_VERTICAL);
        userBox.setBackground(roundBg(0xFFFFF3D6, 30)); userBox.setPadding(12,8,12,8);
        TextView userIcon = new TextView(this); userIcon.setText("👤"); userIcon.setTextSize(24); userIcon.setPadding(0,0,8,0);
        LinearLayout userText = new LinearLayout(this); userText.setOrientation(LinearLayout.VERTICAL);
        userBetNameTxt = new TextView(this); userBetNameTxt.setText(userName); userBetNameTxt.setTextSize(13);
        userBetCoinsTxt = new TextView(this); userBetCoinsTxt.setText("🪙 " + myBetThisRound); userBetCoinsTxt.setTextSize(13);
        userText.addView(userBetNameTxt); userText.addView(userBetCoinsTxt);
        userBetTotalTxt = new TextView(this); userBetTotalTxt.setText("My Bet: 0"); userBetTotalTxt.setTextSize(12); userBetTotalTxt.setGravity(Gravity.CENTER);
        userBox.addView(userIcon); userBox.addView(userText); userBox.addView(userBetTotalTxt);
        LinearLayout.LayoutParams ozp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ozp.setMargins(0,0,10,0); userBox.setLayoutParams(ozp);
        TextView myHist = new TextView(this); myHist.setText("📝 MyHistory ▶");
        myHist.setBackground(roundBg(0xFFFFF3D6, 30)); myHist.setPadding(20,16,20,16); myHist.setGravity(Gravity.CENTER);
        myHist.setOnClickListener(v -> showMyHistory());
        LinearLayout.LayoutParams mhp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        mhp.setMargins(10,0,0,0); myHist.setLayoutParams(mhp);
        myRow.addView(userBox); myRow.addView(myHist); bottom.addView(myRow);

        LinearLayout resRow = new LinearLayout(this);
        resRow.setOrientation(LinearLayout.HORIZONTAL); resRow.setGravity(Gravity.CENTER_VERTICAL);
        resultTxt = new TextView(this); resultTxt.setText("Result  "); resultTxt.setTextColor(0xFFFFFFFF); resultTxt.setTextSize(14);
        resRow.addView(resultTxt);
        HorizontalScrollView hs = new HorizontalScrollView(this);
        LinearLayout hist = new LinearLayout(this); hist.setOrientation(LinearLayout.HORIZONTAL);
        for (String h : history) {
            TextView hv = new TextView(this); hv.setText(h); hv.setTextSize(20);
            hv.setBackground(circleBg(0xFFFFFFFF)); hv.setPadding(16,10,16,10); hv.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(110, 110);
            hp.setMargins(6,0,6,0); hv.setLayoutParams(hp); hist.addView(hv);
        }
        hs.addView(hist); resRow.addView(hs); bottom.addView(resRow);

        // ===== BET CHIPS - 1K 5K 10K 100K - SELECT HOY =====
        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL); chips.setGravity(Gravity.CENTER); chips.setPadding(0,14,0,0);
        String[] chipLabels = {"1K","5K","10K","100K"};
        int[] chipVals = {1000,5000,10000,100000};
        for (int i=0;i<4;i++){
            final int idx = i; final int v = chipVals[i]; final String label = chipLabels[i];
            TextView b = new TextView(this); b.setText("🪙\n" + label); b.setGravity(Gravity.CENTER); b.setTextSize(16); b.setTextColor(0xFFFFFFFF);
            if(i==0) b.setBackground(roundBg(0xFFDAA520, 50)); else b.setBackground(roundBg(0xFFFF69B4, 50));
            b.setPadding(10,20,10,20);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            clp.setMargins(8,0,8,0); b.setLayoutParams(clp);
            b.setOnClickListener(vv -> { chip = v; selectedChipIndex = idx; updateChipUI(); });
            chipViews[i] = b; chips.addView(b);
        }
        bottom.addView(chips);

        // ===== NICHE WALLET + TODAY + AUTO BET - VIDEO ER MOTO =====
        LinearLayout lastRow = new LinearLayout(this);
        lastRow.setOrientation(LinearLayout.HORIZONTAL); lastRow.setGravity(Gravity.CENTER_VERTICAL); lastRow.setPadding(0,12,0,0);
        LinearLayout wallet = new LinearLayout(this);
        wallet.setOrientation(LinearLayout.HORIZONTAL); wallet.setGravity(Gravity.CENTER_VERTICAL);
        wallet.setBackground(roundBg(0xFFFFFFFF, 30)); wallet.setPadding(12,8,12,8);
        TextView wIcon = new TextView(this); wIcon.setText("👤"); wIcon.setTextSize(20);
        LinearLayout wText = new LinearLayout(this); wText.setOrientation(LinearLayout.VERTICAL);
        walletNameTxt = new TextView(this); walletNameTxt.setText(userName); walletNameTxt.setTextSize(12);
        balanceTxt = new TextView(this); balanceTxt.setText("" + balance); balanceTxt.setTextSize(16);
        wText.addView(walletNameTxt); wText.addView(balanceTxt);
        TextView plus = new TextView(this); plus.setText("+"); plus.setTextSize(22); plus.setTextColor(0xFFFFFFFF);
        plus.setBackground(circleBg(0xFF4CAF50)); plus.setPadding(16,4,16,4);
        plus.setOnClickListener(v -> Toast.makeText(this, "Recharge coming soon", Toast.LENGTH_SHORT).show());
        wallet.addView(wIcon); wallet.addView(wText); wallet.addView(plus);
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); wallet.setLayoutParams(wp);
        LinearLayout earnBox = new LinearLayout(this); earnBox.setOrientation(LinearLayout.VERTICAL); earnBox.setGravity(Gravity.CENTER);
        TextView earnLabel = new TextView(this); earnLabel.setText("Today's Earnings"); earnLabel.setTextColor(0xFFFFFFFF); earnLabel.setTextSize(12);
        todayTxt = new TextView(this); todayTxt.setText("🪙 " + totalToday); todayTxt.setTextColor(0xFF333333);
        todayTxt.setBackground(roundBg(0xFFFFFFFF, 30)); todayTxt.setGravity(Gravity.CENTER); todayTxt.setPadding(20,8,20,8);
        earnBox.addView(earnLabel); earnBox.addView(todayTxt);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); earnBox.setLayoutParams(ep);
        Button auto = new Button(this); auto.setText("auto bet"); auto.setTextColor(0xFFFFFFFF);
        auto.setBackground(roundBg(0xFF4CAF50, 50));
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ap.setMargins(10,0,0,0); auto.setLayoutParams(ap);
        auto.setOnClickListener(v -> Toast.makeText(this,"Auto Bet coming soon",Toast.LENGTH_SHORT).show());
        lastRow.addView(wallet); lastRow.addView(earnBox); lastRow.addView(auto); bottom.addView(lastRow);

        statusTxt = new TextView(this); statusTxt.setText("Tap a slot to place Bet"); statusTxt.setTextColor(0xFFFFFF00); statusTxt.setGravity(Gravity.CENTER);
        rankingPointsTxt = new TextView(this); rankingPointsTxt.setText("Points: " + rankingPoints); rankingPointsTxt.setTextColor(0xFFFFFFFF); rankingPointsTxt.setGravity(Gravity.CENTER); rankingPointsTxt.setTextSize(12);
        bottom.addView(statusTxt); bottom.addView(rankingPointsTxt); root.addView(bottom); return root;
    }

    void placeBet(int idx){
        if(!bettingOpen){ Toast.makeText(this,"Wait for next Round",Toast.LENGTH_SHORT).show(); return; }
        if(balance < chip){ Toast.makeText(this,"Not enough coins",Toast.LENGTH_SHORT).show(); return; }
        bets[idx] += chip;
        balance -= chip;
        myBetThisRound += chip; // Uporer box e sudhu plus
        rankingPoints += chip / 100; // Ranking er jonno point
        totalToday += chip;
        myHistoryList.add(0, "Round " + round + " - " + names[idx] + " " + emojis[idx] + " Bet: " + chip);
        saveWallet(); updateUI();
    }
    int getTotalBet(){ int t=0; for(int b: bets) t+=b; return t; }
    void updateUI(){ updateWalletUI(); for(int i=0;i<8;i++) betViews[i].setText("Bet: " + bets[i]); }

    void startRound(){
        bettingOpen = true; timeLeft = 30; myBetThisRound = 0; updateWalletUI();
        roundTxt.setText("Round: " + round); statusTxt.setText("Tap a slot to place Bet");
        if(lionTimerTxt != null) lionTimerTxt.setText("30");
        handler.postDelayed(new Runnable(){
            @Override public void run(){
                timeLeft--;
                if(lionTimerTxt != null) lionTimerTxt.setText("" + timeLeft);
                if(timeLeft > 0){ handler.postDelayed(this, 1000); } else { finishRound(); }
            }
        }, 1000);
    }

    void finishRound(){
        bettingOpen = false; showDrawing();
        // Salad / Pizza Special - 15% chance
        int rand = (int)(Math.random()*100);
        String special = "";
        int winner;
        if(rand < 8){ // Salad
            special = "🥗 SALAD";
            int[] veg = {4,5,6,7}; winner = veg[(int)(Math.random()*4)];
        } else if(rand < 16){ // Pizza
            special = "🍕 PIZZA";
            int[] meat = {0,1,2,3}; winner = meat[(int)(Math.random()*4)];
        } else {
            winner = (int)(Math.random()*8);
        }
        int totalBet = getTotalBet();
        // Salad hole sob Veg jitbe, Pizza hole sob Meat jitbe - Rule Sochol
        int winAmount = 0;
        if(special.contains("SALAD")){
            for(int i=4;i<8;i++) winAmount += bets[i] * getMult(i);
        } else if(special.contains("PIZZA")){
            for(int i=0;i<4;i++) winAmount += bets[i] * getMult(i);
        } else {
            winAmount = bets[winner] * getMult(winner);
        }
        final int finalWinner = winner; final int finalWin = winAmount; final String finalSpecial = special;
        handler.postDelayed(() -> {
            hideDrawing();
            for(int i=0;i<8;i++){ if(boothViews[i]==null) continue; if(i==finalWinner) boothViews[i].setBackground(roundStroke(0xFFFFD700, 0xFFFF0000, 10, 60)); else boothViews[i].setBackground(roundBg(0xFFDDDDDD, 60)); }
            balance += finalWin; saveWallet();
            resultTxt.setText("Result: " + emojis[finalWinner] + " " + finalSpecial);
            statusTxt.setText("WIN: " + emojis[finalWinner] + " " + names[finalWinner] + " " + finalSpecial);
            todayTxt.setText("🪙 " + totalToday);
            showResultPopup(finalWinner, finalWin, totalBet, finalSpecial);
            if(finalWin > 0) showWinBanner(finalWin);
            for(int i=0;i<8;i++) bets[i]=0;
            updateUI(); round++;
            handler.postDelayed(() -> { hideResultPopup(); startRound(); }, 3000);
        }, 2000);
    }
    int getMult(int i){ int[] m = {45,25,15,10,5,5,5,5}; return m[i]; }
}
