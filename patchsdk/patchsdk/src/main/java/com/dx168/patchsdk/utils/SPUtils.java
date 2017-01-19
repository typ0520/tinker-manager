package com.dx168.patchsdk.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by jianjun.lin on 2017/1/18.
 */

public class SPUtils {

    public static final String NAME = "patchsdk";
    public static final String KEY_PATCHED_PATCH = "patched_patch"; //最后一次合成成功的补丁
    public static final String KEY_LOADED_PATCH = "loaded_patch"; //最后一次应用成功的补丁

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static void put(Context context, String key, Object object) {
        SharedPreferences sp = getSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, ((Integer) object).intValue());
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, ((Boolean) object).booleanValue());
        } else if (object instanceof Float) {
            editor.putFloat(key, ((Float) object).floatValue());
        } else if (object instanceof Long) {
            editor.putLong(key, ((Long) object).longValue());
        } else {
            editor.putString(key, object.toString());
        }
        SPUtils.SharedPreferencesCompat.apply(editor);
    }

    public static <T> T get(Context context, String key, T t) {
        SharedPreferences sp = getSharedPreferences(context);
        if (t instanceof String) {
            return (T) sp.getString(key, (String) t);
        }
        if (t instanceof Boolean) {
            return (T) Boolean.valueOf(sp.getBoolean(key, ((Boolean) t).booleanValue()));
        }
        if (t instanceof Integer) {
            return (T) Integer.valueOf(sp.getInt(key, ((Integer) t).intValue()));
        }
        if (t instanceof Float) {
            return (T) Float.valueOf(sp.getFloat(key, ((Float) t).floatValue()));
        }
        if (t instanceof Long) {
            return (T) Long.valueOf(sp.getLong(key, ((Long) t).longValue()));
        }
        return t;
    }

    public static void remove(Context context, String key) {
        SharedPreferences sp = getSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        SPUtils.SharedPreferencesCompat.apply(editor);
    }

    public static void clear(Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        SPUtils.SharedPreferencesCompat.apply(editor);
    }

    public static boolean contains(Context context, String key) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.contains(key);
    }

    public static Map<String, ?> getAll(Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getAll();
    }

    private static class SharedPreferencesCompat {
        private static final Method sApplyMethod = findApplyMethod();

        private SharedPreferencesCompat() {
        }

        private static Method findApplyMethod() {
            try {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply", new Class[0]);
            } catch (NoSuchMethodException var1) {
                return null;
            }
        }

        public static void apply(SharedPreferences.Editor editor) {
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor, new Object[0]);
                    return;
                }
            } catch (IllegalArgumentException var2) {
            } catch (IllegalAccessException var3) {
            } catch (InvocationTargetException var4) {
            }
            editor.commit();
        }
    }

}
