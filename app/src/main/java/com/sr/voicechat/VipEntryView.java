package com.sr.voicechat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.*;
import android.widget.*;
import android.graphics.Color;

public class VipEntryView extends FrameLayout {
    public VipEntryView(Context c){ super(c); }
    public VipEntryView(Context c, AttributeSet a){ super(c,a); }

    public static void show(FrameLayout root, String name, int vipLevel){
        Context ctx = root.getContext();
        FrameLayout overlay = new FrameLayout(ctx);
        overlay.setLayoutParams(new FrameLayout.LayoutParams(-1,-1));
        overlay.setBackgroundColor(Color.parseColor("#80000000"));

        String dragon = "💜";
        if(vipLevel==7) dragon="🐲";
        else if(vipLevel==6) dragon="👑";
        else if(vipLevel==5) dragon="🐉";
        else if(vipLevel==4) dragon="🔥";
        else if(vipLevel==3) dragon="🦅";

        TextView dragonView = new TextView(ctx);
        dragonView.setText(dragon);
        dragonView.setTextSize(120);
        dragonView.setGravity(android.view.Gravity.CENTER);
        FrameLayout.LayoutParams dp = new FrameLayout.LayoutParams(-1,-2);
        dp.topMargin = 250;
        dragonView.setLayoutParams(dp);
        overlay.addView(dragonView);

        AnimationSet set = new AnimationSet(true);
        TranslateAnimation fly = new TranslateAnimation(-800, 800, 0, -100);
        fly.setDuration(2800);
        ScaleAnimation scale = new ScaleAnimation(0.5f, 1.4f, 0.5f, 1.4f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(2800);
        set.addAnimation(fly); set.addAnimation(scale);
        dragonView.startAnimation(set);

        TextView banner = new TextView(ctx);
        banner.setText("💜 VIP " + vipLevel + "  " + name);
        banner.setTextSize(22);
        banner.setTextColor(Color.parseColor("#FFD700"));
        banner.setGravity(android.view.Gravity.CENTER);
        banner.setBackgroundColor(Color.parseColor("#AA2E1A47"));
        banner.setPadding(20, 20, 20, 20);
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(-1,-2);
        bp.topMargin = 650;
        banner.setLayoutParams(bp);
        overlay.addView(banner);

        root.addView(overlay);
        overlay.postDelayed(() -> root.removeView(overlay), 3200);
    }
}
