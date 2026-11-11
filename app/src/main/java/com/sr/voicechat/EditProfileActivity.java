package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private EditText etProfileName;
    private TextView tvSelectedGender, tvSelectedCountry, tvPermanentId;
    private ImageView ivProfilePhoto;
    private Button btnSaveProfile;
    private SharedPreferences prefs;
    private DatabaseReference userDatabaseRef;
    private String firebaseUid = "";
    private String my9DigitId = "";
    private String selectedGender = "Male";
    private String selectedCountry = "Bangladesh";
    private String selectedPhotoBase64 = ""; // গ্যালারি থেকে ছবি
    private boolean isLocked = false;
    private boolean isAlreadySaved = false;

    private final String[] countriesList = {
        "Bangladesh", "Saudi Arabia", "India", "United Arab Emirates",
        "Pakistan", "United States", "United Kingdom", "Malaysia",
        "Oman", "Qatar", "Kuwait", "Bahrain", "Iraq", "Canada"
    };

    // গ্যালারি থেকে ছবি নেওয়া
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) {
                    try {
                        InputStream is = getContentResolver().openInputStream(uri);
                        Bitmap bmp = BitmapFactory.decodeStream(is);
                        // ছোট করে নাও যাতে Firebase এ সেভ হয়
                        Bitmap scaled = Bitmap.createScaledBitmap(bmp, 300, 300, true);
                        ivProfilePhoto.setImageBitmap(scaled);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                        selectedPhotoBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                        Toast.makeText(this, "Photo Selected - Now Press Save", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Photo Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    private String getFlag(String c) {
        if (c == null) return "";
        switch (c) {
            case "Bangladesh": return "🇧🇩";
            case "Saudi Arabia": return "🇸🇦";
            case "India": return "🇮🇳";
            case "United Arab Emirates": return "🇦🇪";
            case "Pakistan": return "🇵🇰";
            case "United States": return "🇺🇸";
            case "United Kingdom": return "🇬🇧";
            case "Malaysia": return "🇲🇾";
            case "Oman": return "🇴🇲";
            case "Qatar": return "🇶🇦";
            case "Kuwait": return "🇰🇼";
            case "Bahrain": return "🇧🇭";
            case "Iraq": return "🇮🇶";
            case "Canada": return "🇨🇦";
            default: return "🌍";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) firebaseUid = user.getUid();
        if (firebaseUid.isEmpty()) firebaseUid = prefs.getString("MY_9_DIGIT_ID", "");
        my9DigitId = prefs.getString("MY_9_DIGIT_ID", "");
        if (my9DigitId.isEmpty()) {
            int r = (int) (Math.random() * 900000000) + 100000000;
            my9DigitId = String.valueOf(r);
            prefs.edit().putString("MY_9_DIGIT_ID", my9DigitId).apply();
        }
        prefs.edit().putString("FIREBASE_UID", firebaseUid).apply();
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 40, 40, 40);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")});
        root.setBackground(bg);

        TextView title = new TextView(this);
        title.setText("⚙️ Complete Your Profile (Permanent)");
        title.setTextColor(Color.parseColor("#FFD700"));
        title.setTextSize(18);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        root.addView(title);

        // ===== প্রোফাইল ছবি - গোল + ক্যামেরা আইকন =====
        LinearLayout photoLayout = new LinearLayout(this);
        photoLayout.setOrientation(LinearLayout.VERTICAL);
        photoLayout.setGravity(Gravity.CENTER);
        photoLayout.setPadding(0, 0, 0, 10);

        ivProfilePhoto = new ImageView(this);
        ivProfilePhoto.setImageResource(android.R.drawable.ic_menu_camera);
        ivProfilePhoto.setBackgroundColor(Color.parseColor("#2A1B4E"));
        ivProfilePhoto.setPadding(30, 30, 30, 30);
        LinearLayout.LayoutParams photoParams = new LinearLayout.LayoutParams(260, 260);
        photoParams.setMargins(0, 0, 0, 10);
        ivProfilePhoto.setLayoutParams(photoParams);
        ivProfilePhoto.setOnClickListener(v -> openGallery());
        photoLayout.addView(ivProfilePhoto);

        TextView tvTapPhoto = new TextView(this);
        tvTapPhoto.setText("📷 Tap to Select Profile Photo");
        tvTapPhoto.setTextColor(Color.parseColor("#B0BEC5"));
        tvTapPhoto.setTextSize(13);
        tvTapPhoto.setGravity(Gravity.CENTER);
        tvTapPhoto.setOnClickListener(v -> openGallery());
        photoLayout.addView(tvTapPhoto);
        root.addView(photoLayout);

        tvPermanentId = new TextView(this);
        tvPermanentId.setText("Permanent ID: " + my9DigitId + " (Locked)");
        tvPermanentId.setTextColor(Color.parseColor("#B0BEC5"));
        tvPermanentId.setTextSize(14);
        tvPermanentId.setPadding(0, 10, 0, 20);
        tvPermanentId.setGravity(Gravity.CENTER);
        root.addView(tvPermanentId);

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

        tvSelectedGender = new TextView(this);
        tvSelectedGender.setText("Gender: Male (Click to Change)");
        tvSelectedGender.setTextColor(Color.WHITE);
        tvSelectedGender.setTextSize(15);
        tvSelectedGender.setPadding(30, 30, 30, 30);
        tvSelectedGender.setBackgroundColor(Color.parseColor("#2A1B4E"));
        tvSelectedGender.setLayoutParams(p1);
        tvSelectedGender.setOnClickListener(v -> showGenderPickerDialog());
        root.addView(tvSelectedGender);

        tvSelectedCountry = new TextView(this);
        tvSelectedCountry.setText("Country: Bangladesh 🇧🇩 (Click to Change)");
        tvSelectedCountry.setTextColor(Color.WHITE);
        tvSelectedCountry.setTextSize(15);
        tvSelectedCountry.setPadding(30, 30, 30, 30);
        tvSelectedCountry.setBackgroundColor(Color.parseColor("#2A1B4E"));
        tvSelectedCountry.setLayoutParams(p1);
        tvSelectedCountry.setOnClickListener(v -> showCountryPickerDialog());
        root.addView(tvSelectedCountry);

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
        checkIfAlreadyLocked();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void checkIfAlreadyLocked() {
        if (firebaseUid.isEmpty()) return;
        userDatabaseRef.child(firebaseUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot s) {
                if (s.exists() && s.hasChild("gender") && s.hasChild("country")) {
                    String g = s.child("gender").getValue(String.class);
                    String c = s.child("country").getValue(String.class);
                    String n = s.child("name").getValue(String.class);
                    String savedId = s.child("my9DigitId").getValue(String.class);
                    String photo = s.child("photoBase64").getValue(String.class);
                    // Firebase এ ID থাকলে ওইটাই রাখো, নাহলে স্ক্রিনেরটা রাখো - নতুন বানাবে না
                    if (savedId != null && !savedId.isEmpty()) {
                        my9DigitId = savedId;
                        prefs.edit().putString("MY_9_DIGIT_ID", my9DigitId).apply();
                        tvPermanentId.setText("Permanent ID: " + my9DigitId + " (Locked)");
                    }
                    if (g != null) selectedGender = g;
                    if (c != null) {
                        selectedCountry = c.replaceAll("[^a-zA-Z ]", "").trim();
                        if (selectedCountry.isEmpty()) selectedCountry = "Bangladesh";
                    }
                    if (n != null) etProfileName.setText(n);
                    if (photo != null && !photo.isEmpty()) {
                        try {
                            byte[] bytes = Base64.decode(photo, Base64.DEFAULT);
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            ivProfilePhoto.setImageBitmap(bmp);
                            selectedPhotoBase64 = photo;
                        } catch (Exception ignored) {}
                    }
                    tvSelectedGender.setText("Gender: " + selectedGender + " (Locked)");
                    tvSelectedCountry.setText("Country: " + selectedCountry + " " + getFlag(selectedCountry) + " (Locked)");
                    isLocked = true;
                    isAlreadySaved = true;
                    btnSaveProfile.setText("Confirm & Enter Home");
                    Toast.makeText(EditProfileActivity.this, "Already Saved - Press Confirm to Enter Home", Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onCancelled(DatabaseError e) {}
        });
    }

    private void showGenderPickerDialog() {
        if (isLocked) { Toast.makeText(this, "Gender is Permanent - Cannot Change", Toast.LENGTH_SHORT).show(); return; }
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
        if (isLocked) { Toast.makeText(this, "Country is Permanent - Cannot Change", Toast.LENGTH_SHORT).show(); return; }
        String[] display = new String[countriesList.length];
        for (int i = 0; i < countriesList.length; i++) display[i] = countriesList[i] + " " + getFlag(countriesList[i]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Your Country (Permanent)");
        builder.setItems(display, (dialog, which) -> {
            selectedCountry = countriesList[which];
            tvSelectedCountry.setText("Country: " + selectedCountry + " " + getFlag(selectedCountry) + " (Locked)");
        });
        builder.show();
    }

    private void saveProfileToFirebase() {
        String name = etProfileName.getText().toString().trim();
        if (name.isEmpty()) { Toast.makeText(this, "Please enter your name!", Toast.LENGTH_SHORT).show(); return; }
        if (firebaseUid.isEmpty()) { Toast.makeText(this, "Login error, please login again", Toast.LENGTH_SHORT).show(); return; }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving... Please Wait");

        prefs.edit().putString("USER_NAME", name)
                .putString("USER_GENDER", selectedGender)
                .putString("USER_COUNTRY", selectedCountry)
                .putString("USER_PHOTO", selectedPhotoBase64)
                .putBoolean("IS_PROFILE_SETUP_DONE", true)
                .apply();

        Map<String, Object> map = new HashMap<>();
        map.put("uid", firebaseUid);
        map.put("my9DigitId", my9DigitId);
        map.put("name", name);
        map.put("gender", selectedGender);
        map.put("country", selectedCountry);
        if (!selectedPhotoBase64.isEmpty()) map.put("photoBase64", selectedPhotoBase64);
        // coins এখানে আর 0 দিয়ে ওভাররাইট করবো না - নাহলে পুরান কয়েন মুছে যেতো, এটাই লুপের একটা কারণ ছিলো

        userDatabaseRef.child(firebaseUid).updateChildren(map).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(EditProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                btnSaveProfile.setEnabled(true);
                btnSaveProfile.setText("Save & Enter Home");
                Toast.makeText(this, "Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Please complete your profile setup first!", Toast.LENGTH_SHORT).show();
    }
}
