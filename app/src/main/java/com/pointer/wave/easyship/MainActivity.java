package com.pointer.wave.easyship;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.Gson;
import com.lxj.xpopup.XPopup;
import com.pointer.wave.easyship.common.activity.BaseActivity;
import com.pointer.wave.easyship.core.ZoomInTransformer;
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

        fragments.add(new HomeFragment());
        fragments.add(new AboutFragment());

        ViewPager2 pager = findViewById(R.id.pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(fragments,this);
        NavigationBar navigationBar = findViewById(R.id.main_navigation);
        pager.setAdapter(adapter);
        pager.setUserInputEnabled(false);
        // 不需要 ViewPager 动画的话可以删除这一行
        pager.setPageTransformer(new ZoomInTransformer());

        navigationBar.bindData(new String[]{ "开始", "关于" }, new int[] { R.mipmap.ic_home, R.mipmap.ic_settings });
        navigationBar.setPositionListener((view, position) -> {
            pager.setCurrentItem(position);
        });
        // FIXME: 2022/4/4 本来我是想 ViewPager 滑动的时候切换下面的 Tab，但是 Tab 上面那个有动画的 View 似乎没效果，我也不清楚应该怎么修复，就暂时禁用了
//        pager.registerOnPageChangeCallback((new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                super.onPageSelected(position);
//                navigationBar.setCurrentItem(position);
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//                super.onPageScrollStateChanged(state);
//            }
//        }));
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

    class ViewPagerAdapter extends FragmentStateAdapter{

        private final List<Fragment> fragments;

        ViewPagerAdapter(List<Fragment> fragments,FragmentActivity activity){
            super(activity);
            this.fragments = fragments;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }
    }
}
