package com.sr.voicechat;

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
    private ValueEventListener coinEventListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_wallet_recharge, container, false);

        tvCoinBalance = view.findViewById(R.id.tvCoinBalance);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            String authUid = mAuth.getCurrentUser().getUid();
            
            // প্রফেশনাল পাথ বাইন্ডিং: প্রথমে মূল ইউজার নোড থেকে ডেটা ফেচ করা
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(authUid);
            
            coinEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && isAdded()) {
                        // অ্যাডমিন প্যানেল থেকে পাঠানো কয়েন বা ব্যালেন্স রিয়েল-টাইমে রিড করা
                        Long coins = snapshot.child("sendingCoins").getValue(Long.class);
                        if (coins == null) {
                            // যদি sendingCoins না থাকে, বিকল্প পাথ বা ব্যালেন্স চেক করা
                            coins = snapshot.child("coins").getValue(Long.class);
                        }
                        
                        if (coins != null) {
                            tvCoinBalance.setText(String.format("%,d", coins)); // কমা ফরম্যাটে সুন্দর দেখাবে
                        } else {
                            tvCoinBalance.setText("0");
                        }
                    } else if (isAdded()) {
                        tvCoinBalance.setText("0");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // প্রফেশনাল এরর লগ বা হ্যান্ডেলিং
                }
            };
            userRef.addValueEventListener(coinEventListener);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // মেমোরি লিক রোধ করতে লিসেনার রিমুভ করা হলো
        if (userRef != null && coinEventListener != null) {
            userRef.removeEventListener(coinEventListener);
        }
    }
}
