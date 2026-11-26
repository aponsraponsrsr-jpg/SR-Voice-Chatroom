package com.sr.voicechat;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class GreedyLionActivity extends AppCompatActivity {

    TextView roundTxt, balanceTxt, walletNameTxt, statusTxt, resultTxt, myBetTxt, todayTxt, lionTimerTxt;
    TextView[] betViews = new TextView[8];
    LinearLayout[] boothViews = new LinearLayout[8];
    int[] bets = new int[8];
    int chip = 1000;
    int balance = 0;
    String userName = "Guest";
    int round = 926;
    int timeLeft = 30;
    boolean bettingOpen = true;
    int lightIndex = 0;
    Handler handler = new Handler();
    Handler lightHandler = new Handler();

    // DEMO SERIAL - Upor theke Clock-wise
    String[] names = {"Chicken","Octopus","Shark","Meat","Grapes","Cabbage","Corn","Strawberry"};
    String[] emojis = {"🍗","🐙","🦈","🥩","🍇","🥬","🌽","🍓"};
    String[] mults = {"x45","x25","x15","x10","x5","x5","x5","x5"};
    int hotIndex = 4; // Grapes Hot
    String[] history = {"🍓","🍇","🌽","🥩","🍓","🦈","🌽","🍓"};

    // ===== GOL DRAWABLE BANANO - Charkona NOY =====
    GradientDrawable roundBg(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }
    GradientDrawable roundStroke(int color, int strokeColor, int strokeW, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setStroke(strokeW, strokeColor);
        d.setCornerRadius(radius);
        return d;
    }
    GradientDrawable circleBg(int color) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        return d;
    }
    GradientDrawable circleStroke(int color, int strokeColor, int strokeW) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        d.setStroke(strokeW, strokeColor);
        return d;
    }

    void loadWallet() {
        SharedPreferences prefs = getSharedPreferences("user_wallet", MODE_PRIVATE);
        userName = prefs.getString("user_name", "Guest");
        balance = prefs.getInt("wallet_coins", 10000);
    }
    void saveWallet() {
        getSharedPreferences("user_wallet", MODE_PRIVATE).edit().putInt("wallet_coins", balance).putString("user_name", userName).apply();
        updateWalletUI();
    }
    void updateWalletUI() {
        if (walletNameTxt != null) walletNameTxt.setText(userName);
        if (balanceTxt != null) balanceTxt.setText("🪙 " + balance);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadWallet();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(createUI());
        updateWalletUI();
        if (getWindow() != null) {
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int)(getResources().getDisplayMetrics().heightPixels * 0.92));
            getWindow().setGravity(Gravity.BOTTOM);
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        startRound();
        startLight();
    }

    // LIGHT GHORA - Real Game er moto
    void startLight() {
        lightHandler.postDelayed(new Runnable(){
            @Override public void run(){
                lightIndex = (lightIndex + 1) % 8;
                updateLight();
                int speed = timeLeft <= 5 ? 80 : 300;
                lightHandler.postDelayed(this, speed);
            }
        }, 300);
    }
    void updateLight() {
        for(int i=0;i<8;i++){
            if(boothViews[i]==null) continue;
            if(i==lightIndex){
                boothViews[i].setBackground(roundStroke(0xFFFFFF99, 0xFFFFD700, 8, 60));
            } else {
                boothViews[i].setBackground(roundBg(0xFFFFF3D6, 60));
            }
        }
    }
    void setWinnerLight(int winner){
        for(int i=0;i<8;i++){
            if(boothViews[i]==null) continue;
            if(i==winner) boothViews[i].setBackground(roundStroke(0xFFFFD700, 0xFFFF0000, 10, 60));
            else boothViews[i].setBackground(roundBg(0xFFDDDDDD, 60));
        }
    }

    LinearLayout createUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF00C2E8);

        // TOP
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(16,12,16,8);
        TextView back = new TextView(this);
        back.setText("⬅"); back.setTextSize(24); back.setTextColor(0xFFFFFFFF);
        back.setPadding(0,0,16,0); back.setOnClickListener(v -> finish());
        roundTxt = new TextView(this);
        roundTxt.setText("Round: " + round); roundTxt.setTextSize(14); roundTxt.setTextColor(0xFF333333);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        roundTxt.setLayoutParams(tp);
        TextView music = new TextView(this); music.setText("🎵"); music.setTextSize(22); music.setPadding(0,0,16,0);
        TextView help = new TextView(this); help.setText("❓"); help.setTextSize(22);
        top.addView(back); top.addView(roundTxt); top.addView(music); top.addView(help);
        root.addView(top);

        LinearLayout rankRow = new LinearLayout(this);
        rankRow.setOrientation(LinearLayout.HORIZONTAL);
        rankRow.setPadding(16,0,16,8);
        TextView rank = new TextView(this);
        rank.setText("🏆\nRanking"); rank.setGravity(Gravity.CENTER); rank.setTextSize(13); rank.setTextColor(0xFFFFD700);
        rankRow.addView(rank);
        root.addView(rankRow);

        // ===== FERRIS WHEEL - GOL CHAKA =====
        FrameLayout wheel = new FrameLayout(this);
        wheel.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1100));

        // Boro Gol Chaka - Center Ring
        TextView wheelRing = new TextView(this);
        FrameLayout.LayoutParams ringLp = new FrameLayout.LayoutParams(750, 750);
        ringLp.leftMargin = 165; ringLp.topMargin = 140; ringLp.gravity = Gravity.CENTER_HORIZONTAL;
        wheelRing.setLayoutParams(ringLp);
        wheelRing.setBackground(circleStroke(0x00FFFFFF, 0xFFFFD700, 18));
        wheel.addView(wheelRing);

        // 8 BOOTH - GOL GOMBUJ - Charkona NOY
        int centerX = 540, centerY = 520, radius = 400;
        for (int i = 0; i < 8; i++) {
            final int idx = i;
            double angle = Math.toRadians(-90 + i * 45);
            int x = (int)(centerX + radius * Math.cos(angle)) - 110;
            int y = (int)(centerY + radius * Math.sin(angle)) - 90;

            LinearLayout booth = new LinearLayout(this);
            booth.setOrientation(LinearLayout.VERTICAL);
            booth.setGravity(Gravity.CENTER);
            booth.setBackground(roundBg(0xFFFFF3D6, 60)); // GOL Corner 60
            booth.setPadding(6,6,6,6);
            FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(220, 230);
            ip.leftMargin = x; ip.topMargin = y;
            booth.setLayoutParams(ip);

            // Uporer Gol Chad - Emoji Boro
            TextView e = new TextView(this);
            e.setText(emojis[i]);
            e.setTextSize(36);
            e.setGravity(Gravity.CENTER);
            // Mult - Sonali, Boro
            TextView m = new TextView(this);
            m.setText(mults[i]);
            m.setTextSize(20);
            m.setGravity(Gravity.CENTER);
            m.setTextColor(0xFFFF8C00);
            m.setShadowLayer(3, 1, 1, 0xFFFFFFFF);
            TextView n = new TextView(this);
            n.setText(names[i]);
            n.setTextSize(10);
            n.setGravity(Gravity.CENTER);
            TextView b = new TextView(this);
            b.setText("Bet: 0");
            b.setTextSize(11);
            b.setGravity(Gravity.CENTER);
            booth.addView(e); booth.addView(m); booth.addView(n); booth.addView(b);

            if (i == hotIndex) {
                TextView hot = new TextView(this);
                hot.setText("Hot");
                hot.setTextColor(0xFFFFFFFF);
                hot.setBackground(roundBg(0xFFFF0000, 20));
                hot.setTextSize(11);
                hot.setGravity(Gravity.CENTER);
                hot.setPadding(14,4,14,4);
                booth.addView(hot);
            }
            booth.setOnClickListener(v -> placeBet(idx));
            betViews[i] = b;
            boothViews[i] = booth;
            wheel.addView(booth);
        }

        // CENTER - GOL SINGHO
        LinearLayout center = new LinearLayout(this);
        center.setOrientation(LinearLayout.VERTICAL);
        center.setGravity(Gravity.CENTER);
        center.setBackground(circleStroke(0xFFFFD700, 0xFFB8860B, 8));
        center.setPadding(10,10,10,10);
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(360, 360);
        cp.leftMargin = 360; cp.topMargin = 340;
        center.setLayoutParams(cp);
        TextView lion = new TextView(this);
        lion.setText("🦁");
        lion.setTextSize(58);
        lion.setGravity(Gravity.CENTER);
        // Timer - Gol Badge
        lionTimerTxt = new TextView(this);
        lionTimerTxt.setText("30");
        lionTimerTxt.setTextSize(26);
        lionTimerTxt.setGravity(Gravity.CENTER);
        lionTimerTxt.setTextColor(0xFF8B4513);
        lionTimerTxt.setBackground(circleStroke(0xFFFFF3D6, 0xFFFFD700, 6));
        lionTimerTxt.setPadding(20,6,20,6);
        center.addView(lion); center.addView(lionTimerTxt);
        wheel.addView(center);
        root.addView(wheel);

        // EXTRA 2 BOOTH - salad / pizza - Demo er niche
        LinearLayout extraRow = new LinearLayout(this);
        extraRow.setOrientation(LinearLayout.HORIZONTAL);
        extraRow.setGravity(Gravity.CENTER);
        extraRow.setPadding(16,0,16,0);
        String[] extra = {"🥗 salad", "🍕 pizza"};
        for(String ex : extra){
            TextView exv = new TextView(this);
            exv.setText(ex);
            exv.setGravity(Gravity.CENTER);
            exv.setTextSize(16);
            exv.setBackground(roundBg(0xFFFFF3D6, 50));
            exv.setPadding(20,16,20,16);
            LinearLayout.LayoutParams exp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            exp.setMargins(12,0,12,0);
            exv.setLayoutParams(exp);
            extraRow.addView(exv);
        }
        root.addView(extraRow);

        // BOTTOM RED
        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.VERTICAL);
        bottom.setBackground(roundBg(0xFFD32F2F, 30));
        bottom.setPadding(16,12,16,12);

        // My Bet + MyHistory - Gol
        LinearLayout myRow = new LinearLayout(this);
        myRow.setOrientation(LinearLayout.HORIZONTAL);
        myRow.setPadding(0,6,0,10);
        myBetTxt = new TextView(this);
        myBetTxt.setText("👤 Ozzy  🪙 767,728  |  My Bet: 0");
        myBetTxt.setTextColor(0xFF333333);
        myBetTxt.setBackground(roundBg(0xFFFFF3D6, 30));
        myBetTxt.setPadding(20,16,20,16);
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        mp.setMargins(0,0,10,0); myBetTxt.setLayoutParams(mp);
        TextView myHist = new TextView(this);
        myHist.setText("📝 MyHistory ▶");
        myHist.setBackground(roundBg(0xFFFFF3D6, 30));
        myHist.setPadding(20,16,20,16);
        LinearLayout.LayoutParams mhp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        mhp.setMargins(10,0,0,0); myHist.setLayoutParams(mhp);
        myRow.addView(myBetTxt); myRow.addView(myHist);
        bottom.addView(myRow);

        // Result - Gol Gol
        LinearLayout resRow = new LinearLayout(this);
        resRow.setOrientation(LinearLayout.HORIZONTAL);
        resRow.setGravity(Gravity.CENTER_VERTICAL);
        resultTxt = new TextView(this);
        resultTxt.setText("Result  ");
        resultTxt.setTextColor(0xFFFFFFFF); resultTxt.setTextSize(14);
        resRow.addView(resultTxt);
        HorizontalScrollView hs = new HorizontalScrollView(this);
        LinearLayout hist = new LinearLayout(this);
        hist.setOrientation(LinearLayout.HORIZONTAL);
        for (String h : history) {
            TextView hv = new TextView(this);
            hv.setText(h); hv.setTextSize(20);
            hv.setBackground(circleBg(0xFFFFFFFF));
            hv.setPadding(16,10,16,10); hv.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(110, 110);
            hp.setMargins(6,0,6,0); hv.setLayoutParams(hp);
            hist.addView(hv);
        }
        hs.addView(hist);
        resRow.addView(hs);
        bottom.addView(resRow);

        // CHIPS - Gol Arch - 1K 5K 10K 100K
        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setGravity(Gravity.CENTER);
        chips.setPadding(0,14,0,0);
        String[] chipLabels = {"1K","5K","10K","100K"};
        int[] chipVals = {1000,5000,10000,100000};
        for (int i=0;i<4;i++){
            final int v = chipVals[i];
            final String label = chipLabels[i];
            TextView b = new TextView(this);
            b.setText("🪙\n" + label);
            b.setGravity(Gravity.CENTER);
            b.setTextSize(16);
            b.setTextColor(0xFFFFFFFF);
            if(i==0) b.setBackground(roundBg(0xFFDAA520, 50));
            else b.setBackground(roundBg(0xFFFF69B4, 50));
            b.setPadding(10,20,10,20);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            clp.setMargins(8,0,8,0); b.setLayoutParams(clp);
            b.setOnClickListener(vv -> { chip = v; myBetTxt.setText("My Bet: " + getTotalBet() + " • Chip: " + label); });
            chips.addView(b);
        }
        bottom.addView(chips);

        // WALLET - Bam Nicher Konay - REAL USER
        LinearLayout lastRow = new LinearLayout(this);
        lastRow.setOrientation(LinearLayout.HORIZONTAL);
        lastRow.setGravity(Gravity.CENTER_VERTICAL);
        lastRow.setPadding(0,12,0,0);
        LinearLayout wallet = new LinearLayout(this);
        wallet.setOrientation(LinearLayout.HORIZONTAL);
        wallet.setGravity(Gravity.CENTER_VERTICAL);
        wallet.setBackground(roundBg(0xFFFFFFFF, 30));
        wallet.setPadding(12,8,12,8);
        TextView wIcon = new TextView(this); wIcon.setText("👤"); wIcon.setTextSize(20);
        LinearLayout wText = new LinearLayout(this); wText.setOrientation(LinearLayout.VERTICAL);
        walletNameTxt = new TextView(this); walletNameTxt.setText(userName); walletNameTxt.setTextSize(12);
        balanceTxt = new TextView(this); balanceTxt.setText("🪙 " + balance); balanceTxt.setTextSize(16);
        wText.addView(walletNameTxt); wText.addView(balanceTxt);
        TextView plus = new TextView(this);
        plus.setText("+"); plus.setTextSize(22); plus.setTextColor(0xFFFFFFFF);
        plus.setBackground(circleBg(0xFF4CAF50)); plus.setPadding(16,4,16,4);
        plus.setOnClickListener(v -> Toast.makeText(this, "Recharge coming soon", Toast.LENGTH_SHORT).show());
        wallet.addView(wIcon); wallet.addView(wText); wallet.addView(plus);
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        wallet.setLayoutParams(wp);

        LinearLayout earnBox = new LinearLayout(this);
        earnBox.setOrientation(LinearLayout.VERTICAL); earnBox.setGravity(Gravity.CENTER);
        TextView earnLabel = new TextView(this); earnLabel.setText("Today's Earnings"); earnLabel.setTextColor(0xFFFFFFFF); earnLabel.setTextSize(12);
        todayTxt = new TextView(this); todayTxt.setText("🪙 0"); todayTxt.setTextColor(0xFF333333);
        todayTxt.setBackground(roundBg(0xFFFFFFFF, 30)); todayTxt.setGravity(Gravity.CENTER); todayTxt.setPadding(20,8,20,8);
        earnBox.addView(earnLabel); earnBox.addView(todayTxt);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        earnBox.setLayoutParams(ep);

        Button auto = new Button(this);
        auto.setText("auto bet"); auto.setTextColor(0xFFFFFFFF);
        auto.setBackground(roundBg(0xFF4CAF50, 50));
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ap.setMargins(10,0,0,0); auto.setLayoutParams(ap);
        auto.setOnClickListener(v -> Toast.makeText(this,"Auto Bet coming soon",Toast.LENGTH_SHORT).show());
        lastRow.addView(wallet); lastRow.addView(earnBox); lastRow.addView(auto);
        bottom.addView(lastRow);

        statusTxt = new TextView(this);
        statusTxt.setText("Tap a slot to place Bet");
        statusTxt.setTextColor(0xFFFFFF00); statusTxt.setGravity(Gravity.CENTER);
        bottom.addView(statusTxt);
        root.addView(bottom);
        return root;
    }

    void placeBet(int idx){
        if(!bettingOpen){ Toast.makeText(this,"Wait for next Round",Toast.LENGTH_SHORT).show(); return; }
        if(balance < chip){ Toast.makeText(this,"Not enough coins",Toast.LENGTH_SHORT).show(); return; }
        bets[idx] += chip;
        balance -= chip;
        saveWallet();
        updateUI();
    }
    int getTotalBet(){ int t=0; for(int b: bets) t+=b; return t; }
    void updateUI(){
        updateWalletUI();
        for(int i=0;i<8;i++) betViews[i].setText("Bet: " + bets[i]);
        myBetTxt.setText("My Bet: " + getTotalBet() + " • Chip: " + chip);
    }

    // TIMER 30 -> 0 -> RESULT -> Abar 30
    void startRound(){
        bettingOpen = true;
        timeLeft = 30;
        roundTxt.setText("Round: " + round);
        statusTxt.setText("Tap a slot to place Bet");
        if(lionTimerTxt != null) lionTimerTxt.setText("30");
        handler.postDelayed(new Runnable(){
            @Override public void run(){
                timeLeft--;
                if(lionTimerTxt != null) lionTimerTxt.setText("" + timeLeft);
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
        setWinnerLight(winner);
        int winAmount = bets[winner] * getMult(winner);
        balance += winAmount;
        saveWallet();
        resultTxt.setText("Result: " + emojis[winner]);
        statusTxt.setText("WIN: " + emojis[winner] + " " + names[winner] + " - No Bet, Wait for next Round");
        todayTxt.setText("🪙 " + winAmount);
        for(int i=0;i<8;i++) bets[i]=0;
        updateUI();
        round++;
        handler.postDelayed(() -> startRound(), 3000);
    }

    int getMult(int i){
        int[] m = {45,25,15,10,5,5,5,5};
        return m[i];
    }
}
