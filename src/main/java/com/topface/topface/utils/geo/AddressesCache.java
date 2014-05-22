package com.topface.topface.utils.geo;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.data.History;
import com.topface.topface.utils.cache.MemoryCacheTemplate;

public class AddressesCache {

    private MemoryCacheTemplate<String, String> mAddressesCache = new MemoryCacheTemplate<>();

    public void mapAddressDetection(final History history, final TextView tv,
                                    final ProgressBar prgsBar) {
        if (history.geo != null) {
            final String key = history.geo.getCoordinates().toString();
            String cachedAddress = mAddressesCache.get(key);

            if (cachedAddress != null) {
                tv.setText(cachedAddress);
                return;
            }

            prgsBar.setVisibility(View.VISIBLE);
            new BackgroundThread() {
                @Override
                public void execute() {
                    final String address = OsmManager.getAddress(
                            history.geo.getCoordinates().getLatitude(),
                            history.geo.getCoordinates().getLongitude()
                    );
                    mAddressesCache.put(key, address);
                    tv.post(new Runnable() {
                        @Override
                        public void run() {
                            tv.setText(address);
                            prgsBar.setVisibility(View.GONE);
                        }
                    });
                }
            };
        }

    }
}
