package com.sr.voicechat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
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

        // FIX - নতুন photoUrl থাকলে ওইটাই, না থাকলে পুরানো Base64
        if (h.avatar != null) {
            String photo = "";
            try { photo = u.getPhotoUrl(); } catch (Exception ignored) {}
            if (photo == null || photo.isEmpty()) photo = u.photoBase64 != null ? u.photoBase64 : "";
            if (photo != null && photo.startsWith("http")) {
                try { Glide.with(h.itemView.getContext()).load(photo).circleCrop().into(h.avatar); } catch (Exception ignored) {}
            } else if (photo != null && !photo.isEmpty()) {
                try {
                    byte[] bytes = Base64.decode(photo, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (bmp != null) h.avatar.setImageBitmap(bmp);
                    else h.avatar.setImageResource(android.R.drawable.ic_menu_camera);
                } catch (Exception e) { h.avatar.setImageResource(android.R.drawable.ic_menu_camera); }
            } else {
                h.avatar.setImageResource(android.R.drawable.ic_menu_camera);
            }
        }
    }
    @Override public int getItemCount() { return list.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView avatar;
        public ViewHolder(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.userName);
            // তোমার item_user.xml এ ImageView এর id userAvatar হতে হবে
            try { avatar = v.findViewById(R.id.userAvatar); } catch (Exception e) { avatar = null; }
        }
    }
}
