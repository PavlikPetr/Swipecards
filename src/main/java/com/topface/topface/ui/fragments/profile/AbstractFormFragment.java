package com.topface.topface.ui.fragments.profile;

import android.app.Activity;
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
import com.topface.topface.Static;
import com.topface.topface.data.BasePendingInit;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.FeedGiftsRequest;
import com.topface.topface.requests.IApiResponse;
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
    private static final String GIFTS_COUNT = "GIFTS_COUNT";
    private static final String USER_STATUS = "USER_STATUS";

    private AbstractFormListAdapter mFormAdapter;
    private LinearLayout mGiftsHeader;
    private TextView mGiftsCounter;
    private GiftsStripAdapter mGiftAdapter;
    private float mGiftSize;
    private float mGiftsCounterSize;
    private int mVisibleGiftsNumber;
    private int mUserId;
    private LinkedList<FormItem> mForms;
    private Profile.Gifts mGifts;
    private int mGiftsCount;
    private ListView mListQuestionnaire;
    private DisplayMetrics mMetrics;
    private String mStatus;

    private BasePendingInit<Profile> mPendingUserInit = new BasePendingInit<>();

    private DataSetObserver mGiftsObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            View root = getView();
            if (root != null && mGiftsHeader != null) {
                fillGiftsStrip();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity activity = getActivity();
        mGiftAdapter = new GiftsStripAdapter(activity, new FeedList<FeedGift>(), null);
        mFormAdapter = createFormAdapter(activity);
        mGiftSize = activity.getResources().getDimension(R.dimen.form_gift_size);
        mGiftsCounterSize = activity.getResources().getDimension(R.dimen.form_gift_counter_size);
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
        mVisibleGiftsNumber = (mMetrics.widthPixels - mGiftsHeader.getPaddingLeft() -
                mGiftsHeader.getPaddingRight() - (int) mGiftsCounterSize) / (int) mGiftSize;
        mGiftAdapter.registerDataSetObserver(mGiftsObserver);

        mListQuestionnaire.addHeaderView(giftsHeaderWrapper);
        mListQuestionnaire.setAdapter(mFormAdapter);

        if (mForms != null) {
            setUserData(mStatus, mUserId, mForms, mGifts, mGiftsCount);
        } else if (savedInstanceState != null) {
            ArrayList<Parcelable> parcelableArrayList = savedInstanceState.getParcelableArrayList(FORM_ITEMS);
            ArrayList<Gift> parcelableGifts = savedInstanceState.getParcelableArrayList(FORM_GIFTS);
            int giftsCount = savedInstanceState.getInt(GIFTS_COUNT);
            if (parcelableArrayList != null && parcelableGifts != null) {
                Profile.Gifts gifts = new Profile.Gifts();
                gifts.addAll(parcelableGifts);
                String status = savedInstanceState.getString(USER_STATUS);
                setUserData(
                        status != null ? status : Static.EMPTY,
                        savedInstanceState.getInt(USER_ID, 0),
                        parcelableArrayList, gifts, giftsCount
                );

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
            fillGiftsStrip();
        }
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
        outState.putInt(GIFTS_COUNT, mGiftsCount);
        outState.putString(USER_STATUS, mStatus);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPendingUserInit.setCanSet(false);
        mGiftAdapter.unregisterDataSetObserver(mGiftsObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void fillGiftsStrip() {
        if (isAdded()) {
            if (!mGiftAdapter.isEmpty()) {
                mGiftsHeader.setVisibility(View.VISIBLE);
            } else {
                mGiftsHeader.setVisibility(View.GONE);
                return;
            }

            int giftsCount = mGiftsCount <= 0 ? mGiftAdapter.getCount() : mGiftsCount;
            int viewsNumber = mGiftsHeader.getChildCount();
            for (int i = (giftsCount < mVisibleGiftsNumber ? giftsCount : mVisibleGiftsNumber) - 1; i >= 0; i--) {
                View giftView;
                if (viewsNumber > i) {
                    giftView = mGiftAdapter.getView(i, mGiftsHeader.getChildAt(i), mGiftsHeader);
                } else {
                    giftView = mGiftAdapter.getView(i, null, mGiftsHeader);
                    giftView.setLayoutParams(new ViewGroup.LayoutParams((int) mGiftSize, (int) mGiftSize));
                }

                if (giftView.getParent() == null) {
                    mGiftsHeader.addView(giftView, 0);
                }
            }

            if (mGiftsCount == -1 && giftsCount >= mVisibleGiftsNumber) {
                getGiftsCounterTextView().setText(R.string.more);
            } else if (giftsCount > mVisibleGiftsNumber) {
                int counter = (giftsCount - mVisibleGiftsNumber);
                getGiftsCounterTextView().setText(counter <= 99 ? "+" + counter : "99+");
            }
        }
    }

    private TextView getGiftsCounterTextView() {
        if (mGiftsCounter == null) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            mGiftsCounter = (TextView) (inflater.inflate(R.layout.remained_gifts_counter,
                    mGiftsHeader, true)).findViewById(R.id.textGiftsCounter);
        }
        return mGiftsCounter;
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

    public void setUserData(String status, int userId, LinkedList<FormItem> forms, Profile.Gifts gifts, int giftsCount) {
        mStatus = status;
        mUserId = userId;
        mForms = forms;
        mGifts = gifts;
        mGiftsCount = giftsCount;

        mFormAdapter.setUserData(mStatus, mForms);
        mFormAdapter.notifyDataSetChanged();
        mGiftAdapter.getData().clear();
        for (Gift gift : gifts) {
            FeedGift feedGift = new FeedGift();
            feedGift.gift = gift;
            mGiftAdapter.add(feedGift);
        }
        mGiftAdapter.notifyDataSetChanged();

        if (isNotEnoughGifts()) {
            requestGifts();
        }
    }

    private boolean isNotEnoughGifts() {
        int giftsCnt = mGiftAdapter.getCount();
        return giftsCnt < mVisibleGiftsNumber && giftsCnt < mGiftsCount;
    }

    private void requestGifts() {
        FeedGiftsRequest giftsRequest = new FeedGiftsRequest(getActivity());
        giftsRequest.uid = mUserId;
        giftsRequest.limit = mVisibleGiftsNumber;
        if (mGiftAdapter.getCount() > 0) {
            giftsRequest.from = mGiftAdapter.getItem(mGiftAdapter.getCount() - 1).gift.feedId;
        }
        registerRequest(giftsRequest);
        giftsRequest.callback(new DataApiHandler<FeedListData<FeedGift>>() {

            @Override
            public void fail(int codeError, IApiResponse response) {

            }

            @Override
            protected void success(FeedListData<FeedGift> data, IApiResponse response) {
                for (int i = 0; i < mVisibleGiftsNumber - mGiftAdapter.getCount(); i++) {
                    if (i < data.items.size()) {
                        mGiftAdapter.add(data.items.get(i));
                    } else {
                        break;
                    }
                }
                mGiftAdapter.notifyDataSetChanged();
            }

            @Override
            protected FeedListData<FeedGift> parseResponse(ApiResponse response) {
                return new FeedListData<>(response.getJsonResult(), FeedGift.class);
            }
        }).exec();
    }

    private void setUserData(String status, int userId, ArrayList<Parcelable> forms, Profile.Gifts gifts, int giftsCount) {
        LinkedList<FormItem> llForms = new LinkedList<>();
        for (Parcelable form : forms) {
            llForms.add((FormItem) form);
        }
        setUserData(status, userId, llForms, gifts, giftsCount);
    }

    private void setUserDataPending(Profile user) {
        setUserData(user.getStatus(), user.uid, user.forms, user.gifts, user.gifts.count);
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
