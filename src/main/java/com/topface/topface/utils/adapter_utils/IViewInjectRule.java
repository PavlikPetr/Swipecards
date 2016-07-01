package com.topface.topface.utils.adapter_utils;

/**
 * Правило, которое отвечает за выбор позиции для вставки вьюхи в списке.
 * Created by tiberal on 23.06.16.
 */
public interface IViewInjectRule {

    boolean isNeedInject(int pos);

}
