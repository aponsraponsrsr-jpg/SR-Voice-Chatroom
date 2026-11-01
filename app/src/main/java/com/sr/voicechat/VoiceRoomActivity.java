package com.sr.voicechat;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class VoiceRoomActivity extends AppCompatActivity {
    private ImageView[] seatsImg = new ImageView[8];
    private TextView[] seatsName = new TextView[8];
    private String myName, myImgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            setContentView(R.layout.activity_voice_room);
        }catch(Exception e){
            Toast.makeText(this, "Opening Room...", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences p = getSharedPreferences("SRVoice", MODE_PRIVATE);
        myName = p.getString("userName", "Sr voice");
        myImgPath = p.getString("userImage", "");

        for(int i=0;i<8;i++){
            int n=i+1;
            seatsImg[i] = findImg("seat"+n+"Image","imgSeat"+n,"ivSeat"+n,"seat_img_"+n);
            seatsName[i] = findTxt("seat"+n+"Name","tvSeat"+n);
            final int idx=i;
            if(seatsImg[i]!=null) seatsImg[i].setOnClickListener(v -> sit(idx));
        }

        Toast.makeText(this, "Click Seat to Sit - "+myName, Toast.LENGTH_SHORT).show();
    }

    private void sit(int idx){
        if(idx<0 || idx>=8) return;
        if(seatsImg[idx]==null) return;
        if(seatsName[idx]!=null) seatsName[idx].setText(myName);
        if(myImgPath!=null && !myImgPath.isEmpty()){
            try{
                File f=new File(myImgPath);
                if(f.exists()) seatsImg[idx].setImageURI(Uri.fromFile(f));
                else seatsImg[idx].setImageURI(Uri.parse(myImgPath));
                Toast.makeText(this, "You sat on Seat "+(idx+1), Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                seatsImg[idx].setImageResource(android.R.drawable.sym_def_app_icon);
            }
        }else{
            Toast.makeText(this, "Set photo in Me first", Toast.LENGTH_SHORT).show();
            seatsImg[idx].setImageResource(android.R.drawable.sym_def_app_icon);
            if(seatsName[idx]!=null) seatsName[idx].setText(myName);
        }
    }

    private ImageView findImg(String... names){for(String n:names){int id=getResources().getIdentifier(n,"id",getPackageName()); if(id!=0){try{return findViewById(id);}catch(Exception e){}}}return null;}
    private TextView findTxt(String... names){for(String n:names){int id=getResources().getIdentifier(n,"id",getPackageName()); if(id!=0){try{return findViewById(id);}catch(Exception e){}}}return null;}
}
