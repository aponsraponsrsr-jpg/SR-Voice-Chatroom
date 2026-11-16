package com.sr.voicechat;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// AGENCY POLICY - ONLY AGENCY CAN SEE THIS
// Agency sees FULL: Target Coins + Host Salary (80%) + Agency Commission (20%) + Total
// Agency sees ALL its Hosts: which Host did how much Target, Total Dollar
// READ ONLY - Agency also CANNOT edit Dollar - Company Database controls everything
public class AgencyPolicyActivity extends AppCompatActivity {
    private LinearLayout listBox, hostListBox;
    private TextView tvMyTotal, tvTotalHosts, tvTotalDollar;
    private String myUid = "";
    private String myAgencyId = "";

    // OFFICIAL POLICY - Company Fixed - From your Image
    // Level, TargetCoins, HostSalary(80%), AgencyCommission(20%), Total
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
    // For calculation - double values
    private final double[] HOST_VAL = {1, 2, 4.5, 7, 14, 24, 32, 40, 56, 76, 104, 132, 168, 220, 292, 348, 440, 584, 752, 1024, 1240, 1600};
    private final double[] AGENCY_VAL = {0.5, 1, 1.5, 2, 3, 6, 8, 10, 14, 19, 26, 33, 42, 55, 73, 87, 110, 146, 188, 256, 310, 400};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        if (fu != null) myUid = fu.getUid();
        // Agency ID = My UID or My 9 Digit ID - passed from Agency Dashboard
        myAgencyId = getIntent().getStringExtra("AGENCY_OWNER_ID");
        if (myAgencyId == null || myAgencyId.isEmpty()) myAgencyId = myUid;

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.parseColor("#0A0518"));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(22, 28, 22, 28);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("🏛️ AGENCY POLICY - FULL");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Only Agency Owner can see this • Host NEVER sees Agency Commission\nCompany Database থেকে হিসাব • কেউ বদলাতে পারবে না");
        sub.setTextColor(Color.parseColor("#B0BEC5"));
        sub.setTextSize(12);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, 8, 0, 18);
        root.addView(sub);

        // MY AGENCY SUMMARY - From Database
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
        root.addView(sumBox);

        tvTotalHosts = new TextView(this);
        tvTotalHosts.setText("My Hosts: Loading...");
        tvTotalHosts.setTextColor(Color.WHITE);
        tvTotalHosts.setTypeface(null, Typeface.BOLD);
        tvTotalHosts.setTextSize(15);
        sumBox.addView(tvTotalHosts);

        tvTotalDollar = new TextView(this);
        tvTotalDollar.setText("My Agency Commission Total: $0");
        tvTotalDollar.setTextColor(Color.parseColor("#FFD700"));
        tvTotalDollar.setTypeface(null, Typeface.BOLD);
        tvTotalDollar.setTextSize(15);
        sumBox.addView(tvTotalDollar);

        tvMyTotal = new TextView(this);
        tvMyTotal.setText("Total Payout (Host + Agency): $0");
        tvMyTotal.setTextColor(Color.parseColor("#00E676"));
        tvMyTotal.setTypeface(null, Typeface.BOLD);
        tvMyTotal.setTextSize(15);
        sumBox.addView(tvMyTotal);

        // TABLE HEADER - FULL
        TextView tableTitle = new TextView(this);
        tableTitle.setText("FULL POLICY TABLE - 22 Levels");
        tableTitle.setTextColor(Color.parseColor("#FFD700"));
        tableTitle.setTypeface(null, Typeface.BOLD);
        tableTitle.setTextSize(14);
        tableTitle.setPadding(0, 4, 0, 10);
        root.addView(tableTitle);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setPadding(10, 12, 10, 12);
        GradientDrawable hBg = new GradientDrawable();
        hBg.setColor(Color.parseColor("#0F5132"));
        hBg.setCornerRadius(12);
        header.setBackground(hBg);
        root.addView(header);
        addHeader(header, "Lvl", 1);
        addHeader(header, "Target", 2);
        addHeader(header, "Host 80%", 2);
        addHeader(header, "Agency 20%", 2);
        addHeader(header, "Total", 2);

        listBox = new LinearLayout(this);
        listBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(listBox);

        for (int i = 0; i < TARGETS.length; i++) {
            addRow(i, i + 1, TARGETS[i], HOST_SALARY[i], AGENCY_COMMISSION[i], TOTAL[i]);
        }

        // MY HOSTS LIST - Agency Management
        TextView hostTitle = new TextView(this);
        hostTitle.setText("👥 MY HOSTS - Management");
        hostTitle.setTextColor(Color.parseColor("#FFD700"));
        hostTitle.setTypeface(null, Typeface.BOLD);
        hostTitle.setTextSize(15);
        hostTitle.setPadding(0, 22, 0, 10);
        root.addView(hostTitle);

        TextView hostSub = new TextView(this);
        hostSub.setText("কোন Host কত Target করছে, তার Total Dollar কত - সব এখানে দেখা যাবে");
        hostSub.setTextColor(Color.parseColor("#B0BEC5"));
        hostSub.setTextSize(12);
        hostSub.setPadding(0, 0, 0, 10);
        root.addView(hostSub);

        hostListBox = new LinearLayout(this);
        hostListBox.setOrientation(LinearLayout.VERTICAL);
        root.addView(hostListBox);

        TextView note = new TextView(this);
        note.setText("• Host শুধু তার Host Salary দেখে, Agency Commission দেখে না\n• Agency Commission শুধু Agency Owner দেখে\n• সব Counting Database থেকে, কেউ নিজে থেকে Dollar লিখতে পারবে না");
        note.setTextColor(Color.parseColor("#B0BEC5"));
        note.setTextSize(12);
        note.setPadding(0, 18, 0, 0);
        root.addView(note);

        setContentView(scroll);
        loadMyHosts();
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

    // Load Agency's Hosts from Database - agencyId == myUid
    private void loadMyHosts() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                hostListBox.removeAllViews();
                int count = 0;
                double agencyTotal = 0, hostTotal = 0;
                for (DataSnapshot child : snap.getChildren()) {
                    String agencyId = "";
                    if (child.hasChild("agencyId") && child.child("agencyId").getValue() != null)
                        agencyId = String.valueOf(child.child("agencyId").getValue());
                    if (!agencyId.equals(myAgencyId) && !agencyId.equals(myUid)) continue;
                    String name = child.hasChild("name") ? String.valueOf(child.child("name").getValue()) : "Host";
                    long received = 0;
                    if (child.hasChild("totalReceivedCoins") && child.child("totalReceivedCoins").getValue() != null) {
                        try { received = Long.parseLong(String.valueOf(child.child("totalReceivedCoins").getValue())); } catch (Exception ignored) {}
                    }
                    int lvl = 0; double hSal = 0, aSal = 0;
                    for (int i = 0; i < TARGETS.length; i++) {
                        if (received >= TARGETS[i]) { lvl = i + 1; hSal = HOST_VAL[i]; aSal = AGENCY_VAL[i]; }
                    }
                    hostTotal += hSal; agencyTotal += aSal; count++;
                    addHostRow(name, received, lvl, hSal, aSal);
                }
                if (count == 0) {
                    TextView empty = new TextView(AgencyPolicyActivity.this);
                    empty.setText("No Host yet under your Agency");
                    empty.setTextColor(Color.parseColor("#B0BEC5"));
                    empty.setGravity(Gravity.CENTER);
                    empty.setPadding(0, 16, 0, 16);
                    hostListBox.addView(empty);
                }
                tvTotalHosts.setText("My Hosts: " + count);
                tvTotalDollar.setText("My Agency Commission Total: $" + formatDouble(agencyTotal));
                tvMyTotal.setText("Total Payout (Host + Agency): $" + formatDouble(hostTotal + agencyTotal));
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void addHostRow(String name, long received, int level, double hSal, double aSal) {
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
        t1.setText(name + " • Level " + (level == 0 ? "-" : level));
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
