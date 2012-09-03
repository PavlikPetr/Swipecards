package com.topface.topface.ui.fragments;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.Data;
import com.topface.topface.Recycle;
import com.topface.topface.Static;
import com.topface.topface.data.Dialog;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.DialogRequest;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.DialogListAdapter;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.ui.adapters.IListLoader.ItemType;
import com.topface.topface.ui.p2r.PullToRefreshBase.OnRefreshListener;
import com.topface.topface.ui.p2r.PullToRefreshListView;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.SwapAnimation;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DialogsFragment extends BaseFragment {
	private boolean mHasUnread;
	// private TextView mFooterView;
	private PullToRefreshListView mListView;
	private DialogListAdapter mListAdapter;
	private AvatarManager<Dialog> mAvatarManager;
	private DoubleBigButton mDoubleButton;	
	private TextView mBackgroundText;
	private ImageView mBannerView;
    
    private View mToolsBar;
    private View mShowToolsBarButton;
    private View mControlsGroup;
    
	private boolean mIsUpdating = false;
	// Constants
	private static final int LIMIT = 40;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
		super.onCreateView(inflater, container, saved);
		View view = inflater.inflate(R.layout.ac_dialog, null);

		// Data
		Data.dialogList = new LinkedList<Dialog>();
		
        // Home Button
        (view.findViewById(R.id.btnNavigationHome)).setOnClickListener((NavigationActivity)getActivity());   
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(getResources().getString(R.string.dashbrd_btn_chat));

        mControlsGroup = view.findViewById(R.id.loControlsGroup);
        mToolsBar = view.findViewById(R.id.loToolsBar);
        mShowToolsBarButton = view.findViewById(R.id.btnNavigationFilterBar);
        mShowToolsBarButton.setVisibility(View.VISIBLE);
        mShowToolsBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mControlsGroup.startAnimation(new SwapAnimation(mControlsGroup, R.id.loToolsBar));
            }
        });
        
        ViewTreeObserver vto = mToolsBar.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int y = -mToolsBar.getMeasuredHeight() + Static.HEADER_SHADOW_SHIFT;
                mControlsGroup.setPadding(mControlsGroup.getPaddingLeft(), y, mControlsGroup.getPaddingRight(), mControlsGroup.getPaddingBottom());
                if(y>0 || y<0) {
                    ViewTreeObserver obs = mControlsGroup.getViewTreeObserver();
                    obs.removeGlobalOnLayoutListener(this);                    
                }
            }
        });

		// ListView background
		mBackgroundText = (TextView) view.findViewById(R.id.tvBackgroundText);

		// Banner
		mBannerView = (ImageView) view.findViewById(R.id.ivBanner);

		// Double Button
		mDoubleButton = (DoubleBigButton) view.findViewById(R.id.btnDoubleBig);
		mDoubleButton.setLeftText(getString(R.string.inbox_btn_dbl_left));
		mDoubleButton.setRightText(getString(R.string.inbox_btn_dbl_right));
		mDoubleButton.setChecked(DoubleBigButton.LEFT_BUTTON);
		mDoubleButton.setLeftListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mHasUnread = false;
				updateData(false);
			}
		});
		mDoubleButton.setRightListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mHasUnread = true;
				updateData(false);
			}
		});

		// ListView
		mListView = (PullToRefreshListView) view.findViewById(R.id.lvInboxList);
		mListView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				updateData(true);
			}
		});
		mListView.getRefreshableView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (!mIsUpdating && Data.dialogList.get(position).isLoaderRetry()) {
					updateUI(new Runnable() {
						public void run() {
							removeLoaderListItem();
							Data.dialogList.add(new Dialog(ItemType.LOADER));
							mListAdapter.notifyDataSetChanged();
						}
					});
					updateDataHistory();
				} else {
					// ImageView iv =
					// (ImageView)view.findViewById(R.id.ivAvatar);
					// Data.userAvatar =
					// ((BitmapDrawable)iv.getDrawable()).getBitmap();
					try {
						Intent intent = new Intent(getActivity(), ChatActivity.class);
						intent.putExtra(ChatActivity.INTENT_USER_ID, Data.dialogList.get(position).uid);
						intent.putExtra(ChatActivity.INTENT_USER_URL, Data.dialogList.get(position).getSmallLink());
						intent.putExtra(ChatActivity.INTENT_USER_NAME, Data.dialogList.get(position).first_name);
						intent.putExtra(ChatActivity.INTENT_USER_AGE, Data.dialogList.get(position).age);
						intent.putExtra(ChatActivity.INTENT_USER_CITY, Data.dialogList.get(position).city_name);
						startActivity(intent);
					} catch (Exception e) {
						Debug.log(DialogsFragment.this,
								"start ChatActivity exception:" + e.toString());
					}
				}
			}
		});

		// Footer
		// mFooterView = new TextView(getApplicationContext());
		// mFooterView.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// updateDataHistory();
		// }
		// });
		// mFooterView.setBackgroundResource(R.drawable.item_list_selector);
		// mFooterView.setText(getString(R.string.general_footer_previous));
		// mFooterView.setTextColor(Color.DKGRAY);
		// mFooterView.setGravity(Gravity.CENTER);
		// mFooterView.setTypeface(Typeface.DEFAULT_BOLD);
		// mFooterView.setVisibility(View.GONE);
		// mListView.getRefreshableView().addFooterView(mFooterView);

		// Control creating
		mAvatarManager = new AvatarManager<Dialog>(getActivity(), Data.dialogList, new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (Data.dialogList.getLast().isLoader() && !mIsUpdating)
					updateDataHistory();

				super.handleMessage(msg);
			}
		});
		mListAdapter = new DialogListAdapter(getActivity(), mAvatarManager);
		mListView.setOnScrollListener(mAvatarManager);
		mListView.setAdapter(mListAdapter);

		mHasUnread = CacheProfile.unread_messages > 0 ? true : false;

		return view;
	}

	// ---------------------------------------------------------------------------
	private void updateData(final boolean isPushUpdating) {
		mIsUpdating = true;
		if (!isPushUpdating)
			onUpdateStart(isPushUpdating);

		mDoubleButton.setChecked(mHasUnread ? DoubleBigButton.RIGHT_BUTTON
				: DoubleBigButton.LEFT_BUTTON);
		
		DialogRequest dialogRequest = new DialogRequest(getActivity());
		registerRequest(dialogRequest);
		dialogRequest.limit = LIMIT;
		if (mHasUnread) 
			dialogRequest.unread = 1;
		else
			dialogRequest.unread = 0;
		dialogRequest.callback(new ApiHandler() {
			@Override
			public void success(ApiResponse response) {
				Data.dialogList.clear();
				Data.dialogList.addAll(Dialog.parse(response));
				updateUI(new Runnable() {
					@Override
					public void run() {
					    CacheProfile.unread_messages = 0;
						// if (mNewUpdating)
						// mFooterView.setVisibility(View.GONE);
						// else
						// mFooterView.setVisibility(View.VISIBLE);
						//
						// if (Data.dialogList.size() == 0 ||
						// Data.dialogList.size() < LIMIT / 2)
						// mFooterView.setVisibility(View.GONE);

						if (!(Data.dialogList.size() == 0 || Data.dialogList.size() < LIMIT / 2)) {
							Data.dialogList.add(new Dialog(IListLoader.ItemType.LOADER));
						}

						onUpdateSuccess(isPushUpdating);
						mListView.onRefreshComplete();
						mListAdapter.notifyDataSetChanged();
						mListView.setVisibility(View.VISIBLE);
						mIsUpdating = false;
					}
				});
			}

			@Override
			public void fail(int codeError, ApiResponse response) {
				updateUI(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity(), getString(R.string.general_data_error),
								Toast.LENGTH_SHORT).show();
						onUpdateFail(isPushUpdating);
						mListView.onRefreshComplete();
						mListView.setVisibility(View.VISIBLE);
						mIsUpdating = false;
					}
				});
			}
		}).exec();
	}

	// ---------------------------------------------------------------------------
	private void updateDataHistory() {
		mIsUpdating = true;
		mHasUnread = mDoubleButton.isRightButtonChecked();

		DialogRequest dialogRequest = new DialogRequest(getActivity());
		registerRequest(dialogRequest);
		dialogRequest.limit = LIMIT;
		if (!Data.dialogList.isEmpty()) {
			if (Data.dialogList.getLast().isLoader() || Data.dialogList.getLast().isLoaderRetry()) {
				dialogRequest.before = Data.dialogList.get(Data.dialogList.size() - 2).id;
			} else {
				dialogRequest.before = Data.dialogList.get(Data.dialogList.size() - 1).id;
			}
		}
		
		if(mHasUnread)
			dialogRequest.unread = 1;
		else
			dialogRequest.unread = 0;
		
		
		dialogRequest.callback(new ApiHandler() {
			@Override
			public void success(ApiResponse response) {
				final LinkedList<Dialog> dialogList = Dialog.parse(response);

				updateUI(new Runnable() {
					@Override
					public void run() {
						removeLoaderListItem();
						if (dialogList.size() > 0) {
							Data.dialogList.addAll(dialogList);
							if (!(Data.dialogList.size() == 0 || Data.dialogList.size() < (LIMIT / 2)))
								Data.dialogList.add(new Dialog(IListLoader.ItemType.LOADER));
						}

						onUpdateSuccess(true);
						mListView.onRefreshComplete();
						mListAdapter.notifyDataSetChanged();
						mIsUpdating = false;
					}
				});
			}

			@Override
			public void fail(int codeError, ApiResponse response) {
				updateUI(new Runnable() {
					@Override
					public void run() {
						onUpdateFail(true);
						Toast.makeText(getActivity(), getString(R.string.general_data_error),
								Toast.LENGTH_SHORT).show();
						mIsUpdating = false;
						removeLoaderListItem();
						Data.dialogList.add(new Dialog(IListLoader.ItemType.RETRY));
						mListView.onRefreshComplete();
						mListAdapter.notifyDataSetChanged();
					}
				});
			}
		}).exec();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mListView = null;

		if (mListAdapter != null)
			mListAdapter.release();
		mListAdapter = null;

		if (mAvatarManager != null)
			mAvatarManager.release();
		mAvatarManager = null;

		Data.friendAvatar = null;
	}

	private void removeLoaderListItem() {
		if (Data.dialogList.size() > 0) {
			if (Data.dialogList.getLast().isLoader() || Data.dialogList.getLast().isLoaderRetry()) {
				Data.dialogList.remove(Data.dialogList.size() - 1);
			}
		}
	}

	@Override
	public void clearLayout() {
		Debug.log(this, "DialogActivity::clearLayout");
		mListView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void fillLayout() {
		Debug.log(this, "DialogActivity::fillLayout");

		updateBanner(mBannerView, BannerRequest.INBOX);
		updateData(false);
	}

	@Override
	protected void onUpdateStart(boolean isPushUpdating) {
		// mLoadingLocker.setVisibility(View.VISIBLE);
		if (!isPushUpdating) {
			mListView.setVisibility(View.INVISIBLE);
			mBackgroundText.setText(R.string.general_dialog_loading);
			mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(Recycle.s_Loader,
					mBackgroundText.getCompoundDrawables()[1],
					mBackgroundText.getCompoundDrawables()[2],
					mBackgroundText.getCompoundDrawables()[3]);
			((AnimationDrawable) mBackgroundText.getCompoundDrawables()[0]).start();
			mDoubleButton.setClickable(false);
		}
	}

	@Override
	protected void onUpdateSuccess(boolean isPushUpdating) {
		if (!isPushUpdating) {
			mListView.setVisibility(View.VISIBLE);
			if (Data.dialogList.isEmpty()) {
				mBackgroundText.setText(R.string.inbox_background_text);
			} else {
				mBackgroundText.setText("");
			}
			
			if (mBackgroundText.getCompoundDrawables()[0] != null) {
				((AnimationDrawable)mBackgroundText.getCompoundDrawables()[0]).stop();
			}
			
			mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(null,
					mBackgroundText.getCompoundDrawables()[1],
					mBackgroundText.getCompoundDrawables()[2],
					mBackgroundText.getCompoundDrawables()[3]);
			mDoubleButton.setClickable(true);
		}
	}

	@Override
	protected void onUpdateFail(boolean isPushUpdating) {
		if (!isPushUpdating) {
			mListView.setVisibility(View.VISIBLE);
			mBackgroundText.setText("");
			
			if (mBackgroundText.getCompoundDrawables()[0] != null) {
				((AnimationDrawable)mBackgroundText.getCompoundDrawables()[0]).stop();
			}
			
			mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(null,
					mBackgroundText.getCompoundDrawables()[1],
					mBackgroundText.getCompoundDrawables()[2],
					mBackgroundText.getCompoundDrawables()[3]);
			mDoubleButton.setClickable(true);
		}
	}
}
