package com.topface.topface.viewModels;

import android.databinding.ObservableField;
import android.view.View;

import com.topface.framework.imageloader.IPhoto;
import com.topface.topface.data.HeaderFooterData;
import com.topface.topface.data.leftMenu.LeftMenuHeaderViewData;
import com.topface.topface.utils.Utils;

public class LeftMenuHeaderViewModel {

    public ObservableField<Object> photo = new ObservableField<>(null);
    public ObservableField<String> userName = new ObservableField<>(Utils.EMPTY);
    public ObservableField<String> userCity = new ObservableField<>(Utils.EMPTY);
    public ObservableField<String> background = new ObservableField<>(null);

    private HeaderFooterData.OnViewClickListener<LeftMenuHeaderViewData> mOnClick;
    private HeaderFooterData<LeftMenuHeaderViewData> mData;

    public LeftMenuHeaderViewModel(HeaderFooterData<LeftMenuHeaderViewData> data) {
        if (mData == null || mData.getData() == null) {
            setPhoto(data.getData().getPhoto());
            setName(data.getData().getName());
            setCity(data.getData().getCity());
            mData = data;
        } else if (mData.getData().getPhoto() == null || !mData.getData().getPhoto().equals(data.getData().getPhoto())) {
            setPhoto(data.getData().getPhoto());
        } else if (mData.getData().getName() == null || !mData.getData().getName().equals(data.getData().getName())) {
            setName(data.getData().getName());
        } else if (mData.getData().getCity() == null || !mData.getData().getCity().equals(data.getData().getCity())) {
            setCity(data.getData().getCity());
        }
        mOnClick = data.getClickListener();

    }

    private void setPhoto(IPhoto photo) {
        if (mData != null && mData.getData() != null) {
            mData.getData().setPhoto(photo);
        }
        this.photo.set(photo);
        background.set(photo != null ? photo.getDefaultLink() : null);
    }

    private void setName(String name) {
        if (mData != null && mData.getData() != null) {
            mData.getData().setName(name);
        }
        userName.set(name);
    }

    private void setCity(String city) {
        if (mData != null && mData.getData() != null) {
            mData.getData().setCity(city);
        }
        userCity.set(city);
    }

    public void onClick(View view) {
        if (mOnClick != null) {
            mOnClick.onClick(view, mData != null ? mData.getData() : null);
        }
    }
}
