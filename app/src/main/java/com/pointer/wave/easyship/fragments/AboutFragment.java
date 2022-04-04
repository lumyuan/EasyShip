package com.pointer.wave.easyship.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.pointer.wave.easyship.R;
import com.pointer.wave.easyship.base.BaseFragment;
import com.pointer.wave.easyship.common.activity.BaseActivity;
import com.pointer.wave.easyship.pojo.TipsBen;
import com.pointer.wave.easyship.pojo.UpdateLogBen;
import com.pointer.wave.easyship.pojo.VersionBen;
import com.pointer.wave.easyship.utils.AndroidInfo;
import com.pointer.wave.easyship.utils.HttpUtils;
import com.pointer.wave.easyship.widget.UpdateLogDialog;
import com.pointer.wave.easyship.widget.feedback.TouchFeedback;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AboutFragment extends BaseFragment implements TouchFeedback.OnFeedBackListener {
    private BaseActivity activity;


    public AboutFragment() {
        super(R.layout.fragment_about);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void initView(View root) {
        super.initView(root);
        TouchFeedback touchFeedback = TouchFeedback.newInstance(requireActivity());
        //初始化标题
        AndroidInfo androidInfo = new AndroidInfo(requireActivity());
        root.<TextView>findViewById(R.id.about_app_info).setText(requireActivity().getString(R.string.app_name) + "  V " + androidInfo.getVerName());
        root.<TextView>findViewById(R.id.version_text).setText("当前：V " + androidInfo.getVerName() + "（" + androidInfo.getVersionCode() + "）");
        touchFeedback.setOnFeedBackListener(this, root.findViewById(R.id.group));
        touchFeedback.setOnFeedBackListener(this, root.findViewById(R.id.developer));
        touchFeedback.setOnFeedBackListener(this, root.findViewById(R.id.email));
        touchFeedback.setOnFeedBackListener(this, root.findViewById(R.id.coolApk));
        touchFeedback.setOnFeedBackListener(this, root.findViewById(R.id.version));
        touchFeedback.setOnFeedBackListener(this, root.findViewById(R.id.paper));
        touchFeedback.setOnFeedBackListener(this, root.findViewById(R.id.reward));
    }

    @Override
    protected void loadSingleData() {
        super.loadSingleData();
        activity = (BaseActivity) requireActivity();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.group:
                String url = "https://github.com/LumnyTool/EasyShip";
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setData(Uri.parse(url));
                requireActivity().startActivity(intent);
                break;
            case R.id.developer:
                String qqUrl = "mqqwpa://im/chat?chat_type=wpa&version=1&uin=2205903933";
                Intent qqIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(qqUrl));
                qqIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(qqIntent);
                break;
            case R.id.email:
                String[] mailto = {"2205903933@qq.com"};
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                String emailBody = "";
                sendIntent.setType("message/rfc822");
                sendIntent.putExtra(Intent.EXTRA_EMAIL, mailto);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                sendIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
                startActivity(sendIntent);
                break;
            case R.id.coolApk:
                Intent coolapkIntent = new Intent();
                coolapkIntent.setClassName("com.coolapk.market", "com.coolapk.market.view.AppLinkActivity");
                coolapkIntent.setAction("android.intent.action.VIEW");
                coolapkIntent.setData(Uri.parse("http://www.coolapk.com/u/2073264"));
                startActivity(coolapkIntent);
                break;
            case R.id.version:
                LoadingPopupView loadingPopupView = (LoadingPopupView) new XPopup.Builder(requireActivity())
                        .dismissOnBackPressed(false)
                        .isLightNavigationBar(true)
                        .isViewMode(true)
                        .asLoading("Loading...")
                        .show();
                HttpUtils httpUtils = new HttpUtils();
                Call post = httpUtils.post("http://ly.lumnytool.club/api/read.php", new String[]{"id=103169318", "api=easy_ship", "dir=update", "name=update.txt"});
                post.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        new Handler(requireActivity().getMainLooper()).post(() -> loadingPopupView.delayDismissWith(0, () -> Toast.makeText(requireActivity(), e.toString(), Toast.LENGTH_SHORT).show()));
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        boolean successful = response.isSuccessful();
                        assert response.body() != null;
                        String string = response.body().string();
                        new Handler(requireActivity().getMainLooper()).post(() -> {
                            if (successful) {
                                Gson gson = new Gson();
                                TipsBen tipsBen = gson.fromJson(string, TipsBen.class);
                                VersionBen versionBen = gson.fromJson(tipsBen.getContent(), VersionBen.class);
                                AndroidInfo androidInfo = new AndroidInfo(requireActivity());
                                if (Integer.parseInt(versionBen.getVersionCode()) > androidInfo.getVersionCode()) {
                                    new XPopup.Builder(requireActivity())
                                            .isDestroyOnDismiss(true)
                                            .asConfirm("有更新啦~", versionBen.getUpdateContent(),
                                                    "知道了", "去更新",
                                                    () -> {
                                                        Intent intent = new Intent();
                                                        intent.setAction("android.intent.action.VIEW");
                                                        intent.setData(Uri.parse(versionBen.getDownloadUrl()));
                                                        requireActivity().startActivity(intent);
                                                    }, null, false).show();
                                } else {
                                    Toast.makeText(requireActivity(), "已是最新版", Toast.LENGTH_SHORT).show();
                                }
                                loadingPopupView.delayDismissWith(0, () -> {
                                });
                            } else {
                                loadingPopupView.delayDismissWith(0, () -> Toast.makeText(requireActivity(), "读取更新信息失败", Toast.LENGTH_SHORT).show());
                            }
                        });
                    }
                });
                break;
            case R.id.paper:
                LoadingPopupView updateLogLoad = (LoadingPopupView) new XPopup.Builder(requireActivity())
                        .dismissOnBackPressed(false)
                        .isLightNavigationBar(true)
                        .isViewMode(true)
                        .asLoading("Loading...")
                        .show();
                HttpUtils updateLogHttp = new HttpUtils();
                Call updateLogPost = updateLogHttp.post("http://ly.lumnytool.club/api/list_dir.php", new String[]{"id=103169318", "api=easy_ship", "dir=update_logs", "m=false"});
                updateLogPost.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        new Handler(requireActivity().getMainLooper()).post(() -> updateLogLoad.delayDismissWith(0, () -> Toast.makeText(requireActivity(), e.toString(), Toast.LENGTH_SHORT).show()));
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        boolean successful = response.isSuccessful();
                        assert response.body() != null;
                        String string = response.body().string();
                        new Handler(requireActivity().getMainLooper()).post(() -> {
                            if (successful) {
                                String[] split = string.split("<br>");
                                Gson gson = new Gson();
                                StringBuilder stringBuilder = new StringBuilder();
                                for (String json :
                                        split) {
                                    json = json.replace("\n", "<br>");
                                    TipsBen tipsBen = gson.fromJson(json, TipsBen.class);
                                    UpdateLogBen log = gson.fromJson(tipsBen.getContent(), UpdateLogBen.class);
                                    stringBuilder.append(log.toString());
                                }
                                updateLogLoad.delayDismissWith(0, () -> UpdateLogDialog.showDialog(activity, stringBuilder.toString()));
                            } else {
                                updateLogLoad.delayDismissWith(0, () -> Toast.makeText(requireActivity(), "读取更新信息失败", Toast.LENGTH_SHORT).show());
                            }
                        });
                    }
                });
                break;
            case R.id.reward:
                try {
                    Intent payIntent = Intent.parseUri("intent://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=https://qr.alipay.com/fkx18765dkdsdimgd4tyjd9%3F_s%3Dweb-other&_t=1472443966571#Intent;scheme=alipayqr;package=com.eg.android.AlipayGphone;end", Intent.URI_INTENT_SCHEME);
                    startActivity(payIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(requireActivity(), "调用支付宝失败", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onLongClick(View view) {

    }
}
