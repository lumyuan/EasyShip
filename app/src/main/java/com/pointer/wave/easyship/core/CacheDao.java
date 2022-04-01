package com.pointer.wave.easyship.core;

import com.pointer.wave.easyship.EasyShip;
import com.pointer.wave.easyship.interfaces.OnProcessDestroyListener;
import com.pointer.wave.easyship.interfaces.OnProcessErrorListener;
import com.pointer.wave.easyship.interfaces.OnRunTimeListener;
import com.pointer.wave.easyship.utils.FileUtil;
import com.pointer.wave.easyship.utils.ShellExecutor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 核心配置加载类，不过从0.0.2版本开始后sh执行体迁移到RealtimeProcess类了
 */
public class CacheDao {

    private String rootDir;
    private String updateEngineStatus;
    private String shellFile = "run.sh";
    private String shellFilePath;
    private String romDir = "";
    private String workdir;
    private String shellPath;
    private final List<OnRunTimeListener> onRunTimeListeners = new ArrayList<>();
    private final List<OnProcessDestroyListener> onProcessDestroyListeners = new ArrayList<>();
    private final MSG msg;
    public final String _7zaName = "7za";
    public final String lisName = "magisk_library.sh";

    public CacheDao(){
        rootDir = EasyShip.getContext().getFilesDir().getAbsolutePath() + "/update_engine";
        updateEngineStatus = rootDir + "/.update";
        shellFilePath = rootDir + "/core";
        String sh = getShell();

        File rootFile = new File(rootDir);
        if (!rootFile.exists()) rootFile.mkdirs();

        File coreFile = new File(shellFilePath);
        if (!coreFile.exists()) coreFile.mkdirs();

        FileUtil fileUtil = new FileUtil(shellFilePath + "/" + shellFile);
        fileUtil.writeText(sh);
        shellPath = fileUtil.getPath();
        msg = new MSG();

        copy(_7zaName, shellFilePath);
        copy(lisName, shellFilePath);
    }

    public CacheDao(String workdir){
        rootDir = EasyShip.getContext().getFilesDir().getAbsolutePath() + "/update_engine";
        updateEngineStatus = rootDir + "/.update";
        shellFilePath = rootDir + "/core";
        this.workdir = workdir;
        String sh = getShell();

        File rootFile = new File(rootDir);
        if (!rootFile.exists()) rootFile.mkdirs();

        File coreFile = new File(shellFilePath);
        if (!coreFile.exists()) coreFile.mkdirs();

        FileUtil fileUtil = new FileUtil(shellFilePath + "/" + shellFile);
        fileUtil.writeText(sh);
        shellPath = fileUtil.getPath();
        msg = new MSG();

        copy(_7zaName, shellFilePath);
    }

    private OnRunTimeListener onRunTimeListener;
    private OnProcessDestroyListener onProcessDestroyListener;
    public CacheDao(String workdir, OnRunTimeListener onRunTimeListener, OnProcessDestroyListener onProcessDestroyListener){
        this(workdir);
        this.onRunTimeListener = onRunTimeListener;
        this.onProcessDestroyListener = onProcessDestroyListener;
    }

    public boolean isStart(){
        return new FileUtil(updateEngineStatus).exists();
    }

    public void startUpdateEngine(){
        new FileUtil(updateEngineStatus).writeText("\n");
    }

    public void stopUpdateEngine(){
        new FileUtil(updateEngineStatus).delete();
    }

    public void selectRom(String path){
        this.romDir = path;
    }

    public void moveResources(){
        copy(shellFile, shellFilePath);
    }

    public void flashRom(){
        flash(getShellPath(), new File(workdir).getParent(), new File(workdir).getName(), getRootDir());
    }

    public String getCmd(){
        return setParams(getShellPath(), new File(workdir).getParent(), new File(workdir).getName(), getRootDir());
    }

    public String setParams(String sh, String... params){
        StringBuilder stringBuilder = new StringBuilder("sh ");
        stringBuilder.append(sh);
        for (String param : params) {
            stringBuilder.append(" ").append("'").append(param).append("'");
        }
        return stringBuilder.toString();
    }

    public void flash(String sh, String... params){
        String cmd = setParams(sh, params);
        startUpdateEngine();
        try{
            Process process = Runtime.getRuntime().exec("su");
            try(DataOutputStream writer = new DataOutputStream(process.getOutputStream());
                BufferedReader success = new BufferedReader(new InputStreamReader(process.getInputStream()))){
                writer.writeBytes(cmd + "\n");
                writer.flush();
                writer.writeBytes("exit\n");
                String s;
                while ((s = success.readLine()) != null) {
                    for (OnRunTimeListener l : onRunTimeListeners) {
                        l.onRead(msg.append(s));
                    }
                }
                process.waitFor();
            }catch (Exception e){
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        remove();
        stopUpdateEngine();
        for (OnProcessDestroyListener l : onProcessDestroyListeners) {
            l.onDestroy();
        }
    }

    public void remove(){
        File file = new File(workdir + "/update");
        file.delete();
    }

    public boolean copy(String filename, String workdir){
        File flFile = new File(workdir);
        if (!flFile.exists()) {
            flFile.mkdirs();
        }
        boolean isCopses = false;
        try {
            InputStream is = EasyShip.getContext().getAssets().open(filename);
            File file = new File(workdir + "/" + filename);
            OutputStream fos = new FileUtil(file.getPath()).openOutputStream();
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            fos.close();
            is.close();
            isCopses = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isCopses;
    }

    public String getShell(){
        String text = null;
        try{
            InputStream inputStream = EasyShip.getContext().getAssets().open(shellFile);
            byte[] bytes = FileUtil.readBytes(inputStream);
            text = new String(bytes);
        }catch (Exception e){
            e.printStackTrace();
        }
        return text;
    }

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public String getUpdateEngineStatus() {
        return updateEngineStatus;
    }

    public void setUpdateEngineStatus(String updateEngineStatus) {
        this.updateEngineStatus = updateEngineStatus;
    }

    public String getShellFilePath() {
        return shellFilePath;
    }

    public void setShellFilePath(String shellFilePath) {
        this.shellFilePath = shellFilePath;
    }

    public String getRomDir() {
        return romDir;
    }

    public void setRomDir(String romDir) {
        this.romDir = romDir;
    }

    public String getShellPath() {
        return shellPath;
    }

    public void addOnRunTimeListener(OnRunTimeListener onRunTimeListener){
        onRunTimeListeners.add(onRunTimeListener);
    }

    public void addOnProcessDestroyListener(OnProcessDestroyListener onProcessDestroyListener) {
        this.onProcessDestroyListeners.add(onProcessDestroyListener);
    }

    public String getShellFile() {
        return shellFile;
    }

    public void setShellFile(String shellFile) {
        this.shellFile = shellFile;
    }

    public String getWorkdir() {
        return workdir;
    }

    public void setWorkdir(String workdir) {
        this.workdir = workdir;
    }

    public void setShellPath(String shellPath) {
        this.shellPath = shellPath;
    }

    public MSG getMsg() {
        return msg;
    }

    public String get_7zaName() {
        return _7zaName;
    }

    static public class MSG{
        private final List<String> list = new ArrayList<>();

        public synchronized MSG append(String s){
            list.add(s + "\n");
            return this;
        }

        public List<String> getList() {
            return list;
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
