package com.topface.topface.ui.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.PhotoAddRequest;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.TopfaceNotificationManager;
import com.topface.topface.utils.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

/**
 * Хелпер для загрузки фотографий в любой активити
 */
public class AddPhotoHelper {

    public static final String CANCEL_NOTIFICATION_RECEIVER = "CancelNotificationReceiver";
    public static String PATH_TO_FILE;
    private String mFileName = "/tmp.jpg";

    private Context mContext;
    private Activity mActivity;
    private Fragment mFragment;
    private Handler mHandler;
    private LockerView mLockerView;

    public static final int ADD_PHOTO_RESULT_OK = 0;
    public static final int ADD_PHOTO_RESULT_ERROR = 1;
    public static final int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA = 101;
    public static final int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY = 100;
    private TopfaceNotificationManager mNotificationManager;
    private File outputFile;
    private static HashMap<String, File> fileNames = new HashMap<String, File>();


    public AddPhotoHelper(Fragment fragment, LockerView mLockerView) {
        this(fragment.getActivity());
        mFragment = fragment;
        this.mLockerView = mLockerView;
    }

    public AddPhotoHelper(Fragment fragment) {
        this(fragment.getActivity());
        mFragment = fragment;
    }

    public AddPhotoHelper(Activity activity) {
        mActivity = activity;
        mContext = activity.getApplicationContext();
        PATH_TO_FILE = StorageUtils.getCacheDirectory(mContext).getPath() + "/topface_profile/";
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
                case R.id.btnAddPhotoAlbum:
                case R.id.btnTakeFormGallery:
                    startChooseFromGallery();
                    break;
                case R.id.btnAddPhotoCamera:
                case R.id.btnTakePhoto:
                    startCamera();
                    break;
            }
        }
    };

    private void startCamera() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                UUID uuid = UUID.randomUUID();
                mFileName = "/" + uuid.toString() + ".jpg";
                File outputDirectory = new File(PATH_TO_FILE);
                //noinspection ResultOfMethodCallIgnored
                if (!outputDirectory.exists()) {
                    if (!outputDirectory.mkdirs()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, R.string.general_data_error, 1500).show();
                            }
                        });
                        return;
                    }
                }
                outputFile = new File(outputDirectory, mFileName);
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile));
                intent = Intent.createChooser(intent, mContext.getResources().getString(R.string.profile_add_title));

                if (Utils.isIntentAvailable(mContext, intent.getAction())) {
                    if (mFragment != null) {
                        if (mFragment.isAdded()) {
                            if (mFragment instanceof ProfilePhotoFragment) {
                                mFragment.getParentFragment().startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA);
                            } else {
                                mFragment.startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA);
                            }
                        }
                    } else {
                        mActivity.startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA);
                    }
                }


            }
        }).start();
    }

    public Activity getActivity() {
        return (mFragment == null) ? mActivity : mFragment.getActivity();
    }

    private void startChooseFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent = Intent.createChooser(intent, mContext.getResources().getString(R.string.profile_add_title));
        if (mFragment != null) {
            if (mFragment instanceof ProfilePhotoFragment) {
                mFragment.getParentFragment().startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY);
            } else {
                mFragment.startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY);
            }
        } else {
            mActivity.startActivityForResult(intent, GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY);
        }
    }

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

    public Uri processActivityResult(int requestCode, int resultCode, Intent data) {
        return processActivityResult(requestCode, resultCode, data, true);
    }

    public Uri processActivityResult(int requestCode, int resultCode, Intent data, boolean sendPhotoRequest) {
        Uri photoUri = null;
        if (mFragment != null) {
            if (mFragment.getActivity() != null && !mFragment.isAdded()) {
                Debug.log("APH::detached");
            }
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA) {
                //Если фотография сделана, то ищем ее во временном файле
                if (outputFile != null) {
                    photoUri = Uri.fromFile(outputFile);
                }
            } else if (requestCode == GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY) {
                //Если она взята из галереи, то получаем URL из данных интента и преобразуем его в путь до файла
                photoUri = data.getData();
            }

            //Отправляем запрос
            if (sendPhotoRequest) {
                sendRequest(photoUri);
            }
        }

        return photoUri;
    }

    /**
     * Отправляет запрос к API с прикрепленной фотографией
     *
     * @param uri фотографии
     */
    public void sendRequest(final Uri uri) {
        if (uri == null) {
            if (mHandler != null) {
                mHandler.sendEmptyMessage(ADD_PHOTO_RESULT_ERROR);
            }
            return;
        }
        showProgressDialog();
        mNotificationManager = TopfaceNotificationManager.getInstance(mContext);

        final TopfaceNotificationManager.TempImageViewRemote fakeImageView = new TopfaceNotificationManager.TempImageViewRemote(mContext);
        fakeImageView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.MATCH_PARENT));
        final OnNotificationListener listener = new OnNotificationListener();
        fakeImageView.setRemoteSrc(uri.toString(), new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                listener.onNotificationIdReceived(mNotificationManager.showProgressNotification(mContext.getString(R.string.default_photo_upload), fakeImageView.getImageBitmap(), new Intent(mActivity, NavigationActivity.class).putExtra(GCMUtils.NEXT_INTENT, BaseFragment.F_PROFILE)));
            }
        });

        final PhotoAddRequest photoAddRequest = new PhotoAddRequest(uri, mContext);
        fileNames.put(photoAddRequest.getId(), outputFile);
        photoAddRequest.callback(new DataApiHandler<Photo>() {
            @Override
            protected void success(Photo photo, ApiResponse response) {
                if (mHandler != null) {
                    Message msg = new Message();
                    msg.what = ADD_PHOTO_RESULT_OK;
                    msg.obj = photo;
                    mHandler.sendMessage(msg);
                }
                listener.onResponseReceived();
                mNotificationManager.showNotification(mContext.getString(R.string.default_photo_upload_complete), "", false, fakeImageView.getImageBitmap(), 1, new Intent(mActivity, NavigationActivity.class).putExtra(GCMUtils.NEXT_INTENT, BaseFragment.F_PROFILE), true);
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
                showErrorMessage(codeError);
                listener.onResponseReceived();
                mNotificationManager.showFailNotification(mContext.getString(R.string.default_photo_upload_error), "", fakeImageView.getImageBitmap(), new Intent(mActivity, NavigationActivity.class).putExtra(GCMUtils.NEXT_INTENT, BaseFragment.F_PROFILE));
            }

            @Override
            public void always(final ApiResponse response) {
                super.always(response);
                hideProgressDialog();
                //Удаляем все временные картинки
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String id = photoAddRequest.getId();
                            if (fileNames != null) {
                                if(fileNames.size() != 0) {
                                    if (fileNames.get(id).delete()) {
                                        Debug.log("Delete temp photo " + id);
                                    } else {
                                        Debug.log("Error delete temp photo " + id);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Debug.error(e);
                        }

                    }
                }).start();
            }
        }).exec();
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mNotificationManager.cancelNotification(intent.getIntExtra("id",1));
                if (!intent.getBooleanExtra("isRetry", false) && photoAddRequest != null) {
                    photoAddRequest.cancel();
                } else if(intent.getBooleanExtra("isRetry", false)) {
                    sendRequest(uri);
                }
                mContext.unregisterReceiver(this);
            }
        }, new IntentFilter(CANCEL_NOTIFICATION_RECEIVER));
    }

    private void showErrorMessage(int codeError) {
        switch (codeError) {
            case ApiResponse.INCORRECT_PHOTO_DATA:
                Toast.makeText(mContext, mContext.getString(R.string.incorrect_photo), 2000).show();
                break;
            case ApiResponse.INCORRECT_PHOTO_FORMAT:
                Toast.makeText(mContext, mContext.getString(R.string.incorrect_photo_format), 2000).show();
                break;
            case ApiResponse.INCORRECT_PHOTO_SIZES:
                Toast.makeText(mContext, mContext.getString(R.string.incorrect_photo_size), 2000).show();
                break;
        }
    }



    private class OnNotificationListener {
        private int notificationId = -1;
        private boolean isResponseReceived = false;

        public void onNotificationIdReceived(int id) {
            if (isResponseReceived) {
                mNotificationManager.cancelNotification(id);
            } else {
                notificationId = id;
            }
        }

        public void onResponseReceived() {
            if (notificationId != -1) {
                mNotificationManager.cancelNotification(notificationId);
            } else {
                isResponseReceived = true;
            }
        }

    }

}

