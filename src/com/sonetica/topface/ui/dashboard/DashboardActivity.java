package com.sonetica.topface.ui.dashboard;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.PreferencesActivity;
import com.sonetica.topface.R;
import com.sonetica.topface.R.layout;
import com.sonetica.topface.R.string;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.social.AuthToken;
import com.sonetica.topface.social.SocialActivity;
import com.sonetica.topface.social.Socium;
import com.sonetica.topface.social.Socium.AuthException;
import com.sonetica.topface.ui.tops.TopsActivity;
import com.sonetica.topface.utils.Memory;
import com.sonetica.topface.utils.Utils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Класс главного активити для навигации по приложению
 */
public class DashboardActivity extends Activity implements View.OnClickListener {
  // Data
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_dashboard);
    Utils.log(this,"+onCreate");
    
    /*
     //регистрация через сервер topface
    String url = "http://api.topface.ru/?v=1";
    String request = "q=";
    
    AuthToken.Token token = new AuthToken(this).getToken(); 
    
    JSONObject obj = new JSONObject();
    try {
      obj.put("service","auth");
      obj.put("sid",token.getUserId());
      obj.put("token",token.getTokenKey());
      obj.put("platform",token.getSocialNet());
    } catch(JSONException e) {
      e.printStackTrace();
    }
    
    //request = request+obj.toString();
    
    String response = Http.httpPostRequest(url,request);
    response+="";
    
    ((TextView)findViewById(R.id.txt)).setText("memory:"+Memory.getUsedHeap()/1024);
    final ImageView ava=(ImageView)findViewById(R.id.avatar);
    ava.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new Thread(new Runnable() {
          @Override
          public void run() {
            Socium socium = null;
            try {
              socium = new Socium(DashboardActivity.this); 
            } catch(AuthException e) {
              startActivity(new Intent(DashboardActivity.this, SocialActivity.class));
            }
//            final Bitmap bmp = socium.getAvatar();
//            if(bmp!=null)
//              ava.post(new Runnable() {
//                @Override
//                public void run() {
//                  ava.setImageBitmap(bmp);  
//                }
//              });           
          }
        }).start();
      }8888
    });
    */
    
    ((Button)findViewById(R.id.btnDashbrdChat)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDashbrdTops)).setOnClickListener(this);
    ((Button)findViewById(R.id.btnDashbrdProfile)).setOnClickListener(this);
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View view) {
    
    if(!Http.isOnline(this)){
      Toast.makeText(this,getString(R.string.internet_off),Toast.LENGTH_SHORT).show();
      return;
    }
    
    switch(view.getId()) {
      case R.id.btnDashbrdChat: {
        Toast.makeText(this,"chat",Toast.LENGTH_SHORT).show();
      } break;
      case R.id.btnDashbrdTops: {
        Toast.makeText(this,"tops",Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this,TopsActivity.class));
      } break;
      case R.id.btnDashbrdProfile: {
        Toast.makeText(this,"profile",Toast.LENGTH_SHORT).show();
      } break;
      default:
    }
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Utils.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  // Menu
  //---------------------------------------------------------------------------
  private static final int MENU_ONE = 0;
  private static final int MENU_PREFERENCES = 1;
  @Override
  public boolean onCreatePanelMenu(int featureId, Menu menu) {
    menu.add(0,MENU_ONE,0,getString(R.string.dashbrd_menu_one));
    menu.add(0,MENU_PREFERENCES,0,getString(R.string.dashbrd_menu_preferences));
    return super.onCreatePanelMenu(featureId, menu);
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onMenuItemSelected(int featureId,MenuItem item) {
    switch (item.getItemId()) {
      case MENU_ONE:
        Toast.makeText(this,getString(R.string.dashbrd_menu_one),Toast.LENGTH_SHORT).show();
        break;
      case MENU_PREFERENCES:
        startActivity(new Intent(this, PreferencesActivity.class));
        break;
    }
    return super.onMenuItemSelected(featureId,item);
  }
  //---------------------------------------------------------------------------
}
