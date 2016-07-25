package com.topface.topface.viewModels;

import android.databinding.ObservableField;
import android.text.TextUtils;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.utils.Utils;

public class HeaderWantTopViewModel {
    public final ObservableField<String> urlAvatar = new ObservableField<>();
    public final ObservableField<String> text = new ObservableField<>();

    public HeaderWantTopViewModel() {
        setUrlAvatar(App.get().getProfile());
        text.set(App.getContext().getString(R.string.photofeed_add_photo_to_feed_title));
    }

    public void setUrlAvatar(Profile profile) {
        String photoUrl = profile.photo != null ? profile.photo.getDefaultLink() : Utils.EMPTY;
        String finalImg = TextUtils.isEmpty(photoUrl)? Utils.getLocalResUrl(profile.sex == Profile.BOY ?
                R.drawable.upload_photo_male : R.drawable.upload_photo_female):photoUrl;
        urlAvatar.set(finalImg);
        text.set(finalImg);
    }
}
