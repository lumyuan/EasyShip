package com.pointer.wave.easyship.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.mixiaoxiao.overscroll.OverScrollDelegate;

public class OverRecyclerView extends RecyclerView implements OverScrollDelegate.OverScrollable {
    private OverScrollDelegate mOverScrollDelegate;

    public OverRecyclerView(Context context) {
        super(context);
        this.createOverScrollDelegate(context);
    }

    public OverRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.createOverScrollDelegate(context);
    }

    public OverRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.createOverScrollDelegate(context);
    }

    private void createOverScrollDelegate(Context context) {
        this.mOverScrollDelegate = new OverScrollDelegate(this);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return this.mOverScrollDelegate.onInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        return this.mOverScrollDelegate.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @SuppressLint("MissingSuperCall")
    public void draw(Canvas canvas) {
        //super.draw(canvas);
        this.mOverScrollDelegate.draw(canvas);
    }

    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return this.mOverScrollDelegate.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    public int superComputeVerticalScrollExtent() {
        return super.computeVerticalScrollExtent();
    }

    public int superComputeVerticalScrollOffset() {
        return super.computeVerticalScrollOffset();
    }

    public int superComputeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    public void superOnTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
    }

    public void superDraw(Canvas canvas) {
        super.draw(canvas);
    }

    public boolean superAwakenScrollBars() {
        return super.awakenScrollBars();
    }

    public boolean superOverScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    public View getOverScrollableView() {
        return this;
    }

    public OverScrollDelegate getOverScrollDelegate() {
        return this.mOverScrollDelegate;
    }
}
