package com.sonetica.topface.net;

import java.util.ArrayList;
import java.util.LinkedList;

public class BitmapManager {
  // Data
  private ArrayList  mUrls;  //список ссылок на изображения полученные из JSON ответа topfase сервера
  private LinkedList mQueue; //очередь ожидающих вьюшек на подгрузку битмапа
  //---------------------------------------------------------------------------
  public BitmapManager() {
    
  }
  //---------------------------------------------------------------------------
}

// сохранять фотки в кэш
// проверять, есть ли в кеше перед загрузкой