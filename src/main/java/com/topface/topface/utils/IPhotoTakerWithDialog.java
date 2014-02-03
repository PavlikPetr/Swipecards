package com.topface.topface.utils;

import android.net.Uri;
import android.support.v4.app.FragmentManager;

import com.topface.topface.data.Photo;

/**
 * Created by kirussell on 31.01.14.
 * Interface for object that can perform TakePhoto actions through AddPhotoHelper
 */
public interface IPhotoTakerWithDialog {
    void takePhoto();

    void choosePhotoFromGallery();

    void onTakePhotoDialogSentSuccess(Photo photo);

    void onTakePhotoDialogSentFailure();

    void onTakePhotoDialogDismiss();

    void sendPhotoRequest(Uri uri);

    FragmentManager getActivityFragmentManager();
}
