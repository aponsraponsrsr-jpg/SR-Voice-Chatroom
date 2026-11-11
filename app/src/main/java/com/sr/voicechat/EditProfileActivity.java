package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private EditText etProfileName;
    private TextView tvSelectedGender, tvSelectedCountry, tvPermanentId;
    private Button btnSaveProfile;
    private SharedPreferences prefs;
    private DatabaseReference userDatabaseRef;
    private String currentUserId = "";
    private String selectedGender = "Male";
    private String selectedCountry = "Bangladesh 🇧🇩";

    // কান্ট্রি লিস্ট (ফ্ল্যাগ সহ)
    private final String[] countriesList = {
        "Bangladesh 🇧🇩", "Saudi Arabia 🇸🇦", "India 🇮🇳", "United Arab Emirates 🇦🇪",
        "Pakistan 🇵🇰", "United States 🇺🇸", "United Kingdom 🇬🇧", "Malaysia 🇲🇾",
        "Oman 🇴🇲", "Qatar 🇶🇦", "Kuwait 🇰🇼", "Bahrain 🇧🇭", "Iraq 🇮🇶", "Canada 🇨🇦"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // প্রিমিয়াম ডার্ক ব্যাকগ্রাউন্ড লেআউট তৈরি
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 40, 40, 40);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")});
        root.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("⚙️ Complete Your Profile (Permanent)");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 30);
        root.addView(title);

        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        currentUserId = prefs.getString("MY_9_DIGIT_ID", "");
        if (currentUserId.isEmpty()) {
            int r = (int) (Math.random() * 900000000) + 100000000;
            currentUserId = String.valueOf(r);
            prefs.edit().putString("MY_9_DIGIT_ID", currentUserId).apply();
        }

        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        // পার্মানেন্ট আইডি ডিসপ্লে (লকড)
        tvPermanentId = new TextView(this);
        tvPermanentId.setText("Permanent ID: " + currentUserId + " (Locked)");
        tvPermanentId.setTextColor(Color.parseColor("#B0BEC5"));
        tvPermanentId.setTextSize(14);
        tvPermanentId.setPadding(0, 0, 0, 20);
        root.addView(tvPermanentId);

        // নাম ইনপুট
        etProfileName = new EditText(this);
        etProfileName.setHint("Enter Your Name");
        etProfileName.setHintTextColor(Color.parseColor("#757575"));
        etProfileName.setTextColor(Color.WHITE);
        etProfileName.setPadding(30, 30, 30, 30);
        etProfileName.setBackgroundColor(Color.parseColor("#2A1B4E"));
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 140);
        p1.setMargins(0, 0, 0, 20);
        etProfileName.setLayoutParams(p1);
        root.addView(etProfileName);

        // জেন্ডার সিলেকশন বাটন
        tvSelectedGender = new TextView(this);
        tvSelectedGender.setText("Gender: Male (Click to Change)");
        tvSelectedGender.setTextColor(Color.WHITE);
        tvSelectedGender.setTextSize(15);
        tvSelectedGender.setPadding(30, 30, 30, 30);
        tvSelectedGender.setBackgroundColor(Color.parseColor("#2A1B4E"));
        tvSelectedGender.setLayoutParams(p1);
        tvSelectedGender.setOnClickListener(v -> showGenderPickerDialog());
        root.addView(tvSelectedGender);

        // কান্ট্রি সিলেকশন বাটন
        tvSelectedCountry = new TextView(this);
        tvSelectedCountry.setText("Country: Bangladesh 🇧🇩 (Click to Change)");
        tvSelectedCountry.setTextColor(Color.WHITE);
        tvSelectedCountry.setTextSize(15);
        tvSelectedCountry.setPadding(30, 30, 30, 30);
        tvSelectedCountry.setBackgroundColor(Color.parseColor("#2A1B4E"));
        tvSelectedCountry.setLayoutParams(p1);
        tvSelectedCountry.setOnClickListener(v -> showCountryPickerDialog());
        root.addView(tvSelectedCountry);

        // সেভ বাটন
        btnSaveProfile = new Button(this);
        btnSaveProfile.setText("Save & Enter Home");
        btnSaveProfile.setTextColor(Color.WHITE);
        btnSaveProfile.setBackgroundColor(Color.parseColor("#FF4081"));
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 140);
        p2.setMargins(0, 30, 0, 0);
        btnSaveProfile.setLayoutParams(p2);
        btnSaveProfile.setOnClickListener(v -> saveProfileToFirebase());
        root.addView(btnSaveProfile);

        setContentView(root);
    }

    private void showGenderPickerDialog() {
        String[] genders = {"Male", "Female"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Gender (Permanent)");
        builder.setItems(genders, (dialog, which) -> {
            selectedGender = genders[which];
            tvSelectedGender.setText("Gender: " + selectedGender + " (Locked)");
        });
        builder.show();
    }

    private void showCountryPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Your Country (Permanent)");
        builder.setItems(countriesList, (dialog, which) -> {
            selectedCountry = countriesList[which];
            tvSelectedCountry.setText("Country: " + selectedCountry + " (Locked)");
        });
        builder.show();
    }

    private void saveProfileToFirebase() {
        String name = etProfileName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name!", Toast.LENGTH_SHORT).show();
            return;
        }

        // লোকাল প্রিফারেন্সেস সেভ
        prefs.edit().putString("USER_NAME", name)
                .putString("USER_GENDER", selectedGender)
                .putString("USER_COUNTRY", selectedCountry)
                .putBoolean("IS_PROFILE_SETUP_DONE", true)
                .apply();

        // ফায়ারবেসে পার্মানেন্ট ডেটা সেভ (আর্নিং ও টার্গেটের সুরক্ষার জন্য)
        Map<String, Object> map = new HashMap<>();
        map.put("uid", currentUserId);
        map.put("name", name);
        map.put("gender", selectedGender);
        map.put("country", selectedCountry);

        userDatabaseRef.child(currentUserId).updateChildren(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to save profile. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // নতুন ইউজার প্রোফাইল সেটআপ না করে বের হতে পারবে না
        Toast.makeText(this, "Please complete your profile setup first!", Toast.LENGTH_SHORT).show();
    }
}
