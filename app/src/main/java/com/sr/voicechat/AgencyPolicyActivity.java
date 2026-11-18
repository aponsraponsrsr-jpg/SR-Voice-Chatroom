package com.sr.voicechat;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// AGENCY POLICY - GATED - ONLY AGENCY CAN SEE THIS
// RULE: Agency nai = Policy kokhono dekhbe na. Agency = Full Table + Own Hosts Only.
// Host / Normal User NEVER sees Agency Commission.
// READ ONLY - Agency also CANNOT edit Dollar.
public class AgencyPolicyActivity extends AppCompatActivity {
    private LinearLayout listBox, hostListBox, root, lockBox;
    private TextView tvMyTotal, tvTotalHosts, tvTotalDollar;
    private String myId = "";
    private String myAgencyId = "";
    private String myName = "Agency";
    private SharedPreferences prefs;
    private boolean isAgency = false;

    private final int[] TARGETS = {
        288000, 588000, 1190000, 1780000, 3280000, 5780000, 7880000, 9980000,
        13850000, 18850000, 26500000, 34500000, 43500000, 56500000, 76500000,
        88500000, 115500000, 155000000, 210000000, 288000000, 350000000, 460000000
    };
    private final String[] HOST_SALARY = {
        "$1", "$2", "$4.5", "$7", "$14", "$24", "$32", "$40", "$56", "$76",
        "$104", "$132", "$168", "$220", "$292", "$348", "$440", "$584", "$752", "$1024", "$1240", "$1600"
    };
    private final String[] AGENCY_COMMISSION = {
        "$0.5", "$1", "$1.5", "$2", "$3", "$6", "$8", "$10", "$14", "$19",
        "$26", "$33", "$42", "$55", "$73", "$87", "$110", "$146", "$188", "$256", "$310", "$400"
    };
    private final String[] TOTAL = {
        "$1.5", "$3", "$6", "$9", "$17", "$30", "$40", "$50", "$70", "$95",
        "$130", "$165", "$210", "$275", "$365", "$435", "$550", "$730", "$940", "$1280", "$1550", "$2000"
    };
    private final double[] HOST_VAL = {1, 2, 4.5, 7, 14, 24, 32, 40, 56, 76, 104, 132, 168, 220, 292, 348, 440, 584, 752, 1024, 1240, 1600};
    private final double[] AGENCY_VAL = {0.5, 1, 1.5, 2, 3, 6, 8, 10, 14, 19, 26, 33, 42, 55, 73, 87, 110, 146, 188, 256, 310, 400};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        myAgencyId = getIntent().getStringExtra("AGENCY_OWNER_ID");
        if (myAgencyId == null || myAgencyId.isEmpty()) myAgencyId = getIntent().getStringExtra("MY_ID");
        if (myAgencyId == null || myAgencyId.isEmpty()) myAgencyId = getIntent().getStringExtra("AGENCY_ID");
        if (myAgencyId == null || myAgencyId.isEmpty()) {
            myAgencyId = prefs.getString("MY_9_DIGIT_ID", "");
            myName = prefs.getString("USER_NAME", "");
            if (myName == null || myName.isEmpty()) myName = prefs.getString("MY_NAME", "Agency");
            if (myName == null || myName.isEmpty()) myName = prefs.getString("name", "Agency");
        }
        if (myAgencyId == null) myAgencyId = "";
        myId = myAgencyId;
        if (myName == null || myName.isEmpty()) myName = "Agency";

        // GATE 1 - Local Cache: Permanent Agency - Instant, Loading nai
        isAgency = prefs.getBoolean("IS_AGENCY_PERMANENT", false);
        if (!isAgency) isAgency = prefs.getBoolean("IS_AGENCY", false) && prefs.getString("MY_9_DIGIT_ID", "").equals(myAgencyId);
        if (getIntent().getBooleanExtra("FROM_AGENCY_DASHBOARD", false)) isAgency = true;
        // Cached Agency Name
        String cachedAgencyName = prefs.getString("CACHED_AGENCY_NAME", myName);
        if (cachedAgencyName != null && !cachedAgencyName.isEmpty()) myName = cachedAgencyName;

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(22, 28, 22, 28);
        scroll.addView(root);

