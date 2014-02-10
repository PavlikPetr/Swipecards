package com.topface.topface.requests;

import android.content.Context;

import java.util.List;

/**
 * Абстрактный класс - основая для простого создания запросов удаления элементов из списков, где id элемента = id юзера
 */
abstract public class DeleteAbstractUsersRequest extends DeleteAbstractRequest {
    public DeleteAbstractUsersRequest(List<String> ids, Context context) {
        super(ids, context);
    }

    public DeleteAbstractUsersRequest(String id, Context context) {
        super(id, context);
    }

    @Override
    protected String getKeyForItems() {
        return "userIds";
    }
}
