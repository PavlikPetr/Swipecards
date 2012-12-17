package com.topface.topface.ui.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
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
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.http.Http;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
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

    public static final String PATH_TO_FILE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp.jpg";
    private Context mContext;
    private AlertDialog mAddPhotoDialog;
    private Activity mActivity;
    private Fragment mFragment;
    private Handler mHandler;
    private LockerView mLockerView;

    public static final int ADD_PHOTO_RESULT_OK = 0;
    public static final int ADD_PHOTO_RESULT_ERROR = 1;
    public static final int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA = 101;
    public static final int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY = 100;


    public AddPhotoHelper(Fragment fragment, LockerView mLockerView) {
        this(fragment.getActivity());
        mFragment = fragment;
        this.mLockerView = mLockerView;
    }

    public AddPhotoHelper(Activity activity) {
        mActivity = activity;
        mContext = activity.getApplicationContext();
    }

    public void showProgressDialog() {
        if (mLockerView != null) {
            mLockerView.setVisibility(View.VISIBLE);
        }
//        if(lock)
//        FragmentManager fm = ((FragmentActivity) mActivity).getSupportFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();
//
//        Fragment prev = fm.findFragmentByTag(ProgressDialogFragment.PROGRESS_DIALOG_TAG);
//        if (prev != null) {
//            ft.remove(prev);
//        }
//        ft.addToBackStack(null);
//
//        DialogFragment newFragment = ProgressDialogFragment.newInstance();
//        ft.add(newFragment, ProgressDialogFragment.PROGRESS_DIALOG_TAG);
//        ft.commitAllowingStateLoss();
    }

    public void hideProgressDialog() {
        if (mLockerView != null) {
            mLockerView.setVisibility(View.GONE);
        }
//        FragmentManager fm = ((FragmentActivity) mActivity).getSupportFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();
//
//        Fragment prev = fm.findFragmentByTag(ProgressDialogFragment.PROGRESS_DIALOG_TAG);
//        if (prev != null) {
//            ft.remove(prev);
//        }
//        ft.commitAllowingStateLoss();
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
                        mFragment.startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY);
//                        mFragment.getActivity().startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY);
                    } else {
                        mActivity.startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY);
                    }
                }
                break;
                case R.id.btnAddPhotoCamera: {
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(PATH_TO_FILE)));
                    intent = Intent.createChooser(intent, mContext.getResources().getString(R.string.profile_add_title));

                    if (Utils.isIntentAvailable(mContext, intent.getAction())) {
                        if (mFragment != null) {
                            mFragment.startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA);
//                            mFragment.getActivity().startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA);
                        } else {
                            mActivity.startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA);
                        }
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
        if (mFragment != null) {
            if (mFragment.getActivity() != null && !mFragment.isAdded()) {
                Debug.log("APH::detached");
            }
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA) {
                if (data == null) {
                    data = new Intent();
                }
                data.putExtra("isCamera", true);
                new AsyncTaskUploader().execute(data);
                return true;
            } else if (requestCode == GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY) {
                if (data == null) {
                    data = new Intent();
                }
                data.putExtra("isCamera", false);
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
            Intent intent = intentList[0];
            try {

                if (intent.getBooleanExtra("isCamera", false)) {
                    File receivedImage = new File(PATH_TO_FILE);
                    rawResponse = getRawResponse(receivedImage);
                } else {
                    rawResponse = getRawResponse(intent.getData());
                }

            } catch (Exception e) {
                Debug.error("Photo not uploaded", e);
            } catch (OutOfMemoryError e) {
                Debug.error("Photo upload OOM: ", e);

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

    private String getRawResponse(Uri imageUri) throws IOException {
        PhotoAddRequest add = new PhotoAddRequest(AddPhotoHelper.this.mContext);
        add.ssid = Data.SSID;

        Cursor cursor = mActivity.getContentResolver().query(imageUri, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
        cursor.moveToFirst();

        //Link to the image
        final String file = cursor.getString(0);
        cursor.close();

//        String data = Base64.encodeFromFile(file);
//        new Base64.OutputStream()

        return Http.httpDataRequest(Http.HTTP_POST_REQUEST, Static.API_URL, add.toString(), file);
    }

    private String getRawResponse(File file) throws IOException {
        PhotoAddRequest add = new PhotoAddRequest(AddPhotoHelper.this.mContext);
        add.ssid = Data.SSID;


//        String data = Base64.encodeFromFile(file.getAbsolutePath());

        return Http.httpDataRequest(Http.HTTP_POST_REQUEST, Static.API_URL, add.toString(), file.getAbsolutePath());
    }

}

