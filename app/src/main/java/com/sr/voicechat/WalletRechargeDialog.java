package com.sr.voicechat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WalletRechargeDialog extends BottomSheetDialogFragment {

    private TextView tvCoinBalance;
    private DatabaseReference userRef;
    private ValueEventListener coinEventListener;
    private SharedPreferences prefs;
    private String currentUserId = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_wallet_recharge, container, false);

        tvCoinBalance = view.findViewById(R.id.tvCoinBalance);

        // কাস্টম ইউজার আইডি ফেচ করা (যাতে অ্যাডমিন প্যানেলের পাঠানো কয়েনের সাথে পাথ হুবহু মিলে যায়)
        prefs = requireActivity().getSharedPreferences("SR_USER", Context.MODE_PRIVATE);
        currentUserId = prefs.getString("MY_9_DIGIT_ID", "493859069");

        if (currentUserId != null && !currentUserId.isEmpty()) {
            // সঠিক কাস্টম ইউজার আইডি পাথ বাইন্ডিং
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
            
            coinEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && isAdded()) {
                        // অ্যাডমিন প্যানেল থেকে পাঠানো কয়েন সরাসরি রিড করা (লেভেলের থেকে সম্পূর্ণ আলাদা)
                        Long coins = snapshot.child("sendingCoins").getValue(Long.class);
                        if (coins == null) {
                            coins = snapshot.child("coins").getValue(Long.class);
                        }
                        
                        if (coins != null) {
                            tvCoinBalance.setText(String.format("%,d", coins));
                        } else {
                            tvCoinBalance.setText("0");
                        }
                    } else if (isAdded()) {
                        tvCoinBalance.setText("0");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // প্রফেশনাল এরর হ্যান্ডেলিং
                }
            };
            userRef.addValueEventListener(coinEventListener);
        }

        // অনলাইন রিচার্জ বা কার্ড পেমেন্ট অপশনগুলোর ক্লিক লিসেনার (যদি লেআউটে থাকে)
        View btnOnlineRecharge = view.findViewById(R.id.btnOnlineRecharge); // লেআউটে আইডি থাকলে কাজ করবে
        if (btnOnlineRecharge != null) {
            btnOnlineRecharge.setOnClickListener(v -> {
                Toast.makeText(getContext(), "💳 Online Recharge Gateway Opening...", Toast.LENGTH_SHORT).show();
            });
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userRef != null && coinEventListener != null) {
            userRef.removeEventListener(coinEventListener);
        }
    }
}
