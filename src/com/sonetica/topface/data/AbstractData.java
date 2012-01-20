package com.sonetica.topface.data;

import org.json.JSONObject;

public abstract class AbstractData {
  public static Object parse(JSONObject response) { return null; }  // разбор ответа сервера
  public String getLink(){ return null; };  // получение линка на изображение для скачивания
}
