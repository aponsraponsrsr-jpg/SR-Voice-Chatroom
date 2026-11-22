package com.sr.voicechat;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * VipEmblemView - PART 1
 * Waki Style Premium Emblem - Emoji NA - Code diye Crown + Glow + Shine
 * VIP 1-8 - Joto VIP Barbe - Toto Glow / Crown Boro Hobe
 * Usage XML: <com.sr.voicechat.VipEmblemView android:id="@+id/vipEmblem" ... />
 * Usage Java: emblem.setVipLevel(7);
 */
public class VipEmblemView extends View {

    private int vipLevel = 1;
    private Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint goldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint darkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint shinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint gemPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float shineX = -1f;
    private ValueAnimator shineAnimator;

    public VipEmblemView(Context context) {
        super(context);
        init();
    }

    public VipEmblemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VipEmblemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        goldPaint.setStyle(Paint.Style.FILL);
        redPaint.setStyle(Paint.Style.FILL);
        darkPaint.setStyle(Paint.Style.FILL);
        darkPaint.setColor(Color.parseColor("#1A0A00"));
        glowPaint.setStyle(Paint.Style.FILL);

        shinePaint.setStyle(Paint.Style.FILL);
        shinePaint.setColor(Color.WHITE);
        shinePaint.setAlpha(90);

        textPaint.setColor(Color.WHITE);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setShadowLayer(8f, 0f, 2f, Color.parseColor("#FFD700"));

        gemPaint.setStyle(Paint.Style.FILL);

