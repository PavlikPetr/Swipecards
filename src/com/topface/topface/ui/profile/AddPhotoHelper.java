package com.topface.topface.ui.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Confirmation;
import com.topface.topface.data.Photo;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.PhotoAddRequest;
import com.topface.topface.ui.fragments.ProgressDialogFragment;
import com.topface.topface.utils.Base64;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.http.Http;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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
    }

    public void showProgressDialog() {
        FragmentManager fm = ((FragmentActivity) mActivity).getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment prev = fm.findFragmentByTag(ProgressDialogFragment.PROGRESS_DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment newFragment = ProgressDialogFragment.newInstance();
        ft.add(newFragment, ProgressDialogFragment.PROGRESS_DIALOG_TAG);
        ft.commitAllowingStateLoss();
    }

    public void hideProgressDialog() {
        FragmentManager fm = ((FragmentActivity) mActivity).getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Fragment prev = fm.findFragmentByTag(ProgressDialogFragment.PROGRESS_DIALOG_TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.commitAllowingStateLoss();
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

    public OnClickListener getAddPhotoClickListener() {
        return mOnAddPhotoClickListener;
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
            if (data != null) {
                new AsyncTaskUploader().execute(data);
                return true;
            }
        }
        return false;
    }

    class AsyncTaskUploader extends AsyncTask<Intent, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected String doInBackground(Intent... intentList) {
            String rawResponse = null;

            PhotoAddRequest add = new PhotoAddRequest(AddPhotoHelper.this.mContext);
            add.ssid = Data.SSID;

            Intent intent = intentList[0];
            if (intent == null)
                return rawResponse;

            Uri imageUri = intent.getData();

            try {

                // Android 4
                //Bundle extras = intent.getExtras();
                //Bitmap thePic = extras.getParcelable("data");
                //User had pick an image.
                Cursor cursor = mActivity.getContentResolver().query(imageUri, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
                cursor.moveToFirst();

                //Link to the image
                final String file = cursor.getString(0);
                cursor.close();

                //is = App.getContext().getContentResolver().openInputStream(uri[0]);
//                String file = FileSystem.getFilePathFromURI(AddPhotoHelper.this.mActivity, imageUri);
                String data = Base64.encodeFromFile(file);
                //String data2 = Base64.encodeBytes(thePic.getNinePatchChunk());
                rawResponse = Http.httpDataRequest(Http.HTTP_POST_REQUEST, Static.API_URL, add.toString(), data);
                data = null;
            } catch (IOException e) {
                Debug.log("Photo not uploaded");
            }

            return rawResponse;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                Confirmation c = Confirmation.parse(new ApiResponse(result));
                if (c.completed) {
                    Message msg = new Message();
                    msg.what = ADD_PHOTO_RESULT_OK;
                    try {
                        msg.obj = new Photo((new JSONObject(result)).optJSONObject("result").optJSONObject("photo"));
                    } catch (JSONException e) {
                        Debug.log(e.toString());
                    }
                    mHandler.sendMessage(msg);
                } else {
                    mHandler.sendEmptyMessage(ADD_PHOTO_RESULT_ERROR);
                }
            } else {
                mHandler.sendEmptyMessage(ADD_PHOTO_RESULT_ERROR);
            }
            hideProgressDialog();
        }
    }
}

