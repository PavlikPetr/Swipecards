package com.topface.topface.requests.transport;

import com.google.gson.annotations.SerializedName;

/**
 * Представление http заголовков
 */
public class Headers {
    /**
     * Парсим мы только Content-Type, остальные заголовки нам не нужны
     */
    @SerializedName("Content-Type")
    public String contentType;

    @SerializedName("Scruffy-Id")
    public String scruffyId;

    public Headers(String scruffyId, String contentType) {
        this.scruffyId = scruffyId;
        this.contentType = contentType;
    }

}
