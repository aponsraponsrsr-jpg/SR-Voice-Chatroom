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
 * PART 5 - VipProfileCardView.java
 * VIP Profile Card - VIP 1-8
 * Profile e dhukle upore Crown + Fire BG + Gold Name Glow
 * Waki Star Style
 *
 * USE in XML:
 * <com.sr.voicechat.VipProfileCardView
 *     android:layout_width="match_parent"
 *     android:layout_height="180dp" />
 * card.setVipLevel(7);
 */
public class VipProfileCardView extends View {

    private int vipLevel = 1;
    private Paint bgPaint;
    private Paint borderPaint;
    private float shineX = -1f;
    private float glowAlpha = 0.5f;
    private float crownBounce = 0f;

    public VipProfileCardView(Context context) {
        super(context);
        init();
    }

    public VipProfileCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f);

        // Shine
        ValueAnimator shineAnim = ValueAnimator.ofFloat(-0.5f, 1.5f);
        shineAnim.setDuration(3000);
        shineAnim.setRepeatCount(ValueAnimator.INFINITE);
        shineAnim.setInterpolator(new LinearInterpolator());
        shineAnim.addUpdateListener(a -> {
            shineX = (float) a.getAnimatedValue();
            invalidate();
        });
        shineAnim.start();

        // Glow pulse
        ValueAnimator glowAnim = ValueAnimator.ofFloat(0.35f, 0.75f);
        glowAnim.setDuration(1600);
        glowAnim.setRepeatCount(ValueAnimator.INFINITE);
        glowAnim.setRepeatMode(ValueAnimator.REVERSE);
        glowAnim.setInterpolator(new LinearInterpolator());
        glowAnim.addUpdateListener(a -> {
            glowAlpha = (float) a.getAnimatedValue();
            invalidate();
        });
        glowAnim.start();

        // Crown bounce - halka upore niche
        ValueAnimator bounceAnim = ValueAnimator.ofFloat(0f, -6f);
        bounceAnim.setDuration(900);
        bounceAnim.setRepeatCount(ValueAnimator.INFINITE);
        bounceAnim.setRepeatMode(ValueAnimator.REVERSE);
        bounceAnim.setInterpolator(new LinearInterpolator());
        bounceAnim.addUpdateListener(a -> {
            crownBounce = (float) a.getAnimatedValue();
            invalidate();
        });
        bounceAnim.start();
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

    // VIP 1-8 Card BG Colors - Video onujayi
    private int[] getColors() {
        switch (vipLevel) {
            case 1: return new int[]{Color.parseColor("#0D47A1"), Color.parseColor("#42A5F5"), Color.parseColor("#B3E5FC")};
            case 2: return new int[]{Color.parseColor("#006064"), Color.parseColor("#26C6DA"), Color.parseColor("#B2EBF2")};
            case 3: return new int[]{Color.parseColor("#1B5E20"), Color.parseColor("#66BB6A"), Color.parseColor("#C8E6C9")};
            case 4: return new int[]{Color.parseColor("#4A148C"), Color.parseColor("#AB47BC"), Color.parseColor("#E1BEE7")};
            case 5: return new int[]{Color.parseColor("#E65100"), Color.parseColor("#FFA726"), Color.parseColor("#FFE0B2")};
            case 6: return new int[]{Color.parseColor("#BF360C"), Color.parseColor("#FF7043"), Color.parseColor("#FFCCBC")};
            case 7: return new int[]{Color.parseColor("#3E0000"), Color.parseColor("#FF1744"), Color.parseColor("#FFD700")};
            case 8: return new int[]{Color.parseColor("#1A0000"), Color.parseColor("#D50000"), Color.parseColor("#FFD700")};
            default: return new int[]{Color.parseColor("#3E0000"), Color.parseColor("#FF1744"), Color.parseColor("#FFD700")};
        }
    }

    private String getVipName() {
        switch (vipLevel) {
            case 1: return "VIP 1 • BLUE EAGLE";
            case 2: return "VIP 2 • CYAN";
            case 3: return "VIP 3 • EMERALD";
            case 4: return "VIP 4 • ROYAL";
            case 5: return "VIP 5 • GOLDEN";
            case 6: return "VIP 6 • FIRE";
            case 7: return "VIP 7 • GOLD LION";
            case 8: return "VIP 8 • PHOENIX KING";
            default: return "VIP " + vipLevel;
        }
    }

    public String getVipTitle() {
        return getVipName();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        int[] colors = getColors();
        float radius = 28f;
        RectF rect = new RectF(2f, 2f, w - 2f, h - 2f);

        // 1. OUTER GLOW - VIP 5+ e beshi
        if (vipLevel >= 5) {
            Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
            glow.setColor(colors[1]);
            glow.setAlpha((int) (glowAlpha * 60));
            glow.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(new RectF(0, 0, w, h), radius + 6, radius + 6, glow);
        }

        // 2. MAIN BG - Dark to Color Gradient
        LinearGradient grad = new LinearGradient(0, 0, w, h,
                new int[]{colors[0], colors[1], colors[0]},
                new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP);
        bgPaint.setShader(grad);
        canvas.drawRoundRect(rect, radius, radius, bgPaint);
        bgPaint.setShader(null);

        // 3. BORDER - Gold for 7-8, Color for others
        if (vipLevel >= 7) {
            borderPaint.setColor(Color.parseColor("#FFD700"));
            borderPaint.setStrokeWidth(3.5f);
            canvas.drawRoundRect(rect, radius, radius, borderPaint);
        } else {
            borderPaint.setColor(colors[2]);
            borderPaint.setAlpha(180);
            borderPaint.setStrokeWidth(2.5f);
            canvas.drawRoundRect(rect, radius, radius, borderPaint);
        }

        // 4. TOP GLASS - Uporer dikta halka sada
        Paint glass = new Paint(Paint.ANTI_ALIAS_FLAG);
        glass.setColor(Color.WHITE);
        glass.setAlpha(45);
        RectF topRect = new RectF(6f, 6f, w - 6f, h * 0.45f);
        canvas.drawRoundRect(topRect, radius - 4, radius - 4, glass);

        // 5. CROWN - Majhkhane upore - Bounce korbe
        float cx = w / 2f;
        float crownY = h * 0.28f + crownBounce;
        drawCrown(canvas, cx, crownY, colors);

        // 6. VIP TEXT - Niche - Gold / White
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        textPaint.setTextSize(h * 0.16f);
        if (vipLevel >= 7) {
            textPaint.setColor(Color.parseColor("#FFD700"));
            // Gold glow shadow
            textPaint.setShadowLayer(12f, 0, 0, Color.parseColor("#FF1744"));
        } else {
            textPaint.setColor(Color.WHITE);
            textPaint.setShadowLayer(8f, 0, 0, colors[1]);
        }
        canvas.drawText(getVipName(), cx, h * 0.72f, textPaint);
        textPaint.setShadowLayer(0, 0, 0, 0);

        // 7. SMALL LINE - Niche
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.WHITE);
        linePaint.setAlpha(120);
        linePaint.setStrokeWidth(2f);
        float lineW = w * 0.25f;
        canvas.drawLine(cx - lineW, h * 0.82f, cx + lineW, h * 0.82f, linePaint);

        // 8. MOVING SHINE
        if (shineX >= -0.5f && shineX <= 1.5f) {
            float sx = shineX * w;
            Paint s = new Paint(Paint.ANTI_ALIAS_FLAG);
            s.setColor(Color.WHITE);
            s.setAlpha(60);
            s.setStrokeWidth(18f);
            canvas.drawLine(sx - 25, 0, sx + 25, h, s);
        }

        // 9. VIP 8 - Extra Stars
        if (vipLevel >= 8) {
            Paint star = new Paint(Paint.ANTI_ALIAS_FLAG);
            star.setColor(Color.parseColor("#FFD700"));
            star.setStyle(Paint.Style.FILL);
            for (int i = 0; i < 5; i++) {
                float sx = w * 0.12f + i * w * 0.19f;
                float sy = h * 0.90f;
                canvas.drawCircle(sx, sy, 3.2f, star);
            }
        }
    }

    private void drawCrown(Canvas canvas, float cx, float cy, int[] colors) {
        Paint crownPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        crownPaint.setStyle(Paint.Style.FILL);
        crownPaint.setColor(Color.parseColor("#FFD700"));

        float cw = 52f;
        float ch = 32f;

        // Crown base
        RectF base = new RectF(cx - cw / 2, cy + ch / 2 - 6, cx + cw / 2, cy + ch / 2 + 6);
        canvas.drawRoundRect(base, 4f, 4f, crownPaint);

        // Crown body - 3 chura
        android.graphics.Path path = new android.graphics.Path();
        path.moveTo(cx - cw / 2, cy + ch / 2 - 6);
        path.lineTo(cx - cw / 2, cy - ch / 2);
        path.lineTo(cx - cw / 4, cy + 2);
        path.lineTo(cx, cy - ch / 2 - 8);
        path.lineTo(cx + cw / 4, cy + 2);
        path.lineTo(cx + cw / 2, cy - ch / 2);
        path.lineTo(cx + cw / 2, cy + ch / 2 - 6);
        path.close();
        canvas.drawPath(path, crownPaint);

        // 3 ta Red Gem
        Paint gem = new Paint(Paint.ANTI_ALIAS_FLAG);
        gem.setColor(Color.parseColor("#FF1744"));
        gem.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx - cw / 2, cy - ch / 2, 4f, gem);
        canvas.drawCircle(cx, cy - ch / 2 - 8, 4.5f, gem);
        canvas.drawCircle(cx + cw / 2, cy - ch / 2, 4f, gem);

        // White shine on crown
        Paint shine = new Paint(Paint.ANTI_ALIAS_FLAG);
        shine.setColor(Color.WHITE);
        shine.setAlpha(150);
        canvas.drawCircle(cx - 12, cy - 2, 3f, shine);
    }
}
