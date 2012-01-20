package com.sonetica.topface.module.pull2refresh;

import com.sonetica.topface.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

public class PullToRefreshGridView extends PullToRefreshAdapterViewBase<GridView> {

	class InternalGridView extends GridView implements EmptyViewMethodAccessor {

		public InternalGridView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void setEmptyView(View emptyView) {
			PullToRefreshGridView.this.setEmptyView(emptyView);
		}

		@Override
		public void setEmptyViewInternal(View emptyView) {
			super.setEmptyView(emptyView);
		}

	}

	public PullToRefreshGridView(Context context) {
		super(context);
	}

	public PullToRefreshGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void invalidateViews() {
	  getRefreshableView().invalidate();
	}
	
	@Override
	protected final GridView createRefreshableView(Context context, AttributeSet attrs) {
		GridView gv = new InternalGridView(context, attrs);

		// Use Generated ID (from res/values/ids.xml)
		gv.setId(R.id.gridview);
		return gv;
	}

}
