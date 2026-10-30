package com.sr.voicechat;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;
import java.util.*;

public class LoginActivity extends AppCompatActivity {
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if(mAuth.getCurrentUser()!=null){
            checkAndGo(mAuth.getCurrentUser().getUid());
            return;
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.btnGoogle).setOnClickListener(v -> {
            startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        checkAndGo(mAuth.getCurrentUser().getUid());
                    }
                });
            } catch (Exception e) {}
        }
    }

    void checkAndGo(String uid){
        // Check if user already has permanent ID
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if(doc.exists() && doc.contains("sr_id")){
                // Already has permanent ID - Go to Home
                goToHome();
            } else {
                // FIRST TIME - Generate 7-8 Digit Permanent ID
                generatePermanentId(uid);
            }
        });
    }

    void generatePermanentId(String uid){
        // 7-8 Digit Mixed ID e.g. 10052384
        Random rand = new Random();
        int myId = 1000000 + rand.nextInt(90000000); // 7 to 8 digit
        String srId = String.valueOf(myId);

        // Save Permanent - Never changes
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("sr_id", srId); // Permanent ID = Room ID
        user.put("room_id", srId);
        user.put("created_at", FieldValue.serverTimestamp());

        db.collection("users").document(uid).set(user).addOnSuccessListener(a -> {
            // Also create room index for search
            Map<String, Object> room = new HashMap<>();
            room.put("room_id", srId);
            room.put("owner_uid", uid);
            room.put("active", true);
            db.collection("rooms").document(srId).set(room);
            goToHome();
        });
    }

    void goToHome(){
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }
}
