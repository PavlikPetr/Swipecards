package com.sonetica.topface.ui.dating;

import java.util.LinkedList;
import android.content.Context;
import com.sonetica.topface.data.SearchUser;
import com.sonetica.topface.net.Http;

public class DatingManager {
  // Data
  //Http.imageLoader(mSearchUserList.get(0).getLink(),mDatingLayout.mImageView);
  private int curr_user;
  private LinkedList<SearchUser> mSearchUserList;
  private Context mContext;
  //---------------------------------------------------------------------------
  public DatingManager(Context context,LinkedList<SearchUser> userList) {
    mContext = context;
    mSearchUserList = userList;
  }
  //---------------------------------------------------------------------------
  public SearchUser getPrevUser() {
    SearchUser user = mSearchUserList.get(++curr_user);
    if(curr_user==mSearchUserList.size()-2) {
      ((DatingActivity)mContext).update();
    }
    return user;
  }
  //---------------------------------------------------------------------------
  public SearchUser getNextUser() {
    SearchUser user = mSearchUserList.get(++curr_user);
    if(curr_user==mSearchUserList.size()-2) {
      ((DatingActivity)mContext).update();
    }
    return user;
  }
  //---------------------------------------------------------------------------
}
