package com.topface.topface.utils;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.topface.topface.data.History;

public class AddressesCache {

    private MemoryCacheTemplate<String, String> mAddressesCache = new MemoryCacheTemplate<String, String>();

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
            new Thread(new Runnable() {
                @Override
                public void run() {
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
            }).start();
        }

    }
}
