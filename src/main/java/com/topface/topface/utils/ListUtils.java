package com.topface.topface.utils;

import java.util.List;

public class ListUtils {

    public static boolean isEntry(int position, List list) {
        return isNotEmpty(list) && position >= 0 && position < list.size();
    }

    public static boolean isNotEmpty(List list) {
        return list != null && !list.isEmpty();
    }

    public static boolean isNotEmpty(int[] list) {
        return list != null && list.length > 0;
    }

    private static void clearList(List list) {
        if (list != null) {
            list.clear();
        }
    }

    public static void clearLists(List... list) {
        if (list != null) {
            for (List item : list) {
                clearList(item);
            }
        }
    }
}
