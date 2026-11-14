package com.sr.voicechat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private DatabaseReference db;
    private ScrollView pageHome;
    private ScrollView pageMoment;
    private ScrollView pageInbox;
    private ScrollView pageMe;
    private LinearLayout homeRoomList;
    private LinearLayout meMenuList;
    private ImageView meProfileImg;
    private TextView meNameText;
    private TextView meIdText;
    private TextView meGenderCountryText;
    private TextView navHomeText;
    private TextView navMomentText;
    private TextView navInboxText;
    private TextView navMeText;
    private TextView meFollowCount;
    private TextView meFansCount;
    private TextView meVisitorCount;
    private TextView meCoinsText;
    private String myUid = "";
    private String my9DigitId = "";
    private String myName = "User";
    private String myPhotoBase64 = "";
    private String myCountry = "Bangladesh";
    private String myGender = "Male";
    private long myWalletCoins = 0;
    private long myExchangeCoins = 0;
    private long myTotalReceived = 0;
    private long selectedRechargeCoins = 100000;
    private String selectedRechargePrice = "USD 0.99";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("SR_PREFS", MODE_PRIVATE);
        myUid = prefs.getString("USER_UID", "");
        my9DigitId = prefs.getString("USER_9DIGIT_ID", "");
        myName = prefs.getString("USER_NAME", "User");
        myPhotoBase64 = prefs.getString("USER_PHOTO_BASE64", "");
        myCountry = prefs.getString("USER_COUNTRY", "Bangladesh");
        myGender = prefs.getString("USER_GENDER", "Male");
        if (myUid.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        db = FirebaseDatabase.getInstance().getReference();
        pageHome = findViewById(R.id.pageHome);
        pageMoment = findViewById(R.id.pageMoment);
        pageInbox = findViewById(R.id.pageInbox);
        pageMe = findViewById(R.id.pageMe);
        homeRoomList = findViewById(R.id.homeRoomList);
        meMenuList = findViewById(R.id.meMenuList);
        meProfileImg = findViewById(R.id.meProfileImg);
        meNameText = findViewById(R.id.meNameText);
        meIdText = findViewById(R.id.meIdText);
        meGenderCountryText = findViewById(R.id.meGenderCountryText);
        navHomeText = findViewById(R.id.navHomeText);
        navMomentText = findViewById(R.id.navMomentText);
        navInboxText = findViewById(R.id.navInboxText);
        navMeText = findViewById(R.id.navMeText);
        meFollowCount = findViewById(R.id.meFollowCount);
        meFansCount = findViewById(R.id.meFansCount);
        meVisitorCount = findViewById(R.id.meVisitorCount);
        meCoinsText = findViewById(R.id.meCoinsText);
        findViewById(R.id.navHome).setOnClickListener(v -> showPage(0));
        findViewById(R.id.navMoment).setOnClickListener(v -> showPage(1));
        findViewById(R.id.navInbox).setOnClickListener(v -> showPage(2));
        findViewById(R.id.navMe).setOnClickListener(v -> showPage(3));
        findViewById(R.id.navCenterPlus).setOnClickListener(v ->
            Toast.makeText(this, "Create Room / Post - Coming Soon", Toast.LENGTH_SHORT).show()
        );
        if (meIdText != null) {
            meIdText.setOnClickListener(v -> {
                String idToCopy = my9DigitId.isEmpty() ? myUid : my9DigitId;
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("ID", idToCopy));
                Toast.makeText(this, "ID Copied: " + idToCopy, Toast.LENGTH_SHORT).show();
            });
        }
        View btnMeProfile = findViewById(R.id.btnMeProfileBox);
        if (btnMeProfile != null) {
            btnMeProfile.setOnClickListener(v -> showEditProfileDialog());
        }
        if (meProfileImg != null) {
            meProfileImg.setOnClickListener(v -> showEditProfileDialog());
        }
        View btnSvip = findViewById(R.id.btnSvipCenter);
        if (btnSvip != null) {
            btnSvip.setOnClickListener(v -> showSvipDialog());
        }
        View btnVip = findViewById(R.id.btnVip);
        if (btnVip != null) {
            btnVip.setOnClickListener(v -> showVipDialog());
        }
        View btnRecharge = findViewById(R.id.btnRecharge);
        if (btnRecharge != null) {
            btnRecharge.setOnClickListener(v -> showRechargeDialog());
        }
        View btnCoins = findViewById(R.id.btnCoins);
        if (btnCoins != null) {
            btnCoins.setOnClickListener(v -> showRechargeDialog());
        }
        View btnStore = findViewById(R.id.btnStore);
        if (btnStore != null) {
            btnStore.setOnClickListener(v -> showStoreDialog());
        }
        View btnDress = findViewById(R.id.btnDress);
        if (btnDress != null) {
            btnDress.setOnClickListener(v -> showDressDialog());
        }
        View btnBirthday = findViewById(R.id.btnBirthday);
        if (btnBirthday != null) {
            btnBirthday.setOnClickListener(v -> showBirthdayDialog());
        }
        View btnLevel = findViewById(R.id.btnLevel);
        if (btnLevel != null) {
            btnLevel.setOnClickListener(v -> showLevelDialog());
        }
        View btnFollow = findViewById(R.id.btnFollowBox);
        if (btnFollow != null) {
            btnFollow.setOnClickListener(v -> Toast.makeText(this, "Following List - Coming Soon", Toast.LENGTH_SHORT).show());
        }
        View btnFans = findViewById(R.id.btnFansBox);
        if (btnFans != null) {
            btnFans.setOnClickListener(v -> Toast.makeText(this, "Fans List - Coming Soon", Toast.LENGTH_SHORT).show());
        }
        View btnVisitor = findViewById(R.id.btnVisitorBox);
        if (btnVisitor != null) {
            btnVisitor.setOnClickListener(v -> Toast.makeText(this, "Visitor List - Coming Soon", Toast.LENGTH_SHORT).show());
        }
        View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                prefs.edit().clear().apply();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            });
        }
        buildMeMenu();
        loadMyProfile();
        loadMyWallet();
        loadFollowFansVisitor();
        loadRooms();
        showPage(0);
    }
    private TextView makeText(String txt, int size, String color, boolean bold, int gravity) {
        TextView t = new TextView(this);
        t.setText(txt);
        t.setTextSize(size);
        t.setTextColor(Color.parseColor(color));
        if (bold) {
            t.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        t.setGravity(gravity);
        return t;
    }
    private Button makeColorBtn(String txt, int color) {
        Button b = new Button(this);
        b.setText(txt);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setTextColor(Color.WHITE);
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(60);
        b.setBackground(g);
        return b;
    }
    private void setDialog85(BottomSheetDialog d) {
        Window w = d.getWindow();
        if (w != null) {
            int sh = getResources().getDisplayMetrics().heightPixels;
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (sh * 0.85));
            w.setGravity(Gravity.BOTTOM);
        }
    }
    private void setDialog75(BottomSheetDialog d) {
        Window w = d.getWindow();
        if (w != null) {
            int sh = getResources().getDisplayMetrics().heightPixels;
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (sh * 0.75));
            w.setGravity(Gravity.BOTTOM);
        }
    }
    private void showPage(int index) {
        pageHome.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        pageMoment.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        pageInbox.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        pageMe.setVisibility(index == 3 ? View.VISIBLE : View.GONE);
        navHomeText.setTextColor(index == 0 ? Color.parseColor("#FF4081") : Color.parseColor("#888888"));
        navMomentText.setTextColor(index == 1 ? Color.parseColor("#FF4081") : Color.parseColor("#888888"));
        navInboxText.setTextColor(index == 2 ? Color.parseColor("#FF4081") : Color.parseColor("#888888"));
        navMeText.setTextColor(index == 3 ? Color.parseColor("#FF4081") : Color.parseColor("#888888"));
        navHomeText.setTypeface(null, index == 0 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        navMomentText.setTypeface(null, index == 1 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        navInboxText.setTypeface(null, index == 2 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        navMeText.setTypeface(null, index == 3 ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }
    @Override
    public void onBackPressed() {
        if (pageMe.getVisibility() == View.VISIBLE || pageMoment.getVisibility() == View.VISIBLE || pageInbox.getVisibility() == View.VISIBLE) {
            showPage(0);
            return;
        }
        super.onBackPressed();
    }
    private void buildMeMenu() {
        if (meMenuList == null) {
            return;
        }
        meMenuList.removeAllViews();
        String[][] items = {
            {"🎁", "Newbie mission"},
            {"👥", "Invite Friend"},
            {"🏅", "Medal"},
            {"👨‍👩‍👧", "Family"},
            {"🎙️", "Host Center"},
            {"📅", "Daily Tasks"},
            {"⚙️", "Setting"}
        };
        for (String[] it : items) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(16, 16, 16, 16);
            meMenuList.addView(row);
            TextView icon = new TextView(this);
            icon.setText(it[0]);
            icon.setTextSize(18);
            row.addView(icon);
            TextView label = new TextView(this);
            label.setText(it[1]);
            label.setTextColor(Color.parseColor("#222222"));
            label.setTextSize(14);
            label.setPadding(12, 0, 0, 0);
            label.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(label);
            TextView arrow = new TextView(this);
            arrow.setText(">");
            arrow.setTextColor(Color.parseColor("#AAAAAA"));
            arrow.setTextSize(16);
            row.addView(arrow);
            View line = new View(this);
            line.setBackgroundColor(Color.parseColor("#EEEEEE"));
            line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
            meMenuList.addView(line);
            row.setOnClickListener(v -> {
                if (it[1].equals("Setting")) {
                    showSettingDialog();
                } else if (it[1].equals("Host Center")) {
                    showHostCenterDialog();
                } else {
                    Toast.makeText(this, it[1] + " - Coming Soon", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void loadMyProfile() {
        if (meNameText != null) {
            meNameText.setText(myName);
        }
        if (meIdText != null) {
            meIdText.setText("ID: " + (my9DigitId.isEmpty() ? myUid : my9DigitId) + "  📋 Copy");
        }
        if (meGenderCountryText != null) {
            String genderIcon = myGender.equalsIgnoreCase("Female") ? "♀" : "♂";
            meGenderCountryText.setText(genderIcon + " " + myGender + " | " + myCountry);
        }
        Bitmap bmp = decodeBase64(myPhotoBase64);
        if (bmp != null && meProfileImg != null) {
            meProfileImg.setImageBitmap(bmp);
        }
        if (!myUid.isEmpty()) {
            db.child("users").child(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot s) {
                    if (!s.exists()) {
                        return;
                    }
                    String n = s.child("name").getValue(String.class);
                    if (n != null && !n.isEmpty()) {
                        myName = n;
                        meNameText.setText(n);
                        prefs.edit().putString("USER_NAME", n).apply();
                    }
                    String g = s.child("gender").getValue(String.class);
                    if (g != null && !g.isEmpty()) {
                        myGender = g;
                        prefs.edit().putString("USER_GENDER", g).apply();
                    }
                    String c = s.child("country").getValue(String.class);
                    if (c != null && !c.isEmpty()) {
                        myCountry = c;
                        prefs.edit().putString("USER_COUNTRY", c).apply();
                    }
                    if (meGenderCountryText != null) {
                        String gIcon = myGender.equalsIgnoreCase("Female") ? "♀" : "♂";
                        meGenderCountryText.setText(gIcon + " " + myGender + " | " + myCountry);
                    }
                    String ph = s.child("photoBase64").getValue(String.class);
                    if (ph != null && !ph.isEmpty()) {
                        myPhotoBase64 = ph;
                        prefs.edit().putString("USER_PHOTO_BASE64", ph).apply();
                        Bitmap b2 = decodeBase64(ph);
                        if (b2 != null) {
                            meProfileImg.setImageBitmap(b2);
                        }
                    }
                    String id9 = s.child("my9DigitId").getValue(String.class);
                    if (id9 != null && !id9.isEmpty()) {
                        my9DigitId = id9;
                        meIdText.setText("ID: " + id9 + "  📋 Copy");
                        prefs.edit().putString("USER_9DIGIT_ID", id9).apply();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError e) {
                }
            });
        }
    }
    private void loadMyWallet() {
        if (myUid.isEmpty()) {
            return;
        }
        db.child("users").child(myUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                if (!s.exists()) {
                    return;
                }
                Long c = s.child("coins").getValue(Long.class);
                myWalletCoins = c != null ? c : 0;
                Long ex = s.child("exchangeCoins").getValue(Long.class);
                myExchangeCoins = ex != null ? ex : 0;
                Long tot = s.child("totalReceived").getValue(Long.class);
                myTotalReceived = tot != null ? tot : 0;
                if (meCoinsText != null) {
                    meCoinsText.setText("🪙 " + String.format("%,d", myWalletCoins));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }
    private void loadFollowFansVisitor() {
        if (myUid.isEmpty()) {
            return;
        }
        db.child("users").child(myUid).child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                long cnt = s.exists() ? s.getChildrenCount() : 0;
                if (meFollowCount != null) {
                    meFollowCount.setText(String.valueOf(cnt));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
        db.child("users").child(myUid).child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                long cnt = s.exists() ? s.getChildrenCount() : 0;
                if (meFansCount != null) {
                    meFansCount.setText(String.valueOf(cnt));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
        db.child("users").child(myUid).child("visitors").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                long cnt = s.exists() ? s.getChildrenCount() : 0;
                if (meVisitorCount != null) {
                    meVisitorCount.setText(String.valueOf(cnt));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
        db.child("users").child(myUid).child("followCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                Long v = s.getValue(Long.class);
                if (v != null && meFollowCount != null) {
                    meFollowCount.setText(String.valueOf(v));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
        db.child("users").child(myUid).child("fansCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                Long v = s.getValue(Long.class);
                if (v != null && meFansCount != null) {
                    meFansCount.setText(String.valueOf(v));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
        db.child("users").child(myUid).child("visitorCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                Long v = s.getValue(Long.class);
                if (v != null && meVisitorCount != null) {
                    meVisitorCount.setText(String.valueOf(v));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }
    private void showRechargeDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(28, 20, 28, 28);
        TextView title = makeText("Coins", 18, "#222222", true, Gravity.CENTER);
        title.setPadding(0, 0, 0, 6);
        root.addView(title);
        TextView bal = makeText("🪙 " + String.format("%,d", myWalletCoins), 28, "#222222", true, Gravity.CENTER);
        bal.setPadding(0, 0, 0, 4);
        root.addView(bal);
        TextView sub = makeText("My Wallet - Original Coins", 13, "#888888", false, Gravity.CENTER);
        sub.setPadding(0, 0, 0, 4);
        root.addView(sub);
        TextView bill = makeText("Detailed Bill >", 13, "#888888", false, Gravity.START);
        bill.setPadding(0, 8, 0, 16);
        bill.setOnClickListener(v -> showDetailedBillDialog());
        root.addView(bill);
        TextView rTitle = makeText("Recharge", 16, "#222222", true, Gravity.START);
        rTitle.setPadding(0, 0, 0, 12);
        root.addView(rTitle);
        String[][] packs = {
            {"100000", "USD 0.99"},
            {"500000", "USD 4.99"},
            {"1000000", "USD 9.99"},
            {"5000000", "USD 49.99"},
            {"10000000", "USD 99.99"}
        };
        for (String[] p : packs) {
            String coinStr = p[0];
            String priceStr = p[1];
            boolean isSel = Long.parseLong(coinStr) == selectedRechargeCoins;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(20, 20, 20, 20);
            GradientDrawable bg = new GradientDrawable();
            if (isSel) {
                bg.setColor(Color.parseColor("#FFFDE7"));
                bg.setStroke(2, Color.parseColor("#FFC107"));
            } else {
                bg.setColor(Color.WHITE);
                bg.setStroke(1, Color.parseColor("#EEEEEE"));
            }
            bg.setCornerRadius(20);
            row.setBackground(bg);
            TextView left = makeText("🪙 " + coinStr, 15, "#222222", true, Gravity.START);
            left.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(left);
            TextView right = makeText(priceStr, 14, "#888888", false, Gravity.END);
            row.addView(right);
            TextView lock = makeText(isSel ? "✅ 🔒" : "🔒", 14, "#999999", false, Gravity.CENTER);
            lock.setPadding(12, 0, 0, 0);
            row.addView(lock);
            row.setOnClickListener(v -> {
                selectedRechargeCoins = Long.parseLong(coinStr);
                selectedRechargePrice = priceStr;
                d.dismiss();
                showRechargeDialog();
            });
            root.addView(row);
            View sp = new View(this);
            sp.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 12));
            root.addView(sp);
        }
        Button charge = makeColorBtn("🔒 Charge - Locked", Color.parseColor("#FFC107"));
        charge.setTextColor(Color.parseColor("#222222"));
        charge.setOnClickListener(v -> {
            Toast.makeText(this, "🔒 Locked - Coming Soon. Aapatoto kena jabe na.", Toast.LENGTH_LONG).show();
        });
        root.addView(charge);
        TextView note = makeText("1 Dollar = 100,000 Coins | 5 Dollar = 500,000 | 10 Dollar = 1,000,000", 11, "#999999", false, Gravity.CENTER);
        note.setPadding(0, 12, 0, 0);
        root.addView(note);
        TextView note2 = makeText("Recharge + Game Win + Gift Receive (30%) - Sob ekhane joma hobe", 11, "#999999", false, Gravity.CENTER);
        note2.setPadding(0, 6, 0, 0);
        root.addView(note2);
        d.setContentView(root);
        setDialog85(d);
        d.show();
    }
    private void showDetailedBillDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(28, 20, 28, 28);
        TextView t = makeText("Detailed Bill - My Wallet", 17, "#222222", true, Gravity.CENTER);
        t.setPadding(0, 0, 0, 16);
        root.addView(t);
        TextView bal = makeText("Balance: 🪙 " + String.format("%,d", myWalletCoins), 15, "#222222", true, Gravity.START);
        bal.setPadding(0, 0, 0, 10);
        root.addView(bal);
        TextView e1 = makeText("Exchange (30% Received): 🪙 " + String.format("%,d", myExchangeCoins), 14, "#7B1FA2", false, Gravity.START);
        e1.setPadding(0, 0, 0, 8);
        root.addView(e1);
        TextView e2 = makeText("Total Received: 🪙 " + String.format("%,d", myTotalReceived), 14, "#222222", false, Gravity.START);
        e2.setPadding(0, 0, 0, 8);
        root.addView(e2);
        TextView e3 = makeText("Recharge: Aapatoto Locked 🔒", 14, "#888888", false, Gravity.START);
        root.addView(e3);
        TextView e4 = makeText("Gift Send: 100% Katbe | Gift Receive: 30% Pabe", 12, "#999999", false, Gravity.START);
        e4.setPadding(0, 10, 0, 0);
        root.addView(e4);
        d.setContentView(root);
        setDialog75(d);
        d.show();
    }
    private void showSvipDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(28, 20, 28, 28);
        TextView t = makeText("👑 SVIP Center", 18, "#222222", true, Gravity.CENTER);
        t.setPadding(0, 0, 0, 8);
        root.addView(t);
        TextView s = makeText("Get SVIP and enjoy various privileges", 13, "#888888", false, Gravity.CENTER);
        s.setPadding(0, 0, 0, 16);
        root.addView(s);
        String[][] items = {
            {"👑", "SVIP 1 - Special Badge"},
            {"💎", "SVIP 2 - Special Entry"},
            {"🌟", "SVIP 3 - Special Gift Effect"}
        };
        for (String[] it : items) {
            TextView row = makeText(it[0] + "  " + it[1], 14, "#222222", false, Gravity.START);
            row.setPadding(0, 14, 0, 14);
            row.setOnClickListener(v -> Toast.makeText(this, "SVIP - Coming Soon 🔒", Toast.LENGTH_SHORT).show());
            root.addView(row);
        }
        d.setContentView(root);
        setDialog75(d);
        d.show();
    }
    private void showVipDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(28, 20, 28, 28);
        TextView t = makeText("👑 VIP", 18, "#222222", true, Gravity.CENTER);
        t.setPadding(0, 0, 0, 8);
        root.addView(t);
        TextView s = makeText("Check now - VIP Privileges", 13, "#888888", false, Gravity.CENTER);
        s.setPadding(0, 0, 0, 16);
        root.addView(s);
        TextView row = makeText("VIP Badge + Special Frame - Coming Soon 🔒", 14, "#222222", false, Gravity.CENTER);
        row.setPadding(0, 14, 0, 14);
        root.addView(row);
        d.setContentView(root);
        setDialog75(d);
        d.show();
    }
    private void showStoreDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(28, 20, 28, 28);
        TextView t = makeText("🛍️ Store", 18, "#222222", true, Gravity.CENTER);
        t.setPadding(0, 0, 0, 16);
        root.addView(t);
        TextView s = makeText("Frame / Bubble / Entry - Coming Soon", 14, "#888888", false, Gravity.CENTER);
        root.addView(s);
        d.setContentView(root);
        setDialog75(d);
        d.show();
    }
    private void showDressDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(28, 20, 28, 28);
        TextView t = makeText("👗 Dress", 18, "#222222", true, Gravity.CENTER);
        t.setPadding(0, 0, 0, 16);
        root.addView(t);
        TextView s = makeText("Avatar Dress - Coming Soon", 14, "#888888", false, Gravity.CENTER);
        root.addView(s);
        d.setContentView(root);
        setDialog75(d);
        d.show();
    }
    private void showBirthdayDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(28, 20, 28, 28);
        TextView t = makeText("🎂 Birthday Gift", 18, "#222222", true, Gravity.CENTER);
        t.setPadding(0, 0, 0, 16);
        root.addView(t);
        TextView s = makeText("Birthday Gift - Coming Soon", 14, "#888888", false, Gravity.CENTER);
        root.addView(s);
        d.setContentView(root);
        setDialog75(d);
        d.show();
    }
    private void showLevelDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(28, 20, 28, 28);
        TextView t = makeText("🏆 Level", 18, "#222222", true, Gravity.CENTER);
        t.setPadding(0, 0, 0, 8);
        root.addView(t);
        TextView s = makeText("Send Lv.0  |  Recv Lv.0", 14, "#222222", true, Gravity.CENTER);
        s.setPadding(0, 0, 0, 16);
        root.addView(s);
        TextView n = makeText("Gift korle Send Level barbe, Gift pele Recv Level barbe", 12, "#888888", false, Gravity.CENTER);
        root.addView(n);
        d.setContentView(root);
        setDialog75(d);
        d.show();
    }
    private void showEditProfileDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(28, 20, 28, 28);
        TextView t = makeText("Edit Profile", 18, "#222222", true, Gravity.CENTER);
        t.setPadding(0, 0, 0, 16);
        root.addView(t);
        TextView n = makeText("Name: " + myName + "\nID: " + my9DigitId + "\n" + myGender + " | " + myCountry, 14, "#222222", false, Gravity.START);
        n.setPadding(0, 0, 0, 16);
        root.addView(n);
        Button copy = makeColorBtn("📋 Copy ID", Color.parseColor("#FF9800"));
        copy.setOnClickListener(v -> {
            String idToCopy = my9DigitId.isEmpty() ? myUid : my9DigitId;
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("ID", idToCopy));
            Toast.makeText(this, "ID Copied: " + idToCopy, Toast.LENGTH_SHORT).show();
            d.dismiss();
        });
        root.addView(copy);
        d.setContentView(root);
        setDialog75(d);
        d.show();
    }
    private void showSettingDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(28, 20, 28, 28);
        TextView t = makeText("⚙️ Setting", 18, "#222222", true, Gravity.CENTER);
        t.setPadding(0, 0, 0, 16);
        root.addView(t);
        TextView n = makeText("Language: English\nCountry: " + myCountry + "\nNotification: ON", 14, "#222222", false, Gravity.START);
        root.addView(n);
        d.setContentView(root);
        setDialog75(d);
        d.show();
    }
    private void showHostCenterDialog() {
        BottomSheetDialog d = new BottomSheetDialog(this, R.style.Theme_Design_BottomSheetDialog);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(28, 20, 28, 28);
        TextView t = makeText("🎙️ Host Center", 18, "#222222", true, Gravity.CENTER);
        t.setPadding(0, 0, 0, 16);
        root.addView(t);
        TextView n = makeText("Host Center - Coming Soon", 14, "#888888", false, Gravity.CENTER);
        root.addView(n);
        d.setContentView(root);
        setDialog75(d);
        d.show();
    }
    private void loadRooms() {
        if (homeRoomList == null) {
            return;
        }
        db.child("rooms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot s) {
                homeRoomList.removeAllViews();
                if (!s.exists()) {
                    addDemoRoom("SR Apon's Room", "Welcome to my voice chat room!", "499506534");
                    addDemoRoom("SHAFIN's Room", "Welcome to my voice chat room!", "499506535");
                    return;
                }
                for (DataSnapshot room : s.getChildren()) {
                    String ownerId = room.getKey();
                    String rName = room.child("meta").child("roomName").getValue(String.class);
                    if (rName == null) {
                        rName = "Room " + ownerId;
                    }
                    String rPhoto = room.child("meta").child("roomPhotoBase64").getValue(String.class);
                    addRoomCard(rName, "Welcome to room!", ownerId, rPhoto);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError e) {
            }
        });
    }
    private void addDemoRoom(String name, String desc, String ownerId) {
        addRoomCard(name, desc, ownerId, null);
    }
    private void addRoomCard(String name, String desc, String ownerId, String photoBase64) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#2A1B4A"));
        card.setPadding(12, 12, 12, 12);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        card.setLayoutParams(lp);
        homeRoomList.addView(card);
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(top);
        ImageView roomImg = new ImageView(this);
        roomImg.setLayoutParams(new LinearLayout.LayoutParams(90, 90));
        roomImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        roomImg.setBackgroundResource(R.drawable.bg_profile_gradient);
        roomImg.setPadding(2, 2, 2, 2);
        roomImg.setImageResource(android.R.drawable.ic_menu_gallery);
        Bitmap bmp = decodeBase64(photoBase64);
        if (bmp != null) {
            roomImg.setImageBitmap(bmp);
        }
        top.addView(roomImg);
        LinearLayout txtBox = new LinearLayout(this);
        txtBox.setOrientation(LinearLayout.VERTICAL);
        txtBox.setPadding(12, 0, 0, 0);
        txtBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(txtBox);
        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(Color.parseColor("#FFD700"));
        tvName.setTextSize(15);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        txtBox.addView(tvName);
        TextView tvDesc = new TextView(this);
        tvDesc.setText(desc);
        tvDesc.setTextColor(Color.parseColor("#AAAAAA"));
        tvDesc.setTextSize(12);
        txtBox.addView(tvDesc);
        TextView live = new TextView(this);
        live.setText("● LIVE");
        live.setTextColor(Color.RED);
        live.setTextSize(11);
        live.setTypeface(null, android.graphics.Typeface.BOLD);
        top.addView(live);
        card.setOnClickListener(v -> {
            Intent i = new Intent(this, VoiceRoomActivity.class);
            i.putExtra("ROOM_OWNER_ID", ownerId);
            i.putExtra("MY_9DIGIT_ID", my9DigitId);
            i.putExtra("MY_UID", myUid);
            startActivity(i);
        });
    }
    private Bitmap decodeBase64(String b64) {
        if (b64 == null || b64.isEmpty()) {
            return null;
        }
        try {
            byte[] b = Base64.decode(b64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } catch (Exception e) {
            return null;
        }
    }
}
