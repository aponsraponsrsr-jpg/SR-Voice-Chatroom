package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class MeActivity extends AppCompatActivity {
    private ImageView imgProfile;
    private TextView tvName, tvId;
    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (imgProfile != null) imgProfile.setImageURI(uri);
                    if (uri != null) getSharedPreferences("SRVoice", MODE_PRIVATE).edit().putString("userImage", uri.toString()).apply();
                }
            }
        );

        imgProfile = findImg("imgProfile","ivProfile","profile_image","imgAvatar","imgPick");
        tvName = findTxt("tvName","tvUserName","name","etName");
        tvId = findTxt("tvId","tvUserId");

        SharedPreferences p = getSharedPreferences("SRVoice", MODE_PRIVATE);
        String name = p.getString("userName", "real CEO");
        String myId = IdManager.getOrCreateUserId(this);
        String img = p.getString("userImage", "");

        if (tvName != null) tvName.setText(name);
        if (tvId != null) tvId.setText("ID: " + myId);
        if (imgProfile != null && !img.isEmpty()) {
            try { imgProfile.setImageURI(Uri.parse(img)); } catch (Exception e){}
        }

        if (imgProfile != null) {
            imgProfile.setOnClickListener(v -> {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                launcher.launch(i);
            });
        }
    }
    private ImageView findImg(String... names){for(String n:names){int id=getResources().getIdentifier(n,"id",getPackageName()); if(id!=0){try{ImageView v=findViewById(id); if(v!=null) return v;}catch(Exception e){}}}return null;}
    private TextView findTxt(String... names){for(String n:names){int id=getResources().getIdentifier(n,"id",getPackageName()); if(id!=0){try{TextView v=findViewById(id); if(v!=null) return v;}catch(Exception e){}}}return null;}
}
