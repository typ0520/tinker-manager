package com.dx168.patchsdk.utils;

import java.util.HashMap;

/**
 * Created by tong on 17/7/3.
 */
public class IgnoreNullHashMap<K,V> extends HashMap<K,V> {
    @Override
    public V put(K key, V value) {
        if (value == null) {
            return null;
        }
        return super.put(key, value);
    }
}
