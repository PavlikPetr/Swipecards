package com.topface.topface.utils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.data.Profile;
import com.topface.topface.utils.config.AppConfig;

public class Editor {

    //Тип режима редактора
    //Режим редактора включен, в зависимости от того, является ли пользователь редактором на проде
    public static final int MODE_USER_FIELD = 0;
    //Режим редактора включен всегда, вне зависимостей типа пользователя
    public static final int MODE_EDITOR = 1;
    //Режим редактора выключен всегда
    public static final int MODE_NOT_EDITOR = 2;

    /**
     * Текущий режим для редакторов
     */
    private static boolean mUserFieldEditor;
    private static int mEditorMode = MODE_USER_FIELD;

    /**
     * Устанавливает текущий режим редактора, получая данные из конфига приложения
     *
     * @param config настройки приложения
     */
    public static void setConfig(AppConfig config) {
        if (config != null) {
            mEditorMode = config.getEditorMode();
            //Нужно обновить данные для дебага
            Debug.setDebugMode(config.getDebugMode());
        }
    }

    public static void init(Profile profile) {
        mUserFieldEditor = profile.isEditor();
        Debug.setDebugMode(App.getAppConfig().getDebugMode());
    }

    public static boolean isEditor() {
        switch (mEditorMode) {
            case MODE_EDITOR:
                return true;
            case MODE_USER_FIELD:
                return mUserFieldEditor;
            case MODE_NOT_EDITOR:
            default:
                return false;
        }
    }
}
