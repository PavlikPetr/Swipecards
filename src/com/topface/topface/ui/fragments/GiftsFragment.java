package com.topface.topface.ui.fragments;

import java.util.LinkedList;

import com.topface.topface.R;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Gift;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.data.User;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.SendGiftRequest;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.adapters.GiftsAdapter;
import com.topface.topface.ui.adapters.GiftsAdapter.ViewHolder;
import com.topface.topface.ui.profile.UserProfileActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.GiftGalleryManager;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.GridLayoutAnimationController;
import android.widget.AdapterView;
import android.widget.GridView;

/**
 * GiftFragment displays gifts on a GridView For displaying all gifts (like
 * GiftActivity do) have to attach GIFTS_ALL_TAG For displaying profile gifts
 * (like UserProfileActiviry do) have to attach GIFTS_PROFILE_TAG: in this case
 * first element will be add_gift button.
 * 
 * @author kirussell
 * 
 */

public class GiftsFragment extends Fragment {

	// Constants
	private String mTag;

	public static final String GIFTS_ALL_TAG = "giftsGridAll";
	public static final String GIFTS_PROFILE_TAG = "giftsGridProfile";
	public static final int GIFTS_COLUMN_PORTRAIT = 3;
	public static final int GIFTS_COLUMN_LANDSCAPE = 5;

	// Grid elements
	private GiftsAdapter mGridAdapter;
	private GiftGalleryManager<Gift> mGalleryManager;
	private GridView mGridView;

	private User mUser;
	private LinkedList<Gift> mGifts = new LinkedList<Gift>();

	// TODO Data giftsList remove

	@Override
	public void onAttach(Activity activity) {
		if (activity instanceof UserProfileActivity) {
			mUser = ((UserProfileActivity) activity).mUser;
			mTag = GIFTS_PROFILE_TAG;
			setGifts(Gift.parse(mUser));
		} else if (activity instanceof GiftsActivity) {
			mTag = GIFTS_ALL_TAG;
		}
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_grid, null);

		mGalleryManager = new GiftGalleryManager<Gift>(this.getActivity().getApplicationContext(),
				mGifts);
		mGridAdapter = new GiftsAdapter(this.getActivity().getApplicationContext(), mGalleryManager);

		mGridView = (GridView) view.findViewById(R.id.fragmentGrid);
		mGridView.setAnimationCacheEnabled(false);
		mGridView.setScrollingCacheEnabled(false);		
		

		int columns = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? GIFTS_COLUMN_PORTRAIT
				: GIFTS_COLUMN_LANDSCAPE;

		mGridView.setNumColumns(columns);

		if (mTag == GIFTS_ALL_TAG) {
			mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent intent = getActivity().getIntent();
					if (view.getTag() instanceof ViewHolder) {
						ViewHolder holder = ((ViewHolder) view.getTag());
						if (holder.mGift.type != Gift.PROFILE && holder.mGift.type != Gift.SEND_BTN) {
							intent.putExtra(GiftsActivity.INTENT_GIFT_ID, holder.mGift.id);
							intent.putExtra(GiftsActivity.INTENT_GIFT_URL, holder.mGift.link);

							getActivity().setResult(Activity.RESULT_OK, intent);
							getActivity().finish();
						}
					}
				}
			});
		} else if (mTag == GIFTS_PROFILE_TAG) {
			mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (view.getTag() instanceof ViewHolder) {
						ViewHolder holder = ((ViewHolder) view.getTag());
						if (holder.mGift.type == Gift.SEND_BTN) {
							Intent intent = new Intent(getActivity().getApplicationContext(),
									GiftsActivity.class);
							startActivityForResult(intent, GiftsActivity.INTENT_REQUEST_GIFT);
						}
					}
				}
			});
		}

		mGridView.setAdapter(mGridAdapter);
		return view;
	}

	@Override
	public void onResume() {
		// if (mUser != null) {
		// setGifts(Gift.parse(mUser));
		// }
		// update();

		super.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == GiftsActivity.INTENT_REQUEST_GIFT) {
				Bundle extras = data.getExtras();
				final int id = extras.getInt(GiftsActivity.INTENT_GIFT_ID);
				final String url = extras.getString(GiftsActivity.INTENT_GIFT_URL);

				if (mUser != null) {
					SendGiftRequest sendGift = new SendGiftRequest(getActivity()
							.getApplicationContext());
					sendGift.giftId = id;
					sendGift.userId = mUser.uid;
					final Gift sendedGift = new Gift();
					sendedGift.id = sendGift.giftId;
					sendedGift.link = url;
					sendedGift.type = Gift.PROFILE_NEW;
					sendGift.callback(new ApiHandler() {
						@Override
						public void success(ApiResponse response) throws NullPointerException {
							SendGiftAnswer answer = SendGiftAnswer.parse(response);
							CacheProfile.power = answer.power;
							CacheProfile.money = answer.money;
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (mGifts.size() > 1) {
										mGifts.add(1, sendedGift);										
									} else {
										mGifts.addLast(sendedGift);										
									}									
									
									update();									
								}
							});
						}

						@Override
						public void fail(int codeError, final ApiResponse response)
								throws NullPointerException {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (response.code == ApiResponse.PAYMENT)
										startActivity(new Intent(getActivity()
												.getApplicationContext(), BuyingActivity.class));
								}
							});
						}
					}).exec();
				}
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Call if Data.giftList changed
	 */
	public void update() {
		mGridAdapter.notifyDataSetChanged();
		mGalleryManager.update();
	}

	public void setGifts(LinkedList<Gift> gifts) {
		mGifts.clear();
		mGifts.addAll(gifts);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mGridAdapter.release();
	}
}
