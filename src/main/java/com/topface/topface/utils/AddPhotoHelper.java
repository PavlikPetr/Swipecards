package com.topface.topface.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.nostra13.universalimageloader.utils.StorageUtils;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PhotoAddRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.dialogs.TakePhotoDialog;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.profile.ProfilePhotoFragment;
import com.topface.topface.utils.notifications.UserNotification;
import com.topface.topface.utils.notifications.UserNotificationManager;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

/**
 * Хелпер для загрузки фотографий в любой активити
 */
public class AddPhotoHelper {

    public static final String CANCEL_NOTIFICATION_RECEIVER = "CancelNotificationReceiver";
    public static final String FILENAME_CONST = "filename";
    public static final int ADD_PHOTO_RESULT_OK = 0;
    public static final int ADD_PHOTO_RESULT_ERROR = 1;
    public static final int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA_WITH_DIALOG = 1703;
    public static final int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA = 1702;
    public static final int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY_WITH_DIALOG = 1701;
    public static final int GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY = 1700;
    public static String PATH_TO_FILE;
    private static HashMap<String, File> fileNames = new HashMap<>();
    private String mFileName = "/tmp.jpg";
    private Context mContext;
    private Activity mActivity;
    private Fragment mFragment;
    private Handler mHandler;
    private View mProgressView;
    private UserNotificationManager mNotificationManager;
    private File outputFile;
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

    public AddPhotoHelper(Fragment fragment, View progressView) {
        this(fragment.getActivity());
        mFragment = fragment;
        this.mProgressView = progressView;
    }

    public AddPhotoHelper(Activity activity) {
        mActivity = activity;
        mContext = activity.getApplicationContext();
        PATH_TO_FILE = StorageUtils.getCacheDirectory(mContext).getPath() + "/topface_profile/";
    }

