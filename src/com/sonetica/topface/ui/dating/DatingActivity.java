package com.sonetica.topface.ui.dating;

import java.util.List;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Filter;
import com.sonetica.topface.data.SearchUser;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.FilterRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.net.SearchRequest;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/*
 *    "оценка фото"
 */
public class DatingActivity extends Activity {
  // Data
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_dating);
    Debug.log(this,"+onCreate");
    
    // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.dating_header_title));
   
   update();
   filter();
  }
  //---------------------------------------------------------------------------
  public void filter() {
    FilterRequest request = new FilterRequest(this);
    request.city = "2";
    request.sex = "1";
    request.agebegin = "0";
    request.ageend = "0";
    request.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        List list = Filter.parse(response.getSearch());
      }
      @Override
      public void fail(int codeError) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  public void update() {
    SearchRequest request = new SearchRequest(this);
    request.limit = "20";
    request.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        List list = SearchUser.parse(response.getSearch());
        if(list.size()>0)
          ;
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
