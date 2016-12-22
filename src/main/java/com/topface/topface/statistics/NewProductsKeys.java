package com.topface.topface.statistics;

import com.topface.statistics.processor.annotations.SendNow;
import com.topface.statistics.processor.annotations.SendPost;

/**
 * Ключи для генерируемой продуктовой статы
 * Created by tiberal on 22.12.16.
 */

public class NewProductsKeys {
    /**
     * Просмотр экрана покупки VIP >2 секунд
     */
    @SendPost(delay = 2)
    public static final String VIP_OPEN = "vip_open";
    /**
     * Просмотр баннера получения триального VIP-статуса
     */
    @SendNow
    public static final String TRIAL_VIP_POPUP_SHOW = "trial_vip_popup_show";
    /**
     * Открытие страницы размещения фотографии в фотоленте
     */
    @SendNow
    public static final String PHOTOFEED_SEND_OPEN = "photofeed_send_open";
    /**
     * Просмотр экрана объяснения фичи "Восхищение" в разделе "знакомства" >2 секунд
     */
    @SendPost(delay = 2)
    public static final String DATING_GO_ADMIRATION = "dating_go_admirations";
    /**
     * Тап по кнопке пополнения симпатий на зеро-дате симпатий
     */
    @SendNow
    public static final String LIKES_ZERODATA_GO_PURCHASES = "likes_zerodata_go_purchases";
    /**
     * Просмотр экрана заблокированных симпатий >2 секунд
     */
    @SendPost(delay = 2)
    public static final String LIKES_BLOCKED_OPEN = "likes_blocked_open";
    /**
     * Просмотр списка подарков, параметры: ref dating/profile/chat, источник посещения
     */
    @SendNow
    public static final String GIFTS_OPEN = "gifts_open";
}
