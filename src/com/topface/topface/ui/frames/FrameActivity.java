package com.topface.topface.ui.frames;

import com.topface.topface.Data;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Banner;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Device;
import com.topface.topface.utils.Http;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public abstract class FrameActivity extends Activity {
  // Data
  protected boolean mIsActive;
  // Abstract Methods
  abstract public void clearLayout();
  abstract public void fillLayout();
  abstract public void release();
  //---------------------------------------------------------------------------  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mIsActive = true;
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    mIsActive = false;
    release();
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  protected void updateUI(Runnable action) {
    if(mIsActive)
      runOnUiThread(action);
  }
  //---------------------------------------------------------------------------
  public void updateBanner(final ImageView bannerView, final String bannerRequestName) {
    if(Data.screen_width<=Device.W_240 || bannerView==null || bannerRequestName==null)
      return;
    
    BannerRequest bannerRequest = new BannerRequest(getApplicationContext());
    bannerRequest.place = bannerRequestName;
    bannerRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        final Banner banner = Banner.parse(response);
        updateUI(new Runnable() {
          @Override
          public void run() {
            Http.bannerLoader(banner.url, bannerView);
            bannerView.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                Intent intent = null;
                if(banner.action.equals(Banner.ACTION_PAGE))
                  intent = new Intent(FrameActivity.this, BuyingActivity.class); // "parameter":"PURCHASE"
                else if(banner.action.equals(Banner.ACTION_URL)) {
                  intent = new Intent(Intent.ACTION_VIEW, Uri.parse(banner.parameter));
                }
                startActivity(intent);
              }
            });
          }
        });
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        Debug.log(this,"banner loading error");
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
}
