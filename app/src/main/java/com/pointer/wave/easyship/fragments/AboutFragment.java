package com.pointer.wave.easyship.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.pointer.wave.easyship.R;
import com.pointer.wave.easyship.pojo.TipsBen;
import com.pointer.wave.easyship.pojo.VersionBen;
import com.pointer.wave.easyship.utils.AndroidInfo;
import com.pointer.wave.easyship.utils.HttpUtils;
import com.pointer.wave.easyship.widget.feedback.TouchFeedback;

import java.io.IOException;
import java.net.URISyntaxException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AboutFragment extends Fragment implements TouchFeedback.OnFeedBackListener {

    public AboutFragment() {
        // Required empty public constructor
    }

    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_about, container, false);
        TouchFeedback touchFeedback = TouchFeedback.newInstance(getContext());
        //初始化标题
        AndroidInfo androidInfo = new AndroidInfo(getContext());
        inflate.<TextView>findViewById(R.id.about_app_info).setText(getContext().getString(R.string.app_name) + "  V " + androidInfo.getVerName());
        inflate.<TextView>findViewById(R.id.version_text).setText("当前：V " + androidInfo.getVerName() + "（" + androidInfo.getVersionCode() + "）");
        touchFeedback.setOnFeedBackListener(this, inflate.findViewById(R.id.group));
        touchFeedback.setOnFeedBackListener(this, inflate.findViewById(R.id.developer));
        touchFeedback.setOnFeedBackListener(this, inflate.findViewById(R.id.email));
        touchFeedback.setOnFeedBackListener(this, inflate.findViewById(R.id.coolApk));
        touchFeedback.setOnFeedBackListener(this, inflate.findViewById(R.id.version));
        touchFeedback.setOnFeedBackListener(this, inflate.findViewById(R.id.paper));
        touchFeedback.setOnFeedBackListener(this, inflate.findViewById(R.id.reward));

        return inflate;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.group:
                break;
            case R.id.developer:
                String qqUrl = "mqqwpa://im/chat?chat_type=wpa&version=1&uin=2205903933";
                Intent qqIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(qqUrl));
                qqIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(qqIntent);
                break;
            case R.id.email:
                String[] mailto = { "2205903933@qq.com" };
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
                LoadingPopupView loadingPopupView = (LoadingPopupView) new XPopup.Builder(getContext())
                        .dismissOnBackPressed(false)
                        .isLightNavigationBar(true)
                        .isViewMode(true)
                        .asLoading("Loading...")
                        .show();
                HttpUtils httpUtils = new HttpUtils();
                Call post = httpUtils.post("http://ly.lumnytool.club/api/read.php", new String[]{ "id=103169318", "api=easy_ship", "dir=update", "name=update.txt" });
                post.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        new Handler(getContext().getMainLooper()).post(()->{
                            loadingPopupView.delayDismissWith(0, new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
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
                                VersionBen versionBen = gson.fromJson(tipsBen.getContent(), VersionBen.class);
                                AndroidInfo androidInfo = new AndroidInfo(getContext());
                                if (Integer.parseInt(versionBen.getVersionCode()) > androidInfo.getVersionCode()){
                                    new XPopup.Builder(getContext())
                                            .isDestroyOnDismiss(true)
                                            .asConfirm("有更新啦~", versionBen.getUpdateContent(),
                                                    "知道了", "去更新",
                                                    () -> {
                                                        Intent intent = new Intent();
                                                        intent.setAction("android.intent.action.VIEW");
                                                        intent.setData(Uri.parse(versionBen.getDownloadUrl()));
                                                        getContext().startActivity(intent);
                                                    }, null, false).show();
                                }else {
                                    Toast.makeText(getContext(), "已是最新版", Toast.LENGTH_SHORT).show();
                                }
                                loadingPopupView.delayDismissWith(0, new Runnable() {
                                    @Override
                                    public void run() {
                                    }
                                });
                            }else {
                                loadingPopupView.delayDismissWith(0, new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getContext(), "读取更新信息失败", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                });
                break;
            case R.id.paper:
                break;
            case R.id.reward:
                try {
                    Intent intent = Intent.parseUri("intent://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=https://qr.alipay.com/fkx18765dkdsdimgd4tyjd9%3F_s%3Dweb-other&_t=1472443966571#Intent;scheme=alipayqr;package=com.eg.android.AlipayGphone;end", Intent.URI_INTENT_SCHEME );
                    startActivity(intent);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getContext(), "调用支付宝失败", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onLongClick(View view) {

    }
}