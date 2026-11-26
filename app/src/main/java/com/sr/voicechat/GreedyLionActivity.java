package com.sr.voicechat;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class GreedyLionActivity extends AppCompatActivity {

    TextView roundTxt, balanceTxt, walletNameTxt, statusTxt, resultTxt, todayTxt, lionTimerTxt;
    TextView userBetNameTxt, userBetCoinsTxt, userBetTotalTxt;
    TextView[] betViews = new TextView[8];
    TextView[] chipViews = new TextView[4];
    LinearLayout[] boothViews = new LinearLayout[8];
    LinearLayout resultHistoryLayout;
    long[] bets = new long[8];

    // Bet - 5K, 10K, 100K, 500K - 1K nai - Default 5K
    int[] chipVals = {5000,10000,100000,500000};
    String[] chipLabels = {"5K","10K","100K","500K"};
    int chip = 5000; int selectedChipIndex = 0;

    // ===== MINE WALLET = GAME WALLET - EKTAI =====
    long balance = 0; long myBetThisRound = 0; long rankingPoints = 0; long totalToday = 0;
    String userName = "Guest"; String userId = "";
    // FIX: userPic ar TextView na - eta Photo URL / Base64 - ImageView e jabe
    String userPhoto = "";
    ImageView userPicView, walletPicView;
    int round = 1; int timeLeft = 30; boolean bettingOpen = true;
    int lightIndex = 0; boolean lightRunning = true; boolean isDrawing = false;
    Handler handler = new Handler(); Handler lightHandler = new Handler();
    FrameLayout rootFrame;
    View drawingOverlay, resultPopup, rankingPopup, introPopup, winBanner, historyPopup;
    ArrayList<String> resultHistory = new ArrayList<>();
    ArrayList<HistoryItem> myHistory = new ArrayList<>();
    boolean autoBetOn = false;
    int lastBetSlot = -1;

    static class RankEntry { String name; long points; long reward; RankEntry(String n,long p,long r){name=n;points=p;reward=r;} }
    ArrayList<RankEntry> rankList = new ArrayList<>();
    String rankMode = "Daily"; String rankSub = "Today";
    TextView settlementTxt;
    long[] rankRewards = {44984,17993,8996,5398,3598,1799,1799,1799,1799,1799};
    int[] rankColors = {0xFFFFD700,0xFF87CEEB,0xFFFFA500,0xFF98FB98,0xFFFF69B4,0xFFDDA0DD,0xFF90EE90,0xFFFFB6C1,0xFFADD8E6,0xFFFFE4B5};

    static class HistoryItem {
        String date; int round; ArrayList<String> betLines = new ArrayList<>();
        String winEmoji; long winAmount;
        HistoryItem(String d,int r,String w,long amt){ date=d; round=r; winEmoji=w; winAmount=amt; }
    }

    String[] names = {"Chicken","Octopus","Shark","Meat","Grapes","Salad","Corn","Strawberry"};
    String[] emojis = {"🍗","🐙","🦈","🥩","🍇","🥬","🌽","🍓"};
    String[] mults = {"x45","x25","x15","x10","x5","x5","x5","x5"};
    int hotIndex = 4;
    int OFFWHITE = 0xFFFFF8E1;
    int PINK = 0xFFFF69B4;
    int LIGHT_YELLOW = 0xFFFFF59D;

    GradientDrawable roundBg(int c,int r){ GradientDrawable d=new GradientDrawable(); d.setColor(c); d.setCornerRadius(r); return d; }
    GradientDrawable roundStroke(int c,int sc,int sw,int r){ GradientDrawable d=new GradientDrawable(); d.setColor(c); d.setStroke(sw,sc); d.setCornerRadius(r); return d; }
    GradientDrawable circleBg(int c){ GradientDrawable d=new GradientDrawable(); d.setShape(GradientDrawable.OVAL); d.setColor(c); return d; }
    GradientDrawable circleStroke(int c,int sc,int sw){ GradientDrawable d=new GradientDrawable(); d.setShape(GradientDrawable.OVAL); d.setColor(c); d.setStroke(sw,sc); return d; }
    String fmt(long v){ return String.format(Locale.US,"%,d",v); }

    int calcGlobalRound(){
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Riyadh"));
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        int secondsSince7pm;
        if(hour >= 19){ secondsSince7pm = (hour-19)*3600 + minute*60 + sec; }
        else { secondsSince7pm = (hour+5)*3600 + minute*60 + sec; }
        return (secondsSince7pm / 35) + 1;
    }
    int calcTimeLeftInRound(){
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Riyadh"));
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        int secondsSince7pm;
        if(hour >= 19) secondsSince7pm = (hour-19)*3600 + minute*60 + sec;
        else secondsSince7pm = (hour+5)*3600 + minute*60 + sec;
        int passed = secondsSince7pm % 35;
        if(passed < 30) return 30 - passed;
        return 30;
    }

    // ===== FIX: PROFILE = SR_USER (Main App) THEKE - NAME + ID + PHOTO =====
    void loadWallet(){
        SharedPreferences main = getSharedPreferences("main_wallet", MODE_PRIVATE);
        SharedPreferences sr = getSharedPreferences("SR_USER", MODE_PRIVATE);

        // 1. Intent theke asle Intent ke priority dao
        if(getIntent()!=null){
            if(getIntent().hasExtra("user_name")) userName = getIntent().getStringExtra("user_name");
            if(getIntent().hasExtra("user_id")) userId = getIntent().getStringExtra("user_id");
            if(getIntent().hasExtra("user_pic")) userPhoto = getIntent().getStringExtra("user_pic");
            if(getIntent().hasExtra("user_photo")) userPhoto = getIntent().getStringExtra("user_photo");
            if(getIntent().hasExtra("wallet_coins")) balance = getIntent().getLongExtra("wallet_coins",0);
        }
        // 2. Intent e na thakle - SR_USER (Main App er Profile) theke nao - Eitai asol
        if(userName==null || userName.equals("Guest") || userName.isEmpty()){
            String n = sr.getString("USER_NAME", "");
            if(n==null||n.isEmpty()) n = sr.getString("PROFILE_NAME", "");
            if(n!=null && !n.isEmpty()) userName = n;
            else userName = main.getString("user_name","Guest");
        }
        if(userId==null || userId.isEmpty()){
            String id = sr.getString("MY_9_DIGIT_ID", "");
            if(id==null||id.isEmpty()) id = sr.getString("PROFILE_ID", "");
            if(id==null||id.isEmpty()) id = main.getString("user_id","");
            userId = id!=null?id:"";
        }
        if(userPhoto==null || userPhoto.isEmpty() || userPhoto.equals("👤")){
            String ph = sr.getString("USER_PHOTO", "");
            if(ph==null||ph.isEmpty()) ph = sr.getString("PROFILE_PHOTO", "");
            if(ph==null||ph.isEmpty()) ph = sr.getString("USER_IMAGE", "");
            if(ph==null||ph.isEmpty()) ph = main.getString("user_pic","");
            // FIX: Jodi purono code emoji save kore rakhe, oita photo na
            if(ph!=null && (ph.equals("👤")||ph.length()<5)) ph = "";
            userPhoto = ph!=null?ph:"";
        }
        // 3. Coins - SR_USER e thakle oita, na thakle main_wallet
        if(!getIntent().hasExtra("wallet_coins")){
            if(sr.contains("wallet_coins")) balance = sr.getLong("wallet_coins",0);
            else if(sr.contains("USER_COINS")) balance = sr.getLong("USER_COINS",0);
            else balance = main.getLong("wallet_coins",0);
        }
        rankingPoints = main.getLong("ranking_points",0);
        totalToday = main.getLong("total_today",0);
        round = calcGlobalRound();
        rankList.clear();
        if(rankingPoints>0) rankList.add(new RankEntry(userName, rankingPoints, 0));
    }
    void saveWallet(){
        // FIX: Dui jaigatei save - Main App + Game - Wallet ek thakbe
        getSharedPreferences("main_wallet",MODE_PRIVATE).edit().putLong("wallet_coins",balance).putString("user_name",userName).putString("user_id",userId).putString("user_pic",userPhoto).putLong("ranking_points",rankingPoints).putLong("total_today",totalToday).putInt("last_round",round).apply();
        getSharedPreferences("SR_USER",MODE_PRIVATE).edit().putLong("wallet_coins",balance).putLong("USER_COINS",balance).putString("USER_NAME",userName).apply();
        updateWalletUI();
    }
    // FIX: Photo kokhono TextView e jabe na - sudhu ImageView e
    void setProfileImage(ImageView iv){
        if(iv==null) return;
        try{
            if(userPhoto!=null && !userPhoto.isEmpty() && !userPhoto.equals("👤")){
                if(userPhoto.startsWith("http")){
                    Glide.with(this).load(userPhoto).circleCrop().into(iv);
                } else {
                    // Base64
                    byte[] b = Base64.decode(userPhoto, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(b,0,b.length);
                    if(bmp!=null){ iv.setImageBitmap(bmp); return; }
                    Glide.with(this).load(b).circleCrop().into(iv);
                }
            } else {
                iv.setImageResource(R.mipmap.ic_launcher);
            }
        }catch(Exception e){ iv.setImageResource(R.mipmap.ic_launcher); }
    }
    void updateWalletUI(){
        if(walletNameTxt!=null) walletNameTxt.setText(userName);
        if(balanceTxt!=null) balanceTxt.setText(fmt(balance));
        if(userBetNameTxt!=null) userBetNameTxt.setText(userName);
        // FIX: Uporer card e My Bet dekhabe, Wallet e Total Balance dekhabe - 2ta alada
        if(userBetCoinsTxt!=null) userBetCoinsTxt.setText("🪙 "+fmt(balance));
        if(userBetTotalTxt!=null) userBetTotalTxt.setText("My Bet: "+fmt(myBetThisRound));
        if(todayTxt!=null) todayTxt.setText("🪙 "+fmt(totalToday));
        setProfileImage(userPicView);
        setProfileImage(walletPicView);
    }
    @Override protected void onResume(){ super.onResume(); loadWallet(); updateWalletUI(); }

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        loadWallet();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        rootFrame = new FrameLayout(this);
        rootFrame.addView(createUI());
        setContentView(rootFrame);
        updateWalletUI(); updateChipUI(); refreshResultHistory();
        if(getWindow()!=null){ getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(getResources().getDisplayMetrics().heightPixels*0.92)); getWindow().setGravity(Gravity.BOTTOM); getWindow().setBackgroundDrawableResource(android.R.color.transparent); }
        startRound(); startLightSlow(); startSettlementCountdown();
    }

    void updateChipUI(){
        for(int i=0;i<4;i++){
            if(chipViews[i]==null) continue;
            if(i==selectedChipIndex){ chipViews[i].setBackground(roundStroke(LIGHT_YELLOW, 0xFFFFD700, 4, 50)); chipViews[i].setTextColor(0xFF333333); }
            else { chipViews[i].setBackground(roundBg(PINK, 50)); chipViews[i].setTextColor(0xFFFFFFFF); }
        }
    }
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
                lightHandler.postDelayed(this, 900);
            }
        }, 900);
    }
    void stopLight(){ lightRunning = false; }
    void resumeLight(){ lightRunning = true; }

    String getSettlementRemaining(){
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Asia/Riyadh"));
        Calendar next = Calendar.getInstance(TimeZone.getTimeZone("Asia/Riyadh"));
        next.set(Calendar.HOUR_OF_DAY,19); next.set(Calendar.MINUTE,0); next.set(Calendar.SECOND,0); next.set(Calendar.MILLISECOND,0);
        if(!next.after(now)) next.add(Calendar.DAY_OF_YEAR,1);
        long diff = next.getTimeInMillis() - now.getTimeInMillis();
        long h = diff/3600000, m = (diff%3600000)/60000, s = (diff%60000)/1000;
        return String.format(Locale.US,"%02d:%02d:%02d",h,m,s);
    }
    void startSettlementCountdown(){
        handler.postDelayed(new Runnable(){ @Override public void run(){
            if(settlementTxt!=null) settlementTxt.setText("Time Remaining for Settlement: "+getSettlementRemaining());
            handler.postDelayed(this,1000);
        }},1000);
    }

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
        isDrawing = true; stopLight();
        LinearLayout lay=new LinearLayout(this); lay.setOrientation(LinearLayout.VERTICAL); lay.setGravity(Gravity.CENTER); lay.setBackgroundColor(0x88000000); lay.setLayoutParams(popupParams());
        TextView t=new TextView(this); t.setText("Drawing in progress, please wait"); t.setTextColor(0xFFFFFFFF); t.setTextSize(18); t.setGravity(Gravity.CENTER);
        lay.addView(t); drawingOverlay=lay; rootFrame.addView(lay);
    }
    void hideDrawing(){ if(drawingOverlay!=null){ rootFrame.removeView(drawingOverlay); drawingOverlay=null; } isDrawing=false; resumeLight(); }

    void showResultPopup(int winner,long winAmount,long totalBet,String special){
        hideResultPopup();
        LinearLayout bg=new LinearLayout(this); bg.setOrientation(LinearLayout.VERTICAL); bg.setGravity(Gravity.CENTER); bg.setBackgroundColor(0xAA000000); bg.setLayoutParams(popupParams());
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER);
        box.setBackground(roundBg(0xFF8B0000,30)); box.setPadding(24,24,24,24);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels*0.92), ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.bottomMargin=100; box.setLayoutParams(bp);
        TextView stage=new TextView(this); stage.setText("⭐⭐⭐⭐⭐"); stage.setGravity(Gravity.CENTER); stage.setTextColor(0xFFFFD700); stage.setTextSize(18);
        TextView lion=new TextView(this); lion.setText("🦁  "+emojis[winner]); lion.setTextSize(64); lion.setGravity(Gravity.CENTER);
        TextView r=new TextView(this); r.setText("Round "+round+" Result "+special); r.setTextColor(0xFFFFFFFF); r.setTextSize(18); r.setGravity(Gravity.CENTER);
        TextView e=new TextView(this); e.setText("Current Earnings:  "+fmt(winAmount)); e.setTextColor(0xFFFFFF00); e.setTextSize(17); e.setGravity(Gravity.CENTER);
        TextView b=new TextView(this); b.setText("Current Bet:  "+fmt(totalBet)); b.setTextColor(0xFFFFFFFF); b.setTextSize(17); b.setGravity(Gravity.CENTER);
        TextView rank=new TextView(this); rank.setText("Ranking in this Round:"); rank.setTextColor(0xFFFFFFFF); rank.setTextSize(14); rank.setGravity(Gravity.CENTER);
        TextView me=new TextView(this); me.setText(userName+" - Points: "+fmt(rankingPoints)); me.setTextColor(0xFFFFD700); me.setTextSize(14); me.setGravity(Gravity.CENTER);
        box.addView(stage); box.addView(lion); box.addView(r); box.addView(e); box.addView(b); box.addView(rank); box.addView(me);
        bg.addView(box); bg.setOnClickListener(v->hideResultPopup());
        resultPopup=bg; rootFrame.addView(bg);
    }
    void hideResultPopup(){ if(resultPopup!=null){ rootFrame.removeView(resultPopup); resultPopup=null; } }
    void showWinBanner(long winAmount){
        hideWinBanner();
        TextView banner=new TextView(this); banner.setText("🏆 Win  💜 "+userName+" x"+fmt(winAmount)); banner.setTextColor(0xFF4B0082); banner.setTextSize(18); banner.setGravity(Gravity.CENTER);
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
        else { for(HistoryItem item : myHistory){ LinearLayout head=new LinearLayout(this); head.setOrientation(LinearLayout.HORIZONTAL); TextView date=new TextView(this); date.setText(item.date); date.setTextSize(12); TextView rnd=new TextView(this); rnd.setText("Round: "+item.round); rnd.setTextSize(12); rnd.setGravity(Gravity.RIGHT); LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); date.setLayoutParams(dp); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); rnd.setLayoutParams(rp); head.addView(date); head.addView(rnd); list.addView(head); for(String line : item.betLines){ TextView tv=new TextView(this); tv.setText(line); tv.setTextSize(14); tv.setPadding(8,4,8,4); list.addView(tv); } TextView win=new TextView(this); win.setText(item.winEmoji+"  🪙 "+fmt(item.winAmount)); win.setTextSize(16); win.setGravity(Gravity.RIGHT); win.setPadding(0,4,0,12); list.addView(win); View line=new View(this); line.setBackgroundColor(0xFFDDDDDD); line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,2)); list.addView(line); } }
        sv.addView(list); box.addView(sv);
        TextView close=new TextView(this); close.setText("✕ Close"); close.setGravity(Gravity.CENTER); close.setPadding(0,12,0,0); close.setOnClickListener(v->hideMyHistory()); box.addView(close);
        bg.addView(box); bg.setOnClickListener(v->hideMyHistory()); historyPopup=bg; rootFrame.addView(bg);
    }
    void hideMyHistory(){ if(historyPopup!=null){ rootFrame.removeView(historyPopup); historyPopup=null; } }

    LinearLayout rankRowView(int rank, String name, long points, long reward, boolean isMe){
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(roundBg(rankColors[rank-1],20)); row.setPadding(10,10,10,10);
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT); rp.setMargins(0,4,0,4); row.setLayoutParams(rp);
        TextView r=new TextView(this); r.setText(""+rank); r.setGravity(Gravity.CENTER); r.setTextSize(14);
        TextView n=new TextView(this); n.setText(name==null?"":name); n.setGravity(Gravity.CENTER); n.setTextSize(13);
        TextView p=new TextView(this); p.setText(points>0?fmt(points):""); p.setGravity(Gravity.CENTER); p.setTextSize(13);
        TextView rw=new TextView(this); rw.setText(points>0?fmt(reward):""); rw.setGravity(Gravity.CENTER); rw.setTextSize(13);
        r.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,0.8f));
        n.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1.5f));
        p.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1.2f));
        rw.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1.2f));
        if(isMe){ r.setTextColor(0xFFD32F2F); n.setTextColor(0xFFD32F2F); }
        row.addView(r); row.addView(n); row.addView(p); row.addView(rw);
        return row;
    }
    void showRanking(){
        hideRanking();
        LinearLayout bg=new LinearLayout(this); bg.setOrientation(LinearLayout.VERTICAL); bg.setGravity(Gravity.CENTER); bg.setBackgroundColor(0xAA000000); bg.setLayoutParams(popupParams());
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(roundBg(OFFWHITE,30)); box.setPadding(16,16,16,16);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels*0.94), (int)(getResources().getDisplayMetrics().heightPixels*0.72));
        bp.bottomMargin=80; box.setLayoutParams(bp);
        LinearLayout topRow=new LinearLayout(this); topRow.setOrientation(LinearLayout.HORIZONTAL); topRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView daily=new TextView(this); daily.setText("  Daily Rankings  "); daily.setTextSize(15); daily.setPadding(16,10,16,10);
        daily.setBackground(roundBg(rankMode.equals("Daily")?0xFFFFD700:0xFFFFFFFF,30));
        daily.setOnClickListener(v->{ rankMode="Daily"; rankSub="Today"; showRanking(); });
        TextView weekly=new TextView(this); weekly.setText("  Weekly Rankings  "); weekly.setTextSize(15); weekly.setPadding(16,10,16,10);
        weekly.setBackground(roundBg(rankMode.equals("Weekly")?0xFFFFD700:0xFFFFFFFF,30));
        weekly.setOnClickListener(v->{ rankMode="Weekly"; rankSub="This Week"; showRanking(); });
        TextView q=new TextView(this); q.setText(" ? "); q.setTextSize(18); q.setBackground(circleBg(0xFFFFD700)); q.setPadding(14,6,14,6); q.setOnClickListener(v->showIntro());
        LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); daily.setLayoutParams(dp);
        LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); weekly.setLayoutParams(wp);
        topRow.addView(daily); topRow.addView(weekly); topRow.addView(q); box.addView(topRow);
        LinearLayout tabs=new LinearLayout(this); tabs.setOrientation(LinearLayout.HORIZONTAL); tabs.setGravity(Gravity.CENTER); tabs.setPadding(0,10,0,10);
        String[] subs = rankMode.equals("Daily") ? new String[]{"Today","Yesterday"} : new String[]{"This Week","Last Week"};
        for(String s: subs){ TextView tv=new TextView(this); tv.setText("  "+s+"  "); tv.setPadding(20,10,20,10); tv.setBackground(roundBg(rankSub.equals(s)?0xFFFFD700:0xFFFFFFFF,30)); tv.setOnClickListener(v->{ rankSub=s; showRanking(); }); tabs.addView(tv); }
        box.addView(tabs);
        LinearLayout head=new LinearLayout(this); head.setOrientation(LinearLayout.HORIZONTAL);
        String[] hs={"Rank","Name","Points","Rewards"}; float[] ws={0.8f,1.5f,1.2f,1.2f};
        for(int i=0;i<4;i++){ TextView h=new TextView(this); h.setText(hs[i]); h.setGravity(Gravity.CENTER); h.setTextSize(13); h.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,ws[i])); head.addView(h); }
        box.addView(head);
        ScrollView sv=new ScrollView(this); LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL);
        for(int i=1;i<=10;i++){
            String nm=""; long pts=0; long rew=0; boolean isMe=false;
            if(i<=rankList.size()){ RankEntry e=rankList.get(i-1); nm=e.name; pts=e.points; rew=rankRewards[i-1]; isMe=nm.equals(userName); }
            list.addView(rankRowView(i,nm,pts,rew,isMe));
        }
        boolean inTop=false; for(RankEntry e: rankList) if(e.name.equals(userName)) inTop=true;
        if(!inTop && rankingPoints>0){
            LinearLayout meRow=new LinearLayout(this); meRow.setOrientation(LinearLayout.HORIZONTAL); meRow.setGravity(Gravity.CENTER_VERTICAL);
            meRow.setBackground(roundBg(0xFFFFD700,20)); meRow.setPadding(10,10,10,10);
            TextView r=new TextView(this); r.setText("10+"); r.setGravity(Gravity.CENTER);
            TextView n=new TextView(this); n.setText(userName); n.setGravity(Gravity.CENTER);
            TextView p=new TextView(this); p.setText(fmt(rankingPoints)); p.setGravity(Gravity.CENTER);
            TextView rw=new TextView(this); rw.setText("0"); rw.setGravity(Gravity.CENTER);
            r.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,0.8f));
            n.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1.5f));
            p.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1.2f));
            rw.setLayoutParams(new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1.2f));
            meRow.addView(r); meRow.addView(n); meRow.addView(p); meRow.addView(rw); list.addView(meRow);
        }
        sv.addView(list); box.addView(sv);
        settlementTxt=new TextView(this); settlementTxt.setText("Time Remaining for Settlement: "+getSettlementRemaining()); settlementTxt.setGravity(Gravity.CENTER); settlementTxt.setTextSize(12); settlementTxt.setPadding(0,10,0,0); box.addView(settlementTxt);
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
        // FIX: Ekhane TextView na - ImageView - Base64 ar lekhai asbe na
        userPicView=new ImageView(this);
        LinearLayout.LayoutParams picLp=new LinearLayout.LayoutParams(72,72); picLp.setMargins(0,0,8,0); userPicView.setLayoutParams(picLp);
        userPicView.setBackground(circleBg(0xFFFFFFFF)); userPicView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout userText=new LinearLayout(this); userText.setOrientation(LinearLayout.VERTICAL);
        userBetNameTxt=new TextView(this); userBetNameTxt.setText(userName); userBetNameTxt.setTextSize(13);
        userBetCoinsTxt=new TextView(this); userBetCoinsTxt.setText("🪙 "+fmt(balance)); userBetCoinsTxt.setTextSize(13);
        userText.addView(userBetNameTxt); userText.addView(userBetCoinsTxt);
        userBetTotalTxt=new TextView(this); userBetTotalTxt.setText("My Bet: 0"); userBetTotalTxt.setTextSize(12); userBetTotalTxt.setGravity(Gravity.CENTER);
        userBox.addView(userPicView); userBox.addView(userText); userBox.addView(userBetTotalTxt);
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
        // FIX: Wallet eo ImageView
        walletPicView=new ImageView(this);
        LinearLayout.LayoutParams wPicLp=new LinearLayout.LayoutParams(64,64); wPicLp.setMargins(0,0,6,0); walletPicView.setLayoutParams(wPicLp);
        walletPicView.setBackground(circleBg(0xFFFFFFFF)); walletPicView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout wText=new LinearLayout(this); wText.setOrientation(LinearLayout.VERTICAL);
        walletNameTxt=new TextView(this); walletNameTxt.setText(userName); walletNameTxt.setTextSize(12);
        balanceTxt=new TextView(this); balanceTxt.setText(fmt(balance)); balanceTxt.setTextSize(16); wText.addView(walletNameTxt); wText.addView(balanceTxt);
        TextView plus=new TextView(this); plus.setText("+"); plus.setTextSize(22); plus.setTextColor(0xFFFFFFFF); plus.setBackground(circleBg(0xFF4CAF50)); plus.setPadding(16,4,16,4); plus.setOnClickListener(v->Toast.makeText(this,"Recharge - Next Update",Toast.LENGTH_SHORT).show());
        wallet.addView(walletPicView); wallet.addView(wText); wallet.addView(plus);
        LinearLayout.LayoutParams wp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); wallet.setLayoutParams(wp);
        LinearLayout earnBox=new LinearLayout(this); earnBox.setOrientation(LinearLayout.VERTICAL); earnBox.setGravity(Gravity.CENTER);
        TextView earnLabel=new TextView(this); earnLabel.setText("Today's Earnings"); earnLabel.setTextColor(0xFFFFFFFF); earnLabel.setTextSize(12);
        todayTxt=new TextView(this); todayTxt.setText("🪙 "+fmt(totalToday)); todayTxt.setTextColor(0xFF333333); todayTxt.setBackground(roundBg(OFFWHITE,30)); todayTxt.setGravity(Gravity.CENTER); todayTxt.setPadding(20,8,20,8);
        earnBox.addView(earnLabel); earnBox.addView(todayTxt);
        LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); earnBox.setLayoutParams(ep);
        Button auto=new Button(this); auto.setText("auto bet"); auto.setTextColor(0xFFFFFFFF); auto.setBackground(roundBg(0xFF4CAF50,50)); LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); ap.setMargins(10,0,0,0); auto.setLayoutParams(ap);
        auto.setOnClickListener(v->{
            autoBetOn = !autoBetOn;
            auto.setText(autoBetOn ? "auto ON" : "auto bet");
            auto.setBackground(roundBg(autoBetOn ? 0xFFFF9800 : 0xFF4CAF50, 50));
            if(autoBetOn && bettingOpen && lastBetSlot >= 0 && balance >= chip){
                placeBet(lastBetSlot);
            }
            if(!autoBetOn) Toast.makeText(this,"Auto Bet OFF",Toast.LENGTH_SHORT).show();
        });
        lastRow.addView(wallet); lastRow.addView(earnBox); lastRow.addView(auto); bottom.addView(lastRow);
        statusTxt=new TextView(this); statusTxt.setText("Tap a slot to place Bet"); statusTxt.setTextColor(0xFFFFFF00); statusTxt.setGravity(Gravity.CENTER); bottom.addView(statusTxt); root.addView(bottom); return root;
    }
    void placeBet(int idx){
        if(!bettingOpen){ Toast.makeText(this,"Wait for next Round",Toast.LENGTH_SHORT).show(); return; }
        if(balance<chip){ Toast.makeText(this,"Not enough coins",Toast.LENGTH_SHORT).show(); return; }
        bets[idx]+=chip; balance-=chip; myBetThisRound+=chip; lastBetSlot=idx;
        rankingPoints+=chip/100;
        saveWallet(); updateUI();
        rankList.clear(); if(rankingPoints>0) rankList.add(new RankEntry(userName,rankingPoints,0));
    }
    long getTotalBet(){ long t=0; for(long b:bets) t+=b; return t; }
    void updateUI(){ updateWalletUI(); for(int i=0;i<8;i++) betViews[i].setText("Bet: "+fmt(bets[i])); }
    void startRound(){
        round = calcGlobalRound();
        bettingOpen=true; timeLeft=calcTimeLeftInRound(); myBetThisRound=0; updateWalletUI();
        roundTxt.setText("Round: "+round); statusTxt.setText("Tap a slot to place Bet");
        if(lionTimerTxt!=null) lionTimerTxt.setText(""+timeLeft);
        handler.postDelayed(new Runnable(){ @Override public void run(){ timeLeft--; if(lionTimerTxt!=null) lionTimerTxt.setText(""+timeLeft); if(timeLeft>0){ handler.postDelayed(this,1000); } else { finishRound(); } } },1000);
    }
    void finishRound(){
        bettingOpen=false; showDrawing();
        int rand=(int)(Math.random()*100); String special=""; int winner;
        if(rand<8){ special="🥗 SALAD"; int[] veg={4,5,6,7}; winner=veg[(int)(Math.random()*4)]; }
        else if(rand<16){ special="🍕 PIZZA"; int[] meat={0,1,2,3}; winner=meat[(int)(Math.random()*4)]; }
        else winner=(int)(Math.random()*8);
        long totalBet=getTotalBet(); long winAmount=0;
        if(special.contains("SALAD")){ for(int i=4;i<8;i++) winAmount+=bets[i]*getMult(i); }
        else if(special.contains("PIZZA")){ for(int i=0;i<4;i++) winAmount+=bets[i]*getMult(i); }
        else winAmount=bets[winner]*getMult(winner);
        final int fw=winner; final long fwin=winAmount; final String fs=special;
        String date=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault()).format(new Date());
        HistoryItem item=new HistoryItem(date,round,emojis[fw],fwin);
        for(int i=0;i<8;i++){ if(bets[i]>0) item.betLines.add(emojis[i]+"  "+names[i]+"  🪙 "+fmt(bets[i])); }
        if(!item.betLines.isEmpty()){ myHistory.add(0,item); if(myHistory.size()>20) myHistory.remove(20); }
        handler.postDelayed(()->{
            hideDrawing();
            for(int i=0;i<8;i++){ if(boothViews[i]==null) continue; if(i==fw) boothViews[i].setBackground(roundStroke(0xFFFFD700,0xFFFF0000,10,60)); else boothViews[i].setBackground(roundBg(0xFFDDDDDD,60)); }
            balance+=fwin;
            if(fwin>0){ totalToday+=fwin; rankingPoints+=fwin/100; }
            saveWallet(); addResult(emojis[fw]);
            statusTxt.setText("WIN: "+emojis[fw]+" "+names[fw]+" "+fs);
            showResultPopup(fw,fwin,totalBet,fs); if(fwin>0) showWinBanner(fwin);
            for(int i=0;i<8;i++) bets[i]=0; updateUI();
            handler.postDelayed(()->{
                hideResultPopup();
                startRound();
                if(autoBetOn && lastBetSlot>=0 && balance>=chip){
                    handler.postDelayed(()->{ if(bettingOpen) placeBet(lastBetSlot); },500);
                }
            },3000);
        },2000);
    }
    int getMult(int i){ int[] m={45,25,15,10,5,5,5,5}; return m[i]; }
}
