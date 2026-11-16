package com.sr.voicechat;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class GiftBoxDialog {

    public interface OnGiftPicked {
        void onPick(String giftName, long giftAmount, String giftEmoji);
    }

    public static void show(Context context, OnGiftPicked listener) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(createView(context, dialog, listener));
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(Gravity.BOTTOM);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    private static LinearLayout createView(Context ctx, Dialog dialog, OnGiftPicked listener) {
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#1A1A2E"));
        root.setPadding(24, 24, 24, 24);

        // TOP BAR - Title + Wallet Corner
        LinearLayout topBar = new LinearLayout(ctx);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(ctx);
        title.setText("🎁  Premium Gifts");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView txtWallet = new TextView(ctx);
        txtWallet.setText("🪙 Wallet: ...");
        txtWallet.setTextColor(Color.parseColor("#FFD700"));
        txtWallet.setTextSize(15);
        txtWallet.setTypeface(null, android.graphics.Typeface.BOLD);
        txtWallet.setGravity(Gravity.END);
        txtWallet.setPadding(12, 8, 12, 8);
        txtWallet.setBackgroundColor(Color.parseColor("#2A2A3E"));

        topBar.addView(title);
        topBar.addView(txtWallet);
        root.addView(topBar);

        // Wallet Load - Real Coin from Database
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseFirestore.getInstance().collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        long coins = 0L;
                        if (doc != null && doc.exists() && doc.getLong("walletCoins") != null) {
                            coins = doc.getLong("walletCoins");
                        }
                        txtWallet.setText("🪙 " + coins + " Coins");
                    })
                    .addOnFailureListener(e -> txtWallet.setText("🪙 0 Coins"));
            } else {
                txtWallet.setText("🪙 0 Coins");
            }
        } catch (Exception e) {
            txtWallet.setText("🪙 0 Coins");
        }

        String[][] gifts = {
            {"Rose", "🌹", "100"},
            {"Heart", "❤️", "500"},
            {"Crown", "👑", "1000"},
            {"Diamond", "💎", "5000"},
            {"Car", "🚗", "10000"},
            {"Castle", "🏰", "50000"},
            {"Rocket", "🚀", "100000"},
            {"Dragon", "🐉", "200000"},
            {"Universe", "🌌", "500000"},
        };

        GridLayout grid = new GridLayout(ctx);
        grid.setColumnCount(3);
        grid.setUseDefaultMargins(true);
        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gridParams.topMargin = 20;
        grid.setLayoutParams(gridParams);

        for (String[] g : gifts) {
            LinearLayout item = new LinearLayout(ctx);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setBackgroundColor(Color.parseColor("#2A2A3E"));
            item.setPadding(16, 20, 16, 20);

            TextView emoji = new TextView(ctx);
            emoji.setText(g[1]);
            emoji.setTextSize(36);
            emoji.setGravity(Gravity.CENTER);

            TextView name = new TextView(ctx);
            name.setText(g[0]);
            name.setTextColor(Color.WHITE);
            name.setTextSize(14);
            name.setTypeface(null, android.graphics.Typeface.BOLD);
            name.setGravity(Gravity.CENTER);

            TextView amount = new TextView(ctx);
            amount.setText(g[2] + " Coins");
            amount.setTextColor(Color.parseColor("#FFD700"));
            amount.setTextSize(12);
            amount.setGravity(Gravity.CENTER);

            item.addView(emoji);
            item.addView(name);
            item.addView(amount);

            item.setOnClickListener(v -> {
                dialog.dismiss();
                if (listener != null) listener.onPick(g[0], Long.parseLong(g[2]), g[1]);
            });

            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = 0;
            p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            item.setLayoutParams(p);
            grid.addView(item);
        }

        root.addView(grid);

        Button close = new Button(ctx);
        close.setText("Close");
        close.setTextColor(Color.WHITE);
        close.setBackgroundColor(Color.parseColor("#444444"));
        close.setOnClickListener(v -> dialog.dismiss());
        root.addView(close);

        return root;
    }
}
