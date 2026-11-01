package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {

    private ImageView imgProfileMe;
    private TextView btnEditProfile, tvProfileName, tvProfileId, tvProfileGender, tvWalletCoins, tvVipStatus;
    
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        try {
            imgProfileMe = findViewById(R.id.imgProfileMe);
            btnEditProfile = findViewById(R.id.btnEditProfile);
            tvProfileName = findViewById(R.id.tvProfileName);
            tvProfileId = findViewById(R.id.tvProfileId);
            tvProfileGender = findViewById(R.id.tvProfileGender);
            tvWalletCoins = findViewById(R.id.tvWalletCoins);
            tvVipStatus = findViewById(R.id.tvVipStatus);

            // গ্যালারি থেকে ছবি সিলেক্ট করার জন্য লঞ্চার ইনিশিয়ালাইজ করা
            imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                getContentResolver().takePersistableUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            
                            // SharedPreferences-এ ছবির ইউআরআই সেভ করা
                            SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
                            prefs.edit().putString("USER_IMAGE", imageUri.toString()).apply();

                            // প্রোফাইল পিকচার সেট করা
                            imgProfileMe.setImageURI(imageUri);
                            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            );

            loadUserData();

            // Edit Profile এ ক্লিক করলে অপশন ডায়ালগ আসবে
            if (btnEditProfile != null) {
                btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
            }

            // ওয়ালেট ক্লিক করলে
            if (tvWalletCoins != null) {
                tvWalletCoins.setOnClickListener(v -> 
                    Toast.makeText(this, "Wallet Balance: 1,000 Coins", Toast.LENGTH_SHORT).show()
                );
            }

            // ভিআইপি ক্লিক করলে
            if (tvVipStatus != null) {
                tvVipStatus.setOnClickListener(v -> 
                    Toast.makeText(this, "VIP Status: Standard", Toast.LENGTH_SHORT).show()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // এডিট অপশনের পপ-আপ ডায়ালগ
    private void showEditProfileDialog() {
        CharSequence[] options = {"Change Profile Picture", "Change Name", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Profile");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Change Profile Picture")) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                imagePickerLauncher.launch(intent);
            } else if (options[item].equals("Change Name")) {
                showChangeNameDialog();
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    // নাম পরিবর্তনের ডায়ালগ
    private void showChangeNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Name");

        final EditText input = new EditText(this);
        input.setHint("Enter new name");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
                prefs.edit().putString("USER_NAME", newName).apply();
                if (tvProfileName != null) tvProfileName.setText(newName);
                Toast.makeText(this, "Name updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // ইউজার ডেটা লোড করার মেথড
    private void loadUserData() {
        try {
            SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
            String name = prefs.getString("USER_NAME", "Sr voice");
            String id = prefs.getString("USER_ID", "6533632");
            String gender = prefs.getString("USER_GENDER", "Male");
            String imageUriStr = prefs.getString("USER_IMAGE", null);

            if (tvProfileName != null) tvProfileName.setText(name);
            if (tvProfileId != null) tvProfileId.setText("ID: " + id);
            if (tvProfileGender != null) tvProfileGender.setText("Gender: " + gender);

            if (imageUriStr != null && !imageUriStr.isEmpty()) {
                try {
                    Uri imageUri = Uri.parse(imageUriStr);
                    imgProfileMe.setImageURI(imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }
}
