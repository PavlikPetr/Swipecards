package com.topface.topface.utils;

import java.util.List;

public class ListUtils {

    public static boolean isEntry(int position, List list) {
        return !list.isEmpty() && position >= 0 && position < list.size();
    }
}
