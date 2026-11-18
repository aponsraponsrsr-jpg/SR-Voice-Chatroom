package com.sr.voicechat;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        holder.textVipName.setText(model.getBadgeEmoji() + " " + model.getVipName() + " • " + model.getTigerType());
        holder.textVipLevel.setText(model.getCoinsText());
        holder.textEffect.setText(model.getTigerEmoji() + " " + model.getEntryEffectName() + " • " + model.getFrameName() + " • " + model.getBubbleName());
        holder.textTiger.setText(model.getTigerEmoji());
        try { holder.layoutBg.setBackgroundColor(Color.parseColor(model.getTopColor())); } catch (Exception e) {}
        holder.itemView.setOnClickListener(v -> showVipDetailsDialog(model));
    }

    private void showVipDetailsDialog(VipModel model) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_vip_details);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView textDetailTitle = dialog.findViewById(R.id.textDetailTitle);
        TextView textDetailDesc = dialog.findViewById(R.id.textDetailDesc);
        TextView textTigerPreview = dialog.findViewById(R.id.textTigerPreview);
        TextView textBadgePreview = dialog.findViewById(R.id.textBadgePreview);
        TextView textFramePreview = dialog.findViewById(R.id.textFramePreview);
        TextView textBubblePreview = dialog.findViewById(R.id.textBubblePreview);
        TextView textEffectPreview = dialog.findViewById(R.id.textEffectPreview);
        Button btnBuyVip = dialog.findViewById(R.id.btnBuyVip);
        Button btnCloseDetail = dialog.findViewById(R.id.btnCloseDetail);

        if (textDetailTitle != null) textDetailTitle.setText(model.getBadgeEmoji() + " " + model.getVipName() + " • " + model.getTigerType());
        if (textTigerPreview != null) textTigerPreview.setText(model.getTigerEmoji());
        if (textBadgePreview != null) textBadgePreview.setText(model.getBadgeEmoji());
        if (textFramePreview != null) textFramePreview.setText("🖼️");
        if (textBubblePreview != null) textBubblePreview.setText("💬");
        if (textEffectPreview != null) textEffectPreview.setText(model.getTigerEmoji());
        if (textDetailDesc != null) textDetailDesc.setText("• " + model.getTigerType() + " Entry Effect\n• " + model.getFrameName() + "\n• " + model.getBubbleName() + "\n• Golden Badge\n• Validity: " + model.getValidity() + " (Admin Controlled)");

        if (btnBuyVip != null) {
            btnBuyVip.setText(model.getBuyText());
            btnBuyVip.setEnabled(false); // Closed - Coming Soon
        }
        if (btnCloseDetail != null) btnCloseDetail.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override public int getItemCount() { return vipList.size(); }

    public static class VipViewHolder extends RecyclerView.ViewHolder {
        TextView textVipName, textVipLevel, textEffect, textTiger;
        View layoutBg;
        public VipViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutBg = itemView.findViewById(R.id.layoutVipRowBg);
            textVipName = itemView.findViewById(R.id.textVipName);
            textVipLevel = itemView.findViewById(R.id.textVipLevel);
            textEffect = itemView.findViewById(R.id.textEffect);
            textTiger = itemView.findViewById(R.id.textTiger);
        }
    }
}
