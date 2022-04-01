package com.pointer.wave.easyship.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;

import androidx.documentfile.provider.DocumentFile;

import com.pointer.wave.easyship.EasyShip;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

public class SAF implements FileInterface {

    private String _dir;
    private String _dir_lc;
    private int _dir_j;
    private ArrayList<DocumentFile> list = new ArrayList<DocumentFile>();
    private DocumentFile _pup_root_df = null, _pup_data_df = null;
    private static final int _RequestCode = 1005;
    private static final String _TreeUri_Starts = "content://com.android.externalstorage.documents/tree/primary%3A",
            _TreeUri_Starts_document_primary = "content://com.android.externalstorage.documents/tree/primary%3A/document/primary%3A",
            _TreeUri_Starts_Android_data = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata",
            _TreeUri_Starts_Android_data2 = "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata";
    private Context _activity;
    private final String _path;
    private DocumentFile _document;
    private final File _file;
    public SAF(String path)
    {
        this._activity = EasyShip.getContext();
        _dir = Environment.getExternalStorageDirectory().toString();
        _dir_lc = _dir.toLowerCase();
        _dir_j = _dir.length() + 1;
        this._path = path;
        upPermission();
        this._document = DocumentFile.fromTreeUri(EasyShip.getContext(), pathToUri(path));
        this._file = new File(path);
    }

    private DocumentFile getDocument(String path){
        return DocumentFile.fromTreeUri(EasyShip.getContext(), pathToUri(path));
    }

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

    private boolean pup(Uri uri)
    {
        String pup = uri.toString();
        if(pup.equals(_TreeUri_Starts)) {
            _pup_root_df = DocumentFile.fromTreeUri(this._activity, uri);
            return true;
        }
        else if(pup.equals(_TreeUri_Starts_Android_data2)) {
            _pup_data_df = DocumentFile.fromTreeUri(this._activity, uri);
            return true;
        }
        return false;
    }

    private boolean upPermission()
    {
        boolean s = false;
        for (UriPermission up : _activity.getContentResolver().getPersistedUriPermissions()) {
            if(up.isReadPermission() && pup(up.getUri())) {
                s = true;
            }
        }
        return s;
    }

    private static boolean isPermission(Activity _activity)
    {
        for (UriPermission up : _activity.getContentResolver().getPersistedUriPermissions()) {
            if(up.isReadPermission()) {
                String pup = up.getUri().toString();
                if(pup.equals(_TreeUri_Starts))
                    return true;
                else if(pup.equals(_TreeUri_Starts_Android_data2))
                    return true;
            }
        }
        return false;
    }

    private static void goPermission(Activity _activity)
    {
        requestPermission(_activity, Uri.parse(_TreeUri_Starts_Android_data));
    }

