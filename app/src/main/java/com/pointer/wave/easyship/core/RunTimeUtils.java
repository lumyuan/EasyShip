package com.pointer.wave.easyship.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RunTimeUtils implements OnResultListener {

    private static RunTimeUtils INSTANCE = new RunTimeUtils();

    private OnResultListener resultListener;
    private RunTimeUtils(){
        resultListener = this;
    }

    public static RunTimeUtils getInstance(){
        return INSTANCE;
    }

    public void cmd(String cmd) throws IOException {
        Process su = Runtime.getRuntime().exec("su");
        InputStream inputStream = su.getInputStream();
        OutputStream outputStream = su.getOutputStream();
        InputStream errorStream = su.getErrorStream();
    }

    @Override
    public void onResult(String result) {

    }

    public OnResultListener getRunning() {
        return resultListener;
    }

    public void setRunning(OnResultListener resultListener) {
        this.resultListener = resultListener;
    }
}
interface OnResultListener{
    void onResult(String result);
}
