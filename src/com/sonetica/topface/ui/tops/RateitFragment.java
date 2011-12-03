package com.sonetica.topface.ui.tops;

import com.sonetica.topface.R;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.utils.Utils;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class RateitFragment extends Fragment {
  // Data
  private String mUrl;
  private ImageView mAvatar;
  //---------------------------------------------------------------------------  
  @Override
  public void onAttach(Activity activity) {
    Utils.log(null,"onAttach");
    super.onAttach(activity);
  }
  //---------------------------------------------------------------------------
  @Override
  public void onCreate(Bundle savedInstanceState) {
    Utils.log(null,"onCreate");
    super.onCreate(savedInstanceState);
  }

  //---------------------------------------------------------------------------
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Utils.log(null,"onCreateView");
    
    if(container == null)
      return null;
    
    ViewGroup layout = (ViewGroup)inflater.inflate(R.layout.fragment_viewpager, container, false);
    mAvatar = (ImageView)(layout.findViewById(R.id.ivRateAvatar));
    //avatar.setBackgroundResource(R.drawable.im_dashbrd_tops);
    
    return layout;
  }
  //---------------------------------------------------------------------------
  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    Utils.log(null,"onActivityCreated");
    
    mAvatar.post(new Runnable() {
      @Override
      public void run() {
        if(mUrl==null)
          return;
        Utils.log(null,"download Image");
        Bitmap bitmap = Http.bitmapLoader(mUrl);
        if(bitmap!=null)
          mAvatar.setImageBitmap(bitmap);
      }
    });
    
    super.onActivityCreated(savedInstanceState);
  }
  //---------------------------------------------------------------------------
  @Override
  public void onStart() {
    Utils.log(null,"onStart");
    super.onStart();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onResume() {
    Utils.log(null,"onResume");
    super.onResume();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onPause() {
    Utils.log(null,"onPause");
    super.onPause();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onStop() {
    Utils.log(null,"onStop");
    super.onStop();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onDestroyView() {
    Utils.log(null,"onDestroyView");
    super.onDestroyView();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onDestroy() {
    Utils.log(null,"onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onDetach() {
    Utils.log(null,"onDetach");
    super.onDetach();
  }
  //---------------------------------------------------------------------------
  public void setUrl(String url) {
    mUrl = url;    
  }
  //---------------------------------------------------------------------------

  
}
