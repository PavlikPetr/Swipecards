package com.topface.topface.ui.fragments;

import java.util.LinkedList;

import com.topface.topface.R;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.FeedGifts;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Profile;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.data.User;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedGiftsRequest;
import com.topface.topface.requests.SendGiftRequest;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.GiftsAdapter;
import com.topface.topface.ui.adapters.GiftsAdapter.ViewHolder;
import com.topface.topface.ui.adapters.IListLoader.ItemType;
import com.topface.topface.ui.profile.UserProfileActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.GiftGalleryManager;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

public class GiftsFragment extends BaseFragment {
	// Data
	private String mTag;

	public static final String GIFTS_ALL_TAG = "giftsGridAll";
	public static final String GIFTS_PROFILE_TAG = "giftsGridProfile";
	public static final int GIFTS_COLUMN_PORTRAIT = 3;
	public static final int GIFTS_COLUMN_LANDSCAPE = 5;

	// Grid elements
	private GiftsAdapter mGridAdapter;
	private GiftGalleryManager<Gift> mGalleryManager;
	private GridView mGridView;

	private Profile mProfile;
	private LinkedList<Gift> mGifts = new LinkedList<Gift>();

	// TODO Data giftsList remove
	
	@Override
	public void onAttach(Activity activity) {
        if (activity instanceof UserProfileActivity) {
            User userData;
            mProfile = userData = ((UserProfileActivity) activity).mUser;
            mTag = GIFTS_PROFILE_TAG;
//            if(mProfile instanceof User)
//              setGifts(Gift.parse(((User)mProfile).gifts));
            setGifts(userData.gifts);
            mGalleryManager = new GiftGalleryManager<Gift>(activity.getApplicationContext(), mGifts,
                    new Handler(){
                        @Override
                        public void handleMessage(Message msg) {
                            if (mGifts.getLast().isLoader()){
                                onNewFeeds();
                            };
                        }
                    });         
        } else if (activity instanceof NavigationActivity) {
            mProfile = CacheProfile.getProfile();
            mTag = GIFTS_ALL_TAG;
            mGalleryManager = new GiftGalleryManager<Gift>(getActivity().getApplicationContext(), mGifts, null);
        } else {
            mTag = GIFTS_ALL_TAG;
            mGalleryManager = new GiftGalleryManager<Gift>(getActivity().getApplicationContext(), mGifts, null);
        }
                
        mGridAdapter = new GiftsAdapter(activity.getApplicationContext(), mGalleryManager);     
        super.onAttach(activity);
	}

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_grid, null);		

		mGridView = (GridView) view.findViewById(R.id.fragmentGrid);
		mGridView.setAnimationCacheEnabled(false);
		mGridView.setScrollingCacheEnabled(true);		

		int columns = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? GIFTS_COLUMN_PORTRAIT
				: GIFTS_COLUMN_LANDSCAPE;

		mGridView.setNumColumns(columns);

		if (mTag == GIFTS_ALL_TAG) {
			((TextView)view.findViewById(R.id.fragmentTitle)).setVisibility(View.GONE);
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
			if (mProfile != null && mGifts.size() == 0) {
	            ((TextView)view.findViewById(R.id.fragmentTitle)).setText(R.string.gifts);
			    ((TextView)view.findViewById(R.id.fragmentTitle)).setVisibility(View.VISIBLE);
			     onNewFeeds();
			}
		} else if (mTag == GIFTS_PROFILE_TAG) {
			((TextView)view.findViewById(R.id.fragmentTitle)).setText(R.string.gifts);
			((TextView)view.findViewById(R.id.fragmentTitle)).setVisibility(View.VISIBLE);
			mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {					
					if (view.getTag() instanceof ViewHolder) {
						ViewHolder holder = ((ViewHolder) view.getTag());
						if (holder.mGift != null) {
							if (holder.mGift.type == Gift.SEND_BTN) {
								Intent intent = new Intent(getActivity().getApplicationContext(),
										GiftsActivity.class);
								startActivityForResult(intent, GiftsActivity.INTENT_REQUEST_GIFT);
							}
						}
					} 
					
					if (mGifts.get(position).isLoaderRetry()) {
						updateUI(new Runnable() {
							public void run() {
								updateUI(new Runnable() {
									
									@Override
									public void run() {
										removeLoaderItem();
										mGifts.add(new Gift(ItemType.LOADER));
										update();
									}
								});
								onNewFeeds();
							}
						});					
					}
				}
			});
		}

		mGridView.setAdapter(mGridAdapter);		
		mGridView.setOnScrollListener(mGalleryManager);
		
		return view;
	}	
    
    @Override
    public void onStart() {
        super.onStart();
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == GiftsActivity.INTENT_REQUEST_GIFT) {
				Bundle extras = data.getExtras();
				final int id = extras.getInt(GiftsActivity.INTENT_GIFT_ID);
				final String url = extras.getString(GiftsActivity.INTENT_GIFT_URL);

				if (mProfile != null) {
					SendGiftRequest sendGift = new SendGiftRequest(getActivity()
							.getApplicationContext());
					registerRequest(sendGift);
					sendGift.giftId = id;
					sendGift.userId = mProfile.uid;
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
									mGalleryManager.update();
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

	private void removeLoaderItem() {
		if (mGifts.size() > 0) {
			if (mGifts.getLast().isLoader() || mGifts.getLast().isLoaderRetry()) {
				mGifts.remove(mGifts.size() - 1);
			}
		}
	}

	private void onNewFeeds() {
	    onNewFeeds(mProfile.uid);
	}
	
	private void onNewFeeds(int userId) {
		FeedGiftsRequest request = new FeedGiftsRequest(getActivity().getApplicationContext());		
		request.limit = UserProfileActivity.GIFTS_LOAD_COUNT;
		request.uid = userId;					
		if (!mGifts.isEmpty()) {
			if (mGifts.getLast().isLoader() || mGifts.getLast().isLoaderRetry()) {
				request.from = mGifts.get(mGifts.size() - 2).feedId;
			} else {
				request.from = mGifts.get(mGifts.size() - 1).feedId;							
			}
		}
		
		request.callback(new ApiHandler() {
			
			@Override
			public void success(ApiResponse response) throws NullPointerException {
			
				final LinkedList<FeedGifts> feedGifts = FeedGifts.parse(response);														
				
				updateUI(new Runnable() {
					
					@Override
					public void run() {									
						removeLoaderItem();
						for (FeedGifts feed : feedGifts) {
							mGifts.add(feed.gift);
						}
						
						if (FeedGifts.more) {
							mGifts.add(new Gift(ItemType.LOADER));
						}
						update();
					}
				});
			}
			
			@Override
			public void fail(int codeError, ApiResponse response) throws NullPointerException {							
				updateUI(new Runnable() {
					@Override
					public void run() {									
						removeLoaderItem();
						mGifts.add(new Gift(ItemType.RETRY));
						update();
					}
				});
			}
		}).exec();
	}
	
	/**
	 * Call if Data.giftList changed
	 */
	public void update() {
		mGridAdapter.notifyDataSetChanged();
		if (mTag == GIFTS_ALL_TAG)
			mGalleryManager.update();
	}

	public void setGifts(LinkedList<Gift> gifts) {
		mGifts.clear();
		mGifts.addAll(gifts);
		if (mTag == GIFTS_PROFILE_TAG) {
			if (mGifts.size() >= UserProfileActivity.GIFTS_LOAD_COUNT)
				mGifts.add(new Gift(ItemType.LOADER));
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mGridAdapter.release();
	}

	@Override
	public void fillLayout() {	}

	@Override
	public void clearLayout() { }
}
