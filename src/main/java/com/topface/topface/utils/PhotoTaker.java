package com.topface.topface.utils;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PhotoMainRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;

public class PhotoTaker implements IPhotoTakerWithDialog {
    private AddPhotoHelper mPhotoHelper;
    private FragmentActivity mActivity;

    public PhotoTaker(final AddPhotoHelper photoHelper, final FragmentActivity activity) {
        mPhotoHelper = photoHelper;
        mActivity = activity;
    }

    @Override
    public void takePhoto() {
        mPhotoHelper.startCamera(true);
    }

    @Override
    public void choosePhotoFromGallery() {
        mPhotoHelper.startChooseFromGallery(true);
    }

    @Override
    public void onTakePhotoDialogSentSuccess(final Photo photo) {
        final Profile profile = App.from(mActivity).getProfile();
        if (profile.photos != null) {
            profile.photos.add(photo);
            profile.photosCount += 1;
        }
        PhotoMainRequest request = new PhotoMainRequest(mActivity);
        request.photoId = photo.getId();
        request.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                profile.photo = photo;
                App.sendProfileRequest();
                Utils.showToastNotification(R.string.photo_add_or, Toast.LENGTH_SHORT);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (codeError == ErrorCodes.NON_EXIST_PHOTO_ERROR) {
                    if (profile.photos != null && profile.photos.contains(photo)) {
                        profile.photos.remove(photo);
                    }
                    Utils.showToastNotification(App.getContext().getString(R.string.general_wrong_photo_upload), Toast.LENGTH_LONG);
                }
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
            }
        }).exec();
    }

    @Override
    public void onTakePhotoDialogSentFailure() {
        Utils.showToastNotification(R.string.photo_add_error, Toast.LENGTH_SHORT);
    }

    @Override
    public void onTakePhotoDialogDismiss() {
        if (CacheProfile.needToSelectCity(mActivity)) {
            CacheProfile.selectCity(mActivity);
        }
    }

    @Override
    public void sendPhotoRequest(Uri uri) {
        mPhotoHelper.sendRequest(uri);
    }

    @Override
    public FragmentManager getActivityFragmentManager() {
        return mActivity.getSupportFragmentManager();
    }
}