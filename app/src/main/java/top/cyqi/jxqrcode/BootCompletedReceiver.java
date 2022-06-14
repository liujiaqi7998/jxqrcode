package top.cyqi.jxqrcode;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.*;

public class BootCompletedReceiver extends BroadcastReceiver {


    public static boolean  already_boot = false;
    public static  String TAG = "JsbTools_BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getAction()) {
            case Intent.ACTION_SHUTDOWN:
                Log.d(TAG, "手机关机了");
                break;
            case ACTION_SCREEN_ON:
                Log.d(TAG, "亮屏");
                JsbTools.auto_get(context);
                break;
            case ACTION_SCREEN_OFF:
                Log.d(TAG, "息屏");
                JsbTools.auto_get(context);
                break;
            case ACTION_USER_PRESENT:
                Log.d(TAG, "手机解锁");
                break;
        }

    }
}