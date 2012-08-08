package com.topface.topface.ui;

import java.util.LinkedList;

import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.Gift;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.GiftsRequest;
import com.topface.topface.ui.fragments.GiftsFragment;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.ui.views.TripleButton;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GiftsActivity extends FragmentActivity {	

	public static final int INTENT_REQUEST_GIFT = 111;
	public static final String INTENT_GIFT_ID = "gift_id";
	public static final String INTENT_GIFT_URL = "gift_url";

	public GiftsCollection mGiftsCollection;
	private GiftsRequest mGiftRequest;

	private LockerView mLoadingLocker;
	private TripleButton mTripleButton;
	
	private GiftsFragment mGiftFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ac_gifts);

		((TextView) findViewById(R.id.tvHeaderTitle)).setText(R.string.gifts_title);
		((Button) findViewById(R.id.btnHeaderBack)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});

		mLoadingLocker = (LockerView) this.findViewById(R.id.llvGiftsLoading);

		mGiftFragment = new GiftsFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.giftGrid,
				mGiftFragment, GiftsFragment.GIFTS_ALL_TAG).commit();
		
		mGiftsCollection = new GiftsCollection();

		// init triple button
		mTripleButton = (TripleButton) findViewById(R.id.btnTriple);
		mTripleButton.setLeftText(Gift.getTypeNameResId(Gift.ROMANTIC));
		mTripleButton.setMiddleText(Gift.getTypeNameResId(Gift.FRIENDS));
		mTripleButton.setRightText(Gift.getTypeNameResId(Gift.PRESENT));

		mTripleButton.setChecked(TripleButton.LEFT_BUTTON);
		
		update();
	}

	/**
	 * Loading array of gifts from server
	 */
	private void update() {
		mLoadingLocker.setVisibility(View.VISIBLE);
		mGiftRequest = new GiftsRequest(getApplicationContext());
		mGiftRequest.callback(new ApiHandler() {
			@Override
			public void success(ApiResponse response) {
				Data.giftsList.addAll(Gift.parse(response));
				mGiftsCollection.add(Data.giftsList);		

				mGiftFragment.setGifts(mGiftsCollection.getGifts());
				
				mTripleButton.setLeftListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						v.post(new Runnable() {							
							@Override
							public void run() {																							
								mGiftFragment.setGifts(mGiftsCollection.getGifts(Gift.ROMANTIC));
								mGiftFragment.update();								
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
								mGiftFragment.setGifts(mGiftsCollection.getGifts(Gift.FRIENDS));
								mGiftFragment.update();
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
								mGiftFragment.setGifts(mGiftsCollection.getGifts(Gift.PRESENT));
								mGiftFragment.update();
							}
						});
					}
				});				
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {						
						mLoadingLocker.setVisibility(View.GONE);
					}
				});
			}

			@Override
			public void fail(int codeError, ApiResponse response) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getApplicationContext(),
								GiftsActivity.this.getString(R.string.general_data_error),
								Toast.LENGTH_SHORT).show();
						mLoadingLocker.setVisibility(View.GONE);
					}
				});
			}			
		}).exec();
	}

	/**
	 * Works with array of gifts, categorizes by type
	 */
	public static class GiftsCollection {
		public int currentType = Gift.ROMANTIC;
		private LinkedList<Gift> mAllGifts = new LinkedList<Gift>();

		public void add(LinkedList<Gift> gifts) {
			mAllGifts.addAll(gifts);
		}

		public LinkedList<Gift> getGifts(int type) {
			LinkedList<Gift> result = new LinkedList<Gift>();
			for (Gift gift : mAllGifts) {
				if (gift.type == type) {
					result.add(gift);
				}
			}

			return result;
		}

		public LinkedList<Gift> getGifts() {
			return getGifts(currentType);
		}

		public void setCurrentType(int type) {
			currentType = type;
		}
	}
}
