package com.topface.topface;

import java.util.LinkedList;
import com.topface.topface.data.Album;
import com.topface.topface.data.City;

/*
 *   Сохранение данных в течении жизни процесса
 */
public class Data {
  // Data
  public static LinkedList<City>  s_CitiesList;
  public static LinkedList<Album> s_PhotoAlbum;
}
