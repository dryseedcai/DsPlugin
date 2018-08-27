package com.dryseed.dsvirtualapk.core.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.support.annotation.WorkerThread;

import com.dryseed.dsvirtualapk.core.PluginManager;
import com.dryseed.dsvirtualapk.core.utils.Constants;
import com.dryseed.dsvirtualapk.core.utils.DexUtil;
import com.dryseed.dsvirtualapk.core.utils.ReflectUtil;

import java.io.File;

import dalvik.system.DexClassLoader;


/**
 * @author caiminming
 */
public class LoadedPlugin {
    private final String mLocation;
    private Resources mResources;
    private ClassLoader mClassLoader;
    private File mNativeLibDir;
    private Context mHostContext;
    private Context mPluginContext;
    private PluginManager mPluginManager;

    public static LoadedPlugin create(PluginManager pluginManager, Context host, File apk) throws Exception {
        return new LoadedPlugin(pluginManager, host, apk);
    }

    /**
     * LoadedPlugin初始化
     * 1. 创建自定义ClassLoader
     *
     * @param pluginManager
     * @param context       宿主的Context
     * @param apk           插件的路径
     * @throws Exception
     */
    LoadedPlugin(PluginManager pluginManager, Context context, File apk) throws Exception {
        this.mPluginManager = pluginManager;
        this.mHostContext = context;
        this.mPluginContext = new PluginContext(this);
        // mNativeLibDir : /data/data/package_name/valibs
        this.mNativeLibDir = context.getDir(Constants.NATIVE_DIR, Context.MODE_PRIVATE);
        this.mResources = createResources(context, apk);
        this.mClassLoader = createClassLoader(context, apk, this.mNativeLibDir, context.getClassLoader());
        this.mLocation = apk.getAbsolutePath();
    }

    /**
     * 创建Resources对象
     * 1. 分为 合并Resources & 独立Resources
     *
     * @param context
     * @param apk
     * @return
     */
    @WorkerThread
    private static Resources createResources(Context context, File apk) {
        /*
            源码创建Resources对象方法：
                Resources getTopLevelResources(String resDir, CompatibilityInfo compInfo) {
                    AssetManager assets = new AssetManager();
                    // 此处将上面的mResDir，也就是宿主的APK在手机中的路径当做资源包添加到AssetManager里，则Resources对象可以通过AssetManager查找资源
                    if (assets.addAssetPath(resDir) == 0) {
                        return null;
                    }
                    // 创建Resources对象，此处依赖AssetManager类来实现资源查找功能。
                    r = new Resources(assets, metrics, getConfiguration(), compInfo);
                }
         */
        if (Constants.COMBINE_RESOURCES) {
            // 如果插件资源合并到宿主里面去的情况，插件可以访问宿主的资源
            Resources resources = ResourcesManager.createResources(context, apk.getAbsolutePath());
            ResourcesManager.hookResources(context, resources);
            return resources;
        } else {
            // 插件使用独立的Resources，不与宿主有关系，无法访问到宿主的资源
            Resources hostResources = context.getResources();
            AssetManager assetManager = createAssetManager(context, apk);
            return new Resources(assetManager, hostResources.getDisplayMetrics(), hostResources.getConfiguration());
        }
    }

    /**
     * 创建插件的AssetManager
     * 1. 独立Resources 方式时，插件拥有独立的Resources对象，结合源码创建Resources方法 构建AssetManager
     *
     * @param context
     * @param apk
     * @return
     */
    private static AssetManager createAssetManager(Context context, File apk) {
        try {
            AssetManager am = AssetManager.class.newInstance();
            ReflectUtil.invoke(AssetManager.class, am, "addAssetPath", apk.getAbsolutePath());
            return am;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 创建ClassLoader
     *
     * @param context
     * @param apk
     * @param libsDir
     * @param parent
     * @return
     */
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
        return Constants.TEST_PACKAGE_NAME;
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

    /**
     * plugin的文件路径
     *
     * @return
     */
    public String getLocation() {
        return this.mLocation;
    }

    /**
     * 更新plugin的Resources
     *
     * @param newResources
     */
    public void updateResources(Resources newResources) {
        this.mResources = newResources;
    }

    /**
     * plugin的Resources
     *
     * @return
     */
    public Resources getResources() {
        return this.mResources;
    }

    /**
     * plugin的Context
     *
     * @return
     */
    public Context getPluginContext() {
        return this.mPluginContext;
    }

    /**
     * 宿主的Context
     *
     * @return
     */
    public Context getHostContext() {
        return this.mHostContext;
    }

    public PluginManager getPluginManager() {
        return this.mPluginManager;
    }


}
