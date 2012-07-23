package com.topface.topface.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Adapter Operates with IListLoader interface.
 * Contains 2 basic types of views: 
 * - Loader(typeId:T_LOADER = 0) 
 * - Retrier(typeId:T_RETRIER = 1)
 * Methods which working with types are Overridden 
 * @author kirussell
 *
 */
public class LoadingListAdapter extends BaseAdapter {

	public static final int T_NONE = -1;
	public static final int T_ALL = 0;
	public static final int T_LOADER = 1;
	public static final int T_RETRIER = 2;
	public int T_COUNT = 3;
	
	protected View mLoaderRetrier;
	protected TextView mLoaderRetrierText;
	protected ProgressBar mLoaderRetrierProgress;
	
	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public IListLoader getItem(int position) {		
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}
		
	/**
	 * returns T_LOADER, T_RETRIER and T_NONE for other types
	 */
	@Override
	public int getItemViewType(int position) {
		if (getItem(position).isLoader()) 
			return T_LOADER;
		else if (getItem(position).isLoaderRetry())
			return T_RETRIER;
		else
			return T_NONE;
	}

	/**
	 * returns number of basic types (T_COUNT = 3)
	 */
	@Override
	public int getViewTypeCount() {
		return T_COUNT;
	}	
		
}
