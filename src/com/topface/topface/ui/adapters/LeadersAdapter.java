package com.topface.topface.ui.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.topface.topface.R;
import com.topface.topface.data.Leaders;
import com.topface.topface.data.UserPhotos;
import com.topface.topface.imageloader.DefaultImageLoader;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;

/**
 * Адаптер для блока лидеров
 *
 * @see com.topface.topface.ui.blocks.LeadersBlock
 */
public class LeadersAdapter extends BaseAdapter {
    public static final int AVATAR_BORDER_RADIUS = 8;
    private LayoutInflater mInflater;
    private ArrayList<Leaders.LeaderUser> mLeaders;
    private Context mContext;

    public LeadersAdapter(Context context, Leaders leaders) {
        super();
        mInflater = LayoutInflater.from(context);
        mLeaders  = leaders.leaders;
        mContext  = context;
    }

    @Override
    public int getCount() {
        return mLeaders.size();
    }

    @Override
    public Leaders.LeaderUser getItem(int i) {
        return mLeaders.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView avatar;
        Leaders.LeaderUser leader = getItem(i);

        if (view == null) {
            view = mInflater.inflate(R.layout.leaders_item, null);
            avatar = (ImageView) view.findViewById(R.id.leaderAvatar);
            view.setTag(avatar);
        }
        else {
            avatar = (ImageView) view.getTag();
        }

        setLeaderAvatar(leader.photo, avatar);

        return view;
    }

    private void setLeaderAvatar(UserPhotos photo, final ImageView avatar) {
        DefaultImageLoader.getInstance().getImageLoader().displayImage(photo.links.get(UserPhotos.SIZE_ORIGINAL), avatar, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(Bitmap loadedImage) {
                super.onLoadingComplete(loadedImage);
                //Не хочется, что бы приложение упало при загрузке лидеров
                try {
                    avatar.setImageBitmap(
                            Utils.getRoundedCornerBitmap(
                                    loadedImage,
                                    loadedImage.getWidth(),
                                    loadedImage.getHeight(),
                                    AVATAR_BORDER_RADIUS
                            )
                    );
                }
                catch (Exception e) {
                    Debug.error("Leader set avatar error", e);
                }

            }
        });
    }
}
