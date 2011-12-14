package com.sonetica.topface.data;

/*
 * Структура для хранения профиля в топе
 */
public class TopUser {
  // Data
  public String uid   = "";
  public String name  = "";
  public String photo = "";
  public String liked = "";
  // Methods
  public TopUser(String uid, String photo, String liked) {
    this.uid   = uid;
    this.photo = photo;
    this.liked = liked;
  }
}
