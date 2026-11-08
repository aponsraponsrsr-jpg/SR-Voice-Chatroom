package com.sr.voicechat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VipAdapter extends RecyclerView.Adapter<VipAdapter.VipViewHolder> {

    private Context context;
    private List<VipModel> vipList;

    public VipAdapter(Context context, List<VipModel> vipList) {
        this.context = context;
        this.vipList = vipList;
    }

    @NonNull
    @Override
    public VipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vip_row, parent, false);
        return new VipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VipViewHolder holder, int position) {
        VipModel model = vipList.get(position);
        holder.textVipName.setText(model.getVipName());
        holder.textVipLevel.setText(model.getVipLevel());
        holder.textEffect.setText("Effect: " + model.getEntryEffectName());
    }

    @Override
    public int getItemCount() {
        return vipList.size();
    }

    public static class VipViewHolder extends RecyclerView.ViewHolder {
        TextView textVipName, textVipLevel, textEffect;
        ImageView imgVipBadge;

        public VipViewHolder(@NonNull View itemView) {
            super(itemView);
            textVipName = itemView.findViewById(R.id.textVipName);
            textVipLevel = itemView.findViewById(R.id.textVipLevel);
            textEffect = itemView.findViewById(R.id.textEffect);
            imgVipBadge = itemView.findViewById(R.id.imgVipBadge);
        }
    }
}
