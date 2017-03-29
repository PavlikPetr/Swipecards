package com.topface.topface.ui.auth.fb_invitation


import android.support.v7.widget.RecyclerView
import android.view.View
import com.topface.topface.R
import com.topface.topface.databinding.LanguageSelectBinding
import com.topface.topface.ui.dialogs.AbstractDialogFragment
import kotlin.properties.Delegates
import android.databinding.adapters.CompoundButtonBindingAdapter.setChecked
import android.view.LayoutInflater
import android.view.ViewGroup
import com.topface.topface.databinding.LanguageSelectItemBinding


class SelectLanguageFragment : AbstractDialogFragment() {

    companion object {
        const val TAG = "select_language"
    }

    private var mBinding by Delegates.notNull<LanguageSelectBinding>()

    private val mViewModel by lazy { SelectLanguageViewModel() }

    override fun getDialogLayoutRes() = R.layout.language_select

    override fun initViews(root: View?) {
        mBinding = LanguageSelectBinding.bind(root)
        mBinding.setViewModel(mViewModel)
        mBinding.recyclerView.adapter =
    }

    override fun isModalDialog(): Boolean = false
    
    inner class CardViewDataAdapter(val languageList: List<Language>) : RecyclerView.Adapter<CardViewDataAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) = ViewHolder(LanguageSelectItemBinding.inflate(LayoutInflater.from(parent?.getContext())))



        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder


            //todo РЕШИТЬ ПРОБЛЕМУ С БИНДИНГОМ
        }

        override fun getItemCount(): Int = languageList.size

        inner class ViewHolder(binding: LanguageSelectItemBinding) : RecyclerView.ViewHolder(binding.root) {


        }

    }
}