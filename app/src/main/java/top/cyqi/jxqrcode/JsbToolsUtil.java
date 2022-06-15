package top.cyqi.jxqrcode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.content.Context.MODE_PRIVATE;

public class JsbToolsUtil {
    private static final String TAG = "JsbTools";
    private static final String getTime_URL = "https://hcms.jilinxiangyun.com/2db4daba3b/jsb-app/getTime/data";
    private static final String saveRequestData_URL = "https://goodjournal.jilinxiangyun.com/healthcode/saveRequestData";
    private static final String qr_color_search_URL = "https://rrym-4c.jilinxiangyun.com/api/v2/qr-color-search";
    private static final int interval_time = 10; // 设置时间间隔为10秒



    /**
     * 检测是否超时
     *
     * @param context 上下文，用于获取SharedPreferences
     * @return 如果为真，则表示超时
     */
    public static boolean check_time_out(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        long expireTime = preferences.getLong("expireTime_timestamp", 0);
        if (expireTime == 0) {
            return true;
        }
        long now_time = System.currentTimeMillis();
        return now_time > expireTime;
    }

    /**
     * 检测时间间隔
     *
     * @param context 上下文，用于获取SharedPreferences
     * @return 如果为0证明已经过了时间间隔，可以发送请求，否则返回剩余时间
     */
    public static int check_interval_time(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        long last_time = preferences.getLong("last_time", 0);
        int temp_time = (int) (interval_time - (System.currentTimeMillis() - last_time) / 1000);
        if (temp_time > 1) {
            return temp_time;
        } else {
            return 0;
        }
    }

