package com.topface.topface.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import com.topface.topface.R;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 10.12.12
 * Time: 20:35
 * To change this template use File | Settings | File Templates.
 */
public class ProfileActionsControl extends View {

    public ProfileActionsControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.control_profile_actions,null);
    }

}
