package com.sonetica.topface.ui.tops;

import com.sonetica.topface.R;
import com.sonetica.topface.utils.Http;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class RateitFragment extends Fragment {
  // Data
  private String mUrl;
  //---------------------------------------------------------------------------
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    if(container == null)
      return null;

    ViewGroup layout = (ViewGroup)inflater.inflate(R.layout.fragment_viewpager, container, false);
    final ImageView avatar = (ImageView)(layout.findViewById(R.id.ivRateAvatar));
    //avatar.setBackgroundResource(R.drawable.im_dashbrd_tops);
    avatar.post(new Runnable() {
      @Override
      public void run() {
        if(mUrl==null)
          return;
        Bitmap bitmap = Http.bitmapLoader(mUrl);
        if(bitmap!=null)
          avatar.setImageBitmap(bitmap);
      }
    });
    
    return layout;
  }
  //---------------------------------------------------------------------------
  public void setUrl(String url) {
    mUrl = url;    
  }
  //---------------------------------------------------------------------------
}
