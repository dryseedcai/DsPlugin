/*
 * Copyright (C) 2017 Beijing Didi Infinity Technology and Development Co.,Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dryseed.dsvirtualapk.core.internal;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.dryseed.dsvirtualapk.core.PluginManager;
import com.dryseed.dsvirtualapk.core.utils.LogUtil;
import com.dryseed.dsvirtualapk.core.utils.PluginUtil;
import com.dryseed.dsvirtualapk.core.utils.ReflectUtil;


/**
 * Created by renyugang on 16/8/10.
 */
public class VAInstrumentation extends Instrumentation implements Handler.Callback {
    public static final String TAG = "MMM-VAInstrumentation";

    private Instrumentation mBase;

    PluginManager mPluginManager;

    public VAInstrumentation(PluginManager pluginManager, Instrumentation base) {
        this.mPluginManager = pluginManager;
        this.mBase = base;
    }

    /**
     * Hook execStartActivity Method
     *
     * @param who
     * @param contextThread
     * @param token
     * @param target
     * @param intent
     * @param requestCode
     * @param options
     * @return
     */
    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        LogUtil.d("VAInstrumentation execStartActivity");

        //隐式启动Activity，先忽略
        //mPluginManager.getComponentsHandler().transformIntentToExplicitAsNeeded(intent);

        // null component is an implicitly intent
        if (intent.getComponent() != null) {
            LogUtil.i(TAG, String.format("execStartActivity[%s : %s]", intent.getComponent().getPackageName(),
                    intent.getComponent().getClassName()));
            // resolve intent with Stub Activity if needed
            this.mPluginManager.getComponentsHandler().markIntentIfNeeded(intent);
        }

        ActivityResult result = realExecStartActivity(who, contextThread, token, target,
                intent, requestCode, options);

        return result;
    }

    private ActivityResult realExecStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        LogUtil.d("VAInstrumentation realExecStartActivity");

        ActivityResult result = null;
        try {
            Class[] parameterTypes = {Context.class, IBinder.class, IBinder.class, Activity.class, Intent.class,
                    int.class, Bundle.class};
            result = (ActivityResult) ReflectUtil.invoke(Instrumentation.class, mBase,
                    "execStartActivity", parameterTypes,
                    who, contextThread, token, target, intent, requestCode, options);
        } catch (Exception e) {
            if (e.getCause() instanceof ActivityNotFoundException) {
                throw (ActivityNotFoundException) e.getCause();
            }
            e.printStackTrace();
        }

        return result;
    }


    /**
     * AMS执行完成后，正式启动Activity
     * 1. 从intent中取出我们的目标Activity
     * 2. 然后通过plugin的ClassLoader去加载
     *
     * @param cl
     * @param className
     * @param intent
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        LogUtil.d("VAInstrumentation newActivity");
        try {
            cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            ComponentName component = PluginUtil.getComponent(intent);
            LoadedPlugin plugin = this.mPluginManager.getLoadedPlugin(component);
            // 1. 从intent中取出我们的目标Activity
            String targetClassName = component.getClassName();

            LogUtil.d(TAG, String.format("newActivity[%s : %s/%s]", className, component.getPackageName(), targetClassName));

            if (plugin != null) {
                // 2. 然后通过plugin的ClassLoader去加载
                Activity activity = mBase.newActivity(plugin.getClassLoader(), targetClassName, intent);
                activity.setIntent(intent);

                try {
                    /**
                     * 系统在创建完Activity对象后，紧接着创建Activity所附着的Context，在 createBaseContextForActivity 方法中创建出来的 ContextImpl appContext
                     * 使用的是宿主的Resources，如果不进行处理紧接着Activity会走入onCreate的生命周期中，此时插件加载资源的时候还是使用的宿主的资源，
                     * 而不是我们特意为插件所创建出来的Resources对象，则会发生找不到资源的问题。
                     * 解决方法：提前设置 ContextThemeWrapper 中的mResources对象，系统所创建的Resources对象其实就用不到了。
                     */
                    // for 4.1+
                    ReflectUtil.setField(ContextThemeWrapper.class, activity, "mResources", plugin.getResources());
                } catch (Exception ignored) {
                    // ignored.
                }

                return activity;
            }
        }

        return mBase.newActivity(cl, className, intent);
    }

    /**
     * 设置了修改了mResources、mBase（Context）、mApplication对象。
     * 以及设置一些可动态设置的属性，这里仅设置了屏幕方向。
     *
     * @param activity
     * @param icicle
     */
    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        LogUtil.d("VAInstrumentation callActivityOnCreate");
        final Intent intent = activity.getIntent();
        if (PluginUtil.isIntentFromPlugin(intent)) {
            Context base = activity.getBaseContext();
            try {
                LoadedPlugin plugin = this.mPluginManager.getLoadedPlugin(intent);
                ReflectUtil.setField(base.getClass(), base, "mResources", plugin.getResources());
                ReflectUtil.setField(ContextWrapper.class, activity, "mBase", plugin.getPluginContext());
                //TODO:
                //ReflectUtil.setField(Activity.class, activity, "mApplication", plugin.getApplication());
                ReflectUtil.setFieldNoException(ContextThemeWrapper.class, activity, "mBase", plugin.getPluginContext());

                // set screenOrientation
                ActivityInfo activityInfo = plugin.getActivityInfo(PluginUtil.getComponent(intent));
                if (activityInfo.screenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                    activity.setRequestedOrientation(activityInfo.screenOrientation);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        mBase.callActivityOnCreate(activity, icicle);
    }

    @Override
    public Context getContext() {
        return mBase.getContext();
    }

    @Override
    public Context getTargetContext() {
        return mBase.getTargetContext();
    }

    @Override
    public ComponentName getComponentName() {
        return mBase.getComponentName();
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }
}
