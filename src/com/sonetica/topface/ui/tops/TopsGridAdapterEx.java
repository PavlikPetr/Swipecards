package com.sonetica.topface.ui.tops;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sonetica.topface.R;
import com.sonetica.topface.data.TopUser;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.ui.ThumbView;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Device;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/*
 * Класс адаптера для отображения галереи в Топ активити
 */
public class TopsGridAdapterEx extends BaseAdapter implements OnScrollListener {
  // Data
  private LayoutInflater mInflater;
  private LinkedList<TopUser> mData;
  //private ExecutorService mThread;
  private HashMap<Integer,Bitmap> mCache;
  private int mBitmapWidth;
  private int mBitmapHeight;
  private Bitmap[] mArray;
  // class ViewHolder
  static class ViewHolder {
    ThumbView mThumbButton;
  };
  //threads
  private boolean isScrolled; // idle(0) scrolling(1)
  private int startPosition;
  private int endPosition = startPosition + 10;
  //---------------------------------------------------------------------------
  public TopsGridAdapterEx(Context context,LinkedList<TopUser> topUserList) {
    mInflater = LayoutInflater.from(context);
    mData = topUserList;
    mBitmapWidth  = Device.getDisplay(context).getWidth()/4;
    mBitmapHeight = (int)(mBitmapWidth*1.25);
    //mThread = Executors.newSingleThreadExecutor();
    mCache  = new HashMap<Integer,Bitmap>();
    mArray  = new Bitmap[topUserList.size()];
    new Thread(RUN).start();
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mData.size();
  }
  //---------------------------------------------------------------------------
  @Override
  public View getView(int position,View convertView,ViewGroup parent) {
    ViewHolder holder = null;
    if(convertView==null) {
      holder = new ViewHolder();

      convertView = (ViewGroup)mInflater.inflate(R.layout.gallery_item, null, false);
      holder.mThumbButton = (ThumbView)convertView.findViewById(R.id.ivTG);
      holder.mThumbButton.setMinimumWidth(mBitmapWidth);
      holder.mThumbButton.setMinimumHeight(mBitmapHeight);

      convertView.setTag(holder);
    } else 
      holder = (ViewHolder)convertView.getTag();

    holder.mThumbButton.mPercent = mData.get(position).liked; 

    getImage(position,holder.mThumbButton);
    
    return convertView;
  }
  //---------------------------------------------------------------------------
  public void getImage(final int position,final ImageView imageView) {
    Debug.log(this,"#1 : " + isScrolled);
    if(isScrolled) {
      final Bitmap bitmap = mArray[position];
      if(bitmap!=null)
        imageView.setImageBitmap(bitmap);
      else
        imageView.setImageResource(R.drawable.im_black_square);      
      return;
    }
    Debug.log(this,"#2 : " + isScrolled);
    
    final Bitmap bitmap = mArray[position];
    if(bitmap!=null) {
      imageView.setImageBitmap(bitmap);
      return;
    }
    
    imageView.setImageResource(R.drawable.im_black_square);
    
    /*
    mThread.execute(new Runnable() {
      @Override
      public void run() {
        final Bitmap rawBitmap = Http.bitmapLoader(mData.get(position).getLink());
        if(rawBitmap==null)
          return;
        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(rawBitmap,mBitmapWidth,mBitmapHeight,false);
        if(scaledBitmap!=null) {
          imageView.post(new Runnable() {
            @Override
            public void run() {
              imageView.setImageBitmap(scaledBitmap);
            }
          });
          mArray[position] = scaledBitmap;
        }
      }
    });
    */
  }
  //---------------------------------------------------------------------------
  @Override
  public Object getItem(int position) {
    return null;
  }
  //---------------------------------------------------------------------------
  @Override
  public long getItemId(int position) {
    return position;
  }
  //---------------------------------------------------------------------------
  @Override
  public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount) {
    if(!isScrolled) {
      startPosition = firstVisibleItem;
      endPosition   = startPosition + 10; 
    }
  }
  //---------------------------------------------------------------------------
  @Override
  public void onScrollStateChanged(AbsListView view,int scrollState) {
    isScrolled = (scrollState==SCROLL_STATE_IDLE) ? false : true;
    Debug.log(this,"state : " + isScrolled);
  }
  //---------------------------------------------------------------------------
  private Runnable RUN = new Runnable() {
    @Override
    public void run() {
      /*
      while(!isScrolled) {
        final Bitmap rawBitmap = Http.bitmapLoader(mData.get(position).getLink());
        if(rawBitmap==null)
          return;
        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(rawBitmap,mBitmapWidth,mBitmapHeight,false);
        if(scaledBitmap!=null) {
          imageView.post(new Runnable() {
            @Override
            public void run() {
              imageView.setImageBitmap(scaledBitmap);
            }
          });
          mArray[position] = scaledBitmap;
        }
      }
      */
    }
  };
  //---------------------------------------------------------------------------
}




