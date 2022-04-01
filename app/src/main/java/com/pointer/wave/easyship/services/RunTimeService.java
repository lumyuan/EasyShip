package com.pointer.wave.easyship.services;

import static com.pointer.wave.easyship.EasyShip.cacheDao;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.pointer.wave.easyship.EasyShip;
import com.pointer.wave.easyship.FlashActivity;
import com.pointer.wave.easyship.R;
import com.pointer.wave.easyship.core.CacheDao;
import com.pointer.wave.easyship.interfaces.RealtimeProcessInterface;
import com.pointer.wave.easyship.core.RealtimeProcess;
import com.pointer.wave.easyship.utils.ShellUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RunTimeService extends Service implements RealtimeProcessInterface {

    @SuppressLint("StaticFieldLeak")
    private static final int NOTIFICATION_ID = 1001;
    @SuppressLint("StaticFieldLeak")
    private static RunTimeService instance = null;

    private static Handler handler;
    private Runnable runnable;
    private Thread thread;

    private static Context context;

    private static Runnable cancelLogRunnable;
    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        instance = this;
        handler = new Handler(getMainLooper());
        runnable = ()->{
            try {
                stop();
            }catch (Exception e){
                e.printStackTrace();
            }
        };
        cancelLogRunnable = ()->{
            System.out.println(ShellUtil.execCommand("ps -ef | grep \"logcat -s update_engine:v\" | grep -v grep | awk '{print $2}' | xargs kill -9", true, true).errorMsg);
        };
        post("系统更新服务启动中");
        if (work == null){
            Toast.makeText(this, "系统更新服务启动失败，ROM路径为空", Toast.LENGTH_SHORT).show();
            stop();
        }

        cacheDao = new CacheDao(work);
        EasyShip.setProcess(new RealtimeProcess(this));
        EasyShip.getProcess().setCommand(cacheDao.getCmd(), "rm -rf " +  new File(work).getParent() + "/update*");
        new Handler(getMainLooper()).post(FlashActivity::startAnimation);
        if (FlashActivity.startFlash!=null){
            FlashActivity.startFlash.setText("刷机中，请稍候...");
            FlashActivity.title.setText("0%");
        }
        thread = new Thread(() -> {
            try {
                try {
                    EasyShip.getProcess().start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            cacheDao.flashRom();
        });
        thread.start();
    }

    @SuppressLint("SetTextI18n")
    private void runDone(){
        new Handler(getMainLooper()).postDelayed(()->{
            if (FlashActivity.title!=null){
                FlashActivity.title.setText("开始刷写ROM");
                FlashActivity.waveLoadingView.setProgressValue(0);
            }
            stop();
        }, 1000);
    }

    private String otherLog = null;
    private synchronized void run(RealtimeProcess.MSG m){
        System.out.println("===============================================================");
        EasyShip.setMsg(m);
        String msg = m.getLastLine();
        if (msg.contains("INFO:")){
            if (msg.contains("Applying") && msg.contains("partition")) {
                point = msg.substring(msg.indexOf("partition ") + 10).replace("\"", "");
            }
            if (isLog(msg, 115) || isLog_2(msg, 361)){ //过滤进度
                String str = "更新总进度：";
                if (isLog(msg, 115)){
                    if (point.equals("")){
                        post(str + getProgress(msg));
                    }else {
                        str = str + getProgress(msg) + "%  当前：" + point.toUpperCase() + "："
                                + msg.substring(msg.indexOf(" (") + 2, msg.indexOf("),"));
                        post(str);
                    }
                    new Handler(getMainLooper()).post(()->{
                        if (FlashActivity.waveLoadingView != null) FlashActivity.waveLoadingView.setProgressValue(getProgress(msg));
                        if (FlashActivity.title!=null) FlashActivity.title.setText(getProgress(msg) + "%");
                    });
                }else if (isLog_2(msg, 361)){
                    str = "等待和并完成：";
                    post(str + getProgress(msg) + "%");
                    new Handler(getMainLooper()).post(()->{
                        if (FlashActivity.waveLoadingView != null) FlashActivity.waveLoadingView.setProgressValue(getProgress(msg));
                        if (FlashActivity.title!=null) FlashActivity.title.setText(getProgress(msg) + "%");
                    });
                }
            }else if (msg.contains("核心") || msg.contains("系统")){
                post(msg);
            }
        }
        if (msg.contains("ota-version failed!")){
            otherLog = "更新失败！目标版本低于当前版本（无法回滚）\n";
        }
        if (msg.contains("Updated Marker")){
            FlashActivity.log("系统更新完成，请重启前前往Magisk，安装到未使用的槽位（OTA后）\n");
            cancelLog();
        }else if (msg.contains(" An update already applied, waiting for reboot")){
            FlashActivity.log("系统已经应用了一个更新，请重启前前往Magisk，安装到未使用的槽位（OTA后），然后重启应用更新\n");
            cancelLog();
        }else if (msg.contains("Terminating cleanup previous update")){
            FlashActivity.log("系统重新启动到新的更新：系统首次从插槽" + getSlot() + "运行，插槽" + getUnusedSlot() + "的状态仍为可启动且成功，而插槽" + getSlot() + "仅可启动，且仍然处于活动但不成功的状态；请等待30~60分钟后再试一次。\n");
            cancelLog();
        }else if (msg.contains("Current update attempt downloads 0 bytes data")){
            FlashActivity.log(otherLog == null ? "更新失败\n" : otherLog);
            otherLog = null; 
            cancelLog();
        }else {
            FlashActivity.log(msg);
            if (msg.toLowerCase().contains("install")){
                handler.removeCallbacks(cancelLogRunnable);
                handler = null;
                handler = new Handler(getMainLooper());
            }
        }
    }

    private String getSlot(){
        String info = EasyShip.info[0];
        char slot = info.charAt(info.length() - 1);
        return String.valueOf(slot).toUpperCase();
    }

    private String getUnusedSlot(){
        char slot = getSlot().charAt(0);
        return slot == 'A' ? "B" : "A";
    }
    
    private void post(String content){
        Notification notification = createForegroundNotification(content);
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    public static String work;
    public static void start(Context context, String workdir) {
        if (isServiceRunning(context , RunTimeService.class.getName())) {
            new Handler(EasyShip.getContext().getMainLooper()).post(() -> Toast.makeText(EasyShip.getContext(), "系统更新服务运行中，请稍后再试", Toast.LENGTH_SHORT).show());
            return;
        }
        Intent service = new Intent(context, RunTimeService.class);
        try{
            context.startForegroundService(service);
            work = workdir;
        }catch (Exception e){
            e.printStackTrace();
            new Handler(EasyShip.getContext().getMainLooper()).post(() -> Toast.makeText(EasyShip.getContext(), "系统更新服务启动失败，请重试", Toast.LENGTH_SHORT).show());
        }
    }

    public static void stop() {
        if (instance != null) {
            instance.stopForeground(true);
            instance.stopSelf();
        }
        System.out.println("stop");
        new Handler(context.getMainLooper()).post(()->{
            if (FlashActivity.startFlash!=null){
                FlashActivity.startFlash.setText("开始刷机");
                FlashActivity.stopAnimation();
            }
        });
    }

    private Notification createForegroundNotification(String text) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String notificationChannelId = getString(R.string.app_name) + "_update_service";
        String channelName = getString(R.string.app_name) + "_update_service";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, channelName, importance);
        notificationChannel.setDescription("Channel description");
        notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
        notificationChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, notificationChannelId);
        Intent intent = new Intent(this, FlashActivity.class);
        PendingIntent  pi = PendingIntent.getActivity(this, 20010316, intent, android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S ? PendingIntent.FLAG_MUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(text);
        builder.setContentIntent(pi);
        builder.setWhen(System.currentTimeMillis());
        return builder.build();
    }

    public static boolean isServiceRunning(Context context , final String className) {
        ActivityManager am =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info = am.getRunningServices(100);
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningServiceInfo aInfo : info) {
            if (className.equals(aInfo.service.getClassName())) return true;
        }
        return false;
    }

    public static boolean isLog(String content, int filter){
        return content.contains("[INFO:delta_performer.cc(" + filter + ")]");
    }

    public static boolean isLog_2(String content, int filter){
        return content.contains("[INFO:cleanup_previous_update_action.cc(" + filter + ")]");
    }

    public static void cancelLog(){
        handler.postDelayed(cancelLogRunnable, 5000);
    }

    public static int getProgress(String content){
        if (isLog(content, 115) || isLog_2(content, 361)){
            int integer = 0;
            try{
                if (isLog_2(content, 361)){
                    String progress1 = content.substring(content.lastIndexOf(" ") + 1, content.lastIndexOf("%.")).trim();
                    integer = Integer.parseInt(progress1);
                }else {
                    String progress = content.substring(content.lastIndexOf(" "), content.lastIndexOf("%")).trim();
                    integer = Integer.parseInt(progress);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return integer;
        }
        return 0;
    }

    private String point = "";
    @SuppressLint("SetTextI18n")
    @Override
    public void onNewStdoutListener(RealtimeProcess.MSG m) {
        run(m);
    }

    @Override
    public void onNewStderrListener(RealtimeProcess.ErrorMSG errorMSG) {
        System.out.println("errorMSG.getLastLine() = " + errorMSG.getLastLine());
    }

    @Override
    public void onProcessFinish(int resultCode) {
        runDone();
    }
}
