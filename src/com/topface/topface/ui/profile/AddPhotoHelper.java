package com.topface.topface.ui.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.PhotoAddRequest;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

import java.io.File;

/**
 * Хелпер для загрузки фотографий в любой активити
 */
public class AddPhotoHelper {

    public static final String PATH_TO_FILE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp.jpg";
    private Context mContext;
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
    }

    public void hideProgressDialog() {
        if (mLockerView != null) {
            mLockerView.setVisibility(View.GONE);
        }
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
                        } else {
                            mActivity.startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA);
                        }
                    }
                }
                break;
            }
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

    public void processActivityResult(int requestCode, int resultCode, Intent data) {
        if (mFragment != null) {
            if (mFragment.getActivity() != null && !mFragment.isAdded()) {
                Debug.log("APH::detached");
            }
        }
        if (resultCode == Activity.RESULT_OK) {
            Uri photoUri = null;
            if (requestCode == GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA) {
                //Если фотография сделана, то ищем ее во временном файле
                photoUri = Uri.fromParts("file", PATH_TO_FILE, null);
            } else if (requestCode == GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY) {
                //Если она взята из галереи, то получаем URL из данных интента и преобразуем его в путь до файла
                photoUri = data.getData();
            }

            //Отправляем запрос
            sendRequest(photoUri);
        }
    }

    /**
     * Отправляет запрос к API с прикрепленной фотографией
     *
     * @param uri фотографии
     */
    private void sendRequest(Uri uri) {
        if (uri == null && mHandler != null) {
            mHandler.sendEmptyMessage(ADD_PHOTO_RESULT_ERROR);
            return;
        }
        showProgressDialog();

        new PhotoAddRequest(uri, mContext).callback(new DataApiHandler<Photo>() {
            @Override
            protected void success(Photo photo, ApiResponse response) {
                if (mHandler != null) {
                    Message msg = new Message();
                    msg.what = ADD_PHOTO_RESULT_OK;
                    msg.obj = photo;
                    mHandler.sendMessage(msg);
                }
            }

            @Override
            protected Photo parseResponse(ApiResponse response) {
                return new Photo(response);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(ADD_PHOTO_RESULT_ERROR);
                }
            }

            @Override
            public void always(ApiResponse response) {
                super.always(response);
                hideProgressDialog();
            }
        }).exec();
    }

}

