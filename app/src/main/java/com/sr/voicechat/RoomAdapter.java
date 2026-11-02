package com.sr.voicechat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.ViewHolder> {
    List<RoomModel> list;
    OnRoomClick listener;
    public interface OnRoomClick { void onClick(RoomModel room); }
    public RoomAdapter(List<RoomModel> list, OnRoomClick listener) {
        this.list = list; this.listener = listener;
    }
    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_room, p, false);
        return new ViewHolder(v);
    }
    @Override public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        RoomModel r = list.get(i);
        h.name.setText(r.getName());
        h.desc.setText(r.getDesc());
        h.itemView.setOnClickListener(v -> listener.onClick(r));
    }
    @Override public int getItemCount() { return list.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, desc;
        public ViewHolder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.roomName);
            desc = v.findViewById(R.id.roomDesc);
        }
    }
}
