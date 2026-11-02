package com.sr.voicechat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.Holder> {

    private List<RoomModel> list;
    private OnRoomClick listener;

    public interface OnRoomClick {
        void onRoomClick(RoomModel room);
    }

    public RoomAdapter(List<RoomModel> list, OnRoomClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        RoomModel r = list.get(position);
        h.name.setText(r.getRoomName());
        h.desc.setText("Host: " + r.getHostId() + " | " + r.getParticipantCount() + " people");
        h.itemView.setOnClickListener(view -> {
            if (listener != null) listener.onRoomClick(r);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        TextView name, desc;
        public Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txtRoomName);
            desc = itemView.findViewById(R.id.txtRoomDesc);
        }
    }
}
