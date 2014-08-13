package com.topface.billing;

import com.topface.framework.JsonUtils;
import com.topface.topface.BuildConfig;

/**
 * Структура дополнительных данных для покупки
 */
public class DeveloperPayload {
    /**
     * Topface id пользователя, который совершил покупку
     */
    public int uid;
    /**
     * Место вызова экрана покупки, нужно для статистики
     */
    public String source;
    /**
     * Версия кода приложения, для определения версии где произшла покупа
     */
    public int codeVersion;
    /**
     * id продукта. Нужен для тестовых покупок и на всякий случай для обычных
     */
    public String sku;

    /**
     * Случайный id покупки, нужен что бы идентифицировать одинаковые запросы (например тестовые)
     */
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private final int hash;

    public DeveloperPayload(int uid, String sku, String source) {
        this.uid = uid;
        this.source = source;
        this.sku = sku;
        this.codeVersion = BuildConfig.VERSION_CODE;
        this.hash = hashCode();

    }

    public String toJson() {
        return JsonUtils.toJson(this);
    }
}