package com.sr.voicechat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * PART 3 - VipEntryView.java
 * VIP Entry Effect - Room e dhukle Full Screen Light + Crown + Name
 *
 * USE in VoiceRoomActivity:
 * VipEntryView entry = findViewById(R.id.vipEntryView);
 * entry.showEntry("Rakib", 7);
 */
public class VipEntryView extends View {

    private int vipLevel = 7;
    private String userName = "";
    private boolean isShowing = false;
    private float progress = 0f;
    private float glowAlpha = 0f;
    private OnEntryFinishListener listener;

    public interface OnEntryFinishListener {
        void onFinish();
    }

    public VipEntryView(Context context) {
        super(context);
        init();
    }

    public VipEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setVisibility(GONE);
        setClickable(false);
    }

    public void showEntry(String name, int level) {
        this.userName = name == null ? "" : name;
        if (level < 1) level = 1;
        if (level > 8) level = 8;
        this.vipLevel = level;
        this.isShowing = true;
        setVisibility(VISIBLE);

        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(2800);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            // Glow - shuru te beshi, majhe kom, sheshe abar
            if (progress < 0.2f) glowAlpha = progress * 5f;
            else if (progress < 0.8f) glowAlpha = 1f;
            else glowAlpha = 1f - (progress - 0.8f) * 5f;
            invalidate();
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isShowing = false;
                setVisibility(GONE);
                if (listener != null) listener.onFinish();
            }
        });
        anim.start();
    }

    public void setOnEntryFinishListener(OnEntryFinishListener l) {
        this.listener = l;
    }

    private int[] getColors() {
        switch (vipLevel) {
            case 1: return new int[]{Color.parseColor("#4FC3F7"), Color.parseColor("#1565C0")};
            case 2: return new int[]{Color.parseColor("#26C6DA"), Color.parseColor("#00838F")};
            case 3: return new int[]{Color.parseColor("#66BB6A"), Color.parseColor("#2E7D32")};
            case 4: return new int[]{Color.parseColor("#AB47BC"), Color.parseColor("#6A1B9A")};
            case 5: return new int[]{Color.parseColor("#FFA726"), Color.parseColor("#EF6C00")};
            case 6: return new int[]{Color.parseColor("#FF7043"), Color.parseColor("#D84315")};
            case 7: return new int[]{Color.parseColor("#FFD700"), Color.parseColor("#FF1744")};
            case 8: return new int[]{Color.parseColor("#FFD700"), Color.parseColor("#D50000")};
            default: return new int[]{Color.parseColor("#FFD700"), Color.parseColor("#FF1744")};
        }
    }

    private String getVipTitle() {
        switch (vipLevel) {
            case 1: return "VIP 1  •  BLUE EAGLE";
            case 2: return "VIP 2  •  CYAN STAR";
            case 3: return "VIP 3  •  EMERALD KING";
            case 4: return "VIP 4  •  PURPLE ROYAL";
            case 5: return "VIP 5  •  GOLDEN LION";
            case 6: return "VIP 6  •  FIRE KING";
            case 7: return "VIP 7  •  RED GOLD LION";
            case 8: return "VIP 8  •  PHOENIX KING";
            default: return "VIP " + vipLevel;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isShowing) return;
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        int[] colors = getColors();
        float centerY = h * 0.42f;
        float scale = 0.6f + progress * 0.4f;
        if (progress > 0.85f) scale = 1f - (progress - 0.85f) * 0.5f;

        // 1. BACKGROUND - Halka Dark + Gold Gradient - nicher dike
        Paint bgPaint = new Paint();
        bgPaint.setShader(new LinearGradient(0, 0, 0, h,
                new int[]{Color.TRANSPARENT, Color.parseColor("#33000000"), Color.parseColor("#66000000")},
                new float[]{0f, 0.6f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bgPaint);
        bgPaint.setShader(null);

        // 2. CENTER GLOW CIRCLE
        Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setColor(colors[0]);
        glowPaint.setAlpha((int) (glowAlpha * 70));
        canvas.drawCircle(w / 2f, centerY, 170f * scale, glowPaint);
        glowPaint.setAlpha((int) (glowAlpha * 110));
        canvas.drawCircle(w / 2f, centerY, 120f * scale, glowPaint);

        // 3. OUTER RING - Ghurbe
        Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(3f);
        ringPaint.setColor(colors[0]);
        ringPaint.setAlpha((int) (glowAlpha * 255));
        canvas.drawCircle(w / 2f, centerY, 105f * scale, ringPaint);
        ringPaint.setStrokeWidth(1.5f);
        ringPaint.setColor(Color.WHITE);
        ringPaint.setAlpha((int) (glowAlpha * 150));
        canvas.drawCircle(w / 2f, centerY, 92f * scale, ringPaint);

        // 4. CROWN - Simple Premium Crown Shape
        Paint crownPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        crownPaint.setColor(Color.parseColor("#FFD700"));
        crownPaint.setStyle(Paint.Style.FILL);
        float cw = 90f * scale;
        float ch = 48f * scale;
        float cx = w / 2f;
        float cy = centerY - 10f;
        // Crown base
        canvas.drawRect(cx - cw / 2f, cy + ch / 2f - 8f * scale, cx + cw / 2f, cy + ch / 2f + 10f * scale, crownPaint);
        // Crown spikes - 3 ta
        float[] pts = new float[]{
                cx - cw / 2f, cy + ch / 2f,
                cx - cw / 2f + 10f * scale, cy - ch / 2f,
                cx - cw / 4f, cy + ch / 4f,
                cx - cw / 4f, cy + ch / 4f,
                cx, cy - ch / 2f - 14f * scale,
                cx + cw / 4f, cy + ch / 4f,
                cx + cw / 4f, cy + ch / 4f,
                cx + cw / 2f - 10f * scale, cy - ch / 2f,
                cx + cw / 2f, cy + ch / 2f
        };
        // Draw as triangles
        Paint triPaint = crownPaint;
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(cx - cw / 2f, cy + ch / 2f);
        path.lineTo(cx - cw / 2f + 8f * scale, cy - ch / 2f);
        path.lineTo(cx - cw / 4f, cy + ch / 4f);
        path.lineTo(cx, cy - ch / 2f - 14f * scale);
        path.lineTo(cx + cw / 4f, cy + ch / 4f);
        path.lineTo(cx + cw / 2f - 8f * scale, cy - ch / 2f);
        path.lineTo(cx + cw / 2f, cy + ch / 2f);
        path.close();
        canvas.drawPath(path, triPaint);

        // Crown Gem - Red
        Paint gemPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gemPaint.setColor(colors[1]);
        canvas.drawCircle(cx, cy + ch / 4f, 9f * scale, gemPaint);
        gemPaint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy + ch / 4f, 3.5f * scale, gemPaint);

        // 5. NAME TEXT - Boro kore
        Paint namePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        namePaint.setColor(Color.WHITE);
        namePaint.setTextSize(44f * scale);
        namePaint.setFakeBoldText(true);
        namePaint.setTextAlign(Paint.Align.CENTER);
        namePaint.setShadowLayer(12f, 0, 0, colors[1]);
        canvas.drawText(userName, w / 2f, centerY + 175f * scale, namePaint);

        // 6. VIP TITLE - Gold
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(colors[0]);
        titlePaint.setTextSize(26f * scale);
        titlePaint.setFakeBoldText(true);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setShadowLayer(8f, 0, 0, Color.BLACK);
        canvas.drawText(getVipTitle(), w / 2f, centerY + 215f * scale, titlePaint);

        // 7. BOTTOM LINE - Gold
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(colors[0]);
        linePaint.setAlpha((int) (glowAlpha * 255));
        linePaint.setStrokeWidth(3f);
        float lineW = 200f * progress;
        canvas.drawLine(w / 2f - lineW / 2f, centerY + 240f * scale, w / 2f + lineW / 2f, centerY + 240f * scale, linePaint);

        // 8. SPARKLES - Charpashe
        Paint sparkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sparkPaint.setColor(Color.WHITE);
        sparkPaint.setAlpha((int) (glowAlpha * 200));
        for (int i = 0; i < 10; i++) {
            double a = (Math.PI * 2 * i) / 10 + progress * 1.2f;
            float sx = (float) (w / 2f + Math.cos(a) * 150f * scale);
            float sy = (float) (centerY + Math.sin(a) * 150f * scale);
            canvas.drawCircle(sx, sy, 2.5f, sparkPaint);
        }
    }
}
