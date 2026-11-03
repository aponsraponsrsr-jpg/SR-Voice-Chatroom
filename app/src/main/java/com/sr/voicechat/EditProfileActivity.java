package com.srvoicechatroom;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class EditProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        EditText etName = null;
        TextView tvId = null;
        Button btnSave = null;
        ImageView imgProfile = null;

        try { etName = findViewById(R.id.etName); } catch(Exception e){}
        try { tvId = findViewById(R.id.tvId); } catch(Exception e){}
        try { btnSave = findViewById(R.id.btnSave); } catch(Exception e){}
        try { imgProfile = findViewById(R.id.imgProfile); } catch(Exception e){}

        // ID বানানো
        String randomId = "ID: 999" + new Random().nextInt(900000);
        if (tvId != null) tvId.setText(randomId);

        // Profile Pic এ Click করলে Toast দেখাবে, Gallery পরে Add করবো
        if (imgProfile != null) {
            imgProfile.setOnClickListener(v -> {
                Toast.makeText(this, "Gallery Coming Soon - STEP 3", Toast.LENGTH_SHORT).show();
            });
        }
        try {
            findViewById(R.id.layoutProfilePic).setOnClickListener(v -> {
                Toast.makeText(this, "Gallery Coming Soon - STEP 3", Toast.LENGTH_SHORT).show();
            });
        } catch(Exception e){}

        // Save করলে Permanent
        EditText finalEtName = etName;
        TextView finalTvId = tvId;
        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String name = finalEtName != null ? finalEtName.getText().toString() : "SR User";
                String userId = finalTvId != null ? finalTvId.getText().toString() : randomId;

                SharedPreferences prefs = getSharedPreferences("SRVoice", MODE_PRIVATE);
                prefs.edit()
                    .putBoolean("isProfileSaved", true)
                    .putString("myName", name)
                    .putString("myId", userId)
                    .apply();

                Toast.makeText(this, "Profile Saved Permanent!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            });
        }
    }
}
