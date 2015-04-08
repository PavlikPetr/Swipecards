package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.BasePendingInit;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.GiftsStripAdapter;
import com.topface.topface.utils.FormItem;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Abstract class for user's and own forms.
 */
public abstract class AbstractFormFragment extends ProfileInnerFragment {

    private static final String FORM_ITEMS = "FORM_ITEMS";
    private static final String FORM_GIFTS = "FORM_GIFTS";
    private static final String POSITION = "POSITION";
    private static final String USER_ID = "USER_ID";

    private AbstractFormListAdapter mFormAdapter;
    private LinearLayout mGiftsHeader;
    private GiftsStripAdapter mGiftAdapter;
    private float mGiftSize;
    private int mUserId;
    private LinkedList<FormItem> mForms;
    private Profile.Gifts mGifts;
    private ListView mListQuestionnaire;
    private DisplayMetrics mMetrics;

    private BasePendingInit<Profile> mPendingUserInit = new BasePendingInit<>();

    private DataSetObserver mGiftsObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            View root = getView();
            if (root != null) {
                fillGiftsStrip();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGiftAdapter = new GiftsStripAdapter(getActivity(), new FeedList<FeedGift>(), null);
        mGiftAdapter.registerDataSetObserver(mGiftsObserver);
        mFormAdapter = createFormAdapter(getActivity());
        mGiftSize = getActivity().getResources().getDimension(R.dimen.form_gift_size);
        mMetrics = getActivity().getResources().getDisplayMetrics();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_form, container, false);
        mListQuestionnaire = (ListView) root.findViewById(R.id.fragmentFormList);

        View giftsHeaderWrapper = inflater.inflate(R.layout.form_gifts, null);
        mGiftsHeader = (LinearLayout) giftsHeaderWrapper.findViewById(R.id.gifts_strip);
        mGiftsHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdded()) {
                    onGiftsClick();
                }
            }
        });
        mListQuestionnaire.addHeaderView(giftsHeaderWrapper);
        mListQuestionnaire.setAdapter(mFormAdapter);

        if (mForms != null) {
            setUserData(mUserId, mForms, mGifts);
        } else if (savedInstanceState != null) {
            ArrayList<Parcelable> parcelableArrayList = savedInstanceState.getParcelableArrayList(FORM_ITEMS);
            ArrayList<Gift> parcelableGifts = savedInstanceState.getParcelableArrayList(FORM_GIFTS);
            if (parcelableArrayList != null && parcelableGifts != null) {
                Profile.Gifts gifts = new Profile.Gifts();
                gifts.addAll(parcelableGifts);

                setUserData(savedInstanceState.getInt(USER_ID, 0),
                        parcelableArrayList, gifts);

                mListQuestionnaire.setSelection(savedInstanceState.getInt(POSITION, 0));
            }
        }
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPendingUserInit.setCanSet(true);
        if (mPendingUserInit.getCanSet()) {
            setUserDataPending(mPendingUserInit.getData());
        }

        if (mPendingUserInit.getCanSet()) fillGiftsStrip();
    }

    protected abstract void onGiftsClick();

    @Override
    public void onResume() {
        super.onResume();
        UserProfileFragment fragment = getUserProfileFragment();
        if (fragment != null) {
            ArrayList<FeedGift> newGifts = fragment.getNewGifts();
            if (newGifts != null && newGifts.size() > 0) {
                ArrayList<FeedGift> adapterGifts = mGiftAdapter.getData();

                boolean needNotifyFormAdapter = false;
                if (mGiftAdapter.isEmpty()) {
                    needNotifyFormAdapter = true;
                }
                adapterGifts.addAll(0, newGifts);
                mGiftAdapter.notifyDataSetChanged();
                clearNewGiftsArray();

                if (needNotifyFormAdapter) {
                    mFormAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(USER_ID, mUserId);
        outState.putParcelableArrayList(FORM_ITEMS, mFormAdapter.saveState());
        outState.putParcelableArrayList(FORM_GIFTS, mGifts);
        outState.putInt(POSITION, mListQuestionnaire.getFirstVisiblePosition());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPendingUserInit.setCanSet(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGiftAdapter.unregisterDataSetObserver(mGiftsObserver);
    }

    private void fillGiftsStrip() {
        if (isAdded()) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            final int visibleGiftsNumber = (mMetrics.widthPixels - mGiftsHeader.getPaddingLeft() -
                    mGiftsHeader.getPaddingRight() - (int) (mGiftSize / 4)) / (int) mGiftSize;

            if (!mGiftAdapter.isEmpty()) {
                mGiftsHeader.setVisibility(View.VISIBLE);
            } else {
                mGiftsHeader.setVisibility(View.GONE);
                return;
            }

            int giftsCount = mGiftAdapter.getCount();
            mGiftsHeader.removeAllViews();
            for (int i = 0; i < giftsCount && i < visibleGiftsNumber; i++) {
                View giftView = mGiftAdapter.getView(i, null, mGiftsHeader);
                giftView.setLayoutParams(new ViewGroup.LayoutParams((int) mGiftSize, (int) mGiftSize));
                mGiftsHeader.addView(giftView);
            }
            if (giftsCount > visibleGiftsNumber) {
                TextView giftsCounter = (TextView) inflater.inflate(R.layout.remained_gifts_counter,
                        mGiftsHeader, false);
                giftsCounter.setText("+" + (giftsCount - visibleGiftsNumber));
                mGiftsHeader.addView(giftsCounter);
            }
        }
    }

    private UserProfileFragment getUserProfileFragment() {
        Fragment fragment = getParentFragment();
        if (fragment instanceof UserProfileFragment) {
            return (UserProfileFragment) fragment;
        } else {
            Debug.error("Fragment not equals UserProfileFragment");
            return null;
        }
    }

    private boolean clearNewGiftsArray() {
        UserProfileFragment fragment = getUserProfileFragment();
        if (fragment != null) {
            fragment.clearNewFeedGift();
            return true;
        }
        return false;
    }

    protected abstract AbstractFormListAdapter createFormAdapter(Context context);

    public void setUserData(int userId, LinkedList<FormItem> forms, Profile.Gifts gifts) {
        mUserId = userId;
        mForms = forms;
        mGifts = gifts;
        mFormAdapter.setUserData(mForms);
        mFormAdapter.notifyDataSetChanged();
        mGiftAdapter.getData().clear();
        for (Gift gift : gifts) {
            FeedGift feedGift = new FeedGift();
            feedGift.gift = gift;
            mGiftAdapter.add(feedGift);
        }
        mGiftAdapter.notifyDataSetChanged();
    }

    private void setUserData(int userId, ArrayList<Parcelable> forms, Profile.Gifts gifts) {
        LinkedList<FormItem> llForms = new LinkedList<>();
        for (Parcelable form : forms) {
            llForms.add((FormItem) form);
        }
        setUserData(userId, llForms, gifts);
    }

    private void setUserDataPending(Profile user) {
        setUserData(user.uid, user.forms, user.gifts);
    }

    public int getUserId() {
        return mUserId;
    }

    public AbstractFormListAdapter getFormAdapter() {
        return mFormAdapter;
    }

    public void setUserData(Profile profile) {
        mPendingUserInit.setData(profile);
        if (mPendingUserInit.getCanSet()) {
            setUserDataPending(mPendingUserInit.getData());
        }
    }
}
