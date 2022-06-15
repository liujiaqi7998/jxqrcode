package top.cyqi.jxqrcode;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import static android.content.Context.MODE_PRIVATE;

/**
 * 水印图片工具类
 *
 * @author cheng
 */
public class ImageUtil {
    private static final String TAG = "JsbImageUtil";
    static final int Green_code_color = 0xFF00A74E; // 绿码的颜色
    public static Bitmap addWaterMark(Bitmap src, String text) {
        return createBitmap(src, getWaterBitmap(src, text));
    }

    private static Bitmap getWaterBitmap(Bitmap src, String text) {
        Bitmap newBitmap = Bitmap.createBitmap(src.getWidth(), 150, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawRGB(0, 0, 0);
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(35.0F);
        textPaint.setColor(Color.WHITE);
        StaticLayout sl = new StaticLayout(text, textPaint, newBitmap.getWidth() - 8, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        canvas.translate(6, 10);
        sl.draw(canvas);
        canvas.save();
        canvas.restore();
        return newBitmap;
    }

    private static Bitmap createBitmap(Bitmap src, Bitmap watermark) {
        if (src == null) {
            return null;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        int wh = watermark.getHeight();
        Bitmap newBitmap = Bitmap.createBitmap(w, h + wh, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(src, 0, 0, null);
        canvas.drawBitmap(watermark, 0, h, null);
        canvas.save();
        canvas.restore();
        src.recycle();
        watermark.recycle();
        return newBitmap;
    }

    /**
     * 生成绿码
     *
     * @param context          上下文，用于获取资源包里面的图标
     * @param content          二维码
     * @param size             图像大小
     * @param background_color 背景颜色
     * @return 生成的绿码
     */
    public static Bitmap GetGreenCode(Context context, String content, int size, int background_color) {
        Bitmap bmp = createQRCodeBitmap(content, size, size, "UTF-8", "H", "1", Green_code_color, background_color);
        Bitmap LogoBmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.jsb_icon_round_pic, null);
        return addLogo(bmp, LogoBmp);
    }

    public static Bitmap GetGreenCode(Context context, String content) {
        return GetGreenCode(context, content, 800, Color.WHITE);
    }

    public static Bitmap addLogo(Bitmap src, Bitmap logo) {

        if (src == null) {
            return null;
        }

        if (logo == null) {
            return src;
        }

        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }

        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2f, srcHeight / 2f);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2f, (srcHeight - logoHeight) / 2f, null);
            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * 生成简单二维码
     *
     * @param content                字符串内容
     * @param width                  二维码宽度
     * @param height                 二维码高度
     * @param character_set          编码方式（一般使用UTF-8）
     * @param error_correction_level 容错率 L：7% M：15% Q：25% H：35%
     * @param margin                 空白边距（二维码与边框的空白区域）
     * @param color_black            黑色色块
     * @param color_white            白色色块
     * @return BitMap
     */
    public static Bitmap createQRCodeBitmap(String content, int width, int height, String character_set, String error_correction_level, String margin, int color_black, int color_white) {
        // 字符串内容判空
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        // 宽和高>=0
        if (width < 0 || height < 0) {
            return null;
        }
        try {
            /* 1.设置二维码相关配置 */
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            // 字符转码格式设置
            if (!TextUtils.isEmpty(character_set)) {
                hints.put(EncodeHintType.CHARACTER_SET, character_set);
            }
            // 容错率设置
            if (!TextUtils.isEmpty(error_correction_level)) {
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction_level);
            }
            // 空白边距设置
            if (!TextUtils.isEmpty(margin)) {
                hints.put(EncodeHintType.MARGIN, margin);
            }
            /* 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象 */
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            /* 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值 */
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = color_black;//黑色色块像素设置
                    } else {
                        pixels[y * width + x] = color_white;// 白色色块像素设置
                    }
                }
            }
            /* 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象 */
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void save_Black_Bitmap(Context context, String str, String qrCode) {
        Bitmap bpm = GetGreenCode(context, qrCode, 600, Color.BLACK);
        bpm = ImageUtil.addWaterMark(bpm, str);
        save_Bitmap(context, bpm);
    }

    public static void save_Bitmap(Context context, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        SharedPreferences preferences = context.getSharedPreferences("user_data", MODE_PRIVATE);
        String bitmap_uri = preferences.getString("bitmap_uri", "");
        SharedPreferences.Editor editor = preferences.edit();
        if (TextUtils.isEmpty(bitmap_uri)) {
            ContentResolver resolver = context.getContentResolver();
            // 在主要的外部存储设备上查找所有图片文件。
            Uri audioCollection;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                audioCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
            } else {
                audioCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }
            // 发表一首图片。
            ContentValues newSongDetails = new ContentValues();
            newSongDetails.put(MediaStore.Images.Media.DISPLAY_NAME, "jsb_qrcode.jpg");
            // 为新歌的URI保留一个句柄，以防我们以后需要修改它。
            Uri myFavoriteSongUri = resolver.insert(audioCollection, newSongDetails);
            Log.d(TAG, "创建新的图片: " + myFavoriteSongUri);
            bitmap_uri = myFavoriteSongUri.toString();
            editor.putString("bitmap_uri", bitmap_uri);
            editor.apply();
        }
        Uri baseUri = Uri.parse(bitmap_uri);
        try {
            ContentResolver localContentResolver = context.getContentResolver();
            OutputStream outputStream = localContentResolver.openOutputStream(baseUri);
            //将bitmap图片保存到Uri对应的数据节点中
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            Log.d(TAG, "保存图片: " + bitmap_uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            editor.putString("bitmap_uri", "");
            editor.apply();
            save_Bitmap(context, bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}