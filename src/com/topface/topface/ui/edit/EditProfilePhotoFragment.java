package com.topface.topface.ui.edit;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.MainRequest;
import com.topface.topface.ui.profile.AddPhotoHelper;
import com.topface.topface.ui.profile.ProfilePhotoGridAdapter;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;

public class EditProfilePhotoFragment extends AbstractEditFragment {

	private ArrayList<Photo> mDeleted = new ArrayList<Photo>();
	
    private ProfilePhotoGridAdapter mPhotoGridAdapter;
    private int mLastSelectedId;
    private int mSelectedId;

    private GridView mPhotoGridView;
    private Photos mPhotoLinks;
    
    private AddPhotoHelper mAddPhotoHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectedId = CacheProfile.photo.getId();
        mLastSelectedId = mSelectedId;
        
        mPhotoLinks = new Photos();
        mPhotoLinks.add(null);
        if (CacheProfile.photos != null) {
            mPhotoLinks.addAll(CacheProfile.photos);
        }
        mPhotoGridAdapter = new EditProfileGrigAdapter(
                getActivity().getApplicationContext(), mPhotoLinks);
        
        mAddPhotoHelper = new AddPhotoHelper(this);
        mAddPhotoHelper.setOnResultHandler(mHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_grid, container, false);

        // Navigation bar
        ((TextView) getActivity().findViewById(R.id.tvNavigationTitle)).setText(R.string.edit_title);
        TextView subTitle = (TextView) getActivity().findViewById(R.id.tvNavigationSubtitle);
        subTitle.setVisibility(View.VISIBLE);
        subTitle.setText(R.string.edit_album);

//        mSaveButton = (Button) getActivity().findViewById(R.id.btnNavigationRightWithText);
//        mSaveButton.setText(getResources().getString(R.string.navigation_save));
//        mSaveButton.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                saveChanges(null);
//            }
//        });
        mRightPrsBar = (ProgressBar) getActivity().findViewById(R.id.prsNavigationRight);

        getActivity().findViewById(R.id.btnNavigationHome).setVisibility(View.GONE);
        mBackButton = (Button) getActivity().findViewById(R.id.btnNavigationBackWithText);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setText(R.string.navigation_edit);
        mBackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mPhotoGridView = (GridView) root.findViewById(R.id.fragmentGrid);
        mPhotoGridView.setNumColumns(3);
        mPhotoGridView.setAdapter(mPhotoGridAdapter);
        mPhotoGridView.setOnItemClickListener(mOnItemClickListener);

        TextView title = (TextView) root.findViewById(R.id.fragmentTitle);
        title.setVisibility(View.INVISIBLE);
        

        return root;
    }

    @Override
    protected boolean hasChanges() {
        return mSelectedId != mLastSelectedId;
    }

    @Override
    protected void saveChanges(final Handler handler) {
    	if (hasChanges()) {
	        prepareRequestSend();
	        MainRequest request = new MainRequest(getActivity().getApplicationContext());
	        registerRequest(request);
	        request.photoid = mSelectedId;
	        request.callback(new ApiHandler() {
	
	            @Override
	            public void success(ApiResponse response) throws NullPointerException {
	                CacheProfile.photo = mPhotoLinks.getByPhotoId(mLastSelectedId);
	                getActivity().setResult(Activity.RESULT_OK);
	                mSelectedId = mLastSelectedId;                
	                finishRequestSend();
	                handler.sendEmptyMessage(0);
	            }
	
	            @Override
	            public void fail(int codeError, ApiResponse response) throws NullPointerException {
	                getActivity().setResult(Activity.RESULT_CANCELED);
	                finishRequestSend();
	                handler.sendEmptyMessage(0);
	            }
	        }).exec();
    	} else {
    		handler.sendEmptyMessage(0);
    	}
    }

    class EditProfileGrigAdapter extends ProfilePhotoGridAdapter {

        public EditProfileGrigAdapter(Context context,
                                      Photos photoLinks) {
            super(context, photoLinks);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            final Photo item = getItem(position);            

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_edit_user_gallery, null, false);
                holder = new ViewHolder();
                holder.photo = (ImageViewRemote) convertView.findViewById(R.id.ivPhoto);
                holder.mBtnSetAsMain = (Button) convertView.findViewById(R.id.btnSetAsMain);
                holder.mBtnDelete = (Button) convertView.findViewById(R.id.btnDeletePhoto);
                holder.mBtnRestore = (Button) convertView.findViewById(R.id.btnRestorePhoto);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            if (position == 0) {
                holder.photo.setBackgroundResource(R.drawable.profile_add_photo_selector);
                holder.mBtnSetAsMain.setVisibility(View.INVISIBLE);
                holder.mBtnDelete.setVisibility(View.INVISIBLE);
            } else {
            	final int itemId = item.getId();
                holder.photo.setPhoto(item);
                if (mDeleted.contains(item)) {
                	holder.mBtnDelete.setVisibility(View.INVISIBLE);
                	holder.mBtnSetAsMain.setVisibility(View.INVISIBLE);
                	holder.mBtnRestore.setVisibility(View.VISIBLE);
                	holder.mBtnRestore.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							mDeleted.remove(item);
							notifyDataSetChanged();
						}
					});
                } else {
                	holder.mBtnRestore.setVisibility(View.INVISIBLE);	                
	                
	                holder.mBtnDelete.setVisibility(View.VISIBLE);
                	holder.mBtnDelete.setOnClickListener(new OnClickListener() {						
						@Override
						public void onClick(View v) {
							if(itemId == mLastSelectedId) {
								mLastSelectedId = mSelectedId; 
							}
							mDeleted.add(item);
							notifyDataSetChanged();
						}
					});
                	
                	if (mLastSelectedId == itemId) {
	                	holder.mBtnSetAsMain.setVisibility(View.INVISIBLE);
	                } else {
	                	holder.mBtnSetAsMain.setVisibility(View.VISIBLE);
	                	holder.mBtnSetAsMain.setOnClickListener(new OnClickListener() {						
							@Override
							public void onClick(View v) {
								mLastSelectedId = itemId;
								notifyDataSetChanged();
							}
						});
	                }
                }
            }

            return convertView;
        }

        class ViewHolder {
            ImageViewRemote photo;
            Button mBtnDelete;
            Button mBtnSetAsMain;
            Button mBtnRestore;
        }
    }   

    @Override
    protected void lockUi() {
        mPhotoGridView.setEnabled(false);
    }

    @Override
    protected void unlockUi() {
        mPhotoGridView.setEnabled(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mAddPhotoHelper.checkActivityResult(requestCode, resultCode, data);
    }

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                mAddPhotoHelper.addPhoto();
                return;
            }
            Data.photos = CacheProfile.photos;            
        }
    };
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_OK) {
            	Photo photo = (Photo) msg.obj;
            	
            	CacheProfile.photos.addFirst(photo);            	
            	mPhotoLinks.add(1,photo);
            	            	
            	mPhotoGridAdapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), R.string.photo_add_or, Toast.LENGTH_SHORT).show();
            } else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR) {
                Toast.makeText(getActivity(), R.string.photo_add_error, Toast.LENGTH_SHORT).show();
            }
        }
    };

}
