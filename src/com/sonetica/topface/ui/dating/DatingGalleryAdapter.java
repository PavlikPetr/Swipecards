package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import com.sonetica.topface.data.SearchUser;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Http;
import com.sonetica.topface.utils.Imager;
import com.sonetica.topface.utils.LeaksManager;
import com.sonetica.topface.utils.MemoryCache;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class DatingGalleryAdapter extends BaseAdapter {
  //---------------------------------------------------------------------------
  // class ViewHolder
  //---------------------------------------------------------------------------
  static class ViewHolder {
    ProgressBar mProgressBar;
    ImageView mImageView;
  };
  //---------------------------------------------------------------------------
  // Data
  private int mW = 0;              // длина
  private int mH = 0;              // высота
  private int mPrevPosition;         // предыдущая позиция фото в альбоме
  private int mPreRunning;           // текущая пред загружаемое фото
  private Bitmap mMainBitmap;        // жесткая ссылка на оцениваемую фотографию
  private MemoryCache mCache;        // кеш фоток
  private SearchUser  mUserData;     // данные пользователя               
  private LayoutInflater mInflater;          
  private DatingControl  mDatingControl;
  private AlphaAnimation mAlphaAnimation;
  //---------------------------------------------------------------------------
  public DatingGalleryAdapter(Context context,DatingControl datingControl) {
    mInflater = LayoutInflater.from(context);
    mDatingControl  = datingControl;
    mAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
    mAlphaAnimation.setDuration(200L);
    mCache = new MemoryCache();
  }
  //---------------------------------------------------------------------------
  public void setUserData(SearchUser user) {
    mUserData = user;
    // очистка
    mPreRunning = 0;
    mPrevPosition = 0;
    if(mMainBitmap!=null)
      mMainBitmap.recycle();
    mMainBitmap = null;
    mCache.clear();
  }
  //---------------------------------------------------------------------------
  public int getCount() {
    if(mUserData==null)
      return 0;
    return mUserData.avatars_big.length;
  }
  //---------------------------------------------------------------------------
  public Object getItem(int position) {
    if(mUserData==null)
      return null;
    return mUserData.avatars_big[position];
  }
  //---------------------------------------------------------------------------
  public long getItemId(int position) {
    if(mUserData==null)
      return 0;
    return position;
  }
  //---------------------------------------------------------------------------
  public View getView(final int position,View convertView, ViewGroup parent) {
    ViewHolder holder = null;
    
    if(convertView==null) {
      mW = mDatingControl.getWidth();
      mH = mDatingControl.getHeight();
      holder = new ViewHolder();
      convertView = (ViewGroup)mInflater.inflate(R.layout.item_album_gallery, null, false);
      convertView.setLayoutParams(new Gallery.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
      holder.mImageView   = (ImageView)convertView.findViewById(R.id.ivPreView);
      holder.mProgressBar = (ProgressBar)convertView.findViewById(R.id.pgrsAlbum);
      // ДОБАВИТЬ РАЗМЕРЫ
      convertView.setTag(holder);
    } else 
      holder = (ViewHolder)convertView.getTag();
    
    if(mUserData==null)
      return convertView;
    
    Bitmap bitmap = mCache.get(position);
    if(bitmap!=null && position==0) {
      holder.mImageView.setImageBitmap(bitmap);
      holder.mProgressBar.setVisibility(View.INVISIBLE);
    } else if(bitmap!=null && position!=0)
      holder.mImageView.setImageBitmap(bitmap);
    else {
      loadingImage(position, holder.mImageView,holder.mProgressBar);
    }
    
    int prePosition = position>=mPrevPosition ? position+1 : position-1;
    if(prePosition>0 && position<(getCount()-1))
      preLoading(prePosition,mW,mH);
    
    // кнопка back
    if(position>2 && position==getCount()-1)
      mDatingControl.controlVisibility(DatingControl.V_SHOW_BACK);
    
    mPrevPosition = position;

    return convertView;
  }
  //---------------------------------------------------------------------------
  public void loadingImage(final int position,final ImageView view,final ProgressBar progressBar) {
    Thread t = new Thread() {
      @Override
      public void run() {
        
        //if(view.getWidth()==0)
          //return;
        
        Bitmap rawBitmap  = Http.bitmapLoader(mUserData.avatars_big[position]);
        
        if(rawBitmap!=null && position==0)
          rawBitmap = Imager.clipping(rawBitmap,mW,mH);
        
        final Bitmap bitmap = rawBitmap;
        
        view.post(new Runnable() {
          @Override
          public void run() {
            
            if(bitmap==null) {
              view.setImageResource(R.drawable.icon_people);
              mDatingControl.controlVisibility(DatingControl.V_SHOW_INFO);
              return;
            }
            
            if(position==0) {
              progressBar.setVisibility(View.INVISIBLE);
              view.setAlpha(255);
              view.setImageBitmap(bitmap);
              mDatingControl.controlVisibility(DatingControl.V_SHOW_INFO);
              view.startAnimation(mAlphaAnimation);
              mMainBitmap = bitmap;
            } else
              view.setImageBitmap(bitmap);
            
            mCache.put(position,bitmap);
          }
        }); // view.post
        
      }
    };
    t.setPriority(Thread.MAX_PRIORITY);
    LeaksManager.getInstance().monitorObject(t);
    t.start();
  }
  //---------------------------------------------------------------------------
  public void preLoading(final int position,final int width,final int height) {
    if(position==mPreRunning || width==0)
      return;
    
    if(mCache.containsKey(position))
      return;
    
    Debug.log(this,"preloader:"+mPrevPosition+":"+position);
    
    Thread t = new Thread() {
      @Override
      public void run() {
        Bitmap rawBitmap  = Http.bitmapLoader(mUserData.avatars_big[position]);
        if(rawBitmap!=null && position==0)
          rawBitmap = Imager.clipping(rawBitmap,width,height);
        if(mCache!=null)
          mCache.put(position,rawBitmap);
      }
    };
    t.setPriority(Thread.MIN_PRIORITY);
    LeaksManager.getInstance().monitorObject(t);
    t.start();
    
    mPreRunning = position;
  }
  //---------------------------------------------------------------------------
  public void release() {
    if(mMainBitmap!=null)
      mMainBitmap.recycle();
    mMainBitmap = null;
    mCache.clear();
    mCache = null;
  }
  //---------------------------------------------------------------------------
}

//  Http.imageLoaderExp(url,view);

/*
// утечка памяти при работе с пулами
  mThreadsPool.execute(new Thread() {
    @Override
    public void run() {
      final Bitmap bitmap = Http.bitmapLoader(url);
      if(bitmap != null)
        view.post(new Runnable() {
          @Override
          public void run() {
            if(position==0) {
              view.setAlpha(255);
              view.setImageBitmap(bitmap);
              view.startAnimation(mAlphaAnimation);
              mDatingControl.controlVisibility(DatingControl.V_INFO);
            } else
              view.setImageBitmap(bitmap);
          }
        });
    }
  });
*/