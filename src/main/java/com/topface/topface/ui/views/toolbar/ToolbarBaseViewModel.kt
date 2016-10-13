package com.topface.topface.ui.views.toolbar

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v7.widget.Toolbar
import com.topface.topface.R
import com.topface.topface.utils.extensions.getString
import org.jetbrains.anko.appcompat.v7.navigationIconResource

/**
 * Created by ppavlik on 07.10.16.
 */
abstract class ToolbarBaseViewModel @JvmOverloads constructor(private val mToolbar: Toolbar,
                                                          @StringRes titleRes: Int = 0,
                                                          title: String = titleRes.getString(R.string.app_name),
                                                          @StringRes subtitleRes: Int = 0,
                                                          subTitle: String = subtitleRes.getString(),
                                                          @DrawableRes icon: Int = R.drawable.ic_arrow_up,
                                                          mNavigation: IToolbarNavigation) {

	init {
//		setTitle(title)
//		setSubtitle(subTitle)
//		setUpButton(icon)
//		mToolbar.setNavigationOnClickListener { mNavigation.onUpButtonClick() }
	}

	open fun setTitle(title: String) {
//		mToolbar.title = title
	}

	open fun setSubtitle(subtitle: String) {
//		mToolbar.subtitle = subtitle
	}

	open fun setUpButton(@DrawableRes icon: Int) {
//		mToolbar.navigationIconResource = icon
	}

	open fun release() {

	}
}