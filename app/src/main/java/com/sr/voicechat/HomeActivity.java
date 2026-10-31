package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // হোমপেজ লোড হওয়ার সাথে সাথে কোনো ক্র্যাশ এড়াতে সেফ কোড রাখা হলো।
        // পরবর্তী ধাপে তুমি নিচের Me পেজ বা রুম ক্রিয়েশনের জন্য নির্দিষ্ট বাটন বা লেআউট যুক্ত করতে পারবে।
    }
}
