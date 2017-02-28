package com.topface.topface.promo.dialogs;

import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.data.leftMenu.NavigationState;
import com.topface.topface.data.leftMenu.WrappedNavigationData;
import com.topface.topface.databinding.PromoExpressMessagesBinding;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.views.toolbar.view_models.BackToolbarViewModel;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.extensions.ResourceExtensionKt;
import com.topface.topface.utils.rx.RxUtils;

import java.util.ArrayList;
import java.util.Random;

import rx.Subscription;
import rx.functions.Action1;

import static com.topface.topface.R.drawable.fake_girl1;

public class PromoExpressMessages extends PromoDialog {

    public final static String TAG = "promo_express_messages";

    private final static String POPUP_NAME = "promo.express.messages";

    private static final int AVATARS_ID_ARRAY_LENGTH = 3;

    private int mCurrentPosition = Integer.MAX_VALUE;

    private Random mRandom;

    private NavigationState mNavigationState;
    private TopfaceAppState mAppState;
    private Subscription mBalanceSubscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNavigationState = App.getAppComponent().navigationState();
        mAppState = App.getAppComponent().appState();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBalanceSubscription = mAppState
                .getObservable(BalanceData.class)
                .subscribe(new Action1<BalanceData>() {
                    @Override
                    public void call(BalanceData balanceData) {
                        if (balanceData.premium) {
                            // хак для открытия диалогов после покупки вип на случай когда протух фрагмент под активити покупок
                            PromoExpressMessages.this.closeFragment();
                            mNavigationState.emmitNavigationState(new WrappedNavigationData(new LeftMenuSettingsData(FragmentIdData.TABBED_DIALOGS), WrappedNavigationData.SELECT_EXTERNALY));
                        }
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        RxUtils.safeUnsubscribe(mBalanceSubscription);
    }

    @Override
    public Options.PromoPopupEntity getPremiumEntity() {
        return App.get().getOptions().premiumMessages;
    }

    @Override
    protected String getPopupName() {
        return POPUP_NAME;
    }

    @Override
    protected int getDeleteButtonText() {
        return R.string.general_delete_messages;
    }

    @Override
    protected String getMessage() {
        Options.PromoPopupEntity premiumEntity = getPremiumEntity();
        int count = premiumEntity.getCount();
        return Utils.replaceDashWithHyphen(Utils.getQuantityString(getPluralForm(), count, count));
    }

    @Override
    protected int getPluralForm() {
        return R.plurals.popup_vip_messages;
    }

    @Override
    protected void deleteMessages() {
    }

    @Override
    protected int getDialogStyleResId() {
        return R.style.Theme_Topface_NoActionBar_DatingLockPopup;
    }

    @Override
    public boolean isUnderActionBar() {
        return false;
    }

    @Override
    public String getMainTag() {
        Options.PromoPopupEntity promoPopupEntity = getPremiumEntity();
        int popupVersion = 0;
        if (promoPopupEntity != null) {
            popupVersion = promoPopupEntity.getPopupVersion();
        }
        return "promo.key31".concat(popupVersion > 0 ? ".v".concat(String.valueOf(popupVersion)) : "");
    }

    @Override
    public int getDialogLayoutRes() {
        return R.layout.promo_express_messages;
    }

    @Override
    public void initViews(View root) {
        super.initViews(root);
        PromoExpressMessagesBinding binding = DataBindingUtil.bind(root);
        BackToolbarViewModel toolbarViewModel = new BackToolbarViewModel(binding.toolbarInclude, ResourceExtensionKt.getString(R.string.settings_messages));
        toolbarViewModel.getUpIcon().set(R.drawable.menu_gray_notification);
        binding.setToolbarViewModel(toolbarViewModel);
        if (getPremiumEntity().getPopupVersion() == 1) {
            binding.deleteMessages.setTextColor(getResources().getColorStateList(R.color.delete_messages_text_color_selector));
            binding.deleteMessages.setBackgroundColor(Color.TRANSPARENT);
        }
        ArrayList<Integer> avatarArray = getFakeAvatars();
        if (avatarArray.size() != 0) {
            binding.firstFakeUser.setResourceSrc(getAvatarId(avatarArray));
            binding.secondFakeUser.setResourceSrc(getAvatarId(avatarArray));
            binding.thirdFakeUser.setResourceSrc(getAvatarId(avatarArray));
        }
    }

    private ArrayList<Integer> getFakeAvatars() {
        Profile profile = App.get().getProfile();
        int arrayId = profile.dating != null && profile.dating.sex == Profile.GIRL ? R.array.fake_girl_avatars : R.array.fake_boy_avatars;
        ArrayList<Integer> avatarsIdArray = new ArrayList<>();
        int randomValue;
        TypedArray imgs = App.getContext().getResources().obtainTypedArray(arrayId);
        ArrayList<Integer> usersFakeArray = new ArrayList<>();
        for (int i = 0; i < imgs.length(); i++) {
            usersFakeArray.add(imgs.getResourceId(i, profile.dating != null && profile.dating.sex == Profile.GIRL ? fake_girl1 : R.drawable.fake_boy1));
        }
        for (int i = 0; i < AVATARS_ID_ARRAY_LENGTH; i++) {
            int iterCounter = 0;
            do {
                iterCounter++;
                randomValue = new Random().nextInt(usersFakeArray.size() - 1);

            } while (avatarsIdArray.contains(usersFakeArray.get(randomValue)) || iterCounter < 30);
            avatarsIdArray.add(usersFakeArray.get(randomValue));
        }
        imgs.recycle();
        return avatarsIdArray;
    }

    private int getAvatarId(ArrayList<Integer> avatars) {
        if (avatars.size() <= mCurrentPosition) {
            mCurrentPosition = 0;
        } else {
            mCurrentPosition++;
        }
        return getNotNullAvatar(avatars, mCurrentPosition);
    }

    private int getNotNullAvatar(ArrayList<Integer> avatars, int position) {
        Profile profile = App.get().getProfile();
        return avatars != null ?
                position < avatars.size() ?
                        avatars.get(position) :
                        avatars.get(getRandom().nextInt(avatars.size())) :
                profile.dating != null && profile.dating.sex == Profile.GIRL ?
                        R.drawable.fake_girl1 :
                        R.drawable.fake_boy1;
    }

    private Random getRandom() {
        if (mRandom == null) {
            mRandom = new Random();
        }
        return mRandom;
    }
}
