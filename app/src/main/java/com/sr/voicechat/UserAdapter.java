package com.sr.voicechat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    List<UserModel> list;
    public UserAdapter(List<UserModel> list) { this.list = list; }
    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        View v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_user, p, false);
        return new ViewHolder(v);
    }
    @Override public void onBindViewHolder(@NonNull ViewHolder h, int i) {
        UserModel u = list.get(i);
        h.name.setText(u.getName());
        if(u.isSpeaking()) h.name.setBackgroundResource(R.drawable.bg_speaking);
        else h.name.setBackgroundResource(R.drawable.bg_user);
    }
    @Override public int getItemCount() { return list.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        public ViewHolder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.userName);
        }
    }
}
