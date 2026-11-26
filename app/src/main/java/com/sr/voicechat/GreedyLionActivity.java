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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GreedyLionActivity extends AppCompatActivity {

    TextView roundTxt, balanceTxt, walletNameTxt, statusTxt, resultTxt, todayTxt, lionTimerTxt;
    TextView userBetNameTxt, userBetCoinsTxt, userBetTotalTxt;
    TextView[] betViews = new TextView[8];
    TextView[] chipViews = new TextView[4];
    LinearLayout[] boothViews = new LinearLayout[8];
    LinearLayout resultHistoryLayout;
    int[] bets = new int[8];

    // ===== CHIP - 1K BAD - 5K THEKE SURU =====
    int[] chipVals = {5000,10000,100000,500000};
    String[] chipLabels = {"5K","10K","100K","500K"};
    int chip = 5000; int selectedChipIndex = 0;

    int balance = 3000; int myBetThisRound = 0; int rankingPoints = 60; int totalToday = 6000;
    String userName = "Guest"; String userId = ""; String userPic = "👤";
    int round = 927; int timeLeft = 30; boolean bettingOpen = true;
    int lightIndex = 0; boolean lightRunning = true; boolean isDrawing = false;
    Handler handler = new Handler(); Handler lightHandler = new Handler();
    FrameLayout rootFrame;
    View drawingOverlay, resultPopup, rankingPopup, introPopup, winBanner, historyPopup;
    ArrayList<String> resultHistory = new ArrayList<>();
    ArrayList<HistoryItem> myHistory = new ArrayList<>();

    static class HistoryItem {
        String date; int round; ArrayList<String> betLines = new ArrayList<>();
        String winEmoji; int winAmount;
        HistoryItem(String d,int r,String w,int amt){ date=d; round=r; winEmoji=w; winAmount=amt; }
    }

    String[] names = {"Chicken","Octopus","Shark","Meat","Grapes","Cabbage","Corn","Strawberry"};
    String[] emojis = {"🍗","🐙","🦈","🥩","🍇","🥬","🌽","🍓"};
    String[] mults = {"x45","x25","x15","x10","x5","x5","x5","x5"};
    boolean[] isMeat = {true,true,true,true,false,false,false,false};
    int hotIndex = 4;

    // ===== COLOR - OFF-WHITE GHEE - BESI SADA NA =====
    int OFFWHITE = 0xFFFFF8E1; // Ghiya / Off-White
    int OFFWHITE2 = 0xFFFFF3D6;
    int PINK = 0xFFFF69B4; // Chip - Sob Pink
    int LIGHT_YELLOW = 0xFFFFF59D; // Select hole Halka Yellow - Besi Deep na

    GradientDrawable roundBg(int c,int r){ GradientDrawable d=new GradientDrawable(); d.setColor(c); d.setCornerRadius(r); return d; }
    GradientDrawable roundStroke(int c,int sc,int sw,int r){ GradientDrawable d=new GradientDrawable(); d.setColor(c); d.setStroke(sw,sc); d.setCornerRadius(r); return d; }
    GradientDrawable circleBg(int c){ GradientDrawable d=new GradientDrawable(); d.setShape(GradientDrawable.OVAL); d.setColor(c); return d; }
    GradientDrawable circleStroke(int c,int sc,int sw){ GradientDrawable d=new GradientDrawable(); d.setShape(GradientDrawable.OVAL); d.setColor(c); d.setStroke(sw,sc); return d; }

    // ===== PROFILE AUTOMATIC - MAIN APP THEKE ASBE =====
    // MainActivity theke evabe pathaba:
    // intent.putExtra("user_name", "Habib khan");
    // intent.putExtra("user_id", "11405981");
    // intent.putExtra("user_pic", "👤");
    void loadWallet(){
        SharedPreferences p = getSharedPreferences("user_wallet", MODE_PRIVATE);
        // 1. Intent theke - Jodi Main App pathay
        if(getIntent()!=null){
            if(getIntent().hasExtra("user_name")) userName = getIntent().getStringExtra("user_name");
            if(getIntent().hasExtra("user_id")) userId = getIntent().getStringExtra("user_id");
            if(getIntent().hasExtra("user_pic")) userPic = getIntent().getStringExtra("user_pic");
            if(getIntent().hasExtra("wallet_coins")) balance = getIntent().getIntExtra("wallet_coins",3000);
        }
        // 2. Jodi Intent e na thake - SharedPreferences theke - Wallet er ta
        if(userName==null || userName.equals("Guest") || userName.isEmpty()){
            userName = p.getString("user_name","Guest");
            userId = p.getString("user_id","");
            userPic = p.getString("user_pic","👤");
        }
        balance = p.getInt("wallet_coins",balance);
        rankingPoints = p.getInt("ranking_points",60);
        totalToday = p.getInt("total_today",6000);
        // Round Automatic - Last Round theke +1
        round = p.getInt("last_round",927);
    }
    void saveWallet(){
        getSharedPreferences("user_wallet",MODE_PRIVATE).edit().putInt("wallet_coins",balance).putString("user_name",userName).putString("user_id",userId).putString("user_pic",userPic).putInt("ranking_points",rankingPoints).putInt("total_today",totalToday).putInt("last_round",round).apply();
        updateWalletUI();
    }
    void updateWalletUI(){
        if(walletNameTxt!=null) walletNameTxt.setText(userName);
        if(balanceTxt!=null) balanceTxt.setText(""+balance);
        if(userBetNameTxt!=null) userBetNameTxt.setText(userName); // Uporer box eo eki nam
        if(userBetCoinsTxt!=null) userBetCoinsTxt.setText("🪙 "+myBetThisRound);
        if(userBetTotalTxt!=null) userBetTotalTxt.setText("My Bet: "+myBetThisRound);
        if(todayTxt!=null) todayTxt.setText("🪙 "+totalToday);
    }

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        loadWallet();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        rootFrame = new FrameLayout(this);
        rootFrame.addView(createUI());
        setContentView(rootFrame);
        updateWalletUI(); updateChipUI(); refreshResultHistory();
        if(getWindow()!=null){ getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(getResources().getDisplayMetrics().heightPixels*0.92)); getWindow().setGravity(Gravity.BOTTOM); getWindow().setBackgroundDrawableResource(android.R.color.transparent); }
        startRound(); startLightSlow();
    }

    // ===== CHIP UI - SOB PINK - SELECT HOLE SUDHU OITAI HALKA YELLOW =====
    void updateChipUI(){
        for(int i=0;i<4;i++){
            if(chipViews[i]==null) continue;
            if(i==selectedChipIndex){
                chipViews[i].setBackground(roundStroke(LIGHT_YELLOW, 0xFFFFD700, 4, 50)); // Halka Yellow
                chipViews[i].setTextColor(0xFF333333);
            } else {
                chipViews[i].setBackground(roundBg(PINK, 50)); // Baki sob Pink
                chipViews[i].setTextColor(0xFFFFFFFF);
            }
        }
    }

    // ===== LIGHT - SLOW - 900ms - DHIRE DHIRE =====
    void startLightSlow(){
        lightHandler.postDelayed(new Runnable(){
            @Override public void run(){
                if(lightRunning && !isDrawing){
                    lightIndex = (lightIndex + 1) % 8;
                    for(int i=0;i<8;i++){
                        if(boothViews[i]==null) continue;
                        if(i==lightIndex) boothViews[i].setBackground(roundStroke(OFFWHITE, 0xFFFFD700, 6, 60));
                        else boothViews[i].setBackground(roundBg(OFFWHITE, 60));
                    }
                }
                // SLOW - 900ms - Age chilo 80ms - Eta Fast chilo
                lightHandler.postDelayed(this, 900);
            }
        }, 900);
    }
    void stopLight(){ lightRunning = false; }
    void resumeLight(){ lightRunning = true; }

    void refreshResultHistory(){
        if(resultHistoryLayout==null) return;
        resultHistoryLayout.removeAllViews();
        for(String h : resultHistory){
            TextView hv=new TextView(this); hv.setText(h); hv.setTextSize(20); hv.setBackground(circleBg(0xFFFFFFFF)); hv.setPadding(16,10,16,10); hv.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(110,110); hp.setMargins(6,0,6,0); hv.setLayoutParams(hp);
            resultHistoryLayout.addView(hv);
        }
    }
    void addResult(String emoji){ resultHistory.add(0, emoji); if(resultHistory.size()>20) resultHistory.remove(20); refreshResultHistory(); }
    FrameLayout.LayoutParams popupParams(){ return new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); }

    void showDrawing(){
        if(drawingOverlay!=null) return;
        isDrawing = true; stopLight(); // Drawing e Light STOP
        LinearLayout lay=new LinearLayout(this); lay.setOrientation(LinearLayout.VERTICAL); lay.setGravity(Gravity.CENTER); lay.setBackgroundColor(0x88000000); lay.setLayoutParams(popupParams());
        TextView t=new TextView(this); t.setText("Drawing in progress, please wait"); t.setTextColor(0xFFFFFFFF); t.setTextSize(18); t.setGravity(Gravity.CENTER);
        lay.addView(t); drawingOverlay=lay; rootFrame.addView(lay);
    }
    void hideDrawing(){ if(drawingOverlay!=null){ rootFrame.removeView(drawingOverlay); drawingOverlay=null; } isDrawing=false; resumeLight(); }

    void showResultPopup(int winner,int winAmount,int totalBet,String special){
        hideResultPopup();
        LinearLayout bg=new LinearLayout(this); bg.setOrientation(LinearLayout.VERTICAL); bg.setGravity(Gravity.CENTER); bg.setBackgroundColor(0xAA000000); bg.setLayoutParams(popupParams());
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER);
        box.setBackground(roundBg(0xFF8B0000,30)); box.setPadding(24,24,24,24);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels*0.92), ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.bottomMargin=100; box.setLayoutParams(bp);
        TextView stage=new TextView(this); stage.setText("⭐⭐⭐⭐⭐"); stage.setGravity(Gravity.CENTER); stage.setTextColor(0xFFFFD700); stage.setTextSize(18);
        TextView lion=new TextView(this); lion.setText("🦁  "+emojis[winner]); lion.setTextSize(64); lion.setGravity(Gravity.CENTER);
        TextView r=new TextView(this); r.setText("Round "+round+" Result "+special); r.setTextColor(0xFFFFFFFF); r.setTextSize(18); r.setGravity(Gravity.CENTER);
        TextView e=new TextView(this); e.setText("Current Earnings:  "+winAmount); e.setTextColor(0xFFFFFF00); e.setTextSize(17); e.setGravity(Gravity.CENTER);
        TextView b=new TextView(this); b.setText("Current Bet:  "+totalBet); b.setTextColor(0xFFFFFFFF); b.setTextSize(17); b.setGravity(Gravity.CENTER);
        TextView rank=new TextView(this); rank.setText("Ranking in this Round:"); rank.setTextColor(0xFFFFFFFF); rank.setTextSize(14); rank.setGravity(Gravity.CENTER);
        TextView me=new TextView(this); me.setText(userName+" - Points: "+rankingPoints); me.setTextColor(0xFFFFD700); me.setTextSize(14); me.setGravity(Gravity.CENTER);
        box.addView(stage); box.addView(lion); box.addView(r); box.addView(e); box.addView(b); box.addView(rank); box.addView(me);
        bg.addView(box); bg.setOnClickListener(v->hideResultPopup());
        resultPopup=bg; rootFrame.addView(bg);
    }
    void hideResultPopup(){ if(resultPopup!=null){ rootFrame.removeView(resultPopup); resultPopup=null; } }
    void showWinBanner(int winAmount){
        hideWinBanner();
        TextView banner=new TextView(this); banner.setText("🏆 Win  💜 "+userName+" x"+winAmount); banner.setTextColor(0xFF4B0082); banner.setTextSize(18); banner.setGravity(Gravity.CENTER);
        banner.setBackground(roundStroke(0xFFFFD700,0xFFFFFFFF,4,30)); banner.setPadding(20,12,20,12);
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(20,20,20,0); lp.gravity=Gravity.TOP; banner.setLayoutParams(lp);
        winBanner=banner; rootFrame.addView(banner); handler.postDelayed(()->hideWinBanner(),3000);
    }
    void hideWinBanner(){ if(winBanner!=null){ rootFrame.removeView(winBanner); winBanner=null; } }

    void showMyHistory(){
        hideMyHistory();
        LinearLayout bg=new LinearLayout(this); bg.setOrientation(LinearLayout.VERTICAL); bg.setGravity(Gravity.CENTER); bg.setBackgroundColor(0xAA000000); bg.setLayoutParams(popupParams());
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(roundBg(OFFWHITE,30)); box.setPadding(16,16,16,16);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels*0.94), (int)(getResources().getDisplayMetrics().heightPixels*0.68));
        bp.bottomMargin=80; box.setLayoutParams(bp);
        TextView t=new TextView(this); t.setText("My History"); t.setGravity(Gravity.CENTER); t.setTextSize(20); t.setTextColor(0xFFD32F2F); box.addView(t);
        ScrollView sv=new ScrollView(this); LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL);
        if(myHistory.isEmpty()){ TextView empty=new TextView(this); empty.setText("No history yet.\nPlay a round to see your bets here."); empty.setGravity(Gravity.CENTER); empty.setPadding(0,20,0,20); list.addView(empty); }
        else { for(HistoryItem item : myHistory){ LinearLayout head=new LinearLayout(this); head.setOrientation(LinearLayout.HORIZONTAL); TextView date=new TextView(this); date.setText(item.date); date.setTextSize(12); TextView rnd=new TextView(this); rnd.setText("Round: "+item.round); rnd.setTextSize(12); rnd.setGravity(Gravity.RIGHT); LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); date.setLayoutParams(dp); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); rnd.setLayoutParams(rp); head.addView(date); head.addView(rnd); list.addView(head); for(String line : item.betLines){ TextView tv=new TextView(this); tv.setText(line); tv.setTextSize(14); tv.setPadding(8,4,8,4); list.addView(tv); } TextView win=new TextView(this); win.setText(item.winEmoji+"  🪙 "+item.winAmount); win.setTextSize(16); win.setGravity(Gravity.RIGHT); win.setPadding(0,4,0,12); list.addView(win); View line=new View(this); line.setBackgroundColor(0xFFDDDDDD); line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,2)); list.addView(line); } }
        sv.addView(list); box.addView(sv);
        TextView close=new TextView(this); close.setText("✕ Close"); close.setGravity(Gravity.CENTER); close.setPadding(0,12,0,0); close.setOnClickListener(v->hideMyHistory()); box.addView(close);
        bg.addView(box); bg.setOnClickListener(v->hideMyHistory()); historyPopup=bg; rootFrame.addView(bg);
    }
    void hideMyHistory(){ if(historyPopup!=null){ rootFrame.removeView(historyPopup); historyPopup=null; } }

    void showRanking(){
        hideRanking();
        LinearLayout bg=new LinearLayout(this); bg.setOrientation(LinearLayout.VERTICAL); bg.setGravity(Gravity.CENTER); bg.setBackgroundColor(0xAA000000); bg.setLayoutParams(popupParams());
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(roundBg(OFFWHITE,30)); box.setPadding(16,16,16,16);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels*0.94), (int)(getResources().getDisplayMetrics().heightPixels*0.68));
        bp.bottomMargin=80; box.setLayoutParams(bp);
        TextView title=new TextView(this); title.setText("Daily Rankings    Weekly Rankings"); title.setGravity(Gravity.CENTER); title.setTextSize(16); box.addView(title);
        LinearLayout tabs=new LinearLayout(this); tabs.setOrientation(LinearLayout.HORIZONTAL); tabs.setGravity(Gravity.CENTER); tabs.setPadding(0,10,0,10);
        TextView today=new TextView(this); today.setText("  Today  "); today.setBackground(roundBg(0xFFFFD700,30)); today.setPadding(20,10,20,10);
        TextView yest=new TextView(this); yest.setText("  Yesterday  "); yest.setBackground(roundBg(0xFFFFFFFF,30)); yest.setPadding(20,10,20,10);
        TextView week=new TextView(this); week.setText("  This Week  "); week.setBackground(roundBg(0xFFFFFFFF,30)); week.setPadding(20,10,20,10);
        tabs.addView(today); tabs.addView(yest); tabs.addView(week); box.addView(tabs);
        TextView head=new TextView(this); head.setText("Rank      Name           Points      Rewards"); head.setGravity(Gravity.CENTER); head.setTextSize(13); box.addView(head);
        ScrollView sv=new ScrollView(this); LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL);
        String[][] data={{"1","Ozzy","767,728","44,984"},{"2","","683,864","17,993"},{"3","profes.","680,000","8,996"},{"4","","623,864","5,398"},{"5","","525,000","3,598"},{"6","","500,000","1,799"},{"7","King K..","495,000","1,799"},{"8","","193,640","1,799"},{"9","","150,000","1,799"},{"10","","145,000","1,799"}};
        for(String[] row: data){ TextView tv=new TextView(this); tv.setText(row[0]+"        "+row[1]+"        "+row[2]+"        "+row[3]); tv.setGravity(Gravity.CENTER); tv.setTextSize(13); tv.setPadding(0,8,0,8); list.addView(tv); }
        TextView me=new TextView(this); me.setText("10+        "+userName+"        "+rankingPoints+"        0"); me.setGravity(Gravity.CENTER); me.setTextSize(13); me.setPadding(0,8,0,8); me.setBackground(roundBg(0xFFFFD700,20)); list.addView(me);
        sv.addView(list); box.addView(sv);
        TextView time=new TextView(this); time.setText("Time Remaining for Settlement: 06:46:42"); time.setGravity(Gravity.CENTER); time.setTextSize(12); time.setPadding(0,10,0,0); box.addView(time);
        TextView close=new TextView(this); close.setText("✕ Close"); close.setGravity(Gravity.CENTER); close.setPadding(0,10,0,0); close.setOnClickListener(v->hideRanking()); box.addView(close);
        bg.addView(box); bg.setOnClickListener(v->hideRanking()); rankingPopup=bg; rootFrame.addView(bg);
    }
    void hideRanking(){ if(rankingPopup!=null){ rootFrame.removeView(rankingPopup); rankingPopup=null; } }
    void showIntro(){
        hideIntro();
        LinearLayout bg=new LinearLayout(this); bg.setOrientation(LinearLayout.VERTICAL); bg.setGravity(Gravity.CENTER); bg.setBackgroundColor(0xAA000000); bg.setLayoutParams(popupParams());
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setBackground(roundBg(OFFWHITE,30)); box.setPadding(20,20,20,20);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels*0.9),ViewGroup.LayoutParams.WRAP_CONTENT); bp.bottomMargin=120; box.setLayoutParams(bp);
        TextView t=new TextView(this); t.setText("Introduction"); t.setGravity(Gravity.CENTER); t.setTextSize(18);
        TextView body=new TextView(this); body.setText("1.All players contribute to the jackpot when placing bets in the game.\n\n2.Players accumulate their own points when they win the game.\n\n3.The more points a player has, the higher their rank on the leaderboard.\n\n4.The top 10 players in the daily rankings can claim the daily jackpot reward the next day. The daily reward is valid for 24 hours; please claim your reward within this period.\n\n5.The jackpot becomes invalid after the expiration date, please claim your reward promptly."); body.setTextSize(13); body.setPadding(0,12,0,0);
        TextView close=new TextView(this); close.setText("✕ Close"); close.setGravity(Gravity.CENTER); close.setPadding(0,12,0,0); close.setOnClickListener(v->hideIntro());
        box.addView(t); box.addView(body); box.addView(close); bg.addView(box); bg.setOnClickListener(v->hideIntro()); introPopup=bg; rootFrame.addView(bg);
    }
    void hideIntro(){ if(introPopup!=null){ rootFrame.removeView(introPopup); introPopup=null; } }

    LinearLayout createUI(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(0xFF00C2E8);
        LinearLayout top=new LinearLayout(this); top.setOrientation(LinearLayout.HORIZONTAL); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(16,10,16,6);
        TextView back=new TextView(this); back.setText("⬅"); back.setTextSize(24); back.setTextColor(0xFFFFFFFF); back.setPadding(0,0,16,0); back.setOnClickListener(v->finish());
        roundTxt=new TextView(this); roundTxt.setText("Round: "+round); roundTxt.setTextSize(14); roundTxt.setTextColor(0xFF333333);
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); roundTxt.setLayoutParams(tp);
        TextView music=new TextView(this); music.setText("🎵"); music.setTextSize(22); music.setPadding(0,0,16,0);
        TextView help=new TextView(this); help.setText("❓"); help.setTextSize(22); help.setOnClickListener(v->showIntro());
        top.addView(back); top.addView(roundTxt); top.addView(music); top.addView(help); root.addView(top);
        LinearLayout rankRow=new LinearLayout(this); rankRow.setOrientation(LinearLayout.HORIZONTAL); rankRow.setPadding(16,0,16,6);
        TextView rank=new TextView(this); rank.setText("🏆\nRanking"); rank.setGravity(Gravity.CENTER); rank.setTextSize(13); rank.setTextColor(0xFFFFD700); rank.setOnClickListener(v->showRanking()); rankRow.addView(rank); root.addView(rankRow);
        FrameLayout wheel=new FrameLayout(this); wheel.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,920));
        TextView wheelRing=new TextView(this); FrameLayout.LayoutParams ringLp=new FrameLayout.LayoutParams(660,660); ringLp.leftMargin=210; ringLp.topMargin=140; wheelRing.setLayoutParams(ringLp); wheelRing.setBackground(circleStroke(0x00FFFFFF,0xFFFFD700,12)); wheel.addView(wheelRing);
        int centerX=540, centerY=470, radius=350;
        for(int i=0;i<8;i++){
            final int idx=i; double angle=Math.toRadians(-90+i*45);
            int x=(int)(centerX+radius*Math.cos(angle))-100; int y=(int)(centerY+radius*Math.sin(angle))-80;
            LinearLayout booth=new LinearLayout(this); booth.setOrientation(LinearLayout.VERTICAL); booth.setGravity(Gravity.CENTER); booth.setBackground(roundBg(OFFWHITE,60)); booth.setPadding(6,6,6,6);
            FrameLayout.LayoutParams ip=new FrameLayout.LayoutParams(200,210); ip.leftMargin=x; ip.topMargin=y; booth.setLayoutParams(ip);
            TextView e=new TextView(this); e.setText(emojis[i]); e.setTextSize(32); e.setGravity(Gravity.CENTER);
            TextView m=new TextView(this); m.setText(mults[i]); m.setTextSize(18); m.setGravity(Gravity.CENTER); m.setTextColor(0xFFFF8C00);
            TextView n=new TextView(this); n.setText(names[i]); n.setTextSize(10); n.setGravity(Gravity.CENTER);
            TextView b=new TextView(this); b.setText("Bet: 0"); b.setTextSize(11); b.setGravity(Gravity.CENTER);
            booth.addView(e); booth.addView(m); booth.addView(n); booth.addView(b);
            if(i==hotIndex){ TextView hot=new TextView(this); hot.setText("Hot"); hot.setTextColor(0xFFFFFFFF); hot.setBackground(roundBg(0xFFFF0000,20)); hot.setTextSize(11); hot.setGravity(Gravity.CENTER); hot.setPadding(14,4,14,4); booth.addView(hot); }
            booth.setOnClickListener(v->placeBet(idx)); betViews[i]=b; boothViews[i]=booth; wheel.addView(booth);
        }
        LinearLayout center=new LinearLayout(this); center.setOrientation(LinearLayout.VERTICAL); center.setGravity(Gravity.CENTER); center.setBackground(circleStroke(0xFFFFD700,0xFFB8860B,8)); center.setPadding(8,8,8,8);
        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(260,260); cp.leftMargin=410; cp.topMargin=340; center.setLayoutParams(cp);
        TextView lion=new TextView(this); lion.setText("🦁"); lion.setTextSize(46); lion.setGravity(Gravity.CENTER);
        lionTimerTxt=new TextView(this); lionTimerTxt.setText("30"); lionTimerTxt.setTextSize(22); lionTimerTxt.setGravity(Gravity.CENTER); lionTimerTxt.setTextColor(0xFF8B4513); lionTimerTxt.setBackground(circleStroke(OFFWHITE,0xFFFFD700,6)); lionTimerTxt.setPadding(16,4,16,4);
        center.addView(lion); center.addView(lionTimerTxt); wheel.addView(center); root.addView(wheel);
        LinearLayout extraRow=new LinearLayout(this); extraRow.setOrientation(LinearLayout.HORIZONTAL); extraRow.setGravity(Gravity.CENTER); extraRow.setPadding(16,0,16,0);
        String[] extra={"🥗 salad","🍕 pizza"};
        for(String ex: extra){ TextView exv=new TextView(this); exv.setText(ex); exv.setGravity(Gravity.CENTER); exv.setTextSize(15); exv.setBackground(roundBg(OFFWHITE,50)); exv.setPadding(20,14,20,14); LinearLayout.LayoutParams exp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); exp.setMargins(12,0,12,0); exv.setLayoutParams(exp); extraRow.addView(exv); }
        root.addView(extraRow);
        LinearLayout bottom=new LinearLayout(this); bottom.setOrientation(LinearLayout.VERTICAL); bottom.setBackground(roundBg(0xFFD32F2F,30)); bottom.setPadding(16,12,16,12);
        LinearLayout myRow=new LinearLayout(this); myRow.setOrientation(LinearLayout.HORIZONTAL); myRow.setPadding(0,6,0,10);
        LinearLayout userBox=new LinearLayout(this); userBox.setOrientation(LinearLayout.HORIZONTAL); userBox.setGravity(Gravity.CENTER_VERTICAL); userBox.setBackground(roundBg(OFFWHITE,30)); userBox.setPadding(12,8,12,8);
        TextView userIcon=new TextView(this); userIcon.setText(userPic); userIcon.setTextSize(24); userIcon.setPadding(0,0,8,0);
        LinearLayout userText=new LinearLayout(this); userText.setOrientation(LinearLayout.VERTICAL);
        userBetNameTxt=new TextView(this); userBetNameTxt.setText(userName); userBetNameTxt.setTextSize(13);
        userBetCoinsTxt=new TextView(this); userBetCoinsTxt.setText("🪙 "+myBetThisRound); userBetCoinsTxt.setTextSize(13);
        userText.addView(userBetNameTxt); userText.addView(userBetCoinsTxt);
        userBetTotalTxt=new TextView(this); userBetTotalTxt.setText("My Bet: 0"); userBetTotalTxt.setTextSize(12); userBetTotalTxt.setGravity(Gravity.CENTER);
        userBox.addView(userIcon); userBox.addView(userText); userBox.addView(userBetTotalTxt);
        LinearLayout.LayoutParams ozp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); ozp.setMargins(0,0,10,0); userBox.setLayoutParams(ozp);
        TextView myHist=new TextView(this); myHist.setText("📝 MyHistory ▶"); myHist.setBackground(roundBg(OFFWHITE,30)); myHist.setPadding(20,16,20,16); myHist.setGravity(Gravity.CENTER); myHist.setOnClickListener(v->showMyHistory());
        LinearLayout.LayoutParams mhp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); mhp.setMargins(10,0,0,0); myHist.setLayoutParams(mhp);
        myRow.addView(userBox); myRow.addView(myHist); bottom.addView(myRow);
        LinearLayout resRow=new LinearLayout(this); resRow.setOrientation(LinearLayout.HORIZONTAL); resRow.setGravity(Gravity.CENTER_VERTICAL);
        resultTxt=new TextView(this); resultTxt.setText("Result  "); resultTxt.setTextColor(0xFFFFFFFF); resultTxt.setTextSize(14); resRow.addView(resultTxt);
        HorizontalScrollView hs=new HorizontalScrollView(this); resultHistoryLayout=new LinearLayout(this); resultHistoryLayout.setOrientation(LinearLayout.HORIZONTAL); hs.addView(resultHistoryLayout); resRow.addView(hs); bottom.addView(resRow);
        LinearLayout chips=new LinearLayout(this); chips.setOrientation(LinearLayout.HORIZONTAL); chips.setGravity(Gravity.CENTER); chips.setPadding(0,14,0,0);
        for(int i=0;i<4;i++){ final int idx=i; final int v=chipVals[i]; final String label=chipLabels[i]; TextView b=new TextView(this); b.setText("🪙\n"+label); b.setGravity(Gravity.CENTER); b.setTextSize(16); b.setTextColor(0xFFFFFFFF); b.setBackground(roundBg(PINK,50)); b.setPadding(10,20,10,20); LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); clp.setMargins(8,0,8,0); b.setLayoutParams(clp); b.setOnClickListener(vv->{ chip=v; selectedChipIndex=idx; updateChipUI(); }); chipViews[i]=b; chips.addView(b); }
        bottom.addView(chips);
        LinearLayout lastRow=new LinearLayout(this); lastRow.setOrientation(LinearLayout.HORIZONTAL); lastRow.setGravity(Gravity.CENTER_VERTICAL); lastRow.setPadding(0,12,0,0);
        LinearLayout wallet=new LinearLayout(this); wallet.setOrientation(LinearLayout.HORIZONTAL); wallet.setGravity(Gravity.CENTER_VERTICAL); wallet.setBackground(roundBg(OFFWHITE,30)); wallet.setPadding(12,8,12,8);
        TextView wIcon=new TextView(this); wIcon.setText(userPic); wIcon.setTextSize(20);
        LinearLayout wText=new LinearLayout(this); wText.setOrientation(LinearLayout.VERTICAL);
        walletNameTxt=new TextView(this); walletNameTxt.setText(userName); walletNameTxt.setTextSize(12);
        balanceTxt=new TextView(this); balanceTxt.setText(""+balance); balanceTxt.setTextSize(16); wText.addView(walletNameTxt); wText.addView(balanceTxt);
        TextView plus=new TextView(this); plus.setText("+"); plus.setTextSize(22); plus.setTextColor(0xFFFFFFFF); plus.setBackground(circleBg(0xFF4CAF50)); plus.setPadding(16,4,16,4); plus.setOnClickListener(v->Toast.makeText(this,"Recharge - Next Update",Toast.LENGTH_SHORT).show());
        wallet.addView(wIcon); wallet.addView(wText); wallet.addView(plus);
        LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); wallet.setLayoutParams(wp);
        LinearLayout earnBox=new LinearLayout(this); earnBox.setOrientation(LinearLayout.VERTICAL); earnBox.setGravity(Gravity.CENTER);
        TextView earnLabel=new TextView(this); earnLabel.setText("Today's Earnings"); earnLabel.setTextColor(0xFFFFFFFF); earnLabel.setTextSize(12);
        todayTxt=new TextView(this); todayTxt.setText("🪙 "+totalToday); todayTxt.setTextColor(0xFF333333); todayTxt.setBackground(roundBg(OFFWHITE,30)); todayTxt.setGravity(Gravity.CENTER); todayTxt.setPadding(20,8,20,8);
        earnBox.addView(earnLabel); earnBox.addView(todayTxt);
        LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); earnBox.setLayoutParams(ep);
        Button auto=new Button(this); auto.setText("auto bet"); auto.setTextColor(0xFFFFFFFF); auto.setBackground(roundBg(0xFF4CAF50,50)); LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); ap.setMargins(10,0,0,0); auto.setLayoutParams(ap); auto.setOnClickListener(v->Toast.makeText(this,"Auto Bet coming soon",Toast.LENGTH_SHORT).show());
        lastRow.addView(wallet); lastRow.addView(earnBox); lastRow.addView(auto); bottom.addView(lastRow);
        statusTxt=new TextView(this); statusTxt.setText("Tap a slot to place Bet"); statusTxt.setTextColor(0xFFFFFF00); statusTxt.setGravity(Gravity.CENTER); bottom.addView(statusTxt); root.addView(bottom); return root;
    }
    void placeBet(int idx){
        if(!bettingOpen){ Toast.makeText(this,"Wait for next Round",Toast.LENGTH_SHORT).show(); return; }
        if(balance<chip){ Toast.makeText(this,"Not enough coins",Toast.LENGTH_SHORT).show(); return; }
        bets[idx]+=chip; balance-=chip; myBetThisRound+=chip; rankingPoints+=chip/100; totalToday+=chip; saveWallet(); updateUI();
    }
    int getTotalBet(){ int t=0; for(int b:bets) t+=b; return t; }
    void updateUI(){ updateWalletUI(); for(int i=0;i<8;i++) betViews[i].setText("Bet: "+bets[i]); }
    void startRound(){
        bettingOpen=true; timeLeft=30; myBetThisRound=0; updateWalletUI();
        roundTxt.setText("Round: "+round); statusTxt.setText("Tap a slot to place Bet");
        if(lionTimerTxt!=null) lionTimerTxt.setText("30");
        handler.postDelayed(new Runnable(){ @Override public void run(){ timeLeft--; if(lionTimerTxt!=null) lionTimerTxt.setText(""+timeLeft); if(timeLeft>0){ handler.postDelayed(this,1000); } else { finishRound(); } } },1000);
    }
    void finishRound(){
        bettingOpen=false; showDrawing();
        int rand=(int)(Math.random()*100); String special=""; int winner;
        if(rand<8){ special="🥗 SALAD"; int[] veg={4,5,6,7}; winner=veg[(int)(Math.random()*4)]; }
        else if(rand<16){ special="🍕 PIZZA"; int[] meat={0,1,2,3}; winner=meat[(int)(Math.random()*4)]; }
        else winner=(int)(Math.random()*8);
        int totalBet=getTotalBet(); int winAmount=0;
        if(special.contains("SALAD")){ for(int i=4;i<8;i++) winAmount+=bets[i]*getMult(i); }
        else if(special.contains("PIZZA")){ for(int i=0;i<4;i++) winAmount+=bets[i]*getMult(i); }
        else winAmount=bets[winner]*getMult(winner);
        final int fw=winner; final int fwin=winAmount; final String fs=special;
        String date=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault()).format(new Date());
        HistoryItem item=new HistoryItem(date,round,emojis[fw],fwin);
        for(int i=0;i<8;i++){ if(bets[i]>0) item.betLines.add(emojis[i]+"  "+names[i]+"  🪙 "+bets[i]); }
        if(!item.betLines.isEmpty()){ myHistory.add(0,item); if(myHistory.size()>20) myHistory.remove(20); }
        // Drawing Animation - Dhireshire Winner er dike - 3 Step
        handler.postDelayed(()->{
            hideDrawing();
            for(int i=0;i<8;i++){ if(boothViews[i]==null) continue; if(i==fw) boothViews[i].setBackground(roundStroke(0xFFFFD700,0xFFFF0000,10,60)); else boothViews[i].setBackground(roundBg(0xFFDDDDDD,60)); }
            balance+=fwin; saveWallet(); addResult(emojis[fw]);
            resultTxt.setText("Result: "+emojis[fw]); statusTxt.setText("WIN: "+emojis[fw]+" "+names[fw]+" "+fs);
            showResultPopup(fw,fwin,totalBet,fs); if(fwin>0) showWinBanner(fwin);
            for(int i=0;i<8;i++) bets[i]=0; updateUI(); round++; saveWallet();
            handler.postDelayed(()->{ hideResultPopup(); startRound(); },3000);
        },2000);
    }
    int getMult(int i){ int[] m={45,25,15,10,5,5,5,5}; return m[i]; }
}
