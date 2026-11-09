package com.sr.voicechat.adapters;

// ... other imports ...
import com.sr.voicechat.R;
import com.sr.voicechat.models.GiftModel;

public class GiftAdapter extends RecyclerView.Adapter<GiftAdapter.GiftViewHolder> {

    // ... variables ...
    private OnGiftSelectedListener listener;
    private int selectedPosition = 0; // ডিফল্টভাবে প্রথম গিফট সিলেক্ট থাকবে

    // ... interface OnGiftSelectedListener ...

    // ... Constructor ...

    @NonNull
    @Override
    public GiftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gift_card, parent, false);
        return new GiftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GiftViewHolder holder, int position) {
        GiftModel gift = giftList.get(position);
        holder.txtGiftName.setText(gift.getGiftName());
        holder.txtGiftPrice.setText(gift.getGiftPrice() + " Coins"); // গর্জিয়াস কয়েন টেক্সট
        holder.imgGiftIcon.setImageResource(gift.getGiftImageRes()); // গর্জিয়াস আইকন

        // সিলেক্টেড গিফটের হাইলাইট কালার (ভিডিওর মতো গর্জিয়াস নীল/গোল্ডেন)
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#44FFD700")); // গোল্ডেন হাইলাইট
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#2A1B4E")); // ডিফল্ট ব্যাকগ্রাউন্ড
        }

        // টাচ লজিক
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition); // আগেরটিকে ডি-সিলেক্ট করা
            notifyItemChanged(selectedPosition); // বর্তমানটিকে সিলেক্ট করা

            if (listener != null) {
                listener.onGiftSelected(gift);
            }
        });
    }

    // ... getItemCount & ViewHolder class ...
}