    public static void getNetQrCode(Context context, String encrypt, Handler callback) {
        if (encrypt == null) {
            send_msg(-1, "个人密钥不能为空，请重新配置", callback);
            return;
        }
        new Thread(() -> {

            String trace_id;
            String now_time;

            try {//第一步：先新建一个trace_id用于随机创建UUID
                trace_id = UUID.randomUUID().toString();
            } catch (Exception e) {
                e.printStackTrace();
                send_msg(-1, "系统错误，无法随机生成UUID", callback);
                return;
            }

            Log.d(TAG, "随机生成一个uuid作为trace_id -> " + trace_id);

            //构造时间器
            try {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                now_time = dtf.format(LocalDateTime.now());
            } catch (Exception e) {
                e.printStackTrace();
                send_msg(-1, "系统错误，无法生成时间信息", callback);
                return;
            }

            Log.d(TAG, "生成当前时间文本 -> " + now_time);


            //创建OKHTTP请求器
            OkHttpClient client;
            Request request;
            JSONObject json = new JSONObject();
            MediaType mediaType;
            try {
                mediaType = MediaType.parse("application/json;charset=utf-8");
            } catch (Exception e) {
                e.printStackTrace();
                send_msg(-1, "系统错误，无法生成发送数据的mediaType", callback);
                return;
            }
            ////////////////////////
            client = new OkHttpClient.Builder().readTimeout(5, TimeUnit.SECONDS).build();
            request = new Request.Builder().url(getTime_URL).get().build();
            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                if (response.body() == null) {
                    throw new Exception("getTime返回数据是空指针");
                }
                Log.d(TAG, "getTime的get返回数据 -> " + Objects.requireNonNull(response.body()).string());
            } catch (IOException e) {
                e.printStackTrace();
                send_msg(-1, "吉祥码更新失败，网络异常！", callback);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                send_msg(-1, "吉祥码更新失败：" + e.getMessage(), callback);
                return;
            }

            SharedPreferences preferences1 = context.getSharedPreferences("user_data", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences1.edit();
            editor.putLong("last_time", System.currentTimeMillis());
            editor.apply();

            ////////////////////////

            try {
                json.put("trace_id", trace_id);
                json.put("request_time", now_time);
                json.put("interface_id", "FourColorQRAPP");
                json.put("app_version", "android3.3.1");
                json.put("mini_version", "");
                json.put("program_start", "jsbapp");
            } catch (JSONException e) {
                e.printStackTrace();
                send_msg(-1, "系统错误，saveRequestData的JSON错误：" + e.getMessage(), callback);
                return;
            }

            Log.d(TAG, "saveRequestData的JSON数据包 -> " + json);

            RequestBody requestBody = RequestBody.Companion.create(String.valueOf(json), mediaType);

            request = new Request.Builder().url(saveRequestData_URL).post(requestBody).build();

            call = client.newCall(request);
            try {
                Response response = call.execute();
                if (response.body() == null) {
                    throw new Exception("saveRequestData返回数据是空指针");
                }
                JSONObject nn = new JSONObject(Objects.requireNonNull(response.body()).string());
                if (nn.getInt("code") != 200) {
                    throw new Exception("更新健康信息失败：" + nn.getString("msg"));
                }
                Log.d(TAG, "saveRequestData的post返回数据  -> " + nn);
            } catch (IOException e) {
                e.printStackTrace();
                send_msg(-1, "吉祥码更新失败，网络异常！", callback);
                return;
            } catch (Exception e) {
                e.printStackTrace();
                send_msg(-1, "吉祥码更新失败：" + e.getMessage(), callback);
                return;
            }

            ////////////////////////

            client = new OkHttpClient();
            requestBody = RequestBody.Companion.create(encrypt, mediaType);
            request = new Request.Builder().addHeader("app_version", "android3.3.1").addHeader("client_source", "1").addHeader("interface_id", "FourColorQRAPP").addHeader("mini_version", "").addHeader("program_start", "jsbapp").addHeader("request_time", now_time).addHeader("Service-Id", "RwiMPu").addHeader("trace_id", trace_id).addHeader("timestamp", String.valueOf(System.currentTimeMillis() / 1000)).addHeader("nonce", UUID.randomUUID().toString().trim().replaceAll("-", "")).url(qr_color_search_URL).post(requestBody).build();

            Log.d(TAG, "qr-color-search的数据包 -> " + encrypt);

            call = client.newCall(request);
            try {
                Response response = call.execute();
                if (response.body() == null) {
                    throw new Exception("qr-color-search返回数据是空指针");
                }
                JSONObject result_data = new JSONObject(Objects.requireNonNull(response.body()).string());
                Log.d(TAG, "qr-color-search的post返回数据 -> " + result_data);

                if (result_data.getString("code").equals("000000")) {
                    JSONObject data = result_data.getJSONObject("data");
                    String expireTime = data.getString("expireTime");
                    String qrCode = data.getString("qrCode");
                    String certType = data.getString("certType");
                    int expireTimeNumber = data.getInt("expireTimeNumber");

                    editor.putString("expireTime", expireTime);
                    editor.putInt("expireTimeNumber", expireTimeNumber);
                    //格式化到期时间
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = sdf.parse(expireTime);
                    //保存时间戳
                    if (date != null && date.getTime() > System.currentTimeMillis()) {
                        editor.putLong("expireTime_timestamp", date.getTime());
                    } else {
                        editor.putLong("expireTime_timestamp", 0);
                    }
                    editor.putString("qrCode", qrCode);
                    editor.putString("certType", certType);
                    editor.apply();
                    send_msg(1, "更新吉祥码成功：" + result_data.getString("msg"), callback);

                    Intent intent = new Intent("android.appwidget.action.APPWIDGET_UPDATE");
                    context.sendBroadcast(intent);

                    Log.d(TAG, "send_msg: 发送广播 -> " + intent);

                    UpdateCodeJob.startJob(context);

                    ImageUtil.save_Black_Bitmap(context,"到期时间:" + expireTime , qrCode);

                } else {
                    throw new Exception(result_data.getString("msg"));
                }

            } catch (IOException e) {
                e.printStackTrace();
                send_msg(-1, "吉祥码更新失败，网络异常！", callback);
            } catch (Exception e) {
                e.printStackTrace();
                send_msg(-1, "吉祥码更新失败：" + e.getMessage(), callback);
            }

        }).start();
    }

    /**
     * 发送回调消息
     *
     * @param type     消息类型 -1 错误 ， 0 警告 ， 1 成功
     * @param msg      消息信息
     * @param callback 回调Handler
     */
    public static void send_msg(int type, String msg, Handler callback) {
        try {
            if (callback == null) {
                return;
            }
            Message msg_sander = new Message();
            msg_sander.what = type;
            msg_sander.obj = msg;
            callback.sendMessage(msg_sander);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "向Handler发送信息出现错误: " + e.getMessage());
        }
    }




    public static void auto_get(Context context) {
        if (JsbToolsUtil.check_time_out(context)) {
            Log.d(TAG, "二维码超时重新获取");
            SharedPreferences preferences = context.getSharedPreferences("user_data", MODE_PRIVATE);
            String encrypt = preferences.getString("encrypt", "{\"encrypt\":\"Pxxxxxx\"}");
            JsbToolsUtil.getNetQrCode(context, encrypt, null);
        } else {
            Log.d(TAG, "二维码未超时无需获取");
        }
    }



}

