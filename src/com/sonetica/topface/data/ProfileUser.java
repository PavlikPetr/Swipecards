package com.sonetica.topface.data;

import java.util.List;
import org.json.JSONObject;

public class ProfileUser extends AbstractData {
  // Data
  /*
    {Array} profiles - список профилей пользователей
    Формат элемента массива следующий:
    {Number} uid - идентификатор пользователя
    {String} first_name - имя пользователя
    {String} platform - платформа пользователя
    {String} first_name_translit - имя пользователя в транслитерации
    {Number} age - возраст пользователя
    {Number} sex - секс пользователя
    {Number} last_visit - таймстамп последнего посещения приложения
    {String} status - статус пользователя
    {Bool} online - флаг наличия пользвоателя в онлайне

    {Object} avatars - массив аватарок пользователя
    {String} avatars.big - большая аватарка пользователя
    {String} avatars.small - маленькая аватарка пользователя

    {Object} geo - геопозиционные данные пользователя
    {String} geo.city - наименование города пользователя
    {Number} geo.city_id - идентификатор города пользователя
    {Null} geo.distance - дистация до пользователя (всегда NULL)
    {Object} geo.coordinates - координаты пользователя
    {Object} geo.coordinates.lat - широта нахождения пользоавтеля
    {Object} geo.coordinates.lng - долгота нахождения пользователя
    */
  //---------------------------------------------------------------------------
  public static List<? extends AbstractData> parse(JSONObject response) {
    return null;
  }
  //---------------------------------------------------------------------------
}

/*
{"result":
  {"profiles":
    {"3246948":{"uid":3246948,
                "platform":"st",
                "first_name":"\u0415\u043a\u0430\u0442\u0435\u0440\u0438\u043d\u0430",
                "first_name_translit":"Ekaterina",
                "age":"22",
                "sex":"0",
                "last_visit":"2011-12-14 03:45:38",
                "status":"\u041c\u0430\u043c\u0435 \u0437\u044f\u0442\u044c \u043d\u0435 \u043d\u0443\u0436\u0435\u043d. \u0422\u0435\u043b\u0435\u0444\u043e\u043d \u043f\u043e\u0442\u0435\u0440\u044f\u043b\u0430. \u0414\u043e\u043c\u0430\u0448\u043d\u0435\u0433\u043e \u043d\u0435\u0442. \u041a\u043e\u0444\u0435 \u043d\u0435 \u043f\u044c\u044e. \u041c\u0430\u0448\u0438\u043d\u043e\u0439 \u043d\u0435 \u0443\u0434\u0438\u0432\u0438\u0448\u044c. \u0412\u0434\u0443\u0432\u0430\u0442\u0435\u043b\u044f\u043c - \u0432\u0434\u0443\u0432\u0430\u0439\u0442\u0435 \u0441\u0435\u0431\u0435 \u0432 \u043a\u0443\u043b\u0430\u0447\u043e\u043a))",
                "online":false,
                "avatars":{"big":"http:\/\/cs1696.vkontakte.ru\/u51993064\/96762451\/m_0c0b8072.jpg",
                "small":"http:\/\/cs1696.vkontakte.ru\/u51993064\/96762451\/s_a13a7cd6.jpg"},
                "geo":{"city":"\u041c\u043e\u0441\u043a\u0432\u0430","city_id":"1","distance":null,"coordinates":{"lat":null,"lng":null}}}}}}
*/