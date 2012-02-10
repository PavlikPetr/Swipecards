package com.sonetica.topface.ui.dating;

import java.util.LinkedList;
import android.content.Context;
import com.sonetica.topface.data.SearchUser;

public class DatingGalleryAdapter {
  // Data
  private Context mContext;
  private LinkedList<SearchUser> mSearchUserList;
  private int curr_id = -1;
  // Http.imageLoader(mSearchUserList.get(0).getLink(),mDatingLayout.mImageView);
  //---------------------------------------------------------------------------
  public DatingGalleryAdapter(Context context,LinkedList<SearchUser> userList) {
    mContext = context;
    mSearchUserList = userList;
  }
  //---------------------------------------------------------------------------
  public SearchUser getUser() {
    SearchUser user = mSearchUserList.get(++curr_id);
    if(curr_id==mSearchUserList.size()-5)
      ((DatingActivity)mContext).update();
    return user;
  }
  //---------------------------------------------------------------------------
}
