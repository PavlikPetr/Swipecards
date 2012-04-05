package com.topface.topface.ui.dating;

import java.util.LinkedList;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.SearchUser;
import com.topface.topface.ui.dating.DatingActivity;
import com.topface.topface.ui.dating.FaceView;
import com.topface.topface.ui.dating.RateControl;
import com.topface.topface.ui.dating.ResourcesView;
import com.topface.topface.utils.Debug;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

/*    ЛАПША КОД - СКОРОСТЬ превыше КАЧЕСТВА
 *    разобраться с листенерами
 */
public class DatingControl extends ViewGroup {
  //---------------------------------------------------------------------------
  // interface OnNeedUpdateListener
  //---------------------------------------------------------------------------
  interface OnNeedUpdateListener {
    public void needUpdate();
  }
  // Data
  private boolean mNotHide;    // блокировка скрытия инфо пользователя
  private int mDataPosition;   // позиция в массиве пользователей на оценку
  private int mGallerySize;    // кол-во фото у оцениваемого пользователя
  private int mGalleryPrevPos; // предыдущая позиция в альбоме
  // Gallery
  private DatingAlbum mDatingGallery;
  private DatingAlbumAdapter mGalleryAdapter;
  // Views
  private Button mBackButton;      // кнопка возврата к оцениваемой фотографии
  private TextView mCounter;         // счетчик позиции в галерее пользователя
  private FaceView mFaceView;          // информация о пользователе
  private RateControl mRateControl;      // звезды
  private ResourcesView mResourcesView;    // монеты и энергия
  private LinkedList<SearchUser> mDataList;  // массив пользователей под оценку
  private ProgressBar mProgressBar;
  private OnNeedUpdateListener mOnNeedUpdateListener;  // нужно подтянуть еще пользователей под оценку
  // Visible States
  public static final int V_SWAP_ALL  = 0;  // состояния скрытия элементов на экране
  public static final int V_SHOW_ALL  = 1;
  public static final int V_HIDE_ALL  = 2;
  public static final int V_SWAP_BACK = 3;
  public static final int V_SHOW_INFO = 4;
  public static final int V_SHOW_BACK = 5;
  //---------------------------------------------------------------------------
  public DatingControl(Context context,AttributeSet attrs) {
    super(context,attrs);

    // Data
    mDataList = new LinkedList<SearchUser>();
    
    // Adapter
    mGalleryAdapter = new DatingAlbumAdapter(context,this);
    mGalleryAdapter.registerDataSetObserver(new DataSetObserver() {
      @Override
      public void onChanged() {
        // Обновление после оценки
        DatingControl.this.setCounter(mGalleryPrevPos+1,mGallerySize); 
      }
      @Override
      public void onInvalidated() {}
    });
    
    // Progress
    mProgressBar = new ProgressBar(getContext());
    //Widget.ProgressBar.Small
    addView(mProgressBar);
    
    // Gallery
    mDatingGallery = new DatingAlbum(context, attrs);
    mDatingGallery.setAdapter(mGalleryAdapter);
    mDatingGallery.setSpacing(0);
    mDatingGallery.setBackgroundColor(Color.TRANSPARENT);
    mDatingGallery.setFadingEdgeLength(0);
    mDatingGallery.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> arg0,View arg1,int position,long arg3) {
        if(position==1 && mGalleryPrevPos==0) {
          DatingControl.this.controlVisibility(DatingControl.V_HIDE_ALL);
        } else if(position==0 && mGalleryPrevPos>0) {
          DatingControl.this.controlVisibility(DatingControl.V_SHOW_ALL);
        }
        mGalleryPrevPos = position;
        DatingControl.this.setCounter(mGalleryPrevPos+1,mGallerySize); //  УПРАВЛЕНИЕ СЧЕТЧИКОМ
      }
      @Override
      public void onNothingSelected(AdapterView<?> arg0) {}
    });
    mDatingGallery.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> arg0,View arg1,int position,long arg3) {
        if(!mNotHide)
          return;
        if(position==0)
          controlVisibility(DatingControl.V_SWAP_ALL);
        else
          controlVisibility(DatingControl.V_SWAP_BACK);
      }
    });
    addView(mDatingGallery);
    
    // Back Button
    mBackButton = new Button(context);
    mBackButton.setBackgroundResource(R.drawable.dating_back);
    mBackButton.setTextColor(Color.WHITE);
    mBackButton.setTypeface(Typeface.DEFAULT_BOLD);
    mBackButton.setText(R.string.dating_back);
    mBackButton.setVisibility(View.INVISIBLE);
    mBackButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mDatingGallery.setSelection(0,true);
      }
    });
    addView(mBackButton);
    
    // Info
    mFaceView = new FaceView(context);
    mFaceView.setVisibility(View.INVISIBLE);
    addView(mFaceView);

    // Power and Money
    mResourcesView = new ResourcesView(context,null);
    addView(mResourcesView);
    
    // Rate Control
    mRateControl = new RateControl(context);
    mRateControl.setBlock(false);
    addView(mRateControl);
    
    // Counter
    mCounter = new TextView(context);
    mCounter.setGravity(Gravity.CENTER_HORIZONTAL);
    mCounter.setTextColor(Color.LTGRAY);
    mCounter.setVisibility(View.INVISIBLE);
    addView(mCounter);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    
    mProgressBar.measure(0,0);
    
    mDatingGallery.measure(widthMeasureSpec,heightMeasureSpec);
    
    int offset_y = Data.s_HeaderHeight;
    
    int mode = MeasureSpec.getMode(heightMeasureSpec);
    int h = MeasureSpec.getSize(heightMeasureSpec);
    int new_h = MeasureSpec.makeMeasureSpec(h-offset_y,mode);
    
    mFaceView.measure(widthMeasureSpec,new_h);
    mRateControl.measure(0,new_h);
    mResourcesView.measure(0,0);
    mCounter.measure(widthMeasureSpec,0);
    mBackButton.measure(0,0);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
    
    int offset_y = Data.s_HeaderHeight;
    
    int x = (right-mProgressBar.getMeasuredWidth())/2;
    int y = (bottom-mProgressBar.getMeasuredHeight())/2;
    mProgressBar.layout(x,y,x+mProgressBar.getMeasuredWidth(),y+mProgressBar.getMeasuredHeight());
    
    
    mFaceView.layout(0,offset_y,right,bottom);
    
    x = mBackButton.getMeasuredWidth()/10;
    y = (mRateControl.getMeasuredHeight()-mBackButton.getMeasuredHeight())/2;
    
    mBackButton.layout(x,y,x+mBackButton.getMeasuredWidth(),y+mBackButton.getMeasuredHeight());
    
    mBackButton.setPadding((int)(mBackButton.getMeasuredWidth()/4),0,0,0);
    
    mDatingGallery.layout(0,0,right,bottom);
    
    int n = mResourcesView.getMeasuredHeight();
    int o = 6;
    mResourcesView.layout(o,offset_y,mResourcesView.getMeasuredWidth()+n,o+offset_y+mResourcesView.getMeasuredHeight()+n);
    
    mRateControl.layout(getMeasuredWidth()-mRateControl.getMeasuredWidth(),offset_y,getMeasuredWidth(),bottom);
    
    mCounter.layout(0,(int)(getMeasuredHeight()-mCounter.getMeasuredHeight()*1.5),getMeasuredWidth(),getMeasuredHeight());
  }
  //---------------------------------------------------------------------------
  public void addDataList(LinkedList<SearchUser> dataList) {
    mDataList.clear();
    mDataList.addAll(dataList);
    next();
  }
  //---------------------------------------------------------------------------
  public void setDataList(LinkedList<SearchUser> dataList) {
    mDataList.addAll(dataList);
  }
  //---------------------------------------------------------------------------
  public int getUserId() {
    return mDataList.get(mDataPosition).uid;
  }
  //---------------------------------------------------------------------------
  public String getUserName() {
    return mDataList.get(mDataPosition).first_name;
  }
  //---------------------------------------------------------------------------
  public void setOnNeedUpdateListener(OnNeedUpdateListener onNeedUpdateListener) {
    mOnNeedUpdateListener = onNeedUpdateListener;
  }
  //---------------------------------------------------------------------------
  // счетчик галереи
  public void setCounter(int index,int size) {
    mCounter.setText(index+"/"+size);
    mCounter.invalidate();
  }
  //---------------------------------------------------------------------------
  // управление скрытием контролов от позиции
  synchronized public void controlVisibility(int state) {
    int visibility;
    switch(state) {
      case V_SWAP_ALL: {
        visibility=mResourcesView.getVisibility();
        if(visibility==View.VISIBLE) {
          DatingActivity.mHeaderBar.setVisibility(View.INVISIBLE);
          mFaceView.setVisibility(View.INVISIBLE);
          mResourcesView.setVisibility(View.INVISIBLE);
          mRateControl.setVisibility(View.INVISIBLE);
        } else {
          DatingActivity.mHeaderBar.setVisibility(View.VISIBLE);
          mFaceView.setVisibility(View.VISIBLE);
          mResourcesView.setVisibility(View.VISIBLE);
          mRateControl.setVisibility(View.VISIBLE);
          mBackButton.setVisibility(View.INVISIBLE);
        }
      } break;
      case V_SHOW_ALL: {
        DatingActivity.mHeaderBar.setVisibility(View.VISIBLE);
        mFaceView.setVisibility(View.VISIBLE);
        mResourcesView.setVisibility(View.VISIBLE);
        mRateControl.setVisibility(View.VISIBLE);
        mBackButton.setVisibility(View.INVISIBLE);
      } break;
      case V_HIDE_ALL: {
        DatingActivity.mHeaderBar.setVisibility(View.INVISIBLE);
        mFaceView.setVisibility(View.INVISIBLE);
        mResourcesView.setVisibility(View.INVISIBLE);
        mRateControl.setVisibility(View.INVISIBLE);        
      } break;
      case V_SWAP_BACK: {
        visibility=mBackButton.getVisibility();
        if(visibility==View.VISIBLE) {
          DatingActivity.mHeaderBar.setVisibility(View.INVISIBLE);
          mBackButton.setVisibility(View.INVISIBLE);
        } else {
          DatingActivity.mHeaderBar.setVisibility(View.VISIBLE);
          mBackButton.setVisibility(View.VISIBLE);          
        }
      }  break;
      case V_SHOW_BACK: {
        visibility=mBackButton.getVisibility();
        if(visibility==View.INVISIBLE) {
          mBackButton.setVisibility(View.VISIBLE);
          DatingActivity.mHeaderBar.setVisibility(View.VISIBLE);
        }
      } break;
      case V_SHOW_INFO: {
        // активировать
        mNotHide = true;
        mRateControl.setBlock(true);
        mDatingGallery.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
        visibility = mResourcesView.getVisibility();
        if(visibility == View.VISIBLE) {
          mFaceView.setVisibility(View.VISIBLE);
          mCounter.setVisibility(View.VISIBLE);
        }
      } break;
    }
  }
  //---------------------------------------------------------------------------
  // Установка следующего пользователя для оценки
  public void next() {
    Debug.log(this,"next");
    
    { // блокировать
      mFaceView.setVisibility(View.INVISIBLE);
      mCounter.setVisibility(View.INVISIBLE);
      mRateControl.setBlock(false);
      mDatingGallery.setVisibility(View.INVISIBLE);
      mProgressBar.setVisibility(View.VISIBLE);
      mNotHide = false;
    }
    
    int count = mDataList.size()-1;
    
    if(mDataPosition>=count) {
      mDataPosition = 0;
      mDataList.clear();
      mOnNeedUpdateListener.needUpdate();
      return;
    }
    
    if(mDataPosition==count-5)
      mOnNeedUpdateListener.needUpdate();
    
    SearchUser user = mDataList.get(++mDataPosition);
    
    mFaceView.age    = user.age;
    mFaceView.city   = user.city_name;
    mFaceView.name   = user.first_name;
    mFaceView.online = user.online;
    mFaceView.status = user.status;
    
    mResourcesView.setResources(Data.s_Power,Data.s_Money);
    
    mGallerySize = user.avatars_big.length;

    mDatingGallery.setSelection(0);
    mGalleryAdapter.setUserData(user);
    mGalleryAdapter.notifyDataSetChanged();

  }
  //---------------------------------------------------------------------------
  public void release() {
    mOnNeedUpdateListener = null;
    mBackButton = null;
    mCounter = null;
    
    mDatingGallery = null;
    
    mGalleryAdapter.release();
    mGalleryAdapter = null;

    mFaceView.release();
    mFaceView = null;
    
    mResourcesView.release();
    mResourcesView = null;
    
    mRateControl.release();
    mRateControl = null;
    
    mDataList.clear();
    mDataList = null;
  }
  //---------------------------------------------------------------------------
}
