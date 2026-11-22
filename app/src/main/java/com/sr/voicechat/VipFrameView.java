package com.sr.voicechat;

import android.animation.*;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

public class VipFrameView extends View {
    private int vipLevel = 8;
    private float pulse = 1f;
    private float rotate = 0f;
    private Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    public VipFrameView(Context c){ super(c); init(); }
    public VipFrameView(Context c, AttributeSet a){ super(c,a); init(); }
    private void init(){
        ValueAnimator a1 = ValueAnimator.ofFloat(1f, 1.08f);
        a1.setDuration(800); a1.setRepeatCount(ValueAnimator.INFINITE); a1.setRepeatMode(ValueAnimator.REVERSE);
        a1.addUpdateListener(v->{ pulse=(float)v.getAnimatedValue(); invalidate(); }); a1.start();
        ValueAnimator a2 = ValueAnimator.ofFloat(0f, 360f);
        a2.setDuration(4000); a2.setRepeatCount(ValueAnimator.INFINITE);
        a2.addUpdateListener(v->{ rotate=(float)v.getAnimatedValue(); invalidate(); }); a2.start();
    }
    public void setVipLevel(int l){ vipLevel=l; invalidate(); }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        int w=getWidth(), h=getHeight(); if(w==0) return;
        int cx=w/2, cy=h/2; float r=Math.min(w,h)/2f * pulse;
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(8f);
        p.setColor(vipLevel==8?Color.parseColor("#B388FF"):Color.parseColor("#FFD700"));
        p.setAlpha(180);
        canvas.drawCircle(cx, cy, r, p);
        p.setStrokeWidth(3f); p.setColor(Color.parseColor("#FFD700")); p.setAlpha(255);
        canvas.drawCircle(cx, cy, r-10, p);
        p.setStyle(Paint.Style.FILL); p.setColor(Color.WHITE);
        for(int i=0;i<8;i++){
            double ang = Math.toRadians(rotate + i*45);
            float x = cx + (float)(Math.cos(ang)*(r+2));
            float y = cy + (float)(Math.sin(ang)*(r+2));
            canvas.drawCircle(x, y, 5f, p);
        }
        p.setColor(Color.parseColor("#FFD700"));
        canvas.drawCircle(cx, cy - r - 4, 8f, p);
    }
}
