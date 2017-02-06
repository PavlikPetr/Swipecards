package com.topface.topface.viewModels;

import android.content.Context;
import android.content.res.Resources;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.view.View;

import com.topface.framework.imageloader.IPhoto;
import com.topface.topface.App;
import com.topface.topface.data.HeaderFooterData;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.LeftMenuHeaderViewData;
import com.topface.topface.utils.Utils;

import static com.topface.topface.ui.fragments.MenuFragment.ITEM_TAG_TEMPLATE;

public class LeftMenuHeaderViewModel {

    public ObservableField<IPhoto> photo = new ObservableField<>(null);
    public ObservableField<String> userName = new ObservableField<>(Utils.EMPTY);
    public ObservableField<String> userAge = new ObservableField<>(Utils.EMPTY);
    public ObservableField<String> userCity = new ObservableField<>(Utils.EMPTY);
    public ObservableField<String> background = new ObservableField<>(null);
    public final static String AGE_TEMPLATE = ", %d";
    public ObservableInt topMargin = new ObservableInt(0);

    private HeaderFooterData.OnViewClickListener<LeftMenuHeaderViewData> mOnClick;
    private HeaderFooterData<LeftMenuHeaderViewData> mData;

    public LeftMenuHeaderViewModel(HeaderFooterData<LeftMenuHeaderViewData> data) {
        if (mData == null || mData.getData() == null) {
            setPhoto(data.getData().getPhoto());
            setName(data.getData().getName());
            setAge(data.getData().getAge());
            setCity(data.getData().getCity());
            mData = data;
        } else if (mData.getData().getPhoto() == null || !mData.getData().getPhoto().equals(data.getData().getPhoto())) {
            setPhoto(data.getData().getPhoto());
        } else if (mData.getData().getName() == null || !mData.getData().getName().equals(data.getData().getName())) {
            setName(data.getData().getName());
        } else if (mData.getData().getAge() != data.getData().getAge()) {
            setAge(data.getData().getAge());
        } else if (mData.getData().getCity() == null || !mData.getData().getCity().equals(data.getData().getCity())) {
            setCity(data.getData().getCity());
        }
        mOnClick = data.getClickListener();
        topMargin.set(mData.getData().isIsTranslucentEnabled() ? getStatusBarHeight(App.getContext()) : 0);
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

    private void setAge(int age) {
        if (mData != null && mData.getData() != null) {
            mData.getData().setAge(age);
        }
        userAge.set(String.format(App.getCurrentLocale(), AGE_TEMPLATE, age));
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

    // тэги для автоматизированного тестирования
    public String getTag(){
        return String.format(App.getCurrentLocale(), ITEM_TAG_TEMPLATE, FragmentIdData.PROFILE);
    }

    private int getStatusBarHeight(Context context) {
        //todo использовать метод из утилит, когда он туда подтянется из предыдущей версии
        Resources resources = context.getApplicationContext().getResources();
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
