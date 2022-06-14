package top.cyqi.jxqrcode;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

public class UpdateCodeJob extends JobService {
    private static final String TAG = "MyJobService";

    /**
     * false: 该系统假设任何任务运行不需要很长时间并且到方法返回时已经完成。
     * true: 该系统假设任务是需要一些时间并且当任务完成时需要调用jobFinished()告知系统。
     */
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "Totally and completely working on job " + params.getJobId());
        Log.d(TAG,"onStartJob");
        return true;
    }

    /**
     * 当收到取消请求时，该方法是系统用来取消挂起的任务的。
     * 如果onStartJob()返回false，则系统会假设没有当前运行的任务，故不会调用该方法。
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "stop job " + params.getJobId());
        return false;
    }

}