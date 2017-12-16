package com.playposse.egoeater.activity.specialcase;

import android.app.Activity;

import com.playposse.egoeater.R;
import com.playposse.egoeater.activity.base.ParentActivity;
import com.playposse.egoeater.util.NetworkConnectivityBroadcastReceiver;

/**
 * An {@link Activity} that tells the user that the network isn't active. The user is held here to
 * avoid triggering any network requests that could error out. The
 * {@link NetworkConnectivityBroadcastReceiver} will route the user away from this activity when the
 * network returns.
 */
public class NoConnectivityActivity extends ParentActivity {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_no_connectivity;
    }
}
