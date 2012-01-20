package com.sonetica.topface.utils;

import java.security.MessageDigest;
import java.util.HashMap;

/*
 *  Набор вспомагательных функций
 */
public class Utils {
  //---------------------------------------------------------------------------
  public static int unixtime(){
    return (int)(System.currentTimeMillis() / 1000L);
  }
  //---------------------------------------------------------------------------
  public static String md5(String value) { 
    if(value==null) 
      return null; 
    try { 
      StringBuffer hexString = new StringBuffer();
      MessageDigest digester = MessageDigest.getInstance("MD5"); 
      digester.update(value.getBytes()); 
      byte[] bytes = digester.digest();
      for(int i=0; i<bytes.length; i++)
        hexString.append(Integer.toHexString(0xFF & bytes[i]));
      return hexString.toString();
    } catch (Exception e) { 
      return null; 
    } 
  } 
  //---------------------------------------------------------------------------
  public static HashMap<String, String> parseQueryString(String query) {
    String[] params = query.split("&");
    HashMap<String, String> map = new HashMap<String, String>();
    for(String param : params) {
      String name  = param.split("=")[0];
      String value = param.split("=")[1];
      map.put(name, value);
    }
    return map;
  }
  //---------------------------------------------------------------------------
}