        TextView btnBack = new TextView(this);
        btnBack.setText("← Back to Agency Dashboard");
        btnBack.setTextColor(Color.WHITE);
        btnBack.setTextSize(14);
        btnBack.setTypeface(null, Typeface.BOLD);
        btnBack.setPadding(10, 10, 10, 10);
        btnBack.setOnClickListener(v -> finish());
        root.addView(btnBack);

        // LOCK BOX - Agency na hole eitai dekhabe, Full Table kokhono dekhbe na
        lockBox = new LinearLayout(this);
        lockBox.setOrientation(LinearLayout.VERTICAL);
        lockBox.setGravity(Gravity.CENTER);
        lockBox.setPadding(30, 60, 30, 60);
        GradientDrawable lockBg = new GradientDrawable();
        lockBg.setColor(Color.parseColor("#1B1035"));
        lockBg.setCornerRadius(22);
        lockBg.setStroke(1, Color.parseColor("#FFD700"));
        lockBox.setBackground(lockBg);
        lockBox.setVisibility(isAgency ? View.GONE : View.VISIBLE);
        root.addView(lockBox);

        TextView lockIcon = new TextView(this);
        lockIcon.setText("🔒");
        lockIcon.setTextSize(48);
        lockIcon.setGravity(Gravity.CENTER);
        lockBox.addView(lockIcon);
        TextView lockTitle = new TextView(this);
        lockTitle.setText("Agency Policy Locked");
        lockTitle.setTextColor(Color.WHITE);
        lockTitle.setTextSize(20);
        lockTitle.setTypeface(null, Typeface.BOLD);
        lockTitle.setGravity(Gravity.CENTER);
        lockTitle.setPadding(0, 16, 0, 8);
        lockBox.addView(lockTitle);
        TextView lockMsg = new TextView(this);
        lockMsg.setText("Only Agency Owner can see this Policy\n\nAgency পেলে এই Policy Automatic খুলে যাবে।");
        lockMsg.setTextColor(Color.parseColor("#B0BEC5"));
        lockMsg.setTextSize(14);
        lockMsg.setGravity(Gravity.CENTER);
        lockBox.addView(lockMsg);

