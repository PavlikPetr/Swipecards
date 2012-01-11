package com.sonetica.topface.ui;

import com.sonetica.topface.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.ListAdapter;

public class PullToRefreshGridView extends PullToRefreshBase<GridView> {

	public PullToRefreshGridView(Context context) {
		super(context);
	}

	public PullToRefreshGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected final GridView createAdapterView(Context context, AttributeSet attrs) {
		GridView gridView = new GridView(context, attrs);

		// Use Generated ID (from res/values/ids.xml)
		gridView.setId(R.id.grdLikesGallary);
		return gridView;
	}
	
  public void setAdapter(ListAdapter adapter) {
    getAdapterView().setAdapter(adapter);
  }
  
  public void invalidateViews() {
    getAdapterView().invalidateViews();
  }
}
