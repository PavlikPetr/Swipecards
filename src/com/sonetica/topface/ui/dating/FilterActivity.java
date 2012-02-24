package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.FilterRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class FilterActivity extends Activity {
  // Data
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_filter);
    Debug.log(this,"+onCreate");
  }
  //---------------------------------------------------------------------------
  public void filter() {
    // вызывает окно фильтра и передает параметры 
    FilterRequest request = new FilterRequest(this);
    request.city     = 2;
    request.sex      = 0;
    request.agebegin = 16;
    request.ageend   = 20;
    request.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        //Filter filter = Filter.parse(response);
        Toast.makeText(FilterActivity.this,"filter success",Toast.LENGTH_SHORT).show();
      }
      @Override
      public void fail(int codeError) {
        Toast.makeText(FilterActivity.this,"filter fail",Toast.LENGTH_SHORT).show();
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