        TextView title = new TextView(this);
        title.setText("🏛️ AGENCY POLICY - FULL");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setVisibility(isAgency ? View.VISIBLE : View.GONE);
        title.setTag("agency_title");
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Only Agency Owner can see this • Host NEVER sees Agency Commission\nCompany Database থেকে হিসাব • কেউ বদলাতে পারবে না");
        sub.setTextColor(Color.parseColor("#B0BEC5"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 18);
        sub.setVisibility(isAgency ? View.VISIBLE : View.GONE);
        sub.setTag("agency_sub");
        root.addView(sub);

        LinearLayout sumBox = new LinearLayout(this);
        sumBox.setOrientation(LinearLayout.VERTICAL);
        sumBox.setPadding(26, 22, 26, 22);
        GradientDrawable sumBg = new GradientDrawable();
        sumBg.setColor(Color.parseColor("#1B1035"));
        sumBg.setCornerRadius(22);
        sumBg.setStroke(1, Color.parseColor("#FFD700"));
        sumBox.setBackground(sumBg);
        LinearLayout.LayoutParams sumLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sumLp.setMargins(0, 0, 0, 18);
        sumBox.setLayoutParams(sumLp);
        sumBox.setVisibility(isAgency ? View.VISIBLE : View.GONE);
        sumBox.setTag("agency_sum");
        root.addView(sumBox);

        tvTotalHosts = new TextView(this);
        // FIXED - Loading uthay dilam. Cache theke sathe sathe.
        tvTotalHosts.setText("My Hosts: " + prefs.getInt("CACHED_HOST_COUNT_" + myAgencyId, 0));
        tvTotalHosts.setTextColor(Color.WHITE);
        tvTotalHosts.setTypeface(null, Typeface.BOLD);
        tvTotalHosts.setTextSize(15);
        sumBox.addView(tvTotalHosts);

        tvTotalDollar = new TextView(this);
        tvTotalDollar.setText("My Agency Commission Total: $" + formatDouble(prefs.getFloat("CACHED_AGENCY_TOTAL_" + myAgencyId, 0)));
        tvTotalDollar.setTextColor(Color.parseColor("#FFD700"));
        tvTotalDollar.setTypeface(null, Typeface.BOLD);
        tvTotalDollar.setTextSize(15);
        sumBox.addView(tvTotalDollar);

        tvMyTotal = new TextView(this);
        tvMyTotal.setText("Total Payout (Host + Agency): $" + formatDouble(prefs.getFloat("CACHED_TOTAL_PAYOUT_" + myAgencyId, 0)));
        tvMyTotal.setTextColor(Color.parseColor("#00E676"));
        tvMyTotal.setTypeface(null, Typeface.BOLD);
        tvMyTotal.setTextSize(15);
        sumBox.addView(tvMyTotal);

        TextView tableTitle = new TextView(this);
        tableTitle.setText("FULL POLICY TABLE - 22 Levels");
        tableTitle.setTextColor(Color.parseColor("#FFD700"));
        tableTitle.setTypeface(null, Typeface.BOLD);
        tableTitle.setTextSize(14);
        tableTitle.setPadding(0, 4, 0, 10);
        tableTitle.setVisibility(isAgency ? View.VISIBLE : View.GONE);
        tableTitle.setTag("agency_table_title");
        root.addView(tableTitle);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding(10, 12, 10, 12);
        GradientDrawable hBg = new GradientDrawable();
        hBg.setColor(Color.parseColor("#0F5132"));
        hBg.setCornerRadius(12);
        header.setBackground(hBg);
        header.setVisibility(isAgency ? View.VISIBLE : View.GONE);
        header.setTag("agency_header");
        root.addView(header);
        addHeader(header, "Lvl", 1);
        addHeader(header, "Target", 2);
        addHeader(header, "Host 80%", 2);
        addHeader(header, "Agency 20%", 2);
        addHeader(header, "Total", 2);

        listBox = new LinearLayout(this);
        listBox.setOrientation(LinearLayout.VERTICAL);
        listBox.setVisibility(isAgency ? View.VISIBLE : View.GONE);
        listBox.setTag("agency_list");
        root.addView(listBox);
        if (isAgency) {
            for (int i = 0; i < TARGETS.length; i++) addRow(i, i + 1, TARGETS[i], HOST_SALARY[i], AGENCY_COMMISSION[i], TOTAL[i]);
        }

        TextView hostTitle = new TextView(this);
        hostTitle.setText("👥 MY HOSTS - Management");
        hostTitle.setTextColor(Color.parseColor("#FFD700"));
        hostTitle.setTypeface(null, Typeface.BOLD);
        hostTitle.setTextSize(15);
        hostTitle.setPadding(0, 22, 0, 10);
        hostTitle.setVisibility(isAgency ? View.VISIBLE : View.GONE);
        hostTitle.setTag("agency_host_title");
        root.addView(hostTitle);

        TextView hostSub = new TextView(this);
        hostSub.setText("কোন Host কত Target করছে, তার Total Dollar কত - সব এখানে দেখা যাবে");
        hostSub.setTextColor(Color.parseColor("#B0BEC5"));
        hostSub.setTextSize(12);
        hostSub.setPadding(0, 0, 0, 10);
        hostSub.setVisibility(isAgency ? View.VISIBLE : View.GONE);
        hostSub.setTag("agency_host_sub");
        root.addView(hostSub);

        hostListBox = new LinearLayout(this);
        hostListBox.setOrientation(LinearLayout.VERTICAL);
        hostListBox.setVisibility(isAgency ? View.VISIBLE : View.GONE);
        hostListBox.setTag("agency_host_list");
        root.addView(hostListBox);

        TextView note = new TextView(this);
        note.setText("• Host শুধু তার Host Salary দেখে, Agency Commission দেখে না\n• Agency Commission শুধু Agency Owner দেখে\n• সব Counting Database থেকে, কেউ নিজে থেকে Dollar লিখতে পারবে না");
        note.setTextColor(Color.parseColor("#B0BEC5"));
        note.setTextSize(12);
        note.setPadding(0, 18, 0, 0);
        note.setVisibility(isAgency ? View.VISIBLE : View.GONE);
        note.setTag("agency_note");
        root.addView(note);

        setContentView(scroll);
        checkAgencyAccess();
        if (isAgency) loadMyHosts();
    }

