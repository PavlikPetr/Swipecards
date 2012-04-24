package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;

/*
 *   Абстрактный класс для пакетов полученных с сервера на запросы
 */
public abstract class AbstractData implements IAlbumData {
  //разбор ответа сервера
  public static Object parse(ApiResponse response) {   
    return null; 
  }
  //получение линка на изображение для скачивания
  public String getBigLink() {
    return null;
  };
  //получение линка на изображение для скачивания
  public String getSmallLink() {
    return null;
  };
}
