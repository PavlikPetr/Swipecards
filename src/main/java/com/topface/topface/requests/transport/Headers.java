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

    @SerializedName("User-Agent")
    public String userAgent;

    @SerializedName("http-status")
    public Integer status = null;

    public Headers(String scruffyId, String contentType, String userAgent) {
        this.scruffyId = scruffyId;
        this.contentType = contentType;
        this.userAgent = userAgent;
    }

}
