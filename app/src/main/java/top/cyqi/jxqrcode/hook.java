package top.cyqi.jxqrcode;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class hook implements IXposedHookLoadPackage {
    public static final String PACKAGE_NAME = "com.miui.aod";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam param) {

        //过滤包
        if (!PACKAGE_NAME.equals(param.packageName)) {
            return;
        }


        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) {
                ClassLoader cl = ((Context) param.args[0]).getClassLoader();
                Class<?> HookClass;
                try {
                    HookClass = cl.loadClass("com.miui.aod.category.ImageSelectorCategoryInfo");
                } catch (Exception e) {
                    XposedBridge.log("寻找com.miui.aod.category.ImageSelectorCategoryInfo失败");
                    return;
                }
                XposedBridge.log("寻找com.miui.aod.category.ImageSelectorCategoryInfo成功");

                XposedHelpers.findAndHookMethod(
                        HookClass,
                        "setCroppedUriString",//方法名称
                        String.class,
                        new XC_MethodHook() {
                            @SuppressLint("SdCardPath")
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) {
                                //Hook自定义功能
                                XposedBridge.log("小奇吉祥码小工具：万象息屏调用了setCroppedUriString");
                                param.args[0] = "/sdcard/" + Environment.DIRECTORY_PICTURES + "/" + "jsb_qrcode.jpg";
                            }
                        });

                XposedHelpers.findAndHookMethod(
                        HookClass,
                        "setUriString",//方法名称
                        String.class,
                        new XC_MethodHook() {
                            @SuppressLint("SdCardPath")
                            @Override
                            protected void beforeHookedMethod (MethodHookParam param) {
                                //Hook自定义功能
                                XposedBridge.log("小奇吉祥码小工具：万象息屏调用了setUriString");
                                param.args[0] = "/sdcard/" + Environment.DIRECTORY_PICTURES + "/" + "jsb_qrcode.jpg";
                            }
                        });
            }
        });
    }
}