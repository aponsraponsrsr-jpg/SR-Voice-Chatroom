package com.sr.voicechat.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sr.voicechat.R;
import com.sr.voicechat.models.GiftModel;

import java.util.List;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.GiftViewHolder> {

    private List<GiftModel> giftList;
    private OnGiftSelectedListener listener;
    private int selectedPosition = 0;

    public interface OnGiftSelectedListener {
        void onGiftSelected(GiftModel gift);
    }

    public GiftAdapter(List<GiftModel> giftList, OnGiftSelectedListener listener) {
        this.giftList = giftList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gift_card, parent, false);
        return new GiftViewHolder(view);
    }

    // GiftModel এ Image 0 দেওয়া - তাই নাম দেখে Emoji বসাবো - Permanent Fix
    private String getEmojiForGift(String name) {
        if (name == null) return "🎁";
        String n = name.toLowerCase();
        if (n.contains("rose")) return "🌹";
        if (n.contains("angel")) return "👼";
        if (n.contains("rocket")) return "🚀";
        if (n.contains("porsche") || n.contains("car")) return "🏎️";
        if (n.contains("heart")) return "❤️";
        if (n.contains("cake")) return "🎂";
        if (n.contains("diamond")) return "💎";
        if (n.contains("crown")) return "👑";
        return "🎁";
    }

    @Override
    public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
        GiftModel gift = giftList.get(position);
        holder.txtGiftName.setText(gift.getGiftName());
        holder.txtGiftPrice.setText(gift.getGiftPrice() + " Coins");

        // যদি আসল ছবি থাকে - ছবি দেখাও - না থাকলে Mic লুকিয়ে Emoji দেখাও
        if (gift.getGiftImageRes() != 0) {
            holder.imgGiftIcon.setVisibility(View.VISIBLE);
            holder.imgGiftIcon.setImageResource(gift.getGiftImageRes());
            holder.txtGiftEmoji.setVisibility(View.GONE);
        } else {
            holder.imgGiftIcon.setVisibility(View.GONE);
            holder.txtGiftEmoji.setVisibility(View.VISIBLE);
            holder.txtGiftEmoji.setText(getEmojiForGift(gift.getGiftName()));
        }

        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#44FFD700"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#2A1B4E"));
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            
            if (previousPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousPosition);
            }
            if (selectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(selectedPosition);
            }

            if (listener != null) {
                listener.onGiftSelected(gift);
            }
        });
    }

    @Override
    public int getItemCount() {
        return giftList.size();
    }

    public static class GiftViewHolder extends RecyclerView.ViewHolder {
        ImageView imgGiftIcon;
        TextView txtGiftName, txtGiftPrice, txtGiftEmoji;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            imgGiftIcon = itemView.findViewById(R.id.imgGiftIcon);
            txtGiftName = itemView.findViewById(R.id.txtGiftName);
            txtGiftPrice = itemView.findViewById(R.id.txtGiftPrice);
            txtGiftEmoji = itemView.findViewById(R.id.txtGiftEmoji);
        }
    }
}
