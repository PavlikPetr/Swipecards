package com.sonetica.topface.ui.dating;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.SearchUser;
import com.sonetica.topface.ui.dating.DatingActivity;
import com.sonetica.topface.ui.dating.RateControl;
import com.sonetica.topface.ui.dating.ResourcesView;
import com.sonetica.topface.ui.dating.FaceView;
import com.sonetica.topface.utils.Debug;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
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
  private boolean mStart;      // первый старт оценок
  private boolean mNotHide;    // блокировка скрытия инфо пользователя
  private int mDataPosition;   // позиция в массиве пользователей на оценку
  private int mGallerySize;    // кол-во фото у оцениваемого пользователя
  private int mGalleryPrevPos; // предыдущая позиция в альбоме
  // Gallery
  private DatingGallery mDatingGallery;
  private DatingGalleryAdapter mGalleryAdapter;
  // Views
  private Button mBackButton;      // кнопка возврата к оцениваемой фотографии
  private TextView mCounter;        // счетчик позиции в галерее пользователя
  private FaceView mFaceView;        // информация о пользователе
  private ProgressBar mProgress;      // прогресс
  private RateControl mRateControl;    // звезды
  private ResourcesView mResourcesView; // монеты и энергия
  private LinkedList<SearchUser> mDataList;  // массив пользователей под оценку
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
    
    mStart = false;
    mDataList = new LinkedList<SearchUser>();
    
    // Progress
    mProgress = new ProgressBar(context);
    addView(mProgress);
    
    // Adapter
    mGalleryAdapter = new DatingGalleryAdapter(context,this);
    mGalleryAdapter.registerDataSetObserver(new DataSetObserver() {
      @Override
      public void onChanged() {
        //mFaceView.setVisibility(View.INVISIBLE);
        DatingControl.this.setCounter(mGalleryPrevPos+1,mGallerySize); //  Обновление после оценки
      }
      @Override
      public void onInvalidated() {}
    });
    
    // Gallery
    mDatingGallery = new DatingGallery(context);
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
    mBackButton.setText(R.string.dating_back);
    mBackButton.setVisibility(View.INVISIBLE);
    //mBackButton.s
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
    mResourcesView = new ResourcesView(context);
    addView(mResourcesView);
    
    // Rate Control
    mRateControl = new RateControl(context);
    mRateControl.setBlock(false);
    addView(mRateControl);
    
    // Counter
    mCounter = new TextView(context);
    mCounter.setGravity(Gravity.CENTER_HORIZONTAL);
    mCounter.setTextColor(Color.WHITE);
    addView(mCounter);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    
    int count = getChildCount();
    for(int i=0;i<count;i++)
      getChildAt(i).measure(widthMeasureSpec,heightMeasureSpec);
    
    mProgress.measure(0,0);
    mBackButton.measure(0,0);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
    mFaceView.layout(left,top,right,bottom);
    
    int offset_y = DatingActivity.mHeaderBar.getHeight();
    
    int px = (getWidth()-mProgress.getMeasuredWidth())/2;
    int py = (getHeight()-mProgress.getMeasuredHeight())/2;
    mProgress.layout(px,py,px+mProgress.getMeasuredWidth(),py+mProgress.getMeasuredHeight());
    
    int x = mBackButton.getMeasuredWidth()/10;
    int y = (mRateControl.getMeasuredHeight()-mBackButton.getMeasuredHeight())/2;
    mBackButton.layout(x,y,x+mBackButton.getMeasuredWidth(),y+mBackButton.getMeasuredHeight());
    mBackButton.setPadding((int)(mBackButton.getMeasuredWidth()/4),0,0,0);
    
    mDatingGallery.layout(0,0,mDatingGallery.getMeasuredWidth(),mDatingGallery.getMeasuredHeight());
    
    mResourcesView.layout(0,offset_y,mResourcesView.getMeasuredWidth(),offset_y+mResourcesView.getMeasuredHeight());
    mRateControl.layout(0,offset_y,mRateControl.getMeasuredWidth(),offset_y+mRateControl.getMeasuredHeight());
    
    mCounter.layout(0,getHeight()-30,getWidth(),getHeight());
  }
  //---------------------------------------------------------------------------
  public void setDataList(LinkedList<SearchUser> dataList) {
    mDataList.addAll(dataList);
    if(!mStart) {
      mStart = true;
      next();
    }
  }
  //---------------------------------------------------------------------------
  public int getUserId() {
    return mDataList.get(mDataPosition).uid;
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
      case V_SHOW_INFO: {
        // активировать
        mNotHide = true;
        mRateControl.setBlock(true);
        visibility=mResourcesView.getVisibility();
        if(visibility==View.VISIBLE) {
          mFaceView.setVisibility(View.VISIBLE);
          mCounter.setVisibility(View.VISIBLE);
          mProgress.setVisibility(View.INVISIBLE);
        }
      } break;
      case V_SHOW_BACK: {
        visibility=mBackButton.getVisibility();
        if(visibility==View.INVISIBLE) {
          mBackButton.setVisibility(View.VISIBLE);
          DatingActivity.mHeaderBar.setVisibility(View.VISIBLE);
        }
      } break;
    }
  }
  //---------------------------------------------------------------------------
  // Установка следующего пользователя для оценки
  public void next() {
    // блокировать
    mProgress.setVisibility(View.VISIBLE);
    mRateControl.setBlock(false);
    mNotHide = false;
    //mFaceView.setVisibility(View.INVISIBLE);
    //mCounter.setVisibility(View.INVISIBLE);
    
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
    
    mResourcesView.money = Data.s_Money;
    mResourcesView.power = Data.s_Power;
    
    
    mGallerySize = user.avatars_big.length;

    mDatingGallery.setSelection(0);
    mGalleryAdapter.setUserData(user);
    mGalleryAdapter.notifyDataSetChanged();
    
  }
  //---------------------------------------------------------------------------
  public void release() {
    mOnNeedUpdateListener = null;
    mDatingGallery = null;
    mBackButton = null;
    
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
    Debug.log(this,"release");
  }
  //---------------------------------------------------------------------------
}
