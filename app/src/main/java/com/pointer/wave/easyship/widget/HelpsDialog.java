package com.pointer.wave.easyship.widget;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.github.mmin18.widget.RealtimeBlurView;
import com.google.gson.Gson;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.enums.PopupAnimation;
import com.pointer.wave.easyship.R;
import com.pointer.wave.easyship.pojo.TipsBen;
import com.pointer.wave.easyship.utils.HttpUtils;
import com.pointer.wave.easyship.widget.adapter.HelpsListAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HelpsDialog extends BottomPopupView {

    public static AppCompatActivity activity;

    @SuppressLint("StaticFieldLeak")
    public static HelpsDialog dialog;
    public HelpsDialog(@NonNull Context context, List<String> list) {
        super(context);
        dialog = this;
        this.list = list;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_full_base;
    }

    public static void showDialog(AppCompatActivity appCompatActivity, List<String> list){
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
                .asCustom(new HelpsDialog(appCompatActivity, list))
                .show();
    }

    private LinearLayout relativeLayout;
    private View inflate;
    private List<String> list = new ArrayList<>();
    private HelpsListAdapter adapter;
    @Override
    protected void onCreate() {
        super.onCreate();
        relativeLayout = findViewById(R.id.root_view);
        inflate = View.inflate(getContext(), R.layout.dialog_helps, null);
        addChildView(inflate);
        RealtimeBlurView blurView = findViewById(R.id.blur_view);
        translation(blurView);

        RecyclerView recyclerView = inflate.findViewById(R.id.list_view);
        adapter = new HelpsListAdapter(list, activity);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

        EditText editText = inflate.findViewById(R.id.editTextTextPersonName);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString());
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void search(String text) {
        new AsyncTask<String, Integer, List<String>>(){

            @Override
            protected List<String> doInBackground(String... strings) {
                List<String> arrayList = new ArrayList<>();
                list.forEach((item)->{
                    for (String string : strings) {
                        if (item.contains(string)){
                            arrayList.add(item);
                        }
                    }
                });
                return arrayList;
            }

            @Override
            protected void onPostExecute(List<String> s) {
                super.onPostExecute(s);
                adapter.updateList(s);
            }
        }.execute(text);
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

}
