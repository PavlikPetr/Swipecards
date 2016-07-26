package com.topface.topface.viewModels;

import android.databinding.ObservableField;
import android.text.TextUtils;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class HeaderPhotoBlogViewModel {
    public final ObservableField<String> urlAvatar = new ObservableField<>();

    public HeaderPhotoBlogViewModel() {
        setUrlAvatar(App.get().getProfile());
    }

    public void setUrlAvatar(@NotNull Profile profile) {
        String photoUrl = profile.photo != null ? profile.photo.getDefaultLink() : Utils.EMPTY;
        urlAvatar.set(TextUtils.isEmpty(photoUrl)? Utils.getLocalResUrl(profile.sex == Profile.BOY ?
                R.drawable.upload_photo_male : R.drawable.upload_photo_female):photoUrl);
    }
}