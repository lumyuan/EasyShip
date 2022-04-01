package com.pointer.wave.easyship.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.Settings;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnCancelListener;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.pointer.wave.easyship.EasyShip;
import com.pointer.wave.easyship.FlashActivity;
import com.pointer.wave.easyship.R;
import com.pointer.wave.easyship.common.activity.BaseActivity;
import com.pointer.wave.easyship.pojo.TipsBen;
import com.pointer.wave.easyship.utils.AndroidInfo;
import com.pointer.wave.easyship.utils.ColorChangeUtils;
import com.pointer.wave.easyship.utils.HttpUtils;
import com.pointer.wave.easyship.widget.PermissionsDialog;
import com.pointer.wave.easyship.widget.feedback.TouchFeedback;

import java.io.IOException;
import java.io.InputStream;

import me.itangqi.waveloadingview.WaveLoadingView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeFragment extends Fragment implements TouchFeedback.OnFeedBackListener {

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private BaseActivity activity;

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        this.activity = (BaseActivity) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        TouchFeedback touchFeedback = TouchFeedback.newInstance(getContext());
        View inflate = inflater.inflate(R.layout.fragment_home, container, false);
        WaveLoadingView waveLoadingView = inflate.findViewById(R.id.wave_view);
        waveLoadingView.setWaveShiftRatio(0.75f);
        View bgView = inflate.findViewById(R.id.bg_view);
        bgView.postDelayed(()->{
            bgView.setLayoutParams(new RelativeLayout.LayoutParams(-1, waveLoadingView.getHeight()));
            new ColorChangeUtils(new int[] {
                    getActivity().getColor(R.color.red),
                    getActivity().getColor(R.color.orange),
                    getActivity().getColor(R.color.yellow),
                    getActivity().getColor(R.color.green),
                    getActivity().getColor(R.color.teal),
                    getActivity().getColor(R.color.blue),
                    getActivity().getColor(R.color.purple),
                    getActivity().getColor(R.color.pink) },
                    bgView).startAnimation();
        },0);

        //初始化设备信息
        AndroidInfo androidInfo = new AndroidInfo(getContext());
        inflate.<TextView>findViewById(R.id.main_device_info).setText("设备型号：" + androidInfo.getBrand() + " " + androidInfo.getModel() + "    V-AB：" + String.valueOf(EasyShip.isVab).toUpperCase());

        final TextView mainTips = inflate.findViewById(R.id.main_tips);
        HttpUtils httpUtils = new HttpUtils();
        Call post = httpUtils.post("http://ly.lumnytool.club/api/read.php", new String[]{ "id=103169318", "api=easy_ship", "dir=home_tips", "name=tips.txt" });
        post.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(getContext().getMainLooper()).post(()->{
                    mainTips.setText(getClickableHtml(getContext().getString(R.string.main_tips)));
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                boolean successful = response.isSuccessful();
                assert response.body() != null;
                String string = response.body().string();
                new Handler(getContext().getMainLooper()).post(()->{
                    if (successful){
                        Gson gson = new Gson();
                        TipsBen tipsBen = gson.fromJson(string, TipsBen.class);
                        mainTips.setText(getClickableHtml(tipsBen.getContent().replace("\n", "<br>")));
                    }else {
                        mainTips.setText(getClickableHtml(getContext().getString(R.string.main_tips)));
                    }
                });
            }
        });

        mainTips.setMovementMethod(LinkMovementMethod.getInstance());

        touchFeedback.setOnFeedBackListener(this, inflate.findViewById(R.id.help_button));
        touchFeedback.setOnFeedBackListener(this, inflate.findViewById(R.id.waring_button));
        touchFeedback.setOnFeedBackListener(this, inflate.findViewById(R.id.start_button));
        touchFeedback.setOnFeedBackListener(this, inflate.findViewById(R.id.download_button));
        touchFeedback.setOnFeedBackListener(this, inflate.findViewById(R.id.home_tips_card));
        return inflate;
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.help_button:
                new XPopup.Builder(getContext())
                        .isDestroyOnDismiss(true)
                        .asConfirm("使用方法", "准备工作：\n下载好与本设备对应的刷机包，并且给软件授予相应的权限\n\n使用方法：\n1. 点击”开始更新“进入刷机界面\n2. 点击”选择“按钮选择下载好的刷机包\n3. 点击”开始刷机“按钮启动刷机服务并等待服务结束\n4. 根据提示安装面具\n5. 重启手机即可",
                                "", "知道了", null, null, true).show();
                break;
            case R.id.waring_button:
                new XPopup.Builder(getContext())
                        .isDestroyOnDismiss(true)
                        .asConfirm("注意事项", "1. 在刷机服务运行时尽量不要关闭软件\n2. 在刷机服务运行时不要将手机关机或重启",
                                "", "知道了", null, null, true).show();
                break;
            case R.id.start_button:
                if (!EasyShip.isVab){
                    new XPopup.Builder(getContext())
                            .isDestroyOnDismiss(true)
                            .asConfirm("提示", "本软件只支持V-AB分区的设备进行ROM刷写，不支持当前设备~",
                                    "关闭软件", "知道了",
                                    null, () -> {
                                        activity.finish();
                                    }, false).show();
                }else if (!activity.hasWritePermission()){
                    PermissionsDialog.showDialog(activity);
                } else {
                    startActivity(new Intent(getActivity(), FlashActivity.class));
                }
                break;
            case R.id.download_button:

                break;
        }
    }

    @Override
    public void onLongClick(View view) {

    }
}