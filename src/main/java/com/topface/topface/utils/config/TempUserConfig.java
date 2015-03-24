package com.topface.topface.utils.config;

import android.content.Context;

/**
 * Created by onikitin on 24.03.15.
 * Конфиг-заглушка. Если вдруг деление конфига проходит долго, а пользователю взумается поменять
 * настройки в это время, то сохраним их тут, а после деления мерджим его в рабочий конфиг.
 */
public class TempUserConfig extends UserConfig {

    public TempUserConfig(Context context) {
        super(context);
    }

    @Override
    protected void initData() {
        getSettingsMap();
    }

}
