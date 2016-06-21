package com.topface.framework.utils.config;

import android.support.annotation.IntDef;

import com.topface.framework.JsonUtils;
import com.topface.topface.utils.DateUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * Расширение для конфига, которое позволяет вести учет записи данных в конфиг
 * Created by tiberal on 20.06.16.
 */
public class DailyConfigExtension {

    private AbstractConfig mAbstractConfig;
    public static final int DEFAULT = 0;
    public static final int EVERY_DAY = 1;
    public static final int DAY_AFTER_LAST_EXTRACT = 2;

    /**
     * Режим работы конфига
     * DEFAULT - считаем каждую запись, никогда не скидываем
     * EVERY_DAY - считаем пока не наступит новый день, потом сначала
     * DAY_AFTER_LAST_EXTRACT - скидываем если прошли сутки с момента последнего чтения
     */
    @IntDef({DEFAULT, EVERY_DAY, DAY_AFTER_LAST_EXTRACT})
    @interface DailyMode {
    }

    public DailyConfigExtension(@NotNull AbstractConfig abstractConfig) {
        mAbstractConfig = abstractConfig;
    }

    public <T> void setDailyConfigField(@NotNull String configObjectKey, @NotNull T configObject) {
        DailyConfigField field = constructDailyConfigField(getConfigInfoByKey(configObjectKey), configObject);
        mAbstractConfig.setField(mAbstractConfig.getSettingsMap(), configObjectKey, field.toString());
        mAbstractConfig.saveConfig();
    }

    /**
     * @return получить значение конфига и дополнительную информацию(колличство чтений и время последней записи)
     */
    @Nullable
    public <T> DailyConfigField<T> getDailyConfigField(@NotNull String configObjectKey, Type type) {
        return JsonUtils.fromJson(getDailyConfigFieldJSON(configObjectKey), type);
    }

    @NotNull
    private String getDailyConfigFieldJSON(@NotNull String configObjectKey) {
        return mAbstractConfig.getStringField(mAbstractConfig.getSettingsMap(), configObjectKey);
    }

    /**
     * @return получам дополнительную информацию о поле, изменяем ее в соответствии с новыми реалиями
     * и перезаписываем поле конфига с новым значение, и новой дополнительной информацией
     */
    @NotNull
    private DailyConfigFieldInfo getConfigInfoByKey(@NotNull String configObjectKey) {
        return JsonUtils.fromJson(getDailyConfigFieldJSON(configObjectKey)
                , DailyConfigField.class).getConfigFieldInfo();
    }

    @NotNull
    private <T> DailyConfigField constructDailyConfigField(@NotNull DailyConfigFieldInfo info, @NotNull T data) {
        long currentTime = System.currentTimeMillis();
        switch (info.mMode) {
            case EVERY_DAY:
                if (DateUtils.isDayBeforeToday(info.getLastWriteTime())) {
                    info.resetAmount();
                }
                info.incrementAmount();
                info.setWriteTime(currentTime);
                break;
            case DAY_AFTER_LAST_EXTRACT:
                if (currentTime - info.getLastWriteTime() >= DateUtils.DAY_IN_MILLISECONDS) {
                    info.setWriteTime(currentTime);
                    info.resetAmount();
                }
                info.incrementAmount();
                break;
            case DEFAULT:
                info.setWriteTime(currentTime);
                info.incrementAmount();
                break;
        }
        return new DailyConfigField<>(data, info);
    }

    public static class DailyConfigField<T> {

        private T mConfigField;
        private DailyConfigFieldInfo mConfigFieldInfo;


        public DailyConfigField(@NotNull T configObject, @NotNull DailyConfigFieldInfo info) {
            mConfigField = configObject;
            mConfigFieldInfo = info;
        }

        public DailyConfigField(@NotNull T configObject, @DailyMode int mode) {
            this(configObject, new DailyConfigFieldInfo(mode, 0));
        }

        @SuppressWarnings("unused")
        public DailyConfigField(@NotNull T configObject) {
            this(configObject, new DailyConfigFieldInfo(DEFAULT, 0));
        }

        @SuppressWarnings("unchecked")
        @NotNull
        public T getConfigField() {
            return mConfigField;
        }

        @NotNull
        public DailyConfigFieldInfo getConfigFieldInfo() {
            return mConfigFieldInfo;
        }

        @Override
        public String toString() {
            return JsonUtils.toJson(this);
        }
    }

    public static class DailyConfigFieldInfo {
        @DailyMode
        private int mMode;
        private long mWriteTime;
        private long mAmount = 1;

        private DailyConfigFieldInfo(@DailyMode int mode, long extractTime) {
            mMode = mode;
            mWriteTime = extractTime;
        }

        private void incrementAmount() {
            mAmount++;
        }

        private void resetAmount() {
            mAmount = 1;
        }

        public long getAmount() {
            return mAmount;
        }

        public long getLastWriteTime() {
            return mWriteTime;
        }

        public void setWriteTime(long writeTime) {
            this.mWriteTime = writeTime;
        }
    }
}
