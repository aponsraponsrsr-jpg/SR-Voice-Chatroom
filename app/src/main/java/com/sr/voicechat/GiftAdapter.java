package com.sr.voicechat.adapters; // তোমার প্রজেক্টের সঠিক প্যাকেজ নাম এখানে দিয়ে দিও

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
    private OnGiftClickListener listener;

    public interface OnGiftClickListener {
        void onGiftClick(GiftModel gift);
    }

    public GiftAdapter(List<GiftModel> giftList, OnGiftClickListener listener) {
        this.giftList = giftList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gift_card, parent, false);
        return new GiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
        GiftModel gift = giftList.get(position);
        holder.tvGiftName.setText(gift.getGiftName());
        holder.tvGiftPrice.setText(gift.getGiftPrice());
        holder.ivGiftIcon.setImageResource(gift.getGiftIconResId());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGiftClick(gift);
            }
        });
    }

    @Override
    public int getItemCount() {
        return giftList.size();
    }

    public static class GiftViewHolder extends RecyclerView.ViewHolder {
        ImageView ivGiftIcon;
        TextView tvGiftName, tvGiftPrice;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGiftIcon = itemView.findViewById(R.id.ivGiftIcon);
            tvGiftName = itemView.findViewById(R.id.tvGiftName);
            tvGiftPrice = itemView.findViewById(R.id.tvGiftPrice);
        }
    }
}
