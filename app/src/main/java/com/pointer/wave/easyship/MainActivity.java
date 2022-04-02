package com.pointer.wave.easyship;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.pointer.wave.easyship.common.activity.BaseActivity;
import com.pointer.wave.easyship.fragments.AboutFragment;
import com.pointer.wave.easyship.fragments.HomeFragment;
import com.pointer.wave.easyship.pojo.TipsBen;
import com.pointer.wave.easyship.pojo.VersionBen;
import com.pointer.wave.easyship.utils.AndroidInfo;
import com.pointer.wave.easyship.utils.HttpUtils;
import com.pointer.wave.easyship.widget.NavigationBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends BaseActivity {

    private List<Fragment> fragments = new ArrayList<>();
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setDoubleTouchClose(true);
        fragments.add(HomeFragment.newInstance());
        fragments.add(AboutFragment.newInstance());
        replaceFragment(fragments.get(0));
        NavigationBar navigationBar = findViewById(R.id.main_navigation);
        navigationBar.bindData(new String[]{ "开始", "关于" }, new int[] { R.mipmap.ic_home, R.mipmap.ic_settings });
        navigationBar.setPositionListener((view, position) -> {
            replaceFragment(fragments.get(position));
        });

    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.fragment_slide_show, R.animator.fragment_slide_hide);
        fragmentTransaction.replace(R.id.main_frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private boolean isCancel;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isCancel){
            isCancel = true;
            HttpUtils httpUtils = new HttpUtils();
            Call post = httpUtils.post("http://ly.lumnytool.club/api/read.php", new String[]{ "id=103169318", "api=easy_ship", "dir=update", "name=update.txt" });
            post.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    boolean successful = response.isSuccessful();
                    assert response.body() != null;
                    String string = response.body().string();
                    new Handler(getMainLooper()).post(()->{
                        if (successful){
                            Gson gson = new Gson();
                            TipsBen tipsBen = gson.fromJson(string, TipsBen.class);
                            VersionBen versionBen = gson.fromJson(tipsBen.getContent(), VersionBen.class);
                            AndroidInfo androidInfo = new AndroidInfo(mContext);
                            if (Integer.parseInt(versionBen.getVersionCode()) > androidInfo.getVersionCode()){
                                new XPopup.Builder(mContext)
                                        .isDestroyOnDismiss(true)
                                        .asConfirm("有更新啦~", versionBen.getUpdateContent(),
                                                "知道了", "去更新",
                                                () -> {
                                                    Intent intent = new Intent();
                                                    intent.setAction("android.intent.action.VIEW");
                                                    intent.setData(Uri.parse(versionBen.getDownloadUrl()));
                                                    startActivity(intent);
                                                }, null, false).show();
                            }
                        }
                    });
                }
            });
        }
    }
}
