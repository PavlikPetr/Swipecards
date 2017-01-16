package com.topface.topface.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.nostra13.universalimageloader.utils.StorageUtils;
import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.AddedPhoto;
import com.topface.topface.data.AppOptions;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Profile;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PhotoAddProfileRequest;
import com.topface.topface.requests.PhotoAddRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.state.EventBus;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.statistics.TakePhotoStatistics;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.dialogs.TakePhotoDialog;
import com.topface.topface.ui.dialogs.take_photo.TakePhotoActionHolder;
import com.topface.topface.ui.fragments.profile.ProfilePhotoFragment;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.notifications.UserNotification;
import com.topface.topface.utils.notifications.UserNotificationManager;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import permissions.dispatcher.NeedsPermission;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

import static com.topface.topface.ui.dialogs.take_photo.TakePhotoPopup.ACTION_CAMERA_CHOSEN;
import static com.topface.topface.ui.dialogs.take_photo.TakePhotoPopup.ACTION_CANCEL;
import static com.topface.topface.ui.dialogs.take_photo.TakePhotoPopup.ACTION_GALLERY_CHOSEN;
import static com.topface.topface.ui.dialogs.take_photo.TakePhotoPopup.ACTION_UNDEFINED;

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
    public static final String EXTRA_BUTTON_ID = "btn_id";
    public static String PATH_TO_FILE;
    private static HashMap<String, File> fileNames = new HashMap<>();
    private String mFileName = "/tmp.jpg";
    private Context mContext;
    private WeakReference<Activity> mActivity;
    private WeakReference<Fragment> mFragment;
    private Handler mHandler;
    private View mProgressView;
    private AppOptions.MinPhotoSize minPhotoSize;
    private UserNotificationManager mNotificationManager;
    private File outputFile;
    private DialogInterface.OnCancelListener mOnDialogCancelListener;
    @Inject
    static TopfaceAppState mAppState;
    @Inject
    EventBus mEventBus;
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

    private Subscription mPhotoActionSubscription;

    public AddPhotoHelper(Fragment fragment, View progressView) {
        this(fragment.getActivity());
        mFragment = new WeakReference<>(fragment);
        this.mProgressView = progressView;
    }

    public AddPhotoHelper(Activity activity) {
        App.get().inject(this);
        minPhotoSize = App.getAppOptions().getMinPhotoSize();
        mActivity = new WeakReference<>(activity);
        mContext = activity.getApplicationContext();
        PATH_TO_FILE = StorageUtils.getCacheDirectory(mContext).getPath() + "/topface_profile/";
        initPhotoActionSubscription();
    }

    private void initPhotoActionSubscription() {
        mPhotoActionSubscription = mEventBus.getObservable(TakePhotoActionHolder.class)
                .filter(new Func1<TakePhotoActionHolder, Boolean>() {
                    @Override
                    public Boolean call(TakePhotoActionHolder holder) {
                        return holder.getAction() != ACTION_UNDEFINED && !TextUtils.isEmpty(holder.getPlc());
                    }
                })
                .subscribe(new Action1<TakePhotoActionHolder>() {
                    @Override
                    public void call(TakePhotoActionHolder holder) {
                        if (holder != null) {
                            switch ((int) holder.getAction()) {
                                case (int) ACTION_CAMERA_CHOSEN:
                                    startCamera(false);
                                    System.out.println("PopupHive  ACTION_CAMERA_CHOSEN ");
                                    TakePhotoStatistics.sendCameraAction(holder.getPlc());
                                    break;
                                case (int) ACTION_GALLERY_CHOSEN:
                                    startChooseFromGallery(false);
                                    System.out.println("PopupHive  ACTION_GALLERY_CHOSEN ");
                                    TakePhotoStatistics.sendGalleryAction(holder.getPlc());
                                    break;
                                case (int) ACTION_CANCEL:
                                    TakePhotoStatistics.sendCancelAction(holder.getPlc());
                            }
                            mEventBus.setData(new TakePhotoActionHolder());
                        }
                    }
                });
    }

    public void releaseHelper() {
        if (!mPhotoActionSubscription.isUnsubscribed()) {
            mPhotoActionSubscription.unsubscribe();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
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

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
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
                preferences.edit().putString(FILENAME_CONST, mFileName).apply();
                File outputDirectory = new File(PATH_TO_FILE);
                //noinspection ResultOfMethodCallIgnored
                if (!outputDirectory.exists()) {
                    if (!outputDirectory.mkdirs() && getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showErrorMessage();
                            }
                        });
                        return;
                    }
                }
                outputFile = new File(outputDirectory, mFileName);
                intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(outputFile));
                intent = Intent.createChooser(intent, mContext.getResources().getString(R.string.album_add_photo_title));

                if (Utils.isIntentAvailable(mContext, intent.getAction())) {
                    startAddPhotoActivity(intent, requestCode);
                }
            }
        };
    }

    @Nullable
    public Activity getActivity() {
        if (mFragment != null && mFragment.get() != null) {
            return mFragment.get().getActivity();
        }
        if (mActivity != null && mActivity.get() != null) {
            return mActivity.get();
        }
        return null;
    }

    public void startChooseFromGallery() {
        startChooseFromGallery(false);
    }

    public void startChooseFromGallery(boolean withDialog) {
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent = Intent.createChooser(intent, mContext.getResources().getString(R.string.album_add_photo_title));
        boolean noSuitableActivity = intent.resolveActivity(getActivity().getPackageManager()) == null;
        int requestCode = withDialog || noSuitableActivity ? GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY_WITH_DIALOG :
                GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY;
        startAddPhotoActivity(intent, requestCode);
    }

    private void startAddPhotoActivity(Intent intent, int requestCode) {
        Fragment fragment = null;
        if (mFragment != null) {
            fragment = mFragment.get();
        }
        Activity activity = getActivity();
        if (fragment != null) {
            if (fragment.isAdded()) {
                if (fragment instanceof ProfilePhotoFragment) {
                    fragment.getParentFragment().startActivityForResult(intent, requestCode);
                } else {
                    fragment.startActivityForResult(intent, requestCode);
                }
            } else if (activity != null) {
                activity.startActivityForResult(intent, requestCode);
            }
        } else {
            if (activity != null) {
                activity.startActivityForResult(intent, requestCode);
            }

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
        //check for result from TakePhotoActivity
        return processActivityResult(requestCode, resultCode, data, true);
    }

    private Uri processActivityResult(int requestCode, int resultCode, Intent data, boolean sendPhotoRequest) {
        Uri photoUri = null;
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
                        if (!TextUtils.isEmpty(filename)) {
                            File outputDirectory = new File(PATH_TO_FILE);
                            //noinspection ResultOfMethodCallIgnored
                            if (outputDirectory.exists()) {
                                outputFile = new File(outputDirectory, filename);
                                photoUri = Uri.fromFile(outputFile);
                                preferences.edit().remove(FILENAME_CONST).apply();
                            }

                        }
                    }
                    break;
                case GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY:
                case GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY_WITH_DIALOG:
                    //Если она взята из галереи, то получаем URL из данных интента и преобразуем его в путь до файла
                    if (data != null) {
                        photoUri = data.getData();
                    }
                    break;
            }

            //Отправляем запрос
            if (sendPhotoRequest && photoUri != null) {
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

        if (!isPhotoCorrectSize(uri)) {
            Utils.showToastNotification(String.format(mContext.getString(R.string.incorrect_photo_size_show_restrictions),
                    minPhotoSize.width,
                    minPhotoSize.height), Toast.LENGTH_SHORT);
            return;
        }
        // если начинаем грузить аватарку, то выставляем флаг, чтобы resumeFragment не вызвал показ попапа
        Profile profile = App.from(mContext).getProfile();
        if (profile.photos != null && profile.photos.size() == 0) {
            UserConfig userConfig = App.getConfig().getUserConfig();
            userConfig.setUserAvatarAvailable(true);
            userConfig.saveConfig();
        }
        Utils.showToastNotification(R.string.photo_is_uploading, Toast.LENGTH_SHORT);
        showProgressDialog();
        mNotificationManager = UserNotificationManager.getInstance();

        final PhotoNotificationListener notificationListener = new PhotoNotificationListener();

        mNotificationManager.showProgressNotificationAsync(
                mContext.getString(R.string.default_photo_upload),
                uri.toString(), getIntentForNotification(), notificationListener
        );

        final PhotoAddRequest photoAddRequest = new PhotoAddProfileRequest(uri, mContext, new IProgressListener() {
            @Override
            public void onProgress(final int percentage) {
                if (notificationListener.notification != null) {
                    //Видимо из-за ошибок в прошивке на редких девайсах с Android 4.0.4
                    //падает - https://rink.hockeyapp.net/manage/apps/26531/app_versions/62/crash_reasons/12857941?type=overview
                    //поэтому ловим все ошибки
                    try {
                        notificationListener.notification.updateProgress(percentage);
                        mNotificationManager.showBuildedNotification(notificationListener.notification);
                    } catch (Exception e) {
                        Debug.error(e);
                    }
                }

            }

            @Override
            public void onSuccess() {

            }
        });
        //TODO также обрабатывать запросы с id...x, где x-порядковый номер переповтора
        fileNames.put(photoAddRequest.getId(), outputFile);
        photoAddRequest.callback(new DataApiHandler<AddedPhoto>() {
            @Override
            protected void success(AddedPhoto photo, IApiResponse response) {
                if (mHandler != null) {
                    Message msg = new Message();
                    msg.what = ADD_PHOTO_RESULT_OK;
                    msg.obj = photo.getPhoto();
                    mHandler.sendMessage(msg);
                }
                mNotificationManager.showNotificationAsync(
                        0,
                        mContext.getString(R.string.default_photo_upload_complete), "", false,
                        uri.toString(), 1, getIntentForNotification(), true, null, null);
                CacheProfile.incrementPhotoPosition(mContext, 1);
            }

            @Override
            protected AddedPhoto parseResponse(ApiResponse response) {
                return new AddedPhoto(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(ADD_PHOTO_RESULT_ERROR);
                } else {
                    Utils.showToastNotification(mContext.getString(R.string.photo_add_error), Toast.LENGTH_LONG);
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
        return new Intent(App.getContext(), NavigationActivity.class)
                .putExtra(GCMUtils.NEXT_INTENT, new LeftMenuSettingsData(FragmentIdData.PROFILE))
                .putExtra(GCMUtils.NOTIFICATION_INTENT, true)
                .putExtra(GCMUtils.GCM_LABEL, GCMUtils.NOTIFICATION_LABEL)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    private void showErrorMessage(int codeError) {
        switch (codeError) {
            case ErrorCodes.INCORRECT_PHOTO_DATA:
                Utils.showToastNotification(mContext.getString(R.string.incorrect_photo), Toast.LENGTH_LONG);
                break;
            case ErrorCodes.INCORRECT_PHOTO_FORMAT:
                Utils.showToastNotification(mContext.getString(R.string.incorrect_photo_format), Toast.LENGTH_LONG);
                break;
            case ErrorCodes.INCORRECT_PHOTO_SIZES:
                Utils.showToastNotification(String.format(mContext.getString(R.string.incorrect_photo_size_show_restrictions),
                        minPhotoSize.width,
                        minPhotoSize.height), Toast.LENGTH_SHORT);
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
    @SuppressWarnings("unused")
    public void showTakePhotoDialog(IPhotoTakerWithDialog photoTaker, Uri photoUri) {
        showTakePhotoDialog(photoTaker, photoUri, null);
    }

    /**
     * Gets TakePhotoDialog from FragmentManager if it has been created before
     * or creates new TakePhotoDialog if there is no any in FragmentManager
     * After it has been obtained or created method shows it
     *
     * @param photoTaker object which implement "take photo's" methods
     * @param photoUri   if we already have photo to show pass it with Uri to show
     * @param message    text that is displayed in the popup
     */
    public void showTakePhotoDialog(final IPhotoTakerWithDialog photoTaker, Uri photoUri, String message) {
        FragmentManager manager = photoTaker.getActivityFragmentManager();
        TakePhotoDialog takePhotoDialog = (TakePhotoDialog) manager.findFragmentByTag(TakePhotoDialog.TAG);
        if (takePhotoDialog == null) {
            takePhotoDialog = TakePhotoDialog.newInstance(photoUri);
            takePhotoDialog.setUri(photoUri).setMessageText(message).show(manager, TakePhotoDialog.TAG);
        } else {
            if (takePhotoDialog.isAdded()) {
                takePhotoDialog.setUri(photoUri).setMessageText(message);
            } else {
                takePhotoDialog.setUri(photoUri).setMessageText(message).show(manager, TakePhotoDialog.TAG);
            }
        }
        setOnResultHandler(new Handler() {
            @SuppressWarnings("ConstantConditions")
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
        takePhotoDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (mOnDialogCancelListener != null) {
                    mOnDialogCancelListener.onCancel(dialog);
                }
                setOnResultHandler(new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_OK) {
                            handlePhotoMessage(msg);
                        }
                    }
                });
            }
        });
    }

    @SuppressWarnings("unused")
    public void setOnCancelListener(DialogInterface.OnCancelListener listener) {
        mOnDialogCancelListener = listener;
    }

    public static void handlePhotoMessage(Message msg) {
        Profile profile = App.get().getProfile();
        if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_OK) {
            Photo photo = (Photo) msg.obj;
            if (profile.photos != null && photo != null) {
                // ставим фото на аватарку только если она едиснтвенная
                if (profile.photos.size() == 0) {
                    profile.photo = photo;
                }
                // добавляется фото в начало списка
                profile.photos.addFirst(photo);
                // Увеличиваем общее количество фотографий юзера
                profile.photosCount += 1;
            }
            // оповещаем всех об изменениях
            CacheProfile.sendUpdateProfileBroadcast();
            mAppState.setData(profile);
            Toast.makeText(App.getContext(), R.string.photo_add_or, Toast.LENGTH_SHORT).show();
        } else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR) {
            // если загрузка аватраки не завершилась успехом, то сбрасываем флаг
            if (profile.photos != null && profile.photos.size() == 0) {
                UserConfig userConfig = App.getConfig().getUserConfig();
                userConfig.setUserAvatarAvailable(false);
                userConfig.saveConfig();
            }
            Toast.makeText(App.getContext(), R.string.photo_add_error, Toast.LENGTH_SHORT).show();
        }
    }

    public static class PhotoNotificationListener implements UserNotificationManager.NotificationImageListener {
        public boolean needShowNotification = true;
        private UserNotification notification;

        @Override
        public void onSuccess(UserNotification notification) {
            this.notification = notification;
        }

        public int getId() {

            return notification == null ? -1 : notification.getId();
        }

        @Override
        public void onFail() {
        }

        @Override
        public boolean needShowNotification() {
            return needShowNotification;
        }
    }

    private BitmapFactory.Options getPhotoSizeByUri(Uri uri) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            Activity activity = getActivity();
            if (activity != null) {
                InputStream stream = activity.getApplicationContext().getContentResolver().openInputStream(uri);
                BitmapFactory.decodeStream(stream, null, options);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return options;
    }

    private boolean isPhotoCorrectSize(Uri uri) {
        BitmapFactory.Options currentPhotoSize = getPhotoSizeByUri(uri);
        return currentPhotoSize != null && !(currentPhotoSize.outWidth < minPhotoSize.width ||
                currentPhotoSize.outHeight < minPhotoSize.height);
    }

}

