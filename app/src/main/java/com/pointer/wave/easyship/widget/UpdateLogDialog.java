package com.pointer.wave.easyship.widget;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mmin18.widget.RealtimeBlurView;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.enums.PopupAnimation;
import com.pointer.wave.easyship.R;
import com.pointer.wave.easyship.common.activity.BaseActivity;

public class UpdateLogDialog extends BottomPopupView {

    public static AppCompatActivity activity;

    @SuppressLint("StaticFieldLeak")
    public static UpdateLogDialog dialog;
    private String html;

    public UpdateLogDialog(@NonNull Context context, String html) {
        super(context);
        dialog = this;
        this.html = html;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_full_base;
    }

    public static void showDialog(AppCompatActivity appCompatActivity, String html){
        activity = appCompatActivity;
        new XPopup.Builder(appCompatActivity)
                .animationDuration(750)
                .dismissOnTouchOutside(false)
                .hasShadowBg(false)
                .hasBlurBg(false)
                .isViewMode(true)
                .popupAnimation(PopupAnimation.TranslateAlphaFromBottom)
                .moveUpToKeyboard(false)
                .isDestroyOnDismiss(true)
                .isRequestFocus(true)
                .asCustom(new UpdateLogDialog(appCompatActivity, html))
                .show();
    }

    private LinearLayout relativeLayout;
    private View inflate;
    @Override
    protected void onCreate() {
        super.onCreate();
        relativeLayout = findViewById(R.id.root_view);
        inflate = View.inflate(getContext(), R.layout.dialog_update_log, null);
        addChildView(inflate);
        RealtimeBlurView blurView = findViewById(R.id.blur_view);
        translation(blurView);

        TextView logView = inflate.findViewById(R.id.log);
        logView.setText(getClickableHtml(html));
    }

    private void addChildView(View view) {
        relativeLayout.removeAllViews();
        relativeLayout.addView(view);
    }

    private void translation(View view){
        ObjectAnimator translationY = ObjectAnimator.ofFloat(view, "translationY", 0, 0.1f);
        translationY.setDuration(100);
        translationY.start();
        new Handler(view.getContext().getMainLooper()).postDelayed(()->{
            translation(view);
        }, 200);
    }

    public void setLinkClickable(SpannableStringBuilder clickableHtml, URLSpan urlSpan) {
        int start = clickableHtml.getSpanStart(urlSpan);
        int end = clickableHtml.getSpanEnd(urlSpan);
        int flags = clickableHtml.getSpanFlags(urlSpan);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if(urlSpan.getURL()!=null){
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.setData(Uri.parse(urlSpan.getURL()));
                    getContext().startActivity(intent);
                }
            }
        };
        clickableHtml.setSpan(clickableSpan, start, end, flags);
    }

    public CharSequence getClickableHtml(String text) {
        Spanned spannedHtml = Html.fromHtml(text);
        SpannableStringBuilder clickableHtmlBuilder = new SpannableStringBuilder(spannedHtml);
        URLSpan[] urls = clickableHtmlBuilder.getSpans(0, spannedHtml.length(), URLSpan.class);
        for (final URLSpan span : urls){
            setLinkClickable(clickableHtmlBuilder, span);
        }
        return clickableHtmlBuilder;
    }

}
