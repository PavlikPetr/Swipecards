package com.topface.topface.utils;

import java.util.Collection;
import java.util.HashMap;

/**
 * Простой класс для работы со списком переключателей
 * Применяется в частности для хранения состойний отмеченных пунктов в списке
 */
public class TriggersList<K, V> {
    private HashMap<K, V> mTriggers;

    public TriggersList() {
        mTriggers = new HashMap<K, V>();
    }

    public boolean toggle(K item, V value) {
        if (isOn(item)) {
            setOff(item);
            return false;
        } else {
            setOn(item, value);
            return true;
        }
    }

    public boolean setOn(K item, V value) {
        mTriggers.put(item, value);
        return true;
    }

    public void setOff(K item) {
        if (mTriggers.containsKey(item)) {
            mTriggers.remove(item);
        }
    }

    public boolean isOn(K item) {
        return mTriggers.containsKey(item) && mTriggers.get(item) != null;
    }

    public Collection<V> getList() {
        return mTriggers.values();
    }

    public int getSize() {
        return mTriggers.size();
    }
}
