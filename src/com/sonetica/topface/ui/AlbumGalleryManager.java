package com.sonetica.topface.ui;

/*
 *  Менеджер изображений, загрузает и кеширует изображения
 */
public class AlbumGalleryManager { /*
  // Data
  private MemoryCache mMemoryCache;
  private LinkedList<? extends AbstractData> mData;
  //---------------------------------------------------------------------------
  public AlbumGalleryManager(Context context,LinkedList<? extends AbstractData> dataList) {
    mData = dataList;
    mMemoryCache  = new MemoryCache();
  }
  //---------------------------------------------------------------------------
  public AbstractData get(int position) {
    return mData.get(position);
  }
  //---------------------------------------------------------------------------
  public void getImage(final int position,ImageView imageView) {
    Bitmap bitmap = mMemoryCache.get(position);
    
    if(bitmap!=null)
      imageView.setImageBitmap(bitmap);
    else {
      //imageView.setImageBitmap(null);
      loadingImage(position,imageView);
    }
  }
  //---------------------------------------------------------------------------
  private void loadingImage(final int position,final ImageView view) {
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        final Bitmap rawBitmap = Http.bitmapLoader(mData.get(position).getBigLink());
        if(rawBitmap==null) return;
        
        mMemoryCache.put(position,rawBitmap);
        
        view.post(new Runnable() {
          @Override
          public void run() {
            view.setImageBitmap(rawBitmap);
          }
        });
        
      }
    });
    LeaksManager.getInstance().monitorObject(t);
    t.start();
  }
  //---------------------------------------------------------------------------
  public void preload(final Integer index) {
    if(index>=size())
      return;
    new Thread(new Runnable() {
      @Override
      public void run() {
        Bitmap bitmap = Http.bitmapLoader(mData.get(index).getBigLink());
        mMemoryCache.put(index,bitmap);
      }
    }).start();
  }
  //---------------------------------------------------------------------------
  public int size() {
    return mData.size();
  }
  //---------------------------------------------------------------------------
  public void release() {
    mMemoryCache.clear();
  }
  //---------------------------------------------------------------------------
*/}




