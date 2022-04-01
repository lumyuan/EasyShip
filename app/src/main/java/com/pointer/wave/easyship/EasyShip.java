package com.pointer.wave.easyship;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.pointer.wave.easyship.common.crash.CrashHandler;
import com.pointer.wave.easyship.core.CacheDao;
import com.pointer.wave.easyship.utils.KeepShellPublic;
import com.pointer.wave.easyship.core.RealtimeProcess;


public class EasyShip extends Application {

    @SuppressLint("StaticFieldLeak") public static Context context;
    public static CacheDao cacheDao;
    public static boolean isVab;
    private static RealtimeProcess process;
    private static RealtimeProcess.MSG msg;

    private CacheDao dao;

    @Override
    public void onCreate() {
        super.onCreate();
        setContext(this);

        dao = new CacheDao();
        new Thread(()->{
            //初始化Magisk的shell函数库
            String cmdSync = KeepShellPublic.INSTANCE.doCmdSync(". " + dao.getShellFilePath() + "/" + dao.lisName + "\n" +
                    "mount_partitions\n");
            String[] split = cmdSync.split("\n");
            System.arraycopy(split, 0, info, 0, split.length);
        }).start();
        //异常捕获
        CrashHandler.init(this);
        isVab = isVAB();
    }

    public static RealtimeProcess getProcess() {
        return process;
    }

    public static void setProcess(RealtimeProcess process) {
        EasyShip.process = process;
    }

    public synchronized static RealtimeProcess.MSG getMsg() {
        return msg;
    }

    public synchronized static void setMsg(RealtimeProcess.MSG msg) {
        EasyShip.msg = msg;
    }

    public static Context getContext()  {
        return context;
    }

    private void setContext(Context context) {
        EasyShip.context = context;
    }

    public static String[] info = new String[2];
    private boolean isVAB(){
        String cmdSync = KeepShellPublic.INSTANCE.doCmdSync("find /dev/block/ -type l -name boot_a");
        System.out.println("cmdSync = " + cmdSync);
        return cmdSync.split("\n").length > 1;
    }
}
