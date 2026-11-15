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
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class EditProfileActivity extends AppCompatActivity {
    private EditText etProfileName;
    private TextView tvSelectedGender, tvSelectedCountry, tvPermanentId;
    private ImageView ivProfilePhoto;
    private Button btnSaveProfile;
    private SharedPreferences prefs;
    private DatabaseReference userRef, usersRef;
    private String firebaseUid = "", userEmail = "", selectedGender = "Male", selectedCountry = "Bangladesh";
    private String selectedPhotoUrl = "";
    private String selectedPhotoBase64 = ""; // FIX - Storage fail hole ei backup diye save hobe
    private String oldPhotoUrl = "";
    private Uri pickedImageUri = null;
    private Bitmap pickedBitmap = null;
    private boolean isLocked = false, isSaving = false, isEditMode = false;
    private final String[] countriesList = {"Bangladesh","Saudi Arabia","India","United Arab Emirates","Pakistan","United States","United Kingdom","Malaysia","Oman","Qatar","Kuwait","Bahrain","Iraq","Canada","Nepal","Sri Lanka","Indonesia","Philippines","Egypt","Turkey","Iran","Afghanistan","Myanmar","Thailand","Vietnam","China","Japan","South Korea","Australia","Germany","France","Italy","Spain","Netherlands","Russia","Brazil","Mexico","South Africa","Nigeria","Kenya","Morocco","Algeria","Jordan","Lebanon","Yemen","Syria","Uzbekistan","Kazakhstan","Singapore","Hong Kong","New Zealand"};

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) {
                    try {
                        pickedImageUri = uri;
                        InputStream is = getContentResolver().openInputStream(uri);
                        Bitmap bmp = BitmapFactory.decodeStream(is);
                        if (bmp == null) return;
                        // FIX - Boro chobi ke choto kore fela, jate Firebase e dhuke
                        int w = bmp.getWidth(); int h = bmp.getHeight();
                        int max = 400;
                        if (w > max || h > max) {
                            float ratio = Math.min((float)max / w, (float)max / h);
                            pickedBitmap = Bitmap.createScaledBitmap(bmp, (int)(w*ratio), (int)(h*ratio), true);
                            try { bmp.recycle(); } catch (Exception ignored) {}
                        } else {
                            pickedBitmap = bmp;
                        }
                        ivProfilePhoto.setImageBitmap(pickedBitmap);
                        // FIX - Age thekei Base64 baniye rakha, Storage fail hole sathe sathe kaje lagbe
                        selectedPhotoBase64 = bitmapToBase64(pickedBitmap);
                        Toast.makeText(this, "Photo Selected - Now Press Save", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) { Toast.makeText(this, "Photo Failed", Toast.LENGTH_SHORT).show(); }
                }
            }
        });

    private String bitmapToBase64(Bitmap bmp) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] data = baos.toByteArray();
            return Base64.encodeToString(data, Base64.DEFAULT);
        } catch (Exception e) { return ""; }
    }

    private String getFlag(String c) {
        if (c == null) return "";
        switch (c) {
            case "Bangladesh": return "🇧🇩"; case "Saudi Arabia": return "🇸🇦"; case "India": return "🇮🇳";
            case "United Arab Emirates": return "🇦🇪"; case "Pakistan": return "🇵🇰"; case "United States": return "🇺🇸";
            case "United Kingdom": return "🇬🇧"; case "Malaysia": return "🇲🇾"; case "Oman": return "🇴🇲";
            case "Qatar": return "🇶🇦"; case "Kuwait": return "🇰🇼"; case "Bahrain": return "🇧🇭";
            case "Iraq": return "🇮🇶"; case "Canada": return "🇨🇦"; case "Nepal": return "🇳🇵";
            case "Sri Lanka": return "🇱🇰"; case "Indonesia": return "🇮🇩"; case "Philippines": return "🇵🇭";
            case "Egypt": return "🇪🇬"; case "Turkey": return "🇹🇷"; case "Australia": return "🇦🇺";
            case "Germany": return "🇩🇪"; case "France": return "🇫🇷"; case "Italy": return "🇮🇹";
            case "Spain": return "🇪🇸"; case "Japan": return "🇯🇵"; case "South Korea": return "🇰🇷";
            case "China": return "🇨🇳"; case "Brazil": return "🇧🇷"; case "Russia": return "🇷🇺";
            default: return "🌍";
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) { firebaseUid = user.getUid(); if (user.getEmail() != null) userEmail = user.getEmail(); }
        if (userEmail.isEmpty()) userEmail = prefs.getString("USER_EMAIL", "");
        if (firebaseUid.isEmpty()) firebaseUid = prefs.getString("FIREBASE_UID", "");
        if (firebaseUid.isEmpty()) { Toast.makeText(this, "Login error, please login again", Toast.LENGTH_LONG).show(); finish(); return; }
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        userRef = usersRef.child(firebaseUid);
        isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setPadding(40,40,40,40); root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#1B1035"), Color.parseColor("#0A0518")}));
        TextView title = new TextView(this);
        title.setText(isEditMode ? "✏️ Edit Profile" : "⚙️ Complete Your Profile (Permanent)");
        title.setTextColor(Color.parseColor("#FFD700")); title.setTextSize(18); title.setTypeface(null, android.graphics.Typeface.BOLD); title.setGravity(Gravity.CENTER); title.setPadding(0,0,0,20); root.addView(title);
        LinearLayout photoLayout = new LinearLayout(this); photoLayout.setOrientation(LinearLayout.VERTICAL); photoLayout.setGravity(Gravity.CENTER); photoLayout.setPadding(0,0,0,10);
        ivProfilePhoto = new ImageView(this); ivProfilePhoto.setImageResource(android.R.drawable.ic_menu_camera); ivProfilePhoto.setBackgroundColor(Color.parseColor("#2A1B4E")); ivProfilePhoto.setPadding(30,30,30,30);
        LinearLayout.LayoutParams photoParams = new LinearLayout.LayoutParams(260,260); photoParams.setMargins(0,0,0,10); ivProfilePhoto.setLayoutParams(photoParams); ivProfilePhoto.setOnClickListener(v -> openGallery()); photoLayout.addView(ivProfilePhoto);
        TextView tvTapPhoto = new TextView(this); tvTapPhoto.setText("📷 Tap to Select Profile Photo"); tvTapPhoto.setTextColor(Color.parseColor("#B0BEC5")); tvTapPhoto.setTextSize(13); tvTapPhoto.setGravity(Gravity.CENTER); tvTapPhoto.setOnClickListener(v -> openGallery()); photoLayout.addView(tvTapPhoto); root.addView(photoLayout);
        tvPermanentId = new TextView(this); tvPermanentId.setText("Permanent ID: Will be generated on Save (One Time)"); tvPermanentId.setTextColor(Color.parseColor("#B0BEC5")); tvPermanentId.setTextSize(14); tvPermanentId.setPadding(0,10,0,20); tvPermanentId.setGravity(Gravity.CENTER); root.addView(tvPermanentId);
        etProfileName = new EditText(this); etProfileName.setHint("Enter Your Name"); etProfileName.setHintTextColor(Color.parseColor("#757575")); etProfileName.setTextColor(Color.WHITE); etProfileName.setPadding(30,30,30,30); etProfileName.setBackgroundColor(Color.parseColor("#2A1B4E"));
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 140); p1.setMargins(0,0,0,20); etProfileName.setLayoutParams(p1); root.addView(etProfileName);
        tvSelectedGender = new TextView(this); tvSelectedGender.setText("Gender: Male (Click to Change)"); tvSelectedGender.setTextColor(Color.WHITE); tvSelectedGender.setTextSize(15); tvSelectedGender.setPadding(30,30,30,30); tvSelectedGender.setBackgroundColor(Color.parseColor("#2A1B4E")); tvSelectedGender.setLayoutParams(p1); tvSelectedGender.setOnClickListener(v -> showGenderPickerDialog()); root.addView(tvSelectedGender);
        tvSelectedCountry = new TextView(this); tvSelectedCountry.setText("Country: Bangladesh 🇧🇩 (Click to Change)"); tvSelectedCountry.setTextColor(Color.WHITE); tvSelectedCountry.setTextSize(15); tvSelectedCountry.setPadding(30,30,30,30); tvSelectedCountry.setBackgroundColor(Color.parseColor("#2A1B4E")); tvSelectedCountry.setLayoutParams(p1); tvSelectedCountry.setOnClickListener(v -> showCountryPickerDialog()); root.addView(tvSelectedCountry);
        btnSaveProfile = new Button(this); btnSaveProfile.setText(isEditMode ? "Update Name & Photo" : "Save & Enter Home"); btnSaveProfile.setTextColor(Color.WHITE); btnSaveProfile.setBackgroundColor(Color.parseColor("#FF4081"));
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 140); p2.setMargins(0,30,0,0); btnSaveProfile.setLayoutParams(p2); btnSaveProfile.setOnClickListener(v -> saveProfileToFirebase()); root.addView(btnSaveProfile);
        setContentView(root);
        checkIfAlreadyExists();
    }

    private void openGallery() { Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); intent.setType("image/*"); pickImageLauncher.launch(intent); }

    private void loadPhoto(String url, String base64) {
        if (url != null && !url.isEmpty() && url.startsWith("http")) {
            try { Glide.with(this).load(url).circleCrop().into(ivProfilePhoto); return; } catch (Exception ignored) {}
        }
        if (base64 != null && !base64.isEmpty()) {
            try { byte[] bytes = Base64.decode(base64, Base64.DEFAULT); Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length); if (bmp != null) ivProfilePhoto.setImageBitmap(bmp); } catch (Exception ignored) {}
        }
    }

    private void checkIfAlreadyExists() {
        if (firebaseUid.isEmpty()) return;
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot s) {
                if (s.exists() && s.hasChild("my9DigitId") && s.hasChild("gender")) {
                    String id = s.child("my9DigitId").getValue(String.class);
                    String n = s.child("name").getValue(String.class);
                    String g = s.child("gender").getValue(String.class);
                    String c = s.child("country").getValue(String.class);
                    String photoUrl = s.child("photoUrl").getValue(String.class);
                    if (photoUrl == null) photoUrl = s.child("photo").getValue(String.class);
                    String base64 = s.child("photoBase64").getValue(String.class);
                    if (photoUrl != null) { oldPhotoUrl = photoUrl; selectedPhotoUrl = photoUrl; }
                    if (isEditMode) {
                        if (n != null) etProfileName.setText(n);
                        if (g != null) selectedGender = g;
                        if (c != null) selectedCountry = c;
                        loadPhoto(photoUrl, base64);
                        if (id != null) tvPermanentId.setText("Permanent ID: " + id + " (Locked - Cannot Change)");
                        if (g != null) tvSelectedGender.setText("Gender: " + g + " (Locked)");
                        if (c != null) tvSelectedCountry.setText("Country: " + c + " " + getFlag(c) + " (Locked)");
                        isLocked = true; return;
                    }
                    prefs.edit().putString("MY_9_DIGIT_ID", id != null ? id : "").putString("USER_NAME", n).putString("USER_GENDER", g).putString("USER_COUNTRY", c).putString("USER_PHOTO", photoUrl != null ? photoUrl : "").putBoolean("IS_PROFILE_SETUP_DONE", true).apply();
                    Intent intent = new Intent(EditProfileActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); startActivity(intent); finish();
                }
            }
            @Override public void onCancelled(DatabaseError e) {}
        });
    }

    private void showGenderPickerDialog() {
        if (isLocked) { Toast.makeText(this, "Gender is Permanent - Cannot Change", Toast.LENGTH_SHORT).show(); return; }
        String[] genders = {"Male","Female"};
        AlertDialog.Builder b = new AlertDialog.Builder(this); b.setTitle("Select Gender (Permanent)");
        b.setItems(genders, (d,w) -> { selectedGender = genders[w]; tvSelectedGender.setText("Gender: " + selectedGender + " (Locked)"); }); b.show();
    }
    private void showCountryPickerDialog() {
        if (isLocked) { Toast.makeText(this, "Country is Permanent - Cannot Change", Toast.LENGTH_SHORT).show(); return; }
        String[] display = new String[countriesList.length];
        for (int i=0;i<countriesList.length;i++) display[i] = countriesList[i] + " " + getFlag(countriesList[i]);
        AlertDialog.Builder b = new AlertDialog.Builder(this); b.setTitle("Select Your Country (Permanent)");
        b.setItems(display, (d,w) -> { selectedCountry = countriesList[w]; tvSelectedCountry.setText("Country: " + selectedCountry + " " + getFlag(selectedCountry) + " (Locked)"); }); b.show();
    }

    private void generateUniqueIdAndSave(String name) {
        String newId = String.valueOf(100000000 + new Random().nextInt(900000000));
        usersRef.orderByChild("my9DigitId").equalTo(newId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot s) { if (s.exists()) generateUniqueIdAndSave(name); else doTransactionSave(name, newId); }
            @Override public void onCancelled(DatabaseError e) { doTransactionSave(name, newId); }
        });
    }

    private void saveProfileToFirebase() {
        if (isSaving) return;
        String name = etProfileName.getText().toString().trim();
        if (name.isEmpty()) { Toast.makeText(this, "Please enter your name!", Toast.LENGTH_SHORT).show(); return; }
        if (firebaseUid.isEmpty()) { Toast.makeText(this, "Login error, please login again", Toast.LENGTH_SHORT).show(); return; }
        isSaving = true; btnSaveProfile.setEnabled(false); btnSaveProfile.setText("Saving... Please Wait");
        if (pickedBitmap != null) {
            uploadPhotoThenSave(name);
        } else {
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(DataSnapshot s) {
                    if (s.exists() && s.hasChild("my9DigitId")) doTransactionSave(name, null);
                    else generateUniqueIdAndSave(name);
                }
                @Override public void onCancelled(DatabaseError e) { doTransactionSave(name, null); }
            });
        }
    }

    private void uploadPhotoThenSave(String name) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            pickedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] data = baos.toByteArray();
            // FIX - Age Storage fail holei sesh hoye jeto. Ekhon fail hole Base64 backup diye Database e save hobe.
            StorageReference ref = FirebaseStorage.getInstance().getReference("profile_photos/" + firebaseUid + ".jpg");
            UploadTask task = ref.putBytes(data);
            task.addOnSuccessListener(t -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                selectedPhotoUrl = uri.toString();
                selectedPhotoBase64 = ""; // Storage success - Base64 lagbe na
                proceedToSave(name);
            }).addOnFailureListener(e -> {
                // URL pawa jay nai - Base64 diye save koro
                Toast.makeText(this, "Storage slow - Saving with backup...", Toast.LENGTH_SHORT).show();
                proceedToSave(name);
            }));
            task.addOnFailureListener(e -> {
                // FIX - Storage Fail = Ekhon ar "Failed" dibe na, Base64 backup cholbe
                Toast.makeText(this, "Storage failed (" + e.getMessage() + ") - Saving with backup...", Toast.LENGTH_LONG).show();
                selectedPhotoUrl = oldPhotoUrl; // Purano URL thakle rakha
                proceedToSave(name);
            });
        } catch (Exception e) {
            Toast.makeText(this, "Saving with backup...", Toast.LENGTH_SHORT).show();
            proceedToSave(name);
        }
    }

    private void proceedToSave(String name) {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot s) {
                if (s.exists() && s.hasChild("my9DigitId")) doTransactionSave(name, null);
                else generateUniqueIdAndSave(name);
            }
            @Override public void onCancelled(DatabaseError e) { doTransactionSave(name, null); }
        });
    }

    private void saveFailed() {
        isSaving = false; btnSaveProfile.setEnabled(true);
        btnSaveProfile.setText(isEditMode ? "Update Name & Photo" : "Save & Enter Home");
        Toast.makeText(this, "Photo Upload Failed, Try Again", Toast.LENGTH_LONG).show();
    }

    private void doTransactionSave(String name, String preGeneratedId) {
        userRef.runTransaction(new Transaction.Handler() {
            @Override public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getValue() == null) {
                    Map<String,Object> map = new HashMap<>();
                    map.put("uid", firebaseUid); map.put("email", userEmail);
                    String newId = preGeneratedId != null ? preGeneratedId : String.valueOf(100000000 + new Random().nextInt(900000000));
                    map.put("my9DigitId", newId); map.put("name", name); map.put("gender", selectedGender); map.put("country", selectedCountry);
                    if (selectedPhotoUrl != null && !selectedPhotoUrl.isEmpty()) {
                        map.put("photoUrl", selectedPhotoUrl);
                        map.put("photo", selectedPhotoUrl);
                    }
                    if (selectedPhotoBase64 != null && !selectedPhotoBase64.isEmpty()) {
                        map.put("photoBase64", selectedPhotoBase64);
                    }
                    map.put("followCount","0"); map.put("fansCount","0"); map.put("visitorCount","0");
                    currentData.setValue(map);
                } else {
                    if (currentData.child("my9DigitId").getValue() == null) {
                        String newId = preGeneratedId != null ? preGeneratedId : String.valueOf(100000000 + new Random().nextInt(900000000));
                        currentData.child("my9DigitId").setValue(newId);
                    }
                    currentData.child("name").setValue(name);
                    if (selectedPhotoUrl != null && !selectedPhotoUrl.isEmpty()) {
                        currentData.child("photoUrl").setValue(selectedPhotoUrl);
                        currentData.child("photo").setValue(selectedPhotoUrl);
                    }
                    // FIX - Storage fail hole Base64 save hobe, ar Storage success hole purano Base64 muche jabe
                    if (selectedPhotoBase64 != null && !selectedPhotoBase64.isEmpty()) {
                        currentData.child("photoBase64").setValue(selectedPhotoBase64);
                    } else if (selectedPhotoUrl != null && !selectedPhotoUrl.isEmpty()) {
                        currentData.child("photoBase64").setValue(null);
                    }
                }
                return Transaction.success(currentData);
            }
            @Override public void onComplete(DatabaseError error, boolean committed, DataSnapshot s) {
                if (error != null || !committed) {
                    isSaving = false; btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText(isEditMode ? "Update Name & Photo" : "Save & Enter Home");
                    Toast.makeText(EditProfileActivity.this, "Failed, Try Again", Toast.LENGTH_LONG).show(); return;
                }
                String finalId = s.child("my9DigitId").getValue(String.class);
                String finalGender = s.child("gender").getValue(String.class);
                String finalCountry = s.child("country").getValue(String.class);
                String finalPhoto = s.child("photoUrl").getValue(String.class);
                if (finalPhoto == null || finalPhoto.isEmpty()) finalPhoto = s.child("photoBase64").getValue(String.class);
                if (finalPhoto == null) finalPhoto = selectedPhotoUrl;
                if (finalPhoto == null) finalPhoto = selectedPhotoBase64;
                SharedPreferences.Editor e = prefs.edit();
                e.putString("FIREBASE_UID", firebaseUid); e.putString("MY_9_DIGIT_ID", finalId != null ? finalId : "");
                e.putString("USER_NAME", name); e.putString("USER_GENDER", finalGender != null ? finalGender : selectedGender);
                e.putString("USER_COUNTRY", finalCountry != null ? finalCountry : selectedCountry); e.putString("USER_EMAIL", userEmail);
                if (finalPhoto != null && !finalPhoto.isEmpty()) { e.putString("USER_PHOTO", finalPhoto); e.putString("PROFILE_PHOTO", finalPhoto); }
                e.putBoolean("IS_PROFILE_SETUP_DONE", true); e.apply();
                Intent broadcast = new Intent("com.sr.voicechat.PROFILE_UPDATED");
                broadcast.putExtra("NEW_PHOTO", finalPhoto != null ? finalPhoto : "");
                broadcast.putExtra("NEW_NAME", name);
                sendBroadcast(broadcast);
                if (isEditMode) {
                    Toast.makeText(EditProfileActivity.this, "Profile Updated!", Toast.LENGTH_LONG).show();
                    Intent back = new Intent();
                    back.putExtra("NEW_PHOTO", finalPhoto != null ? finalPhoto : "");
                    setResult(RESULT_OK, back);
                    finish();
                }
                else {
                    Toast.makeText(EditProfileActivity.this, "Profile Saved! Your ID: " + finalId, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(EditProfileActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); startActivity(intent); finish();
                }
            }
        });
    }
    @Override public void onBackPressed() {
        if (isEditMode) super.onBackPressed();
        else Toast.makeText(this, "Please complete your profile setup first!", Toast.LENGTH_SHORT).show();
    }
}
