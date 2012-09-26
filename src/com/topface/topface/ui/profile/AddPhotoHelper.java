package com.topface.topface.ui.profile;

import java.io.IOException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Confirmation;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.PhotoAddRequest;
import com.topface.topface.utils.Base64;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.FileSystem;
import com.topface.topface.utils.http.Http;

/**
 * Хелпер для загрузки фотографий в любой активити
 * <p/>
 * Как использовать:
 * 1) Вызвать метод addPhoto или addEroPhoto, для показа диалога
 * 1а) Вы можете добавить коллбэк на окончание загрузки фото через метод setOnResultHandler
 * 2) В методе onActivityResult вашей активити вызвать метод checkActivityResult
 * (если это результат с загрузкой фото, то фотография начнет загружаться на сервер)
 */
public class AddPhotoHelper {

    private Context mContext;
    private AlertDialog mAddPhotoDialog;
    private Activity mActivity;
    private Fragment mFragment;
    private ProgressDialog mProgressDialog;
    private Handler mHandler;
    public static final int ADD_PHOTO_RESULT_OK = 0;
    public static final int ADD_PHOTO_RESULT_ERROR = 1;
    public static final int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    
    
    public AddPhotoHelper(Fragment fragment) {
        this(fragment.getActivity());
        mFragment = fragment;
    }

    public AddPhotoHelper(Activity activity) {
        mActivity = activity;
        mContext = activity.getApplicationContext();
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setMessage(mContext.getString(R.string.general_dialog_loading));
    }

    /**
     * Добавление фотографии
     */
    public void addPhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(mContext.getString(R.string.album_add_photo_title));
        View view = LayoutInflater.from(mContext).inflate(R.layout.profile_add_photo, null);
        view.findViewById(R.id.btnAddPhotoAlbum).setOnClickListener(mOnAddPhotoClickListener);
        view.findViewById(R.id.btnAddPhotoCamera).setOnClickListener(mOnAddPhotoClickListener);
        builder.setView(view);
        mAddPhotoDialog = builder.create();
        mAddPhotoDialog.show();
    }

    private View.OnClickListener mOnAddPhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnAddPhotoAlbum: {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent = Intent.createChooser(intent, mContext.getResources().getString(R.string.profile_add_title)); 
                    if (mFragment != null) {
                        mFragment.startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE);
                    } else {
                        mActivity.startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE);
                    }
                }
                break;
                case R.id.btnAddPhotoCamera: {
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    intent = Intent.createChooser(intent, mContext.getResources().getString(R.string.profile_add_title));
                    if (mFragment != null) {
                        mFragment.startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE);
                    } else {
                        mActivity.startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE);
                    }
                }
                break;
            }
            if (mAddPhotoDialog != null && mAddPhotoDialog.isShowing())
                mAddPhotoDialog.cancel();
        }
    };

    /**
     * Коллбэк, вызываемый после загрузки фотографии
     *
     * @param handler который будет вызван
     * @return объект текущего класса
     */
    public AddPhotoHelper setOnResultHandler(Handler handler) {
        mHandler = handler;
        return this;
    }

    public boolean checkActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GALLERY_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = data != null ? data.getData() : null;
            if (imageUri != null) {
                new AsyncTaskUploader().execute(imageUri);
                return true;
            }
        }
        return false;
    }

    class AsyncTaskUploader extends AsyncTask<Uri, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(Uri... uri) {
            String rawResponse = null;
            
            PhotoAddRequest add = new PhotoAddRequest(AddPhotoHelper.this.mContext);
            add.ssid = Data.SSID;

            try {
                //is = App.getContext().getContentResolver().openInputStream(uri[0]);
                String file = FileSystem.getFilePathFromURI(AddPhotoHelper.this.mActivity, uri[0]);
                String data = Base64.encodeFromFile(file);
                rawResponse = Http.httpDataRequest(Http.HTTP_POST_REQUEST, Static.API_URL, add.toString(), data);
                data = null;
           } catch(IOException e) {
               Debug.log("Photo not uploaded");
           }

           return rawResponse;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Confirmation c = Confirmation.parse(new ApiResponse(result));
            if (c.completed)
              mHandler.sendEmptyMessage(ADD_PHOTO_RESULT_OK);
            else
              mHandler.sendEmptyMessage(ADD_PHOTO_RESULT_ERROR);
            mProgressDialog.cancel();
        }

//        private void sendAddRequest(final String[] result, final int price) {
//            mActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    PhotoAddRequest addPhotoRequest = new PhotoAddRequest(mContext);
//                    addPhotoRequest.big = result[0];
//                    addPhotoRequest.medium = result[1];
//                    addPhotoRequest.small = result[2];
//                    addPhotoRequest.ero = mAddEroState;
//                    if (mAddEroState) {
//                        addPhotoRequest.cost = price;
//                    }
//                    addPhotoRequest.callback(new ApiHandler() {
//                        @Override
//                        public void success(ApiResponse response) {
//                            Confirmation add = Confirmation.parse(response);
//                            if (!add.completed)
//                                return;
//
//                            post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mHandler.sendEmptyMessage(ADD_PHOTO_RESULT_OK);
//                                    mProgressDialog.hide();
//                                }
//                            });
//                        }
//
//                        @Override
//                        public void fail(int codeError, ApiResponse response) {
//                            post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mHandler.sendEmptyMessage(ADD_PHOTO_RESULT_ERROR);
//                                    Utils.showErrorMessage(mContext);
//                                    mProgressDialog.hide();
//                                }
//                            });
//                        }
//                    }).exec();
//                }
//            });
//        }
    }
}

