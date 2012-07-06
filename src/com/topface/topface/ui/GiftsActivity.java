package com.topface.topface.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import com.topface.topface.R;
import com.topface.topface.data.Gift;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.GiftsRequest;
import com.topface.topface.ui.adapters.GiftsAdapter;
import com.topface.topface.ui.adapters.GiftsAdapter.ViewHolder;
import com.topface.topface.utils.GiftGalleryManager;
import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.Toast;

public class GiftsActivity extends Activity {

    public static final int INTENT_REQUEST_GIFT = 111;
    public static final String INTENT_GIFT_ID = "gift_id";
    public static final String INTENT_GIFT_URL = "gift_url";
    
    public static final int GIFTS_COLUMN_PORTRAIT = 3;
    public static final int GIFTS_COLUMN_LANDSCAPE = 5;
    public static int dialogWidth = 0;

    private GiftsRequest giftRequest;

    private List<GiftsAdapter> mGridAdapters;
    private List<GiftGalleryManager<Gift>> mGalleryManagers;
    private ProgressBar mProgressBar;
    private TabHost mTabHost;
    private HashMap<Integer, GridView> mGridViews;
//    private GridView mGridView;
//    private LinkedList<Gift> mCurrentGifts;
    
    private RadioButton mBtnLeft;
    private RadioButton mBtnMiddle;
    private RadioButton mBtnRight;
    
    private GiftsTabContent mGiftsTabContent;

    private GiftsCollection mGiftsCollection;    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);              
        this.setContentView(R.layout.ac_gifts);
        
        mProgressBar = (ProgressBar)this.findViewById(R.id.prsGiftsLoading);
        
        mGridAdapters = new LinkedList<GiftsAdapter>();
        mGalleryManagers = new LinkedList<GiftGalleryManager<Gift>>();
        mGiftsCollection = new GiftsCollection();
        mGridViews = new HashMap<Integer, GridView>();
        
        mTabHost = (TabHost)findViewById(R.id.giftsTabHost);
        mGiftsTabContent = new GiftsTabContent();
        
        // localmanager for tabhost
        LocalActivityManager localActivityManager = new LocalActivityManager(this, false);
        localActivityManager.dispatchCreate(savedInstanceState);
        mTabHost.setup(localActivityManager);        
        
        // init triple button
        mBtnLeft = (RadioButton) findViewById(R.id.trplLeft);
        mBtnMiddle = (RadioButton) findViewById(R.id.trplMiddle);
        mBtnRight = (RadioButton) findViewById(R.id.trplRight);
        
        mBtnLeft.setText(Gift.getTypeNameResId(Gift.ROMANTIC));
        mBtnMiddle.setText(Gift.getTypeNameResId(Gift.FRIENDS));
        mBtnRight.setText(Gift.getTypeNameResId(Gift.PRESENT)); 
        
        mBtnLeft.setChecked(true);
        
        update();        
    }

    private void update() {
        mProgressBar.setVisibility(View.VISIBLE);
        giftRequest = new GiftsRequest(this);
        giftRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                mGiftsCollection.add(Gift.parse(response));
                mGridViews.put(Gift.ROMANTIC,createGridView(Gift.ROMANTIC));
                mGridViews.put(Gift.FRIENDS,createGridView(Gift.FRIENDS));
                mGridViews.put(Gift.PRESENT,createGridView(Gift.PRESENT));
//                mCurrentGifts = new LinkedList<Gift>();
//                mGridView = createGridView(Gift.ROMANTIC);
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {              
                        
                        mTabHost.addTab(mTabHost.newTabSpec(Integer.toString(Gift.ROMANTIC))
                                                        .setIndicator(getResources().getText(Gift.getTypeNameResId(Gift.ROMANTIC)))
                                                        .setContent(mGiftsTabContent));                        
                        mTabHost.addTab(mTabHost.newTabSpec(Integer.toString(Gift.FRIENDS))
                                                        .setIndicator(getResources().getText(Gift.getTypeNameResId(Gift.FRIENDS)))
                                                        .setContent(mGiftsTabContent));
                        mTabHost.addTab(mTabHost.newTabSpec(Integer.toString(Gift.PRESENT))
                                                        .setIndicator(getResources().getText(Gift.getTypeNameResId(Gift.PRESENT)))
                                                        .setContent(mGiftsTabContent));
                                                
                        mBtnLeft.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mTabHost.setCurrentTabByTag(Integer.toString(Gift.ROMANTIC));
//                                mGridView = createGridView(Gift.ROMANTIC);
//                                for (GiftsAdapter adapter : mGridAdapters)
//                                    adapter.notifyDataSetChanged();
//                                
//                                for (GiftGalleryManager<Gift> manager : mGalleryManagers) {
//                                    manager.update();                            
//                                }
                            }
                        });
                                                
                        mBtnMiddle.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mTabHost.setCurrentTabByTag(Integer.toString(Gift.FRIENDS));
