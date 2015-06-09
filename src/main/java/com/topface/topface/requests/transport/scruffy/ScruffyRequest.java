package com.topface.topface.requests.transport.scruffy;

import com.topface.framework.JsonUtils;
import com.topface.topface.requests.transport.Headers;

/**
 * Представление данных (и запрос и ответ), которое шлется в Scruffy
 * <p>
 * Кто такой Scruffy?
 * Scruffy - ws proxy для мобильного api Topface.
 *
 * @link https://github.com/Topface/scruffy
 * <p>
 * ░░░░░░░░░░░░░░░░░░░▄▄▄████████░░░░░░░░░
 * ░░░░░░░░░░░░░░░░▄▄█████████████░░░░▄███
 * ░░░░░░░░░░░░░░▄████████████████▄▄████▀░
 * ░░░░░░░░░░░░░░▀████████████████████▀░░░
 * ░░░░░░░░░░░░░░░███████████████▀░█▀░░░░░
 * ░░░░░░░░░░░░░░░▀█░░░░▀▀▀▀▀░░░░░░░▀▄░░░░
 * ░░░░░░░░░░░░░░░░█░░░░░░░░▀▀▀▄░░░░▀█▄░░░
 * ░░░░░░░░░░░░░░░░█░░░░░▄▄▀██▀█░▄▀▀█▀█░░░
 * ░░░░░░░░░░░░░░░▄█▄░░░░▀▄░░░▄▀░░▀▄▄▄▀░░░
 * ░░░░░░░░░░░░░░█▄▄▄░░░░░░███▄▄▄▄░░░█▄░░░
 * ░░░░░░░░░░░░░░▀▄█▄░░░░░░▄▀░░░░▀▀▀▀░░▀▄░
 * ░░░░░░░░░░░░░░▄▄█░░░░░░█░░░░░░░░░░░░░░█
 * ░░░░░░░░░░░░░████░░░░░█░░▄▀▄▀▄▀▄▀▄▀▄▀▄█
 * ░░░░░░░░░▄▄█████░░░░░░▀▄▀░░░░░░▄█░░░░░░
 * ░░░░░░▄█████████▄░░░░░░░░░░░░▄█░░░░░░░░
 * ░░░▄▄███████████▀▀▄▄▄░░░░░▄███▄░░░░░░░░
 * ░▄██████████████░░░░▀▀▀▀▀▀▀░████▄░░░░░░
 * ▄███████████████░░░░░░░░░░░░█████▄░░░░░
 * █████████████████▄▄▄▄▄▄▄▄▄▄▄██████▄░░░░
 */
public class ScruffyRequest {
    /**
     * Все заголовки запроса/ответа из API
     */
    private Headers headers;
    /**
     * Тело запроса/ответа из API
     */
    private String body;

    public ScruffyRequest(Headers headers, String body) {
        this.headers = headers;
        this.body = body;
    }

    public String getContentType() {
        return headers != null ? headers.contentType : null;
    }

    public String getBody() {
        return body;
    }

    public Integer getHttpStatus() {
        if (headers != null) {
            return headers.status == null ? -1 : headers.status;
        }
        return -1;
    }

    @Override
    public String toString() {
        return JsonUtils.toJson(this);
    }

    public String getId() {
        return headers != null ? headers.scruffyId : null;
    }
}
