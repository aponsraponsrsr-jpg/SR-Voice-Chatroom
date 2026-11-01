package com.sr.voicechat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // আগে Login করা থাকলে Direct Home এ যাবে
        SharedPreferences prefs = getSharedPreferences("SRVoice", MODE_PRIVATE);
        String alreadyName = prefs.getString("userName", "");
        if (!alreadyName.isEmpty()) {
            goToHome();
            return;
        }

        btnGoogle = findBtn(
            "btnGoogle", "btnSignInWithGoogle", "btnSignIn", "sign_in_button",
            "btnGoogleSignIn", "btnLogin", "btnContinueWithGoogle", "btnSignInGoogle"
        );

        // যদি ID না পায়, তাহলে সব Button খুঁজে Google লেখা Button টা বের করো
        if (btnGoogle == null) {
            btnGoogle = findGoogleButtonByText();
        }

        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> {
                // Google Click Success - এখন Home এ পাঠাও
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("userName", "Sr Voice User");
                editor.putString("userId", IdManager.getOrCreateUserId(this));
                editor.putString("userImage", "");
                editor.apply();

                Toast.makeText(this, "Welcome to SR Voice!", Toast.LENGTH_SHORT).show();
                goToHome();
            });
        } else {
            Toast.makeText(this, "Button not found - check layout", Toast.LENGTH_LONG).show();
        }
    }

    private void goToHome(){
        try{
            Intent i = new Intent(this, HomeActivity.class);
            startActivity(i);
            finish();
        }catch(Exception e){
            Toast.makeText(this, "HomeActivity not found: "+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private Button findBtn(String... names){
        for(String n:names){
            int id=getResources().getIdentifier(n,"id",getPackageName());
            if(id!=0){
                try{
                    Button b=findViewById(id);
                    if(b!=null) return b;
                }catch(Exception e){}
            }
        }
        return null;
    }

    // Layout এ Button এর ID ভুল থাকলেও Text দিয়ে খুঁজে বের করবে
    private Button findGoogleButtonByText(){
        try{
            android.view.ViewGroup root = (android.view.ViewGroup) findViewById(android.R.id.content);
            return searchButton(root);
        }catch(Exception e){return null;}
    }
    private Button searchButton(android.view.ViewGroup g){
        if(g==null) return null;
        for(int i=0;i<g.getChildCount();i++){
            android.view.View v=g.getChildAt(i);
            if(v instanceof Button){
                Button b=(Button)v;
                String txt=b.getText().toString().toLowerCase();
                if(txt.contains("google") || txt.contains("sign in")){
                    return b;
                }
            }else if(v instanceof android.view.ViewGroup){
                Button r=searchButton((android.view.ViewGroup)v);
                if(r!=null) return r;
            }
        }
        return null;
    }
}