    private static boolean isAllFiles(final Context context)
    {
        if(Build.VERSION.SDK_INT >= 30)
        {
            try {
                java.lang.reflect.Method m = Environment.class.getMethod("isExternalStorageManager");
                Object o = m.invoke(Environment.class);
                if(o != null) {
                    if (!o.equals(true)) {
                        new AlertDialog.Builder(context)
                                .setTitle("所有文件访问权限")
                                .setMessage("请开启所有文件访问权限，以免调试运行效果异常")
                                .setPositiveButton("去开启",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                Intent intent = new Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION");
                                                intent.setData(Uri.parse("package:"+ context.getPackageName()));
                                                context.startActivity(intent);
                                            }
                                        }).setNegativeButton("暂不", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                    }else
                        return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private int isPermission(String path)
    {
        if(_pup_root_df != null)
            return 1;
        if(path.toLowerCase().startsWith("android/data")) {
            if(_pup_data_df != null)
                return 2;
            requestPermission(_activity, Uri.parse(_TreeUri_Starts_Android_data));
            return -2;
        }else {
            requestPermission(_activity, Uri.parse(_TreeUri_Starts_Android_data));
            return -1;
        }
    }

    /**
     * 复制文件夹
     * */
    private int copyDirectory(String dir, String dir2, boolean isCover) {
        if(dir.equals(dir2))
            return 0;
        int count = 0;
        if(dir.toLowerCase().startsWith(String.format("%s/android/data", _dir_lc)))
        {
            String path = getPathConversion(dir);
            int result = isPermission(path);
            if (result > 0) {
                DocumentFile df = getFile(path, false);
                if (df != null) {
                    DocumentFile[] dfs = df.listFiles();
                    for (DocumentFile all : dfs) {
                        if (all.isFile()) {
                            if (copyFile(getFileName(all), String.format("%s/%s", dir2, all.getName()), isCover) > 0)
                                count++;
                        } else if (all.isDirectory())
                            count += copyDirectory(String.format("%s/%s", dir, all.getName()), String.format("%s/%s", dir2, all.getName()), isCover);
                    }
                }
            }
        } else {
            File dirFile = new File(dir);
            File[] childFiles = dirFile.listFiles();
            if (childFiles != null) {
                for (int i = 0; i < childFiles.length; i++) {
                    if (childFiles[i].isFile()) {
                        if (copyFile(childFiles[i].getAbsolutePath(), String.format("%s/%s", dir2, childFiles[i].getName()), isCover) > 0)
                            count++;
                    } else if (childFiles[i].isDirectory()) {
                        count += copyDirectory(String.format("%s/%s", dir, childFiles[i].getName()), String.format("%s/%s", dir2, childFiles[i].getName()), isCover);
                    }
                }
            }
        }
        return count;
    }

    /**
     * 复制文件
     * */
    private int copyFile(String fileName, String fileName2)
    {
        return copyFile(fileName, fileName2, true);
    }
    private int copyFile(String fileName, String fileName2, boolean isCover)
    {
        int result = 1;
        InputStream is = null;
        OutputStream os = null;
        if(fileName.toLowerCase().startsWith(String.format("%s/android/data", _dir_lc)))
        {
            String path = getPathConversion(fileName);
            result = isPermission(path);
            if (result > 0) {
                DocumentFile df = getFile(path, false);
                if (df != null) {
                    try {
                        is = this._activity.getContentResolver().openInputStream(df.getUri());
                    } catch (Exception e) {
                        e.printStackTrace();
                        result = -3;
                    }
                }else
                    result = -5;
            }
        }else
        {
            try {
                is = new FileInputStream(fileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                result = -3;
            }
        }
        if(result > 0)
        {
            if(fileName2.toLowerCase().startsWith(String.format("%s/android/data", _dir_lc)))
            {
                String path2 = getPathConversion(fileName2);
                result = isPermission(path2);
                if (result > 0) {
                    DocumentFile df2 = null;
                    if(isCover)
                        df2 = getFile(path2, true);
                    else{
                        boolean[] bs = { true, false};
                        df2 = getFile(path2, true, bs);
                        if(!bs[1])
                        {
                            result = -6;
                        }
                    }
                    if(result > 0) {
                        if (df2 != null) {
                            try {
                                os = this._activity.getContentResolver().openOutputStream(df2.getUri());
                            } catch (Exception e) {
                                e.printStackTrace();
                                result = -3;
                            }
                        } else
                            result = -5;
                    }
                }
            }else
            {
                File file2 = new File(fileName2);
                if(isCover || !file2.exists()) {
                    File Parent = file2.getParentFile();
                    if (Parent != null)
                        Parent.mkdirs();
                    try {
                        os = new FileOutputStream(fileName2);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        result = -3;
                    }
                }else
                    result = -6;
            }
        }
        if(result > 0 && is != null && os != null)
        {
            try {
                int buf_size = 1024;
                byte[] buffer = new byte[buf_size];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    os.write(buffer, 0, len);
                }
                result = 1;
            } catch (Exception e) {
                e.printStackTrace();
                result = -3;
            }
        }
        if (is != null) {
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (os != null) {
            try {
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 文件重命名
     * */
    private boolean renameFile(String fileName, String name)
    {
        if(!fileName.toLowerCase().startsWith(String.format("%s/android/data", _dir_lc)))
        {
            File f = new File(fileName);
            if(f.exists())
                return f.renameTo(new File(f.getParent() + "/" + name));
            return false;
        }
        String path = getPathConversion(fileName);
        int result = isPermission(path);
        if(result > 0) {
            DocumentFile df = getFile(path, false);
            if(df != null)
                return df.renameTo(name);
        }
        return false;
    }

    private int deleteDir(String dir) {
        int count = 0;
        if (dir.toLowerCase().startsWith(String.format("%s/android/data", _dir_lc))) {
            String path = getPathConversion(dir);
            int result = isPermission(path);
            if (result > 0) {
                DocumentFile df = getFile(path, false);
                if (df != null) {
                    if(df.isFile())
                    {
                        df.delete();
                        return 1;
                    }
                    DocumentFile[] dfs = df.listFiles();
                    for (DocumentFile all : dfs) {
                        if (all.isFile()) {
                            all.delete();
                            count++;
                        } else if (all.isDirectory())
                            count += deleteDir(String.format("%s/%s", dir, all.getName()));
                    }
                    df.delete();
                }
            }
        } else {
            File file = new File(dir);
            if (file.isFile()) {
                file.delete();
                return 1;
            }
            if (file.isDirectory()) {
                File[] childFiles = file.listFiles();
                if (childFiles == null || childFiles.length == 0) {
                    file.delete();
                    return 1;
                }
                for (int i = 0; i < childFiles.length; i++) {
                    if(childFiles[i].isFile()){
                        childFiles[i].delete();
                        count++;
                    }
                    else if(childFiles[i].isDirectory())
                        count += deleteDir(childFiles[i].getAbsolutePath());
                }
                file.delete();
            }
        }
        return count;
    }

    private boolean deleteFile(String fileName)
    {
        if(!fileName.toLowerCase().startsWith(String.format("%s/android/data", _dir_lc)))
        {
            File f = new File(fileName);
            if(f.exists())
                return f.delete();
            return false;
        }
        String path = getPathConversion(fileName);
        int result = isPermission(path);
        if(result > 0) {
            DocumentFile df = getFile(path, false);
            if(df != null)
                return df.delete();
        }
        return false;
    }

    private String ReadSdTextFile(String file, String bm) {
        File file2 = new File(file);
        if (!file2.exists())
            return null;
        FileInputStream fis = null;
        InputStreamReader isr = null;
        StringBuffer strs = new StringBuffer();
        String filecontent = null;
        try {
            fis = new FileInputStream(file2);
            isr = new InputStreamReader(fis, bm);
            char[] Char = new char[512];
            int i = -1;
            while ((i = isr.read(Char)) > 0) {
                strs.append(Char, 0, i);
            }
            filecontent = new String(strs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return filecontent;
    }

    private String readFileText(String fileName)
    {
        if(!fileName.toLowerCase().startsWith(String.format("%s/android/data", _dir_lc)))
        {
            return ReadSdTextFile(fileName, "utf-8");
        }
        byte[] b = readFile(fileName);
        try {
            return new String(b, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] toByteArray(String file) {
        File f = new File(file);
        if (!f.exists())
            return null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length());
        BufferedInputStream in = null;
        byte[] b = null;
        try {
            in = new BufferedInputStream(new FileInputStream(f));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while (-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.write(buffer, 0, len);
            }
            b = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return b;
    }
    private byte[] readFile(String fileName)
    {
        if(!fileName.toLowerCase().startsWith(String.format("%s/android/data", _dir_lc)))
        {
            return toByteArray(fileName);
        }
        byte[] b = null;
        String path = getPathConversion(fileName);
        int result = isPermission(path);
        if(result > 0) {
            DocumentFile df = getFile(path, true);
            if(df != null)
            {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                InputStream is = null;
                try {
                    is = this._activity.getContentResolver().openInputStream(df.getUri());
                    int buf_size = 1024;
                    byte[] buffer = new byte[buf_size];
                    int len;
                    while ((len = is.read(buffer)) > 0) {
                        bos.write(buffer, 0, len);
                    }
                    b = bos.toByteArray();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return b;
    }

    private void WriteSdTextFile(String file, String str, String bm) {
        File file2 = new File(file);
        File Parent = file2.getParentFile();
        if(Parent != null)
            Parent.mkdirs();
        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(file2), bm);
            osw.write(str);
            osw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(osw != null) {
                try {
                    osw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private int writeFile(String fileName, String text)
    {
        if(!fileName.toLowerCase().startsWith(String.format("%s/android/data", _dir_lc)))
        {
            WriteSdTextFile(fileName, text, "utf-8");
            return 1;
        }
        try {
            return writeFile(fileName, text.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return -5;
        }
    }

    private boolean WriteByteFile(String filename, byte[] b) {
        boolean s = true;
        FileOutputStream fos = null;
        File f = new File(filename);
        if (f.exists())
            f.delete();
        else
        {
            File Parent = f.getParentFile();
            if(Parent != null)
                Parent.mkdirs();
        }
        try {
            fos = new FileOutputStream(f);
            fos.write(b, 0, b.length);
        } catch (Exception e) {
            e.printStackTrace();
            s = false;
        }
        if(fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return s;
    }
    private int writeFile(String fileName, byte[] bytes)
    {
        if(!fileName.toLowerCase().startsWith(String.format("%s/android/data", _dir_lc)))
        {
            WriteByteFile(fileName, bytes);
            return 1;
        }
        String path = getPathConversion(fileName);
        int result = isPermission(path);
        if(result > 0) {
            DocumentFile df = getFile(path, true);
            if(df != null)
            {
                OutputStream os = null;
                try {
                    os = this._activity.getContentResolver().openOutputStream(df.getUri());
                    os.write( bytes,0,bytes.length);
                    result = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    result = -3;
                }
                if(os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else
                result = -5;
        }
        return result;
    }

    /**
     * 获取指定目录的文件列表
     * */
    private DocumentFile[] list(String path)
    {
        list.clear();
        path = getPathConversion(path);
        int s = isPermission(path);
        if(s > 0) {
            DocumentFile df = listFiles(path);
            if(df != null) {
                list.add(df.getParentFile());
                if (df.isDirectory())
                    list.addAll(Arrays.asList(df.listFiles()));
            }
        }
        return list.toArray(new DocumentFile[0]);
    }

    /**
     * 获取指定目录的文件列表
     * */
    private DocumentFile list(String path, ArrayList<DocumentFile> list)
    {
        list.clear();
        path = getPathConversion(path);
        int s = isPermission(path);
        if(s > 0) {
            DocumentFile df = listFiles(path);
            if(df != null) {
                if (df.isDirectory())
                    list.addAll(Arrays.asList(df.listFiles()));
                else
                    list.add(df);
                return df;
            }
        }
        return null;
    }

    private DocumentFile listFiles(String path)
    {
        DocumentFile df, df2;
        if(_pup_root_df == null) {
            if(path.toLowerCase().startsWith("android/data/"))
                path = path.substring(13);
            else
                return _pup_data_df;
            df = _pup_data_df;
        }else {
            if(path.length() == 0)
                return _pup_root_df;
            df = _pup_root_df;
        }
        String[] list = path.split("/");
        for(int i=0; i<list.length; i++)
        {
            df2 = listFiles(df, list[i]);
            if(df2 == null) {
                return null;
                //df = df.createDirectory(list[i]);
            }else
                df = df2;
        }
        return df;
    }
    private DocumentFile listFiles(DocumentFile df, String name)
    {
        DocumentFile[] dfs = df.listFiles();
        if(dfs != null) {
            for (DocumentFile all : dfs) {
                if (all.getName().equals(name) && all.isDirectory())
                    return all;
            }
        }
        return null;
    }

    private DocumentFile getFile(String path, boolean isNew)
    {
        return getFile(path, isNew, null);
    }
    private DocumentFile getFile(String path, boolean isNew, boolean[] bs)
    {
        DocumentFile df, df2;
        if(_pup_root_df == null) {
            if(path.toLowerCase().startsWith("android/data/"))
                path = path.substring(13);
            else
                return _pup_data_df;
            df = _pup_data_df;
        }else {
            if(path.length() == 0)
                return _pup_root_df;
            df = _pup_root_df;
        }
        String[] list = path.split("/");
        int j = list.length - 1;
        for(int i=0; i<=j; i++)
        {
            df2 = df.findFile(list[i]);
            if(df2 == null)
            {
                if(!isNew)
                    return null;
                if(i == j) {
                    if(bs != null) {
                        if(bs[0]){
                            df = df.createFile("*/*", list[i]);
                            bs[1] = true;
                        }
                    } else
                        df = df.createFile("*/*", list[i]);
                }else
                    df = df.createDirectory(list[i]);
            }else
                df = df2;
        }
        return df;
    }

    private DocumentFile getFileDir(String path, boolean isNew, boolean[] bs)
    {
        DocumentFile df, df2;
        if(_pup_root_df == null) {
            if(path.toLowerCase().startsWith("android/data/"))
                path = path.substring(13);
            else
                return _pup_data_df;
            df = _pup_data_df;
        }else {
            if(path.length() == 0)
                return _pup_root_df;
            df = _pup_root_df;
        }
        String[] list = path.split("/");
        int j = list.length - 1;
        for(int i=0; i<=j; i++)
        {
            df2 = df.findFile(list[i]);
            if(df2 == null)
            {
                if(!isNew)
                    return null;
                df = df.createDirectory(list[i]);
            }else
                df = df2;
        }
        return df;
    }

    private String getFileName(String url)
    {
        int i = url.lastIndexOf("%3A");
        if(i != -1)
            return _dir + "/" + url.substring(i + 3).replace("%2F", "/");
        return null;
    }
    private String getFileName(DocumentFile df)
    {
        return getFileName(df.getUri().toString());
    }

    private int getPathType(String path)
    {
        path = path.toLowerCase();
        /*
        if(path.startsWith("content://com.android.externalstorage.documents/tree/primary%"))
            return 1;*/
        //if(path.startsWith(String.format("%s/android/data/com.iapp.app", dir_lc)))
        //    return 0;
        if(path.startsWith(String.format("%s/android/data", _dir_lc)))
            return 2;
        return 0;
    }

    private String getPathConversion(String path)
    {
        if(path.toLowerCase().startsWith(_dir_lc))
            path = path.substring(_dir_j);
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        return path;
    }
    /**
     * 要求权限
     * */
    private static void requestPermission(Context _activity, Uri url)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DocumentFile df = DocumentFile.fromTreeUri(_activity, url);
            Intent intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                    | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
            intent1.putExtra(DocumentsContract.EXTRA_INITIAL_URI, url);
            ((Activity)_activity).startActivityForResult(intent1, _RequestCode);
        }
    }

    private static final String[][] MIME_MapTable = {
            // {后缀名，MIME类型}
            { ".3gp", "video/3gpp" },
            { ".apk", "application/vnd.android.package-archive" },
            { ".asf", "video/x-ms-asf" },
            { ".flv", "video/x-flv" },
            { ".rar", "application/x-rar" },
            { ".avi", "video/x-msvideo" },
            { ".ico", "image/x-ico" },
            { ".bin", "application/octet-stream" },
            { ".bmp", "image/bmp" },
            { ".c", "text/plain" },
            { ".class", "application/octet-stream" },
            { ".conf", "text/plain" },
            { ".cpp", "text/plain" },
            { ".doc", "application/msword" },
            { ".docx",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document" },
            { ".xls", "application/vnd.ms-excel" },
            { ".xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" },
            { ".exe", "application/octet-stream" },
            { ".gif", "image/gif" },
            { ".gtar", "application/x-gtar" },
            { ".gz", "application/x-gzip" },
            { ".h", "text/plain" },
            { ".htm", "text/html" },
            { ".html", "text/html" },
            { ".jar", "application/java-archive" },
            { ".java", "text/plain" },
            { ".jpeg", "image/jpeg" },
            { ".jpg", "image/jpeg" },
            { ".js", "application/x-javascript" },
            { ".log", "text/plain" },
            { ".m3u", "audio/x-mpegurl" },
            { ".m4a", "audio/mp4a-latm" },
            { ".m4b", "audio/mp4a-latm" },
            { ".m4p", "audio/mp4a-latm" },
            { ".m4u", "video/vnd.mpegurl" },
            { ".m4v", "video/x-m4v" },
            { ".mov", "video/quicktime" },
            { ".mp2", "audio/x-mpeg" },
            { ".mp3", "audio/x-mpeg" },
            { ".mp4", "video/mp4" },
            { ".mpc", "application/vnd.mpohun.certificate" },
            { ".mpe", "video/mpeg" },
            { ".mpeg", "video/mpeg" },
            { ".mpg", "video/mpeg" },
            { ".mpg4", "video/mp4" },
            { ".mpga", "audio/mpeg" },
            { ".msg", "application/vnd.ms-outlook" },
            { ".ogg", "audio/ogg" },
            { ".pdf", "application/pdf" },
            { ".png", "image/png" },
            { ".pps", "application/vnd.ms-powerpoint" },
            { ".ppt", "application/vnd.ms-powerpoint" },
            { ".pptx",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation" },
            { ".prop", "text/plain" }, { ".rc", "text/plain" },
            { ".rmvb", "audio/x-pn-realaudio" }, { ".rtf", "application/rtf" },
            { ".sh", "text/plain" }, { ".tar", "application/x-tar" },
            { ".tgz", "application/x-compressed" }, { ".txt", "text/plain" },
            { ".wav", "audio/x-wav" }, { ".wma", "audio/x-ms-wma" },
            { ".wmv", "audio/x-ms-wmv" },
            { ".wps", "application/vnd.ms-works" }, { ".xml", "text/plain" },
            { ".z", "application/x-compress" },
            { ".zip", "application/x-zip-compressed" }, { "", "*/*" } };

    /**
     * 根据文件后缀名获得对应的MIME类型。
     */
    private static String getMIMEType(String fName) {

        String type = "*/*";
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0)
            return type;
        String end = fName.substring(dotIndex).toLowerCase();
        if (end.length() == 0)
            return type;
        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (end.equals(MIME_MapTable[i][0]))
                return MIME_MapTable[i][1];
        }
        return type;
    }

    public static Uri pathToUri(String path){
        String[] paths = path.replaceAll(getDirStoragePath(), "").split("/");
        StringBuilder stringBuilder = new StringBuilder("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata");
        for (String p : paths) {
            if (p.length() == 0) continue;
            stringBuilder.append("%2F").append(p);
        }
        return Uri.parse(stringBuilder.toString());
    }

    public static String uriToPath(Uri uri){
        String s = uri.toString();
        s = s.substring(s.lastIndexOf("%3A") + 3);
        return getExternalStoragePath() + "/" + s.replace("%2F", "/");
    }

    public InputStream openInputStream() throws IOException {
        if (isDirectory()) throw new IOException("The Path is a directory.");
        return EasyShip.getContext().getContentResolver().openInputStream(_document.getUri());
    }

    public OutputStream openOutputStream() throws IOException{
        if (isDirectory()) throw new IOException();
        return EasyShip.getContext().getContentResolver().openOutputStream(_document.getUri(), "rwt");
    }

    @Override
    public boolean exists() {
        return getDocument(_path).exists();
    }

    @Override
    public String getName() {
        return _file.getName();
    }

    @Override
    public String getParent() {
        return _file.getParent();
    }

    @Override
    public String getPath() {
        return _path;
    }

    @Override
    public boolean canRead() {
        return getDocument(_path).canRead();
    }

    @Override
    public boolean canWrite() {
        return getDocument(_path).canWrite();
    }

    @Override
    public boolean isDirectory() {
        return getDocument(_path).isDirectory();
    }

    @Override
    public boolean isFile() {
        return getDocument(_path).isFile();
    }

    @Override
    public long lastModified() {
        return getDocument(_path).lastModified();
    }

    @Override
    public long length() {
        return getDocument(_path).length();
    }

    @Override
    public boolean createNewFile() {
        DocumentFile file = getFile(getPathConversion(_path), true)
                .createFile("*/*", _path.substring(_path.lastIndexOf("/") + 1));
        return file != null;
    }

    @Override
    public boolean delete() {
        return getDocument(_path).delete();
    }

    @Override
    public String[] list() {
        DocumentFile[] documentFiles = getDocument(_path).listFiles();
        String[] strings = new String[documentFiles.length];
        for (int i = 0; i < documentFiles.length; i++) {
            strings[i] = uriToPath(documentFiles[i].getUri());
        }
        return documentFiles == null ? null : strings;
    }

    @Override
    public boolean mkDirs() {
        DocumentFile document = getDocument(getDirStoragePath());
        String[] split = _path.replace(getDirStoragePath() + "/", "").split("/");
        StringBuilder stringBuilder = new StringBuilder(getDirStoragePath());
        for (String path : split) {
            stringBuilder.append("/").append(path);
            DocumentFile document1 = getDocument(stringBuilder.toString());
            if (!document1.exists())
                document = document != null ? document.createDirectory(path) : null;
            else document = getDocument(stringBuilder.toString());
        }
        return document.exists();
    }

    @Override
    public boolean renameTo(String name) {
        return getDocument(_path).renameTo(name);
    }
}
