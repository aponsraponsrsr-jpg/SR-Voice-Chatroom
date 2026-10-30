package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseFirestore db;
    EditText etEmail, etPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        if(auth.getCurrentUser()!=null){
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        
        // Google Button - Safe check
        try{
            findViewById(R.id.btnGoogle).setOnClickListener(v -> {
                Toast.makeText(this,"Google Login Coming Soon",Toast.LENGTH_SHORT).show();
            });
        }catch(Exception e){}

        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> register());
    }

    void login(){
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        if(email.isEmpty()||pass.isEmpty()){Toast.makeText(this,"Email Pass Likho",Toast.LENGTH_SHORT).show();return;}
        auth.signInWithEmailAndPassword(email,pass).addOnSuccessListener(r->{
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }).addOnFailureListener(e->Toast.makeText(this,"Login Failed: "+e.getMessage(),Toast.LENGTH_SHORT).show());
    }

    void register(){
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        if(email.isEmpty()||pass.isEmpty()){Toast.makeText(this,"Email Pass Likho",Toast.LENGTH_SHORT).show();return;}
        if(pass.length()<6){Toast.makeText(this,"Password 6+ hote hobe",Toast.LENGTH_SHORT).show();return;}
        
        auth.createUserWithEmailAndPassword(email,pass).addOnSuccessListener(r->{
            FirebaseUser user = r.getUser();
            if(user==null) return;
            String srId = "1005" + (1000 + new Random().nextInt(9000));
            Map<String,Object> data = new HashMap<>();
            data.put("email", email);
            data.put("sr_id", srId);
            data.put("room_id", srId);
            data.put("coins", 1000);
            data.put("created_at", FieldValue.serverTimestamp());
            db.collection("users").document(user.getUid()).set(data).addOnSuccessListener(a->{
                Toast.makeText(this,"Registered! Your ID: "+srId,Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            });
        }).addOnFailureListener(e->Toast.makeText(this,"Register Failed: "+e.getMessage(),Toast.LENGTH_SHORT).show());
    }
}
