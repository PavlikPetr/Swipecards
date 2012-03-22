package com.sonetica.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
//import org.json.JSONObject;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

/*
 * Класс профиля окна топов
 */
public class Symphaty extends AbstractData {
  // Data
  /*
  {Number} unread - текущее значение счетчика непрочитанных симпатий пользователя
  {Array<Object>} feed - список входящих симпатий

  Формат элемента массива следующий:
  {Number} id - идентификатор симпатии в ленте
  {Number} uid - идентификатор отправителя
  {String} first_name - имя отправителя в текущей локали
  {Number} age - возраст отправителя

  {Object} city - описатель города отправителя
  {Number} city.id - идентификатор города
  {String} city.name - наименование города в локали указанной при авторизации
  {String} city.full - полное наименование города с указанием региона, если он определен. Отдается в локали пользователя, указанной при авторизации

  {Boolean} online - флаг нахождения отправителя онлайн
  {Boolean} unread - флаг прочитанной симпатии

  {Object} avatars - описатель главной фото отправителя
  {String} avatars.big - фото большого размера
  {String} avatars.small - фото маленького размера

  При выполнении запроса с указанием new:true симпатии, являющиеся непрочитанными, становятся прочитанными.
  */
  //---------------------------------------------------------------------------
  public static LinkedList<Symphaty> parse(Response response) {
    LinkedList<Symphaty> symphatyList = new LinkedList<Symphaty>();
    
    try {
      JSONArray arr = response.mJSONResult.getJSONArray("top");
      if(arr.length()>0) 
        for(int i=0;i<arr.length();i++) {
          //JSONObject item = arr.getJSONObject(i);
          Symphaty symphaty = new Symphaty();

          symphatyList.add(symphaty);
        }
    } catch(Exception e) {
      Debug.log("TopUser.class","Wrong response parsing: " + e);
    }
    
    return symphatyList;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getBigLink() {
    return null;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getSmallLink() {
    return null;
  }
  //---------------------------------------------------------------------------
}
