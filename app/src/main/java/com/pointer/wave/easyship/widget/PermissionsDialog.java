package com.pointer.wave.easyship.widget;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mmin18.widget.RealtimeBlurView;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.enums.PopupAnimation;
import com.pointer.wave.easyship.R;
import com.pointer.wave.easyship.common.activity.BaseActivity;

public class PermissionsDialog extends BottomPopupView {

    public static AppCompatActivity activity;

    @SuppressLint("StaticFieldLeak")
    public static PermissionsDialog dialog;

    public PermissionsDialog(@NonNull Context context) {
        super(context);
        dialog = this;
    }

    private int icon;
    private String permissionName;
    private String permissionContent;
    private int subIcon;
    private String subContent;
    private String subTips;
    private View.OnClickListener cancel;
    private View.OnClickListener confirm;
    public PermissionsDialog(Context context, int icon, String permissionName, String permissionContent, int subIcon, String subContent, String subTips, View.OnClickListener cancel, View.OnClickListener confirm){
        super(context);
        dialog = this;
        this.icon = icon;
        this.permissionName = permissionName;
        this.permissionContent = permissionContent;
        this.subIcon = subIcon;
        this.subContent = subContent;
        this.subTips = subTips;
        this.cancel = cancel;
        this.confirm = confirm;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_full_base;
    }

    public static void showDialog(AppCompatActivity appCompatActivity){
        activity = appCompatActivity;
        new XPopup.Builder(appCompatActivity)
                .animationDuration(400)
                .dismissOnTouchOutside(false)
                .dismissOnBackPressed(true)
                .hasShadowBg(false)
                .enableDrag(false)
                .hasBlurBg(false)
                .isViewMode(true)
                .popupAnimation(PopupAnimation.ScaleAlphaFromCenter)
                .moveUpToKeyboard(false) //如果不加这个，评论弹窗会移动到软键盘上面
//                        .enableDrag(false)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
//                        .isThreeDrag(true) //是否开启三阶拖拽，如果设置enableDrag(false)则无效
                .asCustom(new PermissionsDialog(appCompatActivity))
                .show();
    }

    public static void showDialog(AppCompatActivity appCompatActivity, int icon, String permissionName, String permissionContent, int subIcon, String subContent, String subTips, View.OnClickListener cancel, View.OnClickListener confirm){
        PermissionsDialog popupView = new PermissionsDialog(appCompatActivity, icon, permissionName, permissionContent, subIcon, subContent, subTips, cancel, confirm);
        new XPopup.Builder(appCompatActivity)
                .animationDuration(400)
                .dismissOnTouchOutside(false)
                .dismissOnBackPressed(true)
                .hasShadowBg(false)
                .enableDrag(false)
                .hasBlurBg(false)
                .isViewMode(true)
                .popupAnimation(PopupAnimation.ScaleAlphaFromCenter)
                .moveUpToKeyboard(false) //如果不加这个，评论弹窗会移动到软键盘上面
//                        .enableDrag(false)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
//                        .isThreeDrag(true) //是否开启三阶拖拽，如果设置enableDrag(false)则无效
                .asCustom(popupView)
                .show();
    }

    private LinearLayout relativeLayout;
    private View inflate;
    @Override
    protected void onCreate() {
        super.onCreate();
        relativeLayout = findViewById(R.id.root_view);
        inflate = View.inflate(getContext(), R.layout.dialog_permissin_write, null);
        addChildView(inflate);
        RealtimeBlurView blurView = findViewById(R.id.blur_view);
        translation(blurView);

        inflate.findViewById(R.id.cancel_button).setOnClickListener((view)->{
           dismiss();
        });

        inflate.findViewById(R.id.confirm_button).setOnClickListener((view)->{
            ((BaseActivity) activity).getWritePermission();
            dismiss();
        });

        if (icon!=0) inflate.<ImageView>findViewById(R.id.icon).setImageResource(icon);
        if (subIcon!=0) inflate.<ImageView>findViewById(R.id.sub_icon).setImageResource(subIcon);
        if (permissionName!=null) inflate.<TextView>findViewById(R.id.name).setText(permissionName);
        if (permissionContent!=null) inflate.<TextView>findViewById(R.id.content).setText(permissionContent);
        if (subContent!=null) inflate.<TextView>findViewById(R.id.sub_content).setText(subContent);
        if (subTips!=null) inflate.<TextView>findViewById(R.id.sub_tips).setText(subTips);

        if (cancel!=null) setCancelClickListener(cancel);
        if (confirm!=null) setConfirmClickListener(confirm);
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

    public void setCancelClickListener(View.OnClickListener cancelClickListener){
        inflate.findViewById(R.id.cancel_button).setOnClickListener(cancelClickListener);
    }

    public void setConfirmClickListener(View.OnClickListener confirmClickListener){
        inflate.findViewById(R.id.confirm_button).setOnClickListener(confirmClickListener);
    }

}
