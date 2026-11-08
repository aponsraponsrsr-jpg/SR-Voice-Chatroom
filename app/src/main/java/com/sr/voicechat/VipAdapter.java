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
        holder.textVipName.setText(model.getVipName());
        holder.textVipLevel.setText(model.getVipLevel());
        holder.textEffect.setText("Effect: " + model.getEntryEffectName());

        // Click listener to open premium VIP details dialog
        holder.itemView.setOnClickListener(v -> showVipDetailsDialog(model));
    }

    private void showVipDetailsDialog(VipModel model) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_vip_details);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView textDetailTitle = dialog.findViewById(R.id.textDetailTitle);
        TextView textDetailDesc = dialog.findViewById(R.id.textDetailDesc);
        Button btnBuyVip = dialog.findViewById(R.id.btnBuyVip);
        Button btnCloseDetail = dialog.findViewById(R.id.btnCloseDetail);

        textDetailTitle.setText("👑 " + model.getVipName() + " Privileges");
        textDetailDesc.setText("• Level: " + model.getVipName() + "\n• Exclusive Profile Frame & Chat Bubble\n• Entry Effect: " + model.getEntryEffectName() + "\n• Validity: 7 Days / 15 Days / 30 Days (Admin Controlled)");

        // Set coin pricing text based on VIP level
        String coinText = "Buy - 2,000,000 Coins (Coming Soon)";
        if (model.getVipName().contains("VIP 2")) coinText = "Buy - 5,000,000 Coins (Coming Soon)";
        else if (model.getVipName().contains("VIP 3")) coinText = "Buy - 8,000,000 Coins (Coming Soon)";
        else if (model.getVipName().contains("VIP 4")) coinText = "Buy - 15,000,000 Coins (Coming Soon)";
        else if (model.getVipName().contains("VIP 5")) coinText = "Buy - 20,000,000 Coins (Coming Soon)";
        else if (model.getVipName().contains("VIP 6")) coinText = "Buy - 30,000,000 Coins (Coming Soon)";
        else if (model.getVipName().contains("VIP 7")) coinText = "Buy - 50,000,000 Coins (Coming Soon)";

        btnBuyVip.setText(coinText);
        btnBuyVip.setEnabled(false); // Closed for purchase as requested

        if (btnCloseDetail != null) {
            btnCloseDetail.setOnClickListener(v -> dialog.dismiss());
        }
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return vipList.size();
    }

    public static class VipViewHolder extends RecyclerView.ViewHolder {
        TextView textVipName, textVipLevel, textEffect;

        public VipViewHolder(@NonNull View itemView) {
            super(itemView);
            textVipName = itemView.findViewById(R.id.textVipName);
            textVipLevel = itemView.findViewById(R.id.textVipLevel);
            textEffect = itemView.findViewById(R.id.textEffect);
        }
    }
}
