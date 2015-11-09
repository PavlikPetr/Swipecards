package com.topface.topface.promo.dialogs;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.promo.PromoPopupManager;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;

import java.util.ArrayList;
import java.util.Random;

import static com.topface.topface.data.Options.PromoPopupEntity.AIR_MESSAGES;

public class PromoExpressMessages extends PromoDialog {

    public final static String TAG = "promo_express_messages";

    private static final int AVATARS_ID_ARRAY_LENGTH = 3;

    private int mCurrentPosition = Integer.MAX_VALUE;

    private int mExtraPaddingTop = 0;

    @Override
    public Options.PromoPopupEntity getPremiumEntity() {
        return CacheProfile.getOptions().premiumMessages;
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
    public String getMainTag() {
        return "promo.key31.v".concat(Integer.toString(getPremiumEntity() != null ? getPremiumEntity().getPopupVersion() : 0));
    }

    @Override
    public int getDialogLayoutRes() {
        return R.layout.promo_express_messages;
    }

    @Override
    public void initViews(View root) {
        super.initViews(root);
        Button btnDelete = (Button) root.findViewById(R.id.deleteMessages);
        if (getPremiumEntity().getPopupVersion() == 1) {
            btnDelete.setTextColor(getResources().getColorStateList(R.color.delete_messages_text_color_selector));
            btnDelete.setBackgroundColor(Color.TRANSPARENT);
        }
        ArrayList<Integer> avatarArray = getFakeAvatars();
        if (avatarArray.size() != 0) {
            ((ImageViewRemote) root.findViewById(R.id.firstFakeUser)).setResourceSrc(getAvatarId(avatarArray));
            ((ImageViewRemote) root.findViewById(R.id.secondFakeUser)).setResourceSrc(getAvatarId(avatarArray));
            ((ImageViewRemote) root.findViewById(R.id.thirdFakeUser)).setResourceSrc(getAvatarId(avatarArray));
        }
    }

    private ArrayList<Integer> getFakeAvatars() {
        int arrayId = CacheProfile.dating != null && CacheProfile.dating.sex == Static.GIRL ? R.array.fake_girl_avatars : R.array.fake_boy_avatars;
        ArrayList<Integer> avatarsIdArray = new ArrayList<>();
        int randomValue;
        TypedArray imgs = App.getContext().getResources().obtainTypedArray(arrayId);
        ArrayList<Integer> usersFakeArray = new ArrayList<>();
        for (int i = 0; i < imgs.length(); i++) {
            usersFakeArray.add(imgs.getResourceId(i, CacheProfile.dating != null && CacheProfile.dating.sex == Static.GIRL ? R.drawable.fake_girl1 : R.drawable.fake_boy1));
        }
        for (int i = 0; i < AVATARS_ID_ARRAY_LENGTH; i++) {
            int iterCounter = 0;
            do {
                iterCounter++;
                randomValue = new Random().nextInt(usersFakeArray.size() - 1);

            } while (avatarsIdArray.contains(usersFakeArray.get(randomValue)) || iterCounter < 30);
            avatarsIdArray.add(usersFakeArray.get(randomValue));
        }
        return avatarsIdArray;
    }

    private int getAvatarId(ArrayList<Integer> avatars) {
        if (avatars.size() <= mCurrentPosition) {
            mCurrentPosition = 0;
        } else {
            mCurrentPosition++;
        }
        return avatars.get(mCurrentPosition);
    }

    @Override
    protected int getPopupPaddingTop() {
        return getActionBarHeight() + mExtraPaddingTop;
    }

    public PromoExpressMessages setExtraPaddingTop(int extraPaddingTopValue) {
        mExtraPaddingTop = extraPaddingTopValue;
        return this;
    }

    public interface PopupRedirectListener {
        void onRedirect();
    }

    public static IStartAction createPromoPopupStartAction(final int priority, final PopupRedirectListener listener) {
        return new IStartAction() {
            @Override
            public void callInBackground() {
            }

            @Override
            public void callOnUi() {
                if (listener != null) {
                    listener.onRedirect();
                }
            }

            @Override
            public boolean isApplicable() {
                return !CacheProfile.premium && PromoPopupManager.checkIsNeedShow(CacheProfile.getOptions().getPremiumEntityByType(AIR_MESSAGES));
            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public String getActionName() {
                return "PromoPopup";
            }

            @Override
            public void setStartActionCallback(OnNextActionListener startActionCallback) {

            }
        };
    }
}
