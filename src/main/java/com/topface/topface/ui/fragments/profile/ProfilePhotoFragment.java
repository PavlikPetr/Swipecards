package com.topface.topface.ui.fragments.profile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.AlbumPhotos;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.Profile;
import com.topface.topface.databinding.FragmentProfilePhotosBinding;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PhotoDeleteRequest;
import com.topface.topface.requests.PhotoMainRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.statistics.FlurryOpenEvent;
import com.topface.topface.ui.IBackPressedListener;
import com.topface.topface.ui.adapters.BasePhotoRecyclerViewAdapter;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.analytics.TrackedFragmentActivity;
import com.topface.topface.ui.dialogs.PermissionAlertDialogFactory;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.ui.fragments.profile.photoswitcher.view.PhotoSwitcherActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;
import com.topface.topface.utils.rx.RxUtils;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.RuntimePermissions;
import rx.Subscription;
import rx.functions.Action1;

import static com.topface.topface.utils.AddPhotoHelper.EXTRA_BUTTON_ID;

@FlurryOpenEvent(name = ProfilePhotoFragment.PAGE_NAME)
@RuntimePermissions
public class ProfilePhotoFragment extends ProfileInnerFragment implements IBackPressedListener {

    private static final String POSITION = "POSITION";
    private static final String FLIPPER_VISIBLE_CHILD = "FLIPPER_VISIBLE_CHILD";
    public static final String PAGE_NAME = "profile.photos";
    public TopfaceAppState mAppState;
    private OwnProfileRecyclerViewAdapter mOwnProfileRecyclerViewAdapter;

