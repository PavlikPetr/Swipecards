package com.topface.topface.ui.fragments.form

import android.databinding.ViewDataBinding

/**
 * Прослойка чтоб делегат мог разворачивать лист
 * Created by tiberal on 02.11.16.
 */
abstract class ExpandableItemDelegate<T : ViewDataBinding, D : IType> : IAdapterItemDelegate<T, ExpandableItem<D>> {

    var expandableList: ExpandableList<*>? = null

}