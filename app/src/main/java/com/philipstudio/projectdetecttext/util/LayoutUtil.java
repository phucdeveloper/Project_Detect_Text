package com.philipstudio.projectdetecttext.util;

import android.content.Context;
import android.content.SharedPreferences;

public class LayoutUtil {
    private SharedPreferences preferences;

    public LayoutUtil(Context context) {
         preferences = context.getSharedPreferences("layout", Context.MODE_PRIVATE);
    }

    public void setChangeLayout(boolean isChange){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("isChange", isChange);
        editor.apply();
    }

    public boolean getChangeLayout(){
        boolean isChange = preferences.getBoolean("isChange", false);
        return isChange;
    }
}
