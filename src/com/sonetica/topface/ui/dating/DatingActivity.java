package com.sonetica.topface.ui.dating;

import java.util.LinkedList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.SearchUser;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.DoRateRequest;
import com.sonetica.topface.net.FilterRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.net.SearchRequest;
import com.sonetica.topface.ui.ProfileActivity;
import com.sonetica.topface.ui.dating.DatingGallery.DatingEventListener;
import com.sonetica.topface.ui.inbox.ChatActivity;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/*
 *    "оценка фото"
 */
public class DatingActivity extends Activity implements DatingEventListener {
  // Data
  public static View mHeaderBar;
  private DatingGallery mDatingGallery;
  private DatingGalleryAdapter mDatingAdapter; 
  private LinkedList<SearchUser> mSearchUserList;
  public  static int HEADER_HEIGHT;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_dating);
    Debug.log(this,"+onCreate");
    
    // Data
    mSearchUserList = new LinkedList<SearchUser>();

   // Title Header
   ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.dating_header_title));
   
   mHeaderBar = findViewById(R.id.tvHeaderBar);
   mHeaderBar.setBackgroundColor(R.drawable.im_dating_header_bar);
   HEADER_HEIGHT = 50; 

   /*
   // Button Header
   Button btnFilter = (Button)findViewById(R.id.tvHeaderButton);
   btnFilter.setText(getString(R.string.dating_header_button));
   btnFilter.setOnClickListener(new View.OnClickListener() {
     @Override
     public void onClick(View v) {
       filter();
     }
   });
   */

   // Dating Gallery
   mDatingGallery = (DatingGallery)findViewById(R.id.galleryDating);
   mDatingAdapter = new DatingGalleryAdapter(this,mSearchUserList);
   mDatingGallery.setAdapter(mDatingAdapter);
   mDatingGallery.setEventListener(this);
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
        mDatingGallery.notifyDataChanged();
        
        Toast.makeText(DatingActivity.this,"update success",Toast.LENGTH_SHORT).show();
      }
      @Override
      public void fail(int codeError) {
        Toast.makeText(DatingActivity.this,"update fail",Toast.LENGTH_SHORT).show();
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  public void filter() {
    // вызывает окно фильтра и передает параметры 
    FilterRequest request = new FilterRequest(this);
    request.city     = 2;
    request.sex      = 0;
    request.agebegin = 16;
    request.ageend   = 40;
    request.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        //Filter filter = Filter.parse(response);
        Toast.makeText(DatingActivity.this,"filter success",Toast.LENGTH_SHORT).show();
      }
      @Override
      public void fail(int codeError) {
        Toast.makeText(DatingActivity.this,"filter fail",Toast.LENGTH_SHORT).show();
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  public void rate(final int userid,final int rate) {
    DoRateRequest doRate = new DoRateRequest(this);
    doRate.userid = userid;
    doRate.rate   = rate;
    doRate.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        Toast.makeText(DatingActivity.this,"rate success:"+rate+",id:"+userid,Toast.LENGTH_SHORT).show();
      }
      @Override
      public void fail(int codeError) {
        Toast.makeText(DatingActivity.this,"rate fail:"+rate+",id:"+userid,Toast.LENGTH_SHORT).show();
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  public void openProfileActivity(int userId) {
    Intent intent = new Intent(this,ProfileActivity.class);
    intent.putExtra(ProfileActivity.INTENT_USER_ID,userId);
    startActivityForResult(intent,0);    
  }
  //---------------------------------------------------------------------------
  public void openChatActivity(int userId) {
    Intent intent = new Intent(this,ChatActivity.class);
    intent.putExtra(ChatActivity.INTENT_USER_ID,userId);
    startActivityForResult(intent,0);
  }
  //---------------------------------------------------------------------------
  public void onRate(int userid,int index) {
    rate(userid,index);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  // Menu
  //---------------------------------------------------------------------------
  private static final int MENU_FILTER = 0;
  @Override
  public boolean onCreatePanelMenu(int featureId, Menu menu) {
    menu.add(0,MENU_FILTER,0,getString(R.string.dating_menu_one));
    return super.onCreatePanelMenu(featureId, menu);
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onMenuItemSelected(int featureId,MenuItem item) {
    switch (item.getItemId()) {
      case MENU_FILTER:
        filter();
      break;
    }
    return super.onMenuItemSelected(featureId,item);
  }
  //---------------------------------------------------------------------------
}
