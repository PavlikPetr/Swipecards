package com.topface.topface.ui.adapters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;

import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Dialog;
import com.topface.topface.data.History;
import com.topface.topface.ui.views.RoundedImageView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.MemorySyncCache;
import com.topface.topface.utils.Osm;
import com.topface.topface.utils.StorageCache;
import com.topface.topface.utils.MemoryCacheTemplate;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.http.Http;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ChatListAdapter extends BaseAdapter {
	// class ViewHolder
	static class ViewHolder {
		RoundedImageView mAvatar;
		TextView mMessage;
		TextView mDate;
		ImageView mGift;
		TextView mAddress;
		ImageView mMapBackground;
		ProgressBar mPrgsAddress;
		// View mInfoGroup;
	}

	private Context mContext;
	private int mFriendId;
	private int mOwnerId;
	private LayoutInflater mInflater;
	private LinkedList<History> mDataList; // data
	private LinkedList<Integer> mItemLayoutList; // types
	private HashMap<Integer, String> mItemTimeList; // date
	private View.OnClickListener mOnClickListener;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
	private MemorySyncCache mMemorySyncCache;
	private MemoryCacheTemplate<String, String> mAddressesCache;
	private StorageCache mStorageCache;
	private int mGiftFrameWidth;
	// Type Item
	private static final int T_USER_PHOTO = 0;
	private static final int T_USER_EXT = 1;
	private static final int T_FRIEND_PHOTO = 2;
	private static final int T_FRIEND_EXT = 3;
	private static final int T_DATE = 4;
	private static final int T_USER_GIFT_PHOTO = 5;
	private static final int T_USER_GIFT_EXT = 6;
	private static final int T_FRIEND_GIFT_PHOTO = 7;
	private static final int T_FRIEND_GIFT_EXT = 8;
	private static final int T_USER_MAP_PHOTO = 9;
	private static final int T_USER_MAP_EXT = 10;
	private static final int T_FRIEND_MAP_PHOTO = 11;
	private static final int T_FRIEND_MAP_EXT = 12;
	private static final int T_COUNT = 13;

	public ChatListAdapter(Context context, int friendId, LinkedList<History> dataList) {
		mContext = context;
		mFriendId = friendId;
		mOwnerId = CacheProfile.uid;
		mItemLayoutList = new LinkedList<Integer>();
		mItemTimeList = new HashMap<Integer, String>();
		mGiftFrameWidth = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.chat_gift_frame).getWidth();
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMemorySyncCache = new MemorySyncCache();
		mAddressesCache = new MemoryCacheTemplate<String, String>();
		mStorageCache = new StorageCache(mContext, StorageCache.EXTERNAL_CACHE);
		prepare(dataList);
	}

	public void setOnAvatarListener(View.OnClickListener onAvatarListener) {
		mOnClickListener = onAvatarListener;
	}

	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public History getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return T_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		return mItemLayoutList.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		final int type = getItemViewType(position);
		final History history = getItem(position);

		if (convertView == null) {
			holder = new ViewHolder();

			switch (type) {
			case T_FRIEND_PHOTO:
				convertView = mInflater.inflate(R.layout.chat_friend, null, false);
				holder.mAvatar = (RoundedImageView) convertView.findViewById(R.id.left_icon);
				holder.mMessage = (TextView) convertView.findViewById(R.id.chat_message);
				holder.mDate = (TextView) convertView.findViewById(R.id.chat_date);
				holder.mAvatar.setOnClickListener(mOnClickListener);
				if (Data.friendAvatar != null)
					holder.mAvatar.setImageBitmap(Data.friendAvatar);
				break;
			case T_FRIEND_EXT:
				convertView = mInflater.inflate(R.layout.chat_friend_ext, null, false);
				holder.mAvatar = (RoundedImageView) convertView.findViewById(R.id.left_icon);
				holder.mMessage = (TextView) convertView.findViewById(R.id.chat_message);
				holder.mDate = (TextView) convertView.findViewById(R.id.chat_date);
				break;
			case T_USER_PHOTO:
				convertView = mInflater.inflate(R.layout.chat_user, null, false);
				holder.mAvatar = (RoundedImageView) convertView.findViewById(R.id.left_icon);
				holder.mMessage = (TextView) convertView.findViewById(R.id.chat_message);
				holder.mDate = (TextView) convertView.findViewById(R.id.chat_date);
				holder.mAvatar.setImageBitmap(Data.ownerAvatar);
				break;
			case T_USER_EXT:
				convertView = mInflater.inflate(R.layout.chat_user_ext, null, false);
				holder.mAvatar = (RoundedImageView) convertView.findViewById(R.id.left_icon);
				holder.mMessage = (TextView) convertView.findViewById(R.id.chat_message);
				holder.mDate = (TextView) convertView.findViewById(R.id.chat_date);
				break;
			case T_DATE:
				convertView = mInflater.inflate(R.layout.chat_date_divider, null, false);
				holder.mDate = (TextView) convertView.findViewById(R.id.tvChatDateDivider);
				break;
			case T_USER_GIFT_PHOTO:
				convertView = mInflater.inflate(R.layout.chat_user_gift, null, false);
				holder.mAvatar = (RoundedImageView) convertView.findViewById(R.id.left_icon);
				holder.mGift = (ImageView) convertView.findViewById(R.id.ivChatGift);
				holder.mAvatar.setImageBitmap(Data.ownerAvatar);
				holder.mAvatar.setVisibility(View.VISIBLE);
				break;
			case T_USER_GIFT_EXT:
				convertView = mInflater.inflate(R.layout.chat_user_gift, null, false);
				holder.mAvatar = (RoundedImageView) convertView.findViewById(R.id.left_icon);
				holder.mGift = (ImageView) convertView.findViewById(R.id.ivChatGift);
				holder.mAvatar.setVisibility(View.INVISIBLE);
				break;
			case T_FRIEND_GIFT_PHOTO:
				convertView = mInflater.inflate(R.layout.chat_friend_gift, null, false);
				holder.mAvatar = (RoundedImageView) convertView.findViewById(R.id.left_icon);
				holder.mGift = (ImageView) convertView.findViewById(R.id.ivChatGift);
				holder.mAvatar.setOnClickListener(mOnClickListener);
				if (Data.friendAvatar != null)
					holder.mAvatar.setImageBitmap(Data.friendAvatar);
				holder.mAvatar.setVisibility(View.VISIBLE);
				break;
			case T_FRIEND_GIFT_EXT:
				convertView = mInflater.inflate(R.layout.chat_friend_gift, null, false);
				holder.mAvatar = (RoundedImageView) convertView.findViewById(R.id.left_icon);
				holder.mGift = (ImageView) convertView.findViewById(R.id.ivChatGift);
				holder.mAvatar.setVisibility(View.INVISIBLE);
				break;
			case T_USER_MAP_PHOTO:
				convertView = mInflater.inflate(R.layout.chat_user_map, null, false);
				holder.mAvatar = (RoundedImageView) convertView.findViewById(R.id.left_icon);
				holder.mAddress = (TextView) convertView.findViewById(R.id.tvChatMapAddress);
				holder.mMapBackground = (ImageView) convertView.findViewById(R.id.ivUserMapBg);
				holder.mPrgsAddress = (ProgressBar) convertView
						.findViewById(R.id.prgsUserMapAddress);
				holder.mAvatar.setImageBitmap(Data.ownerAvatar);
				holder.mAvatar.setVisibility(View.VISIBLE);
				break;
			case T_USER_MAP_EXT:
				convertView = mInflater.inflate(R.layout.chat_user_map, null, false);
				holder.mAvatar = (RoundedImageView) convertView.findViewById(R.id.left_icon);
				holder.mAddress = (TextView) convertView.findViewById(R.id.tvChatMapAddress);
				holder.mMapBackground = (ImageView) convertView.findViewById(R.id.ivUserMapBg);
				holder.mPrgsAddress = (ProgressBar) convertView
						.findViewById(R.id.prgsUserMapAddress);
				holder.mAvatar.setVisibility(View.INVISIBLE);
				break;
			case T_FRIEND_MAP_PHOTO:
				convertView = mInflater.inflate(R.layout.chat_friend_map, null, false);
				holder.mAvatar = (RoundedImageView) convertView.findViewById(R.id.left_icon);
				holder.mAddress = (TextView) convertView.findViewById(R.id.tvChatMapAddress);
				holder.mMapBackground = (ImageView) convertView.findViewById(R.id.ivFriendMapBg);
				holder.mPrgsAddress = (ProgressBar) convertView
						.findViewById(R.id.prgsFriendMapAddress);
				holder.mAvatar.setOnClickListener(mOnClickListener);
				if (Data.friendAvatar != null)
					holder.mAvatar.setImageBitmap(Data.friendAvatar);
				holder.mAvatar.setVisibility(View.VISIBLE);
				break;
			case T_FRIEND_MAP_EXT:
				convertView = mInflater.inflate(R.layout.chat_friend_map, null, false);
				holder.mAvatar = (RoundedImageView) convertView.findViewById(R.id.left_icon);
				holder.mAddress = (TextView) convertView.findViewById(R.id.tvChatMapAddress);
				holder.mMapBackground = (ImageView) convertView.findViewById(R.id.ivFriendMapBg);
				holder.mPrgsAddress = (ProgressBar) convertView
						.findViewById(R.id.prgsFriendMapAddress);
				holder.mAvatar.setVisibility(View.INVISIBLE);
				break;
			}

			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();

		// setting visual information
		if (type == T_DATE) {
			holder.mDate.setText(mItemTimeList.get(position));
			return convertView;
		} else if (type == T_USER_GIFT_PHOTO || type == T_USER_GIFT_EXT
				|| type == T_FRIEND_GIFT_PHOTO || type == T_FRIEND_GIFT_EXT) {
			giftLoading(holder.mGift, history);
			return convertView;
		} else if (type == T_USER_MAP_PHOTO || type == T_USER_MAP_EXT
				|| type == T_FRIEND_MAP_PHOTO || type == T_FRIEND_MAP_EXT) {
			holder.mAddress.setText(Static.EMPTY);
			if (history.currentLocation) {
				holder.mMapBackground.setBackgroundResource(R.drawable.chat_item_place);
			} else {
				holder.mMapBackground.setBackgroundResource(R.drawable.chat_item_map);
			}

			holder.mMapBackground.setTag(history);
			holder.mMapBackground.setOnClickListener(mOnClickListener);

			mapAddressDetection(history, holder.mAddress, holder.mPrgsAddress);
			return convertView;
		}

		// setting textual information
		switch (history.type) {
		case Dialog.DEFAULT:
			holder.mMessage.setText(history.text);
			break;
		case Dialog.PHOTO:
//			if (history.code > 100500) {
//				holder.mMessage.setText(history.text);
//				//holder.mMessage.setText(mContext.getString(R.string.chat_money_in) + ".");
//				break;
//			}
            holder.mMessage.setText("TARGET IT");
//			switch (history.target) {
//			case Dialog.FRIEND_MESSAGE:
//				holder.mMessage.setText(mContext.getString(R.string.chat_rate_in) + " " + history.code + ".");
//				break;
//			case Dialog.USER_MESSAGE:
//				holder.mMessage.setText(mContext.getString(R.string.chat_rate_out) + " " + history.code + ".");
//				break;
//			}
			break;
		case Dialog.MESSAGE:
			holder.mMessage.setText(history.text);			
			break;
		case Dialog.MESSAGE_WISH:
			switch (history.target) {
			case Dialog.FRIEND_MESSAGE:
				holder.mMessage.setText(mContext.getString(R.string.chat_wish_in));
				break;
			case Dialog.USER_MESSAGE:
				holder.mMessage.setText(mContext.getString(R.string.chat_wish_out));
				break;
			}
			break;
		case Dialog.MESSAGE_SEXUALITY:
			switch (history.target) {
			case Dialog.FRIEND_MESSAGE:
				holder.mMessage.setText(mContext.getString(R.string.chat_sexuality_in));
				break;
			case Dialog.USER_MESSAGE:
				holder.mMessage.setText(mContext.getString(R.string.chat_sexuality_out));
				break;
			}
			break;
		case Dialog.LIKE:
			switch (history.target) {
			case Dialog.FRIEND_MESSAGE:
				holder.mMessage.setText(mContext.getString(R.string.chat_like_in));
				break;
			case Dialog.USER_MESSAGE:
				holder.mMessage.setText(mContext.getString(R.string.chat_like_out));
				break;
			}
			break;
		case Dialog.SYMPHATHY:
			switch (history.target) {
			case Dialog.FRIEND_MESSAGE:
				holder.mMessage.setText(mContext.getString(R.string.chat_symphathy_in));
				break;
			case Dialog.USER_MESSAGE:
				holder.mMessage.setText(mContext.getString(R.string.chat_symphathy_out));
				break;
			}
			break;		
		case Dialog.MESSAGE_WINK:
			switch (history.target) {
			case Dialog.FRIEND_MESSAGE:
				holder.mMessage.setText(mContext.getString(R.string.chat_wink_in));
				break;
			case Dialog.USER_MESSAGE:
				holder.mMessage.setText(mContext.getString(R.string.chat_wink_out));
				break;
			}
			break;
		case Dialog.RATE:
            holder.mMessage.setText("RATE IT");
//			switch (history.target) {
//			case Dialog.FRIEND_MESSAGE:
//				holder.mMessage.setText(mContext.getString(R.string.chat_rate_in) + " " + history.code + ".");
//				break;
//			case Dialog.USER_MESSAGE:
//				holder.mMessage.setText(mContext.getString(R.string.chat_rate_out) + " " + history.code + ".");
//				break;
//			}
			break;
		case Dialog.PROMOTION:
			holder.mMessage.setText(history.text);
			break;
		default:
			holder.mMessage.setText("");
			break;
		}

		holder.mDate.setText(dateFormat.format(history.created));
		// Utils.formatTime(holder.mDate, msg.created);

		return convertView;
	}

	public void addSentMessage(History msg) {
		int position = mDataList.size() - 1;
		History prevHistory = null;
		if (position >= 0) {
			prevHistory = mDataList.getLast(); //get(mDataList.size() - 1);
		}

		if (msg.type == Dialog.MESSAGE) {
			if (prevHistory == null) {
				mItemLayoutList.add(T_USER_PHOTO);
			} else {
				if (prevHistory.target == Dialog.USER_MESSAGE)
					mItemLayoutList.add(T_USER_EXT);
				else
					mItemLayoutList.add(T_USER_PHOTO);
			}
		} else if (msg.type == Dialog.GIFT) {
			if (prevHistory == null)
				mItemLayoutList.add(T_USER_GIFT_PHOTO);
			else {
				if (prevHistory.target == Dialog.USER_MESSAGE)
                    mItemLayoutList.add(T_USER_GIFT_EXT);
				else
                    mItemLayoutList.add(T_USER_GIFT_PHOTO);
			}

		} else if (msg.type == Dialog.MAP) {
			if (prevHistory == null)
				mItemLayoutList.add(T_USER_MAP_PHOTO);
			else {
				if (prevHistory.target == Dialog.USER_MESSAGE)
                    mItemLayoutList.add(T_USER_MAP_EXT);
				else
                    mItemLayoutList.add(T_USER_MAP_PHOTO);

			}
		}

		mDataList.add(msg);
	}

	public void setDataList(LinkedList<History> dataList) {
		prepare(dataList);
	}

	private void prepare(LinkedList<History> dataList) {

		SimpleDateFormat dowFormat = new SimpleDateFormat("EEEE");

		long day = 1000 * 60 * 60 * 24;
		long numb = Data.midnight - day * 5;

		mDataList = new LinkedList<History>();

		int prev_target = -1;
		long prev_date = 0;
		int count = dataList.size();
		for (int i = 0; i < count; i++) {
			History history = dataList.get(i);

			long created = history.created;

			// Date
			{
				String formatedDate = Static.EMPTY;
				if (created < numb) {

					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(created);
					cal.set(Calendar.HOUR_OF_DAY, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);

					created = cal.getTimeInMillis();
					if (prev_date != created)
						formatedDate = DateFormat.format("dd MMMM", history.created).toString();

				} else if (created > Data.midnight) {
					created = Data.midnight;
					formatedDate = mContext.getString(R.string.time_today); // м.б.
																			// переделать
																			// на
																			// HashMap
				} else {
					if (created > Data.midnight - day) {
						created = Data.midnight - day;
						formatedDate = mContext.getString(R.string.time_yesterday);
					} else if (created > Data.midnight - day * 2) {
						created = Data.midnight - day * 2;
						formatedDate = dowFormat.format(history.created);
					} else if (created > Data.midnight - day * 3) {
						created = Data.midnight - day * 3;
						formatedDate = dowFormat.format(history.created);
					} else if (created > Data.midnight - day * 4) {
						created = Data.midnight - day * 4;
						formatedDate = dowFormat.format(history.created);
					} else if (created > Data.midnight - day * 5) {
						created = Data.midnight - day * 5;
						formatedDate = dowFormat.format(history.created);
					}
				}

				if (prev_date != created) {
					mItemLayoutList.add(T_DATE);
					mItemTimeList.put(mItemLayoutList.size() - 1, formatedDate.toUpperCase());
					mDataList.add(null);
					prev_date = created;
				}
			}

			// Type
			int item_type = 0;
			if (history.target == Dialog.FRIEND_MESSAGE) {
				switch (history.type) {
				case Dialog.GIFT:
					if (history.target == prev_target)
						item_type = T_FRIEND_GIFT_EXT;
					else
						item_type = T_FRIEND_GIFT_PHOTO;
					break;
				case Dialog.MAP:
					if (history.target == prev_target)
						item_type = T_FRIEND_MAP_EXT;
					else
						item_type = T_FRIEND_MAP_PHOTO;
					break;
				default:
					if (history.target == prev_target)
						item_type = T_FRIEND_EXT;
					else
						item_type = T_FRIEND_PHOTO;
					break;
				}
			} else {
				switch (history.type) {
				case Dialog.GIFT:
					if (history.target == prev_target)
						item_type = T_USER_GIFT_EXT;
					else
						item_type = T_USER_GIFT_PHOTO;
					break;
				case Dialog.MAP:
					if (history.target == prev_target) {
						item_type = T_USER_MAP_EXT;
					} else {
						item_type = T_USER_MAP_PHOTO;
					}
				default:
					if (history.target == prev_target) {
						item_type = T_USER_EXT;
					} else {
						item_type = T_USER_PHOTO;
					}
					break;
				}
			}

			prev_target = history.target;

			mItemLayoutList.add(item_type);
			mDataList.add(history);
		}
	}

	private void giftLoading(final ImageView iv, final History history) {
		Debug.log(this, "#id:" + history.id);
		Bitmap bitmap = mMemorySyncCache.get(history.gift);
		if (bitmap != null) {
			iv.setImageBitmap(bitmap);
			iv.setVisibility(View.VISIBLE);
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				Bitmap rawBitmap = mStorageCache.load(history.gift);
				if (rawBitmap == null)
					rawBitmap = Http.bitmapLoader(history.link);

				if (rawBitmap == null)
					return;

				final Bitmap roundedBitmap = Utils.getScaleAndRoundBitmapOut(rawBitmap,
						mGiftFrameWidth, mGiftFrameWidth, 1.5f);

				iv.post(new Runnable() {
					@Override
					public void run() {
						if (iv != null) {
							iv.setImageBitmap(roundedBitmap);
							iv.setVisibility(View.VISIBLE);
						}
					}
				});
				mMemorySyncCache.put(history.gift, roundedBitmap);
			}
		}).start();
	}

	private void mapAddressDetection(final History history, final TextView tv,
			final ProgressBar prgsBar) {
		StringBuilder sb = new StringBuilder();
		sb.append(history.latitude).append(history.longitude);
		final String key = sb.toString();
		String cachedAddress = mAddressesCache.get(key);

		if (cachedAddress != null) {
			tv.setText(cachedAddress);
			return;
		}

		prgsBar.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String address = Osm.getAddress(history.latitude, history.longitude);
				mAddressesCache.put(key, address);
				tv.post(new Runnable() {
					@Override
					public void run() {
						tv.setText(address);
						prgsBar.setVisibility(View.GONE);
					}
				});
			}
		}).start();

	}

	public void release() {
		if (mDataList != null)
			mDataList.clear();
		if (mItemLayoutList != null)
			mItemLayoutList.clear();
		mDataList = null;
		mInflater = null;
		mItemLayoutList = null;
	}
}
