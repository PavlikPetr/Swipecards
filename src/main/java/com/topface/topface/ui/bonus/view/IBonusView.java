package com.topface.topface.ui.bonus.view;

import com.topface.topface.ui.bonus.models.IOfferwallBaseModel;

import java.util.ArrayList;

public interface IBonusView {

    void setProgressState(boolean isVisible);

    void showEmptyViewVisibility(boolean isVisible);

    void showOfferwallsVisibility(boolean isVisible);

    void showOffers(ArrayList<IOfferwallBaseModel> offers);
}
