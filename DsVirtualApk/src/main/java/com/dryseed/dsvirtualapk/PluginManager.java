package com.dryseed.dsvirtualapk;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;

import com.dryseed.dsvirtualapk.internal.LoadedPlugin;
import com.dryseed.dsvirtualapk.internal.VAInstrumentation;
import com.dryseed.dsvirtualapk.utils.ContextUtil;
import com.dryseed.dsvirtualapk.utils.ReflectUtil;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author caiminming
 */
public class PluginManager {
    public static final String TAG = "PluginManager";

    private static volatile PluginManager sInstance = null;

    /**
     * Hooked instrumentation
     */
    private Instrumentation mInstrumentation;

    /**
     * Context of host app
     */
    private Context mContext;

    public static PluginManager getInstance(Context base) {
        if (sInstance == null) {
            synchronized (PluginManager.class) {
                if (sInstance == null)
                    sInstance = new PluginManager(base);
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

    private void prepare() {
        ContextUtil.sHostContext = getHostContext();
        this.hookInstrumentationAndHandler();
        //this.hookSystemServices();
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
            //ReflectUtil.setHandlerCallback(this.mContext, instrumentation);
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

    }

    public Context getHostContext() {
        return this.mContext;
    }
}
