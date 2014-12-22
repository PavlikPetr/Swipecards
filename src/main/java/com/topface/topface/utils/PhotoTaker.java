package com.topface.topface.utils;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PhotoMainRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.fragments.profile.PhotoSwitcherActivity;

import java.util.ArrayList;

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
        if (CacheProfile.photos != null) {
            CacheProfile.photos.add(photo);
            CacheProfile.totalPhotos += 1;
            Intent intent = new Intent(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT);
            intent.putExtra(PhotoSwitcherActivity.INTENT_PHOTOS, CacheProfile.photos);
            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(intent);
        } else {
            Intent intent = new Intent(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT);
            ArrayList<Photo> photos = new ArrayList<>();
            photos.add(photo);
            intent.putParcelableArrayListExtra(PhotoSwitcherActivity.INTENT_PHOTOS, photos);
        }
        PhotoMainRequest request = new PhotoMainRequest(mActivity);
        request.photoId = photo.getId();
        request.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                CacheProfile.photo = photo;
                App.sendProfileRequest();
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (codeError == ErrorCodes.NON_EXIST_PHOTO_ERROR) {
                    if (CacheProfile.photos != null && CacheProfile.photos.contains(photo)) {
                        CacheProfile.photos.remove(photo);
                    }
                    Toast.makeText(
                            mActivity,
                            App.getContext().getString(R.string.general_wrong_photo_upload),
                            Toast.LENGTH_LONG
                    ).show();
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
        Toast.makeText(App.getContext(), R.string.photo_add_error, Toast.LENGTH_SHORT).show();
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