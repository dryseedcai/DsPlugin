package com.dryseed.resourceloaderhostapk;

import java.io.File;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

/**
 * @version $Id: LoaderResManager.java, v 0.1 2015年12月11日 下午7:58:59 mochuan.zhb
 * @Author Zheng Haibo
 * @PersonalWebsite http://www.mobctrl.net
 * @Description 动态加载资源的管理器
 */
public class BundlerResourceLoader {

    private static final String THIRD_APK_NAME = "ResourceLoaderBundleApk-debug.apk";

    private static AssetManager createAssetManager(String apkPath) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            try {
                AssetManager.class.getDeclaredMethod("addAssetPath", String.class).invoke(
                        assetManager, apkPath);
            } catch (Throwable th) {
                System.out.println("debug:createAssetManager :" + th.getMessage());
                th.printStackTrace();
            }
            return assetManager;
        } catch (Throwable th) {
            System.out.println("debug:createAssetManager :" + th.getMessage());
            th.printStackTrace();
        }
        return null;
    }

    /**
     * 获取Bundle中的资源
     *
     * @param context
     * @return
     */
    public static Resources getBundleResource(Context context) {
        AssetsManager.copyAllAssetsApk(context);
        File dir = context.getDir(AssetsManager.APK_DIR, Context.MODE_PRIVATE);
        String apkPath = String.format("%s/%s", dir.getAbsolutePath(), THIRD_APK_NAME);
        System.out.println("debug:apkPath = " + apkPath + ",exists=" + (new File(apkPath).exists()));
        AssetManager assetManager = createAssetManager(apkPath);
        return new Resources(assetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
    }

}
