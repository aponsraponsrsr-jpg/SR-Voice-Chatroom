package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MeActivity extends AppCompatActivity {
    private ImageView imgProfile;
    private TextView tvName, tvId, tvGender;
    private SharedPreferences prefs;
    private ActivityResultLauncher<Intent> launcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);
        prefs = getSharedPreferences("SRVoice", MODE_PRIVATE);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) {
                    String savedPath = copyImageToInternal(uri);
                    if (savedPath != null) {
                        prefs.edit().putString("userImage", savedPath).apply();
                        loadImage(savedPath);
                        Toast.makeText(this, "Photo Saved Permanently!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        imgProfile = findView("imgProfile","ivProfile","profile_image","imgAvatar","imgPick","profile");
        tvName = findView("tvName","tvUserName","name");
        tvId = findView("tvId","tvUserId");
        tvGender = findView("tvGender","gender");

        // Edit Profile লেখা টা Click করলেও Gallery খুলবে
        TextView editBtn = findEditProfileText();
        if(editBtn != null) editBtn.setOnClickListener(v -> openGallery());
        if(imgProfile != null) imgProfile.setOnClickListener(v -> openGallery());
        if(tvName != null) tvName.setOnClickListener(v -> openGallery());

        loadProfile();
    }

    private void openGallery(){
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        launcher.launch(i);
    }

    private void loadProfile(){
        String name = prefs.getString("userName", "Sr voice");
        String myId = IdManager.getOrCreateUserId(this);
        String gender = prefs.getString("userGender", "Male");
        String imgPath = prefs.getString("userImage", "");

        if(tvName != null) tvName.setText(name);
        if(tvId != null) tvId.setText("ID: " + myId);
        if(tvGender != null) tvGender.setText("Gender: " + gender);
        if(!imgPath.isEmpty()) loadImage(imgPath);
    }

    private void loadImage(String path){
        if(imgProfile == null) return;
        try{
            Glide.with(this).load(new File(path)).circleCrop().into(imgProfile);
        }catch(Exception e){
            try{ Glide.with(this).load(path).circleCrop().into(imgProfile); }catch(Exception e2){}
        }
    }

    // Gallery ছবি App এর ভিতরে Permanent Save
    private String copyImageToInternal(Uri uri){
        try{
            InputStream is = getContentResolver().openInputStream(uri);
            File file = new File(getFilesDir(), "my_profile.jpg");
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while((len = is.read(buffer)) > 0) fos.write(buffer, 0, len);
            is.close(); fos.close();
            return file.getAbsolutePath();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private <T> T findView(String... names){for(String n:names){int id=getResources().getIdentifier(n,"id",getPackageName()); if(id!=0){try{T v=(T)findViewById(id); if(v!=null) return v;}catch(Exception e){}}}return null;}

    private TextView findEditProfileText(){
        try{
            android.view.ViewGroup root = (android.view.ViewGroup) findViewById(android.R.id.content);
            return searchText(root);
        }catch(Exception e){return null;}
    }
    private TextView searchText(android.view.ViewGroup g){
        if(g==null) return null;
        for(int i=0;i<g.getChildCount();i++){
            android.view.View v=g.getChildAt(i);
            if(v instanceof TextView){
                TextView tv=(TextView)v;
                if(tv.getText().toString().toLowerCase().contains("edit profile")) return tv;
            }else if(v instanceof android.view.ViewGroup){
                TextView r=searchText((android.view.ViewGroup)v);
                if(r!=null) return r;
            }
        }
        return null;
    }
}
