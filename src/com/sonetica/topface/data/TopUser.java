package com.sonetica.topface.data;

/*
 * Структура для хранения профиля в топе
 */
public class TopUser extends AbstractData {
  // Data
  public String uid;
  public String photo;
  public String liked;
  // Methods
  @Override
  public String getLink() {
    return photo;
  }
}
