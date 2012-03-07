package com.sonetica.topface.ui.profile;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Album;
import com.sonetica.topface.data.PhotoOpen;
import com.sonetica.topface.data.PhotoVote;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.net.PhotoOpenRequest;
import com.sonetica.topface.net.PhotoVoteRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.ui.BuyingActivity;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class EroAlbumActivity extends Activity implements View.OnClickListener {
  // Data
  private int mCurrentPos;
  private int mUserId;
  private Button mLikeButton;
  private Button mDislikeButton;
  private Button mBuyButton;
  private Button mNextButton;
  private ImageView mEroView;
  private ProgressBar mProgress;
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
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText("Oo");
    
    // Data List
    mAlbumsList = Data.s_PhotoAlbum;
    
    // Image Ero
    mEroView = ((ImageView)findViewById(R.id.ivEroPhoto));
    
    // Progress
    mProgress = (ProgressBar)findViewById(R.id.pgrsEroAlbum);
    
    // button рекомендую
    mLikeButton = ((Button)findViewById(R.id.btnEroAlbumLike));
    mLikeButton.setOnClickListener(this);
    
    // button не рекомендую 
    mDislikeButton = ((Button)findViewById(R.id.btnEroAlbumDislike));
    mDislikeButton.setOnClickListener(this);
    
    // button купить    
    mBuyButton = ((Button)findViewById(R.id.btnEroAlbumBuy));
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
    
    mUserId = getIntent().getIntExtra(INTENT_USER_ID,-1);
    mCurrentPos = getIntent().getIntExtra(INTENT_ALBUM_POS,-1);

    if(mUserId==-1 || mCurrentPos==-1)
      finish();
    
    // Progress Dialog
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    //mProgressDialog.show();
    
    showImage();
  }
  //---------------------------------------------------------------------------
  public void voteImage(int vote) {
    PhotoVoteRequest photoVoteRequest = new PhotoVoteRequest(this);
    photoVoteRequest.uid   = mUserId;
    photoVoteRequest.photo = mAlbumsList.get(mCurrentPos).id;
    photoVoteRequest.vote  = vote; 
    photoVoteRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        PhotoVote photoVote = PhotoVote.parse(response);
      }
      @Override
      public void fail(int codeError) {
        
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  public void showImage() {
    //mEroView.setVisibility(View.INVISIBLE);
    
    final Album album = mAlbumsList.get(mCurrentPos);
    
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
      
      new Thread(new LoaderEroPhoto()).start();     // загрузка эро фотографии

    } else {  // запрос на покупку

      PhotoOpenRequest photoOpenRequest = new PhotoOpenRequest(this);
      photoOpenRequest.uid = mUserId;
      photoOpenRequest.photo = album.id;
      photoOpenRequest.callback(new ApiHandler() {
        @Override
        public void success(Response response) {
          PhotoOpen photoOpen = PhotoOpen.parse(response);
          if(photoOpen.completed) {
            album.buy = true;
            controlVisibility(S_SHOW_LIKE_DISLIKE);
            
            new Thread(new LoaderEroPhoto()).start();     // загрузка эро фотографии
            
          } else
            startActivity(new Intent(EroAlbumActivity.this,BuyingActivity.class));  // окно на покупку монет
        }
        @Override
        public void fail(int codeError) {
          EroAlbumActivity.this.finish();  // какие-то неполадки
        }
      }).exec();
      
    }
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
      if(mAlbumsList.get(nextPos).buy)
        controlVisibility(S_SHOW_NEXT);
      else
        controlVisibility(S_SHOW_NEXT_BUY);
    }
    
  }
  //---------------------------------------------------------------------------
  synchronized public void controlVisibility(int state) {
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
