package com.sr.voicechat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class RoomGridAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<RoomModel> roomList;
    private LayoutInflater inflater;

    public RoomGridAdapter(Context context, ArrayList<RoomModel> roomList) {
        this.context = context;
        this.roomList = roomList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return roomList.size();
    }

    @Override
    public Object getItem(int position) {
        return roomList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_room_card, parent, false);
        }

        ImageView roomImage = convertView.findViewById(R.id.cardRoomImage);
        TextView roomName = convertView.findViewById(R.id.cardRoomName);
        TextView roomBio = convertView.findViewById(R.id.cardRoomBio);

        RoomModel room = roomList.get(position);

        roomName.setText(room.getRoomName() != null ? room.getRoomName() : "SR Voice Room");
        roomBio.setText(room.getRoomBio() != null ? room.getRoomBio() : "Welcome!");

        String imgBase64 = room.getRoomOwnerImage();
        if (imgBase64 != null && !imgBase64.isEmpty()) {
            try {
                byte[] decodedString = android.util.Base64.decode(imgBase64, android.util.Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (decodedByte != null) {
                    roomImage.setImageBitmap(decodedByte);
                }
            } catch (Exception e) {
                roomImage.setImageResource(android.R.drawable.ic_menu_my_calendar);
            }
        } else {
            roomImage.setImageResource(android.R.drawable.ic_menu_my_calendar);
        }

        return convertView;
    }
}
