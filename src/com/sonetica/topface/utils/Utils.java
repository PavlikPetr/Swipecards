package com.sonetica.topface.utils;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.HashMap;
import com.sonetica.topface.R;
import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.widget.TextView;

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
  public static void formatTime(TextView tv,long time) {
    Context context = tv.getContext();
    String text;
    long now = System.currentTimeMillis()/1000;
    long full_time = time * 1000;
    long t = now - time;
    if((time > now) || t < 60)
      text = context.getString(R.string.time_now);
    else if(t < 3600)
      text = formatMinute(context,t/60);
    else if(t < 6*3600)
      text = formatHour(context,t/3600);
    else if(DateUtils.isToday(full_time))
      text = context.getString(R.string.time_today)+DateFormat.format(" kk:mm",full_time).toString();
    else { 
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.HOUR_OF_DAY,0);
      cal.set(Calendar.MINUTE,0);
      if(full_time > (now-(now-cal.getTimeInMillis())-(24*60*60*1000)))
        text = context.getString(R.string.time_yesterday)+DateFormat.format(" kk:mm",full_time).toString();
      else
        text = DateFormat.format("dd.MM.yyyy kk:mm",full_time).toString();
    }
    tv.setText(text);
  }
  //---------------------------------------------------------------------------
  private static String formatHour(Context context,long hours) {
    byte caseValue = 0;
    if((hours < 11) || (hours > 19)) {
      if(hours%10 == 1)
        caseValue = 1;
      if((hours%10 == 2) || (hours%10 == 3) || (hours%10 == 4))
        caseValue = 2;
    }
    switch(caseValue) {
     case 1:  return String.format(context.getString(R.string.time_hour_0),hours);
     case 2:  return String.format(context.getString(R.string.time_hour_1),hours);
     default: return String.format(context.getString(R.string.time_hours),hours);
    }
  }
  //---------------------------------------------------------------------------
  private static String formatMinute(Context context,long minutes) {
    byte caseValue = 0;
    if((minutes < 11) || (minutes > 19)) {
      if(minutes%10 == 1)
        caseValue = 1;
      if((minutes%10 == 2) || (minutes%10 == 3) || (minutes%10 == 4))
        caseValue = 2;
    }
    switch(caseValue) {
     case 1:  return String.format(context.getString(R.string.time_minute_0),minutes);
     case 2:  return String.format(context.getString(R.string.time_minute_1),minutes);
     default: return String.format(context.getString(R.string.time_minutes),minutes);
    }
  }
  //---------------------------------------------------------------------------
}
