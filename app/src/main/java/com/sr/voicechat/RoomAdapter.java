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
    public RoomAdapter(List<RoomModel> list){ this.list = list; }
    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int v){ return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_room, p, false)); }
    @Override public void onBindViewHolder(@NonNull ViewHolder h, int p){
        RoomModel r = list.get(p);
        h.tvName.setText(r.name);
        h.tvPeople.setText(r.people);
        h.card.setBackgroundColor(r.color);
    }
    @Override public int getItemCount(){ return list.size(); }
    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvName, tvPeople; View card;
        public ViewHolder(@NonNull View v){ super(v); tvName=v.findViewById(R.id.tvRoomName); tvPeople=v.findViewById(R.id.tvRoomPeople); card=v.findViewById(R.id.cardBg); }
    }
}