    private BroadcastReceiver mPhotosReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != mOwnProfileRecyclerViewAdapter) {
                mOwnProfileRecyclerViewAdapter.updateData(App.get().getProfile().photos, App.from(context).getProfile().photosCount, true);
            }
        }
    };
    private Subscription mSubscription;
    private FragmentProfilePhotosBinding mBinding;

    private BasePhotoRecyclerViewAdapter.OnRecyclerViewItemClickListener mClickListener = new BasePhotoRecyclerViewAdapter.OnRecyclerViewItemClickListener() {
        @Override
        public void itemClick(View view, int itemPosition, Photo photo) {
            Profile profile = App.from(getActivity()).getProfile();
            if (itemPosition == 0) {
                mBinding.vfFlipper.setDisplayedChild(1);
            } else if (itemPosition <= profile.photosCount) {
                startActivity(PhotoSwitcherActivity.getPhotoSwitcherIntent(
                        itemPosition - 1,
                        profile.uid,
                        profile.photosCount,
                        profile.photos
                ));
            }
        }
    };

    private BasePhotoRecyclerViewAdapter.OnRecyclerViewItemLongClickListener longClickListener = new BasePhotoRecyclerViewAdapter.OnRecyclerViewItemLongClickListener() {
        @Override
        public void itemLongClick(View view, int itemPosition, Photo photo) {
            if (needDialog(photo)) {
                startPhotoDialog(photo, itemPosition);
            }
        }
    };

    private View createGridViewFooter() {
        return ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.gridview_footer_progress_bar, null, false);
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    private void sendAlbumRequest() {
        Photos photoLinks = mOwnProfileRecyclerViewAdapter.getAdapterData();
        if (photoLinks == null || photoLinks.size() < 2) {
            return;
        }
        //откидываем еще фейк для футера
        final Photo photo = mOwnProfileRecyclerViewAdapter.getItem(photoLinks.size() - 2);
        int position = photo.getPosition();
        final Profile profile = App.get().getProfile();
        AlbumRequest request = new AlbumRequest(
                getActivity(),
                profile.uid,
                position + 1,
                AlbumRequest.MODE_ALBUM,
                AlbumLoadController.FOR_GALLERY
        );
        request.callback(new DataApiHandler<AlbumPhotos>() {

            @Override
            protected void success(AlbumPhotos data, IApiResponse response) {
                if (mOwnProfileRecyclerViewAdapter != null) {
                    mOwnProfileRecyclerViewAdapter.addPhotos(data, data.more, false, false);
                    profile.photos = mOwnProfileRecyclerViewAdapter.getPhotos();
                    mAppState.setData(profile);
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

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public void takeAlbumPhoto() {
        if (mBinding != null) {
            mBinding.vfFlipper.setDisplayedChild(0);
        }
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(
                new Intent(AbstractProfileFragment.ADD_PHOTO_INTENT).putExtra(EXTRA_BUTTON_ID, R.id.btnAddPhotoAlbum));
    }

    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public void takeCameraPhoto() {
        if (mBinding != null) {
            mBinding.vfFlipper.setDisplayedChild(0);
        }
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(
                new Intent(AbstractProfileFragment.ADD_PHOTO_INTENT).putExtra(EXTRA_BUTTON_ID, R.id.btnAddPhotoCamera));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ProfilePhotoFragmentPermissionsDispatcher.onRequestPermissionsResult(ProfilePhotoFragment.this, requestCode, grantResults);
        App.getAppConfig().putPermissionsState(permissions, grantResults);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mAppState = App.getAppComponent().appState();
        if (getActivity() instanceof TrackedFragmentActivity) {
            ((TrackedFragmentActivity) getActivity()).setBackPressedListener(this);
        }
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_photos, container, false);
        ProfilePhotoFragmentViewModel viewModel = new ProfilePhotoFragmentViewModel(mBinding,
                new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        ProfilePhotoFragmentPermissionsDispatcher.takeCameraPhotoWithCheck(ProfilePhotoFragment.this);
                        return null;
                    }
                },
                new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        ProfilePhotoFragmentPermissionsDispatcher.takeAlbumPhotoWithCheck(ProfilePhotoFragment.this);
                        return null;
                    }
                });
        mBinding.setViewModel(viewModel);
        if (getActivity() instanceof EditContainerActivity) {
            getActivity().setResult(Activity.RESULT_OK);
            //TODO TITLE getString(R.string.edit_title), getString(R.string.edit_album)
        }
        mOwnProfileRecyclerViewAdapter = new OwnProfileRecyclerViewAdapter(new Photos(),
                App.from(getActivity()).getProfile().photosCount, new LoadingListAdapter.Updater() {
            @Override
            public void onUpdate() {
                sendAlbumRequest();
            }
        });
        mOwnProfileRecyclerViewAdapter.setOnItemClickListener(mClickListener);
        mOwnProfileRecyclerViewAdapter.setOnItemLongClickListener(longClickListener);
        mOwnProfileRecyclerViewAdapter.setFooter(createGridViewFooter(), false);
        final int position;
        if (savedInstanceState != null) {
            position = savedInstanceState.getInt(POSITION, 0);
            mBinding.vfFlipper.setDisplayedChild(savedInstanceState.getInt(FLIPPER_VISIBLE_CHILD, 0));
        } else {
            position = 0;
        }
        int spanCount = getResources().getInteger(R.integer.add_to_leader_column_count);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        mBinding.usedGrid.setLayoutManager(manager);
        mBinding.usedGrid.post(new Runnable() {
            @Override
            public void run() {
                mBinding.usedGrid.setAdapter(mOwnProfileRecyclerViewAdapter);
                mBinding.usedGrid.scrollToPosition(position);
            }
        });
        mSubscription = mAppState.getObservable(Profile.class).subscribe(new RxUtils.ShortSubscription<Profile>() {
            @Override
            public void onNext(Profile profile) {
                super.onNext(profile);
                if (mOwnProfileRecyclerViewAdapter != null && profile.photos != null &&
                        mOwnProfileRecyclerViewAdapter.getPhotos().size() != profile.photos.size()) {

                    mOwnProfileRecyclerViewAdapter.setData(profile.photos, profile.photos.size() < profile.photosCount, true, true);
                }
            }
        });
        return mBinding.getRoot();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mOwnProfileRecyclerViewAdapter != null) {
            outState.putInt(POSITION, (mBinding.usedGrid != null) ? mOwnProfileRecyclerViewAdapter.getFirstVisibleItemPos() : 0);
        }
        outState.putInt(FLIPPER_VISIBLE_CHILD, mBinding.vfFlipper.getDisplayedChild());
    }

    public void startPhotoDialog(final Photo photo, final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(new String[]{getString(R.string.edit_set_as_main), getString(R.string.edit_delete)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mBinding.fppLocker.setVisibility(View.VISIBLE);
                final Profile profile = App.from(getActivity()).getProfile();
                switch (which) {
                    case 0:
                        PhotoMainRequest request = new PhotoMainRequest(getActivity());
                        request.photoId = photo.getId();
                        request.callback(new SimpleApiHandler() {
                            @Override
                            public void success(IApiResponse response) {
                                super.success(response);
                                profile.photo = photo;
                                mAppState.setData(profile);
                                CacheProfile.sendUpdateProfileBroadcast();
                            }

                            @Override
                            public void fail(int codeError, IApiResponse response) {
                                int errorStringResource = 0;
                                switch (codeError) {
                                    // если пользователь пытается поставить на аватарку фото, которое было удалено модератором
                                    case ErrorCodes.NON_EXIST_PHOTO_ERROR:
                                        errorStringResource = R.string.general_non_exist_photo_error;
                                        // обновляем профиль пользователя
                                        App.sendProfileRequest(null);
                                        break;
                                    case ErrorCodes.CODE_CANNOT_SET_PHOTO_AS_MAIN:
                                        Utils.showCantSetPhotoAsMainToast(response);
                                        break;
                                    default:
                                        errorStringResource = R.string.general_server_error;
                                        break;
                                }
                                if (errorStringResource > 0) {
                                    Utils.showToastNotification(errorStringResource, Toast.LENGTH_SHORT);
                                }
                            }

                            @Override
                            public void always(IApiResponse response) {
                                super.always(response);
                                mBinding.fppLocker.setVisibility(View.GONE);
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
                                profile.photosCount -= 1;
                                profile.photos.remove(photo);
                                mAppState.setData(profile);
                                if (position < profile.photo.position) {
                                    CacheProfile.incrementPhotoPosition(getActivity(), -1);
                                }
                            }

                            @Override
                            public void always(IApiResponse response) {
                                super.always(response);
                                mBinding.fppLocker.setVisibility(View.GONE);
                            }
                        }).exec();
                        break;
                }
            }
        });
        builder.create().show();
    }

    private boolean needDialog(Photo photo) {
        return App.from(getActivity()).getProfile().photo != null && photo != null && !photo.isFake() && App.from(getActivity()).getProfile().photo.getId() != photo.getId();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        RxUtils.safeUnsubscribe(mSubscription);
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mPhotosReceiver,
                new IntentFilter(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT)
        );
        mOwnProfileRecyclerViewAdapter.updateData(App.from(getActivity()).getProfile().photos, App.from(getActivity()).getProfile().photosCount, true);
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mPhotosReceiver);
        super.onPause();
    }

    @Override
    public boolean onBackPressed() {
        if (mBinding.vfFlipper != null && mBinding.vfFlipper.getDisplayedChild() == 1) {
            mBinding.vfFlipper.setDisplayedChild(0);
            return true;
        }
        return false;
    }

    @OnNeverAskAgain({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})
    public void onNeverAskAgain() {
        Activity activity = getActivity();
        if (activity != null) {
            new PermissionAlertDialogFactory().constructNeverAskAgain(activity);
        }
    }
}
