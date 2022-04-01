package com.pointer.wave.easyship;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;
import com.pointer.wave.easyship.common.activity.BaseActivity;
import com.pointer.wave.easyship.editor.TextEditor;
import com.pointer.wave.easyship.pojo.TipsBen;
import com.pointer.wave.easyship.services.RunTimeService;
import com.pointer.wave.easyship.utils.DensityUtil;
import com.pointer.wave.easyship.utils.HttpUtils;
import com.pointer.wave.easyship.widget.PermissionsDialog;
import com.pointer.wave.easyship.widget.feedback.TouchFeedback;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import me.itangqi.waveloadingview.WaveLoadingView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FlashActivity extends BaseActivity implements TouchFeedback.OnFeedBackListener {

    private static final int IMPORT_REQUEST_CODE = 316;
    private EditText pathEditor;
    private CardView selectButton;
    @SuppressLint("StaticFieldLeak") public static TextView startFlash;
    private TextView tipsTitle;

    private static CardView handCard;
    public static WaveLoadingView waveLoadingView;
    private Runnable runnable;
    private Handler tipsHandler;
    private final List<TipsBen> jsonList = new ArrayList<>();
    private final Random random = new Random();
    private CardView tipsCard;
    @SuppressLint("StaticFieldLeak") public static TextEditor editor;
    @SuppressLint("StaticFieldLeak") public static TextView title;

    private static int cH, cW;
    private static final long duration = 1000;
    private static int px;
    private static int cardViewHeight;

    private TouchFeedback touchFeedback;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash);
        touchFeedback = TouchFeedback.newInstance(this);

        NotificationManagerCompat notification = NotificationManagerCompat.from(this);
        boolean isEnabled = notification.areNotificationsEnabled();
        if (!isEnabled){
            PermissionsDialog.showDialog(this, R.mipmap.ic_notification, "通知权限申请", this.getString(R.string.app_name) + "在刷机过程中会在通知栏发送刷机进度通知，但这需要系统已允许" + this.getString(R.string.app_name) + "发送通知。",
                    R.mipmap.ic_sub_notification,
                    "用于" + this.getString(R.string.app_name) + "的发送刷机进度功能", "授予该权限后，软件可以在通知栏发送通知", null, (vw)->{
                        Intent intent = new Intent();
                        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                        intent.putExtra("android.provider.extra.APP_PACKAGE", this.getPackageName());
                        startActivity(intent);
                        if (PermissionsDialog.dialog!=null) PermissionsDialog.dialog.dismiss();
                    });
        }

        pathEditor = findViewById(R.id.rom_path);
        selectButton = findViewById(R.id.select_button);
        startFlash = findViewById(R.id.start_flash);
        tipsTitle = findViewById(R.id.tips_title);
        title = findViewById(R.id.title);

        handCard = findViewById(R.id.card_view);
        handCard.post(()->{
            cH = handCard.getHeight();
            cW = handCard.getWidth();
            px = DensityUtil.dip2px(this, 90);
            cardViewHeight = handCard.getHeight();
            if (RunTimeService.isServiceRunning(this, RunTimeService.class.getName())){
                startAnimation();
                editor.setText(EasyShip.getMsg().toString(), false);
                List<String> list = EasyShip.getMsg().getList();
                AtomicReference<String> msg = new AtomicReference<>("0");
                list.forEach((s)->{
                    log(s);
                    if (RunTimeService.isLog(s, 115) || RunTimeService.isLog_2(s, 361)){
                        msg.set(s);
                    }
                });
                pathEditor.setText(RunTimeService.work);
                startFlash.setText("刷机中，请稍候...");
                title.setText(RunTimeService.getProgress(msg.get()) + "%");
                waveLoadingView.setProgressValue(RunTimeService.getProgress(msg.get()));
            }
        });
        waveLoadingView = findViewById(R.id.wave_view);

        tipsCard = findViewById(R.id.tips_card);
        tipsCard.setVisibility(View.GONE);

        editor = findViewById(R.id.editor);

        tipsHandler = new Handler(getMainLooper());

        runnable = new Runnable() {
            @Override
            public void run() {
                String content = jsonList.get(getPosition()).getContent();
                tipsTitle.setText(content);
                tipsHandler.postDelayed(this, 10000);
            }
        };

        HttpUtils httpUtils = new HttpUtils();
        Call post = httpUtils.post("http://ly.lumnytool.club/api/list_dir.php", new String[]{ "id=103169318", "api=easy_ship", "dir=tips", "m=false" });
        post.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                boolean successful = response.isSuccessful();
                assert response.body() != null;
                String string = response.body().string();
                runOnUiThread(()->{
                    if (successful){
                        String[] jsonArray = string.split("<br>");
                        Gson gson = new Gson();
                        for (String json : jsonArray) {
                            TipsBen tipsBen = gson.fromJson(json, TipsBen.class);
                            jsonList.add(tipsBen);
                        }
                        tipsHandler.post(runnable);
                        tipsCard.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

        touchFeedback.setOnFeedBackListener(this, selectButton);
        touchFeedback.setOnFeedBackListener(this, startFlash);
        touchFeedback.setOnFeedBackListener(this, tipsCard);
    }

    private int lastPosition = 0;
    private int getPosition(){
        int i = random.nextInt(jsonList.size());
        if (i == lastPosition) return getPosition();
        lastPosition = i;
        return i;
    }

    public static void log(String log){
        if (log.contains("] ")){
            log = log.substring(log.indexOf("] ") + 2);
        }else if (log.contains("update_engine: ")){
            log = log.substring(log.indexOf("update_engine: ") + 15);
        }
        log = getDate() + ": " + log;
        if (editor == null)
            return;
        String finalLog = log;
        editor.post(()->{
            editor.append(finalLog);
        });
        System.out.println("log = " + log);
    }

    private static String getDate() {
        Date date = new Date();
        String time = date.toLocaleString();
        Log.i("md", "时间time为： " + time);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    public static void startAnimation(){
        new Handler(mContext.getMainLooper()).postDelayed(()->{
            ValueAnimator cardHeight = ValueAnimator.ofInt(cardViewHeight, cardViewHeight - px);
            cardHeight.setDuration(duration);
            cardHeight.setInterpolator(new DecelerateInterpolator());
            cardHeight.start();
            cardHeight.addUpdateListener(animation -> {
                int v = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = handCard.getLayoutParams();
                layoutParams.height = v;
                handCard.requestLayout();
            });

            ValueAnimator cardWidth = ValueAnimator.ofInt(handCard.getWidth(), cardViewHeight - px);
            cardWidth.setDuration(duration);
            cardWidth.setInterpolator(new DecelerateInterpolator());
            cardWidth.start();
            cardWidth.addUpdateListener(animation -> {
                int v = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = handCard.getLayoutParams();
                layoutParams.width = v;
                handCard.requestLayout();
            });

            ValueAnimator cardRadius = ValueAnimator.ofInt((int) handCard.getRadius(), (cardViewHeight - px) / 2);
            cardRadius.setDuration(duration);
            cardRadius.setInterpolator(new AccelerateDecelerateInterpolator());
            cardRadius.start();
            cardRadius.addUpdateListener(animation -> {
                int v = (int) animation.getAnimatedValue();
                handCard.setRadius(v);
            });

            cardRadius.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    @SuppressLint("ObjectAnimatorBinding") ObjectAnimator backgroundColor = ObjectAnimator.ofInt(waveLoadingView, "waveBgColor", mContext.getColor(R.color.indicator_color), mContext.getColor(R.color.white));
                    backgroundColor.setDuration(duration);
                    backgroundColor.setEvaluator(new ArgbEvaluator());
                    backgroundColor.start();

                    @SuppressLint("ObjectAnimatorBinding") ObjectAnimator textColor = ObjectAnimator.ofInt(title, "textColor", mContext.getColor(R.color.white), mContext.getColor(R.color.black));
                    textColor.setDuration(duration);
                    textColor.setEvaluator(new ArgbEvaluator());
                    textColor.start();

                    @SuppressLint("ObjectAnimatorBinding") ObjectAnimator waveColor = ObjectAnimator.ofInt(waveLoadingView, "waveColor", mContext.getColor(R.color.white), mContext.getColor(R.color.indicator_color));
                    waveColor.setDuration(duration);
                    waveColor.setEvaluator(new ArgbEvaluator());
                    waveColor.start();
                }
            });
        }, 0);
    }

    public static void stopAnimation(){
        new Handler(mContext.getMainLooper()).postDelayed(()->{
            ValueAnimator cardHeight = ValueAnimator.ofInt(handCard.getHeight(), cH);
            cardHeight.setDuration(duration);
            cardHeight.setInterpolator(new DecelerateInterpolator());
            cardHeight.start();
            cardHeight.addUpdateListener(animation -> {
                int v = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = handCard.getLayoutParams();
                layoutParams.height = v;
                handCard.requestLayout();
            });

            ValueAnimator cardWidth = ValueAnimator.ofInt(handCard.getWidth(), cW);
            cardWidth.setDuration(duration);
            cardWidth.setInterpolator(new DecelerateInterpolator());
            cardWidth.start();

            cardWidth.addUpdateListener(animation -> {
                int v = (int) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = handCard.getLayoutParams();
                layoutParams.width = v;
                handCard.requestLayout();
            });

            ValueAnimator cardRadius = ValueAnimator.ofInt((int) handCard.getRadius(), 0);
            cardRadius.setDuration(duration);
            cardRadius.setInterpolator(new AccelerateDecelerateInterpolator());
            cardRadius.start();
            cardRadius.addUpdateListener(animation -> {
                int v = (int) animation.getAnimatedValue();
                handCard.setRadius(v);
            });

            cardRadius.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    @SuppressLint("ObjectAnimatorBinding") ObjectAnimator backgroundColor = ObjectAnimator.ofInt(waveLoadingView, "waveBgColor", mContext.getColor(R.color.white), mContext.getColor(R.color.indicator_color));
                    backgroundColor.setDuration(duration);
                    backgroundColor.setEvaluator(new ArgbEvaluator());
                    backgroundColor.start();

                    @SuppressLint("ObjectAnimatorBinding") ObjectAnimator textColor = ObjectAnimator.ofInt(title, "textColor", mContext.getColor(R.color.black), mContext.getColor(R.color.white));
                    textColor.setDuration(duration);
                    textColor.setEvaluator(new ArgbEvaluator());
                    textColor.start();

                    @SuppressLint("ObjectAnimatorBinding") ObjectAnimator waveColor = ObjectAnimator.ofInt(waveLoadingView, "waveColor", mContext.getColor(R.color.indicator_color), mContext.getColor(R.color.background));
                    waveColor.setDuration(duration);
                    waveColor.setEvaluator(new ArgbEvaluator());
                    waveColor.start();
                }
            });
        }, 0);
    }

    private int i;
    @Override
    public void onClick(View v) {
        if (v.equals(selectButton)){
            if (!RunTimeService.isServiceRunning(this, RunTimeService.class.getName())){
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(intent,IMPORT_REQUEST_CODE);
                }catch (Exception e){
                    Toast.makeText(this, "刷机包路径解析失败", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(this, "刷机进程运行中，请稍后再试", Toast.LENGTH_SHORT).show();
            }
        }else if (v.equals(startFlash)){
            String path = pathEditor.getText().toString().trim();
            if (path.equals("")){
                Toast.makeText(this, "刷机包路径地址不能为空", Toast.LENGTH_SHORT).show();
            }else if (!path.endsWith(".zip")){
                Toast.makeText(this, "刷机包路径有误，请重新选择或填写", Toast.LENGTH_SHORT).show();
            }else if (RunTimeService.isServiceRunning(this, RunTimeService.class.getName())){
                Toast.makeText(this, "刷机服务运行中，请稍后再试", Toast.LENGTH_SHORT).show();
            }else {
                RunTimeService.start(this, path);
            }
        }else if (v.equals(tipsCard)){
            tipsHandler.removeCallbacks(runnable);
            tipsHandler = new Handler(this.getMainLooper());
            tipsHandler.post(runnable);
        }
    }

    @Override
    public void onLongClick(View view) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri != null) {
                    try {
                        String path = getPath(this, uri);
                        log("Selected ROM at: " + path + "\n");
                        try {
                            EasyShip.getMsg().append("Selected ROM at: " + path);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        pathEditor.setText(path);
                    }catch (Exception e){
                        Toast.makeText(this, "刷机包路径解析失败", Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, "错误：" + e, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    public String getPath(final Context context, final Uri uri) {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
