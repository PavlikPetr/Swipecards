package com.topface.topface.statistics;

import com.topface.topface.utils.FlurryManager;
import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

/**
 * Говнометоды для Flurry
 * Created by tiberal on 27.05.16.
 */
public class FlurryUtils {

    public static String getOpenEventNаme(@Nullable String className) {
        try {
            Class cls = Class.forName(className);
            if (cls.isAnnotationPresent(FlurryOpenEvent.class)) {
                Annotation annotation = cls.getAnnotation(FlurryOpenEvent.class);
                FlurryOpenEvent flurryOpenEvent = (FlurryOpenEvent) annotation;
                return flurryOpenEvent.name();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Utils.EMPTY;
    }

    public static void sendOpenEvent(@Nullable String className) {
        if (className != null) {
            try {
                ScreensShowStatistics.sendScreenShow(Class.forName(className).getSimpleName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            FlurryManager.getInstance().sendPageOpenEvent(getOpenEventNаme(className));
        }
    }

}
