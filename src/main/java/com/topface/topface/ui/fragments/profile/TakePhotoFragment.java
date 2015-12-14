package com.topface.topface.ui.fragments.profile;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;

public class TakePhotoFragment extends BaseFragment implements View.OnClickListener {
    public static final int ACTION_CAMERA_CHOOSEN = 1;
    public static final int ACTION_GALLERY_CHOOSEN = 2;
    public static final int ACTION_CANCEL = 3;

    @IntDef({ACTION_CAMERA_CHOOSEN, ACTION_GALLERY_CHOOSEN, ACTION_CANCEL})
    public @interface TakePhotoUserAction{
    }

    public static TakePhotoFragment newInstance() {
        TakePhotoFragment fragment = new TakePhotoFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        getSupportActionBar().show();
        super.onCreateView(inflater, container, saved);
        View root = inflater.inflate(R.layout.fragment_take_photo, null);
        View view = root.findViewById(R.id.upload_photo_placeholder);
        view.setBackgroundResource(CacheProfile.sex == Static.GIRL ? R.drawable.upload_photo_female : R.drawable.upload_photo_male);

        Button btn = (Button) root.findViewById(R.id.btn_take_photo);
        btn.setOnClickListener(this);
        btn = (Button) root.findViewById(R.id.btn_choose_photo);
        btn.setOnClickListener(this);

        return root;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.album_add_photo_title);
    }

    @Override
    public void onClick(View v) {
        FragmentActivity activity = getActivity();
        if(activity != null && activity instanceof ITakePhotoUserActionListener) {
            int viewId = v.getId();
            switch (viewId) {
                case R.id.btn_take_photo:
                    ((ITakePhotoUserActionListener) activity).onTakePhotoUserAction(ACTION_CAMERA_CHOOSEN);
                    break;
                case R.id.btn_choose_photo:
                    ((ITakePhotoUserActionListener) activity).onTakePhotoUserAction(ACTION_GALLERY_CHOOSEN);
                    break;
            }
        }
    }

    public interface ITakePhotoUserActionListener {
        void onTakePhotoUserAction(@TakePhotoUserAction int userAction);
    }
}
