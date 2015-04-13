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
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
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
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.GridViewWithHeaderAndFooter;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.ui.fragments.OwnAvatarFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ProfilePhotoFragment extends ProfileInnerFragment {

    private static final String PHOTOS = "PHOTOS";
    private static final String POSITION = "POSITION";
    private static final String FLIPPER_VISIBLE_CHILD = "FLIPPER_VISIBLE_CHILD";

    private OwnPhotoGridAdapter mProfilePhotoGridAdapter;

    private ViewFlipper mViewFlipper;
    private GridViewWithHeaderAndFooter mGridAlbum;
    private View mLoadingLocker;
    private View mGridFooterView;
    private BroadcastReceiver mProfileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isAdded() && getView() != null && CacheProfile.photos != null && mProfilePhotoGridAdapter != null) {
                mProfilePhotoGridAdapter.setData(
                        CacheProfile.photos,
                        CacheProfile.photos.size() < CacheProfile.totalPhotos
                );
            }
        }
    };
    private BroadcastReceiver mPhotosReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<Photo> arrList = intent.getParcelableArrayListExtra(PhotoSwitcherActivity.INTENT_PHOTOS);
            boolean clear = intent.getBooleanExtra(PhotoSwitcherActivity.INTENT_CLEAR, false);
            Photos newPhotos = new Photos();

            newPhotos.addAll(arrList);
            boolean more = intent.getBooleanExtra(PhotoSwitcherActivity.INTENT_MORE, false);
            if (clear) {
                mProfilePhotoGridAdapter.setData(newPhotos, more);
            } else {
                mProfilePhotoGridAdapter.addData(newPhotos, more);
            }
        }
    };
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 0) {
                mViewFlipper.setDisplayedChild(1);
            } else {
                startActivity(PhotoSwitcherActivity.getPhotoSwitcherIntent(
                        null,
                        position - 1,
                        CacheProfile.uid,
                        CacheProfile.totalPhotos,
                        CacheProfile.photos
                ));
            }
        }
    };

    private View createGridViewFooter() {
        return ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.gridview_footer_progress_bar, null, false);
    }

    private void sendAlbumRequest() {
        Photos photoLinks = mProfilePhotoGridAdapter.getAdapterData();
        if (photoLinks == null || photoLinks.size() < 2) {
            return;
        }
        mGridFooterView.setVisibility(View.VISIBLE);
        Photo photo = mProfilePhotoGridAdapter.getItem(photoLinks.size() - 1);
        int position = photo.getPosition();
        AlbumRequest request = new AlbumRequest(
                getActivity(),
                CacheProfile.uid,
                position + 1,
                AlbumRequest.MODE_ALBUM,
                AlbumLoadController.FOR_GALLERY
        );
        request.callback(new DataApiHandler<AlbumPhotos>() {

            @Override
            protected void success(AlbumPhotos data, IApiResponse response) {
                if (mProfilePhotoGridAdapter != null) {
                    mProfilePhotoGridAdapter.addPhotos(data, data.more, false);
                }
            }

            @Override
            protected AlbumPhotos parseResponse(ApiResponse response) {
                return new AlbumPhotos(response);
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                mGridFooterView.setVisibility(View.GONE);
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
        if (CacheProfile.photos != null) {
            photoLinks.addAll(CacheProfile.photos);
        }
        return photoLinks;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_profile_photos, container, false);
        mGridFooterView = createGridViewFooter();
        //Navigation bar

        if (getActivity() instanceof EditContainerActivity) {
            getActivity().setResult(Activity.RESULT_OK);
            setActionBarTitles(getString(R.string.edit_title), getString(R.string.edit_album));
        }

        mLoadingLocker = root.findViewById(R.id.fppLocker);

        mViewFlipper = (ViewFlipper) root.findViewById(R.id.vfFlipper);

        mGridAlbum = (GridViewWithHeaderAndFooter) root.findViewById(R.id.usedGrid);
        mProfilePhotoGridAdapter = new OwnPhotoGridAdapter(getActivity().getApplicationContext(), getPhotoLinks(),
                CacheProfile.totalPhotos, new LoadingListAdapter.Updater() {
            @Override
            public void onUpdate() {
                sendAlbumRequest();
            }
        });
        int position = 0;
        if (savedInstanceState != null) {
            try {
                mProfilePhotoGridAdapter.setData(new Photos(
                        new JSONArray(savedInstanceState.getString(PHOTOS))), false);
            } catch (JSONException e) {
                Debug.error(e);
            }
            position = savedInstanceState.getInt(POSITION, 0);
            mViewFlipper.setDisplayedChild(savedInstanceState.getInt(FLIPPER_VISIBLE_CHILD, 0));
        }
        addFooterView();
        mGridAlbum.setSelection(position);
        mGridAlbum.setOnItemClickListener(mOnItemClickListener);
        mGridAlbum.setOnScrollListener(mProfilePhotoGridAdapter);
        mGridAlbum.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position11, long id) {
                Photo item = (Photo) parent.getItemAtPosition(position11);
                if (needDialog(item)) {
                    startPhotoDialog(item, position11 - 1);
                    return true;
                }
                return false;
            }
        });
        mGridAlbum.post(new Runnable() {
            @Override
            public void run() {
                mGridAlbum.setAdapter(mProfilePhotoGridAdapter);
            }
        });

        root.findViewById(R.id.btnAddPhotoAlbum).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewFlipper.setDisplayedChild(0);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                        new Intent(AbstractProfileFragment.ADD_PHOTO_INTENT).putExtra("btn_id", R.id.btnAddPhotoAlbum));
            }
        });
        root.findViewById(R.id.btnAddPhotoCamera).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewFlipper.setDisplayedChild(0);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
                        new Intent(AbstractProfileFragment.ADD_PHOTO_INTENT).putExtra("btn_id", R.id.btnAddPhotoCamera));
            }
        });
        root.findViewById(R.id.btnCancel).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mViewFlipper.setDisplayedChild(0);
            }
        });

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mPhotosReceiver,
                new IntentFilter(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT)
        );


        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void addFooterView() {
        if (mGridAlbum != null) {
            if (mGridAlbum.getFooterViewCount() == 0) {
                mGridAlbum.addFooterView(mGridFooterView);
            }
            mGridFooterView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            outState.putString(PHOTOS, mProfilePhotoGridAdapter.getAdapterData().toJson().toString());
        } catch (JSONException e) {
            Debug.error(e);
        }
        outState.putInt(POSITION, mGridAlbum.getFirstVisiblePosition());
        outState.putInt(FLIPPER_VISIBLE_CHILD, mViewFlipper.getDisplayedChild());
    }

    @Override
    protected void onLoadProfile() {
        super.onLoadProfile();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mProfileUpdateReceiver,
                new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION)
        );
    }

    public void startPhotoDialog(final Photo photo, final int position) {
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
                            public void fail(int codeError, IApiResponse response) {
                                int errorStringResource;
                                switch (codeError) {
                                    // если пользователь пытается поставить на аватарку фото, которое было удалено модератором
                                    case ErrorCodes.NON_EXIST_PHOTO_ERROR:
                                        errorStringResource = R.string.general_non_exist_photo_error;
                                        // обновляем профиль пользователя
                                        App.sendProfileRequest();
                                        break;
                                    default:
                                        errorStringResource = R.string.general_server_error;
                                        break;
                                }
                                Toast.makeText(getActivity(), errorStringResource, Toast.LENGTH_SHORT)
                                        .show();
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
                                //Декрементим общее количество фотографий
                                CacheProfile.totalPhotos -= 1;
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
                                Intent changeAvatarPosIntent = new Intent(OwnAvatarFragment.UPDATE_AVATAR_POSITION);
                                if (position < CacheProfile.photo.position) {
                                    changeAvatarPosIntent.putExtra(OwnAvatarFragment.DECREMENT_AVATAR_POSITION, 1);
                                }
                                LocalBroadcastManager.getInstance(App.getContext())
                                        .sendBroadcast(changeAvatarPosIntent);
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
        return CacheProfile.photo != null && photo != null && !photo.isFake() && CacheProfile.photo.getId() != photo.getId();
    }

    @Override
    public void onResume() {
        getPhotoLinks();
        mProfilePhotoGridAdapter.updateData();
        mProfilePhotoGridAdapter.notifyDataSetChanged();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mPhotosReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mProfileUpdateReceiver);
    }
}
