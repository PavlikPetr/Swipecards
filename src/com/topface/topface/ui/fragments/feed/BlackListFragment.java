package com.topface.topface.ui.fragments.feed;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.BlackListItem;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BlackListDeleteRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.SimpleApiHandler;
import com.topface.topface.ui.adapters.BlackListAdapter;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Черный список. Сюда попадают заблокированые пользователи, отныне от них не приходит никакая активность
 */
public class BlackListFragment extends FeedFragment<BlackListItem> implements View.OnClickListener{

    private static final int BLACK_LIST_DELETE_BUTTON = 0;

    private LinkedList<Integer> mDeleteList = new LinkedList<Integer>();

    @Override
    protected int getLayout() {
        return R.layout.ac_feed_black_list;
    }

    @Override
    protected Drawable getBackIcon() {
        return null;
    }

    @Override
    protected int getTitle() {
        return R.string.black_list_title;
    }

    @Override
    protected int getTypeForGCM() {
        return GCMUtils.GCM_TYPE_UNKNOWN;
    }

    @Override
    protected int getTypeForCounters() {
        return -1;
    }

    @Override
    protected BlackListAdapter getNewAdapter() {
        return new BlackListAdapter(getActivity().getApplicationContext(), getUpdaterCallback());
    }

    @Override
    protected FeedListData<BlackListItem> getFeedList(JSONObject response) {
        return new FeedListData<BlackListItem>(response, BlackListItem.class);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.BLACK_LIST;
    }

    @Override
    protected int getEmptyFeedText() {
        return R.string.black_list_empty;
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    protected void initNavigationBar(View view) {
        // Navigation bar
        View backButton = view.findViewById(R.id.btnNavigationBack);
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               getActivity().finish();
            }
        });

        final Button editButton = (Button) view.findViewById(R.id.btnEditList);
        editButton.setVisibility(View.VISIBLE);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditList(editButton);
            }
        });
    }

    private void toggleEditList(Button editButton) {
        editButton.setText(((BlackListAdapter) getAdapter()).isEditMode() ?
            R.string.general_edit_button :
            R.string.general_save_button
        );
        final BlackListAdapter adapter = ((BlackListAdapter) mListAdapter);
        //Удаляем отмеченные элементы, отправляя запрос на сервер
        deleteMarkedItems(adapter);
        //Переключаем адаптер
        adapter.toggleEditMode();
    }

    private void deleteMarkedItems(final BlackListAdapter adapter) {
        ArrayList<Integer> markedForDelete = adapter.getMarkedForDelete();
        if (adapter.isEditMode() && markedForDelete.size() > 0) {
            mLockView.setVisibility(View.VISIBLE);
            new BlackListDeleteRequest(markedForDelete, getActivity())
                    .callback(new SimpleApiHandler() {
                        @Override
                        public void success(ApiResponse response) {
                            adapter.removeDeleted();
                        }

                        @Override
                        public void always(ApiResponse response) {
                            super.always(response);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mLockView.setVisibility(View.GONE);
                                }
                            });
                        }
                    })
                    .exec();
        }
    }

    @Override
    protected DialogInterface.OnClickListener getLongTapActionsListener(final int id) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case BLACK_LIST_DELETE_BUTTON:
                        mLockView.setVisibility(View.VISIBLE);
                        onRemoveFromBlackList(id);
                        break;
                }
            }
        };
    }

    private void onRemoveFromBlackList(final int position) {
        mLockView.setVisibility(View.VISIBLE);
        new BlackListDeleteRequest(getItem(position).user.id, getActivity())
                .callback(new SimpleApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getListAdapter().removeItem(position);
                            }
                        });
                    }

                    @Override
                    public void always(ApiResponse response) {
                        mLockView.setVisibility(View.GONE);
                    }

                }).exec();
    }

    @Override
    protected String[] getLongTapActions() {
        if (editButtonsNames == null) {
            editButtonsNames = new String[]{ getString(R.string.black_list_delete) };
        }
        return editButtonsNames;
    }

    @Override
    protected void initDoubleButton(View view) {}

    @Override
    protected AdapterView.OnItemClickListener getOnItemClickListener() {
        final AdapterView.OnItemClickListener baseListener = super.getOnItemClickListener();
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BlackListAdapter adapter = (BlackListAdapter) getAdapter();
                if (adapter.isEditMode()) {
                    //Добавляем в список пользователей для удаления
                    FeedItem item = (FeedItem) parent.getItemAtPosition(position);
                    adapter.toggleItemDeleteMark((int) id);
                } else {
                    baseListener.onItemClick(parent, view, position, id);
                }
            }
        };
    }

    @Override
    protected void initFloatBlock(ViewGroup view) {}
}
