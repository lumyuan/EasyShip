package com.pointer.wave.easyship.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * JSON实用工具，方便快速访问JSON各节点
 *
 * 支持点连接访问
 */
public class JsonUtils {

    String TAG = "JSONUTILS";

    private JSONObject jsonObject;

    //临时对象
    private JSONObject tmpJsonObject;
    private JSONArray tmpJsonArray;
    private Object tmpObject;

    /**
     * 初始化
     *
     * @param json
     */
    public JsonUtils(String json) {
        try {
            this.tmpJsonObject = this.jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取JSON对象
     *
     * @param key
     * @return
     */
    public JsonUtils getJSONObject(String key) {
        try {
            this.tmpJsonObject = this.tmpJsonObject.getJSONObject(key);
        } catch (JSONException e) {
            Log.e(TAG, "键值：" + key + "不存在或是一个数组，正在尝试解析为数组...");
            this.getJSONArray(key);
        }

        return this;
    }

    /**
     * 获取对象，指非JSON对象，调用这个方法可以进行格式化返回结果，如toInt,toString等
     *
     * @param key
     * @return
     */
    public JsonUtils get(String key) {
        try {
            this.tmpObject = this.tmpJsonObject.get(key);
        } catch (JSONException e) {
            Log.e(TAG, "键值：" + key + "不存在或是一个数组，正在尝试解析为数组...");
            this.getJSONArray(key);
        }

        return this;
    }

    //重置临时JSON对象,调用toXXX函数时自动重置
    public JsonUtils reset() {
        this.tmpJsonObject = this.jsonObject;
        return this;
    }


    /**
     * 获取JSON数组对象
     *
     * @param key
     * @return
     */
    public JsonUtils getJSONArray(String key) {
        try {
            this.tmpJsonArray = this.tmpJsonObject.getJSONArray(key);
        } catch (JSONException e) {
            Log.e(TAG, "键值：" + key + "不是一个数组");
        }

        return this;
    }

    /**
     * 获取指定索引的数组项，JSON数组
     *
     * @param idx
     * @return
     */
    public JsonUtils getJSONArrayItem(int idx) {
        try {
            this.tmpJsonObject = this.tmpJsonArray.getJSONObject(idx);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * 获取指定索引的数组项，非JSON数组
     *
     * @param idx
     * @return
     */
    public JsonUtils getArraytem(int idx) {
        try {
            this.tmpObject = this.tmpJsonArray.get(idx);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return this;
    }

    /**
     * 点连接的方式访问键值，示例：a.b.c
     * @param key
     * @return
     */
    public JsonUtils getByDotKey(String key)
    {
        String[] keys = key.split("\\.");

        String tmpKey = "";
        String[] tmpKeys;

        JSONObject tmpJsonObject = this.tmpJsonObject;

        try
        {
            for(int i=0;i<keys.length;i++)
            {
                tmpKey = keys[i];

                //是一个数组
                if (tmpKey.contains(":"))
                {
                    tmpKeys = tmpKey.split(":");
                    this.tmpJsonArray = tmpJsonObject.getJSONArray(tmpKeys[0]);
                    tmpJsonObject = this.tmpJsonArray.getJSONObject(Integer.valueOf(tmpKeys[1]));
                }else
                {
                    if (i+1>=keys.length)
                    {
                        this.tmpObject = tmpJsonObject.get(tmpKey);
                    }else{
                        tmpJsonObject = tmpJsonObject.getJSONObject(tmpKey);
                    }
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        return this;
    }

    public int toInt()
    {
        reset();
        return Integer.valueOf(tmpObject.toString());
    }

    public float toFloat()
    {
        reset();
        return Float.valueOf(tmpObject.toString());
    }

    public String toString()
    {
        reset();
        return tmpObject.toString();
    }

    public Boolean toBoolean()
    {
        reset();
        return Boolean.valueOf(tmpObject.toString());
    }
}