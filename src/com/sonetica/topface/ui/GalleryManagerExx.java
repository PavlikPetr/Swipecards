package com.sonetica.topface.ui;

/*
 *  Менеджер изображений, загрузает и кеширует изображения
 */
public class GalleryManagerExx  { /*
  // Data
  private MemoryCache mCacheManager;
  private ExecutorService mThreadsPool;
  private LinkedList<? extends AbstractData> mData;
  //private int mStartPos;
  private int mThreadCount;
  public  int mBitmapWidth;
  public  int mBitmapHeight;
  public  boolean mRunning = true;
  //Constants
  private static final int THREAD_DEFAULT = 1;
  //---------------------------------------------------------------------------
  public GalleryManagerExx(Context context,LinkedList<? extends AbstractData> dataList) {
    this(context,dataList,THREAD_DEFAULT);
  }
  //---------------------------------------------------------------------------
  public GalleryManagerExx(Context context,LinkedList<? extends AbstractData> dataList,int threadCount) {
    mData         = dataList;
    mThreadCount  = threadCount;
    mCacheManager = new MemoryCache();
    mThreadsPool  = Executors.newFixedThreadPool(mThreadCount);
    mBitmapWidth  = Device.getDisplay(context).getWidth()/4;
    mBitmapHeight = (int)(mBitmapWidth*1.25);
  }
  //---------------------------------------------------------------------------
  public AbstractData get(int position) {
    return mData.get(position);
  }
  //---------------------------------------------------------------------------
  public void setStartPos(int startPosition) {
    //mStartPos = startPosition;
  }
  //---------------------------------------------------------------------------
  public void clearStartPos() {
    //mStartPos = -1;
  }
  //---------------------------------------------------------------------------
  public void getImage(final int position,final ImageView imageView) {
    final Bitmap bitmap = mCacheManager.get(position);
    if(bitmap==null)
      setImageToQueue(position,imageView);
    imageView.setImageBitmap(bitmap);
  }
  //---------------------------------------------------------------------------
  public void setImageToQueue(int position,ImageView imageView ) {
    
  }
  //---------------------------------------------------------------------------
  public void restart() {
    mThreadsPool = Executors.newFixedThreadPool(mThreadCount);
  }
  //---------------------------------------------------------------------------
  public void stop() {
    mThreadsPool.shutdown();
  }
  //---------------------------------------------------------------------------
  public int size() {
    return mData.size();
  }
  //---------------------------------------------------------------------------
  public void release() {
    mThreadsPool.shutdown();
    //mCacheManager.clear();
  }
  //---------------------------------------------------------------------------
  */
}
