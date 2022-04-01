package com.pointer.wave.easyship.utils;

public interface FileInterface {
    public boolean exists();

    public String getName();

    public String getParent();

    public String getPath();

    public boolean canRead();

    public boolean canWrite();

    public boolean isDirectory();

    public boolean isFile();

    public long lastModified();

    public long length();

    public boolean createNewFile();

    public boolean delete();

    public String[] list();

    public boolean mkDirs();

    public boolean renameTo(String name);
}
