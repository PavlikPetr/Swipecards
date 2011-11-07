package com.sonetica.topface.dashboard;

import com.sonetica.topface.PreferencesActivity;
import com.sonetica.topface.R;
import com.sonetica.topface.R.layout;
import com.sonetica.topface.R.string;
import com.sonetica.topface.social.SocialActivity;
import com.sonetica.topface.social.Socium;
import com.sonetica.topface.social.Socium.AuthException;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Класс главного активити для навигации по приложению
 */
public class DashboardActivity extends Activity {
  // Data
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_dashboard);
    Utils.log(this,"+onCreate");
    /*
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
      }
    });
    */
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    super.onDestroy();
    Utils.log(this,"-onDestroy");
  }
  //---------------------------------------------------------------------------
  // Menu
  //---------------------------------------------------------------------------
  private static final int MENU_ONE = 0;
  private static final int MENU_PREFERENCES = 1;
  @Override
  public boolean onCreatePanelMenu(int featureId, Menu menu) {
    menu.add(0,MENU_ONE,0,getString(R.string.dashbrd_one));
    menu.add(0,MENU_PREFERENCES,0,getString(R.string.dashbrd_preferences));
    return super.onCreatePanelMenu(featureId, menu);
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onMenuItemSelected(int featureId,MenuItem item) {
    switch (item.getItemId()) {
      case MENU_ONE:
        Toast.makeText(this,getString(R.string.dashbrd_one),Toast.LENGTH_SHORT).show();
        break;
      case MENU_PREFERENCES:
        startActivity(new Intent(this, PreferencesActivity.class));
        break;
    }
    return super.onMenuItemSelected(featureId,item);
  }
  //---------------------------------------------------------------------------
}
