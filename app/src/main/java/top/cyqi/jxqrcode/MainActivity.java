package top.cyqi.jxqrcode;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "JSB_MainActivity";

    // 刷新界面
    @SuppressLint("SetTextI18n")
    public void Refresh_interface(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("user_data", MODE_PRIVATE);
        String encrypt = preferences.getString("encrypt", "{\"encrypt\":\"Pxxxxxx\"}");
        String expireTime = preferences.getString("expireTime", "未获取");
        String qrCode = preferences.getString("qrCode", "0000");


        // 吉祥码到期时间文本框
        TextView last_get_txt = findViewById(R.id.last_get_txt);
        last_get_txt.setText("吉祥码到期时间：" + expireTime);

        // 密钥输入框
        EditText encrypt_txt = findViewById(R.id.encrypt_txt);
        encrypt_txt.setText(encrypt);

        // 吉祥码图片
        ImageView imageView = findViewById(R.id.imageView);
        if (!qrCode.equals("0000")) {
            Bitmap bmp = ImageUtil.GetGreenCode(context, qrCode);
            imageView.setImageBitmap(bmp);
        } else {
            last_get_txt.setText("无法获取到吉祥码，请填写密钥");
            imageView.setImageResource(R.drawable.error_icon);
        }

        TextView interval_time_txt = findViewById(R.id.interval_time_txt);
        if (UpdateCodeJob.isServiceRunning("top.cyqi.jxqrcode.UpdateCodeJob",context)){
            int last_time = preferences.getInt("expireTimeNumber", 0);
            interval_time_txt.setText(last_time + "秒后自动刷新");
        }else{
            interval_time_txt.setText("自动刷新未启动");
        }
    }

    Timer timer;
    AtomicInteger interval_time = new AtomicInteger();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Refresh_interface(this);

        ImageView imageView = findViewById(R.id.imageView);
        // 吉祥码到期时间文本框
        TextView interval_time_txt = findViewById(R.id.interval_time_txt);

        // 初始化绑定按钮控件
        Button get_button = findViewById(R.id.get_button);

        Handler mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == 0) {
                    get_button.setEnabled(true);
                    String message = (String) msg.obj;
                    TextView last_get_txt = findViewById(R.id.last_get_txt);
                    last_get_txt.setText(message);
                }

                if (msg.what == -1) {
                    get_button.setEnabled(true);
                    imageView.setBackgroundColor(Color.GRAY);
                    imageView.setImageResource(R.drawable.error_icon);
                    String message = (String) msg.obj;
                    TextView last_get_txt = findViewById(R.id.last_get_txt);
                    last_get_txt.setText(message);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }

                if (msg.what == 1) {
                    get_button.setEnabled(true);
                    String message = (String) msg.obj;
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    Refresh_interface(MainActivity.this);
                }

                if (msg.what == 101) {
                    get_button.setEnabled(false);
                    int message = (int) msg.obj;
                    interval_time_txt.setText("请" + message + "秒后获取");
                }
                if (msg.what == 102) {
                    get_button.setEnabled(true);
                    interval_time_txt.setText("");
                }
            }
        };


        get_button.setOnClickListener(v -> {
            get_button.setEnabled(false);
            EditText encrypt_txt = findViewById(R.id.encrypt_txt);
            String encrypt = String.valueOf(encrypt_txt.getText());
            SharedPreferences preferences1 = getSharedPreferences("user_data", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences1.edit();
            editor.putString("encrypt", encrypt);
            editor.apply();
            interval_time.set(JsbToolsUtil.check_interval_time(MainActivity.this));
            if (interval_time.get() > 0) {
                interval_time_txt.setText("请" + interval_time + "秒后获取");
                if (timer != null) {
                    return;
                }
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        if (interval_time.get() > 0) {
                            interval_time.decrementAndGet();
                            message.what = 101;
                            message.obj = interval_time.get();
                        } else {
                            if (timer != null) {
                                timer.cancel();
                            }
                            timer = null;
                            interval_time.set(0);
                            message.what = 102;
                        }
                        mHandler.sendMessage(message);
                    }
                }, 0, 1000);
                return;
            } else {
                interval_time_txt.setText("");
            }

            JsbToolsUtil.getNetQrCode(MainActivity.this, encrypt, mHandler);
        });


        // 开启后台权限
        Button Background_button = findViewById(R.id.Background_button);
        Background_button.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "请允许“后台运行”，选择“无限制”", Toast.LENGTH_SHORT).show();
            XXPermissions.with(this)
                    // 申请单个权限
                    .permission(Permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    .request(new OnPermissionCallback() {

                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            Toast.makeText(MainActivity.this, "设置“后台运行”成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            Toast.makeText(MainActivity.this, "设置“后台运行”失败", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }
}