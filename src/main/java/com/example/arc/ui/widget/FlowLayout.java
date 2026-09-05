package com.example.arc.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

/** Lays children out left to right, wrapping to a new line when the row is full. */
public class FlowLayout extends ViewGroup {

    private int horizontalGap;
    private int verticalGap;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;
        horizontalGap = (int) (8 * density);
        verticalGap = (int) (8 * density);
    }

    public void setGap(int horizontalPx, int verticalPx) {
        this.horizontalGap = horizontalPx;
        this.verticalGap = verticalPx;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int available = width - getPaddingLeft() - getPaddingRight();

        int x = 0;
        int y = 0;
        int rowHeight = 0;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            measureChild(child,
                    MeasureSpec.makeMeasureSpec(available, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

            int cw = child.getMeasuredWidth();
            int ch = child.getMeasuredHeight();
            if (x > 0 && x + cw > available) {
                x = 0;
                y += rowHeight + verticalGap;
                rowHeight = 0;
            }
            x += cw + horizontalGap;
            rowHeight = Math.max(rowHeight, ch);
        }
        int height = y + rowHeight + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, resolveSize(height, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int available = getWidth() - getPaddingLeft() - getPaddingRight();
        int x = getPaddingLeft();
        int y = getPaddingTop();
        int rowHeight = 0;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            int cw = child.getMeasuredWidth();
            int ch = child.getMeasuredHeight();
            if (x > getPaddingLeft() && x + cw > getPaddingLeft() + available) {
                x = getPaddingLeft();
                y += rowHeight + verticalGap;
                rowHeight = 0;
            }
            child.layout(x, y, x + cw, y + ch);
            x += cw + horizontalGap;
            rowHeight = Math.max(rowHeight, ch);
        }
    }
}
