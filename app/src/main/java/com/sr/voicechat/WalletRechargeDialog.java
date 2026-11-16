package com.sr.voicechat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.widget.TextView;

public class WalletRechargeDialog extends BottomSheetDialogFragment {

    private TextView tvCoinBalance;
    private View btnOnlineRecharge;
    private DatabaseReference userRef;
    private ValueEventListener coinEventListener;
    private SharedPreferences prefs;
    private String currentUserId = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_wallet_recharge, container, false);

        tvCoinBalance = view.findViewById(R.id.tvCoinBalance);
        btnOnlineRecharge = view.findViewById(R.id.btnOnlineRecharge);

        prefs = requireActivity().getSharedPreferences("SR_USER", Context.MODE_PRIVATE);

        // PERMANENT FIX: Kokhono karor ID default hishebe bosano jabe na.
        // Age Firebase UID diye khujbo, na pele 9-digit ID diye khujbo.
        // Kichu na pele coins 0 dekhabe, onno karor coins dekhabe na.
        String firebaseUid = "";
        FirebaseUser fu = FirebaseAuth.getInstance().getCurrentUser();
        if (fu != null) firebaseUid = fu.getUid();
        if (firebaseUid == null || firebaseUid.isEmpty()) {
            firebaseUid = prefs.getString("FIREBASE_UID", "");
        }

        String nineDigitId = prefs.getString("MY_9_DIGIT_ID", "");
        if (nineDigitId == null) nineDigitId = "";

        // Primary key = Firebase UID. Eita sobar jonno auto kaj korbe.
        // Je keu login korlei tar nijer wallet dekhbe.
        if (firebaseUid != null && !firebaseUid.isEmpty()) {
            currentUserId = firebaseUid;
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        } else if (!nineDigitId.isEmpty()) {
            currentUserId = nineDigitId;
            userRef = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        } else {
            currentUserId = "";
            userRef = null;
        }

        if (userRef != null) {
            coinEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!isAdded()) return;
                    if (snapshot.exists()) {
                        Long coins = null;
                        // 1st: sendingCoins, 2nd: coins, 3rd: walletCoins - 3 ta check korbo jate purano user der o thik dekhay
                        if (snapshot.child("sendingCoins").getValue() != null) {
                            try { coins = snapshot.child("sendingCoins").getValue(Long.class); } catch (Exception ignored) {}
                        }
                        if (coins == null && snapshot.child("coins").getValue() != null) {
                            try { coins = snapshot.child("coins").getValue(Long.class); } catch (Exception ignored) {}
                        }
                        if (coins == null && snapshot.child("walletCoins").getValue() != null) {
                            try { coins = snapshot.child("walletCoins").getValue(Long.class); } catch (Exception ignored) {}
                        }
                        if (coins != null) {
                            tvCoinBalance.setText(String.format("%,d", coins));
                        } else {
                            tvCoinBalance.setText("0");
                        }
                    } else {
                        tvCoinBalance.setText("0");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (isAdded() && tvCoinBalance != null) tvCoinBalance.setText("0");
                }
            };
            userRef.addValueEventListener(coinEventListener);
        } else {
            if (tvCoinBalance != null) tvCoinBalance.setText("0");
        }

        // PERMANENT LOCK: Online Recharge OFF - Ekhon ar package dialog khulbe na
        // Keu joto bar chap dik, ektai message asbe. Reseller option ekhane nai.
        // Coins sudhu Super Admin / Reseller dite parbe.
        if (btnOnlineRecharge != null) {
            btnOnlineRecharge.setOnClickListener(v -> {
                showRechargeLockedDialog();
            });
        }

        return view;
    }

    private void showRechargeLockedDialog() {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
            .setTitle("🎁 Recharge")
            .setMessage("Online Recharge is OFF.\n\nCoins nite hole Super Admin / Reseller er kache jan.\n\nPrices:\n100000 = USD 0.99\n500000 = USD 4.99\n1000000 = USD 9.99\n5000000 = USD 49.99\n10000000 = USD 99.99\n\nNOTE: Kono online payment ekhan theke hobe na.")
            .setPositiveButton("OK", (d, w) -> {
                // Shudhu dialog bondho hobe, kono charge hobe na
            })
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userRef != null && coinEventListener != null) {
            userRef.removeEventListener(coinEventListener);
        }
    }
}
