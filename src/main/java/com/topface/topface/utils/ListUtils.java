package com.topface.topface.utils;

import java.util.List;

public class ListUtils {

    public static boolean isEntry(int position, List list) {
        return isNotEmpty(list) && position >= 0 && position < list.size();
    }

    public static boolean isNotEmpty(List list) {
        return list != null && !list.isEmpty();
    }
}
