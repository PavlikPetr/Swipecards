package com.topface.topface.ui.likes;

/*
 *  Класс адаптера для отображения галереи в Like активити
 */
public class LikesGridAdapterEx{} /* extends BaseAdapter {
  // Data
  Context mContext;
  private LayoutInflater mInflater;
  private GalleryGridManager<Like> mGalleryManager;
  // class ViewHolder
  static class ViewHolder {
    ThumbView mThumbView;
  };
  //---------------------------------------------------------------------------
  public LikesGridAdapterEx(Context context,GalleryGridManager<Like> galleryManager) {
    mContext = context;
    mInflater = LayoutInflater.from(context);
    mGalleryManager = galleryManager;
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mGalleryManager.size();
  }
  //---------------------------------------------------------------------------
  @Override
  public View getView(int position,View convertView,ViewGroup parent) {
    ViewHolder holder = null;
    if(convertView==null) {
      holder = new ViewHolder();
      convertView = (ViewGroup)mInflater.inflate(R.layout.item_grid_gallery, null, false);
      holder.mThumbView = (ThumbView)convertView.findViewById(R.id.ivTG);
      holder.mThumbView.setMinimumWidth(mGalleryManager.mBitmapWidth);
      holder.mThumbView.setMinimumHeight(mGalleryManager.mBitmapHeight);
      //holder.miv.setScaleType(ScaleType.CENTER);
      convertView.setTag(holder);
    } else 
      holder = (ViewHolder)convertView.getTag();

    //holder.mTopButton.mPercent = mGalleryCachedManager.get(position).liked;
    
    Like like = ((Like)mGalleryManager.get(position));
    
    holder.mThumbView.mAge    = like.age;
    holder.mThumbView.mName   = like.first_name;
    holder.mThumbView.mOnline = like.online;
        
    mGalleryManager.getImage(position,holder.mThumbView);
    
    return convertView;
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
}
*/