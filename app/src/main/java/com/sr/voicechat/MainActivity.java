package com.sr.voicechat;

import android.os.Bundle;
import android.widget.TextView;
import android.view.Gravity;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        TextView tv = new TextView(this);
        tv.setText("SR VOICE CHATROOM\n\nAlhamdulillah!\nApp Open Hoise Bondhu!");
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(22);
        tv.setPadding(50,400,50,50);
        setContentView(tv);
    }
}
