package com.sr.voicechat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomVH> {

    public interface OnRoomClick {
        void onRoomClick(RoomModel room);
    }

    private List<RoomModel> list;
    private OnRoomClick listener;

    public RoomAdapter(List<RoomModel> list, OnRoomClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomVH holder, int position) {
        RoomModel room = list.get(position);
        holder.txtName.setText(room.getRoomName());
        holder.txtCount.setText(room.getOnlineCount() + " Online");
        
        // Star List Top User
        if (room.getTopUserName() != null && !room.getTopUserName().isEmpty()) {
            holder.txtTop.setText("⭐ " + room.getTopUserName());
            holder.txtTop.setVisibility(View.VISIBLE);
        } else {
            holder.txtTop.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onRoomClick(room));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class RoomVH extends RecyclerView.ViewHolder {
        TextView txtName, txtCount, txtTop;
        public RoomVH(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtRoomNameItem);
            txtCount = itemView.findViewById(R.id.txtOnlineCount);
            txtTop = itemView.findViewById(R.id.txtTopUser);
        }
    }
}
