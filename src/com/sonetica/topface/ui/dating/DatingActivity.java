package com.sonetica.topface.ui.dating;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.DoRate;
import com.sonetica.topface.data.SearchUser;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.DoRateRequest;
import com.sonetica.topface.net.MessageRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.net.SearchRequest;
import com.sonetica.topface.ui.dating.DatingControl.OnNeedUpdateListener;
import com.sonetica.topface.ui.dating.StarsView.OnRateListener;
import com.sonetica.topface.ui.inbox.ChatActivity;
import com.sonetica.topface.ui.profile.ProfileActivity;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/* "оценка фото" */
public class DatingActivity extends Activity implements OnNeedUpdateListener,OnRateListener,OnClickListener{
  // Data
  private DatingControl mDatingControl;
  private Dialog   mCommentDialog;
  private EditText mCommentText;
  private InputMethodManager mInputManager;
  // Constants
  public static ViewGroup mHeaderBar;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_dating);
    Debug.log(this,"+onCreate");

    // Header Bar
    mHeaderBar = (ViewGroup)findViewById(R.id.loHeader);
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.dating_header_title));
    
    // Chat Button
    ((Button)findViewById(R.id.chatBtn)).setOnClickListener(this);
    
    // Profile Button
    ((Button)findViewById(R.id.profileBtn)).setOnClickListener(this);
    
    // Dating Gallery
    mDatingControl = (DatingControl)findViewById(R.id.galleryDating);
    mDatingControl.setOnNeedUpdateListener(this);
    
    // Stars Button
    ((StarsView)findViewById(R.id.starsView)).setOnRateListener(this);

    // Клавиатура
    mInputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    
    // Comment window
    mCommentDialog = new Dialog(this);
    mCommentDialog.setTitle(R.string.chat_comment);    
    mCommentDialog.setContentView(R.layout.popup_comment);
    //mCommentDialog.getWindow().setBackgroundDrawableResource(R.drawable.popup_comment);
    mCommentDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    mCommentText = (EditText)mCommentDialog.findViewById(R.id.etPopupComment);
   
    update(true);
  }
  //---------------------------------------------------------------------------
  public void update(final boolean firstQuery) {
    SearchRequest request = new SearchRequest(this.getApplicationContext());
    request.limit  = 20;
    request.geo    = Data.s_Profile.filter_geo;
    request.online = Data.s_Profile.filter_online;
    request.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        LinkedList<SearchUser> userList = SearchUser.parse(response);
        if(firstQuery)
          mDatingControl.addDataList(userList);
        else
          mDatingControl.setDataList(userList);
      }
      @Override
      public void fail(int codeError) {
        Toast.makeText(DatingActivity.this,"dating update fail",Toast.LENGTH_SHORT).show();
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void rate(final int userid,final int rate,String comment) {
    DoRateRequest doRate = new DoRateRequest(this.getApplicationContext());
    doRate.userid  = userid;
    doRate.rate    = rate;
    doRate.comment = comment;
    doRate.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        DoRate rate = DoRate.parse(response);
        Data.s_Power = rate.power;
        Data.s_Money = rate.money;
        Data.s_AverageRate = rate.average;
      }
      @Override
      public void fail(int codeError) {
        //Toast.makeText(DatingActivity.this,"dating rate failed",Toast.LENGTH_SHORT).show();
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  @Override
  public void needUpdate() {
    update(false);
  }
  //---------------------------------------------------------------------------
  @Override
  public void onRate(final int rate) {
    if(rate==10 || rate==9) {
      ((Button)mCommentDialog.findViewById(R.id.btnPopupCommentSend)).setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          String comment = mCommentText.getText().toString();
          if(comment.equals(""))
            return;
          int uid = mDatingControl.getUserId();
          
          // отправка комментария к оценке
          MessageRequest message = new MessageRequest(DatingActivity.this.getApplicationContext());
          message.message = comment; 
          message.userid  = uid;
          message.callback(new ApiHandler() {
            @Override
            public void success(Response response) {
            }
            @Override
            public void fail(int codeError) {
            }
          }).exec();
          
          // отправка оценки
          rate(uid,rate,comment);
          
          mCommentText.setText("");
          mCommentDialog.cancel();
          
          // скрыть клавиатуру
          mInputManager.hideSoftInputFromWindow(mCommentText.getWindowToken(),InputMethodManager.HIDE_IMPLICIT_ONLY);
          
          // подгрузка следующего
          mDatingControl.next();
        }
      });
      
      // показать окно отправки сообщения
      mCommentDialog.show();
      
    } else {
      rate(mDatingControl.getUserId(),rate,null);
      mDatingControl.next();
    }
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View view) {
    switch(view.getId()) {
      case R.id.chatBtn: {
        Intent intent = new Intent(this,ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_USER_ID,mDatingControl.getUserId());
        startActivityForResult(intent,0);
      } break;
      case R.id.profileBtn: {
        Intent intent = new Intent(this,ProfileActivity.class);
        intent.putExtra(ProfileActivity.INTENT_USER_ID,mDatingControl.getUserId());
        startActivityForResult(intent,0);
      } break;
    }
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onActivityResult(int requestCode,int resultCode,Intent data) {
    super.onActivityResult(requestCode,resultCode,data);
    if(resultCode == Activity.RESULT_OK && requestCode == FilterActivity.INTENT_FILTER_ACTIVITY) {
      update(true);
    }
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    mDatingControl.release();
    mDatingControl = null;
    mHeaderBar = null;

    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  // Menu
  //---------------------------------------------------------------------------
  private static final int MENU_FILTER = 0;
  @Override
  public boolean onCreatePanelMenu(int featureId,Menu menu) {
    menu.add(0,MENU_FILTER,0,getString(R.string.dating_menu_one));
    return super.onCreatePanelMenu(featureId,menu);
  }
  //---------------------------------------------------------------------------
  @Override
  public boolean onMenuItemSelected(int featureId,MenuItem item) {
    switch(item.getItemId()) {
      case MENU_FILTER:
        Intent intent = new Intent(this,FilterActivity.class);
        startActivityForResult(intent,FilterActivity.INTENT_FILTER_ACTIVITY);
      break;
    }
    return super.onMenuItemSelected(featureId,item);
  }
  //---------------------------------------------------------------------------  
}
