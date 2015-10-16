package com.topface.topface.utils.controllers.startactions;

/**
 * Интерфейс для реализации очереди попапов. onNextAction нужно вызватm при закрытии попапа.
 * В реализации интерфейса необходимо указать что делать после закрытия попапа. Реализацию назначить
 * классу реализующему IStartAction
 * Created by onikitin on 18.03.15.
 */
public interface OnNextActionListener {

    void onNextAction();

    void saveNextActionPosition();

}