        startShine();
    }

    public void setVipLevel(int level) {
        if (level < 0) level = 0;
        if (level > 8) level = 8;
        this.vipLevel = level;
        invalidate();
    }

    public int getVipLevel() {
        return vipLevel;
    }

    // ===== VIP 1-8 COLOR - Video Onujayi =====
    private int[] getMainColors() {
        switch (vipLevel) {
            case 0: return new int[]{Color.parseColor("#9E9E9E"), Color.parseColor("#616161")};
            case 1: return new int[]{Color.parseColor("#4FC3F7"), Color.parseColor("#01579B")}; // Blue Eagle
            case 2: return new int[]{Color.parseColor("#29B6F6"), Color.parseColor("#0277BD")};
            case 3: return new int[]{Color.parseColor("#AB47BC"), Color.parseColor("#4A148C")}; // Purple
            case 4: return new int[]{Color.parseColor("#FF7043"), Color.parseColor("#BF360C")}; // Orange
            case 5: return new int[]{Color.parseColor("#FFD54F"), Color.parseColor("#FF6D00")}; // Gold Orange
            case 6: return new int[]{Color.parseColor("#FFCA28"), Color.parseColor("#E65100")}; // Gold
            case 7: return new int[]{Color.parseColor("#FFD700"), Color.parseColor("#D32F2F")}; // Gold + Red Lion
            case 8: return new int[]{Color.parseColor("#FFE082"), Color.parseColor("#B71C1C")}; // Gold + Deep Red Phoenix
            default: return new int[]{Color.parseColor("#FFD700"), Color.parseColor("#D32F2F")};
        }
    }

    private void startShine() {
        if (shineAnimator != null) return;
        shineAnimator = ValueAnimator.ofFloat(-1f, 2f);
        shineAnimator.setDuration(2200);
        shineAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shineAnimator.setRepeatMode(ValueAnimator.RESTART);
        shineAnimator.setInterpolator(new LinearInterpolator());
        shineAnimator.addUpdateListener(animation -> {
            shineX = (float) animation.getAnimatedValue();
            invalidate();
        });
        shineAnimator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (vipLevel == 0) return;

        float w = getWidth();
        float h = getHeight();
        if (w == 0 || h == 0) return;
        float cx = w / 2f;
        float cy = h / 2f;
        float radius = Math.min(w, h) / 2f;

        int[] colors = getMainColors();
        int main = colors[0];
        int dark = colors[1];

        // ===== 1. OUTER GLOW - VIP Joto Boro - Glow Toto Boro =====
        float glowRadius = radius * (1.25f + vipLevel * 0.03f);
        RadialGradient glow = new RadialGradient(cx, cy, glowRadius,
                Color.argb(110 + vipLevel * 12, Color.red(main), Color.green(main), Color.blue(main)),
                Color.TRANSPARENT, Shader.TileMode.CLAMP);
        glowPaint.setShader(glow);
        canvas.drawCircle(cx, cy, glowRadius, glowPaint);
        glowPaint.setShader(null);

        // ===== 2. OUTER GOLD RING =====
        float ringR = radius * 0.92f;
        LinearGradient ringGrad = new LinearGradient(0, cy - ringR, 0, cy + ringR,
                Color.parseColor("#FFF8E1"), Color.parseColor("#FFD700"), Shader.TileMode.CLAMP);
        goldPaint.setShader(ringGrad);
        canvas.drawCircle(cx, cy, ringR, goldPaint);
        goldPaint.setShader(null);

        // ===== 3. INNER DARK + RED BG =====
        float innerR = radius * 0.82f;
        LinearGradient bgGrad = new LinearGradient(0, cy - innerR, 0, cy + innerR,
                main, dark, Shader.TileMode.CLAMP);
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setShader(bgGrad);
        canvas.drawCircle(cx, cy, innerR, bgPaint);

        // ===== 4. CROWN SHAPE - Center =====
        float cw = radius * 1.05f;
        float ch = radius * 0.70f;
        float crownTop = cy - ch * 0.55f;
        float crownBottom = cy + ch * 0.45f;

        Path crown = new Path();
        crown.moveTo(cx - cw / 2, crownBottom);
        crown.lineTo(cx - cw / 2, crownTop + ch * 0.25f);
        crown.lineTo(cx - cw * 0.25f, crownTop + ch * 0.45f);
        crown.lineTo(cx, crownTop); // Middle Peak - Tallest
        crown.lineTo(cx + cw * 0.25f, crownTop + ch * 0.45f);
        crown.lineTo(cx + cw / 2, crownTop + ch * 0.25f);
        crown.lineTo(cx + cw / 2, crownBottom);
        crown.close();

        LinearGradient crownGrad = new LinearGradient(0, crownTop, 0, crownBottom,
                Color.parseColor("#FFFDE7"), Color.parseColor("#FFB300"), Shader.TileMode.CLAMP);
        goldPaint.setShader(crownGrad);
        canvas.drawPath(crown, goldPaint);
        goldPaint.setShader(null);

        // Crown Base Band - Red for VIP 7-8, Gold for others
        RectF band = new RectF(cx - cw / 2, crownBottom - ch * 0.18f, cx + cw / 2, crownBottom + ch * 0.08f);
        if (vipLevel >= 7) {
            redPaint.setColor(Color.parseColor("#D32F2F"));
            canvas.drawRoundRect(band, 8f, 8f, redPaint);
        } else {
            goldPaint.setColor(Color.parseColor("#FF8F00"));
            canvas.drawRoundRect(band, 8f, 8f, goldPaint);
        }

        // ===== 5. CENTER GEM - VIP 7-8 Red Diamond, Others Gold =====
        float gemR = radius * 0.14f + vipLevel * 1.2f;
        if (vipLevel >= 7) gemPaint.setColor(Color.parseColor("#FF1744"));
        else if (vipLevel >= 5) gemPaint.setColor(Color.parseColor("#FFD700"));
        else gemPaint.setColor(Color.WHITE);
        gemPaint.setShadowLayer(10f, 0f, 0f, gemPaint.getColor());

        Path gem = new Path();
        gem.moveTo(cx, cy - gemR);
        gem.lineTo(cx + gemR * 0.7f, cy);
        gem.lineTo(cx, cy + gemR);
        gem.lineTo(cx - gemR * 0.7f, cy);
        gem.close();
        canvas.drawPath(gem, gemPaint);
        gemPaint.clearShadowLayer();

        // ===== 6. VIP TEXT - Bottom =====
        textPaint.setTextSize(radius * 0.38f);
        canvas.drawText("VIP " + vipLevel, cx, cy + radius * 0.78f, textPaint);

        // ===== 7. SHINE SWEEP - Chokchok Effect =====
        if (shineX >= 0) {
            float sx = w * shineX;
            canvas.save();
            canvas.clipRect(0, 0, w, h);
            // Diagonal shine band
            Path shine = new Path();
            shine.moveTo(sx - 30, 0);
            shine.lineTo(sx + 10, 0);
            shine.lineTo(sx - 10, h);
            shine.lineTo(sx - 50, h);
            shine.close();
            canvas.drawPath(shine, shinePaint);
            canvas.restore();
        }

        // ===== 8. VIP 7-8 EXTRA FIRE DOTS =====
        if (vipLevel >= 7) {
            Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
            dot.setColor(Color.parseColor("#FFEB3B"));
            for (int i = 0; i < 6; i++) {
                double angle = (i / 6.0) * Math.PI * 2;
                float dx = (float) (Math.cos(angle) * radius * 0.95f);
                float dy = (float) (Math.sin(angle) * radius * 0.95f);
                canvas.drawCircle(cx + dx, cy + dy, radius * 0.06f, dot);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (shineAnimator != null) {
            shineAnimator.cancel();
            shineAnimator = null;
        }
    }
}
