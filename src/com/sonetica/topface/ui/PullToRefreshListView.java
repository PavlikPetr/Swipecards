package com.sonetica.topface.ui;

import com.sonetica.topface.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListAdapter;
import android.widget.ListView;

public class PullToRefreshListView extends PullToRefreshBase<ListView> {

	public PullToRefreshListView(Context context) {
		super(context);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected final ListView createAdapterView(Context context, AttributeSet attrs) {
		ListView lv = new ListView(context, attrs);

		// Set it to this so it can be used in ListActivity/ListFragment
		lv.setId(R.id.lvChatList);
		return lv;
	}
	public void setAdapter(ListAdapter adapter) {
	  getAdapterView().setAdapter(adapter);
	}
}
