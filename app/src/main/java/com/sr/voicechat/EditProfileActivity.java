package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editName;
    private TextView textUserId;
    private RadioGroup genderGroup;
    private Button btnSave;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("SR_USER", MODE_PRIVATE);

        // ফাইন্ডিং ভিউজ (XML-এর সঠিক আইডির সাথে ম্যাপ করা হয়েছে)
        editName = findViewById(R.id.editName);
        textUserId = findViewById(R.id.textUserId);
        genderGroup = findViewById(R.id.genderGroup);
        btnSave = findViewById(R.id.btnSave);

        // ১. SharedPreferences থেকে ৯+ ডিজিটের ইউনিক আইডি লোড করে স্ক্রিনে দেখানো
        String uniqueId = prefs.getString("MY_9_DIGIT_ID", "77788899");
        textUserId.setText("ID: " + uniqueId);

        // ফায়ারবেস থেকে আগের ডিসপ্লে নেম থাকলে তা বসিয়ে দেওয়া
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getDisplayName() != null) {
            editName.setText(mAuth.getCurrentUser().getDisplayName());
        }

        // ২. সেভ বাটন ক্লিক লজিক
        btnSave.setOnClickListener(v -> {
            String newName = editName.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            // জেন্ডার সিলেক্ট করা
            int selectedGenderId = genderGroup.getCheckedRadioButtonId();
            String gender = "Male";
            if (selectedGenderId == R.id.radioFemale) {
                gender = "Female";
            }

            // লোকাল SharedPreferences-এ পার্মানেন্টলি সেভ করা এবং প্রোফাইল কমপ্লিট ফ্ল্যাগ true করা
            prefs.edit()
                    .putString("USER_NAME", newName)
                    .putString("USER_GENDER", gender)
                    .putBoolean("PROFILE_COMPLETED", true)
                    .apply();

            // ফায়ারবেসেও ডেটা সিংক করা (ব্যাকগ্রাউন্ডে)
            if (mAuth.getCurrentUser() != null) {
                Map<String, Object> update = new HashMap<>();
                update.put("name", newName);
                update.put("gender", gender);
                update.put("uniqueId", uniqueId);

                db.collection("users").document(mAuth.getCurrentUser().getUid())
                        .set(update, SetOptions.merge());
            }

            Toast.makeText(this, "Profile Saved Successfully!", Toast.LENGTH_SHORT).show();

            // ৩. সরাসরি হোম পেজে চলে যাওয়া এবং বর্তমান অ্যাক্টিভিটি ফিনিশ করা
            Intent intent = new Intent(EditProfileActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
