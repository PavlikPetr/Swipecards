package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.analytics.TrackedDialogFragment;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.cache.SearchCacheManager;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Created by Петр on 15.03.2016.
 * DilaogFragment for select app language
 */
public class SelectLanguageDialog extends TrackedDialogFragment {
    public static final String LOCALE = "confirm_selection_locale";

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String[] locales = getActivity().getResources().getStringArray(R.array.application_locales);
        final String[] languages = new String[locales.length];
        int selectedLocaleIndex = 0;
        Locale appLocale = new Locale(App.getLocaleConfig().getApplicationLocale());
        for (int i = 0; i < locales.length; i++) {
            Locale locale = new Locale(locales[i]);
            languages[i] = Utils.capitalize(locale.getDisplayName(locale));
            if (locale.equals(appLocale)) {
                selectedLocaleIndex = i;
            }
        }
        final int selectedLocaleIndexFinal = selectedLocaleIndex;
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.settings_select_language)
                .setSingleChoiceItems(languages, selectedLocaleIndex, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogLocales, int which) {
                        dialogLocales.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialogLocales, int whichButton) {
                        final int selectedPosition = ((AlertDialog) dialogLocales).getListView().getCheckedItemPosition();
                        if (selectedLocaleIndexFinal == selectedPosition) {
                            dialogLocales.dismiss();
                            return;
                        }
                        ConfirmChangesDialog.newInstance(locales[selectedPosition]).show(getFragmentManager(), ConfirmChangesDialog.class.getName());
                    }
                }).create();
    }

    public static class ConfirmChangesDialog extends TrackedDialogFragment {

        public static ConfirmChangesDialog newInstance(String locale) {
            ConfirmChangesDialog dialog = new ConfirmChangesDialog();
            Bundle args = new Bundle();
            args.putString(LOCALE, locale);
            dialog.setArguments(args);
            return dialog;
        }

        @NotNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final String locale = getArguments().getString(LOCALE, null);
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.settings_select_language)
                    .setMessage(R.string.restart_to_change_locale)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogConfirm, int which) {
                            (new SearchCacheManager()).clearCache();
                            LocaleConfig.changeLocale(getActivity(), locale);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogConfirm, int which) {
                        }
                    }).create();
        }
    }
}
