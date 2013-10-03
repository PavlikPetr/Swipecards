package com.topface.topface.ui;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Gift;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.GiftsRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.ui.fragments.GiftsFragment;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.ui.views.TripleButton;

import java.util.ArrayList;
import java.util.LinkedList;

public class GiftsActivity extends BaseFragmentActivity {

    public static final int INTENT_REQUEST_GIFT = 111;
    public static final String INTENT_GIFT_ID = "gift_id";
    public static final String INTENT_GIFT_URL = "gift_url";
    public static final String INTENT_GIFT_PRICE = "gift_price";
    public static final String GIFTS_LIST = "gifts_list";
    public static ArrayList<Gift> mGiftsList = new ArrayList<Gift>();

    public GiftsCollection mGiftsCollection;

    private LockerView mLoadingLocker;
    private TripleButton mTripleButton;

    private GiftsFragment mGiftFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.ac_gifts);

        getSupportActionBar().setTitle(getString(R.string.gifts_title));

        mLoadingLocker = (LockerView) this.findViewById(R.id.llvGiftsLoading);

        mGiftFragment = new GiftsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.giftGrid, mGiftFragment, GiftsFragment.GIFTS_ALL_TAG).commit();

        mGiftsCollection = new GiftsCollection();

        // init triple button
        mTripleButton = (TripleButton) findViewById(R.id.btnTriple);
        mTripleButton.setLeftText(Gift.getTypeNameResId(Gift.ROMANTIC));
        mTripleButton.setMiddleText(Gift.getTypeNameResId(Gift.FRIENDS));
        mTripleButton.setRightText(Gift.getTypeNameResId(Gift.PRESENT));

        mTripleButton.setLeftListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        mGiftsCollection.setCurrentType(Gift.ROMANTIC);
                        mGiftFragment.setGifts(mGiftsCollection.getGifts());
                    }
                });
            }
        });

        mTripleButton.setMiddleListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        mGiftsCollection.setCurrentType(Gift.FRIENDS);
                        mGiftFragment.setGifts(mGiftsCollection.getGifts());
                    }
                });

            }
        });

        mTripleButton.setRightListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        mGiftsCollection.setCurrentType(Gift.PRESENT);
                        mGiftFragment.setGifts(mGiftsCollection.getGifts());
                    }
                });
            }
        });

    }

    /**
     * Loading array of gifts from server
     */
    private void update() {
        if (mGiftsList.isEmpty()) {
            mTripleButton.setChecked(TripleButton.LEFT_BUTTON);
            mLoadingLocker.setVisibility(View.VISIBLE);
            GiftsRequest giftRequest = new GiftsRequest(this);
            registerRequest(giftRequest);
            giftRequest.callback(new DataApiHandler<LinkedList<Gift>>() {

                @Override
                protected void success(LinkedList<Gift> data, IApiResponse response) {
                    mGiftsList.addAll(data);
                    mGiftsCollection.add(mGiftsList);
                    mGiftFragment.setGifts(mGiftsCollection.getGifts());

                    mLoadingLocker.setVisibility(View.GONE);
                }

                @Override
                protected LinkedList<Gift> parseResponse(ApiResponse response) {
                    return Gift.parse(response);
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    Toast.makeText(
                            App.getContext(),
                            R.string.general_data_error,
                            Toast.LENGTH_SHORT
                    ).show();
                    mLoadingLocker.setVisibility(View.GONE);
                }
            }).exec();
        } else {
            mGiftsCollection.add(mGiftsList);
            mGiftFragment.setGifts(mGiftsCollection.getGifts());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGiftsList.isEmpty()) {
            update();
        } else {
            mGiftsCollection.add(mGiftsList);
            mGiftFragment.setGifts(mGiftsCollection.getGifts());
        }

        switch (GiftsCollection.currentType) {
            case Gift.ROMANTIC:
                mTripleButton.setChecked(TripleButton.LEFT_BUTTON);
                break;
            case Gift.FRIENDS:
                mTripleButton.setChecked(TripleButton.MIDDLE_BUTTON);
                break;
            case Gift.PRESENT:
                mTripleButton.setChecked(TripleButton.RIGHT_BUTTON);
                break;
            default:
                break;
        }
    }

    /**
     * Works with array of gifts, categorizes by type
     */
    public static class GiftsCollection {
        public static int currentType = Gift.ROMANTIC;
        private LinkedList<Gift> mAllGifts = new LinkedList<Gift>();

        public void add(ArrayList<Gift> gifts) {
            mAllGifts.addAll(gifts);
        }

        public ArrayList<Gift> getGifts(int type) {
            ArrayList<Gift> result = new ArrayList<Gift>();
            for (Gift gift : mAllGifts) {
                if (gift.type == type) {
                    result.add(gift);
                }
            }

            return result;
        }

        public ArrayList<Gift> getGifts() {
            return getGifts(GiftsCollection.currentType);
        }

        public void setCurrentType(int type) {
            GiftsCollection.currentType = type;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(GIFTS_LIST, mGiftsList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mGiftsList = savedInstanceState.getParcelableArrayList(GIFTS_LIST);
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

}
