package com.eve.bill_it;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;

public class ThemePicker {
    static void setStatusBar(Activity activity){
        switch (activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                setLightStatusBar(activity);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                clearLightStatusBar(activity);
                break;
        }
    }

    private static void setLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().setStatusBarColor(activity.getColor(R.color.bg));
        }
    }

    private static void clearLightStatusBar(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = activity.getWindow().getDecorView().getSystemUiVisibility(); // get current flag
            flags = flags ^ View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; // use XOR here for remove LIGHT_STATUS_BAR from flags
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
            activity.getWindow().setStatusBarColor(activity.getColor(R.color.white));
        }
    }
}
