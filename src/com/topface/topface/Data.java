package com.topface.topface;

import java.util.LinkedList;
import android.graphics.Bitmap;
import com.topface.topface.data.Album;
import com.topface.topface.data.City;

/*
 *   Сохранение данных в течении жизни процесса
 */
public class Data {
  // Data
  public static LinkedList<City>  s_CitiesList;
  public static LinkedList<Album> s_PhotoAlbum;
  public static Bitmap s_UserAvatar;
  public static Bitmap s_OwnerAvatar;
}
