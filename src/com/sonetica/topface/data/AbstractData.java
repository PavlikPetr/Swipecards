package com.sonetica.topface.data;

import com.sonetica.topface.net.Response;

/*
 *   Абстрактный класс для пакетов полученных с сервера на запросы
 */
public abstract class AbstractData {
  public static Object parse(Response response) { return null; }  // разбор ответа сервера
  public String getLink(){ return null; };  // получение линка на изображение для скачивания
}
