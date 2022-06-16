package top.cyqi.jxqrcode;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import static top.cyqi.jxqrcode.ImageUtil.Green_code_color;

public class NoticeService extends Service {

    private static final String TAG = "JSB_NoticeService";

    

    public void createMusicNotification(Context context) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //android 8.0的判断、需要加入NotificationChannel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("QiJsbQr", "小奇吉祥码通知栏显示",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "QiJsbQr");
        //自定义布局必须加上、否则布局会有显示问题、可以自己try try
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setOngoing(true);//代表是常驻的，主要是配合服务

        SharedPreferences preferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        String expireTime = preferences.getString("expireTime", "未获取");
        String qrCode = preferences.getString("qrCode", "0000");

        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        builder.setSound(null);//关闭声音
        builder.setVibrate(null);//关闭震动
        builder.setLights(0, 0, 0);//关闭LED灯

        if (!qrCode.equals("0000")) {
            builder.setContentTitle("有效期:" + expireTime);

            Bitmap bmp = ImageUtil.createQRCodeBitmap(qrCode, 500, 300, "UTF-8", "H", "1", Green_code_color, Color.WHITE);
            Bitmap LogoBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.jsb_icon_round_pic, null);
            bmp = ImageUtil.addLogo(bmp, LogoBmp);

            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(bmp));
        } else {
            builder.setContentTitle("无法获取到吉祥码");
            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(BitmapFactory.decodeResource(getResources(), R.drawable.error_icon)));
        }


        Notification notification = builder.build();
        //0x11 为通知id 自定义可
        startForeground(0x11, notification);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createMusicNotification(this);
        Log.i(TAG, "创建通知服务");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        Log.i(TAG, "销毁通知服务");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
