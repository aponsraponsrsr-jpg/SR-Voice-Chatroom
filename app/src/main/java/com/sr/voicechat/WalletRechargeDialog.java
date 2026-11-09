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
            String userId = mAuth.getCurrentUser().getUid();
            
            // গ্লোবাল এবং সিকিউর ফায়ারবেস পাথ বাইন্ডিং
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            
            coinEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && isAdded()) {
                        Long coins = snapshot.child("sendingCoins").getValue(Long.class);
                        if (coins != null) {
                            tvCoinBalance.setText(String.format("%,d", coins));
                        } else {
                            tvCoinBalance.setText("0");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // প্রফেশনাল এরর হ্যান্ডেলিং
                }
            };
            userRef.addValueEventListener(coinEventListener);
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
