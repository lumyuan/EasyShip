package com.pointer.wave.easyship.core;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LoadShellUtil {

    public final Context context;
    public String _sh = "";
    public final String workFolder;
    public final String shellName = "run.sh";
    public final String shell2Name = "run.sh";
    public final String _7zaName = "7za";

    public LoadShellUtil(Context context){
        this.context = context;
        workFolder = this.context.getFilesDir().getPath() + "/work";
    }

    public void moveResources(){
        copy(shellName);
        //copy(shell2Name);
        copy(_7zaName);
    }

    public void remove(){
        File file = new File(workFolder);
        file.delete();
    }

    private boolean copy(String filename){
        File flFile = new File(workFolder);
        if (!flFile.exists()) {
            flFile.mkdirs();
        }
        boolean isCopysucss = false;
        try {
            InputStream is = this.context.getAssets().open(filename);//从assets文件夹中复制文件
            File file = new File(workFolder + "/" + filename);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            fos.close();
            is.close();
            isCopysucss = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isCopysucss;
    }

}
