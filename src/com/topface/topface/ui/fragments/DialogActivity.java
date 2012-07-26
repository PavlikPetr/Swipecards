package com.topface.topface.ui.fragments;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.Data;
import com.topface.topface.data.Dialog;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.DialogRequest;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.adapters.DialogListAdapter;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.ui.adapters.IListLoader.ItemType;
import com.topface.topface.ui.p2r.PullToRefreshBase.OnRefreshListener;
import com.topface.topface.ui.p2r.PullToRefreshListView;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class DialogActivity extends BaseFragment {
    // Data
    private boolean mNewUpdating;
//    private TextView mFooterView;
    private PullToRefreshListView mListView;
    private DialogListAdapter mListAdapter;
    private AvatarManager<Dialog> mAvatarManager;
    private DoubleBigButton mDoubleButton;
    private ProgressBar mProgressBar;
    private ImageView mBannerView;
    private boolean mIsUpdating = false;
    // Constants
    private static final int LIMIT = 40;
    //---------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View view = inflater.inflate(R.layout.ac_dialog, null);
        Debug.log(this, "+onCreate");

        // Data
        Data.dialogList = new LinkedList<Dialog>();

        // Progress
        mProgressBar = (ProgressBar)view.findViewById(R.id.prsInboxLoading);

        // Banner
        mBannerView = (ImageView)view.findViewById(R.id.ivBanner);

        // Double Button
        mDoubleButton = (DoubleBigButton)view.findViewById(R.id.btnDoubleBig);
        mDoubleButton.setLeftText(getString(R.string.inbox_btn_dbl_left));
        mDoubleButton.setRightText(getString(R.string.inbox_btn_dbl_right));
        mDoubleButton.setChecked(DoubleBigButton.LEFT_BUTTON);
        mDoubleButton.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewUpdating = false;
                updateData(false);
            }
        });
        mDoubleButton.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewUpdating = true;
                updateData(false);
            }
        });

        // ListView
        mListView = (PullToRefreshListView)view.findViewById(R.id.lvInboxList);
        mListView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateData(true);
            }
        });
        mListView.getRefreshableView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,View view,int position,long id) {
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
//	                ImageView iv = (ImageView)view.findViewById(R.id.ivAvatar);
//	                Data.userAvatar = ((BitmapDrawable)iv.getDrawable()).getBitmap();
	                try {
	                    Intent intent = new Intent(getActivity(), ChatActivity.class);
	                    intent.putExtra(ChatActivity.INTENT_USER_ID, Data.dialogList.get(position).uid);
	                    intent.putExtra(ChatActivity.INTENT_USER_URL, Data.dialogList.get(position).getSmallLink());
	                    intent.putExtra(ChatActivity.INTENT_USER_NAME, Data.dialogList.get(position).first_name);
	                    startActivity(intent);
	                } catch(Exception e) {
	                    Debug.log(DialogActivity.this, "start ChatActivity exception:" + e.toString());
	                }
            	}
            }
        });

        // Footer
//        mFooterView = new TextView(getApplicationContext());
//        mFooterView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                updateDataHistory();
//            }
//        });
//        mFooterView.setBackgroundResource(R.drawable.item_list_selector);
//        mFooterView.setText(getString(R.string.general_footer_previous));
//        mFooterView.setTextColor(Color.DKGRAY);
//        mFooterView.setGravity(Gravity.CENTER);
//        mFooterView.setTypeface(Typeface.DEFAULT_BOLD);
//        mFooterView.setVisibility(View.GONE);
//        mListView.getRefreshableView().addFooterView(mFooterView);

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

        mNewUpdating = CacheProfile.unread_messages > 0 ? true : false;
        CacheProfile.unread_messages = 0;
        return view;
    }
    //---------------------------------------------------------------------------
    private void updateData(boolean isPushUpdating) {
    	mIsUpdating = true;
        if (!isPushUpdating)
            mProgressBar.setVisibility(View.VISIBLE);

        mDoubleButton.setChecked(mNewUpdating ? DoubleBigButton.RIGHT_BUTTON : DoubleBigButton.LEFT_BUTTON);

        DialogRequest dialogRequest = new DialogRequest(getActivity());
        dialogRequest.limit = LIMIT;
        dialogRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                Data.dialogList.clear();
                Data.dialogList.addAll(Dialog.parse(response));
                updateUI(new Runnable() {
                    @Override
                    public void run() {
//                        if (mNewUpdating)
//                            mFooterView.setVisibility(View.GONE);
//                        else
//                            mFooterView.setVisibility(View.VISIBLE);
//
//                        if (Data.dialogList.size() == 0 || Data.dialogList.size() < LIMIT / 2)
//                            mFooterView.setVisibility(View.GONE);

                    	if (!(Data.dialogList.size() == 0 || Data.dialogList.size() < LIMIT / 2)) {
                     			Data.dialogList.add(new Dialog(IListLoader.ItemType.LOADER));
                     	}
                     	
                    	
                    	mProgressBar.setVisibility(View.GONE);
                        mListView.onRefreshComplete();
                        mListAdapter.notifyDataSetChanged();
                        mListView.setVisibility(View.VISIBLE);
                        mIsUpdating = false;
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                    	Toast.makeText(getActivity(), getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                        mListView.onRefreshComplete();
                        mListView.setVisibility(View.VISIBLE);
                        mIsUpdating = false;
                    }
                });
            }
        }).exec();
    }
    //---------------------------------------------------------------------------
    private void updateDataHistory() {
    	mIsUpdating = true;
    	mNewUpdating = mDoubleButton.isRightButtonChecked();

        DialogRequest dialogRequest = new DialogRequest(getActivity());
        dialogRequest.limit = LIMIT;        
        if (!mNewUpdating) {
        	if (Data.dialogList.getLast().isLoader() || Data.dialogList.getLast().isLoaderRetry()) {
        		dialogRequest.before = Data.dialogList.get(Data.dialogList.size() - 2).id;
        	} else {
        		dialogRequest.before = Data.dialogList.get(Data.dialogList.size() - 1).id;
        	}        	
        }
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
                            if (!(Data.dialogList.size() == 0 || Data.dialogList.size() < (LIMIT/2)))
                            	Data.dialogList.add(new Dialog(IListLoader.ItemType.LOADER));
                        }
                    	
                        mProgressBar.setVisibility(View.GONE);
                        mListView.onRefreshComplete();
                        mListAdapter.notifyDataSetChanged();
                        mIsUpdating = false;
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                    	mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();                        
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
    //---------------------------------------------------------------------------
    // FrameActivity
    //---------------------------------------------------------------------------
    @Override
    public void clearLayout() {
        Debug.log(this, "DialogActivity::clearLayout");
        mListView.setVisibility(View.INVISIBLE);
    }
    //---------------------------------------------------------------------------
    @Override
    public void fillLayout() {
        Debug.log(this, "DialogActivity::fillLayout");

        updateBanner(mBannerView, BannerRequest.INBOX);
        updateData(false);
    }
    //---------------------------------------------------------------------------
    @Override
    public void release() {
        mListView = null;

        if (mListAdapter != null)
            mListAdapter.release();
        mListAdapter = null;

        if (mAvatarManager != null)
            mAvatarManager.release();
        mAvatarManager = null;

        Data.userAvatar = null;
    }
    //---------------------------------------------------------------------------
    private void removeLoaderListItem() {
    	if (Data.dialogList.size() > 0 ) {
	    	if (Data.dialogList.getLast().isLoader() || Data.dialogList.getLast().isLoaderRetry()) {
	    		Data.dialogList.remove(Data.dialogList.size() - 1);
	    	}
    	}
    }
}
