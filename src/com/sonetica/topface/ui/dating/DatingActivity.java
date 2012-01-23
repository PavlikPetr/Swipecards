package com.sonetica.topface.ui.dating;

import java.util.LinkedList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Filter;
import com.sonetica.topface.data.SearchUser;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.FilterRequest;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.net.SearchRequest;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

/*
 *    "оценка фото"
 */
public class DatingActivity extends Activity {
  // Data
  ImageView mDatingView;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_dating);
    Debug.log(this,"+onCreate");
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.dating_header_title));
   mDatingView = (ImageView)findViewById(R.id.ivDating);
   
   update();
   //filter();
  }
  //---------------------------------------------------------------------------
  public void filter() {
    FilterRequest request = new FilterRequest(this);
    request.city     = 2;
    request.sex      = 0;
    request.agebegin = 16;
    request.ageend   = 40;
    request.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        Filter filter = Filter.parse(response);
      }
      @Override
      public void fail(int codeError) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  public void update() {
    SearchRequest request = new SearchRequest(this);
    request.limit = 20;
    request.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        LinkedList<SearchUser> list = SearchUser.parse(response);
        if(list.size()>0)
          Http.imageLoader(list.get(0).getLink(),mDatingView);
      }
      @Override
      public void fail(int codeError) {

      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