//                                mGridView = createGridView(Gift.FRIENDS);
//                                for (GiftsAdapter adapter : mGridAdapters)
//                                    adapter.notifyDataSetChanged();
//                                
//                                for (GiftGalleryManager<Gift> manager : mGalleryManagers) {
//                                    manager.update();                            
//                                }
                            }
                        });
                                                
                        mBtnRight.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mTabHost.setCurrentTabByTag(Integer.toString(Gift.PRESENT));
//                                mGridView = createGridView(Gift.PRESENT);
//                                for (GiftsAdapter adapter : mGridAdapters)
//                                    adapter.notifyDataSetChanged();
//                                
//                                for (GiftGalleryManager<Gift> manager : mGalleryManagers) {
//                                    manager.update();                            
//                                }
                            }
                        });                        
                                                
                        if(mBtnMiddle.isChecked()) mTabHost.setCurrentTabByTag(Integer.toString(Gift.FRIENDS));;
                        if(mBtnRight.isChecked()) mTabHost.setCurrentTabByTag(Integer.toString(Gift.PRESENT));;

                        mProgressBar.setVisibility(View.GONE);
                        for (GiftsAdapter adapter : mGridAdapters)
                            adapter.notifyDataSetChanged();
                        
                        for (GiftGalleryManager<Gift> manager : mGalleryManagers) {
                            manager.update();                            
                        }
                    }
                });
            }

            @Override
            public void fail(int codeError,ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GiftsActivity.this,GiftsActivity.this.getString(R.string.general_data_error),Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).exec();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (GiftsAdapter adapter : mGridAdapters) {
            adapter.release();
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
//        for (GiftsAdapter adapter : mGridAdapters) {
//            adapter.release();
//        }
    }
    
    @Override
    protected void onPause() {
    	// TODO Auto-generated method stub
    	super.onPause();
    }
    
    private GridView createGridView(int type) {
//        mCurrentGifts.clear();
//        mCurrentGifts.addAll(mGiftsCollection.getGifts(type));
//        if (mGridView  == null) {
            GridView gridView = new GridView(GiftsActivity.this);
            gridView.setAnimationCacheEnabled(false);
            gridView.setScrollingCacheEnabled(false);
            gridView.setScrollbarFadingEnabled(true);
            gridView.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
            int columns = this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? GIFTS_COLUMN_PORTRAIT : GIFTS_COLUMN_LANDSCAPE; 
            gridView.setNumColumns(columns);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent,View view,int position,long id) {
                    Intent intent = GiftsActivity.this.getIntent();
                    if (view.getTag() instanceof ViewHolder) {
                        ViewHolder holder = ((ViewHolder)view.getTag());
                        intent.putExtra(INTENT_GIFT_ID, holder.mGift.id);
                        intent.putExtra(INTENT_GIFT_URL, holder.mGift.link);                    
    
                        GiftsActivity.this.setResult(RESULT_OK, intent);
                        GiftsActivity.this.finish();
                    }
                }
            });
    
            GiftGalleryManager<Gift> gridManager = new GiftGalleryManager<Gift>(GiftsActivity.this, (LinkedList<Gift>) mGiftsCollection.getGifts(type));//mCurrentGifts);
            GiftsAdapter gridAdapter = new GiftsAdapter(GiftsActivity.this, gridManager);
            gridView.setAdapter(gridAdapter);
            gridView.setOnScrollListener(gridManager);
    
            mGridAdapters.add(gridAdapter);
            mGalleryManagers.add(gridManager);
            
            return gridView;
//        } else {
//            return mGridView;
//        }
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
//            Debug.log(this, "OLOLO " + tag);
            return mGridViews.get(type);
//            return mGridView;
        }

    }    
}
