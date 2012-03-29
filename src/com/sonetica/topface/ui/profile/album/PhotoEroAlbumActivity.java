package com.sonetica.topface.ui.profile.album;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.billing.BuyingActivity;
import com.sonetica.topface.data.Album;
import com.sonetica.topface.data.PhotoOpen;
import com.sonetica.topface.requests.ApiHandler;
import com.sonetica.topface.requests.ApiResponse;
import com.sonetica.topface.requests.PhotoOpenRequest;
import com.sonetica.topface.requests.PhotoVoteRequest;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Http;
import com.sonetica.topface.utils.LeaksManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PhotoEroAlbumActivity extends Activity implements View.OnClickListener {
  // Data
  private int mCurrentPos;
  private int mUserId;
  private TextView mMoney;
  private TextView mCounter;
  private TextView mPhotoCost;
  private Button mLikeButton;
  private Button mDislikeButton;
  private View mBuyButton;
  private Button mNextButton;
  private ImageView mEroView;
  //private ProgressBar mProgress;
  private LinkedList<Album> mAlbumsList;
  private ProgressDialog mProgressDialog;
  // States
  public static final int S_HIDE_ALL          = 0;
  public static final int S_SHOW_LIKE_DISLIKE = 1;
  public static final int S_SHOW_NEXT_BUY     = 2;
  public static final int S_SHOW_NEXT         = 3;
  // Types
  public static final int T_LIKE = 1;
  public static final int T_DISLIKE = -1;
  // Constants
  public static final String INTENT_OWNER = "owner";
  public static final String INTENT_USER_ID = "user_id";
  public static final String INTENT_ALBUM_POS = "album_position";
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_ero_album);
    Debug.log(this,"+onCreate");
    
    LeaksManager.getInstance().monitorObject(this);
    
    // Title Header
    mCounter = (TextView)findViewById(R.id.tvHeaderTitle);
    
    // Data List
    mAlbumsList = Data.s_PhotoAlbum;
    
    // Image Ero
    mEroView = ((ImageView)findViewById(R.id.ivEroPhoto));

    // Progress
    //mProgress = (ProgressBar)findViewById(R.id.pgrsEroAlbum);
    
    // button рекомендую
    mLikeButton = ((Button)findViewById(R.id.btnEroAlbumLike));
    mLikeButton.setOnClickListener(this);
    
    // button не рекомендую 
    mDislikeButton = ((Button)findViewById(R.id.btnEroAlbumDislike));
    mDislikeButton.setOnClickListener(this);
    
    // button купить    
    mBuyButton = findViewById(R.id.btnEroAlbumBuy);
    mBuyButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if(++mCurrentPos == mAlbumsList.size())
          mCurrentPos = 0;
        showImage();
      }
    });
    
    // показать следующую    
    mNextButton = ((Button)findViewById(R.id.btnEroAlbumNext));
    mNextButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if(++mCurrentPos == mAlbumsList.size())
          mCurrentPos = 0;
        showImage();
      }
    });
    
    //
    mPhotoCost = (TextView)findViewById(R.id.tvEroPhotoCost);
    
    mUserId = getIntent().getIntExtra(INTENT_USER_ID,-1);
    mCurrentPos = getIntent().getIntExtra(INTENT_ALBUM_POS,-1);

    if(mUserId==-1 || mCurrentPos==-1) {
      finish();
    }
    
    // Progress Dialog
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    //mProgressDialog.show();
    
    // Money
    Drawable drwbl = getResources().getDrawable(R.drawable.dating_money);
    mMoney = (TextView)findViewById(R.id.tvEroAlbumMoney);
    mMoney.setCompoundDrawablePadding(5);
    mMoney.setCompoundDrawablesWithIntrinsicBounds(null,null,drwbl,null);
    
    updateCounter();
    
    showImage();
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onDestroy() {
    mAlbumsList = null;
    mLikeButton = null;
    mDislikeButton = null;
    mBuyButton = null;
    mProgressDialog = null;
    mEroView = null;
    
    Debug.log(this,"-onDestroy");
    super.onDestroy();  
  }
  //---------------------------------------------------------------------------
  public void voteImage(int vote) {
    PhotoVoteRequest photoVoteRequest = new PhotoVoteRequest(getApplicationContext());
    photoVoteRequest.uid   = mUserId;
    photoVoteRequest.photo = mAlbumsList.get(mCurrentPos).id;
    photoVoteRequest.vote  = vote; 
    photoVoteRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        //PhotoVote photoVote = PhotoVote.parse(response);
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  public void showImage() {
    //mEroView.setVisibility(View.INVISIBLE);
    
    final Album album = mAlbumsList.get(mCurrentPos);
    
    mPhotoCost.setText(""+album.cost);  // стоимость фото
    
    if(album.buy) {  // данная фотография уже куплена
      
      if(mAlbumsList.size()==1) {
        controlVisibility(S_HIDE_ALL);
      } else {
        int nextPos = mCurrentPos + 1;
        if(nextPos >= mAlbumsList.size())
          nextPos = 0;
        if(mAlbumsList.get(nextPos).buy)
          controlVisibility(S_SHOW_NEXT);
        else
          controlVisibility(S_SHOW_NEXT_BUY);
      }
      
      Thread t = new Thread(new LoaderEroPhoto());     // загрузка эро фотографии
      LeaksManager.getInstance().monitorObject(t);
      t.start();

    } else {  // запрос на покупку
      PhotoOpenRequest photoOpenRequest = new PhotoOpenRequest(getApplicationContext());
      photoOpenRequest.uid = mUserId;
      photoOpenRequest.photo = album.id;
      photoOpenRequest.callback(new ApiHandler() {
        @Override
        public void success(ApiResponse response) {
          PhotoOpen photoOpen = PhotoOpen.parse(response);
          if(photoOpen.completed) {
            Data.s_Money = photoOpen.money;
            album.buy = true;
            controlVisibility(S_SHOW_LIKE_DISLIKE);
            
            Thread t = new Thread(new LoaderEroPhoto());     // загрузка эро фотографии
            LeaksManager.getInstance().monitorObject(t);
            t.start();
            
          } else
            startActivity(new Intent(PhotoEroAlbumActivity.this.getApplicationContext(),BuyingActivity.class));  // окно на покупку монет
        }
        @Override
        public void fail(int codeError,ApiResponse response) {
          if(codeError==ApiResponse.PAYMENT)
            startActivity(new Intent(PhotoEroAlbumActivity.this.getApplicationContext(),BuyingActivity.class));
          PhotoEroAlbumActivity.this.finish();
        }
      }).exec();
      
    }
  }
  //---------------------------------------------------------------------------
  // счетчик галереи
  public void updateCounter() {
    // money
    mMoney.setText(""+Data.s_Money);
    mMoney.invalidate();
    // counter
    mCounter.setText((mCurrentPos+1)+"/"+mAlbumsList.size());
    mCounter.invalidate();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View view) {
    // voting    
    switch(view.getId()) {
      case R.id.btnEroAlbumLike:
        voteImage(T_LIKE);
        break;
      case R.id.btnEroAlbumDislike:
        voteImage(T_DISLIKE);
        break;
    }
    
    if(mAlbumsList.size()==1) {
      controlVisibility(S_HIDE_ALL);
    } else {
      int nextPos = mCurrentPos + 1;
      if(nextPos >= mAlbumsList.size())
        nextPos = 0;
      
      mPhotoCost.setText(""+mAlbumsList.get(nextPos).cost);  // стоимость фото
      
      if(mAlbumsList.get(nextPos).buy)
        controlVisibility(S_SHOW_NEXT);
      else {
        controlVisibility(S_SHOW_NEXT_BUY);
      }
    }
    
  }
  //---------------------------------------------------------------------------
  synchronized public void controlVisibility(int state) {
    
    updateCounter();
    
    switch(state) {
      case S_HIDE_ALL: {
        mLikeButton.setVisibility(View.INVISIBLE);
        mDislikeButton.setVisibility(View.INVISIBLE);
        mBuyButton.setVisibility(View.INVISIBLE);
        mNextButton.setVisibility(View.INVISIBLE);
      } break;
      case S_SHOW_LIKE_DISLIKE: {
        mLikeButton.setVisibility(View.VISIBLE);
        mDislikeButton.setVisibility(View.VISIBLE);
        mBuyButton.setVisibility(View.INVISIBLE);
        mNextButton.setVisibility(View.INVISIBLE);
      } break;
      case S_SHOW_NEXT_BUY: {
        mLikeButton.setVisibility(View.INVISIBLE);
        mDislikeButton.setVisibility(View.INVISIBLE);
        mBuyButton.setVisibility(View.VISIBLE);
        mNextButton.setVisibility(View.INVISIBLE);
      } break;
      case S_SHOW_NEXT: {
        mLikeButton.setVisibility(View.INVISIBLE);
        mDislikeButton.setVisibility(View.INVISIBLE);
        mBuyButton.setVisibility(View.INVISIBLE);
        mNextButton.setVisibility(View.VISIBLE);
      } break;
    }
  }
  //---------------------------------------------------------------------------
  // class LoaderEroPhoto
  //---------------------------------------------------------------------------
  class LoaderEroPhoto implements Runnable {
    @Override
    public void run() {
      final Bitmap bitmap = Http.bitmapLoader(mAlbumsList.get(mCurrentPos).getBigLink());
      if(bitmap!=null)
        mEroView.post(new Runnable() {
          @Override
          public void run() {
            //mEroView.setVisibility(View.VISIBLE);
            mEroView.setImageBitmap(bitmap);
          }
        });
    }
  }
  //---------------------------------------------------------------------------
}
