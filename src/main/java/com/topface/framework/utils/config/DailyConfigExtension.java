package com.topface.framework.utils.config;

import android.annotation.SuppressLint;
import android.support.annotation.IntDef;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
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
        saveDailyConfigField(configObjectKey, field);
    }

    private void saveDailyConfigField(@NotNull String configObjectKey, @NotNull DailyConfigField field) {
        mAbstractConfig.setField(mAbstractConfig.getSettingsMap(), configObjectKey, field.toString());
        mAbstractConfig.saveConfig();
    }

    /**
     * @return получить значение конфига и дополнительную информацию(колличство чтений и время последней записи)
     */
    @Nullable
    public <T> DailyConfigField<T> getDailyConfigField(@NotNull String configObjectKey, Type type) {
        DailyConfigField<T> configField = JsonUtils.fromJson(getDailyConfigFieldJSON(configObjectKey), type);
        resetAndSaveFieldInfoIfNeed(configObjectKey, configField);
        return configField;
    }

    /**
     * Если на момент запроса поля прошел заданные интервал, то обнуляем информацию о поле
     */
    private void resetAndSaveFieldInfoIfNeed(@NotNull String configObjectKey, @NotNull DailyConfigField configField) {
        DailyConfigFieldInfo info = configField.getConfigFieldInfo();
        if (isNeedResetAmount(info)) {
            info.resetAmount();
            info.setWriteTime(0);
            saveDailyConfigField(configObjectKey, configField);
        }
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
            case DAY_AFTER_LAST_EXTRACT:
                calculateAmount(isNeedResetAmount(info), info);
                break;
            case DEFAULT:
                info.incrementAmount();
                break;
        }
        info.setWriteTime(currentTime);
        return new DailyConfigField<>(data, info);
    }

    @SuppressLint("SwitchIntDef")
    private boolean isNeedResetAmount(@NotNull DailyConfigFieldInfo info) {
        long lastWriteTime = info.getLastWriteTime();
        switch (info.mMode) {
            case EVERY_DAY:
                return lastWriteTime != 0 && DateUtils.isDayBeforeToday(lastWriteTime);
            case DAY_AFTER_LAST_EXTRACT:
                long currentTime = System.currentTimeMillis();
                return lastWriteTime != 0 && currentTime - lastWriteTime >= DateUtils.DAY_IN_MILLISECONDS;
            default:
                return false;
        }
    }

    private void calculateAmount(boolean isNeedReset, DailyConfigFieldInfo info) {
        if (isNeedReset) {
            info.resetAmount();
        } else {
            info.incrementAmount();
        }
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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof DailyConfigField)) return false;
            DailyConfigField<?> that = (DailyConfigField<?>) o;
            return mConfigField != null ? mConfigField.equals(that.mConfigField) :
                    that.mConfigField == null && (mConfigFieldInfo != null ?
                            mConfigFieldInfo.equals(that.mConfigFieldInfo) :
                            that.mConfigFieldInfo == null);

        }

        @Override
        public int hashCode() {
            int result = mConfigField != null ? mConfigField.hashCode() : 0;
            result = 31 * result + (mConfigFieldInfo != null ? mConfigFieldInfo.hashCode() : 0);
            return result;
        }
    }

    public static class DailyConfigFieldInfo {

        private final static int DEFAULT_AMOUNT = 1;
        @DailyMode
        private int mMode;
        private long mWriteTime;
        private long mAmount = DEFAULT_AMOUNT;

        private DailyConfigFieldInfo(@DailyMode int mode, long extractTime) {
            mMode = mode;
            mWriteTime = extractTime;
        }

        private void incrementAmount() {
            Debug.log("FullscreenController : DailyConfigExtension incrementAmount ");
            mAmount++;
        }

        private void resetAmount() {
            Debug.log("FullscreenController : DailyConfigExtension resetAmount ");
            mAmount = DEFAULT_AMOUNT;
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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof DailyConfigFieldInfo)) return false;
            DailyConfigFieldInfo that = (DailyConfigFieldInfo) o;
            return mMode == that.mMode &&
                    mWriteTime == that.mWriteTime &&
                    mAmount == that.mAmount;

        }

        @Override
        public int hashCode() {
            int result = mMode;
            result = 31 * result + (int) (mWriteTime ^ (mWriteTime >>> 32));
            result = 31 * result + (int) (mAmount ^ (mAmount >>> 32));
            return result;
        }
    }
}
