package com.pointer.wave.easyship.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.pointer.wave.easyship.R;
import com.pointer.wave.easyship.utils.DensityUtil;

/*
 符号栏类
 */
public class SymbolView {
    private final int TILE_WIDTH = 60;
    private PopupWindow popupWindow;
    private View rootView;
    private OnSymbolViewClick onSymbolViewClick;
    private boolean visible = false;
    private InputMethodManager inputMethodManager;
    private boolean isFirst = true;
    private int maxLayoutHeight = 0;//布局总长
    private int currentLayoutHeight = 0;//当前布局高
    private Context context;
    private boolean isUC = true;

    @SuppressLint({"ClickableViewAccessibility", "ResourceType"})
    public SymbolView(final Context context, final View rootView) {
        this.context = context;
        this.rootView = rootView;
        popupWindow = new PopupWindow(context);
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = LayoutInflater.from(context).inflate(R.layout.symbol_view, null);
        LinearLayout linearLayout = view.findViewById(R.id.linear_container);
        final float[] tempPoint = new float[2];
        String symbol = "←<br>→<br>换行<br>删行<br>+CVars=<br>=<br>r.<br>[<br>]<br>.<br>+<br>[UserCustom DeviceProfile]";
        if (!isFirst){
            symbol = "←<br>→<br>换行<br>+CVars=<br>=<br>r.<br>[<br>]<br>.<br>+<br>[UserCustom DeviceProfile]<br>[FansSwitcher]<br>[FansCustom]";
        }
        String[] symbolArrary = symbol.split("<br>");
        for (int i = 0; i < symbolArrary.length; i++) {
            TextView textView = new TextView(context);
            textView.setGravity(Gravity.CENTER);
            textView.setText(symbolArrary[i]);
            textView.setClickable(true);
            textView.setTextSize(20);
            //textView.setWidth();
            textView.setPadding(DensityUtil.dip2px(context, 10), DensityUtil.dip2px(context, 5), DensityUtil.dip2px(context, 10), DensityUtil.dip2px(context, 5));
            textView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int color = v.getDrawingCacheBackgroundColor();
                    int motionEvent = event.getAction();
                    TextView tv = (TextView) v;

                    if (motionEvent == MotionEvent.ACTION_DOWN) {
                        tempPoint[0] = event.getX();
                        tempPoint[1] = event.getY();
                        tv.setBackgroundColor(0xffcecfd1);

                    } else if (motionEvent == MotionEvent.ACTION_UP || motionEvent == MotionEvent.ACTION_CANCEL) {
                        tv.setBackgroundColor(Color.parseColor(context.getString(R.color.white)));
                        if (Math.abs(event.getX() - tempPoint[0]) < TILE_WIDTH) {
                            if (onSymbolViewClick != null)
                                onSymbolViewClick.onClick(tv, tv.getText().toString());
                        }
                    }
                    return true;
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.addView(textView, layoutParams);

        }
        popupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        //popupWindow.setHeight(EditCodeActivity.height);
        popupWindow.getBackground().setAlpha(0);//窗口完全透明
        view.setBackgroundColor(Color.parseColor(context.getString(R.color.white)));//视图不完全透明
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        popupWindow.setContentView(view);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        rootView.getWindowVisibleDisplayFrame(r);
                        if (isFirst) {
                            maxLayoutHeight = r.bottom;//初始化时为布局的最高高度
                            currentLayoutHeight = r.bottom;//当前弹出的布局高
                            isFirst = false;
                        } else {
                            currentLayoutHeight = r.bottom;//当前弹出的布局高
                        }
                        if (currentLayoutHeight == maxLayoutHeight || !visible) {
                            hide();
                        } else if (currentLayoutHeight < maxLayoutHeight) {
                            show(rootView.getHeight() - r.bottom);
                        }
                    }
                });
    }

    public void setUC(boolean UC) {
        isUC = UC;

    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    private void show(int bottom) {
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, bottom);
    }

    private void hide() {
        popupWindow.dismiss();
    }

    public void setOnSymbolViewClick(OnSymbolViewClick onSymbolViewClick) {
        this.onSymbolViewClick = onSymbolViewClick;
    }


    public interface OnSymbolViewClick {
        void onClick(View view, String text);
    }
}