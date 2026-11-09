package com.sr.voicechat; // তোমার প্রজেক্টের সঠিক প্যাককেজ নেম এখানে দেবে

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WalletRechargeDialog extends BottomSheetDialogFragment {

    private TextView tvCoinBalance;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_wallet_recharge, container, false);

        tvCoinBalance = view.findViewById(R.id.tvCoinBalance); // ওয়ালেট লেআউটের কয়েন দেখানোর TextView আইডি
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid(); // অথবা তোমার অ্যাপের ব্যবহৃত ইউজার আইডি / ফোন / জিমেইল পাথ
            
            // ফায়ারবেস থেকে লাইভ কয়েন রিড করার গ্লোবাল পাথ
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // অ্যাডমিন প্যানেল থেকে পাঠানো 'sendingCoins' ডাটা ফেচ করা হচ্ছে
                        Long coins = snapshot.child("sendingCoins").getValue(Long.class);
                        if (coins != null) {
                            tvCoinBalance.setText(String.valueOf(coins));
                        } else {
                            tvCoinBalance.setText("0");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // কোনো এরর হলে হ্যান্ডেল করার জন্য
                }
            });
        }

        return view;
    }
}
