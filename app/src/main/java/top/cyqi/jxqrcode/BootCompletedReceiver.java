package top.cyqi.jxqrcode;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.*;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;


import static android.content.Context.MODE_PRIVATE;
import static android.content.Intent.*;

public class BootCompletedReceiver extends BroadcastReceiver {


    public static boolean  already_boot = false;
    public static  String TAG = "JsbTools_BootCompletedReceiver";



    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, NoticeService.class);
        switch (intent.getAction()) {
            case Intent.ACTION_SHUTDOWN:
                Log.d(TAG, "手机关机了");
                break;
            case ACTION_SCREEN_ON:
                Log.d(TAG, "亮屏");
                SharedPreferences preferences = context.getSharedPreferences("user_data", MODE_PRIVATE);
                boolean NoticeShow = preferences.getBoolean("NoticeShow", false);
                if (NoticeShow) {
                    //创建通知
                    context.startService(serviceIntent);
                }
                break;
            case ACTION_SCREEN_OFF:
                Log.d(TAG, "息屏");
                break;
            case ACTION_USER_PRESENT:
                Log.d(TAG, "手机解锁");
                context.stopService(serviceIntent);
                break;
        }

    }
}