    public void showProgressDialog() {
        if (mProgressView != null) {
            mProgressView.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressDialog() {
        if (mProgressView != null) {
            mProgressView.setVisibility(View.GONE);
        }
    }

    public OnClickListener getAddPhotoClickListener() {
        return mOnAddPhotoClickListener;
    }

    public void startCamera() {
        startCamera(false);
    }

    public void startCamera(final boolean withDialog) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        new BackgroundThread() {
            @Override
            public void execute() {
                int requestCode = withDialog ? GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA_WITH_DIALOG :
                        GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA;
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                UUID uuid = UUID.randomUUID();
                mFileName = "/" + uuid.toString() + ".jpg";
                preferences.edit().putString(FILENAME_CONST, mFileName).commit();
                File outputDirectory = new File(PATH_TO_FILE);
                //noinspection ResultOfMethodCallIgnored
                if (!outputDirectory.exists()) {
                    if (!outputDirectory.mkdirs()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, R.string.general_data_error, Toast.LENGTH_SHORT).show();
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
                                mFragment.getParentFragment().startActivityForResult(intent, requestCode);
                            } else {
                                mFragment.startActivityForResult(intent, requestCode);
                            }
                        }
                    } else {
                        mActivity.startActivityForResult(intent, requestCode);
                    }
                }


            }
        };
    }

    public Activity getActivity() {
        return (mFragment == null) ? mActivity : mFragment.getActivity();
    }

    public void startChooseFromGallery() {
        startChooseFromGallery(false);
    }

    public void startChooseFromGallery(boolean withDialog) {
        int requestCode = withDialog ? GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY_WITH_DIALOG :
                GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY;
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent = Intent.createChooser(intent, mContext.getResources().getString(R.string.profile_add_title));
        if (mFragment != null) {
            if (mFragment instanceof ProfilePhotoFragment) {
                mFragment.getParentFragment().startActivityForResult(intent, requestCode);
            } else {
                mFragment.startActivityForResult(intent, requestCode);
            }
        } else {
            mActivity.startActivityForResult(intent, requestCode);
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
            switch (requestCode) {
                case GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA:
                case GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA_WITH_DIALOG:
                    //Если фотография сделана, то ищем ее во временном файле
                    if (outputFile != null) {
                        photoUri = Uri.fromFile(outputFile);
                    } else {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                        String filename = preferences.getString(FILENAME_CONST, "");
                        if (!filename.equals("")) {
                            File outputDirectory = new File(PATH_TO_FILE);
                            //noinspection ResultOfMethodCallIgnored
                            if (outputDirectory.exists()) {
                                outputFile = new File(outputDirectory, filename);
                                photoUri = Uri.fromFile(outputFile);
                                preferences.edit().remove(FILENAME_CONST).commit();
                            }

                        }
                    }
                    break;
                case GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY:
                case GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY_WITH_DIALOG:
                    //Если она взята из галереи, то получаем URL из данных интента и преобразуем его в путь до файла
                    photoUri = data.getData();
                    break;
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
        Toast.makeText(mContext, R.string.photo_is_uploading, Toast.LENGTH_SHORT).show();
        showProgressDialog();
        mNotificationManager = UserNotificationManager.getInstance(mContext);

        final PhotoNotificationListener notificationListener = new PhotoNotificationListener();

        mNotificationManager.showProgressNotificationAsync(
                mContext.getString(R.string.default_photo_upload),
                uri.toString(), getIntentForNotification(), notificationListener
        );

        final PhotoAddRequest photoAddRequest = new PhotoAddRequest(uri, mContext, new Base64.ProgressListener() {
            @Override
            public void onProgress(final int percentage) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (notificationListener.notification != null) {
                            notificationListener.notification.updateProgress(percentage);
                            mNotificationManager.showBuildedNotification(notificationListener.notification);
                        }

                    }
                });
            }

            @Override
            public void onSuccess() {

            }
        });
        //TODO также обрабатывать запросы с id...x, где x-порядковый номер переповтора
        fileNames.put(photoAddRequest.getId(), outputFile);
        photoAddRequest.callback(new DataApiHandler<Photo>() {
            @Override
            protected void success(Photo photo, IApiResponse response) {
                if (mHandler != null) {
                    Message msg = new Message();
                    msg.what = ADD_PHOTO_RESULT_OK;
                    msg.obj = photo;
                    mHandler.sendMessage(msg);
                }
                mNotificationManager.showNotificationAsync(
                        mContext.getString(R.string.default_photo_upload_complete), "", false,
                        uri.toString(), 1, getIntentForNotification(), true, null, null);
            }

            @Override
            protected Photo parseResponse(ApiResponse response) {
                return new Photo(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(ADD_PHOTO_RESULT_ERROR);
                }
                photoAddRequest.cancel();
                showErrorMessage(codeError);
                notificationListener.needShowNotification = false;
                mNotificationManager.showFailNotificationAsync(
                        mContext.getString(R.string.default_photo_upload_error), "",
                        uri.toString(), getIntentForNotification(), null);
            }

            @Override
            public void always(final IApiResponse response) {
                super.always(response);
                hideProgressDialog();
                mNotificationManager.cancelNotification(notificationListener.getId());
                //Удаляем все временные картинки
                new BackgroundThread() {
                    @Override
                    public void execute() {
                        try {
                            String id = photoAddRequest.getId();
                            //TODO также обрабатывать запросы с id...x, где x-порядковый номер переповтора
                            if (fileNames != null) {
                                if (fileNames.size() != 0) {
                                    File file = fileNames.get(id);
                                    if (file != null && file.delete()) {
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
                };
            }
        }).exec();

    }

    private Intent getIntentForNotification() {
        return new Intent(mActivity, NavigationActivity.class)
                .putExtra(GCMUtils.NEXT_INTENT, BaseFragment.FragmentId.F_PROFILE)
                .putExtra(GCMUtils.NOTIFICATION_INTENT, true)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    

    private void showErrorMessage(int codeError) {
        switch (codeError) {
            case ErrorCodes.INCORRECT_PHOTO_DATA:
                Toast.makeText(mContext, mContext.getString(R.string.incorrect_photo), Toast.LENGTH_LONG).show();
                break;
            case ErrorCodes.INCORRECT_PHOTO_FORMAT:
                Toast.makeText(mContext, mContext.getString(R.string.incorrect_photo_format), Toast.LENGTH_LONG).show();
                break;
            case ErrorCodes.INCORRECT_PHOTO_SIZES:
                Toast.makeText(mContext, mContext.getString(R.string.incorrect_photo_size), Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * Gets TakePhotoDialog from FragmentManager if it has been created before
     * or creates new TakePhotoDialog if there is no any in FragmentManager
     * After it has been obtained or created method shows it
     *
     * @param photoTaker object which implement "take photo's" methods
     * @param photoUri   if we already have photo to show pass it with Uri to show
     */
    public void showTakePhotoDialog(final IPhotoTakerWithDialog photoTaker, Uri photoUri) {
        FragmentManager manager = photoTaker.getActivityFragmentManager();
        TakePhotoDialog takePhotoDialog = (TakePhotoDialog) manager.findFragmentByTag(TakePhotoDialog.TAG);
        if (takePhotoDialog == null) {
            takePhotoDialog = TakePhotoDialog.newInstance(photoUri);
            takePhotoDialog.setUri(photoUri).show(manager, TakePhotoDialog.TAG);
        } else {
            if (takePhotoDialog.isAdded()) {
                takePhotoDialog.setUri(photoUri);
            } else {
                takePhotoDialog.setUri(photoUri).show(manager, TakePhotoDialog.TAG);
            }
        }
        setOnResultHandler(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_OK) {
                    Photo photo = (Photo) msg.obj;
                    if (photoTaker != null)
                        photoTaker.onTakePhotoDialogSentSuccess(photo);
                } else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR) {
                    if (photoTaker != null) photoTaker.onTakePhotoDialogSentFailure();
                }
            }
        });
        takePhotoDialog.setPhotoTaker(photoTaker);
    }

    public static class PhotoNotificationListener implements UserNotificationManager.NotificationImageListener {
        public boolean needShowNotification = true;
        private UserNotification notification;

        @Override
        public void onSuccess(UserNotification notification) {
            this.notification = notification;
        }

        public int getId() {

            return notification == null? -1 : notification.getId();
        }

        @Override
        public void onFail() {
        }

        @Override
        public boolean needShowNotification() {
            return needShowNotification;
        }
    }



}

