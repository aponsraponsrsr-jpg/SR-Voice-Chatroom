package com.sr.voicechat;

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
import android.view.animation.LinearInterpolator;

/**
 * PART 2 - VipFrameView.java
 * VIP 1-8 Avatar Frame - Gold + Red + Glow + Shine
 * Waki Star Style - Code diye Premium Frame
 *
 * USE:
 * <com.sr.voicechat.VipFrameView
 *     android:layout_width="84dp"
 *     android:layout_height="84dp" />
 * frameView.setVipLevel(7);
 */
public class VipFrameView extends View {

    private int vipLevel = 1;
    private Paint borderPaint;
    private Paint glowPaint;
    private Paint shinePaint;
    private Paint innerPaint;
    private float shineX = -1f;
    private float glowAlpha = 0.6f;

    public VipFrameView(Context context) {
        super(context);
        init();
    }

    public VipFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.STROKE);

        shinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shinePaint.setColor(Color.WHITE);
        shinePaint.setAlpha(0);

        innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerPaint.setStyle(Paint.Style.STROKE);
        innerPaint.setColor(Color.parseColor("#22000000"));
        innerPaint.setStrokeWidth(1.5f);

        startAnimation();
    }

    public void setVipLevel(int level) {
        if (level < 1) level = 1;
        if (level > 8) level = 8;
        this.vipLevel = level;
        invalidate();
    }

    public int getVipLevel() {
        return vipLevel;
    }

    private void startAnimation() {
        // Shine - Baam theke Daan e jabe - bar bar
        ValueAnimator shineAnim = ValueAnimator.ofFloat(-1f, 2f);
        shineAnim.setDuration(2600);
        shineAnim.setRepeatCount(ValueAnimator.INFINITE);
        shineAnim.setRepeatMode(ValueAnimator.RESTART);
        shineAnim.setInterpolator(new LinearInterpolator());
        shineAnim.addUpdateListener(a -> {
            shineX = (float) a.getAnimatedValue();
            invalidate();
        });
        shineAnim.start();

        // Glow - Shash neyar moto uthbe nambe
        ValueAnimator glowAnim = ValueAnimator.ofFloat(0.35f, 0.85f);
        glowAnim.setDuration(1400);
        glowAnim.setRepeatCount(ValueAnimator.INFINITE);
        glowAnim.setRepeatMode(ValueAnimator.REVERSE);
        glowAnim.setInterpolator(new LinearInterpolator());
        glowAnim.addUpdateListener(a -> {
            glowAlpha = (float) a.getAnimatedValue();
            invalidate();
        });
        glowAnim.start();
    }

    // VIP 1-8 Color - Video onujayi
    private int[] getColors() {
        switch (vipLevel) {
            case 1: return new int[]{Color.parseColor("#4FC3F7"), Color.parseColor("#1565C0"), Color.parseColor("#B3E5FC")};
            case 2: return new int[]{Color.parseColor("#26C6DA"), Color.parseColor("#00838F"), Color.parseColor("#B2EBF2")};
            case 3: return new int[]{Color.parseColor("#66BB6A"), Color.parseColor("#2E7D32"), Color.parseColor("#C8E6C9")};
            case 4: return new int[]{Color.parseColor("#AB47BC"), Color.parseColor("#6A1B9A"), Color.parseColor("#E1BEE7")};
            case 5: return new int[]{Color.parseColor("#FFA726"), Color.parseColor("#EF6C00"), Color.parseColor("#FFE0B2")};
            case 6: return new int[]{Color.parseColor("#FF7043"), Color.parseColor("#D84315"), Color.parseColor("#FFCCBC")};
            case 7: return new int[]{Color.parseColor("#FFD700"), Color.parseColor("#FF1744"), Color.parseColor("#FFF59D")};
            case 8: return new int[]{Color.parseColor("#FFD700"), Color.parseColor("#D50000"), Color.parseColor("#FFFFFF")};
            default: return new int[]{Color.parseColor("#FFD700"), Color.parseColor("#FF1744"), Color.parseColor("#FFF59D")};
        }
    }

    private float getStrokeWidth() {
        // VIP joto boro - Frame toto mota
        if (vipLevel >= 8) return 9f;
        if (vipLevel >= 7) return 8f;
        if (vipLevel >= 5) return 6.5f;
        if (vipLevel >= 3) return 5f;
        return 4f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        float cx = w / 2f;
        float cy = h / 2f;
        float radius = Math.min(w, h) / 2f - getStrokeWidth();

        int[] colors = getColors();

        // 1. OUTER GLOW - VIP 5+ e beshi jolbe
        if (vipLevel >= 5) {
            glowPaint.setStrokeWidth(getStrokeWidth() + 10f);
            glowPaint.setColor(colors[1]);
            glowPaint.setAlpha((int) (glowAlpha * 70));
            canvas.drawCircle(cx, cy, radius + 2f, glowPaint);

            glowPaint.setStrokeWidth(getStrokeWidth() + 5f);
            glowPaint.setAlpha((int) (glowAlpha * 110));
            canvas.drawCircle(cx, cy, radius + 1f, glowPaint);
        } else {
            glowPaint.setStrokeWidth(getStrokeWidth() + 4f);
            glowPaint.setColor(colors[1]);
            glowPaint.setAlpha((int) (glowAlpha * 60));
            canvas.drawCircle(cx, cy, radius + 1f, glowPaint);
        }

        // 2. MAIN FRAME - Gradient Gold + Red / Blue
        LinearGradient gradient = new LinearGradient(0, 0, w, h,
                new int[]{colors[0], colors[1], colors[2], colors[1], colors[0]},
                new float[]{0f, 0.3f, 0.5f, 0.7f, 1f},
                Shader.TileMode.CLAMP);
        borderPaint.setShader(gradient);
        borderPaint.setStrokeWidth(getStrokeWidth());
        canvas.drawCircle(cx, cy, radius, borderPaint);
        borderPaint.setShader(null);

        // 3. INNER THIN GOLD LINE
        innerPaint.setColor(Color.WHITE);
        innerPaint.setAlpha(120);
        innerPaint.setStrokeWidth(1.2f);
        canvas.drawCircle(cx, cy, radius - getStrokeWidth() - 1f, innerPaint);

        // 4. VIP 7-8 - DOUBLE RING - Extra Premium
        if (vipLevel >= 7) {
            Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            dotPaint.setColor(colors[0]);
            dotPaint.setStyle(Paint.Style.FILL);
            // 8 ta choto dot - Crown er moto
            for (int i = 0; i < 8; i++) {
                double angle = (Math.PI * 2 * i) / 8;
                float dx = (float) (cx + Math.cos(angle) * (radius + getStrokeWidth() + 3f));
                float dy = (float) (cy + Math.sin(angle) * (radius + getStrokeWidth() + 3f));
                canvas.drawCircle(dx, dy, 2.2f, dotPaint);
            }
        }

        // 5. SHINE - Chokchok
        if (shineX >= 0 && shineX <= 1.5f) {
            float sx = shineX * w;
            Paint sPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            sPaint.setColor(Color.WHITE);
            sPaint.setAlpha(90);
            sPaint.setStrokeWidth(10f);
            canvas.drawLine(sx - 18, 0, sx + 18, h, sPaint);
            sPaint.setAlpha(45);
            sPaint.setStrokeWidth(22f);
            canvas.drawLine(sx - 18, 0, sx + 18, h, sPaint);
        }

        // 6. CROWN DOT - UPORE - VIP 6+ e
        if (vipLevel >= 6) {
            Paint crownDot = new Paint(Paint.ANTI_ALIAS_FLAG);
            crownDot.setColor(Color.parseColor("#FFD700"));
            crownDot.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx, 4f, 4f, crownDot);
            crownDot.setColor(Color.WHITE);
            canvas.drawCircle(cx, 4f, 1.8f, crownDot);
        }
    }
}