    private void showUnlocked() {
        if (isAgency) return;
        isAgency = true;
        prefs.edit().putBoolean("IS_AGENCY", true).putBoolean("IS_AGENCY_PERMANENT", true).apply();
        if (lockBox != null) lockBox.setVisibility(View.GONE);
        for (int i = 0; i < root.getChildCount(); i++) {
            View v = root.getChildAt(i);
            if (v.getTag() != null && String.valueOf(v.getTag()).startsWith("agency_")) v.setVisibility(View.VISIBLE);
        }
        if (listBox != null && listBox.getChildCount() == 0) {
            for (int i = 0; i < TARGETS.length; i++) addRow(i, i + 1, TARGETS[i], HOST_SALARY[i], AGENCY_COMMISSION[i], TOTAL[i]);
        }
        loadMyHosts();
    }

    // FIXED - Agency Check: agencies/{ID} + Cache + my9DigitId - UID diye na
    private void checkAgencyAccess() {
        if (myAgencyId.isEmpty()) return;
        if (isAgency) return; // Dashboard theke / Cache theke already Agency - abar check dorkar nai
        FirebaseDatabase.getInstance().getReference("agencies").child(myAgencyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists()) { showUnlocked(); return; }
                // 2nd check - users e my9DigitId diye isAgency check
                FirebaseDatabase.getInstance().getReference("users").orderByChild("my9DigitId").equalTo(myAgencyId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot s2) {
                        for (DataSnapshot ds : s2.getChildren()) {
                            if (ds.hasChild("isAgency") && Boolean.TRUE.equals(ds.child("isAgency").getValue(Boolean.class))) { showUnlocked(); return; }
                            if (ds.hasChild("agencyId") && ds.child("agencyId").getValue() != null && String.valueOf(ds.child("agencyId").getValue()).equals(myAgencyId)) { showUnlocked(); return; }
                        }
                        // Agency na - Lock e thakbe, finish korbo na - Lock msg dekhbe
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void addHeader(LinearLayout row, String txt, float w) {
        TextView t = new TextView(this);
        t.setText(txt);
        t.setTextColor(Color.WHITE);
        t.setTypeface(null, Typeface.BOLD);
        t.setTextSize(11);
        t.setGravity(Gravity.CENTER);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, w));
        row.addView(t);
    }

