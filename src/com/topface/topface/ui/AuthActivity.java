package com.topface.topface.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import org.json.JSONException;
import org.json.JSONObject;
import com.facebook.android.*;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook.*;
import com.topface.topface.R;
import com.topface.topface.Data;
import com.topface.topface.data.Auth;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.utils.AuthToken;
import com.topface.topface.utils.Debug;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class AuthActivity extends Activity implements View.OnClickListener  {
  // Data
  private Button mFBButton;
  private Button mVKButton;
  private ProgressBar mProgressBar;
  private AsyncFacebookRunner mAsyncFacebookRunner;
  private AuthRequest authRequest;
  private String[] FB_PERMISSIONS = {"user_photos","publish_stream","email","publish_actions"};
  //---------------------------------------------------------------------------
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Debug.log(this,"+onCreate");
    setContentView(R.layout.ac_auth);
    
    // Facebook button
    mFBButton = (Button)findViewById(R.id.btnAuthFB);
    mFBButton.setOnClickListener(this);

    // Vkontakte button
    mVKButton = (Button)findViewById(R.id.btnAuthVK);
    mVKButton.setOnClickListener(this);
    
    // Progress
    mProgressBar = (ProgressBar)findViewById(R.id.prsAuthLoading);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    mAsyncFacebookRunner = null;
    if(authRequest!=null) authRequest.cancel();
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onActivityResult(int requestCode,int resultCode,Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Debug.log(this,"onActivityResult");

    if(requestCode==WebAuthActivity.INTENT_WEB_AUTH && resultCode==Activity.RESULT_OK) {
      startActivity(new Intent(this,MainActivity.class));
      finish();
    } else if(requestCode!=WebAuthActivity.INTENT_WEB_AUTH && resultCode==Activity.RESULT_OK) {
      Data.facebook.authorizeCallback(requestCode, resultCode, data);
    }
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View view) {
    if(view.getId() == R.id.btnAuthVK) {
      Intent intent = new Intent(getApplicationContext(), WebAuthActivity.class);
      startActivityForResult(intent, WebAuthActivity.INTENT_WEB_AUTH);
    } else if(view.getId() == R.id.btnAuthFB) {
      mAsyncFacebookRunner = new AsyncFacebookRunner(Data.facebook);
      Data.facebook.authorize(this, FB_PERMISSIONS, mDialogListener);
    }
  }
  //---------------------------------------------------------------------------
  private void showButtons() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mFBButton.setVisibility(View.VISIBLE);
        mVKButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE); 
      }
    });
  }
  //---------------------------------------------------------------------------
  private void hideButtons() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mFBButton.setVisibility(View.INVISIBLE);
        mVKButton.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE); 
      }
    });
  }
  //---------------------------------------------------------------------------
  private void auth(AuthToken token){
    authRequest = new AuthRequest(getApplicationContext());
    authRequest.platform = token.getSocialNet();
    authRequest.sid      = token.getUserId();
    authRequest.token    = token.getTokenKey();
    authRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        Debug.log(this,"Auth");
        Auth auth = Auth.parse(response);
        Data.saveSSID(getApplicationContext(),auth.ssid);
        post(new Runnable() {
          @Override
          public void run() {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
          }
        });
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        showButtons();
        post(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(AuthActivity.this,getString(R.string.general_server_error),Toast.LENGTH_SHORT).show();
          }
        });
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private DialogListener mDialogListener = new DialogListener(){
    @Override
    public void onComplete(Bundle values) {
      Debug.log("FB","mDialogListener::onComplete");
      mAsyncFacebookRunner.request("/me", mRequestListener);
      hideButtons();
    }
    @Override
    public void onFacebookError(FacebookError e) {
      Debug.log("FB","mDialogListener::onFacebookError:"+e.getMessage());
      showButtons();
    }
    @Override
    public void onError(DialogError e) {
      Debug.log("FB","mDialogListener::onError");
      showButtons();
    }
    @Override
    public void onCancel() {
      Debug.log("FB","mDialogListener::onCancel");
      showButtons();
    }
  };
  //---------------------------------------------------------------------------
  private RequestListener mRequestListener = new RequestListener() {
    @Override
    public void onComplete(String response,Object state) {
      try {
        Debug.log("FB","mRequestListener::onComplete");
        JSONObject jsonResult = new JSONObject(response);
        String user_id = jsonResult.getString("id");
        final AuthToken authToken = new AuthToken(getApplicationContext());
        authToken.saveToken(AuthToken.SN_FACEBOOK, user_id, Data.facebook.getAccessToken(), ""+Data.facebook.getAccessExpires());
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            AuthActivity.this.auth(authToken); 
          }
        });
      } catch(JSONException e) {
        Debug.log("FB","mRequestListener::onComplete:error");
        showButtons();
      }
    }
    @Override
    public void onMalformedURLException(MalformedURLException e,Object state) {
      Debug.log("FB","mRequestListener::onMalformedURLException");
      showButtons();
    }
    @Override
    public void onIOException(IOException e,Object state) {
      Debug.log("FB","mRequestListener::onIOException");
      showButtons();
    }
    @Override
    public void onFileNotFoundException(FileNotFoundException e,Object state) {
      Debug.log("FB","mRequestListener::onFileNotFoundException");
      showButtons();
    }
    @Override
    public void onFacebookError(FacebookError e,Object state) {
      Debug.log("FB","mRequestListener::onFacebookError:"+e+":"+state);
      showButtons();
    }
  };
  //---------------------------------------------------------------------------
}
