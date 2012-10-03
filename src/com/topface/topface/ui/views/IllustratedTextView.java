package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.TextView;
import com.topface.topface.R;

import java.util.ArrayList;


/**
 * Это TextView, которое заменит текстовые шаблоны на изображения согласно своему конфигу в itv_icons.
 * Например если установить во View данного типа текст "Этот подраок стоит 10 {{coins}}",
 * то текст {{coins}} будет заменен на иконку монет.
 * Укзать шаблоны, и изображения, на которые они заменяеются можно в файле itv_icons.xml
 */
public class IllustratedTextView extends TextView {

    public static final int ICON_ALIGN = ImageSpan.ALIGN_BASELINE;
    private boolean mIsTextChanged = false;

    public IllustratedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int before, int after) {
        //Так как мы из метода onTextChanged вызываем изменение текста,
        //то что бы не попась в бесконечный цикл, мы используем флаг.
        if (!mIsTextChanged) {
            mIsTextChanged = true;
            setText(illustrateText(text));
        } else {
            mIsTextChanged = false;
        }
        super.onTextChanged(text, start, before, after);
    }

    protected SpannableString replaceTemplates(CharSequence text, String template, int icon) {
        SpannableString spannableString;
        //Ставим иконку монетки

        ArrayList<Integer> indexes = getAllIndexesOfSubstring(text.toString(), template);
        if (indexes.size() > 0) {
            spannableString = new SpannableString(text);
            int templateLength = template.length();
            Context ctx = getContext();
            for (int spanStart : indexes) {
                spannableString.setSpan(
                        new ImageSpan(ctx, icon, ICON_ALIGN),
                        spanStart,
                        spanStart + templateLength,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }

        }
        else {
            spannableString = (SpannableString) text;
        }

        return spannableString;
    }

    protected SpannableString illustrateText(CharSequence text) {
        if (text == null || text.length() < 1) {
            return null;
        }

        SpannableString spannableString = new SpannableString(text);

        String[] templates = getResources().getStringArray(R.array.itv_templates);
        TypedArray icons = getResources().obtainTypedArray(R.array.itv_icons);

        for (int i = 0; i < templates.length; i++) {
            //Если упало на OutOfIndex, то сами виноваты
            //массивы itv_templates и itv_icons должны иметь одинаковую длину
            spannableString = replaceTemplates(spannableString, templates[i], icons.getResourceId(i, 0));
        }

        return spannableString;
    }

    protected ArrayList<Integer> getAllIndexesOfSubstring(String text, String substr) {
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        int index = text.indexOf(substr);
        while (index >= 0) {
            indexes.add(index);
            index = text.indexOf(substr, index + substr.length());
        }

        return indexes;
    }

}
