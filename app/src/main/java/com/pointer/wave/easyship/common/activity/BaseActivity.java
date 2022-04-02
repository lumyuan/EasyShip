package com.pointer.wave.easyship.common.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.gyf.barlibrary.ImmersionBar;
import com.pointer.wave.easyship.EasyShip;
import com.pointer.wave.easyship.interfaces.OnNavigationStateListener;
import com.pointer.wave.easyship.widget.feedback.TouchFeedback;

import java.util.ArrayList;

public abstract class BaseActivity extends AppCompatActivity{

    @SuppressLint("StaticFieldLeak")
    public static AppCompatActivity mContext;

    private ArrayList<String> mPermissionList;
    String[] permissions = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE };

    public ArrayList<View> feedViews = new ArrayList<>();

    private TouchFeedback touchFeedback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPermissionList = new ArrayList<>();
        mContext = this;
        touchFeedback = TouchFeedback.newInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ImmersionBar.with(this)
                .transparentStatusBar()
                .statusBarDarkFont(true)
                .init();
    }

    public boolean isNightMode(){
        return (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public Context getApp() {
        return EasyShip.getContext();
    }

    public void getWritePermission(){
        //6.0才用动态权限
        //逐个判断你要的权限是否已经通过
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);//添加还未授予的权限
            }
        }
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            int mRequestCode = 100;
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        }
    }

    public boolean hasWritePermission(){
        //6.0才用动态权限
        //逐个判断你要的权限是否已经通过
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);//添加还未授予的权限
            }
        }
        return mPermissionList.size() <= 0;
    }

    public void getAllWritePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()){
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
    }

    public boolean hasAllWritePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        }else {
            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void getPrivateDirPermission(){
        Uri uri1 = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata");
        DocumentFile documentFile = DocumentFile.fromTreeUri(this, uri1);
        Intent intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        assert documentFile != null;
        intent1.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentFile.getUri());
        startActivityForResult(intent1, 11);
    }

    public boolean hasDataPermission(){
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) && hasPermissions();
    }

    private boolean hasPermissions() {
        for (UriPermission persistedUriPermission : getContentResolver().getPersistedUriPermissions()) {
            if (persistedUriPermission.isReadPermission() && persistedUriPermission.getUri().toString().equals("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionList.clear();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);//添加还未授予的权限
            }
        }
    }


    @SuppressLint("WrongConstant")
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri;

        if (data == null) {
            return;
        }

        if (requestCode == 11 && (uri = data.getData()) != null) {
            getContentResolver().takePersistableUriPermission(uri, data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
        }

    }

    @Override
    public void onBackPressed() {
        if (isOpenDoubleTouchClose){
            if (System.currentTimeMillis() - time > 2000) {
                Toast.makeText(this, "再按一次退出软件", Toast.LENGTH_SHORT).show();
                time = System.currentTimeMillis();
            } else {
                finish();
                super.onBackPressed();
            }
        }else {
            finish();
            super.onBackPressed();
        }
    }

    private boolean isOpenDoubleTouchClose = false;
    private long time;

    public void setDoubleTouchClose(boolean isOpenDoubleTouchClose){
        this.isOpenDoubleTouchClose = isOpenDoubleTouchClose;
    }

    public static void isNavigationBarExist(Activity activity, final OnNavigationStateListener onNavigationStateListener) {
        if (activity == null) {
            return;
        }
        final int height = getNavigationHeight(activity);
        activity.getWindow().getDecorView().setOnApplyWindowInsetsListener((v, windowInsets) -> {
            boolean isShowing = false;
            int b = 0;
            if (windowInsets != null) {
                b = windowInsets.getSystemWindowInsetBottom();
                isShowing = (b == height);
            }
            if (onNavigationStateListener != null && b <= height) {
                onNavigationStateListener.onNavigationState(isShowing, b);
            }
            return windowInsets;
        });
    }

    public static int getNavigationHeight(Context activity) {
        if (activity == null) {
            return 0;
        }
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        int height = 0;
        if (resourceId > 0) {
            //获取NavigationBar的高度
            height = resources.getDimensionPixelSize(resourceId);
        }
        return height;
    }
}