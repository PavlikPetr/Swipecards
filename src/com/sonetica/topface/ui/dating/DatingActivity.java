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
import com.sonetica.topface.ui.ProfileActivity;
import com.sonetica.topface.ui.album.AlbumActivity;
import com.sonetica.topface.ui.inbox.ChatActivity;
import com.sonetica.topface.ui.inbox.InboxActivity;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.content.Intent;
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
  private DatingGallery mDatingGallery;
  private DatingManager mDatingManager;
  private LinkedList<SearchUser> mSearchUserList;
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

   // Data
   mSearchUserList = new LinkedList<SearchUser>();
   
   // Dating Gallery
   mDatingGallery = (DatingGallery)findViewById(R.id.galleryDating);
   mDatingManager = new DatingManager(this,mSearchUserList);
   mDatingGallery.setGalleryManager(mDatingManager);
   
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
        mSearchUserList.addAll(SearchUser.parse(response));
      }
      @Override
      public void fail(int codeError) {

      }
    }).exec();
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
  public void doRate(final int userid,final int rate) {
    DoRateRequest doRate = new DoRateRequest(this);
    doRate.userid = 6665705;
    doRate.rate   = rate;
    doRate.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        Toast.makeText(DatingActivity.this,"rate ok:"+rate+",id:"+userid,Toast.LENGTH_SHORT).show();
      }
      @Override
      public void fail(int codeError) {
        Toast.makeText(DatingActivity.this,"rate not ok:"+rate+",id:"+userid,Toast.LENGTH_SHORT).show();
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  public void openChat(int userid) {
    Intent intent = new Intent(this,ChatActivity.class);
    intent.putExtra(ChatActivity.INTENT_USER_ID,userid);
    startActivityForResult(intent,0);
  }
  //---------------------------------------------------------------------------
  public void openProfile(int userId) {
    Intent intent = new Intent(this,ProfileActivity.class);
    intent.putExtra(ProfileActivity.INTENT_USER_ID,userId);
    startActivityForResult(intent,0);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
}
