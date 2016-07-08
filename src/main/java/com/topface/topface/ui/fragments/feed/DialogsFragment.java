package com.topface.topface.ui.fragments.feed;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.google.gson.reflect.TypeToken;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.History;
import com.topface.topface.data.Options;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.data.leftMenu.NavigationState;
import com.topface.topface.data.leftMenu.WrappedNavigationData;
import com.topface.topface.databinding.AppOfTheDayLayoutBinding;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteDialogsRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.statistics.FlurryOpenEvent;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.DialogListAdapter;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.adapter_utils.IInjectViewFactory;
import com.topface.topface.utils.adapter_utils.IViewInjectRule;
import com.topface.topface.utils.adapter_utils.InjectViewBucket;
import com.topface.topface.utils.config.FeedsCache;
import com.topface.topface.utils.gcmutils.GCMUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;

@FlurryOpenEvent(name = DialogsFragment.PAGE_NAME)
public class DialogsFragment extends FeedFragment<FeedDialog> {

    public static final String PAGE_NAME = "Dialogs";

    @Inject
    NavigationState mNavigationState;

    public DialogsFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.get().inject(this);
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getListAdapter() != null) {
            boolean isVip = App.get().getProfile().premium;
            for (FeedDialog feed : getListAdapter().getData()) {
                if (feed.type == FeedDialog.MESSAGE_AUTO_REPLY && isVip) {
                    getListAdapter().getData().clear();
                    updateData(false, false);
                    break;
                }
            }
        }
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FeedAdapter adapter = getListAdapter();
        final Options.AppOfTheDay appOfTheDay = App.get().getOptions().appOfTheDay;
        if (adapter != null && appOfTheDay != null) {
            InjectViewBucket bucket = new InjectViewBucket(new IInjectViewFactory() {
                @Override
                public View construct() {
                    AppOfTheDayLayoutBinding binding = DataBindingUtil.inflate((LayoutInflater) App.getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE), R.layout.app_of_the_day_layout, null, true);
                    binding.setClick(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Utils.goToUrl(getActivity(), appOfTheDay.targetUrl);
                        }
                    });
                    binding.setAppOfTheDay(appOfTheDay);
                    binding.executePendingBindings();
                    return binding.getRoot();
                }
            });
            bucket.addFilter(new IViewInjectRule() {
                @Override
                public boolean isNeedInject(int pos) {
                    return pos == 0;
                }
            });
            adapter.registerViewBucket(bucket);
        }
    }

    @Override
    protected Type getFeedListDataType() {
        return new TypeToken<FeedList<FeedDialog>>() {
        }.getType();
    }

    @Override
    protected Class getFeedListItemClass() {
        return FeedDialog.class;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.settings_messages);
    }

    @Override
    protected void makeAllItemsRead() {
    }

    @Override
    protected DialogListAdapter createNewAdapter() {
        return new DialogListAdapter(getActivity().getApplicationContext(), getUpdaterCallback());
    }

    @Override
    protected void makeItemReadWithFeedId(String id) {
        //feed will be marked read in another method
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.DIALOGS;
    }

    @Override
    protected void initLockedFeed(View inflated, int errorCode) {
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
        inflated.findViewById(R.id.btnBuyVip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(PurchasesActivity.createBuyingIntent("EmptyDialogs", App.from(getActivity()).getOptions().topfaceOfferwallRedirect));
            }
        });

        inflated.findViewById(R.id.btnStartRate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNavigationState.emmitNavigationState(new WrappedNavigationData(new LeftMenuSettingsData(FragmentIdData.DATING), WrappedNavigationData.SELECT_EXTERNALY));
            }
        });
    }

    /**
     * Этот метод используется для получения id элементов ленты при удалении.
     * Но в диалогах у нас работает не так как в остальных лентах
     * и приходится вручную пробрасывать id юзеров вместо id итема
     */
    @Override
    protected List<String> getSelectedFeedIds(FeedAdapter<FeedDialog> adapter) {
        return adapter.getSelectedUsersStringIds();
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_dialogs;
    }

    @Override
    protected int[] getTypesForGCM() {
        return new int[]{GCMUtils.GCM_TYPE_DIALOGS, GCMUtils.GCM_TYPE_MESSAGE, GCMUtils.GCM_TYPE_GIFT};
    }

    @Override
    protected int getFeedType() {
        return CountersManager.DIALOGS;
    }

    @NotNull
    @Override
    protected FeedsCache.FEEDS_TYPE getFeedsType() {
        return FeedsCache.FEEDS_TYPE.DATA_DIALOGS_FEEDS;
    }

    @Override
    protected DeleteAbstractRequest getDeleteRequest(List<String> ids) {
        return new DeleteDialogsRequest(ids, getActivity());
    }

    @Override
    protected int getUnreadCounter() {
        // dialogs are not auto-read
        return mCountersData.getDialogs();
    }

    @Override
    protected String getGcmUpdateAction() {
        return GCMUtils.GCM_DIALOGS_UPDATE;
    }

    @Override
    protected boolean considerDublicates(FeedDialog first, FeedDialog second) {
        return first.user == null ? second.user == null : first.user.id == second.user.id;
    }

    @Override
    protected void onChatActivityResult(int resultCode, Intent data) {
        super.onChatActivityResult(resultCode, data);
        if (data != null) {
            History history = data.getParcelableExtra(ChatActivity.LAST_MESSAGE);
            int userId = data.getIntExtra(ChatActivity.LAST_MESSAGE_USER_ID, -1);
            if (history != null && userId > 0) {
                if (getListAdapter() instanceof DialogListAdapter) {
                    DialogListAdapter adapter = (DialogListAdapter) getListAdapter();
                    if (adapter != null) {
                        FeedDialog dialog;
                        for (int i = 0; i < adapter.getCount(); i++) {
                            dialog = adapter.getItem(i);
                            if (dialog != null && dialog.user != null && dialog.user.id == userId) {
                                adapter.replacePreview(i, history);
                            }
                        }
                    }
                }
            }
        }
    }
}
