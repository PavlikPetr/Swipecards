package com.topface.topface.viewModels;

import android.databinding.ObservableField;
import android.view.View;

import com.topface.framework.imageloader.IPhoto;
import com.topface.topface.data.leftMenu.LeftMenuHeaderData;
import com.topface.topface.utils.Utils;

/**
 * Created by ppavlik on 05.05.16.
 */
public class LeftMenuHeaderViewModel {

    public ObservableField<IPhoto> photo = new ObservableField<>(null);
    public ObservableField<String> userName = new ObservableField<>(Utils.EMPTY);
    public ObservableField<String> userCity = new ObservableField<>(Utils.EMPTY);
    public ObservableField<String> background = new ObservableField<>(null);

    private IPhoto mPhoto;
    private String mUserNAme;
    private String mUserCity;
    private View.OnClickListener mOnClick;

    public LeftMenuHeaderViewModel(LeftMenuHeaderData data) {
        setPhoto(data.getPhoto());
        setName(data.getName());
        setCity(data.getCity());
        mOnClick = data.getOnHeaderClickListener();
    }

    private void setPhoto(IPhoto photo) {
        if (mPhoto == null || !mPhoto.equals(photo)) {
            mPhoto = photo;
            this.photo.set(photo);
            background.set(photo != null ? photo.getDefaultLink() : null);
        }
    }

    private void setName(String name) {
        if (mUserNAme == null || !mUserNAme.equals(name)) {
            mUserNAme = name;
            userName.set(name);
        }
    }

    private void setCity(String city) {
        if (mUserCity == null || !mUserCity.equals(city)) {
            mUserCity = city;
            userCity.set(city);
        }
    }

    public void onClick(View view) {
        if (mOnClick != null) {
            mOnClick.onClick(view);
        }
    }
}
