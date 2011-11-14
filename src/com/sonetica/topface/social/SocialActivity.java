package com.sonetica.topface.social;

import com.sonetica.topface.R;
import com.sonetica.topface.utils.Utils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


/**
 * Класс активити выбора социальной сети для аутентификации
 */
public class SocialActivity extends Activity {
  // Data
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_social);
    
    // VKontakte Button
    ((Button)findViewById(R.id.btnSocialVk)).setOnClickListener(
      new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(SocialActivity.this, SocialWebActivity.class);
          intent.putExtra(SocialWebActivity.TYPE,SocialWebActivity.TYPE_VKONTAKTE);
          startActivity(intent);
        }
      });
    
    // Facebook Button
    ((Button)findViewById(R.id.btnSocialFb)).setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Intent intent = new Intent(SocialActivity.this, SocialWebActivity.class);
            intent.putExtra(SocialWebActivity.TYPE,SocialWebActivity.TYPE_FACEBOOK);
            startActivity(intent);
          }
        });
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    super.onDestroy();
    Utils.log(this,"-onDestroy");
  }
  //---------------------------------------------------------------------------
}
