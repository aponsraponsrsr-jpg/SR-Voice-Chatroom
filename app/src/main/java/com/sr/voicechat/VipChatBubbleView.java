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
 * PART 4 - VipChatBubbleView.java
 * VIP Chat Bubble - VIP 1-8
 * Room / Private Chat e VIP er message er background
 * Gold + Red + Glass + Shine
 *
 * USE in XML:
 * <com.sr.voicechat.VipChatBubbleView
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content" />
 * bubble.setVipLevel(7);
 */
public class VipChatBubbleView extends View {

    private int vipLevel = 1;
    private Paint bgPaint;
    private Paint borderPaint;
    private Paint shinePaint;
    private float shineX = -1f;
    private float glowAlpha = 0.5f;

    public VipChatBubbleView(Context context) {
        super(context);
        init();
    }

    public VipChatBubbleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2.5f);

        shinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Shine animation
        ValueAnimator shineAnim = ValueAnimator.ofFloat(-0.5f, 1.5f);
        shineAnim.setDuration(2800);
        shineAnim.setRepeatCount(ValueAnimator.INFINITE);
        shineAnim.setInterpolator(new LinearInterpolator());
        shineAnim.addUpdateListener(a -> {
            shineX = (float) a.getAnimatedValue();
            invalidate();
        });
        shineAnim.start();

        // Glow pulse
        ValueAnimator glowAnim = ValueAnimator.ofFloat(0.3f, 0.7f);
        glowAnim.setDuration(1500);
        glowAnim.setRepeatCount(ValueAnimator.INFINITE);
        glowAnim.setRepeatMode(ValueAnimator.REVERSE);
        glowAnim.setInterpolator(new LinearInterpolator());
        glowAnim.addUpdateListener(a -> {
            glowAlpha = (float) a.getAnimatedValue();
            invalidate();
        });
        glowAnim.start();
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

    // VIP 1-8 Bubble Colors - Video onujayi
    private int[] getColors() {
        switch (vipLevel) {
            case 1: return new int[]{Color.parseColor("#E3F2FD"), Color.parseColor("#90CAF9"), Color.parseColor("#1565C0")};
            case 2: return new int[]{Color.parseColor("#E0F7FA"), Color.parseColor("#80DEEA"), Color.parseColor("#00838F")};
            case 3: return new int[]{Color.parseColor("#E8F5E9"), Color.parseColor("#A5D6A7"), Color.parseColor("#2E7D32")};
            case 4: return new int[]{Color.parseColor("#F3E5F5"), Color.parseColor("#CE93D8"), Color.parseColor("#6A1B9A")};
            case 5: return new int[]{Color.parseColor("#FFF3E0"), Color.parseColor("#FFCC80"), Color.parseColor("#EF6C00")};
            case 6: return new int[]{Color.parseColor("#FBE9E7"), Color.parseColor("#FFAB91"), Color.parseColor("#D84315")};
            case 7: return new int[]{Color.parseColor("#FFFDE7"), Color.parseColor("#FFE082"), Color.parseColor("#FF1744")};
            case 8: return new int[]{Color.parseColor("#FFF8E1"), Color.parseColor("#FFD700"), Color.parseColor("#D50000")};
            default: return new int[]{Color.parseColor("#FFFDE7"), Color.parseColor("#FFE082"), Color.parseColor("#FF1744")};
        }
    }

    private int getTextColor() {
        // VIP 7-8 te dark red, baki te dark
        if (vipLevel >= 7) return Color.parseColor("#4A0000");
        if (vipLevel >= 5) return Color.parseColor("#3E2723");
        return Color.parseColor("#212121");
    }

    public int getVipTextColor() {
        return getTextColor();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        int[] colors = getColors();
        float radius = 22f;
        RectF rect = new RectF(2f, 2f, w - 2f, h - 2f);

        // 1. OUTER GLOW - VIP 5+ e beshi
        if (vipLevel >= 5) {
            Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
            glow.setColor(colors[2]);
            glow.setAlpha((int) (glowAlpha * 55));
            glow.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(new RectF(0, 0, w, h), radius + 4, radius + 4, glow);
        }

        // 2. MAIN BG - Light Gradient
        LinearGradient grad = new LinearGradient(0, 0, w, h,
                new int[]{colors[0], colors[1]},
                new float[]{0f, 1f}, Shader.TileMode.CLAMP);
        bgPaint.setShader(grad);
        canvas.drawRoundRect(rect, radius, radius, bgPaint);
        bgPaint.setShader(null);

        // 3. BORDER - VIP Color
        if (vipLevel >= 7) {
            // VIP 7-8 - Double Border - Gold + Red
            borderPaint.setColor(Color.parseColor("#FFD700"));
            borderPaint.setStrokeWidth(3.5f);
            canvas.drawRoundRect(rect, radius, radius, borderPaint);

            Paint innerBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
            innerBorder.setStyle(Paint.Style.STROKE);
            innerBorder.setColor(colors[2]);
            innerBorder.setStrokeWidth(1.5f);
            canvas.drawRoundRect(new RectF(5f, 5f, w - 5f, h - 5f), radius - 3, radius - 3, innerBorder);
        } else {
            borderPaint.setColor(colors[2]);
            borderPaint.setAlpha(180);
            borderPaint.setStrokeWidth(2f);
            canvas.drawRoundRect(rect, radius, radius, borderPaint);
        }

        // 4. TOP GLASS SHINE - Uporer dikta chokchok
        Paint glass = new Paint(Paint.ANTI_ALIAS_FLAG);
        glass.setColor(Color.WHITE);
        glass.setAlpha(70);
        RectF topRect = new RectF(4f, 4f, w - 4f, h / 2.2f);
        canvas.drawRoundRect(topRect, radius - 2, radius - 2, glass);

        // 5. MOVING SHINE - Baam theke Daan
        if (shineX >= -0.5f && shineX <= 1.5f) {
            float sx = shineX * w;
            Paint s = new Paint(Paint.ANTI_ALIAS_FLAG);
            s.setColor(Color.WHITE);
            s.setAlpha(70);
            s.setStrokeWidth(14f);
            canvas.drawLine(sx - 20, 0, sx + 20, h, s);
        }

        // 6. VIP BADGE DOT - Dan pase choto Crown dot - VIP 6+
        if (vipLevel >= 6) {
            Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
            dot.setColor(Color.parseColor("#FFD700"));
            dot.setStyle(Paint.Style.FILL);
            canvas.drawCircle(w - 14f, 14f, 5f, dot);
            dot.setColor(Color.WHITE);
            canvas.drawCircle(w - 14f, 14f, 2f, dot);
        }
    }
}
