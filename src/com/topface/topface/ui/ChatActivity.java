package com.topface.topface.ui;

import java.util.LinkedList;

import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.Static;
import com.topface.topface.data.Confirmation;
import com.topface.topface.data.History;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.CoordinatesRequest;
import com.topface.topface.requests.HistoryRequest;
import com.topface.topface.requests.MessageRequest;
import com.topface.topface.requests.SendGiftRequest;
import com.topface.topface.ui.adapters.ChatListAdapter;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.ui.views.SwapControl;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.GeoLocationManager;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.GeoLocationManager.LocationProviderType;
import com.topface.topface.utils.Http;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends Activity implements View.OnClickListener, LocationListener {
    // Data
    private int mUserId;
    private String mUserAvatarUrl;
    private int mAvatarWidth;
    private boolean mProfileInvoke;
    private boolean mIsAddPanelOpened;
    private ListView mListView;
    private ChatListAdapter mAdapter;
    private LinkedList<History> mHistoryList;
    private EditText mEditBox;
    private TextView mHeaderTitle;
//    private ProgressBar mProgressBar;
    private LockerView mLoadinLocker;
    
    private MessageRequest messageRequest;
    private HistoryRequest historyRequest;
    private SwapControl mSwapControl;
    private static ProgressDialog mProgressDialog;
    private boolean mLocationDetected = false;
    
    // Constants
    private static final int LIMIT = 50; // !!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_USER_URL = "user_url";
    public static final String INTENT_USER_NAME = "user_name";
    public static final String INTENT_PROFILE_INVOKE = "profile_invoke";
    private static final int DIALOG_GPS_ENABLE_NO_AGPS_ID = 1;
    private static final int DIALOG_LOCATION_PROGRESS_ID = 3;
    private static long LOCATION_PROVIDER_TIMEOUT = 10000;
    
    //Managers
    private GeoLocationManager mGeoManager = null;
    
    boolean bibi;
    //---------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_chat);
        Debug.log(this, "+onCreate");

        // Data
        mHistoryList = new LinkedList<History>();

        // Swap Control
        mSwapControl = ((SwapControl)findViewById(R.id.swapFormView));

        // Title Header
        mHeaderTitle = ((TextView)findViewById(R.id.tvHeaderTitle));

        // Progress
        mLoadinLocker = (LockerView)findViewById(R.id.llvChatLoading);

        // Params
        mUserId = getIntent().getIntExtra(INTENT_USER_ID, -1);
        mUserAvatarUrl = getIntent().getStringExtra(INTENT_USER_URL);
        mProfileInvoke = getIntent().getBooleanExtra(INTENT_PROFILE_INVOKE, false);
        mHeaderTitle.setText(getIntent().getStringExtra(INTENT_USER_NAME));        
        mAvatarWidth = getResources().getDrawable(R.drawable.chat_avatar_frame).getIntrinsicWidth();
        
        // Profile Button
        View btnProfile = findViewById(R.id.btnChatProfile);
        btnProfile.setVisibility(View.VISIBLE);
        btnProfile.setOnClickListener(this);

        // Add Button        
        ((Button)findViewById(R.id.btnChatAdd)).setOnClickListener(this);

        // Gift Button
        ((Button)findViewById(R.id.btnChatGift)).setOnClickListener(this);

        // Place Button
        ((Button)findViewById(R.id.btnChatPlace)).setOnClickListener(this);

        // Map Button
        ((Button)findViewById(R.id.btnChatMap)).setOnClickListener(this);

        // Edit Box
        mEditBox = (EditText)findViewById(R.id.edChatBox);
        mEditBox.setOnEditorActionListener(mEditorActionListener);

        // ListView
        mListView = (ListView)findViewById(R.id.lvChatList);

        // Adapter
        mAdapter = new ChatListAdapter(getApplicationContext(), mUserId, mHistoryList);
        mAdapter.setOnAvatarListener(this);
        mListView.setAdapter(mAdapter);

        Http.avatarOwnerPreloading();

        update();
    }
    //---------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        if (messageRequest != null)
            messageRequest.cancel();
        if (historyRequest != null)
            historyRequest.cancel();

        release();
        Data.userAvatar = null;
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }
    //---------------------------------------------------------------------------
    private void update() {
        mLoadinLocker.setVisibility(View.VISIBLE);
        historyRequest = new HistoryRequest(getApplicationContext());
        historyRequest.userid = mUserId;
        historyRequest.limit = LIMIT;
        historyRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
            	Data.userAvatar = Utils.getRoundedBitmap(Http.bitmapLoader(mUserAvatarUrl), mAvatarWidth, mAvatarWidth);
                LinkedList<History> dataList = History.parse(response);
                mAdapter.setDataList(dataList);
                post(new Runnable() {
                    @Override
                    public void run() {
                        mLoadinLocker.setVisibility(View.GONE);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChatActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        mLoadinLocker.setVisibility(View.GONE);
                    }
                });
            }
        }).exec();
    }
    //---------------------------------------------------------------------------
    private void release() {
        mEditBox = null;
        mListView = null;
        if (mAdapter != null)
            mAdapter.release();
        mAdapter = null;
        mHistoryList = null;
    }
    //---------------------------------------------------------------------------
    @Override
    public void onClick(View v) {
    	if (v instanceof ImageView) {
    		if (v.getTag() instanceof History) {
    			History history = (History)v.getTag();
				Intent intent = new Intent(this, GeoMapActivity.class);
				intent.putExtra(GeoMapActivity.INTENT_LATITUDE_ID, history.latitude);
				intent.putExtra(GeoMapActivity.INTENT_LONGITUDE_ID, history.longitude);
				startActivity(intent);
				return;
    		}
    	} 
    	
        switch (v.getId()) {
            case R.id.btnChatAdd: {
                if (mIsAddPanelOpened)
                    mSwapControl.snapToScreen(0);
                else
                    mSwapControl.snapToScreen(1);
                mIsAddPanelOpened = !mIsAddPanelOpened;
            } break;
            case R.id.btnChatGift: {
                startActivityForResult(new Intent(this, GiftsActivity.class), GiftsActivity.INTENT_REQUEST_GIFT);
            } break;
            case R.id.btnChatPlace: {            	
            	sendUserCurrentLocation();
//                Toast.makeText(ChatActivity.this, "Place", Toast.LENGTH_SHORT).show();
            } break;
            case R.id.btnChatMap: {
            	startActivityForResult(new Intent(this, GeoMapActivity.class), GeoMapActivity.INTENT_REQUEST_GEO);            	
//                Toast.makeText(ChatActivity.this, "Map", Toast.LENGTH_SHORT).show();
            } break;
            default: {
                if (mProfileInvoke) {
                    finish();
                    return;
                }
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra(ProfileActivity.INTENT_USER_ID, mUserId);
                intent.putExtra(ProfileActivity.INTENT_CHAT_INVOKE, true);
                intent.putExtra(ProfileActivity.INTENT_USER_NAME, mHeaderTitle.getText());
                startActivity(intent);
            } break;
        }
    }
    //---------------------------------------------------------------------------
    private TextView.OnEditorActionListener mEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v,int actionId,KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                final String text = v.getText().toString();

                if (text == null || text.length() == 0)
                    return false;

                mLoadinLocker.setVisibility(View.VISIBLE);

                messageRequest = new MessageRequest(ChatActivity.this.getApplicationContext());
                messageRequest.message = mEditBox.getText().toString();
                messageRequest.userid = mUserId;
                messageRequest.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                    	final Confirmation confirm = Confirmation.parse(response);
                        runOnUiThread(new Runnable() {                        	
                            @Override
                            public void run() {
                                if (confirm.completed) {
	                                History history = new History();
	                                history.code = 0;
	                                history.gift = 0;
	                                history.owner_id = CacheProfile.uid;
	                                history.created = System.currentTimeMillis();
	                                history.text = text;
	                                history.type = History.MESSAGE;
	                                mAdapter.addSentMessage(history);
	                                mAdapter.notifyDataSetChanged();
	                                mEditBox.getText().clear();
	                                mLoadinLocker.setVisibility(View.GONE);
	                                
	                                InputMethodManager imm = (InputMethodManager)getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	                                imm.hideSoftInputFromWindow(mEditBox.getWindowToken(), 0);
                                } else {
                                	Toast.makeText(ChatActivity.this, getString(R.string.general_server_error), Toast.LENGTH_SHORT).show();
                                }	
                            }
                        });
                    }
                    @Override
                    public void fail(int codeError,ApiResponse response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ChatActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                                mLoadinLocker.setVisibility(View.GONE);
                            }
                        });
                    }
                }).exec();
                return true;
            }
            return false;
        }
    };
    //---------------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
        	if (requestCode == GiftsActivity.INTENT_REQUEST_GIFT) {
	            mLoadinLocker.setVisibility(View.VISIBLE);
	            Bundle extras = data.getExtras();
	            final int id = extras.getInt(GiftsActivity.INTENT_GIFT_ID);            
	            final String url = extras.getString(GiftsActivity.INTENT_GIFT_URL);
	            Debug.log(this, "id:" + id + " url:" + url);
	            SendGiftRequest sendGift = new SendGiftRequest(getApplicationContext());
	            sendGift.giftId = id;
	            sendGift.userId = mUserId;
	            if (mIsAddPanelOpened)
                    mSwapControl.snapToScreen(0);                
                mIsAddPanelOpened = false;
	            sendGift.callback(new ApiHandler() {
	                @Override
	                public void success(ApiResponse response) throws NullPointerException {
	                    SendGiftAnswer answer = SendGiftAnswer.parse(response);
	                    CacheProfile.power = answer.power;
	                    CacheProfile.money = answer.money;
	                    Debug.log(ChatActivity.this, "power:" + answer.power + " money:" + answer.money);
	                    runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {              
	                        	History history = new History();
	                            history.code = 0;
	                            history.gift = id;
	                            history.owner_id = CacheProfile.uid;
	                            history.created = System.currentTimeMillis();
	                            history.text = Static.EMPTY;
	                            history.type = History.GIFT;
	                            history.link = url;
	                            mAdapter.addSentMessage(history);
	                            mAdapter.notifyDataSetChanged();
	                            mLoadinLocker.setVisibility(View.GONE);
	                        }
	                    });
	                }
	                
	                @Override
	                public void fail(int codeError,final ApiResponse response) throws NullPointerException {
	                	runOnUiThread(new Runnable() {
	                        @Override
	                        public void run() {
	                            if(response.code==ApiResponse.PAYMENT)
	                                startActivity(new Intent(getApplicationContext(), BuyingActivity.class));
	                        }
	                    });
	                }
	            }).exec();
        	} else if (requestCode == GeoMapActivity.INTENT_REQUEST_GEO) {
        		Bundle extras = data.getExtras();
        		final double latitude = extras.getDouble(GeoMapActivity.INTENT_LATITUDE_ID);
        		final double longitude = extras.getDouble(GeoMapActivity.INTENT_LONGITUDE_ID);
//        		final String address = extras.getString(GeoMapActivity.INTENT_ADDRESS_ID);        		
        		
        		CoordinatesRequest coordRequest = new CoordinatesRequest(getApplicationContext());
        		coordRequest.userid = mUserId;
        		coordRequest.latitude = latitude;
        		coordRequest.longitude = longitude;
        		mLoadinLocker.setVisibility(View.VISIBLE);
        		coordRequest.callback(new ApiHandler() {
        			
        			@Override
        			public void success(ApiResponse response) throws NullPointerException {
        				final Confirmation confirm = Confirmation.parse(response);
        				runOnUiThread(new Runnable() {                        	
                            @Override
                            public void run() {
                            	mLoadinLocker.setVisibility(View.GONE);
                            	if (confirm.completed) {
                            		History history = new History();
        					        history.type = History.LOCATION;
        					        history.currentLocation = false;
                                    history.latitude = latitude;
                                    history.longitude = longitude;
                                    mAdapter.addSentMessage(history);
                                    mAdapter.notifyDataSetChanged();
                            		
//                					Toast.makeText(ChatActivity.this, latitude + " " + longitude, Toast.LENGTH_SHORT).show();			
                				} else {
                					Toast.makeText(ChatActivity.this, R.string.general_server_error, Toast.LENGTH_SHORT).show();
                				}
                            }
        				});
        			}
        			
        			@Override
        			public void fail(int codeError, ApiResponse response)
        					throws NullPointerException {
        				runOnUiThread(new Runnable() {                        	
                            @Override
                            public void run() {
                            	Toast.makeText(ChatActivity.this, R.string.general_server_error, Toast.LENGTH_SHORT).show();
                            }
        				});
        			}
        		}).exec();
        		
        		if (mIsAddPanelOpened)
                    mSwapControl.snapToScreen(0);                
                mIsAddPanelOpened = false;
        	}
        }
    }
    //---------------------------------------------------------------------------
    private void sendUserCurrentLocation() {
    	mLocationDetected = false;    	
    	
    	if (mGeoManager == null)
    		mGeoManager = new GeoLocationManager(getApplicationContext());
    	
    	if(mGeoManager.availableLocationProvider(LocationProviderType.AGPS)) {
    		mGeoManager.setLocationListener(LocationProviderType.AGPS, this);
    		showDialog(DIALOG_LOCATION_PROGRESS_ID);
    	} else if (mGeoManager.availableLocationProvider(LocationProviderType.GPS)){
    		mGeoManager.setLocationListener(LocationProviderType.GPS, this);
    		(new CountDownTimer(LOCATION_PROVIDER_TIMEOUT,LOCATION_PROVIDER_TIMEOUT) {
    			
    			@Override
    			public void onTick(long millisUntilFinished) { }
    			
    			@Override
    			public void onFinish() {
    				synchronized (mGeoManager) {
    					if (!mLocationDetected) {						
    						mGeoManager.removeLocationListener(ChatActivity.this);
    						mProgressDialog.dismiss();    
    						Toast.makeText(ChatActivity.this, R.string.chat_toast_fail_location, Toast.LENGTH_SHORT).show();
    					}
    				}
    			}
    		}).start();	
    		showDialog(DIALOG_LOCATION_PROGRESS_ID);
    	} else {
    		showDialog(DIALOG_GPS_ENABLE_NO_AGPS_ID);
    	}        
    }
    //---------------------------------------------------------------------------
	@Override
	public void onLocationChanged(Location location) {
//		Debug.log(this, location.getLatitude() + " / " + location.getLongitude());
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();
		CoordinatesRequest coordRequest = new CoordinatesRequest(getApplicationContext());
		coordRequest.userid = mUserId;
		coordRequest.latitude = latitude;
		coordRequest.longitude = longitude;
		coordRequest.callback(new ApiHandler() {
			
			@Override
			public void success(ApiResponse response) throws NullPointerException {
				final Confirmation confirm = Confirmation.parse(response);
//				final String address = mGeoManager.getLocationAddress(latitude, longitude);
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {						
						if (confirm.completed) {				
							if (mIsAddPanelOpened)
					            mSwapControl.snapToScreen(0);                
					        mIsAddPanelOpened = false;

					        History history = new History();
					        history.type = History.CURRENT_LOCATION;
					        history.currentLocation = true;
                            history.latitude = latitude;
                            history.longitude = longitude;
                            mAdapter.addSentMessage(history);
                            mAdapter.notifyDataSetChanged();
                            
//					        Toast.makeText(ChatActivity.this, history.latitude + " " + history.longitude, Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(ChatActivity.this, R.string.general_server_error, Toast.LENGTH_SHORT).show();
						}				
						mProgressDialog.dismiss();
					}
				});
				
			}
			
			@Override
			public void fail(int codeError, ApiResponse response)
					throws NullPointerException {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						mProgressDialog.dismiss();
						Toast.makeText(ChatActivity.this, R.string.general_server_error, Toast.LENGTH_SHORT).show();
					}
				});
				
			}
		}).exec();
				
        mGeoManager.removeLocationListener(this);
        mLocationDetected = true;        
	}
	@Override
	public void onProviderDisabled(String provider) { }
	@Override
	public void onProviderEnabled(String provider) { }
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) { }
	//---------------------------------------------------------------------------
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder;
		AlertDialog alert;
		switch(id) {
		case DIALOG_GPS_ENABLE_NO_AGPS_ID:
			builder = new AlertDialog.Builder(this);  
			builder.setMessage(this.getText(R.string.chat_dialog_gps))  
			     .setCancelable(false)  
			     .setPositiveButton(this.getText(R.string.chat_dialog_btn_gps_settings),  
			          new DialogInterface.OnClickListener(){  
			          public void onClick(DialogInterface dialog, int id){  
			        	  Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
			              startActivity(gpsOptionsIntent);  
			          }  
			     });  
			     builder.setNegativeButton(this.getText(R.string.chat_dialog_btn_gps_cancel),
			          new DialogInterface.OnClickListener(){  
			          public void onClick(DialogInterface dialog, int id){  
			               dialog.cancel();
			          }  
			     });  
			alert = builder.create();  
			return alert;			
		case DIALOG_LOCATION_PROGRESS_ID:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setMessage(this.getText(R.string.map_location_progress));
			return mProgressDialog;
		default:
			return super.onCreateDialog(id);
		
		}
	}
    //---------------------------------------------------------------------------
}
