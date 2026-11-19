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
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sr.voicechat.models.GiftModel;
import java.util.List;

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

        // TOP BAR
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

        // Wallet Load
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

        // SELECTED TEXT
        TextView txtSelected = new TextView(ctx);
        txtSelected.setText("Select a gift to send");
        txtSelected.setTextColor(Color.parseColor("#FFD700"));
        txtSelected.setTextSize(14);
        txtSelected.setGravity(Gravity.CENTER);
        txtSelected.setPadding(0, 20, 0, 0);
        root.addView(txtSelected);

        // 4 PREMIUM GIFTS ONLY - From GiftModel
        List<GiftModel> gifts = GiftModel.createPremiumList();
        // Emoji Map - Gift Name -> Emoji
        java.util.HashMap<String, String> emojiMap = new java.util.HashMap<>();
        emojiMap.put("Rose", "🌹");
        emojiMap.put("Angel", "👼");
        emojiMap.put("Rocket", "🚀");
        emojiMap.put("Porsche Car", "🏎️");

        GridLayout grid = new GridLayout(ctx);
        grid.setColumnCount(2);
        grid.setUseDefaultMargins(true);
        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gridParams.topMargin = 20;
        grid.setLayoutParams(gridParams);

        final GiftModel[] selected = new GiftModel[1];
        final LinearLayout[] itemViews = new LinearLayout[gifts.size()];

        for (int i = 0; i < gifts.size(); i++) {
            GiftModel g = gifts.get(i);
            String emojiStr = emojiMap.containsKey(g.getGiftName()) ? emojiMap.get(g.getGiftName()) : "🎁";

            LinearLayout item = new LinearLayout(ctx);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setBackgroundColor(Color.parseColor("#2A2A3E"));
            item.setPadding(16, 20, 16, 20);

            TextView emoji = new TextView(ctx);
            emoji.setText(emojiStr);
            emoji.setTextSize(42);
            emoji.setGravity(Gravity.CENTER);

            TextView name = new TextView(ctx);
            name.setText(g.getGiftName());
            name.setTextColor(Color.WHITE);
            name.setTextSize(16);
            name.setTypeface(null, android.graphics.Typeface.BOLD);
            name.setGravity(Gravity.CENTER);

            TextView premium = new TextView(ctx);
            premium.setText("✨ PREMIUM");
            premium.setTextColor(Color.parseColor("#FF4081"));
            premium.setTextSize(11);
            premium.setTypeface(null, android.graphics.Typeface.BOLD);
            premium.setGravity(Gravity.CENTER);

            TextView amount = new TextView(ctx);
            amount.setText(g.getGiftPrice() + " Coins");
            amount.setTextColor(Color.parseColor("#FFD700"));
            amount.setTextSize(13);
            amount.setGravity(Gravity.CENTER);

            item.addView(emoji);
            item.addView(name);
            item.addView(premium);
            item.addView(amount);

            final int index = i;
            item.setOnClickListener(v -> {
                selected[0] = g;
                txtSelected.setText("Selected: " + g.getGiftName() + " - " + g.getGiftPrice() + " Coins");
                // Highlight - Selected = Gold Border, Others = Normal
                for (int j = 0; j < itemViews.length; j++) {
                    if (itemViews[j] != null) {
                        itemViews[j].setBackgroundColor(Color.parseColor(j == index ? "#FFD700" : "#2A2A3E"));
                    }
                }
            });

            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = 0;
            p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            item.setLayoutParams(p);
            itemViews[i] = item;
            grid.addView(item);
        }

        root.addView(grid);

        // SEND BUTTON
        Button btnSend = new Button(ctx);
        btnSend.setText("🎁  SEND GIFT");
        btnSend.setTextColor(Color.WHITE);
        btnSend.setBackgroundColor(Color.parseColor("#FF4081"));
        btnSend.setTextSize(16);
        btnSend.setOnClickListener(v -> {
            if (selected[0] == null) {
                Toast.makeText(ctx, "Please select a gift first", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            String em = emojiMap.containsKey(selected[0].getGiftName()) ? emojiMap.get(selected[0].getGiftName()) : "🎁";
            long amt = 0;
            try { amt = Long.parseLong(selected[0].getGiftPrice()); } catch (Exception e) {}
            if (listener != null) listener.onPick(selected[0].getGiftName(), amt, em);
        });
        root.addView(btnSend);

        Button close = new Button(ctx);
        close.setText("Close");
        close.setTextColor(Color.WHITE);
        close.setBackgroundColor(Color.parseColor("#444444"));
        close.setOnClickListener(v -> dialog.dismiss());
        root.addView(close);

        return root;
    }
}
