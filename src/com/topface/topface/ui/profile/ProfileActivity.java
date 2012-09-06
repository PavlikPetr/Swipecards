package com.topface.topface.ui.profile;

import com.topface.topface.R;
import com.topface.topface.utils.Debug;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/* "Профиль" */
public class ProfileActivity extends Activity{
    // Data
	private Button mEditButton;
	
    //Constants
    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_MUTUAL_ID = "mutual_id";
    public static final String INTENT_USER_NAME = "user_name";
    public static final String INTENT_CHAT_INVOKE = "chat_invoke";
    public static final int FORM_TOP = 0;
    public static final int FORM_BOTTOM = 1;
    public static final int GALLARY_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    public static final int ALBUM_ACTIVITY_REQUEST_CODE = 101;
    public static final int EDITOR_ACTIVITY_REQUEST_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_profile);
        
        Debug.log(this, "+onCreate");
    }
  
    @Override
    protected void onStart() {
        super.onStart();
//        if (mIsOwner) {
//            mResourcesPower.setBackgroundResource(Utils.getBatteryResource(CacheProfile.power));
//            mResourcesPower.setText("" + CacheProfile.power + "%");
//            mResourcesMoney.setText("" + CacheProfile.money);
//        }
    }
  
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == EDITOR_ACTIVITY_REQUEST_CODE/* && resultCode ==
//                                                        * RESULT_OK */) {
//            setOwnerProfileInfo(CacheProfile.getProfile());
//        }
//        if (requestCode == ALBUM_ACTIVITY_REQUEST_CODE/* && resultCode ==
//                                                       * RESULT_OK */)
//            if (mIsOwner)
//                updateOwnerAlbum();
//        if (requestCode == GALLARY_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
//            Uri imageUri = data != null ? data.getData() : null;
//            if (imageUri == null)
//                return;
//            new AsyncTaskUploader().execute(imageUri);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void release() {

    }

//    class AsyncTaskUploader extends AsyncTask<Uri, Void, String[]> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            mProgressDialog.show();
//        }
//        @Override
//        protected String[] doInBackground(Uri... uri) {
//            Socium soc = new Socium(getApplicationContext());
//            return soc.uploadPhoto(uri[0]);
//        }
//        @Override
//        protected void onPostExecute(final String[] result) {
//            super.onPostExecute(result);
//
//            if (mAddEroState) {
//                // попап с выбором цены эро фотографии
//                final CharSequence[] items = {getString(R.string.profile_coin_1),getString(R.string.profile_coin_2),getString(R.string.profile_coin_3)};
//                new AlertDialog.Builder(ProfileActivity.this).setTitle(getString(R.string.profile_ero_price)).setItems(items, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog,int item) {
//                        sendAddRequest(result, item + 1);
//                    }
//                }).create().show();
//            } else
//                sendAddRequest(result, 0);
//        }
//        private void sendAddRequest(final String[] result,final int price) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    PhotoAddRequest addPhotoRequest = new PhotoAddRequest(ProfileActivity.this.getApplicationContext());
//                    addPhotoRequest.big = result[0];
//                    addPhotoRequest.medium = result[1];
//                    addPhotoRequest.small = result[2];
//                    addPhotoRequest.ero = mAddEroState;
//                    if (mAddEroState)
//                        addPhotoRequest.cost = price;
//                    addPhotoRequest.callback(new ApiHandler() {
//                        @Override
//                        public void success(ApiResponse response) {
//                            Confirmation confirm = Confirmation.parse(response);
//                            if (!confirm.completed)
//                                return;
//
//                            Album album = new Album();
//                            album.big = result[0];
//                            album.small = result[2];
//
//                            post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    updateOwnerAlbum();
//                                    mProgressDialog.hide();
//                                }
//                            });
//                        }
//                        @Override
//                        public void fail(int codeError,ApiResponse response) {
//                            post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(ProfileActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
//                                    mProgressDialog.hide();
//                                }
//                            });
//                        }
//                    }).exec();
//                }
//            });//runOnUiThread
//        }
//    }
    
}
