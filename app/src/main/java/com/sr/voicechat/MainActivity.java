package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Uri profileUri = null;
    private ImageView imgProfile;
    private EditText etName;
    private Button btnSave;
    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    profileUri = result.getData().getData();
                    if (imgProfile != null) imgProfile.setImageURI(profileUri);
                }
            }
        );

        imgProfile = findImg("imgProfile","imgPick","profileImage","ivProfile","imgAvatar");
        etName = findEdit("etName","etUserName","etRoomName","name");
        btnSave = findBtn("btnSave","btnSaveAndEnter","btnSaveEnterChatroom","btnEnter","btnOpenRoom");

        if (imgProfile != null) {
            imgProfile.setOnClickListener(v -> {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                launcher.launch(i);
            });
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                String name = etName != null ? etName.getText().toString().trim() : "CEO";
                if (name.isEmpty()) {
                    Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show();
                    return;
                }
                SharedPreferences p = getSharedPreferences("SRVoice", MODE_PRIVATE);
                p.edit().putString("userName", name).putString("userImage", profileUri != null ? profileUri.toString() : "").apply();
                Toast.makeText(this, "Welcome " + name, Toast.LENGTH_SHORT).show();
                try {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                } catch (Exception e) {
                    Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private ImageView findImg(String... names){for(String n:names){int id=getResources().getIdentifier(n,"id",getPackageName()); if(id!=0){try{return findViewById(id);}catch(Exception e){}}}return null;}
    private EditText findEdit(String... names){for(String n:names){int id=getResources().getIdentifier(n,"id",getPackageName()); if(id!=0){try{return findViewById(id);}catch(Exception e){}}}return null;}
    private Button findBtn(String... names){for(String n:names){int id=getResources().getIdentifier(n,"id",getPackageName()); if(id!=0){try{return findViewById(id);}catch(Exception e){}}}return null;}
}
