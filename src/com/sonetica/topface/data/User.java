package com.sonetica.topface.data;

/*
 * Структура профиля пользователя (не владельца устройства с программой)
 */
public class User {
  // Data
  public String uid   = "";
  public String name  = "";
  public String photo = "";
  public String liked = "";
  // Methods
  public User(String uid, String photo, String liked) {
    this.uid   = uid;
    this.photo = photo;
    this.liked = liked;
  }
}
