package com.dryseed.dsvirtualapk.core;

import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.dryseed.dsvirtualapk.core.internal.ComponentsHandler;
import com.dryseed.dsvirtualapk.core.internal.LoadedPlugin;
import com.dryseed.dsvirtualapk.core.internal.VAInstrumentation;
import com.dryseed.dsvirtualapk.core.utils.ContextUtil;
import com.dryseed.dsvirtualapk.core.utils.PluginUtil;
import com.dryseed.dsvirtualapk.core.utils.ReflectUtil;
import com.dryseed.dsvirtualapk.core.utils.RunUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author caiminming
 */
public class PluginManager {
    public static final String TAG = "MMM-PluginManager";

    private static volatile PluginManager sInstance = null;
    private Map<String, LoadedPlugin> mPlugins = new ConcurrentHashMap<>();

    /**
     * Hooked instrumentation
     */
    private Instrumentation mInstrumentation;

    /**
     * Context of host app
     */
    private Context mContext;

    private ComponentsHandler mComponentsHandler;

    public static PluginManager getInstance(Context base) {
        if (sInstance == null) {
            synchronized (PluginManager.class) {
                if (sInstance == null) {
                    sInstance = new PluginManager(base);
                }
            }
        }

        return sInstance;
    }

    private PluginManager(Context context) {
        Context app = context.getApplicationContext();
        if (app == null) {
            this.mContext = context;
        } else {
            this.mContext = ((Application) app).getBaseContext();
        }
        prepare();
    }

    /**
     * 单例初始化时被调用
     * 1. hook instrumentation
     * 2. hook main handler
     */
    private void prepare() {
        ContextUtil.sHostContext = getHostContext();
        this.hookInstrumentationAndHandler();
        //this.hookSystemServices();
    }

    /**
     * Application初始化时被调用
     * 1. 创建ComponentsHandler，将PluginManager传进去
     */
    public void init() {
        mComponentsHandler = new ComponentsHandler(this);
        RunUtil.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                doInWorkThread();
            }
        });
    }

    private void doInWorkThread() {
    }

    /**
     * Hook Instrumentation & Handler
     */
    private void hookInstrumentationAndHandler() {
        try {
            Instrumentation baseInstrumentation = ReflectUtil.getInstrumentation(this.mContext);
            if (baseInstrumentation.getClass().getName().contains("lbe")) {
                // reject executing in paralell space, for example, lbe.
                System.exit(0);
            }

            final VAInstrumentation instrumentation = new VAInstrumentation(this, baseInstrumentation);
            Object activityThread = ReflectUtil.getActivityThread(this.mContext);
            ReflectUtil.setInstrumentation(activityThread, instrumentation);
            ReflectUtil.setHandlerCallback(this.mContext, instrumentation);
            this.mInstrumentation = instrumentation;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * load a plugin into memory, then invoke it's Application.
     *
     * @param apk the file of plugin, should end with .apk
     * @throws Exception
     */
    public void loadPlugin(File apk) throws Exception {
        if (null == apk) {
            throw new IllegalArgumentException("error : apk is null.");
        }

        if (!apk.exists()) {
            throw new FileNotFoundException(apk.getAbsolutePath());
        }

        LoadedPlugin plugin = LoadedPlugin.create(this, this.mContext, apk);
        if (null != plugin) {
            this.mPlugins.put(plugin.getPackageName(), plugin);
//            synchronized (mCallbacks) {
//                for (int i = 0; i < mCallbacks.size(); i++) {
//                    mCallbacks.get(i).onAddedLoadedPlugin(plugin);
//                }
//            }
//            // try to invoke plugin's application
//            plugin.invokeApplication();
        } else {
            throw new RuntimeException("Can't load plugin which is invalid: " + apk.getAbsolutePath());
        }
    }

    public Context getHostContext() {
        return this.mContext;
    }

    public LoadedPlugin getLoadedPlugin(String packageName) {
        return this.mPlugins.get(packageName);
    }

    /**
     * 根据 Intent 查找 LoadedPlugin
     * 1. intent中存的package名
     *
     * @param intent
     * @return
     */
    public LoadedPlugin getLoadedPlugin(Intent intent) {
        ComponentName component = PluginUtil.getComponent(intent);
        return getLoadedPlugin(component.getPackageName());
    }

    /**
     * 根据 ComponentName 查找 LoadedPlugin
     * 1. ComponentName中的package名
     *
     * @param component
     * @return
     */
    public LoadedPlugin getLoadedPlugin(ComponentName component) {
        return this.getLoadedPlugin(component.getPackageName());
    }

    public ComponentsHandler getComponentsHandler() {
        return mComponentsHandler;
    }
}
