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

package com.dryseed.dsvirtualapk.core.utils;

import android.content.ComponentName;
import android.content.Intent;

/**
 * Created by renyugang on 16/8/15.
 */
public class PluginUtil {

    /**
     * 根据Intent构建ComponentName
     * 1. intent中存的插件package名
     * 2. intent中存的插件activity名
     *
     * @param intent
     * @return
     */
    public static ComponentName getComponent(Intent intent) {
        return new ComponentName(intent.getStringExtra(Constants.KEY_TARGET_PACKAGE),
                intent.getStringExtra(Constants.KEY_TARGET_ACTIVITY));
    }

    /**
     * 是否是插件的Intent
     *
     * @param intent
     * @return
     */
    public static boolean isIntentFromPlugin(Intent intent) {
        return intent.getBooleanExtra(Constants.KEY_IS_PLUGIN, false);
    }
}
