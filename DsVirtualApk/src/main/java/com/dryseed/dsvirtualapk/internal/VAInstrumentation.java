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

package com.dryseed.dsvirtualapk.internal;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dryseed.dsvirtualapk.PluginManager;
import com.dryseed.dsvirtualapk.utils.LogUtil;


/**
 * Created by renyugang on 16/8/10.
 */
public class VAInstrumentation extends Instrumentation {
    public static final String TAG = "VAInstrumentation";

    private Instrumentation mBase;

    PluginManager mPluginManager;

    public VAInstrumentation(PluginManager pluginManager, Instrumentation base) {
        this.mPluginManager = pluginManager;
        this.mBase = base;
    }

    @Override
    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        LogUtil.d("VAInstrumentation newActivity");
        return mBase.newActivity(cl, className, intent);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        LogUtil.d("VAInstrumentation callActivityOnCreate");
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
}
