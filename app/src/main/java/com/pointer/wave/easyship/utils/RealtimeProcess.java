package com.pointer.wave.easyship.utils;

import com.pointer.wave.easyship.core.CacheDao;
import com.pointer.wave.easyship.interfaces.RealtimeProcessInterface;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RealtimeProcess{
    // 是否在执行
    private boolean isRunning = false;
    // 存放命令行
    private ArrayList<RealtimeProcessCommand> commandList = new ArrayList<>();
    // 保存所有的输出信息
    private StringBuffer mStringBuffer = new StringBuffer();
    private BufferedReader readStdout = null;
    private BufferedReader readStderr = null;
    // 回调用到的接口
    private RealtimeProcessInterface mInterface = null;
    private int resultCode = 0;
    private String ROOT_DIR = null;
    private String tmp1 = null;
    private String tmp2 = null;
    private final MSG msg;
    private final ErrorMSG errorMSG;

    public MSG getMsg() {
        return msg;
    }

    public ErrorMSG getErrorMSG() {
        return errorMSG;
    }

    public RealtimeProcess(RealtimeProcessInterface mInterface){
        // 实例化接口对象
        this.mInterface = mInterface;
        this.errorMSG = new ErrorMSG();
        this.msg = new MSG();
    }
    public void setCommand(String ...commands){
        // 遍历命令
        for(String cmd : commands){
            RealtimeProcessCommand mRealtimeProcessCommand = new RealtimeProcessCommand();
            mRealtimeProcessCommand.setCommand(cmd);
            commandList.add(mRealtimeProcessCommand);
        }
    }
    public void setDirectory(String directory){
        this.ROOT_DIR = directory;
    }

    public void start() throws IOException, InterruptedException{
        isRunning = true;
        Process p = Runtime.getRuntime().exec("su");
        DataOutputStream write = new DataOutputStream(p.getOutputStream());
        for(RealtimeProcessCommand mRealtimeProcessCommand : commandList){
            write.writeBytes(mRealtimeProcessCommand.getCommand());
            write.writeBytes("\n");
            write.flush();
        }
        write.writeBytes("exit");
        write.writeBytes("\n");
        write.flush();
        exec(p);
    }
    public String getAllResult(){
        return mStringBuffer.toString();
    }

    private final Random random = new Random();
    private void exec(final Process process){
        // 获取标准输出
        readStdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
        // 获取错误输出
        readStderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        // 创建线程执行
        try {
            // 逐行读取
            while((tmp1 = readStdout.readLine()) != null || (tmp2 = readStderr.readLine()) != null){
                if(tmp1 != null){
                    mStringBuffer.append(tmp1 + "\n");
                    // 回调接口方法
                    mInterface.onNewStdoutListener(msg.append(tmp1));
                    System.out.println("tmp1 = " + tmp1);
                }
                if(tmp2 != null){
                    mStringBuffer.append(tmp2 + "\n");
                    mInterface.onNewStderrListener(errorMSG.append(tmp2));
                    System.out.println("tmp2 = " + tmp2);
                }
//                Thread.currentThread().sleep(random.nextInt(101));
            }
            resultCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        isRunning = false;
        mInterface.onProcessFinish(resultCode);
    }
    public boolean isRunning(){
        return this.isRunning;
    }
    public int getCommandSize(){
        return commandList.size();
    }
    public RealtimeProcessCommand getRealtimeProcessCommand(int p){
        return commandList.get(p);
    }

    static class RealtimeProcessCommand{
        private String directory = null;
        private String command = null;
        public RealtimeProcessCommand(){}

        public void setDirectory(String directory){
            this.directory = directory;
        }
        public void setCommand(String command){
            this.command = command;
        }
        public String getDirectory(){
            return this.directory;
        }
        public String getCommand(){
            return this.command;
        }

    }

    static public class MSG{
        private final List<String> list = new ArrayList<>();

        public synchronized MSG append(String s){
            list.add(s + "\n");
            return this;
        }

        public synchronized String getLastLine(){
            return list.size() > 0 ? list.get(list.size() - 1) : "";
        }

        @Override
        public synchronized String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : list) {
                stringBuilder.append(s);
            }
            return stringBuilder.toString();
        }

        public List<String> getList() {
            return list;
        }
    }

    static public class ErrorMSG{
        private final List<String> list = new ArrayList<>();

        public List<String> getList() {
            return list;
        }

        public synchronized ErrorMSG append(String s){
            list.add(s + "\n");
            return this;
        }

        public synchronized String getLastLine(){
            return list.size() > 0 ? list.get(list.size() - 1) : "";
        }

        @Override
        public synchronized String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            for (String s : list) {
                stringBuilder.append(s);
            }
            return stringBuilder.toString();
        }
    }
}
