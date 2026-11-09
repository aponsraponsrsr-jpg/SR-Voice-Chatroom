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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gift_grid, parent, false);
        return new GiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
        GiftModel gift = giftList.get(position);
        holder.txtGiftName.setText(gift.getGiftName());
        holder.txtGiftPrice.setText(gift.getGiftPrice() + " Coins");
        holder.imgGiftIcon.setImageResource(gift.getGiftImageRes());

        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#44FFD700"));
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#2A1B4E"));
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

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
        TextView txtGiftName, txtGiftPrice;

        public GiftViewHolder(@NonNull View itemView) {
            super(itemView);
            imgGiftIcon = itemView.findViewById(R.id.imgGiftIcon);
            txtGiftName = itemView.findViewById(R.id.txtGiftName);
            txtGiftPrice = itemView.findViewById(R.id.txtGiftPrice);
        }
    }
}
