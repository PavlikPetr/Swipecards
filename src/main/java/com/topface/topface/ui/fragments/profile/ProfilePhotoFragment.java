package com.topface.topface.ui.fragments.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.AlbumPhotos;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PhotoDeleteRequest;
import com.topface.topface.requests.PhotoMainRequest;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ProfilePhotoFragment extends ProfileInnerFragment {

    private static final String PHOTOS = "PHOTOS";
    private static final String POSITION = "POSITION";
    private static final String FLIPPER_VISIBLE_CHILD = "FLIPPER_VISIBLE_CHILD";
    private static final String NEED_MORE = "NEED_MORE";

    private ProfilePhotoGridAdapter mProfilePhotoGridAdapter;
    private boolean mMore;

    private ViewFlipper mViewFlipper;
    private GridView mGridAlbum;
    private View mLoadingLocker;
    private TextView mTitle;
    private BroadcastReceiver mPhotosReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Photo> arrList = intent.getParcelableArrayListExtra(PhotoSwitcherActivity.INTENT_PHOTOS);
            boolean clear = intent.getBooleanExtra(PhotoSwitcherActivity.INTENT_CLEAR, false);
            Photos newPhotos = new Photos();

            newPhotos.addAll(arrList);
            if (clear) {
                newPhotos.addFirst(new Photo());
                ((ProfilePhotoGridAdapter) mGridAlbum.getAdapter()).setData(newPhotos, intent.getBooleanExtra(PhotoSwitcherActivity.INTENT_MORE, false));
            } else {
                ((ProfilePhotoGridAdapter) mGridAlbum.getAdapter()).addData(newPhotos, intent.getBooleanExtra(PhotoSwitcherActivity.INTENT_MORE, false));
            }
            initTitleText(mTitle);
        }
    };
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                mViewFlipper.setDisplayedChild(1);
                return;
            }
            Intent intent = new Intent(getActivity().getApplicationContext(), PhotoSwitcherActivity.class);
            intent.putExtra(PhotoSwitcherActivity.INTENT_USER_ID, CacheProfile.uid);
            intent.putExtra(PhotoSwitcherActivity.INTENT_ALBUM_POS, position);
            intent.putParcelableArrayListExtra(PhotoSwitcherActivity.INTENT_PHOTOS, ((ProfileGridAdapter) mGridAlbum.getAdapter()).getData());
            startActivity(intent);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProfilePhotoGridAdapter = new ProfilePhotoGridAdapter(getActivity().getApplicationContext(), getPhotoLinks(), CacheProfile.totalPhotos, new LoadingListAdapter.Updater() {
            @Override
            public void onUpdate() {
                sendAlbumRequest();
            }
        });

    }

    private void sendAlbumRequest() {
        Photos photoLinks = mProfilePhotoGridAdapter.getData();
        if (photoLinks == null || photoLinks.size() < 2) {
            return;
        }
        Photo photo = mProfilePhotoGridAdapter.getLastItem();
        if (photo.isFake()) return;
        int position = photo.getPosition();
        AlbumRequest request = new AlbumRequest(
                getActivity(),
                CacheProfile.uid,
                AlbumRequest.DEFAULT_PHOTOS_LIMIT,
                position + 1,
                AlbumRequest.MODE_ALBUM
        );
        request.callback(new DataApiHandler<AlbumPhotos>() {

            @Override
            protected void success(AlbumPhotos data, IApiResponse response) {
                if (mProfilePhotoGridAdapter != null) {
                    mProfilePhotoGridAdapter.addPhotos(data, data.more, false);
                    mMore = data.more;
                }
            }

            @Override
            protected AlbumPhotos parseResponse(ApiResponse response) {
                return new AlbumPhotos(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Utils.showErrorMessage();
            }
        }).exec();
    }

    private Photos getPhotoLinks() {
        Photos photoLinks = new Photos();
        photoLinks.clear();
        photoLinks.add(new Photo());
        if (CacheProfile.photos != null) {
            photoLinks.addAll(CacheProfile.photos);
        }
        return photoLinks;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_profile_photos, container, false);

        //Navigation bar

        if (getActivity() instanceof EditContainerActivity) {
            getActivity().setResult(Activity.RESULT_OK);
            setActionBarTitles(getString(R.string.edit_title), getString(R.string.edit_album));
        }

        mLoadingLocker = root.findViewById(R.id.fppLocker);

        mViewFlipper = (ViewFlipper) root.findViewById(R.id.vfFlipper);

        int position = 0;
        if (savedInstanceState != null) {
            try {
                mProfilePhotoGridAdapter.setData(new Photos(new JSONArray(savedInstanceState.getString(PHOTOS))),
                        savedInstanceState.getBoolean(NEED_MORE));
            } catch (JSONException e) {
                Debug.error(e);
            }
            position = savedInstanceState.getInt(POSITION, 0);
            mViewFlipper.setDisplayedChild(savedInstanceState.getInt(FLIPPER_VISIBLE_CHILD, 0));
        }

        mGridAlbum = (GridView) root.findViewById(R.id.usedGrid);
        mGridAlbum.setAdapter(mProfilePhotoGridAdapter);
        mGridAlbum.setSelection(position);
        mGridAlbum.setOnItemClickListener(mOnItemClickListener);
        mGridAlbum.setOnScrollListener(mProfilePhotoGridAdapter);
        mGridAlbum.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Photo item = (Photo) parent.getItemAtPosition(position);
                if (needDialog(item)) {
                    startPhotoDialog(item);
                    return true;
                }
                return false;
            }
        });

        mTitle = (TextView) root.findViewById(R.id.usedTitle);

        initTitleText(mTitle);

        root.findViewById(R.id.btnAddPhotoAlbum).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(AbstractProfileFragment.ADD_PHOTO_INTENT).putExtra("btn_id", R.id.btnAddPhotoAlbum));
            }
        });
        root.findViewById(R.id.btnAddPhotoCamera).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(AbstractProfileFragment.ADD_PHOTO_INTENT).putExtra("btn_id", R.id.btnAddPhotoCamera));
            }
        });
        root.findViewById(R.id.btnCancel).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mViewFlipper.setDisplayedChild(0);
            }
        });

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mPhotosReceiver, new IntentFilter(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT));

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            outState.putString(PHOTOS, mProfilePhotoGridAdapter.getData().toJson().toString());
        } catch (JSONException e) {
            Debug.error(e);
        }
        outState.putInt(POSITION, mGridAlbum.getFirstVisiblePosition());
        outState.putInt(FLIPPER_VISIBLE_CHILD, mViewFlipper.getDisplayedChild());
        outState.putBoolean(NEED_MORE, mMore);
    }

    public void startPhotoDialog(final Photo photo) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(new String[]{getString(R.string.edit_set_as_main), getString(R.string.edit_delete)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mLoadingLocker.setVisibility(View.VISIBLE);
                switch (which) {
                    case 0:
                        PhotoMainRequest request = new PhotoMainRequest(getActivity());
                        request.photoId = photo.getId();
                        request.callback(new SimpleApiHandler() {
                            @Override
                            public void success(IApiResponse response) {
                                super.success(response);
                                CacheProfile.photo = photo;
                                CacheProfile.sendUpdateProfileBroadcast();
                            }

                            @Override
                            public void always(IApiResponse response) {
                                super.always(response);
                                mLoadingLocker.setVisibility(View.GONE);
                            }
                        }).exec();
                        break;
                    case 1:
                        PhotoDeleteRequest deleteRequest = new PhotoDeleteRequest(getActivity());
                        deleteRequest.photos = new int[]{photo.getId()};
                        deleteRequest.callback(new SimpleApiHandler() {
                            @Override
                            public void success(IApiResponse response) {
                                super.success(response);
                                CacheProfile.photos.remove(photo);
                                Intent intent = new Intent(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT);
                                Photos newPhotos = new Photos();
                                // TODO перенести логику в адаптер
                                for (int i = 0; i < CacheProfile.photos.size(); i++) {
                                    newPhotos.add(i, CacheProfile.photos.get(i));
                                }
                                intent.putExtra(PhotoSwitcherActivity.INTENT_CLEAR, true);
                                intent.putExtra(PhotoSwitcherActivity.INTENT_PHOTOS, newPhotos);
                                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                            }

                            @Override
                            public void always(IApiResponse response) {
                                super.always(response);
                                mLoadingLocker.setVisibility(View.GONE);
                            }
                        }).exec();
                        break;
                }
            }
        });
        builder.create().show();
    }

    private boolean needDialog(Photo photo) {
        return CacheProfile.photo != null && photo != null && CacheProfile.photo.getId() != photo.getId();
    }

    private void initTitleText(TextView title) {
        if (title != null) {
            title.setVisibility(View.VISIBLE);
            int size = mProfilePhotoGridAdapter.getCount();
            if (size > 1) {
                title.setText(Utils.formatPhotoQuantity(size - 1));
                return;
            }
            title.setText(R.string.upload_photos);
        }
    }

    @Override
    public void onResume() {
        getPhotoLinks();
        mProfilePhotoGridAdapter.updateData();
        mProfilePhotoGridAdapter.notifyDataSetChanged();
        initTitleText(mTitle);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            mViewFlipper.setDisplayedChild(0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mPhotosReceiver);
    }
}
 