package com.dryseed.dsvirtualapk.core.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;

import com.dryseed.dsvirtualapk.core.PluginManager;
import com.dryseed.dsvirtualapk.core.utils.Constants;
import com.dryseed.dsvirtualapk.core.utils.DexUtil;

import java.io.File;

import dalvik.system.DexClassLoader;


/**
 * @author caiminming
 */
public class LoadedPlugin {
    private ClassLoader mClassLoader;
    private File mNativeLibDir;

    public static LoadedPlugin create(PluginManager pluginManager, Context host, File apk) throws Exception {
        return new LoadedPlugin(pluginManager, host, apk);
    }

    /**
     * LoadedPlugin初始化
     * 1. 创建自定义ClassLoader
     *
     * @param pluginManager
     * @param context
     * @param apk
     * @throws Exception
     */
    LoadedPlugin(PluginManager pluginManager, Context context, File apk) throws Exception {
        // mNativeLibDir : /data/data/package_name/valibs
        this.mNativeLibDir = context.getDir(Constants.NATIVE_DIR, Context.MODE_PRIVATE);
        this.mClassLoader = createClassLoader(context, apk, this.mNativeLibDir, context.getClassLoader());
    }

    private static ClassLoader createClassLoader(Context context, File apk, File libsDir, ClassLoader parent) {
        // dexOutputDir : /data/data/package_name/dex
        File dexOutputDir = context.getDir(Constants.OPTIMIZE_DIR, Context.MODE_PRIVATE);
        String dexOutputPath = dexOutputDir.getAbsolutePath();
        DexClassLoader loader = new DexClassLoader(apk.getAbsolutePath(), dexOutputPath, libsDir.getAbsolutePath(), parent);

        if (Constants.COMBINE_CLASSLOADER) {
            try {
                // 将DexClassLoader的DexElements合并到PathClassLoader中
                DexUtil.insertDex(loader);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return loader;
    }

    public ClassLoader getClassLoader() {
        return this.mClassLoader;
    }

    /**
     * plugin的package名
     *
     * @return
     */
    public String getPackageName() {
        //return this.mPackage.packageName;
        return "com.dryseed.dexclassloaderbundleapk";
    }

    /**
     * plugin的ActivityInfo
     *
     * @param componentName
     * @return
     */
    public ActivityInfo getActivityInfo(ComponentName componentName) {
        ActivityInfo activityInfo = new ActivityInfo();
        activityInfo.launchMode = ActivityInfo.LAUNCH_MULTIPLE;
        return activityInfo;
    }
}
