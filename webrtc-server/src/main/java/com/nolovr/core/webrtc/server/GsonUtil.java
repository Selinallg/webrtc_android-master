package com.nolovr.core.webrtc.server;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class GsonUtil {
    private static String TAG  = "GsonUtil";
    private static Gson   gson = null;

    static {
        if (gson == null) {
            gson = new Gson();
        }
    }

    private GsonUtil() {
    }

    /**
     * 转成json
     *
     * @param object
     * @return
     */
    public static String GsonString(Object object) {
        try {
            String gsonString = null;
            if (gson != null) {
                gsonString = gson.toJson(object);
            }
            return gsonString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 保存json的List对象
     */
    public static <T> String listToJson(List<T> lists) {
        String jsonString = gson.toJson(lists);
        return jsonString;
    }

    /**
     * 解析jsonArray
     */
    public static <T> List<T> jsonObjArray(String jsonArray, Class<T> clazz) {
        if (TextUtils.isEmpty(jsonArray)) {
            return null;
        }
        List<T>    lists  = new ArrayList<T>();
        JsonParser parser = new JsonParser();
        JsonArray  array  = parser.parse(jsonArray).getAsJsonArray();
        for (JsonElement obj : array) {
            T t = gson.fromJson(obj, clazz);
            lists.add(t);
        }
        return lists;
    }

    /**
     * 转成bean
     *
     * @param gsonString
     * @param cls
     * @return
     */
//    public static <T> T GsonToBean(String gsonString, Class<T> cls) {
//        T t = null;
//        if (gson != null && gsonString != null) {
//            android.util.JsonReader reader = new JsonReader(new StringReader(gsonString));
//            reader.setLenient(true);
//            t = gson.fromJson(reader, cls);
//        }
//
//        return t;
//    }

    /**
     * 解析json
     */
    public static <T> T jsonObj(String jsonData, Class<T> cls) {
        if (TextUtils.isEmpty(jsonData)) {
            return null;
        }
        T t = null;
        try {
            t = new Gson().fromJson(jsonData.trim(), cls);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            Log.i(TAG,jsonData);
            return null;
        } catch (JsonParseException e) {
            e.printStackTrace();
            Log.i(TAG,e.toString());
            return null;
        } catch (Exception e) {
            Log.i(TAG,e.toString());
            return null;
        }
        return t;
    }

    /**
     * 转成list
     *
     * @param gsonString
     * @param cls
     * @return
     */
    public static <T> List<T> GsonToList(String gsonString, Class<T> cls) {
        List<T> list = null;
        if (gson != null) {
            list = gson.fromJson(gsonString, new TypeToken<List<T>>() {
            }.getType());
        }
        return list;
    }


    /**
     * 转成list中有map的
     *
     * @param gsonString
     * @return
     */
    public static <T> List<Map<String, T>> GsonToListMaps(String gsonString) {
        List<Map<String, T>> list = null;
        if (gson != null) {
            list = gson.fromJson(gsonString,
                    new TypeToken<List<Map<String, T>>>() {
                    }.getType());
        }
        return list;
    }

    /**
     * 转成map的
     *
     * @param gsonString
     * @return
     */
    public static <T> Map<String, T> GsonToMaps(String gsonString) {
        Map<String, T> map = null;
        if (gson != null) {
            map = gson.fromJson(gsonString, new TypeToken<Map<String, T>>() {
            }.getType());
        }
        return map;
    }

    public static String mapToJson(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        return gson.toJson(data);
    }

    public static <T> List<T> parseString2List(String json, Class clazz) {
        java.lang.reflect.Type type = new ParameterizedTypeImpl(clazz);
        List<T>      list =  new Gson().fromJson(json, type);
        return list;
    }

    private static class ParameterizedTypeImpl implements java.lang.reflect.ParameterizedType {
        Class clazz;

        public ParameterizedTypeImpl(Class clz) {
            clazz = clz;
        }

        @Override
        public java.lang.reflect.Type[] getActualTypeArguments() {
            return new java.lang.reflect.Type[]{clazz};
        }

        @Override
        public java.lang.reflect.Type getRawType() {
            return List.class;
        }

        @Override
        public java.lang.reflect.Type getOwnerType() {
            return null;
        }
    }
}