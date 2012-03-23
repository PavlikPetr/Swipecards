package com.sonetica.topface.data;

import com.sonetica.topface.net.ApiResponse;

/*
 *   Абстрактный класс для пакетов полученных с сервера на запросы
 */
public abstract class AbstractData {
  public static Object parse(ApiResponse response) { return null; }  // разбор ответа сервера
  public abstract String getBigLink();    // получение линка на изображение для скачивания
  public abstract String getSmallLink();  // получение линка на изображение для скачивания
}
