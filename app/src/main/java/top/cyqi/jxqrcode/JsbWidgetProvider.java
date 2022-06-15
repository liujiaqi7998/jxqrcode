package top.cyqi.jxqrcode;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.*;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class JsbWidgetProvider extends AppWidgetProvider {

    private static final String CLICK_ACTION = "top.cyqi.APPWIDGET_CLICK";
    boolean isDisable = false;
    Timer timer;

    //onReceive不存在widget生命周期中，它是用来接收广播，通知全局的
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("SimpleWidgetProvider", "接收到广播: " + intent.getAction());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        //当我们点击桌面上的widget按钮（这个按钮我们在onUpdate中已经为它设置了监听），widget就会发送广播
        //这个广播我们也在onUpdate中为它设置好了意图，设置了action，在这里我们接收到对应的action并做相应处理
        if (intent.getAction().equals(CLICK_ACTION)) {

            if (isDisable) {
                return;
            }
            isDisable = true;

            SharedPreferences preferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
            int interval_time = JsbToolsUtil.check_interval_time(context);
            if (interval_time > 0) {
                remoteViews.setTextViewText(R.id.wait_time_txt2, "请" + interval_time + "秒后获取");
                appWidgetManager.updateAppWidget(new ComponentName(context, JsbWidgetProvider.class), remoteViews);
                if (timer != null) {
                    return;
                }
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        isDisable = false;
                        timer.cancel();
                        timer = null;
                        remoteViews.setTextViewText(R.id.wait_time_txt2, "");
                        appWidgetManager.updateAppWidget(new ComponentName(context, JsbWidgetProvider.class), remoteViews);
                    }
                }, interval_time * 1000L);
                return;
            }


            remoteViews.setTextViewText(R.id.wait_time_txt2, "正在获取...");
            appWidgetManager.updateAppWidget(new ComponentName(context, JsbWidgetProvider.class), remoteViews);


            Handler mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    isDisable = false;
                    if (msg.what == 0) {
                        String message = (String) msg.obj;
                        remoteViews.setTextViewText(R.id.wait_time_txt, message);
                        appWidgetManager.updateAppWidget(new ComponentName(context, JsbWidgetProvider.class), remoteViews);
                    }
                    if (msg.what == -1) {
                        String message = (String) msg.obj;
                        remoteViews.setTextViewText(R.id.wait_time_txt, message);
                        remoteViews.setTextViewText(R.id.wait_time_txt2, "");
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        remoteViews.setImageViewResource(R.id.DesktopimageView, R.drawable.error_icon);
                        appWidgetManager.updateAppWidget(new ComponentName(context, JsbWidgetProvider.class), remoteViews);
                    }
                    if (msg.what == 1) {
                        String message = (String) msg.obj;
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                        update_msg(appWidgetManager, context);
                    }
                }
            };
            String encrypt = preferences.getString("encrypt", "{\"encrypt\":\"0\"}");
            JsbToolsUtil.getNetQrCode(context, encrypt, mHandler);
        } else {
            update_msg(appWidgetManager, context);
        }
    }


    public void update_msg(AppWidgetManager appWidgetManager, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String expireTime = preferences.getString("expireTime", "未获取");
        String qrCode = preferences.getString("qrCode", "0000");
        //因为点击按钮后要对布局中的文本进行更新，所以需要创建一个远程view
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
        //为对应的TextView设置文本
        remoteViews.setTextViewText(R.id.wait_time_txt, "有效期:" + expireTime);
        if (!qrCode.equals("0000")) {
            Bitmap bmp = ImageUtil.GetGreenCode(context, qrCode);
            remoteViews.setImageViewBitmap(R.id.DesktopimageView, bmp);
        } else {
            remoteViews.setTextViewText(R.id.wait_time_txt, "无法获取到吉祥码");
            remoteViews.setImageViewResource(R.id.DesktopimageView, R.drawable.error_icon);
        }

        if (UpdateCodeJob.isServiceRunning("top.cyqi.jxqrcode.UpdateCodeJob",context)){
            int last_time = preferences.getInt("expireTimeNumber", 0);
            remoteViews.setTextViewText(R.id.wait_time_txt2, last_time + "秒后自动刷新");
        }else{
            remoteViews.setTextViewText(R.id.wait_time_txt2, "自动刷新未启动");
        }

        appWidgetManager.updateAppWidget(new ComponentName(context, JsbWidgetProvider.class), remoteViews);

    }


    //当widget第一次添加到桌面的时候回调，可添加多次widget，但该方法只回调一次
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Toast.makeText(context, "创建成功", Toast.LENGTH_SHORT).show();
    }

    //当widget被初次添加或大小被改变时回调
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    /**
     * 当widget更新时回调
     *
     * @param appWidgetIds 这个数组使用用来存储已经创建的widget的id，因为可能创建了多个widget
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        //因为可能有多个widget，所以要对它们全部更新
        for (int appWidgetId : appWidgetIds) {
            //创建一个远程view，绑定我们要操控的widget布局文件
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
            Intent intentClick = new Intent();
            //这个必须要设置，不然点击效果会无效
            intentClick.setClass(context, JsbWidgetProvider.class);
            intentClick.setAction(CLICK_ACTION);

            //PendingIntent表示的是一种即将发生的意图，区别于Intent它不是立即会发生的
            @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentClick, PendingIntent.FLAG_UPDATE_CURRENT);
            //为布局文件中的按钮设置点击监听
            remoteViews.setOnClickPendingIntent(R.id.DesktopimageView, pendingIntent);
            //告诉AppWidgetManager对当前应用程序小部件执行更新
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

            update_msg(appWidgetManager, context);
        }
    }

    //当 widget 被删除时回调
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    //当最后一个widget实例被删除时回调.
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }
}