    private void addRow(int idx, int level, int target, String host, String agency, String total) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(10, 14, 10, 14);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(idx % 2 == 0 ? Color.parseColor("#1B1035") : Color.parseColor("#140B26"));
        bg.setCornerRadius(10);
        row.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 3, 0, 3);
        row.setLayoutParams(lp);
        listBox.addView(row);
        TextView t1 = cell("L" + level, Color.WHITE, 1, 12);
        TextView t2 = cell(format(target), Color.parseColor("#FFD700"), 2, 11);
        TextView t3 = cell(host, Color.parseColor("#00E676"), 2, 11);
        TextView t4 = cell(agency, Color.parseColor("#FFD700"), 2, 11);
        TextView t5 = cell(total, Color.WHITE, 2, 11);
        row.addView(t1); row.addView(t2); row.addView(t3); row.addView(t4); row.addView(t5);
    }

    private TextView cell(String txt, int color, float w, float size) {
        TextView t = new TextView(this);
        t.setText(txt);
        t.setTextColor(color);
        t.setTypeface(null, Typeface.BOLD);
        t.setTextSize(size);
        t.setGravity(Gravity.CENTER);
        t.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, w));
        return t;
    }

    // FIXED - Load Hosts: agencies/{myAgencyId}/hosts theke - sob User scan na
    private void loadMyHosts() {
        FirebaseDatabase.getInstance().getReference("agencies").child(myAgencyId).child("hosts").addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                hostListBox.removeAllViews();
                if (!snap.exists() || snap.getChildrenCount() == 0) {
                    TextView empty = new TextView(AgencyPolicyActivity.this);
                    empty.setText("No Host yet under your Agency");
                    empty.setTextColor(Color.parseColor("#B0BEC5"));
                    empty.setGravity(Gravity.CENTER);
                    empty.setPadding(0, 16, 0, 16);
                    hostListBox.addView(empty);
                    if (tvTotalHosts != null) tvTotalHosts.setText("My Hosts: 0");
                    return;
                }
                int[] count = {0};
                double[] agencyTotal = {0}, hostTotal = {0};
                int[] serial = {0};
                for (DataSnapshot hostSnap : snap.getChildren()) {
                    String hostId = hostSnap.getKey();
                    if (hostId == null) continue;
                    if (hostId.equals(myAgencyId)) continue;
                    fetchHostRow(hostId, serial, count, agencyTotal, hostTotal, (int) snap.getChildrenCount());
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void fetchHostRow(String hostId, int[] serial, int[] count, double[] agencyTotal, double[] hostTotal, int expected) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(hostId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                if (!snap.exists()) {
                    usersRef.orderByChild("my9DigitId").equalTo(hostId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot s2) {
                            for (DataSnapshot ds : s2.getChildren()) { showHostRow(ds, hostId, serial, count, agencyTotal, hostTotal, expected); return; }
                            count[0]++; if (count[0] >= expected) updateTotals(count[0], agencyTotal[0], hostTotal[0]);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError e) {}
                    });
                    return;
                }
                showHostRow(snap, hostId, serial, count, agencyTotal, hostTotal, expected);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void showHostRow(DataSnapshot child, String hostId, int[] serial, int[] count, double[] agencyTotal, double[] hostTotal, int expected) {
        String name = child.hasChild("name") ? String.valueOf(child.child("name").getValue()) : "Host";
        if (child.hasChild("USER_NAME")) name = String.valueOf(child.child("USER_NAME").getValue());
        long received = 0;
        String[] coinKeys = {"totalReceivedCoins", "totalCoins", "coins", "receivedCoins"};
        for (String k : coinKeys) {
            if (child.hasChild(k) && child.child(k).getValue() != null) {
                try { received = Long.parseLong(String.valueOf(child.child(k).getValue())); break; } catch (Exception ignored) {}
            }
        }
        int lvl = 0; double hSal = 0, aSal = 0;
        for (int i = 0; i < TARGETS.length; i++) {
            if (received >= TARGETS[i]) { lvl = i + 1; hSal = HOST_VAL[i]; aSal = AGENCY_VAL[i]; }
        }
        hostTotal[0] += hSal; agencyTotal[0] += aSal; count[0]++; serial[0]++;
        addHostRow(serial[0], hostId, name, received, lvl, hSal, aSal);
        if (count[0] >= expected) updateTotals(count[0], agencyTotal[0], hostTotal[0]);
    }

    private void updateTotals(int count, double agencyTotal, double hostTotal) {
        if (tvTotalHosts != null) tvTotalHosts.setText("My Hosts: " + count);
        if (tvTotalDollar != null) tvTotalDollar.setText("My Agency Commission Total: $" + formatDouble(agencyTotal));
        if (tvMyTotal != null) tvMyTotal.setText("Total Payout (Host + Agency): $" + formatDouble(hostTotal + agencyTotal));
        prefs.edit().putInt("CACHED_HOST_COUNT_" + myAgencyId, count)
            .putFloat("CACHED_AGENCY_TOTAL_" + myAgencyId, (float) agencyTotal)
            .putFloat("CACHED_TOTAL_PAYOUT_" + myAgencyId, (float) (hostTotal + agencyTotal))
            .apply();
    }

    private void addHostRow(int serial, String hostId, String name, long received, int level, double hSal, double aSal) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(22, 16, 22, 16);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1B1035"));
        bg.setCornerRadius(16);
        row.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        row.setLayoutParams(lp);
        hostListBox.addView(row);
        TextView t1 = new TextView(this);
        t1.setText(serial + ". " + name + " (ID: " + hostId + ") • Level " + (level == 0 ? "-" : level));
        t1.setTextColor(Color.WHITE);
        t1.setTypeface(null, Typeface.BOLD);
        t1.setTextSize(14);
        row.addView(t1);
        TextView t2 = new TextView(this);
        t2.setText("Received: " + format(received) + " Coins  |  Host: $" + formatDouble(hSal) + "  |  Agency: $" + formatDouble(aSal));
        t2.setTextColor(Color.parseColor("#FFD700"));
        t2.setTextSize(12);
        row.addView(t2);
    }

    private String format(long n) { return String.format("%,d", n); }
    private String formatDouble(double d) {
        if (d == (long) d) return String.valueOf((long) d);
        return String.valueOf(d);
    }
}
