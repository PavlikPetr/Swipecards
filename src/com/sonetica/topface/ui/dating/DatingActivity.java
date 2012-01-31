package com.sonetica.topface.ui.dating;

import java.util.LinkedList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Filter;
import com.sonetica.topface.data.SearchUser;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.DoRateRequest;
import com.sonetica.topface.net.FilterRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.net.SearchRequest;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/*
 *    "оценка фото"
 */
public class DatingActivity extends Activity {
  // Data
  DatingGallery mDatingGallery;
  LinkedList<SearchUser> mSearchUserList;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_dating);
    Debug.log(this,"+onCreate");

   // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.dating_header_title));

   // Button Header
   Button btnFilter = (Button)findViewById(R.id.tvHeaderButton);
   btnFilter.setText(getString(R.string.dating_header_button));
   btnFilter.setOnClickListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       Toast.makeText(DatingActivity.this,"Filter",Toast.LENGTH_SHORT).show();
     }
   });

   // Dating Gallery
   mDatingGallery = (DatingGallery)findViewById(R.id.galleryDating);
   
   update();
   //filter();
  }
  //---------------------------------------------------------------------------
  public void update() {
    SearchRequest request = new SearchRequest(this);
    request.limit = 20;
    request.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        mSearchUserList = SearchUser.parse(response);
        if(mSearchUserList.size()>0)
          mDatingGallery.setUserList(mSearchUserList);
      }
      @Override
      public void fail(int codeError) {

      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  public void doRate(final int userid,final int rate) {
    DoRateRequest doRate = new DoRateRequest(this);
    doRate.userid = userid;
    doRate.rate   = rate;
    doRate.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        Toast.makeText(DatingActivity.this,"rate:"+rate+",id:"+userid,Toast.LENGTH_SHORT).show();
      }
      @Override
      public void fail(int codeError) {
      }
    });
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
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
