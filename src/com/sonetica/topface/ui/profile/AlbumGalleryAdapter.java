package com.sonetica.topface.ui.profile;

public class AlbumGalleryAdapter{} /* extends BaseAdapter {
  // Data
  private LayoutInflater mInflater;
  private AlbumGalleryManager mGalleryManager; // менеджер изображений
  //---------------------------------------------------------------------------
  public AlbumGalleryAdapter(Context context,AlbumGalleryManager galleryManager) {
    mGalleryManager = galleryManager;
    mInflater = LayoutInflater.from(context);
  }
  //---------------------------------------------------------------------------
  public int getCount() {
    return mGalleryManager.size();
  }
  //---------------------------------------------------------------------------
  public Object getItem(int position) {
    return position;
  }
  //---------------------------------------------------------------------------
  public long getItemId(int position) {
    return position;
  }
  //---------------------------------------------------------------------------
  public View getView(final int position,View convertView, ViewGroup parent) {
    Debug.log(">>>>>>>>>>>>>>","GALLERY:"+convertView);
    
    convertView = (ViewGroup)mInflater.inflate(R.layout.item_album_gallery, null, false);
    ImageView imageView = (ImageView)convertView.findViewById(R.id.ivPreView);
    imageView.setMinimumHeight(300);
    imageView.setMinimumWidth(300);
    
    if(mGalleryManager.size()==0)
      return convertView;
    
    mGalleryManager.getImage(position,imageView);
    
    return convertView;
  }
  //---------------------------------------------------------------------------
}
*/