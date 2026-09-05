package com.example.arc.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.arc.R;

/**
 * The signature of the design: one tick per day of the cycle, arranged in a circle,
 * so the shape alone says how far along you are before you read a word.
 *
 * <p>Deep ticks are bleeding days, sand ticks the fertile window, the tall dark tick
 * is today. A thin arc sweeps from the top to mark progress.
 */
public class CycleRingView extends View {

    private static final float TICK_RADIUS_DP = 118f;
    private static final float ARC_RADIUS_DP = 105f;
    private static final float TICK_W_DP = 2.5f;
    private static final float TICK_H_DP = 13f;
    private static final float TICK_H_PERIOD_DP = 15f;
    private static final float TICK_W_TODAY_DP = 3.5f;
    private static final float TICK_H_TODAY_DP = 22f;
    private static final float ARC_STROKE_DP = 2.5f;

    private final Paint tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final RectF arcBounds = new RectF();

    private int cycleLength = 28;
    private int periodLength = 5;
    private int fertileStart = 12;
    private int fertileEnd = 16;
    private int today = 1;

    private int colorPeriod;
    private int colorFertile;
    private int colorPast;
    private int colorFuture;
    private int colorToday;
    private int colorAccent;

    public CycleRingView(Context context) {
        this(context, null);
    }

    public CycleRingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        colorPeriod = ContextCompat.getColor(context, R.color.arc_deep);
        colorFertile = ContextCompat.getColor(context, R.color.arc_tick_fertile);
        colorPast = ContextCompat.getColor(context, R.color.arc_tick_past);
        colorFuture = ContextCompat.getColor(context, R.color.arc_tick_future);
        colorToday = ContextCompat.getColor(context, R.color.arc_ink);
        colorAccent = ContextCompat.getColor(context, R.color.arc_accent);

        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        arcPaint.setColor(colorAccent);
        arcPaint.setAlpha(230);
    }

    /**
     * @param today        1-based day of the cycle
     * @param cycleLength  days in the cycle
     * @param periodLength leading days that count as bleeding
     * @param fertileStart first fertile day (1-based, inclusive)
     * @param fertileEnd   last fertile day (1-based, inclusive)
     */
    public void setCycle(int today, int cycleLength, int periodLength,
                         int fertileStart, int fertileEnd) {
        this.today = today;
        this.cycleLength = Math.max(cycleLength, 1);
        this.periodLength = periodLength;
        this.fertileStart = fertileStart;
        this.fertileEnd = fertileEnd;
        invalidate();
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        // Progress arc, starting at 12 o'clock.
        float arcR = dp(ARC_RADIUS_DP);
        arcPaint.setStrokeWidth(dp(ARC_STROKE_DP));
        arcBounds.set(cx - arcR, cy - arcR, cx + arcR, cy + arcR);
        float sweep = 360f * Math.min(today, cycleLength) / cycleLength;
        canvas.drawArc(arcBounds, -90f, sweep, false, arcPaint);

        float tickR = dp(TICK_RADIUS_DP);
        float corner = dp(2f);

        for (int i = 1; i <= cycleLength; i++) {
            boolean isPeriod = i <= periodLength;
            boolean isFertile = i >= fertileStart && i <= fertileEnd;
            boolean isToday = i == today;
            boolean past = i < today;

            float w = dp(TICK_W_DP);
            float h = dp(TICK_H_DP);
            int color;
            int alpha = 255;

            if (isPeriod) {
                color = colorPeriod;
                h = dp(TICK_H_PERIOD_DP);
            } else if (isFertile) {
                color = colorFertile;
            } else if (past) {
                color = colorPast;
            } else {
                color = colorFuture;
                alpha = 191; // 0.75
            }
            if (isToday) {
                color = colorToday;
                w = dp(TICK_W_TODAY_DP);
                h = dp(TICK_H_TODAY_DP);
                alpha = 255;
            }

            tickPaint.setColor(color);
            tickPaint.setAlpha(alpha);

            canvas.save();
            canvas.rotate((i - 1) * 360f / cycleLength, cx, cy);
            rect.set(cx - w / 2f, cy - tickR - h / 2f, cx + w / 2f, cy - tickR + h / 2f);
            canvas.drawRoundRect(rect, corner, corner, tickPaint);
            canvas.restore();
        }
    }
}
