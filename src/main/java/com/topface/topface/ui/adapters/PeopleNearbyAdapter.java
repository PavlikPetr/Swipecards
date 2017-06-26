package com.topface.topface.ui.adapters;

import android.content.Context;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.FeedGeo;

public class PeopleNearbyAdapter extends FeedAdapter<FeedGeo> {

	private final static String PLC = "people_nearby_list_loader";

	public PeopleNearbyAdapter(Context context, Updater updateCallback) {
		super(context, updateCallback);
	}

	@Override
	String getPlc() {
		return PLC;
	}

	@Override
	protected void setItemMessage(FeedGeo item, TextView messageView) {
		String text;
		if (item.user.deleted) {
			text = getContext().getString(R.string.user_is_deleted);
		} else if (item.user.banned) {
			text = getContext().getString(R.string.user_is_banned);
		} else {
			double distance;
			if (item.distance >= 1000) {
				distance = item.distance / 1000;
				text = String.format(getContext().getString(R.string.general_distance_km), distance);
			} else {
				distance = item.distance >= 1 ? item.distance : 1;
				text = String.format(getContext().getString(R.string.general_distance_m), (int) distance);
			}
		}
		messageView.setText(text);
	}

	@Override
	public ILoaderRetrierCreator<FeedGeo> getLoaderRetrierCreator() {
		return new ILoaderRetrierCreator<FeedGeo>() {
			@Override
			public FeedGeo getLoader() {
				FeedGeo result = new FeedGeo();
				result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
				return result;
			}

			@Override
			public FeedGeo getRetrier() {
				FeedGeo result = new FeedGeo();
				result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
				return result;
			}
		};
	}
}
