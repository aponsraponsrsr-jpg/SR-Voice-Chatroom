package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ProfileSetupActivity extends AppCompatActivity {

    ImageView imgProfile;
    EditText etName;
    RadioGroup rgGender;
    Button btnConfirm;
    TextView tvId;
    String savedImagePath = "";
    ActivityResultLauncher<Intent> launcher;
    String myGender = "Male";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        SharedPreferences prefs = getSharedPreferences("SRVoice", MODE_PRIVATE);

        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
            if(r.getResultCode()==RESULT_OK && r.getData()!=null){
                Uri uri=r.getData().getData();
                if(uri!=null){
                    String path=copyToInternal(uri);
                    if(path!=null){
                        savedImagePath=path;
                        imgProfile.setImageURI(Uri.fromFile(new File(path)));
                    }
                }
            }
        });

        imgProfile = findViewById(getResources().getIdentifier("imgProfile","id",getPackageName()));
        if(imgProfile==null) imgProfile = findViewById(getResources().getIdentifier("ivProfile","id",getPackageName()));
        
        etName = findViewById(getResources().getIdentifier("etName","id",getPackageName()));
        if(etName==null) etName = findViewById(getResources().getIdentifier("editName","id",getPackageName()));
        
        rgGender = findViewById(getResources().getIdentifier("rgGender","id",getPackageName()));
        tvId = findViewById(getResources().getIdentifier("tvId","id",getPackageName()));
        
        btnConfirm = findViewById(getResources().getIdentifier("btnConfirm","id",getPackageName()));
        if(btnConfirm==null) btnConfirm = findViewById(getResources().getIdentifier("btnSave","id",getPackageName()));

        // ID Show করো
        String uid = IdManager.getOrCreateUserId(this);
        if(tvId!=null) tvId.setText("ID: " + uid);

        // আগে Save থাকলে Load করো
        String oldImg = prefs.getString("userImage","");
        if(!oldImg.isEmpty()){
            savedImagePath=oldImg;
            try{ imgProfile.setImageURI(Uri.fromFile(new File(oldImg))); }catch(Exception e){}
        }

        if(imgProfile!=null){
            imgProfile.setOnClickListener(v->{
                Intent i=new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                launcher.launch(i);
            });
        }

        if(rgGender!=null){
            rgGender.setOnCheckedChangeListener((group, checkedId) -> {
                int maleId = getResources().getIdentifier("rbMale","id",getPackageName());
                int femaleId = getResources().getIdentifier("rbFemale","id",getPackageName());
                if(checkedId==maleId) myGender="Male";
                else if(checkedId==femaleId) myGender="Female";
                else myGender="Male";
            });
        }

        if(btnConfirm!=null){
            btnConfirm.setOnClickListener(v->{
                String name = etName!=null ? etName.getText().toString().trim() : "";
                if(name.isEmpty()){
                    Toast.makeText(this,"Name লিখো",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(savedImagePath.isEmpty()){
                    Toast.makeText(this,"Profile Photo সেট করো",Toast.LENGTH_SHORT).show();
                    return;
                }

                prefs.edit()
                    .putString("userName",name)
                    .putString("userGender",myGender)
                    .putString("userImage",savedImagePath)
                    .putString("userId",uid)
                    .apply();

                Toast.makeText(this,"Profile Saved! Welcome",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            });
        }
    }

    private String copyToInternal(Uri uri){
        try{
            InputStream is=getContentResolver().openInputStream(uri);
            File f=new File(getFilesDir(),"profile.jpg");
            FileOutputStream fos=new FileOutputStream(f);
            byte[] b=new byte[1024]; int len;
            while((len=is.read(b))>0) fos.write(b,0,len);
            is.close(); fos.close();
            return f.getAbsolutePath();
        }catch(Exception e){return null;}
    }
}
