package com.sr.voicechat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {

    public interface OnRoomClick { void onClick(RoomModel room); }

    private List<RoomModel> list;
    private OnRoomClick listener;

    public RoomAdapter(List<RoomModel> list, OnRoomClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RoomModel room = list.get(position);
        holder.title.setText(room.roomName);
        holder.bio.setText(room.roomBio + " | ID: " + room.ownerId);
        holder.itemView.setOnClickListener(v -> listener.onClick(room));
    }

    @Override public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, bio;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            bio = itemView.findViewById(android.R.id.text2);
        }
    }
}
