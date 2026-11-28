package com.sr.voicechat;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
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
    TextView todayRankTxt;
    TextView[] betViews = new TextView[8];
    TextView[] chipViews = new TextView[4];
    LinearLayout[] boothViews = new LinearLayout[8];
    LinearLayout resultHistoryLayout;
    long[] bets = new long[8];

    int[] chipVals = {1000,5000,10000,100000};
    String[] chipLabels = {"1K","5K","10K","100K"};
    int chip = 1000; int selectedChipIndex = 0;

    long balance = 0; long myBetThisRound = 0; long rankingPoints = 0; long totalToday = 0;
    String userName = "Guest"; String userId = "";
    String userPhoto = "";
    ImageView walletPicView;
    boolean walletLoadedOnce = false;
    int round = 1; int timeLeft = 30; boolean bettingOpen = true;
    int lightIndex = 0; boolean lightRunning = true; boolean isDrawing = false;
    Handler handler = new Handler(); Handler lightHandler = new Handler();
    Runnable roundRunnable = null;
    FrameLayout rootFrame;
    View drawingOverlay, resultPopup, rankingPopup, introPopup, winBanner, historyPopup;
    ArrayList<String> resultHistory = new ArrayList<>();
    ArrayList<HistoryItem> myHistory = new ArrayList<>();
    boolean autoBetOn = false;
    int lastBetSlot = -1;

    static class RankEntry { String name; String id; long points; long reward; RankEntry(String n,String id,long p,long r){name=n;this.id=id;points=p;reward=r;} }
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
    int OFFWHITE = 0xFFFFF2C0;
    int BOOTH_BG = 0xFFFFF2C0;
    int PINK = 0xFFFF69B4;
    int LIGHT_YELLOW = 0xFFFFF59D;
    int SHARK_BLUE = 0xFF2196F3;
    int SKY = 0xFF00C2E8;
    // FIX 3 - GHIYA COLOR - Sada na, halka ghiya, premium
    int GHIYA = 0xFFFFE9A8;

    GradientDrawable roundBg(int c,int r){ GradientDrawable d=new GradientDrawable(); d.setColor(c); d.setCornerRadius(r); return d; }
    GradientDrawable roundStroke(int c,int sc,int sw,int r){ GradientDrawable d=new GradientDrawable(); d.setColor(c); d.setStroke(sw,sc); d.setCornerRadius(r); return d; }
    GradientDrawable circleBg(int c){ GradientDrawable d=new GradientDrawable(); d.setShape(GradientDrawable.OVAL); d.setColor(c); return d; }
    GradientDrawable circleStroke(int c,int sc,int sw){ GradientDrawable d=new GradientDrawable(); d.setShape(GradientDrawable.OVAL); d.setColor(c); d.setStroke(sw,sc); return d; }
    String fmt(long v){ return String.format(Locale.US,"%,d",v); }

    String fmtBet(long v){
        if(v<=0) return "";
        if(v>=1000000000L){
            long b=v/1000000000L; long rem=(v%1000000000L)/100000000L;
            if(rem==0) return b+"B";
            return b+"."+rem+"B";
        }
        if(v>=1000000L){
            long m=v/1000000L; long rem=(v%1000000L)/100000L;
            if(rem==0) return m+"M";
            return m+"."+rem+"M";
        }
        if(v>=1000L){
            long k=v/1000L; long rem=(v%1000L)/100L;
            if(rem==0) return k+"K";
            return k+"."+rem+"K";
        }
        return String.valueOf(v);
    }

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
    void checkDailyReset(){
        SharedPreferences sp = getSharedPreferences("greedy_lion_wallet", MODE_PRIVATE);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Riyadh"));
        String saudiDate = cal.get(Calendar.YEAR)+"-"+(cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.DAY_OF_MONTH);
        String lastDate = sp.getString("last_play_date","");
        if(!saudiDate.equals(lastDate) && !lastDate.isEmpty()){
            rankingPoints = 0; totalToday = 0; rankList.clear(); resultHistory.clear(); myHistory.clear();
            sp.edit().putString("last_play_date", saudiDate).putLong("ranking_points",0).putLong("total_today",0).apply();
        } else {
            sp.edit().putString("last_play_date", saudiDate).apply();
        }
    }
    int calcGlobalWinner(int roundNum, String[] outSpecial){
        long seed = (long)roundNum * 7919L + 12345L;
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Riyadh"));
        seed += cal.get(Calendar.DAY_OF_YEAR) * 100000L;
        int rand = (int)((seed * 1103515245L + 12345L) % 100);
        if(rand < 0) rand = -rand;
        outSpecial[0] = "";
        if(rand < 8){ outSpecial[0]="🥗 SALAD"; int[] veg={4,5,6,7}; return veg[(int)(seed % 4)]; }
        else if(rand < 16){ outSpecial[0]="🍕 PIZZA"; int[] meat={0,1,2,3}; return meat[(int)(seed % 4)]; }
        else return (int)(seed % 8);
    }

    void loadWallet(){
        SharedPreferences sr = getSharedPreferences("SR_USER", MODE_PRIVATE);
        SharedPreferences game = getSharedPreferences("greedy_lion_wallet", MODE_PRIVATE);
        if(walletLoadedOnce){
            rankingPoints = game.getLong("ranking_points",rankingPoints);
            totalToday = game.getLong("total_today",totalToday);
            return;
        }
        walletLoadedOnce = true;
        if(getIntent()!=null){
            if(getIntent().hasExtra("user_name")) userName = getIntent().getStringExtra("user_name");
            if(getIntent().hasExtra("user_id")) userId = getIntent().getStringExtra("user_id");
            if(getIntent().hasExtra("user_pic")) userPhoto = getIntent().getStringExtra("user_pic");
            if(getIntent().hasExtra("user_photo")) userPhoto = getIntent().getStringExtra("user_photo");
        }
        if(userName==null || userName.equals("Guest") || userName.isEmpty()){
            String n = sr.getString("USER_NAME", "");
            if(n==null||n.isEmpty()) n = sr.getString("PROFILE_NAME", "");
            if(n!=null && !n.isEmpty()) userName = n;
            else userName = game.getString("user_name","Guest");
        }
        if(userId==null || userId.isEmpty()){
            String id = sr.getString("MY_9_DIGIT_ID", "");
            if(id==null||id.isEmpty()) id = sr.getString("PROFILE_ID", "");
            if(id==null||id.isEmpty()) id = game.getString("user_id","");
            userId = id!=null?id:"";
        }
        if(userPhoto==null || userPhoto.isEmpty() || userPhoto.equals("👤")){
            String ph = sr.getString("USER_PHOTO", "");
            if(ph==null||ph.isEmpty()) ph = sr.getString("PROFILE_PHOTO", "");
            if(ph==null||ph.isEmpty()) ph = sr.getString("USER_IMAGE", "");
            if(ph==null||ph.isEmpty()) ph = game.getString("user_pic","");
            if(ph!=null && (ph.equals("👤")||ph.length()<5)) ph = "";
            userPhoto = ph!=null?ph:"";
        }
        if(game.contains("wallet_coins")) balance = game.getLong("wallet_coins",0);
        else if(sr.contains("wallet_coins")) balance = sr.getLong("wallet_coins",0);
        else if(getIntent()!=null && getIntent().hasExtra("wallet_coins")) balance = getIntent().getLongExtra("wallet_coins",0);
        else balance = 0;
        rankingPoints = game.getLong("ranking_points",0);
        totalToday = game.getLong("total_today",0);
        round = calcGlobalRound();
        rankList.clear();
        if(rankingPoints>0) rankList.add(new RankEntry(userName,userId, rankingPoints, 0));
        checkDailyReset();
    }
    void saveWallet(){
        getSharedPreferences("greedy_lion_wallet",MODE_PRIVATE).edit().putLong("wallet_coins",balance).putString("user_name",userName).putString("user_id",userId).putString("user_pic",userPhoto).putLong("ranking_points",rankingPoints).putLong("total_today",totalToday).putInt("last_round",round).apply();
        getSharedPreferences("SR_USER",MODE_PRIVATE).edit().putLong("wallet_coins",balance).apply();
        updateWalletUI();
    }
    void setProfileImage(ImageView iv){
        if(iv==null) return;
        try{
            if(userPhoto!=null && !userPhoto.isEmpty() && !userPhoto.equals("👤")){
                if(userPhoto.startsWith("http")){
                    Glide.with(this).load(userPhoto).circleCrop().into(iv);
                } else {
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
        if(balanceTxt!=null){
            balanceTxt.setText("🪙 "+fmt(balance));
            balanceTxt.setTextColor(0xFF000000);
            balanceTxt.setTypeface(null, android.graphics.Typeface.BOLD);
            balanceTxt.getPaint().setFakeBoldText(true);
        }
        if(todayTxt!=null){
            todayTxt.setText("🪙 "+fmt(totalToday));
            todayTxt.setTextColor(0xFF000000);
            todayTxt.setTypeface(null, android.graphics.Typeface.BOLD);
            todayTxt.getPaint().setFakeBoldText(true);
        }
        if(todayRankTxt!=null) todayRankTxt.setText("Today's Rank  ▶");
        setProfileImage(walletPicView);
    }
    @Override protected void onResume(){ super.onResume(); loadWallet(); updateWalletUI(); updateBetUI(); }

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        loadWallet();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        rootFrame = new FrameLayout(this);
        rootFrame.addView(createUI());
        setContentView(rootFrame);
        updateWalletUI(); updateChipUI(); refreshResultHistory(); updateBetUI();
        if(getWindow()!=null){ getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(int)(getResources().getDisplayMetrics().heightPixels*0.98)); getWindow().setGravity(Gravity.BOTTOM); getWindow().setBackgroundDrawableResource(android.R.color.transparent); }
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
                        if(bets[i]>0){
                            boothViews[i].setBackground(roundStroke(0xFFFFD700, 0xFFFF8C00, 5, 70));
                        } else if(i==lightIndex){
                            boothViews[i].setBackground(roundStroke(BOOTH_BG, 0xFFFFD700, 6, 70));
                        } else {
                            boothViews[i].setBackground(roundBg(BOOTH_BG, 70));
                        }
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
            if(settlementTxt!=null) settlementTxt.setText("Time Remaining for Settlement: "+getSettlementRemaining() + " (Saudi 7PM)");
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

    // FIX 1 - Result Popup e Salad hole Salad logo, Pizza hole Pizza logo
    void showResultPopup(int winner,long winAmount,long totalBet,String special,String displayEmoji){
        hideResultPopup();
        LinearLayout bg=new LinearLayout(this); bg.setOrientation(LinearLayout.VERTICAL); bg.setGravity(Gravity.CENTER); bg.setBackgroundColor(0xAA000000); bg.setLayoutParams(popupParams());
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER);
        box.setBackground(roundBg(0xFF8B0000,30)); box.setPadding(32,32,32,32);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels*0.92), ViewGroup.LayoutParams.WRAP_CONTENT);
        bp.bottomMargin=100; box.setLayoutParams(bp);
        TextView stage=new TextView(this); stage.setText("⭐⭐⭐⭐⭐"); stage.setGravity(Gravity.CENTER); stage.setTextColor(0xFFFFD700); stage.setTextSize(18);
        TextView lion=new TextView(this); lion.setText("🦁  "+displayEmoji); lion.setTextSize(64); lion.setGravity(Gravity.CENTER);
        TextView r=new TextView(this); r.setText("Round "+round+" Result "+special); r.setTextColor(0xFFFFFFFF); r.setTextSize(18); r.setGravity(Gravity.CENTER); r.setPadding(0,8,0,16);
        TextView betLine=new TextView(this); betLine.setText("Bet:  "+fmt(totalBet)); betLine.setTextColor(0xFFFFFFFF); betLine.setTextSize(20); betLine.setGravity(Gravity.CENTER); betLine.setPadding(0,8,0,8);
        TextView winLine=new TextView(this); winLine.setText("Win:  "+fmt(winAmount)); winLine.setTextColor(0xFFFFFF00); winLine.setTextSize(22); winLine.setGravity(Gravity.CENTER); winLine.setPadding(0,8,0,16);
        TextView me=new TextView(this); me.setText(userName+" - Points: "+fmt(rankingPoints)); me.setTextColor(0xFFFFD700); me.setTextSize(14); me.setGravity(Gravity.CENTER);
        box.addView(stage); box.addView(lion); box.addView(r); box.addView(betLine); box.addView(winLine); box.addView(me);
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
        box.setBackground(roundBg(GHIYA,30)); box.setPadding(16,16,16,16);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels*0.96), (int)(getResources().getDisplayMetrics().heightPixels*0.78));
        bp.bottomMargin=60; box.setLayoutParams(bp);
        TextView t=new TextView(this); t.setText("My History"); t.setGravity(Gravity.CENTER); t.setTextSize(22); t.setTextColor(0xFFD32F2F); box.addView(t);
        ScrollView sv=new ScrollView(this); LinearLayout list=new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL);
        if(myHistory.isEmpty()){ TextView empty=new TextView(this); empty.setText("No history yet.\nPlay a round to see your bets here."); empty.setGravity(Gravity.CENTER); empty.setPadding(0,20,0,20); list.addView(empty); }
        else { for(HistoryItem item : myHistory){ LinearLayout head=new LinearLayout(this); head.setOrientation(LinearLayout.HORIZONTAL); TextView date=new TextView(this); date.setText(item.date); date.setTextSize(12); TextView rnd=new TextView(this); rnd.setText("Round: "+item.round); rnd.setTextSize(12); rnd.setGravity(Gravity.RIGHT); LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); date.setLayoutParams(dp); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); rnd.setLayoutParams(rp); head.addView(date); head.addView(rnd); list.addView(head); for(String line : item.betLines){ TextView tv=new TextView(this); tv.setText(line); tv.setTextSize(15); tv.setPadding(8,6,8,6); list.addView(tv); } TextView win=new TextView(this); win.setText(item.winEmoji+"  🪙 "+fmt(item.winAmount)); win.setTextSize(17); win.setGravity(Gravity.RIGHT); win.setPadding(0,4,0,12); list.addView(win); View line=new View(this); line.setBackgroundColor(0xFFDDDDDD); line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,2)); list.addView(line); } }
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
        box.setBackground(roundBg(GHIYA,30)); box.setPadding(16,16,16,16);
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
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setBackground(roundBg(GHIYA,30)); box.setPadding(20,20,20,20);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams((int)(getResources().getDisplayMetrics().widthPixels*0.9),ViewGroup.LayoutParams.WRAP_CONTENT); bp.bottomMargin=120; box.setLayoutParams(bp);
        TextView t=new TextView(this); t.setText("Introduction"); t.setGravity(Gravity.CENTER); t.setTextSize(18);
        TextView body=new TextView(this); body.setText("1.All players contribute to the jackpot when placing bets in the game.\n\n2.Players accumulate their own points when they win the game.\n\n3.The more points a player has, the higher their rank on the leaderboard.\n\n4.The top 10 players in the daily rankings can claim the daily jackpot reward the next day. The daily reward is valid for 24 hours; please claim your reward within this period.\n\n5.The jackpot becomes invalid after the expiration date, please claim your reward promptly."); body.setTextSize(13); body.setPadding(0,12,0,0);
        TextView close=new TextView(this); close.setText("✕ Close"); close.setGravity(Gravity.CENTER); close.setPadding(0,12,0,0); close.setOnClickListener(v->hideIntro());
        box.addView(t); box.addView(body); box.addView(close); bg.addView(box); bg.setOnClickListener(v->hideIntro()); introPopup=bg; rootFrame.addView(bg);
    }
    void hideIntro(){ if(introPopup!=null){ rootFrame.removeView(introPopup); introPopup=null; } }

    LinearLayout saladPizzaItem(String emoji, String label){
        LinearLayout col=new LinearLayout(this); col.setOrientation(LinearLayout.VERTICAL); col.setGravity(Gravity.CENTER);
        TextView photo=new TextView(this); photo.setText(emoji); photo.setTextSize(28); photo.setGravity(Gravity.CENTER);
        photo.setPadding(0,0,0,0);
        TextView name=new TextView(this); name.setText(label); name.setGravity(Gravity.CENTER); name.setTextSize(11); name.setTextColor(0xFF333333);
        name.setBackground(roundBg(GHIYA,50)); name.setPadding(20,3,20,3);
        LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); np.topMargin=1; name.setLayoutParams(np);
        col.addView(photo); col.addView(name);
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); col.setLayoutParams(cp);
        return col;
    }

    LinearLayout createUI(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(SKY);
        LinearLayout top=new LinearLayout(this); top.setOrientation(LinearLayout.HORIZONTAL); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(16,6,16,0);
        TextView back=new TextView(this); back.setText("⬅"); back.setTextSize(22); back.setTextColor(0xFFFFFFFF); back.setPadding(0,0,16,0); back.setOnClickListener(v->finish());
        roundTxt=new TextView(this); roundTxt.setText("Round: "+round); roundTxt.setTextSize(13); roundTxt.setTextColor(0xFF333333);
        LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); roundTxt.setLayoutParams(tp);
        TextView music=new TextView(this); music.setText("🎵"); music.setTextSize(20); music.setPadding(0,0,16,0);
        TextView help=new TextView(this); help.setText("❓"); help.setTextSize(20); help.setOnClickListener(v->showIntro());
        top.addView(back); top.addView(roundTxt); top.addView(music); top.addView(help); root.addView(top);

        LinearLayout rankRow=new LinearLayout(this); rankRow.setOrientation(LinearLayout.HORIZONTAL); rankRow.setPadding(16,0,16,0);
        TextView rank=new TextView(this); rank.setText("🏆\nRanking"); rank.setGravity(Gravity.CENTER); rank.setTextSize(14); rank.setTextColor(0xFFFFD700); rank.setOnClickListener(v->showRanking()); rank.setPadding(4,2,4,2); rankRow.addView(rank); root.addView(rankRow);

        // WHEEL - Chicken matha katbe na - centerY 330 -> 350, radius 282 -> 270
        FrameLayout wheel=new FrameLayout(this); wheel.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,800));
        TextView wheelRing=new TextView(this); FrameLayout.LayoutParams ringLp=new FrameLayout.LayoutParams(540,540); ringLp.leftMargin=270; ringLp.topMargin=90; wheelRing.setLayoutParams(ringLp); wheelRing.setBackground(circleStroke(0x00FFFFFF,0xFFFFD700,10)); wheel.addView(wheelRing);
        int centerX=540, centerY=350, radius=270;
        for(int i=0;i<8;i++){
            final int idx=i; double angle=Math.toRadians(-90+i*45);
            int x=(int)(centerX+radius*Math.cos(angle))-95; int y=(int)(centerY+radius*Math.sin(angle))-68;
            FrameLayout boothFrame=new FrameLayout(this);
            FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(190,215); fp.leftMargin=x; fp.topMargin=y; boothFrame.setLayoutParams(fp);
            LinearLayout booth=new LinearLayout(this); booth.setOrientation(LinearLayout.VERTICAL); booth.setGravity(Gravity.CENTER); booth.setBackground(roundBg(BOOTH_BG,60)); booth.setPadding(6,4,6,22);
            FrameLayout.LayoutParams bp2=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); booth.setLayoutParams(bp2);
            TextView e=new TextView(this);
            if(i==2){ e.setText(emojis[i]); e.setTextColor(SHARK_BLUE); } else e.setText(emojis[i]);
            e.setTextSize(24); e.setGravity(Gravity.CENTER);
            TextView m=new TextView(this); m.setText(mults[i]); m.setTextSize(14); m.setGravity(Gravity.CENTER); m.setTextColor(0xFFFF8C00);
            TextView n=new TextView(this); n.setText(names[i]); n.setTextSize(10); n.setGravity(Gravity.CENTER);
            booth.addView(e); booth.addView(m); booth.addView(n);
            boothFrame.addView(booth);
            TextView b=new TextView(this); b.setText(""); b.setTextSize(12); b.setGravity(Gravity.CENTER); b.setTextColor(0xFFFFFFFF); b.setPadding(10,4,10,4); b.setSingleLine(true); b.setEllipsize(TextUtils.TruncateAt.END);
            b.setBackground(roundBg(0xFFFF8C00,50));
            FrameLayout.LayoutParams bLp=new FrameLayout.LayoutParams(160, ViewGroup.LayoutParams.WRAP_CONTENT); bLp.gravity=Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL; bLp.bottomMargin=6; b.setLayoutParams(bLp); b.setVisibility(View.GONE);
            boothFrame.addView(b);
            booth.setOnClickListener(v->placeBet(idx)); b.setOnClickListener(v->placeBet(idx));
            betViews[i]=b; boothViews[i]=booth; wheel.addView(boothFrame);
        }
        LinearLayout center=new LinearLayout(this); center.setOrientation(LinearLayout.VERTICAL); center.setGravity(Gravity.CENTER); center.setBackground(circleStroke(0xFFFFD700,0xFFB8860B,7)); center.setPadding(6,6,6,6);
        FrameLayout.LayoutParams cp=new FrameLayout.LayoutParams(230,230); cp.leftMargin=425; cp.topMargin=235; center.setLayoutParams(cp);
        TextView lion=new TextView(this); lion.setText("🦁"); lion.setTextSize(42); lion.setGravity(Gravity.CENTER);
        lionTimerTxt=new TextView(this); lionTimerTxt.setText("30"); lionTimerTxt.setTextSize(20); lionTimerTxt.setGravity(Gravity.CENTER); lionTimerTxt.setTextColor(0xFF8B4513); lionTimerTxt.setBackground(circleStroke(BOOTH_BG,0xFFFFD700,5)); lionTimerTxt.setPadding(14,3,14,3);
        center.addView(lion); center.addView(lionTimerTxt); wheel.addView(center); root.addView(wheel);

        LinearLayout extraRow=new LinearLayout(this); extraRow.setOrientation(LinearLayout.HORIZONTAL); extraRow.setGravity(Gravity.CENTER); extraRow.setPadding(16,2,16,4); extraRow.setBackgroundColor(SKY);
        extraRow.addView(saladPizzaItem("🥗","salad"));
        extraRow.addView(saladPizzaItem("🍕","pizza"));
        root.addView(extraRow);

        LinearLayout bottom=new LinearLayout(this); bottom.setOrientation(LinearLayout.VERTICAL); bottom.setBackground(roundBg(0xFFD32F2F,30)); bottom.setPadding(16,6,16,10);
        LinearLayout myRow=new LinearLayout(this); myRow.setOrientation(LinearLayout.HORIZONTAL); myRow.setGravity(Gravity.CENTER); myRow.setPadding(0,0,0,6);
        // FIX 3 - Today's Rank / MyHistory - Sada na, GHIYA
        todayRankTxt=new TextView(this); todayRankTxt.setText("Today's Rank  ▶"); todayRankTxt.setGravity(Gravity.CENTER); todayRankTxt.setTextSize(15); todayRankTxt.setTextColor(0xFF333333); todayRankTxt.setBackground(roundBg(GHIYA,30)); todayRankTxt.setPadding(18,16,18,16); todayRankTxt.setSingleLine(true); todayRankTxt.setEllipsize(TextUtils.TruncateAt.END);
        todayRankTxt.setOnClickListener(v->showRanking());
        todayRankTxt.setMinimumWidth(400);
        LinearLayout.LayoutParams rankLp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1.2f); rankLp.setMargins(0,0,6,0); todayRankTxt.setLayoutParams(rankLp);
        TextView myHist=new TextView(this); myHist.setText("📝 MyHistory ▶"); myHist.setBackground(roundBg(GHIYA,30)); myHist.setPadding(18,16,18,16); myHist.setGravity(Gravity.CENTER); myHist.setTextSize(15); myHist.setSingleLine(true); myHist.setEllipsize(TextUtils.TruncateAt.END); myHist.setOnClickListener(v->showMyHistory());
        myHist.setMinimumWidth(400);
        LinearLayout.LayoutParams mhp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1.2f); mhp.setMargins(6,0,0,0); myHist.setLayoutParams(mhp);
        myRow.addView(todayRankTxt); myRow.addView(myHist); bottom.addView(myRow);

        LinearLayout resRow=new LinearLayout(this); resRow.setOrientation(LinearLayout.HORIZONTAL); resRow.setGravity(Gravity.CENTER_VERTICAL);
        resultTxt=new TextView(this); resultTxt.setText("Result  "); resultTxt.setTextColor(0xFFFFFFFF); resultTxt.setTextSize(13); resRow.addView(resultTxt);
        HorizontalScrollView hs=new HorizontalScrollView(this); resultHistoryLayout=new LinearLayout(this); resultHistoryLayout.setOrientation(LinearLayout.HORIZONTAL); hs.addView(resultHistoryLayout); resRow.addView(hs); bottom.addView(resRow);
        LinearLayout chips=new LinearLayout(this); chips.setOrientation(LinearLayout.HORIZONTAL); chips.setGravity(Gravity.CENTER); chips.setPadding(0,10,0,0);
        for(int i=0;i<4;i++){ final int idx=i; final int v=chipVals[i]; final String label=chipLabels[i]; TextView b=new TextView(this); b.setText("🪙\n"+label); b.setGravity(Gravity.CENTER); b.setTextSize(14); b.setTextColor(0xFFFFFFFF); b.setBackground(roundBg(PINK,50)); b.setPadding(6,10,6,10); LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f); clp.setMargins(5,0,5,0); b.setLayoutParams(clp); b.setOnClickListener(vv->{ chip=v; selectedChipIndex=idx; updateChipUI(); }); chipViews[i]=b; chips.addView(b); }
        bottom.addView(chips);

        // FIX 2 - LAST ROW - Today's Earnings BAME / MAJHKHANE, Auto theke BAIRE, pura coin dekha jabe
        LinearLayout lastRow=new LinearLayout(this); lastRow.setOrientation(LinearLayout.HORIZONTAL); lastRow.setGravity(Gravity.CENTER_VERTICAL); lastRow.setPadding(0,8,0,2);
        LinearLayout walletCol=new LinearLayout(this); walletCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams walletColLp=new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,0.75f); walletCol.setLayoutParams(walletColLp);
        walletNameTxt=new TextView(this); walletNameTxt.setText(userName); walletNameTxt.setTextSize(13); walletNameTxt.setTextColor(0xFFFFFFFF); walletNameTxt.setGravity(Gravity.LEFT); walletNameTxt.setSingleLine(true); walletNameTxt.setEllipsize(TextUtils.TruncateAt.END); walletNameTxt.setPadding(78,0,0,2);
        walletCol.addView(walletNameTxt);
        FrameLayout walletFrame=new FrameLayout(this);
        LinearLayout.LayoutParams frameLp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); walletFrame.setLayoutParams(frameLp);
        // FIX 3 - Wallet Banner - GHIYA
        LinearLayout coinsBanner=new LinearLayout(this); coinsBanner.setOrientation(LinearLayout.HORIZONTAL); coinsBanner.setGravity(Gravity.CENTER_VERTICAL); coinsBanner.setBackground(roundBg(GHIYA,30)); coinsBanner.setPadding(38,8,12,8);
        FrameLayout.LayoutParams cbLp=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); cbLp.leftMargin=38; cbLp.topMargin=10; coinsBanner.setLayoutParams(cbLp);
        coinsBanner.setMinimumWidth(270);
        balanceTxt=new TextView(this); balanceTxt.setText("🪙 "+fmt(balance)); balanceTxt.setTextSize(14); balanceTxt.setTextColor(0xFF000000); balanceTxt.setTypeface(null, android.graphics.Typeface.BOLD); balanceTxt.getPaint().setFakeBoldText(true); balanceTxt.setSingleLine(true); balanceTxt.setEllipsize(null); balanceTxt.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams balLp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); balanceTxt.setLayoutParams(balLp);
        TextView plus=new TextView(this); plus.setText("+"); plus.setTextSize(16); plus.setTextColor(0xFFFFFFFF); plus.setBackground(circleBg(0xFF4CAF50)); plus.setPadding(10,2,10,2); plus.setOnClickListener(v->Toast.makeText(this,"Recharge - Next Update",Toast.LENGTH_SHORT).show());
        coinsBanner.addView(balanceTxt); coinsBanner.addView(plus);
        coinsBanner.setOnClickListener(v->showRanking());
        walletFrame.addView(coinsBanner);
        walletPicView=new ImageView(this);
        FrameLayout.LayoutParams wPicLp=new FrameLayout.LayoutParams(74,74); wPicLp.leftMargin=0; wPicLp.topMargin=4; walletPicView.setLayoutParams(wPicLp);
        walletPicView.setBackground(roundBg(0xFFFFFFFF,20)); walletPicView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        walletFrame.addView(walletPicView);
        walletCol.addView(walletFrame);
        // FIX 2 - Today's Earnings - BAME / MAJHKHANE, LOMBA, Auto theke dure, pura coin dekha jabe
        LinearLayout earnBox=new LinearLayout(this); earnBox.setOrientation(LinearLayout.VERTICAL); earnBox.setGravity(Gravity.CENTER);
        TextView earnLabel=new TextView(this); earnLabel.setText("Today's Earnings"); earnLabel.setTextColor(0xFFFFFFFF); earnLabel.setTextSize(10); earnLabel.setSingleLine(true); earnLabel.setEllipsize(null); earnLabel.setGravity(Gravity.CENTER); earnLabel.setPadding(0,0,0,2);
        todayTxt=new TextView(this); todayTxt.setText("🪙 "+fmt(totalToday)); todayTxt.setTextColor(0xFF000000); todayTxt.setTypeface(null, android.graphics.Typeface.BOLD); todayTxt.getPaint().setFakeBoldText(true); todayTxt.setBackground(roundBg(GHIYA,30)); todayTxt.setGravity(Gravity.CENTER); todayTxt.setPadding(14,10,14,10); todayTxt.setTextSize(14); todayTxt.setSingleLine(true); todayTxt.setEllipsize(null);
        todayTxt.setMinimumWidth(280);
        earnBox.addView(earnLabel); earnBox.addView(todayTxt);
        LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1.25f); ep.setMargins(8,0,8,0); earnBox.setLayoutParams(ep);
        // Auto - Choto, konay, Earnings er gaye lagbe na
        Button auto=new Button(this); auto.setText("auto bet"); auto.setTextSize(11); auto.setTextColor(0xFFFFFFFF); auto.setBackground(roundBg(0xFF4CAF50,50)); LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,0.38f); ap.setMargins(0,14,0,0); auto.setLayoutParams(ap);
        auto.setPadding(4,8,4,8);
        auto.setOnClickListener(v->{
            autoBetOn = !autoBetOn;
            auto.setText(autoBetOn ? "auto ON" : "auto bet");
            auto.setBackground(roundBg(autoBetOn ? 0xFFFF9800 : 0xFF4CAF50, 50));
            if(autoBetOn && bettingOpen && lastBetSlot >= 0 && balance >= chip){
                placeBet(lastBetSlot);
            }
            if(!autoBetOn) Toast.makeText(this,"Auto Bet OFF",Toast.LENGTH_SHORT).show();
        });
        lastRow.addView(walletCol); lastRow.addView(earnBox); lastRow.addView(auto); bottom.addView(lastRow);
        statusTxt=new TextView(this); statusTxt.setText(""); statusTxt.setTextSize(1); statusTxt.setGravity(Gravity.CENTER); bottom.addView(statusTxt); root.addView(bottom); return root;
    }
    int countDistinctBets(){ int c=0; for(long b: bets) if(b>0) c++; return c; }

    void updateBetUI(){
        for(int i=0;i<8;i++){
            if(betViews[i]!=null){
                if(bets[i]>0){
                    betViews[i].setText("🪙 "+fmtBet(bets[i]));
                    betViews[i].setBackground(roundBg(0xFFFF8C00,50));
                    betViews[i].setTextColor(0xFFFFFFFF);
                    betViews[i].setVisibility(View.VISIBLE);
                    betViews[i].bringToFront();
                } else {
                    betViews[i].setText("");
                    betViews[i].setVisibility(View.GONE);
                }
            }
            if(boothViews[i]!=null){
                if(bets[i]>0) boothViews[i].setBackground(roundStroke(0xFFFFD700, 0xFFFF8C00, 5, 70));
                else boothViews[i].setBackground(roundBg(BOOTH_BG, 70));
            }
        }
    }

    void placeBet(int idx){
        if(idx<0 || idx>=8) return;
        if(!bettingOpen){ Toast.makeText(this,"Wait for next Round",Toast.LENGTH_SHORT).show(); return; }
        if(balance<chip){ Toast.makeText(this,"Not enough coins",Toast.LENGTH_SHORT).show(); return; }
        if(bets[idx]==0 && countDistinctBets() >= 6){
            String msg = "Max 6 slots only! You can bet on max 6 slots";
            Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
            return;
        }
        bets[idx]+=chip; balance-=chip; myBetThisRound+=chip; lastBetSlot=idx;
        rankingPoints+=chip/100;
        saveWallet();
        updateBetUI();
        rankList.clear(); if(rankingPoints>0) rankList.add(new RankEntry(userName,userId,rankingPoints,0));
    }
    long getTotalBet(){ long t=0; for(long b:bets) t+=b; return t; }
    void updateUI(){ updateWalletUI(); updateBetUI(); }
    void startRound(){
        if(roundRunnable!=null) handler.removeCallbacks(roundRunnable);
        round = calcGlobalRound();
        bettingOpen=true; timeLeft=calcTimeLeftInRound(); myBetThisRound=0; updateWalletUI();
        if(roundTxt!=null) roundTxt.setText("Round: "+round);
        if(lionTimerTxt!=null) lionTimerTxt.setText(""+timeLeft);
        roundRunnable = new Runnable(){ @Override public void run(){ timeLeft--; if(lionTimerTxt!=null) lionTimerTxt.setText(""+timeLeft); if(timeLeft>0){ handler.postDelayed(this,1000); } else { finishRound(); } } };
        handler.postDelayed(roundRunnable,1000);
    }
    void finishRound(){
        bettingOpen=false; showDrawing();
        String[] specialArr = new String[1];
        int winner = calcGlobalWinner(round, specialArr);
        String special = specialArr[0];
        long totalBet=getTotalBet(); long winAmount=0;
        // FIX 1 + KHALI GHOR - Salad hole sudhu salad group, Pizza hole sudhu pizza group, khali ghore Win 0
        // Display Emoji - Salad hole 🥗, Pizza hole 🍕, na hole winner er emoji
        String displayEmoji = emojis[winner];
        if(special.contains("SALAD")){
            displayEmoji = "🥗";
            for(int i=4;i<8;i++){ if(bets[i]>0) winAmount+=bets[i]*getMult(i); }
        } else if(special.contains("PIZZA")){
            displayEmoji = "🍕";
            for(int i=0;i<4;i++){ if(bets[i]>0) winAmount+=bets[i]*getMult(i); }
        } else {
            if(bets[winner]>0) winAmount=bets[winner]*getMult(winner);
            else winAmount=0;
        }
        final int fw=winner; final long fwin=winAmount; final String fs=special; final String fDisplay=displayEmoji;
        String date=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault()).format(new Date());
        HistoryItem item=new HistoryItem(date,round,fDisplay,fwin);
        for(int i=0;i<8;i++){ if(bets[i]>0) item.betLines.add(emojis[i]+"  "+names[i]+"  🪙 "+fmt(bets[i])); }
        if(!item.betLines.isEmpty()){ myHistory.add(0,item); if(myHistory.size()>20) myHistory.remove(20); }
        handler.postDelayed(()->{
            hideDrawing();
            for(int i=0;i<8;i++){ if(boothViews[i]==null) continue; if(i==fw) boothViews[i].setBackground(roundStroke(0xFFFFD700,0xFFFF0000,10,70)); else boothViews[i].setBackground(roundBg(0xFFDDDDDD,70)); }
            if(fwin>0){ balance+=fwin; totalToday+=fwin; rankingPoints+=fwin/100; }
            saveWallet(); addResult(fDisplay);
            showResultPopup(fw,fwin,totalBet,fs,fDisplay); if(fwin>0) showWinBanner(fwin);
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
