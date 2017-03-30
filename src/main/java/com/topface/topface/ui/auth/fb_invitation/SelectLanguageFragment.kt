package com.topface.topface.ui.auth.fb_invitation


import android.databinding.DataBindingUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.topface.topface.R
import com.topface.topface.databinding.LanguageSelectBinding
import com.topface.topface.databinding.LanguageSelectItemBinding
import com.topface.topface.ui.dialogs.AbstractDialogFragment
import com.topface.topface.utils.extensions.getInt
import kotlin.properties.Delegates


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
        with(mBinding.recyclerView) {
            adapter = LanguageDataAdapter(listOf(Language("Русский", "ru"),
                    Language("Английский", "en"),
                    Language("Польский", "po"),
                    Language("Испанский", "es"),
                    Language("Французский", "fr"),
                    Language("Итальянский", "it"),
                    Language("Греческий", "gr"),
                    Language("Английский", "en"),
                    Language("Польский", "po"),
                    Language("Испанский", "es"),
                    Language("Французский", "fr"),
                    Language("Итальянский", "it"),
                    Language("Греческий", "gr")))
            layoutManager = LinearLayoutManager(context).apply {  }
        }
    }

    override fun isModalDialog(): Boolean = false

    inner class LanguageDataAdapter(val languageList: List<Language>) : RecyclerView.Adapter<LanguageDataAdapter.LanguageSelectionItemHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) = LanguageSelectionItemHolder(LanguageSelectItemBinding.inflate(LayoutInflater.from(parent?.getContext()),parent,false).root)

        override fun onBindViewHolder(viewHolder: LanguageSelectionItemHolder, position: Int) {
            viewHolder.binding.setViewModel(SelectLanguageItemViewModel(languageList.get(position)))
        }

        override fun getItemCount(): Int = languageList.size

        inner class LanguageSelectionItemHolder(v: View) : RecyclerView.ViewHolder(v) {
            val binding = DataBindingUtil.bind<LanguageSelectItemBinding>(v)
        }

    }
}