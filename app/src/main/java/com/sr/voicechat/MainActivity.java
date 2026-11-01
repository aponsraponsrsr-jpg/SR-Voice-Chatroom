package com.srvoice.chatroom;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("SR Voice - Green Tick OK - Ready for Login");
        tv.setTextSize(20f);
        tv.setPadding(50,100,50,100);
        setContentView(tv);
    }
}
