package top.cyqi.jxqrcode;

import android.app.*;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationCompat;

import java.util.List;

public class UpdateCodeJob extends JobService {
    private static final String TAG = "JSB_UpdateCodeJob";

    /**
     * 判断服务是否处于运行状态.
     * @param ServiceName 服务名称
     * @param context 上下文
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public static boolean isServiceRunning(String ServiceName,Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> infos = am.getRunningServices(100);
        for(ActivityManager.RunningServiceInfo info: infos){
            if(ServiceName.equals(info.service.getClassName())){
                return true;
            }
        }
        return false;
    }



    public static void startJob(Context context) {
        ComponentName jobService = new ComponentName(context, UpdateCodeJob.class);
        Intent service = new Intent(context, UpdateCodeJob.class);
        context.startService(service);
        SharedPreferences preferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        int expireTimeNumber = preferences.getInt("expireTimeNumber", 300);
        expireTimeNumber = expireTimeNumber - 30; // 减去30秒，去除刷新带来的延迟
        Log.d(TAG, "启动一个Job延迟" + expireTimeNumber + "秒");

        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        JobInfo jobInfo = new JobInfo.Builder(10087, jobService) //任务Id等于123
                .setMinimumLatency(expireTimeNumber * 1000L) //最小延迟时间
//                .setMinimumLatency(60000L) //最小延迟时间
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)// 网络条件，默认值NETWORK_TYPE_NONE
                .setRequiresCharging(false)// 是否充电
                .setRequiresDeviceIdle(false)// 设备是否空闲
                .setPersisted(true) //设备重启后是否继续执行 需要权限android.permission.RECEIVE_BOOT_COMPLETED
                .setBackoffCriteria(3000, JobInfo.BACKOFF_POLICY_LINEAR) //设置退避/重试策略
                .build();

        scheduler.schedule(jobInfo);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
    }


    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.i(TAG, "on start job: " + params.getJobId());
        startJob(this);
        // 返回true，很多工作都会执行这个地方，我们手动结束这个任务
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // 停止跟踪这些作业参数，因为我们已经完成工作。
        Log.i(TAG, "开始更新二维码job: " + params.getJobId());
        SharedPreferences preferences = this.getSharedPreferences("user_data", MODE_PRIVATE);
        String encrypt = preferences.getString("encrypt", "{\"encrypt\":\"Pxxxxxx\"}");
        JsbToolsUtil.getNetQrCode(this, encrypt, null);
        // 返回false来销毁这个工作
        return true;
    }
}