package com.topface.topface.requests

import android.content.Context
import org.json.JSONObject

/**
 * Created by tiberal on 01.12.16.
 * Запрос для шапки в диалогах
 * @param limit - {Number} количество запрашиваемых элементов ленты. ОДЗ: [1;50] по умолчанию 10
 * @param from -  {Number} идентификатор элемента ленты, с которого имеет смысл подгузить данные. ОДЗ: [0;∞)
 * @param to - {Number} идентификатор элемента ленты, до которого подгружать данные. ОДЗ: [0;∞)
 */

class MutualBandGetListRequest(context: Context, private val limit: Int, private val form: Int? = null
                               , private val to: Int? = null) : ApiRequest(context) {

    override fun getServiceName() = "mutualBand.getList"

    override fun getRequestData() = JSONObject().apply {
        put("limit", limit)
        put("from", form)
        put("to", to)
    }

}