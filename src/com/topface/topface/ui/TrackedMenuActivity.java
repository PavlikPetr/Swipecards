package com.topface.topface.ui;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.google.android.apps.analytics.easytracking.TrackedActivity;
import com.topface.topface.R;

public class TrackedMenuActivity extends TrackedActivity {
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.settings:
                startActivity(
                        new Intent(this, SettingsActivity.class)
                );
                break;
            case R.id.filter:
                startActivity(
                        new Intent(this, FilterActivity.class)
                );
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
