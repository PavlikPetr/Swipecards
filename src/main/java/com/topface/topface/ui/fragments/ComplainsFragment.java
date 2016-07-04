package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.requests.ComplainRequest;
import com.topface.topface.ui.ComplainsMessageActivity;

import butterknife.BindView;

public class ComplainsFragment extends BaseFragment {

    private static final String SCROLL_VIEW_LIST = "scroll_view_list";

    public static final String USERID = "USERID";
    public static final String FEEDID = "FEEDID";
    private int userId;
    private String feedId;

    @BindView(R.id.complains_scroll)
    ScrollView mScroll;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.complains_fragment, container, false);
        bindView(this, root);
        Bundle args = getArguments();
        int scroll = 0;
        if (savedInstanceState != null) {
            scroll = savedInstanceState.getInt(SCROLL_VIEW_LIST);
        }
        userId = args.getInt(USERID, -1);
        feedId = args.getString(FEEDID);
        initViews(root);
        if (userId == -1) {
            getActivity().finish();
        }
        mScroll.scrollTo(0, scroll);
        return root;
    }

    private void initViews(View root) {
        setHeaders(root);
        initItems(root);
    }

    private void setHeaders(View root) {
        setText(R.string.complain_photo_header, (TextView) root.findViewById(R.id.loPhotoHeader), ComplainRequest.ClassNames.PHOTO);
        setText(R.string.complain_info_header, (TextView) root.findViewById(R.id.loInfoHeader), ComplainRequest.ClassNames.USER);
        setText(R.string.complain_msg_header, (TextView) root.findViewById(R.id.loMsgHeader), ComplainRequest.ClassNames.PRIVATE_MSG);
    }

    private void initItems(View root) {
        initItem(root, ComplainRequest.ClassNames.PHOTO, ComplainRequest.TypesNames.PORN);
        initItem(root, ComplainRequest.ClassNames.PHOTO, ComplainRequest.TypesNames.ERO);
        initItem(root, ComplainRequest.ClassNames.PHOTO, ComplainRequest.TypesNames.FAKE_PHOTO);
        initItem(root, ComplainRequest.ClassNames.PHOTO, ComplainRequest.TypesNames.FAKE_USER);

        initItem(root, ComplainRequest.ClassNames.PRIVATE_MSG, ComplainRequest.TypesNames.SPAM);
        initItem(root, ComplainRequest.ClassNames.PRIVATE_MSG, ComplainRequest.TypesNames.SWEARING);

        initItem(root, ComplainRequest.ClassNames.USER, ComplainRequest.TypesNames.SPAM);
        initItem(root, ComplainRequest.ClassNames.USER, ComplainRequest.TypesNames.FAKE_DATA);
        initItem(root, ComplainRequest.ClassNames.USER, ComplainRequest.TypesNames.SWEARING);
    }

    private void initItem(View root, final ComplainRequest.ClassNames className, final ComplainRequest.TypesNames typeName) {
        ComplainItem item = getItemIdByClassAndType(className, typeName);
        if (item != null) {
            TextView text = (TextView) root.findViewById(item.id);
            if (canHideItem(className)) {
                text.setVisibility(View.GONE);
            } else {
                setText(item.title, text, className);
                text.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(ComplainsMessageActivity.createIntent(getActivity(), userId, feedId, className, typeName));
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
                return className == ComplainRequest.ClassNames.PRIVATE_MSG ?
                        new ComplainItem(R.id.typeSpam, R.string.complain_type_spam_msg) :
                        new ComplainItem(R.id.typeCommercial, R.string.complain_type_spam_profile);
            case SWEARING:
                return className == ComplainRequest.ClassNames.PRIVATE_MSG ?
                        new ComplainItem(R.id.typeSwear, R.string.complain_type_swearing_msg) :
                        new ComplainItem(R.id.typeSwearData, R.string.complain_type_swearing_profile);
            case FAKE_DATA:
                return new ComplainItem(R.id.typeFakeData, R.string.complain_type_fake_data);
        }
        return null;
    }

    private void setText(int titleId, TextView text, ComplainRequest.ClassNames className) {
        if (canHideItem(className)) {
            text.setVisibility(View.GONE);
        } else {
            text.setText(titleId);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mScroll != null) {
            outState.putInt(SCROLL_VIEW_LIST, mScroll.getScrollY());
        }
    }
}
