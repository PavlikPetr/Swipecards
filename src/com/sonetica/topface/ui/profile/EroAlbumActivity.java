package com.sonetica.topface.ui.profile;

import java.util.LinkedList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Album;
import com.sonetica.topface.net.AlbumRequest;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.ui.dating.ResourcesView;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class EroAlbumActivity extends Activity {
  // Data
  private int mCurrentPos;
  private Button mLikeButton;
  private Button mDislikeButton;
  private Button mBuyButton;
  private ImageView mEroView;
  private LinkedList<Album> mAlbumsList;
  private ProgressDialog mProgressDialog;
  // Constants
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
    
    // Image Ero
    mEroView = ((ImageView)findViewById(R.id.ivEroPhoto));
    
    // button рекомендую
    mLikeButton = ((Button)findViewById(R.id.btnEroAlbumLike));
    mLikeButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mLikeButton.setVisibility(View.INVISIBLE);
        mDislikeButton.setVisibility(View.INVISIBLE);
        mBuyButton.setVisibility(View.VISIBLE);
      }
    });
    
    // button не рекомендую 
    mDislikeButton = ((Button)findViewById(R.id.btnEroAlbumDislike));
    mDislikeButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mLikeButton.setVisibility(View.INVISIBLE);
        mDislikeButton.setVisibility(View.INVISIBLE);
        mBuyButton.setVisibility(View.VISIBLE);
      }
    });
    
    // button купить    
    mBuyButton = ((Button)findViewById(R.id.btnEroAlbumBuy));
    mBuyButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if(++mCurrentPos == mAlbumsList.size())
          mCurrentPos = 0;
        showImage(mCurrentPos);
        mLikeButton.setVisibility(View.VISIBLE);
        mDislikeButton.setVisibility(View.VISIBLE);
        mBuyButton.setVisibility(View.INVISIBLE);
      }
    });
    
    
    int uid = getIntent().getIntExtra(INTENT_USER_ID,-1);
    mCurrentPos = getIntent().getIntExtra(INTENT_ALBUM_POS,-1);

    if(uid==-1 || mCurrentPos==-1) {
      finish();      
    }
    
    // Progress Dialog
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    mProgressDialog.show();
    
    update(uid,mCurrentPos);
  }
  //---------------------------------------------------------------------------
  private void update(int uid,final int position) {
    AlbumRequest albumRequest = new AlbumRequest(this);
    albumRequest.uid  = uid;
    albumRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        mAlbumsList = Album.parse(response);
        showImage(position);
        mProgressDialog.cancel();
      }
      @Override
      public void fail(int codeError) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  public void showImage(final int position) {
    mEroView.setVisibility(View.INVISIBLE);
    
    new Thread(new Runnable() {
      @Override
      public void run() {
        final Bitmap bitmap = Http.bitmapLoader(mAlbumsList.get(position).getBigLink());
        if(bitmap!=null)
          mEroView.post(new Runnable() {
            @Override
            public void run() {
              mEroView.setVisibility(View.VISIBLE);
              mEroView.setImageBitmap(bitmap);

            }
          });
      }
    }).start();
    
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();  
  }
  //---------------------------------------------------------------------------
}
