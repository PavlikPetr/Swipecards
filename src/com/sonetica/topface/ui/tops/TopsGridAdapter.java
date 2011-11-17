package com.sonetica.topface.ui.tops;

import java.io.BufferedOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sonetica.topface.R;
import com.sonetica.topface.utils.Http;
import com.sonetica.topface.utils.BitmapCache;
import com.sonetica.topface.utils.ImageLoader;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/*
 * Класс адаптера для отображения галлереи в Топ активити
 */
public class TopsGridAdapter extends BaseAdapter {
  // Data
  private Context mContext;
  private ArrayList<String> mUrlList;  // список ссылок
  private BitmapCache mCache;   // кеш изображений
  private LinkedList<Pair<View,Integer>> mQueue; // очередь вьюшек на загрузку изображения
  private ExecutorService mThreadPool =  Executors.newFixedThreadPool(4); //пул потоков загружающих изображения
  //---------------------------------------------------------------------------
  public TopsGridAdapter(Context context, ArrayList<String> urlList) {
    mContext = context;
    mUrlList = urlList;
    mCache = new BitmapCache(context,"tops");
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mUrlList.size();
  }
  //---------------------------------------------------------------------------
  @Override
  public View getView(int position,View convertView,ViewGroup parent) {
    if(convertView == null) {
      convertView = new TopButton(mContext);
      convertView.setMinimumWidth(115);
      convertView.setMinimumHeight(115);
    }

    imageLoader(position,convertView);
    
    return convertView;
  }
  //---------------------------------------------------------------------------
  public void imageLoader(final int position,final View view) {
    // отдельный кеширующий поток менеджер, который сохраняет и подгружает
    if(!mCache.containsKey(position)) {
      mThreadPool.execute(new Runnable() {
        @Override
        public void run() {
          Bitmap bitmap = Http.bitmapLoader(mUrlList.get(position));
          mCache.put(position,bitmap);
          
          view.post(new Runnable() {
            @Override
            public void run() {
              ((ImageView)view).setImageBitmap(mCache.get(position));
              view.invalidate();
            }
          });
        }
      });
    } else {
      ((ImageView)view).setImageBitmap(mCache.get(position));
    }
  }
  //---------------------------------------------------------------------------
  public void setUrlList(ArrayList<String> urlList) {
    mUrlList = urlList;
  }
  //---------------------------------------------------------------------------
  @Override
  public Object getItem(int position) {
    return null;
  }
  //---------------------------------------------------------------------------
  @Override
  public long getItemId(int position) {
    return 0;
  }
  //---------------------------------------------------------------------------
}

// BitmapFactory.decodeFile
/*
try {
          File file = mContext.getFilesDir();
          BufferedOutputStream bos = null;
  bos = new BufferedOutputStream(new FileOutputStream(file));
} catch(FileNotFoundException e1) {e1.printStackTrace();}
          //try {bos.close();}catch(IOException e){e.printStackTrace();}
*/