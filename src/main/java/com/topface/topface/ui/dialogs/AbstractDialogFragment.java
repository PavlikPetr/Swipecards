package com.topface.topface.ui.dialogs;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.analytics.TrackedDialogFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.social.AuthToken;

import java.util.HashSet;

/**
 * Extend this class if you need DialogFragment
 * with semi-transparent-black background
 * shifted under actionbar
 * This class allows to show fragment with tag only in one instance at once
 */
public abstract class AbstractDialogFragment extends TrackedDialogFragment {

    private boolean mNeedActionBarIndent = true;
    private int mActionBarSize;
    private static HashSet<String> mShowingDialogs = new HashSet<>();
    private String mTag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //По стилю это у нас не диалог, а кастомный дизайн -
        //закрывает весь экран оверлеем и ниже ActionBar показывает контент
        setStyle(STYLE_NO_FRAME, R.style.Topface_Theme_TranslucentDialog);
        final TypedArray styledAttributes = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        mActionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_base, container, false);
        root.setPadding(0, mNeedActionBarIndent ? mActionBarSize : 0, 0, 0);
        ViewStub stub = (ViewStub) root.findViewById(R.id.vsContent);
        stub.setLayoutResource(getDialogLayoutRes());
        View view = stub.inflate();
        initViews(view);
        return root;
    }

    /**
     * Don't show popup, when user logged out
     */
    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            Fragment dialog = manager.findFragmentByTag(tag);
            if (!CacheProfile.isEmpty() && !AuthToken.getInstance().isEmpty() && !mShowingDialogs.contains(tag) &&
                    ((dialog != null && !dialog.isAdded()) || dialog == null)
                    ) {
                    mTag = tag;
                    mShowingDialogs.add(tag);
                    super.show(manager, tag);
            }
        } catch (Exception e) {
            Debug.error("AbstractDialogFragment " + tag + " show error: " + e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mShowingDialogs.remove(mTag);
    }

    /**
     * Gives access to content view of dialog which is defined
     * throw layout from getDialogLayoutRes() method
     *
     * @param root content view
     */
    protected abstract void initViews(View root);

    @Override
    public final void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode != -1) {
            intent.putExtra(Static.INTENT_REQUEST_KEY, requestCode);
        }
        super.startActivityForResult(intent, requestCode);
    }

    public abstract int getDialogLayoutRes();

    protected final void setNeedActionBarIndent(boolean value) {
        mNeedActionBarIndent = value;
    }
}
