package com.pointer.wave.easyship.fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

import com.google.gson.Gson;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.pointer.wave.easyship.EasyShip;
import com.pointer.wave.easyship.FlashActivity;
import com.pointer.wave.easyship.R;
import com.pointer.wave.easyship.base.BaseFragment;
import com.pointer.wave.easyship.common.activity.BaseActivity;
import com.pointer.wave.easyship.pojo.TipsBen;
import com.pointer.wave.easyship.utils.AndroidInfo;
import com.pointer.wave.easyship.utils.ColorChangeUtils;
import com.pointer.wave.easyship.utils.HttpUtils;
import com.pointer.wave.easyship.widget.HelpsDialog;
import com.pointer.wave.easyship.widget.PermissionsDialog;
import com.pointer.wave.easyship.widget.feedback.TouchFeedback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.itangqi.waveloadingview.WaveLoadingView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeFragment extends BaseFragment implements TouchFeedback.OnFeedBackListener{
    private View root;
    private BaseActivity activity;

    public HomeFragment(){
        super(R.layout.fragment_home);
    }

    @Override
    protected void initView(View root) {
        super.initView(root);
        this.root = root;
        TouchFeedback touchFeedback = TouchFeedback.newInstance(requireActivity());
        WaveLoadingView waveLoadingView = root.findViewById(R.id.wave_view);
        waveLoadingView.setWaveShiftRatio(0.75f);
        View bgView = root.findViewById(R.id.bg_view);
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

        touchFeedback.setOnFeedBackListener(this, root.findViewById(R.id.help_button));
        touchFeedback.setOnFeedBackListener(this, root.findViewById(R.id.waring_button));
        touchFeedback.setOnFeedBackListener(this, root.findViewById(R.id.start_button));
        touchFeedback.setOnFeedBackListener(this, root.findViewById(R.id.download_button));
        touchFeedback.setOnFeedBackListener(this, root.findViewById(R.id.home_tips_card));

    }

    @Override
    protected void loadSingleData() {
        super.loadSingleData();
        activity = (BaseActivity) requireActivity();
        //初始化设备信息
        AndroidInfo androidInfo = new AndroidInfo(requireActivity());
        root.<TextView>findViewById(R.id.main_device_info).setText("设备型号：" + androidInfo.getBrand() + " " + androidInfo.getModel() + "    V-AB：" + String.valueOf(EasyShip.isVab).toUpperCase());

        final TextView mainTips = root.findViewById(R.id.main_tips);
        HttpUtils httpUtils = new HttpUtils();
        Call post = httpUtils.post("http://ly.lumnytool.club/api/read.php", new String[]{ "id=103169318", "api=easy_ship", "dir=home_tips", "name=tips.txt" });
        post.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                new Handler(requireActivity().getMainLooper()).post(()->{
                    mainTips.setText(getClickableHtml(requireActivity().getString(R.string.main_tips)));
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                boolean successful = response.isSuccessful();
                assert response.body() != null;
                String string = response.body().string();
                new Handler(requireActivity().getMainLooper()).post(()->{
                    if (successful){
                        Gson gson = new Gson();
                        TipsBen tipsBen = gson.fromJson(string, TipsBen.class);
                        mainTips.setText(getClickableHtml(tipsBen.getContent().replace("\n", "<br>")));
                    }else {
                        mainTips.setText(getClickableHtml(requireActivity().getString(R.string.main_tips)));
                    }
                });
            }
        });

        mainTips.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private CharSequence getClickableHtml(String text) {
        Spanned spannedHtml = Html.fromHtml(text);
        SpannableStringBuilder clickableHtmlBuilder = new SpannableStringBuilder(spannedHtml);
        URLSpan[] urls = clickableHtmlBuilder.getSpans(0, spannedHtml.length(), URLSpan.class);
        for (final URLSpan span : urls){
            setLinkClickable(clickableHtmlBuilder, span);
        }
        return clickableHtmlBuilder;
    }

    private void setLinkClickable(SpannableStringBuilder clickableHtml, URLSpan urlSpan) {
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
                    requireActivity().startActivity(intent);
                }
            }
        };
        clickableHtml.setSpan(clickableSpan, start, end, flags);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.help_button:
                new XPopup.Builder(requireActivity())
                        .isDestroyOnDismiss(true)
                        .asConfirm("使用方法", "准备工作：\n下载好与本设备对应的刷机包，并且给软件授予相应的权限\n\n使用方法：\n1. 点击”开始更新“进入刷机界面\n2. 点击”选择“按钮选择下载好的刷机包\n3. 点击”开始刷机“按钮启动刷机服务并等待服务结束\n4. 根据提示安装面具\n5. 重启手机即可",
                                "", "知道了", null, null, true).show();
                break;
            case R.id.waring_button:
                LoadingPopupView loadingPopupView = (LoadingPopupView) new XPopup.Builder(requireActivity())
                        .dismissOnBackPressed(false)
                        .isLightNavigationBar(true)
                        .isViewMode(true)
                        .asLoading("Loading...")
                        .show();
                HttpUtils httpUtils = new HttpUtils();
                Call post = httpUtils.post("http://ly.lumnytool.club/api/list_dir.php", new String[]{ "id=103169318", "api=easy_ship", "dir=helps", "m=false" });
                post.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        new Handler(activity.getMainLooper()).post(()->{
                            loadingPopupView.delayDismissWith(0, () -> Toast.makeText(requireActivity(), e.toString(), Toast.LENGTH_SHORT).show());
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        boolean successful = response.isSuccessful();
                        assert response.body() != null;
                        String string = response.body().string();
                        new Handler(activity.getMainLooper()).post(()->{
                            if (successful){
                                List<String> list = new ArrayList<>();
                                String[] jsonArray = string.split("<br>");
                                list.addAll(Arrays.asList(jsonArray));
                                HelpsDialog.showDialog(activity, list);
                                loadingPopupView.delayDismissWith(0, () -> {
                                });
                            }else {
                                loadingPopupView.delayDismissWith(0, () -> Toast.makeText(requireActivity(), "无法连接服务器", Toast.LENGTH_SHORT).show());
                            }
                        });
                    }
                });

                break;
            case R.id.start_button:
                if (!EasyShip.isVab){
                    new XPopup.Builder(requireActivity())
                            .isDestroyOnDismiss(true)
                            .asConfirm("提示", "本软件只支持V-AB分区的设备进行ROM刷写，不支持当前设备~",
                                    "关闭软件", "知道了",
                                    null, () -> {
                                        activity.finish();
                                    }, false).show();
                }else if (!activity.hasWritePermission()){
                    PermissionsDialog.showDialog(activity);
                } else {
                    ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            requireActivity(), new Pair<>(root.<TextView>findViewById(R.id.start_button), "start_button"));
                    Intent intent = new Intent(requireActivity(), FlashActivity.class);
                    ActivityCompat.startActivity(requireActivity(), intent, activityOptions.toBundle());
                    //startActivity(new Intent(getActivity(), FlashActivity.class));
                }
                break;
            case R.id.download_button:
                String device = Build.DEVICE;
                new XPopup.Builder(requireActivity())
                        .isDestroyOnDismiss(true)
                        .asConfirm("温馨提示", "当前ROM下载功能由第三方网站提供，所以ROM历代版本可能会有遗漏，请以网站实际情况为准",
                                "网站①", "网站②", () -> {
                                    String url = "https://xiaomirom.com/series/" + (device.equals("mars") ? "star" : device) + "/";
                                    href(url);
                                }, () ->  {
                                    String url = "http://miui.511i.cn/";
                                    href(url);
                                }, false).show();
                break;
        }
    }

    private void href(String url){
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse(url));
        requireActivity().startActivity(intent);
    }

    @Override
    public void onLongClick(View view) {

    }
}
