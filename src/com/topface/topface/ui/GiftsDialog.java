package com.topface.topface.ui;

import java.util.LinkedList;
import java.util.List;
import com.topface.topface.R;
import com.topface.topface.data.Gift;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.GiftsRequest;
import com.topface.topface.ui.adapters.GiftsAdapter;
import com.topface.topface.utils.GiftGalleryGridManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TabHost;

public class GiftsDialog extends Dialog {

    private Context mContext;
    private Activity mActivity;

    public static final int GIFTS_COLUMN = 3;
    public static int dialogWidth = 0;

    private GiftsRequest giftRequest;

    private LayoutInflater inflater;    
    private List<GiftsAdapter> mGridAdapters;
    private List<GiftGalleryGridManager<Gift>> mGalleryGridManagers;
    private ProgressBar mProgressBar;
    private TabHost mTabHost;
    
    private GiftsTabContent mGiftsTabContent;

    private GiftsCollection mGiftsCollection;

    public GiftsDialog(Context context, Activity activity) {
        super(context);
        mContext = context;
        mActivity = activity;
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setTitle(mContext.getResources().getText(R.string.gifts));
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.dialog_gifts, null, false);
        ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setContentView(v, p);

        mProgressBar = (ProgressBar)this.findViewById(R.id.prsGiftsLoading);
        
        mGridAdapters = new LinkedList<GiftsAdapter>();
        mGalleryGridManagers = new LinkedList<GiftGalleryGridManager<Gift>>();
        mGiftsCollection = new GiftsCollection();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mTabHost = (TabHost)findViewById(R.id.giftsTabHost);
        mGiftsTabContent = new GiftsTabContent();
        
        LocalActivityManager localActivityManager = new LocalActivityManager(mActivity, false);
        localActivityManager.dispatchCreate(savedInstanceState);
        mTabHost.setup(localActivityManager);
        
        update();        
    }

    private void update() {
        mProgressBar.setVisibility(View.VISIBLE);
        giftRequest = new GiftsRequest(mContext);
        giftRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                mGiftsCollection.add(Gift.parse(response));                
                post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                        mTabHost.addTab(mTabHost.newTabSpec(Integer.toString(Gift.ROMANTIC))
                                                        .setIndicator(mContext.getResources().getText(Gift.getTypeNameResId(Gift.ROMANTIC)))
                                                        .setContent(mGiftsTabContent));
                        mTabHost.addTab(mTabHost.newTabSpec(Integer.toString(Gift.FRIENDS))
                                                        .setIndicator(mContext.getResources().getText(Gift.getTypeNameResId(Gift.FRIENDS)))
                                                        .setContent(mGiftsTabContent));
                        mTabHost.addTab(mTabHost.newTabSpec(Integer.toString(Gift.PRESENT))
                                                        .setIndicator(mContext.getResources().getText(Gift.getTypeNameResId(Gift.PRESENT)))
                                                        .setContent(mGiftsTabContent));
                        for (GiftsAdapter adapter : mGridAdapters)
                            adapter.notifyDataSetChanged();
                        
                        for (GiftGalleryGridManager<Gift> manager : mGalleryGridManagers)
                            manager.update();
                    }
                });
            }

            @Override
            public void fail(int codeError,ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        // Toast.makeText(GiftsDialog.this,getString(R.string.),Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).exec();
    }

    class GiftsCollection {
        public int defaultType = Gift.FRIENDS;
        private List<Gift> mAllGifts = new LinkedList<Gift>();

        public void add(List<Gift> gifts) {
            mAllGifts.addAll(gifts);
        }

        public List<Gift> getGifts(int type) {
            List<Gift> result = new LinkedList<Gift>();
            for (Gift gift : mAllGifts) {
                if (gift.type == type) {
                    result.add(gift);
                }
            }

            return result;
        }

        public List<Gift> getGifts() {
            return getGifts(defaultType);
        }

    }

    class GiftsTabContent implements TabHost.TabContentFactory {

        @Override
        public View createTabContent(String tag) {            
            int type = Integer.parseInt(tag);            
            GridView gridView = new GridView(getContext());
            gridView.setAnimationCacheEnabled(false);
            gridView.setScrollingCacheEnabled(false);
            gridView.setScrollbarFadingEnabled(true);
            gridView.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
            gridView.setNumColumns(GIFTS_COLUMN);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent,View view,int position,long id) {
                    
                }
            });

            GiftGalleryGridManager<Gift> gridManager = new GiftGalleryGridManager<Gift>(getContext(), (LinkedList<Gift>) mGiftsCollection.getGifts(type));
            GiftsAdapter gridAdapter = new GiftsAdapter(getContext(), gridManager);
            gridView.setAdapter(gridAdapter);
            gridView.setOnScrollListener(gridManager);

            mGridAdapters.add(gridAdapter);
            mGalleryGridManagers.add(gridManager);
            
            return gridView;
        }

    }
}
