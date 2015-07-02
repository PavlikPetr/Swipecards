package com.topface.topface.promo.dialogs;

import android.content.res.TypedArray;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;
import java.util.Random;

public class PromoExpressMessages extends PromoDialog {

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
        return Utils.getQuantityString(getPluralForm(), count, count);
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
        return "promo.expressMessages";
    }

    @Override
    public int getDialogLayoutRes() {
        return R.layout.promo_express_messages;
    }

    @Override
    public void initViews(View root) {
        super.initViews(root);
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
}
