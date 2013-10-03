package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.requests.ComplainRequest;
import com.topface.topface.ui.BaseFragmentActivity;

public class ComplainsFragment extends BaseFragment {

    public static final String USERID = "USERID";
    public static final String FEEDID = "FEEDID";
    private int userId;
    private String feedId;

    public static ComplainsFragment newInstance(int userId) {
        ComplainsFragment fragment = new ComplainsFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(USERID, userId);
        fragment.setArguments(arguments);
        return fragment;
    }

    public static ComplainsFragment newInstance(int userId, String feedId) {
        ComplainsFragment fragment = new ComplainsFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(USERID, userId);
        arguments.putString(FEEDID, feedId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View root = inflater.inflate(R.layout.complains_fragment, container, false);
        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getInt(USERID);
            feedId = arguments.getString(FEEDID);
            initViews(root);
        } else {
            getActivity().finish();
        }
        return root;
    }

    private void initViews(View root) {
        setHeaders(root);
        initItems(root);
    }

    private void setHeaders(View root) {
        setText(R.string.complain_photo_header, (ViewGroup)root.findViewById(R.id.loPhotoHeader), ComplainRequest.ClassNames.PHOTO);
        setText(R.string.complain_info_header, (ViewGroup) root.findViewById(R.id.loInfoHeader), ComplainRequest.ClassNames.USER);
        setText(R.string.complain_msg_header, (ViewGroup) root.findViewById(R.id.loMsgHeader), ComplainRequest.ClassNames.PRIVATE_MSG);
    }

    private void initItems(View root) {
        initItem(root, ComplainRequest.ClassNames.PHOTO, ComplainRequest.TypesNames.PORN, R.drawable.edit_big_btn_top_selector);
        initItem(root, ComplainRequest.ClassNames.PHOTO, ComplainRequest.TypesNames.ERO, R.drawable.edit_big_btn_middle_selector);
        initItem(root, ComplainRequest.ClassNames.PHOTO, ComplainRequest.TypesNames.FAKE_PHOTO, R.drawable.edit_big_btn_middle_selector);
        initItem(root, ComplainRequest.ClassNames.PHOTO, ComplainRequest.TypesNames.FAKE_USER, R.drawable.edit_big_btn_bottom_selector);

        initItem(root, ComplainRequest.ClassNames.PRIVATE_MSG, ComplainRequest.TypesNames.SPAM, R.drawable.edit_big_btn_top_selector);
        initItem(root, ComplainRequest.ClassNames.PRIVATE_MSG, ComplainRequest.TypesNames.SWEARING, R.drawable.edit_big_btn_bottom_selector);

        initItem(root, ComplainRequest.ClassNames.USER, ComplainRequest.TypesNames.SPAM, R.drawable.edit_big_btn_top_selector);
        initItem(root, ComplainRequest.ClassNames.USER, ComplainRequest.TypesNames.FAKE_DATA, R.drawable.edit_big_btn_middle_selector);
        initItem(root, ComplainRequest.ClassNames.USER, ComplainRequest.TypesNames.SWEARING, R.drawable.edit_big_btn_bottom_selector);
    }

    private void initItem(View root, final ComplainRequest.ClassNames className, final ComplainRequest.TypesNames typeName, int bgId) {
        ComplainItem item = getItemIdByClassAndType(className, typeName);
        if (item != null) {
            RelativeLayout frame = (RelativeLayout) root.findViewById(item.id);
            if (canHideItem(className)) {
                frame.setVisibility(View.GONE);
            } else {
                ((ImageView)frame.findViewWithTag("ivEditBackground")).setImageResource(bgId);
                setText(item.title, frame, className);
                frame.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((BaseFragmentActivity)getActivity()).startFragment(ComplainsMessageFragment.newInstance(userId, feedId, className, typeName));
                    }
                });
            }
        }
    }

    public static ComplainItem getItemIdByClassAndType(ComplainRequest.ClassNames className, ComplainRequest.TypesNames typeName) {
        switch (typeName) {
            case PORN:
                return new ComplainItem(R.id.typePorn, R.string.complain_type_porn);
            case ERO:
                return new ComplainItem(R.id.typeEro, R.string.complain_type_ero);
            case FAKE_PHOTO:
                return new ComplainItem(R.id.typePicture, R.string.complain_type_picture);
            case FAKE_USER:
                return new ComplainItem(R.id.typeFake, R.string.complain_type_fake_user);
            case SPAM:
                return className == ComplainRequest.ClassNames.PRIVATE_MSG?
                        new ComplainItem(R.id.typeSpam, R.string.complain_type_spam_msg) :
                        new ComplainItem(R.id.typeCommercial, R.string.complain_type_spam_profile);
            case SWEARING:
                return className == ComplainRequest.ClassNames.PRIVATE_MSG?
                        new ComplainItem(R.id.typeSwear, R.string.complain_type_swearing_msg) :
                        new ComplainItem(R.id.typeSwearData, R.string.complain_type_swearing_profile);
            case FAKE_DATA:
                return new ComplainItem(R.id.typeFakeData, R.string.complain_type_fake_data);
        }
        return null;
    }

    private void setText(int titleId, ViewGroup frame, ComplainRequest.ClassNames className) {
        if (canHideItem(className)) {
            frame.setVisibility(View.GONE);
        } else {
            ((TextView) frame.findViewWithTag("tvTitle")).setText(titleId);
        }
    }

    private boolean canHideItem(ComplainRequest.ClassNames className) {
        return feedId != null && className != ComplainRequest.ClassNames.PRIVATE_MSG;
    }

    public static class ComplainItem {
        public int id;
        public int title;

        ComplainItem(int id, int title) {
            this.id = id;
            this.title = title;
        }
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_complain);
    }
}
