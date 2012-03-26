package com.sonetica.topface.social;

import com.sonetica.topface.R;
import com.sonetica.topface.ui.dashboard.DashboardActivity;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


/**
 * Класс активити выбора социальной сети для аутентификации
 */
public class SocialActivity extends Activity implements View.OnClickListener {
  // Data
  public static final int INTENT_SOCIAL_ACTIVITY = 102;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_social);
    
    // clear web cache
    //deleteDatabase("webview.db");
    //deleteDatabase("webviewCache.db");
    
    // VKontakte Button
    ((Button)findViewById(R.id.btnSocialVk)).setOnClickListener(this);
    
    // Facebook Button
    ((Button)findViewById(R.id.btnSocialFb)).setOnClickListener(this);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onActivityResult(int requestCode,int resultCode,Intent data) {
    if(requestCode != SocialWebActivity.INTENT_SOCIAL_WEB)
      return;
    if(resultCode == Activity.RESULT_OK) {
      startActivity(new Intent(this,DashboardActivity.class));
      finish();
    }
  }  
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View button) {
    Intent intent = new Intent(SocialActivity.this, SocialWebActivity.class);
    switch(button.getId()) {
      case R.id.btnSocialVk:
        intent.putExtra(SocialWebActivity.TYPE,SocialWebActivity.TYPE_VKONTAKTE);
        break;
      case R.id.btnSocialFb:
        intent.putExtra(SocialWebActivity.TYPE,SocialWebActivity.TYPE_FACEBOOK);
        break;
    }
    startActivityForResult(intent,SocialWebActivity.INTENT_SOCIAL_WEB);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------  
}
