package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MeActivity extends AppCompatActivity {
    private ImageView imgProfile;
    private TextView tvName, tvId;
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
                    String path = copyImage(uri);
                    if (path != null) {
                        prefs.edit().putString("userImage", path).apply();
                        setImage(path);
                        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        imgProfile = findImage("imgProfile","ivProfile","profile_image","imgAvatar","profile");
        tvName = findText("tvName","tvUserName");
        tvId = findText("tvId","tvUserId");

        String name = prefs.getString("userName", "Sr voice");
        String myId = IdManager.getOrCreateUserId(this);
        String imgPath = prefs.getString("userImage", "");

        if(tvName != null) tvName.setText(name);
        if(tvId != null) tvId.setText("ID: " + myId);
        if(!imgPath.isEmpty()) setImage(imgPath);

        if(imgProfile != null) imgProfile.setOnClickListener(v -> openGallery());
        // Edit Profile লেখা খুঁজে Clickable করো
        TextView edit = searchEditText();
        if(edit != null) edit.setOnClickListener(v -> openGallery());
    }

    private void openGallery(){
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        launcher.launch(i);
    }
    private void setImage(String path){
        if(imgProfile == null) return;
        try{
            File f = new File(path);
            if(f.exists()){
                imgProfile.setImageURI(Uri.fromFile(f));
            }else{
                imgProfile.setImageURI(Uri.parse(path));
            }
        }catch(Exception e){}
    }
    private String copyImage(Uri uri){
        try{
            InputStream is = getContentResolver().openInputStream(uri);
            File file = new File(getFilesDir(), "my_profile.jpg");
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len = is.read(buf)) > 0) fos.write(buf, 0, len);
            is.close(); fos.close();
            return file.getAbsolutePath();
        }catch(Exception e){ return null; }
    }
    private ImageView findImage(String... names){for(String n:names){int id=getResources().getIdentifier(n,"id",getPackageName()); if(id!=0){try{return findViewById(id);}catch(Exception e){}}}return null;}
    private TextView findText(String... names){for(String n:names){int id=getResources().getIdentifier(n,"id",getPackageName()); if(id!=0){try{return findViewById(id);}catch(Exception e){}}}return null;}
    private TextView searchEditText(){
        try{
            android.view.ViewGroup root = (android.view.ViewGroup) findViewById(android.R.id.content);
            return search(root);
        }catch(Exception e){return null;}
    }
    private TextView search(android.view.ViewGroup g){
        if(g==null) return null;
        for(int i=0;i<g.getChildCount();i++){
            android.view.View v=g.getChildAt(i);
            if(v instanceof TextView){
                if(((TextView)v).getText().toString().toLowerCase().contains("edit")) return (TextView)v;
            }else if(v instanceof android.view.ViewGroup){
                TextView r=search((android.view.ViewGroup)v);
                if(r!=null) return r;
            }
        }
        return null;
    }
}
