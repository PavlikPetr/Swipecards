package com.sonetica.topface;

import java.util.LinkedList;
import com.sonetica.topface.data.City;
import com.sonetica.topface.data.Inbox;
import com.sonetica.topface.data.Like;
import com.sonetica.topface.data.Rate;
import com.sonetica.topface.data.TopUser;

/*
 *   Сохранение данных в течении жизни процесса
 */
public class Data {
  // Data
  public static LinkedList<Inbox>   s_InboxList;
  public static LinkedList<Like>    s_LikesList;
  public static LinkedList<TopUser> s_TopsList;
  public static LinkedList<Rate>    s_RatesList;
  public static LinkedList<City>    s_CitiesList;
  //---------------------------------------------------------------------------
  public static void init() {
    s_InboxList  = new LinkedList<Inbox>();
    s_LikesList  = new LinkedList<Like>();
    s_TopsList   = new LinkedList<TopUser>();
    s_RatesList  = new LinkedList<Rate>();
    s_CitiesList = new LinkedList<City>();
  }
  //---------------------------------------------------------------------------
  public static void clear() {
    s_InboxList.clear();
    s_InboxList = null;
    s_LikesList.clear();
    s_LikesList = null;
    s_TopsList.clear();
    s_TopsList = null;
    s_RatesList.clear();
    s_RatesList = null;
    s_CitiesList.clear();
    s_CitiesList = null;
  }
  //---------------------------------------------------------------------------
}
