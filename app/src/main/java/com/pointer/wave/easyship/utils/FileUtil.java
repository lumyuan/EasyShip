package com.pointer.wave.easyship.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class FileUtil implements FileInterface {

    public static String getExternalStoragePath(){
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static String getDirStoragePath(){
        return Environment.getExternalStorageDirectory().getPath() + "/Android/data";
    }

    @SuppressLint("SdCardPath")
    public static String getSDCardStoragePath(){
        return "/sdcard/Android/data";
    }

    private final String path;
    private final File file;
    private final SAF saf;
    public FileUtil(String path){
        this.path = path;
        file = new File(path);
        saf = new SAF(path);
    }

    private boolean isFileDir(){
        return (path.startsWith(getDirStoragePath()) || path.startsWith(getSDCardStoragePath())) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

    public String readText(){
        try {
            return new String(readBytes(openInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean writeText(String text){
        try{
            return writeBytes(openOutputStream(), text.getBytes(StandardCharsets.UTF_8));
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] readBytes(InputStream inputStream){
        try(ByteArrayOutputStream byteArrayOutputStream =new ByteArrayOutputStream(1024);
            BufferedInputStream bis = new BufferedInputStream(inputStream)) {
            byte[] temp=new byte[1024];
            int size = 0;
            while((size = bis.read(temp)) != -1) {
                byteArrayOutputStream.write(temp,0,size);
            }
            return byteArrayOutputStream.toByteArray();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean writeBytes(OutputStream os, byte[] bs){
        try(BufferedOutputStream bos = new BufferedOutputStream(os)){
            bos.write(bs);
            return true;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }finally {
            if (os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public InputStream openInputStream() throws IOException {
        if (isFileDir()) return saf.openInputStream();
        else return new FileInputStream(path);
    }

    public OutputStream openOutputStream() throws IOException {
        if (isFileDir()) return saf.openOutputStream();
        else return new FileOutputStream(path);
    }


    @Override
    public boolean exists() {
        if (isFileDir()) return saf.exists();
        else return file.exists();
    }

    @Override
    public String getName() {
        if (isFileDir()) return saf.getName();
        else return file.getName();
    }

    @Override
    public String getParent() {
        if (isFileDir()) return saf.getParent();
        else return file.getParent();
    }

    @Override
    public String getPath() {
        if (isFileDir()) return saf.getPath();
        else return file.getPath();
    }

    @Override
    public boolean canRead() {
        if (isFileDir()) return saf.canRead();
        else return file.canRead();
    }

    @Override
    public boolean canWrite() {
        if (isFileDir()) return saf.canWrite();
        else return file.canWrite();
    }

    @Override
    public boolean isDirectory() {
        if (isFileDir()) return saf.isDirectory();
        else return file.isDirectory();
    }

    @Override
    public boolean isFile() {
        if (isFileDir()) return saf.isFile();
        else return file.isFile();
    }

    @Override
    public long lastModified() {
        if (isFileDir()) return saf.lastModified();
        else return file.lastModified();
    }

    @Override
    public long length() {
        if (isFileDir()) return saf.length();
        else return file.length();
    }

    @Override
    public boolean createNewFile() {
        if (isFileDir()) return saf.createNewFile();
        else {
            try {
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public boolean delete() {
        if (isFileDir()) return saf.delete();
        else return file.delete();
    }

    @Override
    public String[] list() {
        if (isFileDir()) return saf.list();
        else return file.list();
    }

    @Override
    public boolean mkDirs() {
        if (isFileDir()) return saf.mkDirs();
        else return file.mkdirs();
    }

    @Override
    public boolean renameTo(String name) {
        if (isFileDir()) return saf.renameTo(name);
        else return file.renameTo(new File(getParent() + "/" + name));
    }
}
